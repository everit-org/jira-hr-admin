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
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.jira.configuration.plugin.ManageSchemeComponent.SchemeDTO;
import org.everit.jira.configuration.plugin.SchemeUsersComponent.QUserSchemeEntityParameter;
import org.everit.jira.configuration.plugin.schema.qdsl.QUserWorkScheme;
import org.everit.jira.configuration.plugin.schema.qdsl.QWorkScheme;
import org.everit.web.partialresponse.PartialResponseBuilder;

import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;

/**
 * Servlet that allows the users to view and edit working schemes.
 */
public class WorkSchemesServlet extends AbstractPageServlet {

  private static final long serialVersionUID = 5855299893731146143L;

  public static final String WORK_SCHEME_SCOPE_GLOBAL = "GLOBAL";

  private final ManageSchemeComponent manageSchemeComponent =
      new ManageSchemeComponent(this::listWorkSchemes, this::saveScheme, this::updateScheme,
          this::deleteScheme, this::applySchemeSelectionChange);

  private final SchemeUsersComponent schemeUsersComponent;

  public WorkSchemesServlet() {
    QUserSchemeEntityParameter qUserSchemeEntityParameter = new QUserSchemeEntityParameter();
    QUserWorkScheme userworkscheme = QUserWorkScheme.userWorkScheme;
    qUserSchemeEntityParameter.entityPath = userworkscheme;
    qUserSchemeEntityParameter.dateRangeId = userworkscheme.dateRangeId;
    qUserSchemeEntityParameter.schemeId = userworkscheme.workSchemeId;
    qUserSchemeEntityParameter.userId = userworkscheme.userId;
    qUserSchemeEntityParameter.userSchemeId = userworkscheme.userWorkSchemeId;

    schemeUsersComponent = new SchemeUsersComponent(qUserSchemeEntityParameter);
  }

  private void applySchemeSelectionChange(final Long schemeId, final PartialResponseBuilder prb,
      final Locale locale) {
    Map<String, Object> vars = new HashMap<>();
    prb.replace("#work-schemes-tabs",
        (writer) -> pageTemplate.render(writer, vars, locale, "work-schemes-tabs"));
  }

  private void deleteScheme(final long schemeId) {
    querydslSupport.execute((connection, configuration) -> {
      QWorkScheme qWorkScheme = QWorkScheme.workScheme;
      return new SQLDeleteClause(connection, configuration, qWorkScheme)
          .where(qWorkScheme.workSchemeId.eq(schemeId)).execute();
    });
  }

  @Override
  protected void doGetInternal(final HttpServletRequest req, final HttpServletResponse resp,
      final Map<String, Object> vars) throws ServletException, IOException {

    vars.put("manageSchemeComponent", manageSchemeComponent);
    vars.put("areYouSureDialogComponent", AreYouSureDialogComponent.INSTANCE);
    vars.put("schemeUsers", schemeUsersComponent);

    pageTemplate.render(resp.getWriter(), vars, resp.getLocale(), null);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {

    if (manageSchemeComponent.getSupportedActions().contains(req.getParameter("action"))) {
      manageSchemeComponent.processAction(req, resp);
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
              qWorkScheme.name_.as("name")))
          .from(qWorkScheme)
          .where(qWorkScheme.scope_.eq(WORK_SCHEME_SCOPE_GLOBAL))
          .fetch();
    });
  }

  private long saveScheme(final String schemeName) {
    return querydslSupport.execute((connection, configuration) -> {
      QWorkScheme qWorkScheme = QWorkScheme.workScheme;
      return new SQLInsertClause(connection, configuration, qWorkScheme)
          .set(qWorkScheme.name_, schemeName)
          .set(qWorkScheme.scope_, WORK_SCHEME_SCOPE_GLOBAL)
          .executeWithKey(qWorkScheme.workSchemeId);
    });
  }

  private void updateScheme(final SchemeDTO scheme) {
    querydslSupport.execute((connection, configuration) -> {
      QWorkScheme qWorkScheme = QWorkScheme.workScheme;
      return new SQLUpdateClause(connection, configuration, qWorkScheme)
          .set(qWorkScheme.name_, scheme.name)
          .where(qWorkScheme.workSchemeId.eq(scheme.schemeId))
          .execute();
    });
  }
}
