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
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.jira.configuration.plugin.ManageSchemeComponent.SchemeDTO;
import org.everit.jira.configuration.plugin.SchemeUsersComponent.QUserSchemeEntityParameter;
import org.everit.jira.configuration.plugin.schema.qdsl.QDateRange;
import org.everit.jira.configuration.plugin.schema.qdsl.QHolidayScheme;
import org.everit.jira.configuration.plugin.schema.qdsl.QPublicHoliday;
import org.everit.jira.configuration.plugin.schema.qdsl.QUserHolidayScheme;
import org.everit.jira.configuration.plugin.schema.qdsl.QWorkScheme;
import org.everit.web.partialresponse.PartialResponseBuilder;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;

/**
 * Managing holiday schemes.
 */
public class HolidaySchemesServlet extends AbstractPageServlet {

  public static class PublicHolidayDTO {

    public Date date;

    public String description;

    public long publicHolidayId;

    public Date replacementDate;

  }

  private static final long serialVersionUID = 1073648466982165361L;

  private final ManageSchemeComponent manageSchemeComponent =
      new ManageSchemeComponent(this::listHolidaySchemes, this::saveScheme, this::updateScheme,
          this::deleteScheme, this::applySchemeSelectionChange);

  private final SchemeUsersComponent schemeUsersComponent;

  public HolidaySchemesServlet() {
    QUserSchemeEntityParameter qUserSchemeEntityParameter = new QUserSchemeEntityParameter();
    QUserHolidayScheme userworkscheme = QUserHolidayScheme.userHolidayScheme;
    QWorkScheme workscheme = QWorkScheme.workScheme;
    qUserSchemeEntityParameter.userSchemeEntityPath = userworkscheme;
    qUserSchemeEntityParameter.schemeEntityPath = workscheme;
    qUserSchemeEntityParameter.schemeSchemeId = workscheme.workSchemeId;
    qUserSchemeEntityParameter.schemeName = workscheme.name;
    qUserSchemeEntityParameter.dateRangeId = userworkscheme.dateRangeId;
    qUserSchemeEntityParameter.userSchemeSchemeId = userworkscheme.holidaySchemeId;
    qUserSchemeEntityParameter.userId = userworkscheme.userId;
    qUserSchemeEntityParameter.userSchemeId = userworkscheme.userHolidaySchemeId;

    schemeUsersComponent =
        new SchemeUsersComponent(qUserSchemeEntityParameter, transactionTemplate);
  }

  private void appendPublicHolidaysToVars(final HttpServletRequest req,
      final Map<String, Object> vars,
      final Long schemeId) {
    List<Integer> publicHolidayYears = resolvePublicHolidayYears();
    vars.put("publicHolidayYears", publicHolidayYears);
    Integer publicHolidaySelectedYear = resolvePublicHolidaySelectedYear(req, publicHolidayYears);
    vars.put("publicHolidaySelectedYear", publicHolidaySelectedYear);
    vars.put("publicHolidays", listPublicHolidays(schemeId, publicHolidaySelectedYear));
  }

  private void applySchemeSelectionChange(final HttpServletRequest request, final Long schemeId,
      final PartialResponseBuilder prb, final Locale locale) {
    Map<String, Object> vars = new HashMap<>();
    vars.put("schemeId", schemeId);
    vars.put("schemeUsers", schemeUsersComponent);
    appendPublicHolidaysToVars(request, vars, schemeId);
    vars.put("request", request);
    vars.put("locale", locale);
    prb.replace("#holiday-schemes-tabs-container",
        (writer) -> pageTemplate.render(writer, vars, locale, "holiday-schemes-tabs-container"));
  }

