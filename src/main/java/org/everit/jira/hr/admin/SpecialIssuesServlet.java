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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.jira.hr.admin.schema.qdsl.QSpecialIssue;
import org.everit.jira.hr.admin.schema.qdsl.QSpecialProject;
import org.everit.jira.querydsl.schema.QJiraissue;
import org.everit.jira.querydsl.schema.QProject;
import org.everit.web.partialresponse.PartialResponseBuilder;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;

/**
 * Selecting issues and projects to have special behavior.
 */
public class SpecialIssuesServlet extends AbstractPageServlet {

  private static final long serialVersionUID = -4733879091733857960L;

  private void addInputDbContentsToVars(final Map<String, Object> vars) {
    Map<String, Collection<String>> specialIssues = querySpecialIssues();
    Map<String, Collection<String>> specialProjects = querySpecialProjects();

    vars.put("holidayIssues", convertCollectionToCommaSeparated(specialIssues, "holiday"));
    vars.put("nonWorkingIssues", convertCollectionToCommaSeparated(specialIssues, "nowork"));
    vars.put("holidayProjects", convertCollectionToCommaSeparated(specialProjects, "holiday"));
    vars.put("nonWorkingProjects", convertCollectionToCommaSeparated(specialProjects, "nowork"));
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

  private void checkNoProjectKeyMissing(final Collection<String> queriedProjectKeys,
      final Collection<String> returnedProjectKeys) {

    if (queriedProjectKeys.size() == returnedProjectKeys.size()) {
      return;
    }

    for (String queriedProjectKey : queriedProjectKeys) {
      if (!returnedProjectKeys.contains(queriedProjectKey)) {
        throw new MissingProjectKeyException(queriedProjectKey);
      }
    }
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
      addInputDbContentsToVars(vars);
    }

    pageTemplate.render(resp.getWriter(), vars, resp.getLocale(), null);
  }

  @Override
  protected void doPostInternal(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {

    String action = req.getParameter("action");
    if ("save".equals(action)) {
      String holidayIssues = req.getParameter("holidayIssues");
      String holidayProjects = req.getParameter("holidayProjects");

      String nonWorkingIssues = req.getParameter("nonWorkingIssues");
      String nonWorkingProjects = req.getParameter("nonWorkingProjects");
      try {
        save(holidayIssues, holidayProjects, nonWorkingIssues, nonWorkingProjects);
      } catch (MissingProjectKeyException e) {
        renderError("Project key not found", e.getProjectKey(), req, resp);
        return;
      } catch (IssueKeySyntaxException e) {
        renderError("Issue key is invalid", e.getIssueKey(), req, resp);
        return;
      } catch (MissingIssueException e) {
        renderError("Issue not found", e.getIssue(), req, resp);
        return;
      }
    }

    if ("XMLHttpRequest".equals(req.getHeader("X-Requested-With"))) {
      try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
        Map<String, Object> vars = createCommonVars(req, resp);
        addInputDbContentsToVars(vars);

        prb.replace("#specialIssuesFormBody",
            (writer) -> pageTemplate.render(writer, vars, resp.getLocale(),
                "specialIssuesFormBody"));

        prb.append("#specialIssuesFormBody", (writer) -> AlertComponent.INSTANCE
            .render(writer, "Saving changes successful", "info", resp.getLocale()));
      }
    } else {
      doGet(req, resp);
    }
  }

  @Override
  protected String getTemplateBase() {
    return "/META-INF/pages/special_issues";
  }

  @Override
  protected boolean isWebSudoNecessary() {
    return true;
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
        String issueId = tuple.get(project.pkey) + '-' + tuple.get(jiraIssue.issuenum);
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

  private void removeIssueFromMap(final Map<String, Collection<Long>> projectKeyIssueNumMap,
      final String projectKey, final Long issueNum) {

    Collection<Long> issueNums = projectKeyIssueNumMap.get(projectKey);
    issueNums.remove(issueNum);
    if (issueNums.isEmpty()) {
      projectKeyIssueNumMap.remove(projectKey);
    }
  }

  private void renderError(final String messageKey, final String targetObjectKey,
      final HttpServletRequest req, final HttpServletResponse resp) throws IOException {

    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      prb.append("#specialIssuesFormBody",
          (writer) -> AlertComponent.INSTANCE.render(writer, messageKey + ": " + targetObjectKey,
              "error", resp.getLocale()));
    }
  }

