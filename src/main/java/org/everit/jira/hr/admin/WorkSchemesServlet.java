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
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.jira.hr.admin.ManageSchemeComponent.SchemeDTO;
import org.everit.jira.hr.admin.SchemeUsersComponent.QUserSchemeEntityParameter;
import org.everit.jira.hr.admin.schema.qdsl.QExactWork;
import org.everit.jira.hr.admin.schema.qdsl.QUserWorkScheme;
import org.everit.jira.hr.admin.schema.qdsl.QWeekdayWork;
import org.everit.jira.hr.admin.schema.qdsl.QWorkScheme;
import org.everit.jira.hr.admin.schema.qdsl.util.DateRangeUtil;
import org.everit.web.partialresponse.PartialResponseBuilder;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.querydsl.core.types.Projections;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;

/**
 * Servlet that allows the users to view and edit working schemes.
 */
public class WorkSchemesServlet extends AbstractPageServlet {

  public static class WeekdayWorkDTO {

    public int duration;

    public Time startTime;

    public byte weekday;

    public long weekdayWorkId;

    public DayOfWeek getDayOfWeek() {
      // Convert the data stored in db to ISO format. DB format starts on Sunday, while ISO starts
      // on Monday.
      if (weekday == 1) {
        return DayOfWeek.SUNDAY;
      } else {
        return DayOfWeek.of(weekday - 1);
      }
    }

    public String getDayOfWeekDisplayName(final Locale locale) {
      return getDayOfWeek().getDisplayName(TextStyle.FULL, locale);
    }

    public int getDurationInMinutes() {
      return duration / SECONDS_IN_MINUTE;
    }

  }

  private static class WeekdayWorkDTOComparator implements Comparator<WeekdayWorkDTO> {

    @Override
    public int compare(final WeekdayWorkDTO o1, final WeekdayWorkDTO o2) {
      int result = o1.getDayOfWeek().compareTo(o2.getDayOfWeek());
      if (result != 0) {
        return result;
      }
      result = o1.startTime.compareTo(o2.startTime);
      if (result != 0) {
        return result;
      }
      return Long.compare(o1.weekdayWorkId, o2.weekdayWorkId);
    }

  }

  private static final int SECONDS_IN_MINUTE = 60;

  private static final long serialVersionUID = 5855299893731146143L;

  private static final Comparator<WeekdayWorkDTO> WEEKDAY_WORK_DTO_COMPARATOR =
      new WeekdayWorkDTOComparator();

  public static final String WORK_SCHEME_SCOPE_GLOBAL = "GLOBAL";

  private final ManageSchemeComponent manageSchemeComponent =
      new ManageSchemeComponent(this::listWorkSchemes, this::saveScheme, this::updateScheme,
          this::deleteScheme, this::applySchemeSelectionChange);

  private final SchemeUsersComponent schemeUsersComponent;

  private final TransactionTemplate transactionTemplate =
      ComponentAccessor.getOSGiComponentInstanceOfType(TransactionTemplate.class);

  public WorkSchemesServlet() {
    QUserSchemeEntityParameter qUserSchemeEntityParameter = new QUserSchemeEntityParameter();
    QUserWorkScheme userworkscheme = QUserWorkScheme.userWorkScheme;
    QWorkScheme workscheme = QWorkScheme.workScheme;
    qUserSchemeEntityParameter.userSchemeEntityPath = userworkscheme;
    qUserSchemeEntityParameter.schemeEntityPath = workscheme;
    qUserSchemeEntityParameter.schemeSchemeId = workscheme.workSchemeId;
    qUserSchemeEntityParameter.schemeName = workscheme.name;
    qUserSchemeEntityParameter.dateRangeId = userworkscheme.dateRangeId;
    qUserSchemeEntityParameter.userSchemeSchemeId = userworkscheme.workSchemeId;
    qUserSchemeEntityParameter.userId = userworkscheme.userId;
    qUserSchemeEntityParameter.userSchemeId = userworkscheme.userWorkSchemeId;

    schemeUsersComponent =
        new SchemeUsersComponent(qUserSchemeEntityParameter, transactionTemplate);
  }

  private void applySchemeSelectionChange(final HttpServletRequest request, final Long schemeId,
      final PartialResponseBuilder prb, final Locale locale) {
    Map<String, Object> vars = new HashMap<>();
    vars.put("schemeId", schemeId);
    vars.put("schemeUsers", schemeUsersComponent);
    vars.put("request", request);
    vars.put("locale", locale);
    if (schemeId != null) {
      vars.put("weekdayWorks", getWeekdayWorks(schemeId));
    }
    prb.replace("#work-schemes-tabs-container",
        (writer) -> pageTemplate.render(writer, vars, locale, "work-schemes-tabs-container"));
  }

  private byte convertDbDayOfWeek(final DayOfWeek dayOfWeek) {
    int index = dayOfWeek.getValue();
    if (index == DayOfWeek.SUNDAY.getValue()) {
      index = 1;
    } else {
      index++;
    }
    return (byte) index;
  }

