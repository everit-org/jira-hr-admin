/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.jira.configuration.plugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.jira.configuration.plugin.schema.qdsl.QDateRange;
import org.everit.jira.configuration.plugin.schema.qdsl.QUserHolidayAmount;
import org.everit.jira.configuration.plugin.util.QueryResultWithCount;
import org.everit.jira.querydsl.schema.QCwdUser;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQuery;

/**
 * Assigning holiday amounts to users.
 */
public class UserHolidayAmountServlet extends AbstractPageServlet {

  public static class UserHolidayAmountDTO {
    public int amount;

    public String description;

    public String displayName;

    public Date endDateExcluded;

    public Date startDate;

    public long userHolidayAmountId;
  }

  private static final int PAGE_SIZE = 50;

  private static final long serialVersionUID = 1073648466982165361L;

  private final QuerydslSupport querydslSupport;

  private final TransactionTemplate transactionTemplate;

  public UserHolidayAmountServlet() {
    try {
      querydslSupport = new QuerydslSupportImpl();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    transactionTemplate =
        ComponentAccessor.getOSGiComponentInstanceOfType(TransactionTemplate.class);
  }

  @Override
  protected void doGetInternal(final HttpServletRequest req, final HttpServletResponse resp,
      final Map<String, Object> vars) throws ServletException, IOException {

    boolean currentTimeRanges = Boolean.valueOf(req.getParameter("currentTimeRanges"));
    String user = req.getParameter("user");
    int pageIndex = Integer.parseInt(Objects.toString(req.getParameter("pageIndex"), "1"));

    vars.put("currentTimeRanges", currentTimeRanges);
    vars.put("user", user);
    vars.put("pageIndex", pageIndex);
    vars.put("userHolidayAmounts", getUserHolidayAmounts(user, currentTimeRanges, pageIndex));

    super.doGetInternal(req, resp, vars);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {

    if (!checkWebSudo(req, resp)) {
      return;
    }

    String user = req.getParameter("user");
    String startDate = req.getParameter("start-date");
    String endDate = req.getParameter("end-date");
    String amount = req.getParameter("amount");
    String description = req.getParameter("description");

    System.out.println(user + startDate + endDate + amount + description);
  }

  @Override
  protected String getTemplateBase() {
    return "/META-INF/pages/user_holiday_amount";
  }

  private QueryResultWithCount<UserHolidayAmountDTO> getUserHolidayAmounts(final String user,
      final boolean currentTimeRanges, final int pageIndex) {

    return querydslSupport.execute((connection, configuration) -> {
      QUserHolidayAmount qUserHolidayAmount = QUserHolidayAmount.userHolidayAmount;
      QCwdUser qUser = QCwdUser.cwdUser;
      QDateRange qDateRange = QDateRange.dateRange;
      SQLQuery<UserHolidayAmountDTO> query = new SQLQuery<>(connection, configuration);
      query
          .select(Projections.fields(UserHolidayAmountDTO.class,
              qUserHolidayAmount.userHolidayAmountId, qUser.displayName,
              qDateRange.startDate, qDateRange.endDateExcluded, qUserHolidayAmount.amount,
              qUserHolidayAmount.description))
          .from(qUserHolidayAmount)
          .innerJoin(qUser).on(qUserHolidayAmount.userId.eq(qUser.id))
          .innerJoin(qDateRange).on(qUserHolidayAmount.dateRangeId.eq(qDateRange.dateRangeId));

      List<Predicate> predicates = new ArrayList<>();
      if (user != null) {
        predicates.add(qUser.userName.eq(user));
      }
      if (currentTimeRanges) {
        java.sql.Date currentDate = new java.sql.Date(new Date().getTime());
        predicates.add(
            qDateRange.startDate.loe(currentDate)
                .and(qDateRange.endDateExcluded.gt(currentDate)));
      }

      if (!predicates.isEmpty()) {
        query.where(predicates.toArray(new Predicate[0]));
      }

      long count = query.fetchCount();

      query.orderBy(qUser.userName.asc(), qDateRange.endDateExcluded.desc());
      query.limit(PAGE_SIZE);
      query.offset((pageIndex - 1) * PAGE_SIZE);

      List<UserHolidayAmountDTO> resultSet = query.fetch();

      return new QueryResultWithCount<>(resultSet, count);
    });
  }

  @Override
  protected boolean isWebSudoNecessary() {
    return true;
  }
}