  private Collection<Long> resolveIssueIds(final String issuesString) {
    Map<String, Collection<Long>> projectKeyIssueNumMap =
        resolveProjectKeyIssueNumMap(issuesString);

    if (projectKeyIssueNumMap.isEmpty()) {
      return Collections.emptySet();
    }

    Map<String, Long> projectKeyIdMap = resolveProjectKeyIdMap(projectKeyIssueNumMap.keySet());

    return querydslSupport.execute((connection, configuration) -> {

      // Create the predicate that selects records for all issues
      BooleanExpression predicate = null;
      for (Entry<String, Collection<Long>> projectKeyIssueNumEntry : projectKeyIssueNumMap
          .entrySet()) {

        String projectKey = projectKeyIssueNumEntry.getKey();
        Long projectId = projectKeyIdMap.get(projectKey);
        Collection<Long> issueNums = projectKeyIssueNumEntry.getValue();

        BooleanExpression bexpr = QJiraissue.jiraissue.project.eq(projectId)
            .and(QJiraissue.jiraissue.issuenum.in(issueNums));

        if (predicate == null) {
          predicate = bexpr;
        } else {
          predicate = predicate.or(bexpr);
        }
      }

      // Do the query
      List<Tuple> tuples = new SQLQuery<Tuple>(connection, configuration)
          .select(QProject.project.pkey, QJiraissue.jiraissue.issuenum, QJiraissue.jiraissue.id)
          .from(QJiraissue.jiraissue)
          .innerJoin(QProject.project).on(QJiraissue.jiraissue.project.eq(QProject.project.id))
          .where(predicate).fetch();

      Set<Long> result = new LinkedHashSet<>();
      for (Tuple tuple : tuples) {
        String projectKey = tuple.get(QProject.project.pkey);
        Long issueNum = tuple.get(QJiraissue.jiraissue.issuenum);
        Long issueId = tuple.get(QJiraissue.jiraissue.id);
        result.add(issueId);
        removeIssueFromMap(projectKeyIssueNumMap, projectKey, issueNum);
      }
      if (!projectKeyIssueNumMap.isEmpty()) {
        Entry<String, Collection<Long>> entry = projectKeyIssueNumMap.entrySet().iterator().next();
        String issue = entry.getKey() + '-' + entry.getValue().iterator().next();
        throw new MissingIssueException(issue);
      }
      return result;
    });
  }

  private Collection<Long> resolveProjectIds(final String projectsString) {
    if ("".equals(projectsString.trim())) {
      return Collections.emptySet();
    }
    String[] projectKeyArray = projectsString.split(",");
    Set<String> projectKeys = new HashSet<>();
    for (String projectKey : projectKeyArray) {
      String trimmed = projectKey.trim();
      if (!"".equals(trimmed)) {
        projectKeys.add(trimmed);
      }
    }

    return resolveProjectKeyIdMap(projectKeys).values();
  }

  private Map<String, Long> resolveProjectKeyIdMap(final Set<String> projectKeys) {
    Map<String, Long> result = querydslSupport.execute((connection, configuration) -> {
      List<Tuple> resultSet = new SQLQuery<>(connection, configuration)
          .select(QProject.project.pkey, QProject.project.id)
          .from(QProject.project)
          .where(QProject.project.pkey.in(projectKeys)).fetch();

      Map<String, Long> localResult = new HashMap<>();
      for (Tuple tuple : resultSet) {
        localResult.put(tuple.get(QProject.project.pkey), tuple.get(QProject.project.id));
      }
      return localResult;
    });

    checkNoProjectKeyMissing(projectKeys, result.keySet());
    return result;
  }