  private void deleteScheme(final long schemeId) {
    transactionTemplate.execute(() -> querydslSupport.execute((connection, configuration) -> {
      removeAllUsersFromScheme(schemeId, connection, configuration);
      removeAllRegularWorkTimesFromScheme(schemeId, connection, configuration);
      removeAllExactWorkFromScheme(schemeId, connection, configuration);

      QWorkScheme qWorkScheme = QWorkScheme.workScheme;
      return new SQLDeleteClause(connection, configuration, qWorkScheme)
          .where(qWorkScheme.workSchemeId.eq(schemeId)).execute();
    }));
  }

  private void deleteWeekday(final long weekdayRecordId) {
    querydslSupport.execute((connection, configuration) -> {
      QWeekdayWork qWeekdayWork = QWeekdayWork.weekdayWork;
      new SQLDeleteClause(connection, configuration, qWeekdayWork)
          .where(qWeekdayWork.weekdayWorkId.eq(weekdayRecordId)).execute();
      return null;
    });
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
    Long userCount = schemeUsersComponent.schemeUserCount(schemeIdParameter);

    vars.put("schemeId", schemeIdParameter);
    vars.put("schemeUserCount", userCount);
    vars.put("schemeUsers", schemeUsersComponent);
    vars.put("locale", resp.getLocale());
    vars.put("manageSchemeComponent", manageSchemeComponent);
    vars.put("areYouSureDialogComponent", AreYouSureDialogComponent.INSTANCE);
    vars.put("deleteSchemaValidationComponent", DeleteSchemaValidationComponent.INSTANCE);

    if (schemeIdParameter != null) {
      long schemeId = Long.parseLong(schemeIdParameter);
      vars.put("weekdayWorks", getWeekdayWorks(schemeId));
    }

    String event = req.getParameter("event");
    if ("schemeChange".equals(event)) {
      try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
        prb.replace("#work-schemes-tabs-container", (writer) -> {
          pageTemplate.render(writer, vars, resp.getLocale(), "work-schemes-tabs-container");
        });
        prb.replace("#delete-schema-validation-dialog", (writer) -> {
          DeleteSchemaValidationComponent.INSTANCE.render(writer, resp.getLocale(), userCount);
        });
      }
      return;
    }

