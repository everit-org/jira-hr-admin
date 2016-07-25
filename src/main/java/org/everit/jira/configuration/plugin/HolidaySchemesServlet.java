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
import org.everit.jira.configuration.plugin.schema.qdsl.QHolidayScheme;
import org.everit.jira.configuration.plugin.schema.qdsl.QUserHolidayScheme;
import org.everit.jira.configuration.plugin.schema.qdsl.QWorkScheme;
import org.everit.web.partialresponse.PartialResponseBuilder;

import com.querydsl.core.types.Projections;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;

/**
 * Managing holiday schemes.
 */
public class HolidaySchemesServlet extends AbstractPageServlet {

  private static final long serialVersionUID = 1073648466982165361L;

  private final ManageSchemeComponent manageSchemeComponent =
      new ManageSchemeComponent(this::listWorkSchemes, this::saveScheme, this::updateScheme,
          this::deleteScheme, this::applySchemeSelectionChange);

  private final SchemeUsersComponent schemeUsersComponent;

  public HolidaySchemesServlet() {
    QUserSchemeEntityParameter qUserSchemeEntityParameter = new QUserSchemeEntityParameter();
    QUserHolidayScheme userworkscheme = QUserHolidayScheme.userHolidayScheme;
    QWorkScheme workscheme = QWorkScheme.workScheme;
    qUserSchemeEntityParameter.userSchemeEntityPath = userworkscheme;
    qUserSchemeEntityParameter.schemeEntityPath = workscheme;
    qUserSchemeEntityParameter.schemeSchemeId = workscheme.workSchemeId;
    qUserSchemeEntityParameter.schemeName = workscheme.name_;
    qUserSchemeEntityParameter.dateRangeId = userworkscheme.dateRangeId;
    qUserSchemeEntityParameter.userSchemeSchemeId = userworkscheme.holidaySchemeId;
    qUserSchemeEntityParameter.userId = userworkscheme.userId;
    qUserSchemeEntityParameter.userSchemeId = userworkscheme.userHolidaySchemeId;

    schemeUsersComponent =
        new SchemeUsersComponent(qUserSchemeEntityParameter, transactionTemplate);
  }

  private void applySchemeSelectionChange(final HttpServletRequest request, final Long schemeId,
      final PartialResponseBuilder prb,
      final Locale locale) {
    Map<String, Object> vars = new HashMap<>();
    vars.put("schemeId", schemeId);
    vars.put("schemeUsers", schemeUsersComponent);
    vars.put("request", request);
    prb.replace("#holiday-schemes-tabs-container",
        (writer) -> pageTemplate.render(writer, vars, locale, "holiday-schemes-tabs-container"));
  }

  private void deleteScheme(final long schemeId) {
    querydslSupport.execute((connection, configuration) -> {
      QHolidayScheme qHolidayScheme = QHolidayScheme.holidayScheme;
      return new SQLDeleteClause(connection, configuration, qHolidayScheme)
          .where(qHolidayScheme.holidaySchemeId.eq(schemeId)).execute();
    });
  }

  @Override
  protected void doGetInternal(final HttpServletRequest req, final HttpServletResponse resp,
      final Map<String, Object> vars) throws ServletException, IOException {

    vars.put("manageSchemeComponent", manageSchemeComponent);
    vars.put("areYouSureDialogComponent", AreYouSureDialogComponent.INSTANCE);
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
    return "/META-INF/pages/holiday_schemes";
  }

  @Override
  protected boolean isWebSudoNecessary() {
    return true;
  }

  private Collection<SchemeDTO> listWorkSchemes() {
    return querydslSupport.execute((connection, configuration) -> {
      QHolidayScheme qHolidayScheme = QHolidayScheme.holidayScheme;
      return new SQLQuery<SchemeDTO>(connection, configuration)
          .select(Projections.fields(SchemeDTO.class, qHolidayScheme.holidaySchemeId.as("schemeId"),
              qHolidayScheme.name_.as("name")))
          .from(qHolidayScheme)
          .fetch();
    });
  }

  private long saveScheme(final String schemeName) {
    return querydslSupport.execute((connection, configuration) -> {
      QHolidayScheme qHolidayScheme = QHolidayScheme.holidayScheme;
      return new SQLInsertClause(connection, configuration, qHolidayScheme)
          .set(qHolidayScheme.name_, schemeName)
          .executeWithKey(qHolidayScheme.holidaySchemeId);
    });
  }

  private void updateScheme(final SchemeDTO scheme) {
    querydslSupport.execute((connection, configuration) -> {
      QHolidayScheme qHolidayScheme = QHolidayScheme.holidayScheme;
      return new SQLUpdateClause(connection, configuration, qHolidayScheme)
          .set(qHolidayScheme.name_, scheme.name)
          .where(qHolidayScheme.holidaySchemeId.eq(scheme.schemeId))
          .execute();
    });
  }
}