  private Map<String, Collection<Long>> resolveProjectKeyIssueNumMap(final String issues) {
    if (issues == null || issues.trim().isEmpty()) {
      return Collections.emptyMap();
    }

    Map<String, Collection<Long>> result = new LinkedHashMap<>();
    String[] projectNameWithIssueNums = issues.split(",");
    for (String projectNameWithIssueNum : projectNameWithIssueNums) {
      String trimmedProjectNameWithIssueNum = projectNameWithIssueNum.trim();
      String[] issueNameParts = trimmedProjectNameWithIssueNum.split("-");
      if (issueNameParts.length != 2) {
        throw new IssueKeySyntaxException(trimmedProjectNameWithIssueNum);
      }

      String projectName = issueNameParts[0];
      try {
        Long issueNum = Long.parseLong(issueNameParts[1]);
        Collection<Long> issueNums = result.get(projectName);
        if (issueNums == null) {
          issueNums = new LinkedHashSet<>();
          result.put(projectName, issueNums);
        }
        issueNums.add(issueNum);
      } catch (NumberFormatException e) {
        throw new IssueKeySyntaxException(trimmedProjectNameWithIssueNum);
      }
    }
    return result;
  }

  private void save(final String holidayIssues, final String holidayProjects,
      final String nonWorkingIssues,
      final String nonWorkingProjects) {

    Collection<Long> holidayIssueIds = resolveIssueIds(holidayIssues);
    Collection<Long> nonWorkingIssueIds = resolveIssueIds(nonWorkingIssues);
    Collection<Long> holidayProjectIds = resolveProjectIds(holidayProjects);
    Collection<Long> nonWorkingProjectIds = resolveProjectIds(nonWorkingProjects);

    transactionTemplate.execute(() -> {
      saveSpecialIssues(holidayIssueIds, "holiday");
      saveSpecialIssues(nonWorkingIssueIds, "nowork");
      saveSpecialProjects(holidayProjectIds, "holiday");
      saveSpecialProjects(nonWorkingProjectIds, "nowork");
      return null;
    });
  }

  private void saveSpecialIssues(final Collection<Long> issueIds, final String specialty) {
    querydslSupport.execute((connection, configuration) -> {
      Set<Long> existingIssueIds = new LinkedHashSet<>(new SQLQuery<Long>(connection, configuration)
          .select(QSpecialIssue.specialIssue.issueId)
          .from(QSpecialIssue.specialIssue)
          .where(QSpecialIssue.specialIssue.specialty.eq(specialty))
          .fetch());

      // Insert non existent issue ids
      for (Long issueId : issueIds) {
        if (!existingIssueIds.remove(issueId)) {
          new SQLInsertClause(connection, configuration, QSpecialIssue.specialIssue)
              .set(QSpecialIssue.specialIssue.issueId, issueId)
              .set(QSpecialIssue.specialIssue.specialty, specialty)
              .execute();
        }
      }

      if (!existingIssueIds.isEmpty()) {
        new SQLDeleteClause(connection, configuration, QSpecialIssue.specialIssue)
            .where(QSpecialIssue.specialIssue.specialty.eq(specialty)
                .and(QSpecialIssue.specialIssue.issueId.in(existingIssueIds)))
            .execute();
      }

      return null;
    });

  }

  private void saveSpecialProjects(final Collection<Long> projectIds, final String specialty) {
    querydslSupport.execute((connection, configuration) -> {
      Set<Long> existingProjectIds = new HashSet<Long>(new SQLQuery<Long>(connection, configuration)
          .select(QSpecialProject.specialProject.projectId)
          .from(QSpecialProject.specialProject)
          .where(QSpecialProject.specialProject.specialty.eq(specialty))
          .fetch());

      for (Long projectId : projectIds) {
        if (!existingProjectIds.remove(projectId)) {
          new SQLInsertClause(connection, configuration, QSpecialProject.specialProject)
              .set(QSpecialProject.specialProject.projectId, projectId)
              .set(QSpecialProject.specialProject.specialty, specialty)
              .execute();
        }
      }

      if (!existingProjectIds.isEmpty()) {
        new SQLDeleteClause(connection, configuration, QSpecialProject.specialProject)
            .where(QSpecialProject.specialProject.specialty.eq(specialty)
                .and(QSpecialProject.specialProject.projectId.in(existingProjectIds)))
            .execute();
      }
      return null;
    });

  }

}