    vars.put("weekdays", DayOfWeek.values());
    vars.put("textStyle", TextStyle.FULL);

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
      case "newWeekday":
        processNewWeekday(req, resp);
        break;
      case "deleteWeekday":
        processDeleteWeekday(req, resp);
        break;
      default:
        break;
    }
  }

  @Override
  protected String getTemplateBase() {
    return "/META-INF/pages/work_schemes";
  }

  private Set<WeekdayWorkDTO> getWeekdayWorks(final long workSchemeId) {

    List<WeekdayWorkDTO> resultSet = querydslSupport.execute((connection, configuration) -> {
      QWeekdayWork qWeekdayWork = QWeekdayWork.weekdayWork;
      return new SQLQuery<WeekdayWorkDTO>(connection, configuration)
          .select(
              Projections.fields(WeekdayWorkDTO.class, qWeekdayWork.weekdayWorkId,
                  qWeekdayWork.weekday, qWeekdayWork.startTime, qWeekdayWork.duration))
          .from(qWeekdayWork)
          .where(qWeekdayWork.workSchemeId.eq(workSchemeId))
          .fetch();
    });
    TreeSet<WeekdayWorkDTO> weekdayWorks = new TreeSet<WeekdayWorkDTO>(WEEKDAY_WORK_DTO_COMPARATOR);
    weekdayWorks.addAll(resultSet);

    return weekdayWorks;
  }

  @Override
  protected boolean isWebSudoNecessary() {
    return true;
  }

  private Collection<SchemeDTO> listWorkSchemes() {
    return querydslSupport.execute((connection, configuration) -> {
      QWorkScheme qWorkScheme = QWorkScheme.workScheme;
      return new SQLQuery<SchemeDTO>(connection, configuration)
          .select(Projections.fields(SchemeDTO.class, qWorkScheme.workSchemeId.as("schemeId"),
              qWorkScheme.name.as("name")))
          .from(qWorkScheme)
          .where(qWorkScheme.scope.eq(WORK_SCHEME_SCOPE_GLOBAL))
          .orderBy(qWorkScheme.name.asc())
          .fetch();
    });
  }

  private void processDeleteWeekday(final HttpServletRequest req, final HttpServletResponse resp)
      throws IOException {
    long schemeId = Long.parseLong(req.getParameter("schemeId"));
    long weekdayRecordId = Long.parseLong(req.getParameter("weekdayRecordId"));

    deleteWeekday(weekdayRecordId);

    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      renderAlertOnPrb("Record deleted", "info", prb, resp.getLocale());
      renderWeekdayTableOnPrb(req, resp, schemeId, prb);
    }
  }

  private void processNewWeekday(final HttpServletRequest req, final HttpServletResponse resp)
      throws IOException {
    long schemeId = Long.parseLong(req.getParameter("schemeId"));
    int weekdayIndex = Integer.parseInt(req.getParameter("weekday"));
    String startTimeParam = req.getParameter("start-time");
    Time startTime = Time.valueOf(startTimeParam + ':' + "00");
    int durationInSeconds = Integer.parseInt(req.getParameter("duration")) * SECONDS_IN_MINUTE;
    saveWeekday(schemeId, DayOfWeek.of(weekdayIndex), startTime, durationInSeconds);

    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      renderAlertOnPrb("Weekday record saved", "info", prb, resp.getLocale());
      renderWeekdayTableOnPrb(req, resp, schemeId, prb);
    }
  }

  private void removeAllExactWorkFromScheme(final long schemeId, final Connection connection,
      final Configuration configuration) {

    QExactWork qExactWork = QExactWork.exactWork;
    new SQLDeleteClause(connection, configuration, qExactWork)
        .where(qExactWork.workSchemeId.eq(schemeId)).execute();
  }

  private void removeAllRegularWorkTimesFromScheme(final long schemeId, final Connection connection,
      final Configuration configuration) {

    QWeekdayWork qWeekdayWork = QWeekdayWork.weekdayWork;
    new SQLDeleteClause(connection, configuration, qWeekdayWork)
        .where(qWeekdayWork.workSchemeId.eq(schemeId)).execute();
  }

  private void removeAllUsersFromScheme(final long schemeId, final Connection connection,
      final Configuration configuration) {

    final int batchSize = 100;
    QUserWorkScheme qUserWorkScheme = QUserWorkScheme.userWorkScheme;

    SQLQuery<Long> sqlQuery = new SQLQuery<Long>(connection, configuration)
        .select(qUserWorkScheme.dateRangeId).from(qUserWorkScheme)
        .where(qUserWorkScheme.workSchemeId.eq(schemeId)).limit(batchSize);

    List<Long> dateRangeIds = sqlQuery.fetch();
    DateRangeUtil dateRangeUtil = new DateRangeUtil(connection, configuration);

    while (!dateRangeIds.isEmpty()) {
      new SQLDeleteClause(connection, configuration, qUserWorkScheme)
          .where(qUserWorkScheme.dateRangeId.in(dateRangeIds)).execute();

      dateRangeUtil.removeDateRange(dateRangeIds.toArray(new Long[dateRangeIds.size()]));

      dateRangeIds = sqlQuery.fetch();
    }

  }

  private void renderAlertOnPrb(final String message, final String alertType,
      final PartialResponseBuilder prb, final Locale locale) {

    prb.append("#aui-message-bar",
        (writer) -> AlertComponent.INSTANCE.render(writer, message, alertType, locale));
  }

  private void renderWeekdayTableOnPrb(final HttpServletRequest req, final HttpServletResponse resp,
      final long schemeId, final PartialResponseBuilder prb) throws IOException {
    Map<String, Object> vars = createCommonVars(req, resp);
    vars.put("schemeId", schemeId);
    vars.put("weekdayWorks", getWeekdayWorks(schemeId));
    prb.replace("#weekday-table",
        (writer) -> pageTemplate.render(writer, vars, resp.getLocale(), "weekday-table"));
  }

  private long saveScheme(final String schemeName) {
    return querydslSupport.execute((connection, configuration) -> {
      QWorkScheme qWorkScheme = QWorkScheme.workScheme;
      return new SQLInsertClause(connection, configuration, qWorkScheme)
          .set(qWorkScheme.name, schemeName)
          .set(qWorkScheme.scope, WORK_SCHEME_SCOPE_GLOBAL)
          .executeWithKey(qWorkScheme.workSchemeId);
    });
  }

  private void saveWeekday(final long schemeId, final DayOfWeek dayOfWeek, final Time startTime,
      final int duration) {

    querydslSupport.execute((connection, configuration) -> {
      QWeekdayWork qWeekdayWork = QWeekdayWork.weekdayWork;
      new SQLInsertClause(connection, configuration, qWeekdayWork)
          .set(qWeekdayWork.workSchemeId, schemeId)
          .set(qWeekdayWork.weekday, convertDbDayOfWeek(dayOfWeek))
          .set(qWeekdayWork.startTime, startTime)
          .set(qWeekdayWork.duration, duration)
          .execute();
      return null;
    });
  }

  private void updateScheme(final SchemeDTO scheme) {
    querydslSupport.execute((connection, configuration) -> {
      QWorkScheme qWorkScheme = QWorkScheme.workScheme;
      return new SQLUpdateClause(connection, configuration, qWorkScheme)
          .set(qWorkScheme.name, scheme.name)
          .where(qWorkScheme.workSchemeId.eq(scheme.schemeId))
          .execute();
    });
  }
}
