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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.jira.configuration.plugin.schema.qdsl.QSpecialIssue;
import org.everit.jira.configuration.plugin.schema.qdsl.QSpecialProject;
import org.everit.jira.querydsl.schema.QJiraissue;
import org.everit.jira.querydsl.schema.QProject;
import org.everit.jira.querydsl.support.QuerydslSupport;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;

import com.querydsl.core.Tuple;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;

/**
 * Selecting issues and projects to have special behavior.
 */
public class SpecialIssuesServlet extends AbstractPageServlet {

  private static final long serialVersionUID = -4733879091733857960L;

  private final QuerydslSupport querydslSupport;

  public SpecialIssuesServlet() {
    try {
      querydslSupport = new QuerydslSupportImpl();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void addSpecialElementToMap(final String specialty, final String element,
      final Map<String, Collection<String>> map) {

    Collection<String> elements = map.get(specialty);
    if (elements == null) {
      elements = new LinkedHashSet<>();
      map.put(specialty, elements);
    }
    elements.add(element);
  }

  private String convertCollectionToCommaSeparated(final Map<String, Collection<String>> specialMap,
      final String specialty) {

    Collection<String> collection = specialMap.get(specialty);
    if (collection == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (String specialElement : collection) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(specialElement);
    }
    return sb.toString();
  }

  @Override
  protected void doGetInternal(final HttpServletRequest req, final HttpServletResponse resp,
      final Map<String, Object> vars) throws ServletException, IOException {

    String holidayIssuesParameter = req.getParameter("holidayIssues");
    String nonWorkingIssuesParameter = req.getParameter("nonWorkingIssues");
    String holidayProjectsParameter = req.getParameter("holidayProjects");
    String nonWorkingProjectsParameter = req.getParameter("nonWorkingProjects");

    if (holidayIssuesParameter != null && nonWorkingIssuesParameter != null
        && holidayProjectsParameter != null && nonWorkingProjectsParameter != null) {

      vars.put("holidayIssues", holidayIssuesParameter);
      vars.put("nonWorkingIssues", nonWorkingIssuesParameter);
      vars.put("holidayProjects", holidayProjectsParameter);
      vars.put("nonWorkingProjects", nonWorkingProjectsParameter);
    } else {
      Map<String, Collection<String>> specialIssues = querySpecialIssues();
      Map<String, Collection<String>> specialProjects = querySpecialProjects();

      vars.put("holidayIssues", convertCollectionToCommaSeparated(specialIssues, "holiday"));
      vars.put("nonWorkingIssues", convertCollectionToCommaSeparated(specialIssues, "nowork"));
      vars.put("holidayProjects", convertCollectionToCommaSeparated(specialProjects, "holiday"));
      vars.put("nonWorkingProjects", convertCollectionToCommaSeparated(specialProjects, "nowork"));
    }

    pageTemplate.render(resp.getWriter(), vars);
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {

    String action = req.getParameter("action");
    if ("save".equals(action)) {
      String holidayIssues = req.getParameter("holidayIssues");
      String holidayProjects = req.getParameter("holidayProjects");

      String nonWorkingIssues = req.getParameter("nonWorkingIssues");
      String nonWorkingProjects = req.getParameter("nonWorkingProjects");

      save(holidayIssues, holidayProjects, nonWorkingIssues, nonWorkingProjects);
    }

    doGet(req, resp);
  }

  @Override
  protected String getPageTemplateResourceURL() {
    return "/META-INF/pages/special_issues.html";
  }

  private Map<String, Collection<String>> querySpecialIssues() {
    return querydslSupport.execute((connection, configuration) -> {
      Map<String, Collection<String>> result = new HashMap<>();
      SQLQuery<String> query = new SQLQuery<>(connection, configuration);
      QSpecialIssue specialIssue = QSpecialIssue.specialIssue;
      QJiraissue jiraIssue = QJiraissue.jiraissue;
      QProject project = QProject.project;

      List<Tuple> resultSet = query.select(specialIssue.specialty, project.pkey, jiraIssue.issuenum)
          .from(specialIssue)
          .innerJoin(jiraIssue).on(specialIssue.issueId.eq(jiraIssue.id))
          .innerJoin(project).on(jiraIssue.project.eq(project.id))
          .fetch();

      for (Tuple tuple : resultSet) {
        String issueId = tuple.get(project.pkey) + tuple.get(jiraIssue.issuenum);
        String specialty = tuple.get(specialIssue.specialty);
        addSpecialElementToMap(specialty, issueId, result);
      }
      return result;
    });
  }

  private Map<String, Collection<String>> querySpecialProjects() {
    return querydslSupport.execute((connection, configuration) -> {
      Map<String, Collection<String>> result = new HashMap<>();
      SQLQuery<String> query = new SQLQuery<>(connection, configuration);
      QSpecialProject specialProject = QSpecialProject.specialProject;
      QProject project = QProject.project;

      List<Tuple> resultSet = query.select(specialProject.specialty, project.pkey)
          .from(specialProject)
          .innerJoin(project).on(specialProject.projectId.eq(project.id))
          .fetch();

      for (Tuple tuple : resultSet) {
        String projectKey = tuple.get(project.pkey);
        String specialty = tuple.get(specialProject.specialty);
        addSpecialElementToMap(specialty, projectKey, result);
      }
      return result;
    });
  }

  private void save(final String holidayIssues, final String holidayProjects,
      final String nonWorkingIssues,
      final String nonWorkingProjects) {

    saveSpecialIssues(holidayIssues, "holiday");
    saveSpecialIssues(holidayIssues, "nowork");
  }

  private void saveSpecialIssues(final String issues, final String specialty) {
    if (issues == null || issues.isEmpty()) {
      querydslSupport.execute((connection,
          configuration) -> new SQLDeleteClause(connection, configuration,
              QSpecialIssue.specialIssue)
                  .where(QSpecialIssue.specialIssue.specialty.eq(specialty)));
      return;
    }

    String[] issueKeyArray = issues.split(",");
    for (String issueKey : issueKeyArray) {
      String trimmedIssueKey = issueKey.trim();
      if (!trimmedIssueKey.isEmpty()) {
        String[] issueKeyParts = trimmedIssueKey.split("-");
        if (issueKeyParts.length != 2) {
          throw new IssueKeySyntaxException(trimmedIssueKey);
        }
      }
    }
  }

}
