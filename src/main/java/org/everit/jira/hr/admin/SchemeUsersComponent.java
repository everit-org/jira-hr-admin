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
package org.everit.jira.hr.admin;

import java.io.StringWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.everit.jira.hr.admin.schema.qdsl.QDateRange;
import org.everit.jira.hr.admin.schema.qdsl.util.DateRangeUtil;
import org.everit.jira.hr.admin.util.AvatarUtil;
import org.everit.jira.hr.admin.util.DateUtil;
import org.everit.jira.hr.admin.util.LocalizedTemplate;
import org.everit.jira.hr.admin.util.QueryResultWithCount;
import org.everit.jira.hr.admin.util.QueryUtil;
import org.everit.jira.querydsl.schema.QAvatar;
import org.everit.jira.querydsl.schema.QCwdUser;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;
import org.everit.web.partialresponse.PartialResponseBuilder;

import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Coalesce;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;

public class SchemeUsersComponent {

  public static class QUserSchemeEntityParameter {
    public NumberPath<Long> dateRangeId;

    public RelationalPath<?> schemeEntityPath;

    public StringExpression schemeName;

    public NumberPath<Long> schemeSchemeId;

    public NumberPath<Long> userId;

    public RelationalPath<?> userSchemeEntityPath;

    public NumberPath<Long> userSchemeId;

    public NumberPath<Long> userSchemeSchemeId;
  }

  public static class SchemeUserDTO {

    public Long avatarId;

    public String avatarOwner;

    public long dateRangeId;

    public Date endDateExcluded;

    public Date startDate;

    public String userDisplayName;

    public String userName;

    public long userSchemeId;

    public Date getEndDate() {
      return DateUtil.addDays(endDateExcluded, -1);
    }

  }

  private static final int PAGE_SIZE = 500000;

  private static final Set<String> SUPPORTED_ACTIONS;

  private static final LocalizedTemplate TEMPLATE =
      new LocalizedTemplate("/META-INF/component/scheme_users",
          ManageSchemeComponent.class.getClassLoader());

  static {
    Set<String> supportedActions = new HashSet<>();
    supportedActions.add("scheme-user-savenew");
    supportedActions.add("scheme-user-delete");
    supportedActions.add("scheme-user-update");
    supportedActions.add("scheme-user-filter");
    SUPPORTED_ACTIONS = Collections.unmodifiableSet(supportedActions);
  }

  private final QuerydslSupport querydslSupport;
  private final QUserSchemeEntityParameter qUserSchemeEntityParameter;

  private final TransactionTemplate transactionTemplate;