  private void deleteScheme(final long schemeId) {
    transactionTemplate.execute(() -> querydslSupport.execute((connection, configuration) -> {
      removeAllUsersFromScheme(schemeId, connection, configuration);

      QHolidayScheme qHolidayScheme = QHolidayScheme.holidayScheme;
      return new SQLDeleteClause(connection, configuration, qHolidayScheme)
          .where(qHolidayScheme.holidaySchemeId.eq(schemeId)).execute();
    }));
  }

  @Override
  protected void doGetInternal(final HttpServletRequest req, final HttpServletResponse resp,
      final Map<String, Object> vars) throws ServletException, IOException {

    String action = req.getParameter("action");
    if (schemeUsersComponent.getSupportedActions().contains(action)) {
      schemeUsersComponent.processAction(req, resp);
      return;
    }

    String schemeIdParameter = req.getParameter("schemeId");
    Long schemeId = (schemeIdParameter != null) ? Long.parseLong(schemeIdParameter) : null;
    vars.put("schemeId", schemeId);
    vars.put("schemeUsers", schemeUsersComponent);
    vars.put("locale", resp.getLocale());

    if (schemeId != null) {
      appendPublicHolidaysToVars(req, vars, schemeId);
    }

    String event = req.getParameter("event");
    if ("schemeChange".equals(event)) {
      try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
        prb.replace("#holiday-schemes-tabs-container", (writer) -> {
          pageTemplate.render(writer, vars, resp.getLocale(), "holiday-schemes-tabs-container");
        });
      }
      return;
    }

    vars.put("manageSchemeComponent", manageSchemeComponent);
    vars.put("areYouSureDialogComponent", AreYouSureDialogComponent.INSTANCE);
    pageTemplate.render(resp.getWriter(), vars, resp.getLocale(), null);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {

    String action = req.getParameter("action");
    if (manageSchemeComponent.getSupportedActions().contains(action)) {
      manageSchemeComponent.processAction(req, resp);
      return;
    }
    if (schemeUsersComponent.getSupportedActions().contains(action)) {
      schemeUsersComponent.processAction(req, resp);
      return;
    }

    switch (action) {
      case "newPublicHoliday":
        processSavePublicHoliday(req, resp);

        break;

      default:
        break;
    }
  }

  @Override
  protected String getTemplateBase() {
    return "/META-INF/pages/holiday_schemes";
  }

  @Override
  protected boolean isWebSudoNecessary() {
    return true;
  }

  private Collection<SchemeDTO> listHolidaySchemes() {
    return querydslSupport.execute((connection, configuration) -> {
      QHolidayScheme qHolidayScheme = QHolidayScheme.holidayScheme;

      return new SQLQuery<SchemeDTO>(connection, configuration)
          .select(Projections.fields(SchemeDTO.class, qHolidayScheme.holidaySchemeId.as("schemeId"),
              qHolidayScheme.name.as("name")))
          .from(qHolidayScheme)
          .fetch();
    });
  }

  private Collection<PublicHolidayDTO> listPublicHolidays(final long schemeId, final Integer year) {
    if (year == null) {
      return Collections.emptySet();
    }
    return querydslSupport.execute((connection, configuration) -> {
      QPublicHoliday qPublicHoliday = QPublicHoliday.publicHoliday;
      return new SQLQuery<PublicHolidayDTO>(connection, configuration)
          .select(Projections.fields(PublicHolidayDTO.class, qPublicHoliday.publicHolidayId,
              qPublicHoliday.date, qPublicHoliday.replacementDate, qPublicHoliday.description))
          .from(qPublicHoliday)
          .where(qPublicHoliday.holidaySchemeId.eq(schemeId).and(qPublicHoliday.date.year().eq(year)
              .or(qPublicHoliday.replacementDate.year().eq(year))))
          .orderBy(qPublicHoliday.date.asc())
          .fetch();
    });
  }

  private void processSavePublicHoliday(final HttpServletRequest req,
      final HttpServletResponse resp) {
    Date date = Date.valueOf(req.getParameter("date"));
    Date replacementDate = Date.valueOf(req.getParameter("replacementDate"));
    String description = req.getParameter("description");

    savePublicHoliday(date, replacementDate, description);
  }

