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
import java.util.Collection;
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
import org.everit.jira.configuration.plugin.schema.qdsl.QUserWorkScheme;
import org.everit.jira.configuration.plugin.schema.qdsl.QWeekdayWork;
import org.everit.jira.configuration.plugin.schema.qdsl.QWorkScheme;
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

  }

  private static final long serialVersionUID = 5855299893731146143L;

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

  private void addWeekdaysTableToVariables(final long schemeId, final Map<String, Object> vars) {
    querydslSupport.execute((connection, configuration) -> {
      QWeekdayWork qWeekdayWork = QWeekdayWork.weekdayWork;
      new SQLQuery<WeekdayWorkDTO>(connection, configuration)
          .select(Projections.fields(WeekdayWorkDTO.class, qWeekdayWork.duration_));
      // TODO
      return null;
    });
  }

  private void applySchemeSelectionChange(final HttpServletRequest request, final Long schemeId,
      final PartialResponseBuilder prb, final Locale locale) {
    Map<String, Object> vars = new HashMap<>();
    vars.put("schemeId", schemeId);
    vars.put("schemeUsers", schemeUsersComponent);
    vars.put("request", request);
    vars.put("locale", locale);
    prb.replace("#work-schemes-tabs-container",
        (writer) -> pageTemplate.render(writer, vars, locale, "work-schemes-tabs-container"));
  }

  private void deleteScheme(final long schemeId) {
    transactionTemplate.execute(() -> querydslSupport.execute((connection, configuration) -> {
      removeAllUsersFromScheme(schemeId, connection, configuration);

      QWorkScheme qWorkScheme = QWorkScheme.workScheme;
      return new SQLDeleteClause(connection, configuration, qWorkScheme)
          .where(qWorkScheme.workSchemeId.eq(schemeId)).execute();
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
    vars.put("schemeId", schemeIdParameter);
    vars.put("schemeUsers", schemeUsersComponent);
    vars.put("locale", resp.getLocale());
    vars.put("manageSchemeComponent", manageSchemeComponent);
    vars.put("areYouSureDialogComponent", AreYouSureDialogComponent.INSTANCE);

    if (schemeIdParameter != null) {
      long schemeId = Long.parseLong(schemeIdParameter);
      addWeekdaysTableToVariables(schemeId, vars);

      String event = req.getParameter("event");
      if ("schemeChange".equals(event)) {
        try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
          prb.replace("#work-schemes-tabs-container", (writer) -> {
            pageTemplate.render(writer, vars, resp.getLocale(), "work-schemes-tabs-container");
          });
        }
        return;
      }
    }

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
  }

  @Override
  protected String getTemplateBase() {
    return "/META-INF/pages/work_schemes";
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

  private void removeAllUsersFromScheme(final long schemeId, final Connection connection,
      final Configuration configuration) {

    final int batchSize = 100;
    QUserWorkScheme qUserWorkScheme = QUserWorkScheme.userWorkScheme;

    SQLQuery<Long> sqlQuery = new SQLQuery<Long>(connection, configuration)
        .select(qUserWorkScheme.dateRangeId).from(qUserWorkScheme)
        .where(qUserWorkScheme.workSchemeId.eq(schemeId)).limit(batchSize);

    List<Long> dateRangeIds = sqlQuery.fetch();

    while (!dateRangeIds.isEmpty()) {
      new SQLDeleteClause(connection, configuration, qUserWorkScheme)
          .where(qUserWorkScheme.dateRangeId.in(dateRangeIds)).execute();

      new SQLDeleteClause(connection, configuration, QDateRange.dateRange)
          .where(QDateRange.dateRange.dateRangeId.in(dateRangeIds));
      dateRangeIds = sqlQuery.fetch();
    }

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