  public SchemeUsersComponent(final QUserSchemeEntityParameter qUserSchemeEntityParameter,
      final TransactionTemplate transactionTemplate) {
    this.qUserSchemeEntityParameter = qUserSchemeEntityParameter;
    this.transactionTemplate = transactionTemplate;
    try {
      this.querydslSupport = new QuerydslSupportImpl();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void delete(final Long userSchemeId) {
    transactionTemplate.execute(() -> querydslSupport.execute((connection, configuration) -> {
      QDateRange qDateRange = QDateRange.dateRange;
      Long dateRangeId = new SQLQuery<Long>(connection, configuration)
          .select(qDateRange.dateRangeId)
          .from(qDateRange)
          .innerJoin(qUserSchemeEntityParameter.userSchemeEntityPath)
          .on(qUserSchemeEntityParameter.dateRangeId.eq(qDateRange.dateRangeId))
          .where(qUserSchemeEntityParameter.userSchemeId.eq(userSchemeId))
          .fetchOne();

      new SQLDeleteClause(connection, configuration,
          qUserSchemeEntityParameter.userSchemeEntityPath)
              .where(qUserSchemeEntityParameter.userSchemeId.eq(userSchemeId))
              .execute();

      new DateRangeUtil(connection, configuration).removeDateRange(dateRangeId);
      return null;
    }));
  }

  private Set<String> getSchemeNamesWithOverlappingTimeRange(final Long userId,
      final Date startDate, final Date endDateExcluded, final Long userSchemeIdToExclude) {

    List<String> schemeNames = querydslSupport.execute((connection, configuration) -> {
      QDateRange qDateRange = QDateRange.dateRange;
      SQLQuery<String> query = new SQLQuery<>(connection, configuration)
          .select(qUserSchemeEntityParameter.schemeName)
          .from(qDateRange)
          .innerJoin(qUserSchemeEntityParameter.userSchemeEntityPath)
          .on(qDateRange.dateRangeId.eq(qUserSchemeEntityParameter.dateRangeId))
          .innerJoin(qUserSchemeEntityParameter.schemeEntityPath)
          .on(qUserSchemeEntityParameter.userSchemeSchemeId
              .eq(qUserSchemeEntityParameter.schemeSchemeId));

      List<Predicate> predicates = new ArrayList<>();
      predicates.add(qUserSchemeEntityParameter.userId.eq(userId));

      if (userSchemeIdToExclude != null) {
        predicates.add(qUserSchemeEntityParameter.userSchemeId.ne(userSchemeIdToExclude));
      }

      predicates.add(QueryUtil.dateRangeOverlaps(qDateRange, ConstantImpl.create(startDate),
          ConstantImpl.create(endDateExcluded)));

      query.where(predicates.toArray(new Predicate[predicates.size()]));
      return query.fetch();
    });
    return new TreeSet<>(schemeNames);
  }

  public Set<String> getSupportedActions() {
    return SUPPORTED_ACTIONS;
  }

  private Long getUserId(final String user) {
    if (user == null) {
      return null;
    }
    return querydslSupport.execute((connection, configuration) -> {
      QCwdUser qUser = QCwdUser.cwdUser;
      return new SQLQuery<Long>(connection, configuration)
          .select(qUser.id).from(qUser)
          .where(qUser.lowerUserName.eq(user.toLowerCase()))
          .fetchOne();
    });
  }

  public void processAction(final HttpServletRequest req, final HttpServletResponse resp) {
    String action = req.getParameter("action");

    switch (action) {
      case "scheme-user-filter":
        processFilter(req, resp);
        break;
      case "scheme-user-savenew":
        processSave(req, resp);
        break;
      case "scheme-user-update":
        processEdit(req, resp);
        break;
      case "scheme-user-delete":
        processDelete(req, resp);
        break;
      default:
        break;
    }
  }

  private void processDelete(final HttpServletRequest req, final HttpServletResponse resp) {
    Long userSchemeId = Long.parseLong(req.getParameter("scheme-user-userscheme-id"));
    delete(userSchemeId);
    Long userCount = schemeUserCount(req.getParameter("schemeId"));

    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      prb.replace("#scheme-user-table", render(req, resp.getLocale(), "scheme-user-table"));
      prb.replace("#delete-schema-validation-dialog", (writer) -> {
        DeleteSchemaValidationComponent.INSTANCE.render(writer, resp.getLocale(), userCount);
      });
    }
  }

  private void processEdit(final HttpServletRequest req, final HttpServletResponse resp) {
    long recordId = Long.parseLong(req.getParameter("record-id"));
    long schemeId = Long.parseLong(req.getParameter("schemeId"));
    String userName = req.getParameter("user");
    Date startDate = Date.valueOf(req.getParameter("start-date"));
    Date endDate = Date.valueOf(req.getParameter("end-date"));
    Date endDateExcluded = DateUtil.addDays(endDate, 1);

    Long userId = getUserId(userName);
    if (userId == null) {
      renderAlert("User does not exist", "error", resp);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (startDate.compareTo(endDate) > 0) {
      renderAlert("Start date must not be after end date", "error", resp);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    Set<String> schemeNamesWithOverlappingTimeRange =
        getSchemeNamesWithOverlappingTimeRange(userId, startDate, endDateExcluded, recordId);

    if (!schemeNamesWithOverlappingTimeRange.isEmpty()) {
      renderAlert(
          "The user is assigned overlapping with the specified date range to the"
              + " following scheme(s): " + schemeNamesWithOverlappingTimeRange.toString(),
          "error", resp);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    update(recordId, schemeId, userId, startDate, endDateExcluded);

    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      renderAlertOnPrb("Updating user successful", "info", prb, resp.getLocale());
      prb.replace("#scheme-user-table", render(req, resp.getLocale(), "scheme-user-table"));
    }
  }

  private void processFilter(final HttpServletRequest req, final HttpServletResponse resp) {
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      prb.replace("#scheme-user-table", render(req, resp.getLocale(), "scheme-user-table"));
    }
  }

  private void processSave(final HttpServletRequest req, final HttpServletResponse resp) {
    long schemeId = Long.parseLong(req.getParameter("schemeId"));
    String userName = req.getParameter("user");
    Date startDate = Date.valueOf(req.getParameter("start-date"));
    Date endDate = Date.valueOf(req.getParameter("end-date"));
    Date endDateExcluded = DateUtil.addDays(endDate, 1);

    Long userId = getUserId(userName);
    if (userId == null) {
      renderAlert("User does not exist", "error", resp);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    if (startDate.compareTo(endDate) > 0) {
      renderAlert("Start date must not be after end date", "error", resp);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    Set<String> schemeNamesWithOverlappingTimeRange =
        getSchemeNamesWithOverlappingTimeRange(userId, startDate, endDateExcluded, null);
    if (!schemeNamesWithOverlappingTimeRange.isEmpty()) {
      renderAlert(
          "The user is assigned overlapping with the specified date range to the"
              + " following scheme(s): " + schemeNamesWithOverlappingTimeRange.toString(),
          "error", resp);
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    save(schemeId, userName, startDate, endDateExcluded);
    Long userCount = schemeUserCount(String.valueOf(schemeId));
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      renderAlertOnPrb("Assiging user successful", "info", prb, resp.getLocale());
      prb.replace("#scheme-user-table", render(req, resp.getLocale(), "scheme-user-table"));
      prb.replace("#delete-schema-validation-dialog", (writer) -> {
        DeleteSchemaValidationComponent.INSTANCE.render(writer, resp.getLocale(), userCount);
      });
    }
  }

  private QueryResultWithCount<SchemeUserDTO> querySchemeUsers(final int pageIndex,
      final long schemeId, final String userName, final boolean currentTimeRanges) {
    return querydslSupport.execute((connection, configuration) -> {
      QDateRange qDateRange = QDateRange.dateRange;
      QCwdUser qCwdUser = QCwdUser.cwdUser;

      SQLQuery<SchemeUserDTO> query = new SQLQuery<>(connection, configuration);
      query.from(qUserSchemeEntityParameter.userSchemeEntityPath)
          .innerJoin(qDateRange)
          .on(qDateRange.dateRangeId.eq(qUserSchemeEntityParameter.dateRangeId))
          .innerJoin(qCwdUser).on(qCwdUser.id.eq(qUserSchemeEntityParameter.userId));

      QAvatar qAvatar = AvatarUtil.joinAvatarToCwdUser(query, qCwdUser, "avatar");

      List<Predicate> predicates = new ArrayList<>();
      predicates.add(qUserSchemeEntityParameter.userSchemeSchemeId.eq(schemeId));

      if (currentTimeRanges) {
        Date currentDate = new Date(new java.util.Date().getTime());
        predicates.add(qDateRange.startDate.loe(currentDate)
            .and(qDateRange.endDateExcluded.gt(currentDate)));
      }

      if (userName != null) {
        predicates.add(qCwdUser.userName.eq(userName));
      }

      Expression<Long> defaultAvatarId = ConstantImpl.create(AvatarUtil.DEFAULT_AVATAR_ID);

      StringExpression userDisplayNameExpression = qCwdUser.displayName.as("userDisplayName");
      query.select(Projections.fields(SchemeUserDTO.class,
          qUserSchemeEntityParameter.userSchemeId.as("userSchemeId"), qDateRange.dateRangeId,
          qCwdUser.userName, userDisplayNameExpression, qDateRange.startDate,
          qDateRange.endDateExcluded,
          new Coalesce<>(Long.class, qAvatar.id, defaultAvatarId).as("avatarId"),
          qAvatar.owner.as("avatarOwner")));

      query.where(predicates.toArray(new Predicate[0]));

      long count = query.fetchCount();

      query.orderBy(userDisplayNameExpression.asc(), qDateRange.startDate.desc());
      long offset = PAGE_SIZE * (pageIndex - 1);
      if (offset >= count) {
        offset = PAGE_SIZE * (count / PAGE_SIZE - 1);
        if (offset < 0) {
          offset = 0;
        }
      }

      List<SchemeUserDTO> resultSet;
      if (offset >= count) {
        resultSet = Collections.emptyList();
      } else {
        resultSet = query.limit(PAGE_SIZE).offset(offset).fetch();
      }

      QueryResultWithCount<SchemeUserDTO> result = new QueryResultWithCount<>(resultSet, count);
      return result;
    });
  }

  public String render(final HttpServletRequest request, final Locale locale) {
    return render(request, locale, "body");
  }

  public String render(final HttpServletRequest request, final Locale locale,
      final String fragment) {
    String userFilter = request.getParameter("schemeUsersUserFilter");
    boolean currentOnly = Boolean.parseBoolean(request.getParameter("schemeUsersCurrentFilter"));
    String schemeIdParam = request.getParameter("schemeId");
    Long schemeId = (schemeIdParam != null) ? Long.parseLong(schemeIdParam) : null;
    int pageIndex = Integer.parseInt(Objects.toString(request.getParameter("pageIndex"), "1"));

    QueryResultWithCount<SchemeUserDTO> schemeUsers;
    if (schemeId != null) {
      schemeUsers =
          querySchemeUsers(pageIndex, schemeId, userFilter, currentOnly);
    } else {
      schemeUsers = new QueryResultWithCount<>(Collections.emptyList(), 0);
    }

    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("request", request);
    vars.put("schemeUsers", schemeUsers);
    vars.put("userFilter", userFilter);
    vars.put("currentTimeRangesFilter", currentOnly);
    vars.put("pageIndex", pageIndex);

    StringWriter sw = new StringWriter();
    TEMPLATE.render(sw, vars, locale, fragment);
    return sw.toString();
  }

  private void renderAlert(final String message, final String alertType,
      final HttpServletResponse resp) {

    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      renderAlertOnPrb(message, alertType, prb, resp.getLocale());
    }
  }

  private void renderAlertOnPrb(final String message, final String alertType,
      final PartialResponseBuilder prb, final Locale locale) {

    prb.append("#aui-message-bar",
        (writer) -> AlertComponent.INSTANCE.render(writer, message, alertType, locale));
  }

  public String renderInitialFragments(final HttpServletRequest req,
      final HttpServletResponse resp) {
    StringWriter writer = new StringWriter();
    Map<String, Object> vars = new HashMap<>();
    vars.put("request", req);
    vars.put("response", resp);
    TEMPLATE.render(writer, vars, resp.getLocale(), "dialogs");
    return writer.toString();
  }

  private void save(final long schemeId, final String userName, final Date startDate,
      final Date endDateExcluded) {
    transactionTemplate.execute(() -> querydslSupport.execute((connection, configuration) -> {
      QCwdUser qCwdUser = QCwdUser.cwdUser;
      Long userId = new SQLQuery<Long>(connection, configuration)
          .select(qCwdUser.id)
          .from(qCwdUser)
          .where(qCwdUser.userName.eq(userName)).fetchOne();

      Long dateRangeId =
          new DateRangeUtil(connection, configuration).createDateRange(startDate, endDateExcluded);

      new SQLInsertClause(connection, configuration,
          qUserSchemeEntityParameter.userSchemeEntityPath)
              .set(qUserSchemeEntityParameter.dateRangeId, dateRangeId)
              .set(qUserSchemeEntityParameter.userId, userId)
              .set(qUserSchemeEntityParameter.userSchemeSchemeId, schemeId).execute();

      return null;
    }));
  }

  /**
   * ▼ Count users belonging to a scheme (work or holiday scheme).
   *
   * @param schemeIdString
   *          Scheme id in string, usually from request parameter.
   * @return If parameter null or empty, return 0. Otherwise, returns the number of users belonging
   *         to the specified schema.
   */
  public Long schemeUserCount(final String schemeIdString) {
    if (StringUtils.isEmpty(schemeIdString)) {
      return Long.valueOf(0);
    } else {
      Long schemeId = Long.valueOf(schemeIdString);
      return querydslSupport.execute((connection, configuration) -> {
        // select count(user_id) from everit_jira_user_work_scheme where work_scheme_id = schemeId;
        return new SQLQuery<Long>(connection, configuration)
            .from(qUserSchemeEntityParameter.userSchemeEntityPath)
            .where(qUserSchemeEntityParameter.userSchemeSchemeId.eq(schemeId))
            .fetchCount();
      });
    }
  }

  private void update(final long recordId, final long schemeId, final long userId,
      final Date startDate, final Date endDateExcluded) {
    transactionTemplate.execute(() -> querydslSupport.execute((connection, configuration) -> {

      Long dateRangeId = new SQLQuery<Long>(connection, configuration)
          .select(qUserSchemeEntityParameter.dateRangeId)
          .from(qUserSchemeEntityParameter.userSchemeEntityPath)
          .where(qUserSchemeEntityParameter.userSchemeId.eq(recordId)).fetchOne();

      new DateRangeUtil(connection, configuration).modifyDateRange(dateRangeId, startDate,
          endDateExcluded);

      new SQLUpdateClause(connection, configuration,
          qUserSchemeEntityParameter.userSchemeEntityPath)
              .set(qUserSchemeEntityParameter.userId, userId)
              .where(qUserSchemeEntityParameter.userSchemeId.eq(recordId));
      return null;
    }));

  }
}
