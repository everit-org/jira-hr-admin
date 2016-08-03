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
package org.everit.jira.hr.admin.internal;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.everit.jira.hr.admin.schema.qdsl.QDateRange;
import org.everit.jira.hr.admin.schema.qdsl.QDateSequence;
import org.everit.jira.hr.admin.util.DateUtil;
import org.everit.jira.querydsl.support.ri.QuerydslSupportImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLInsertClause;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.osgi.OSGiResourceAccessor;

/**
 * The activator class tries upgrading the database schema.
 */
public class JiraHRAdminPluginActivator implements BundleActivator {

  private void fillDateSequenceFromDateRanges(final Connection connection,
      final Configuration configuration, final long dateRangeCount) {

    QDateRange qDateRange = QDateRange.dateRange;
    final int batchSize = 100;
    long i = 0;
    Set<Date> insertedDates = new HashSet<>();
    List<Date> dateBatch = new ArrayList<>(batchSize);

    while (i < dateRangeCount) {
      List<Tuple> dateRangeTuples = new SQLQuery<>(connection, configuration)
          .select(qDateRange.startDate, qDateRange.endDateExcluded).from(qDateRange)
          .orderBy(qDateRange.dateRangeId.asc())
          .limit(batchSize).offset(i).fetch();

      for (Tuple tuple : dateRangeTuples) {
        Date startDate = tuple.get(qDateRange.startDate);
        Date endDateExcluded = tuple.get(qDateRange.endDateExcluded);
        processDateRange(startDate, endDateExcluded, insertedDates, dateBatch, batchSize,
            connection, configuration);
      }
      i += batchSize;
    }

    if (dateBatch.size() > 0) {
      flushDateBatch(dateBatch, connection, configuration);
    }
  }

  private void flushDateBatch(final List<Date> dateBatch, final Connection connection,
      final Configuration configuration) {

    QDateSequence dateSequence = QDateSequence.dateSequence;
    SQLInsertClause insertClause = new SQLInsertClause(connection, configuration, dateSequence);
    for (Date date : dateBatch) {
      insertClause.set(dateSequence.date, date).addBatch();
    }
    insertClause.execute();
    dateBatch.clear();

  }

  private void processDateRange(final Date startDate, final Date endDateExcluded,
      final Set<Date> insertedDates, final List<Date> dateBatch, final int batchSize,
      final Connection connection, final Configuration configuration) {

    for (Date dateCursor = startDate; dateCursor.before(endDateExcluded); dateCursor =
        DateUtil.addDays(dateCursor, 1)) {

      if (!insertedDates.contains(dateCursor)) {
        insertedDates.add(dateCursor);
        dateBatch.add(dateCursor);
        if (dateBatch.size() == batchSize) {
          flushDateBatch(dateBatch, connection, configuration);
        }
      }
    }
  }

  @Override
  public void start(final BundleContext context) throws Exception {
    try (Connection connection = DefaultOfBizConnectionFactory.getInstance().getConnection()) {
      DatabaseConnection databaseConnection = new JdbcConnection(connection);
      Liquibase liquibase =
          new Liquibase("META-INF/liquibase/org.everit.jira.hr.admin.changelog.xml",
              new OSGiResourceAccessor(context.getBundle()), databaseConnection);

      liquibase.update("production");
    }

    QuerydslSupportImpl querydslSupport = new QuerydslSupportImpl();

    querydslSupport.execute((connection,
        configuration) -> synchronizeDateSequenceIfNecessary(connection, configuration));
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
  }

  private Object synchronizeDateSequenceIfNecessary(final Connection connection,
      final Configuration configuration) {

    QDateSequence qDateSequence = QDateSequence.dateSequence;
    long dateSequenceCount = new SQLQuery<>(connection, configuration)
        .select(Expressions.ONE.count()).from(qDateSequence).fetchOne();

    if (dateSequenceCount == 0) {
      QDateRange qDateRange = QDateRange.dateRange;
      long dateRangeCount = new SQLQuery<>(connection, configuration)
          .select(Expressions.ONE.count()).from(qDateRange).fetchOne();
      if (dateRangeCount != 0) {
        fillDateSequenceFromDateRanges(connection, configuration, dateRangeCount);
      }
    }
    return null;
  }

}