  private void removeAllUsersFromScheme(final long schemeId, final Connection connection,
      final Configuration configuration) {

    final int batchSize = 100;
    QUserHolidayScheme qUserHolidayScheme = QUserHolidayScheme.userHolidayScheme;

    SQLQuery<Long> sqlQuery = new SQLQuery<Long>(connection, configuration)
        .select(qUserHolidayScheme.dateRangeId).from(qUserHolidayScheme)
        .where(qUserHolidayScheme.holidaySchemeId.eq(schemeId)).limit(batchSize);

    List<Long> dateRangeIds = sqlQuery.fetch();

    while (!dateRangeIds.isEmpty()) {
      new SQLDeleteClause(connection, configuration, qUserHolidayScheme)
          .where(qUserHolidayScheme.dateRangeId.in(dateRangeIds)).execute();

      new SQLDeleteClause(connection, configuration, QDateRange.dateRange)
          .where(QDateRange.dateRange.dateRangeId.in(dateRangeIds));
      dateRangeIds = sqlQuery.fetch();
    }

  }

  private Integer resolvePublicHolidaySelectedYear(final HttpServletRequest req,
      final List<Integer> publicHolidayYears) {
    if (publicHolidayYears.isEmpty()) {
      return null;
    }
    String parameter = req.getParameter("publicHolidaySelectedYear");
    Integer selectedYear = null;
    if (parameter != null) {
      selectedYear = Integer.valueOf(parameter);
      if (!publicHolidayYears.contains(selectedYear)) {
        selectedYear = null;
      }
    }

    if (selectedYear == null) {
      int currentYear = LocalDate.now().getYear();
      if (publicHolidayYears.contains(currentYear)) {
        selectedYear = currentYear;
      } else {
        selectedYear = publicHolidayYears.get(0);
      }
    }
    return selectedYear;
  }

  private List<Integer> resolvePublicHolidayYears() {
    return querydslSupport.execute((connection, configuration) -> {
      QPublicHoliday qPublicHoliday = QPublicHoliday.publicHoliday;
      NumberExpression<Integer> yearExpression =
          qPublicHoliday.date.year().as("public_holiday_year");
      return new SQLQuery<Integer>(connection, configuration)
          .select(yearExpression)
          .from(qPublicHoliday)
          .groupBy(yearExpression)
          .orderBy(yearExpression.asc())
          .fetch();
    });
  }

  private void savePublicHoliday(final Date date, final Date replacementDate,
      final String description) {

    querydslSupport.execute((connection, configuration) -> {
      QPublicHoliday qPublicHoliday = QPublicHoliday.publicHoliday;
      SQLInsertClause insertClause = new SQLInsertClause(connection, configuration, qPublicHoliday)
          .set(qPublicHoliday.date, date);
      if (replacementDate != null) {
        insertClause.set(qPublicHoliday.replacementDate, date);
      }
      return null;
    });
  }

  private long saveScheme(final String schemeName) {
    return querydslSupport.execute((connection, configuration) -> {
      QHolidayScheme qHolidayScheme = QHolidayScheme.holidayScheme;
      return new SQLInsertClause(connection, configuration, qHolidayScheme)
          .set(qHolidayScheme.name, schemeName)
          .executeWithKey(qHolidayScheme.holidaySchemeId);
    });
  }

  private void updateScheme(final SchemeDTO scheme) {
    querydslSupport.execute((connection, configuration) -> {
      QHolidayScheme qHolidayScheme = QHolidayScheme.holidayScheme;
      return new SQLUpdateClause(connection, configuration, qHolidayScheme)
          .set(qHolidayScheme.name, scheme.name)
          .where(qHolidayScheme.holidaySchemeId.eq(scheme.schemeId))
          .execute();
    });
  }
}
