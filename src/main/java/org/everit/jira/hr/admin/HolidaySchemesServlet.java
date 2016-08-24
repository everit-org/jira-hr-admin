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

import org.everit.jira.hr.admin.ManageSchemeComponent.SchemeDTO;
import org.everit.jira.hr.admin.SchemeUsersComponent.QUserSchemeEntityParameter;
import org.everit.jira.hr.admin.schema.qdsl.QHolidayScheme;
import org.everit.jira.hr.admin.schema.qdsl.QPublicHoliday;
import org.everit.jira.hr.admin.schema.qdsl.QUserHolidayScheme;
import org.everit.jira.hr.admin.schema.qdsl.QWorkScheme;
import org.everit.jira.hr.admin.schema.qdsl.util.DateRangeUtil;
import org.everit.jira.hr.admin.util.DateUtil;
import org.everit.web.partialresponse.PartialResponseBuilder;

import com.querydsl.core.types.Predicate;
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
      final Map<String, Object> vars, final Long schemeId, final Integer year) {
    List<Integer> publicHolidayYears = resolvePublicHolidayYears(schemeId);
    vars.put("publicHolidayYears", publicHolidayYears);
    Integer publicHolidaySelectedYear =
        resolvePublicHolidaySelectedYear(req, publicHolidayYears, year);
    vars.put("publicHolidaySelectedYear", publicHolidaySelectedYear);
    vars.put("publicHolidays", listPublicHolidays(schemeId, publicHolidaySelectedYear));
  }

  private void applySchemeSelectionChange(final HttpServletRequest request, final Long schemeId,
      final PartialResponseBuilder prb, final Locale locale) {
    Map<String, Object> vars = new HashMap<>();

    if (schemeId == null) {
      vars.put("schemeId", null);
    } else {
      vars.put("schemeId", schemeId);
      vars.put("schemeUsers", schemeUsersComponent);
      appendPublicHolidaysToVars(request, vars, schemeId, null);
      vars.put("request", request);
      vars.put("locale", locale);
    }

    prb.replace("#holiday-schemes-tabs-container",
        (writer) -> pageTemplate.render(writer, vars, locale, "holiday-schemes-tabs-container"));
  }

  private boolean checkPublicHolidayOverlapping(final long schemeId, final Date date,
      final Date replacementDate, final Long publicHolidayRecordToExclude,
      final HttpServletResponse resp) {

    if (date.equals(replacementDate)) {
      renderAlertOnPrb("Public holiday and replacement day cannot be the same", "error",
          new PartialResponseBuilder(resp), resp.getLocale());
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return true;
    }

    return querydslSupport.execute((connection, configuration) -> {
      QPublicHoliday qPublicHoliday = QPublicHoliday.publicHoliday;
      Predicate[] predicates = new Predicate[(publicHolidayRecordToExclude == null) ? 2 : 2 + 1];
      predicates[0] = qPublicHoliday.holidaySchemeId.eq(schemeId);
      predicates[1] = qPublicHoliday.date.eq(date);

      if (publicHolidayRecordToExclude != null) {
        predicates[2] = qPublicHoliday.publicHolidayId.ne(publicHolidayRecordToExclude);
      }

      long count = new SQLQuery<Long>(connection, configuration)
          .select(qPublicHoliday.publicHolidayId.count())
          .from(qPublicHoliday)
          .where(predicates)
          .fetchOne();

      if (count > 0) {
        renderAlertOnPrb("There is another public holiday with the same date", "error",
            new PartialResponseBuilder(resp), resp.getLocale());
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return true;
      }

      if (replacementDate == null) {
        return false;
      }

      predicates[1] = qPublicHoliday.replacementDate.eq(replacementDate);

      count = new SQLQuery<Long>(connection, configuration)
          .select(qPublicHoliday.publicHolidayId.count())
          .from(qPublicHoliday)
          .where(predicates)
          .fetchOne();

      if (count > 0) {
        renderAlertOnPrb("There is another replacement day with the same date", "error",
            new PartialResponseBuilder(resp), resp.getLocale());
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        return true;
      }

      return false;
    });
  }

  private void deletePublicHoliday(final long publicHolidayId) {
    QPublicHoliday qPublicHoliday = QPublicHoliday.publicHoliday;
    querydslSupport.execute((connection, configuration) -> {
      new SQLDeleteClause(connection, configuration, qPublicHoliday)
          .where(qPublicHoliday.publicHolidayId.eq(publicHolidayId)).execute();
      return null;
    });

  }

  private void deleteScheme(final long schemeId) {
    transactionTemplate.execute(() -> querydslSupport.execute((connection, configuration) -> {
      removeAllUsersFromScheme(schemeId, connection, configuration);
      removeAllPublicHolidays(schemeId, connection, configuration);

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

    if ("publicHolidayYearChange".equals(action)) {
      int year = Integer.parseInt(req.getParameter("year"));
      renderPubliHolidayPanelOnPrb(req, resp, schemeId, new PartialResponseBuilder(resp), year);
      return;
    }

    Long userCount = schemeUsersComponent.schemeUserCount(schemeIdParameter);

    vars.put("schemeId", schemeId);
    vars.put("schemeUserCount", userCount);
    vars.put("schemeUsers", schemeUsersComponent);
    vars.put("locale", resp.getLocale());

    if (schemeId != null) {
      appendPublicHolidaysToVars(req, vars, schemeId, null);
    }

    String event = req.getParameter("event");
    if ("schemeChange".equals(event)) {
      try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
        prb.replace("#holiday-schemes-tabs-container", (writer) -> {
          pageTemplate.render(writer, vars, resp.getLocale(), "holiday-schemes-tabs-container");
        });
        prb.replace("#delete-schema-validation-dialog", (writer) -> {
          DeleteSchemaValidationComponent.INSTANCE.render(writer, resp.getLocale(), userCount);
        });
      }
      return;
    }

    vars.put("manageSchemeComponent", manageSchemeComponent);
    vars.put("areYouSureDialogComponent", AreYouSureDialogComponent.INSTANCE);
    vars.put("deleteSchemaValidationComponent", DeleteSchemaValidationComponent.INSTANCE);
    pageTemplate.render(resp.getWriter(), vars, resp.getLocale(), null);
  }

  @Override
  protected void doPostInternal(final HttpServletRequest req, final HttpServletResponse resp)
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
      case "updatePublicHoliday":
        processUpdatePublicHoliday(req, resp);
        break;
      case "publicHolidayDelete":
        processDeletePublicHoliday(req, resp);
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

  private void processDeletePublicHoliday(final HttpServletRequest req,
      final HttpServletResponse resp) throws IOException {
    long publicHolidayId = Long.parseLong(req.getParameter("publicHolidayId"));
    deletePublicHoliday(publicHolidayId);

    long schemeId = Long.parseLong(req.getParameter("schemeId"));
    int year = Integer.parseInt(req.getParameter("year"));
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      renderAlertOnPrb("Public holiday record deleted", "info", prb, resp.getLocale());
      renderPubliHolidayPanelOnPrb(req, resp, schemeId, prb, year);
    }
  }

  private void processSavePublicHoliday(final HttpServletRequest req,
      final HttpServletResponse resp) throws IOException {

    long schemeId = Long.parseLong(req.getParameter("schemeId"));
    Date date = Date.valueOf(req.getParameter("date"));
    Date replacementDate = DateUtil.parseDate(req.getParameter("replacementDate"));

    if (checkPublicHolidayOverlapping(schemeId, date, replacementDate, null, resp)) {
      return;
    }

    String description = req.getParameter("description");

    savePublicHoliday(schemeId, date, replacementDate, description);

    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      renderAlertOnPrb("Public holiday saved", "info", prb, resp.getLocale());
      renderPubliHolidayPanelOnPrb(req, resp, schemeId, prb, date.toLocalDate().getYear());
    }
  }

  private void processUpdatePublicHoliday(final HttpServletRequest req,
      final HttpServletResponse resp)
      throws IOException {
    long schemeId = Long.parseLong(req.getParameter("schemeId"));
    long publicHolidayId = Long.parseLong(req.getParameter("publicHolidayId"));
    Date date = Date.valueOf(req.getParameter("date"));
    Date replacementDate = DateUtil.parseDate(req.getParameter("replacementDate"));

    if (checkPublicHolidayOverlapping(schemeId, date, replacementDate, publicHolidayId, resp)) {
      return;
    }

    String description = req.getParameter("description");

    updatePublicHoliday(publicHolidayId, date, replacementDate, description);

    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      renderAlertOnPrb("Public holiday updated", "info", prb, resp.getLocale());

      int year = date.toLocalDate().getYear();
      renderPubliHolidayPanelOnPrb(req, resp, schemeId, prb, year);
    }
  }

  private void removeAllPublicHolidays(final long schemeId, final Connection connection,
      final Configuration configuration) {
    QPublicHoliday qPublicHoliday = QPublicHoliday.publicHoliday;
    new SQLDeleteClause(connection, configuration, qPublicHoliday)
        .where(qPublicHoliday.holidaySchemeId.eq(schemeId)).execute();
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

      new DateRangeUtil(connection, configuration)
          .removeDateRange(dateRangeIds.toArray(new Long[dateRangeIds.size()]));

      dateRangeIds = sqlQuery.fetch();
    }

  }

  private void renderAlertOnPrb(final String message, final String alertType,
      final PartialResponseBuilder prb, final Locale locale) {

    prb.append("#aui-message-bar",
        (writer) -> AlertComponent.INSTANCE.render(writer, message, alertType, locale));
  }

  private void renderPubliHolidayPanelOnPrb(final HttpServletRequest req,
      final HttpServletResponse resp,
      final long schemeId, final PartialResponseBuilder prb, final int year) throws IOException {
    Map<String, Object> vars = createCommonVars(req, resp);
    appendPublicHolidaysToVars(req, vars, schemeId, year);
    prb.replace("#public-holiday-panel",
        (writer) -> pageTemplate.render(writer, vars, resp.getLocale(), "public-holiday-panel"));
  }

  private Integer resolvePublicHolidaySelectedYear(final HttpServletRequest req,
      final List<Integer> publicHolidayYears, final Integer year) {

    if (year != null && publicHolidayYears.contains(year)) {
      return year;
    }

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

  private List<Integer> resolvePublicHolidayYears(final long schemeId) {
    return querydslSupport.execute((connection, configuration) -> {
      QPublicHoliday qPublicHoliday = QPublicHoliday.publicHoliday;
      NumberExpression<Integer> yearExpression =
          qPublicHoliday.date.year().as("public_holiday_year");
      return new SQLQuery<Integer>(connection, configuration)
          .select(yearExpression)
          .from(qPublicHoliday)
          .groupBy(yearExpression)
          .orderBy(yearExpression.asc())
          .where(qPublicHoliday.holidaySchemeId.eq(schemeId))
          .fetch();
    });
  }

  private void savePublicHoliday(final long schemeId, final Date date, final Date replacementDate,
      final String description) {

    querydslSupport.execute((connection, configuration) -> {
      QPublicHoliday qPublicHoliday = QPublicHoliday.publicHoliday;
      SQLInsertClause insertClause = new SQLInsertClause(connection, configuration, qPublicHoliday)
          .set(qPublicHoliday.date, date);
      if (replacementDate != null) {
        insertClause.set(qPublicHoliday.replacementDate, replacementDate);
      }
      if (description != null && !"".equals(description.trim())) {
        insertClause.set(qPublicHoliday.description, description);
      }
      insertClause.set(qPublicHoliday.holidaySchemeId, schemeId);
      insertClause.execute();
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

  private void updatePublicHoliday(final long publicHolidayId, final Date date,
      final Date replacementDate,
      final String description) {

    querydslSupport.execute((connection, configuration) -> {
      QPublicHoliday qPublicHoliday = QPublicHoliday.publicHoliday;
      SQLUpdateClause update = new SQLUpdateClause(connection, configuration, qPublicHoliday)
          .set(qPublicHoliday.date, date);

      if (replacementDate != null) {
        update.set(qPublicHoliday.replacementDate, replacementDate);
      } else {
        update.setNull(qPublicHoliday.replacementDate);
      }

      if (description != null) {
        update.set(qPublicHoliday.description, description);
      } else {
        update.setNull(qPublicHoliday.description);
      }

      update.where(qPublicHoliday.publicHolidayId.eq(publicHolidayId)).execute();
      return null;
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
