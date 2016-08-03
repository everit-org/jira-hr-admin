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
package org.everit.jira.hr.admin.schema.qdsl.util;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.everit.jira.hr.admin.schema.qdsl.QDateRange;
import org.everit.jira.hr.admin.schema.qdsl.QDateSequence;
import org.everit.jira.hr.admin.util.DateUtil;

import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;

public class DateRangeUtil {

  private final Configuration configuration;

  private final Connection connection;

  public DateRangeUtil(final Connection connection, final Configuration configuration) {
    this.connection = connection;
    this.configuration = configuration;
  }

  public long createDateRange(final Date startDate, final Date endDateExcluded) {
    fillDateSequenceFromRange(startDate, endDateExcluded);

    QDateRange dateRange = QDateRange.dateRange;
    return new SQLInsertClause(connection, configuration, dateRange)
        .set(dateRange.startDate, startDate)
        .set(dateRange.endDateExcluded, endDateExcluded)
        .executeWithKey(dateRange.dateRangeId);
  }

  private void fillDateSequenceFromRange(final Date startDate, final Date endDateExcluded) {
    Date dateCursor = startDate;
    final int batchSize = 100;
    QDateSequence qDates = QDateSequence.dateSequence;

    while (dateCursor.before(endDateExcluded)) {
      Date batchEndDateExcluded = DateUtil.addDays(dateCursor, batchSize);

      if (batchEndDateExcluded.after(endDateExcluded)) {
        batchEndDateExcluded = endDateExcluded;
      }

      List<Date> existingDates =
          new SQLQuery<Date>(connection, configuration)
              .select(qDates.date)
              .from(qDates)
              .where(qDates.date.goe(dateCursor).and(qDates.date.lt(batchEndDateExcluded)))
              .orderBy(qDates.date.asc()).fetch();

      Date[] existingDateArray =
          existingDates.toArray(new Date[existingDates.size()]);

      List<Date> dateBatch = new ArrayList<>(batchSize);
      while (dateCursor.before(batchEndDateExcluded)) {
        if (Arrays.binarySearch(existingDateArray, dateCursor) < 0) {
          dateBatch.add(dateCursor);
        }
        dateCursor = DateUtil.addDays(dateCursor, 1);
      }

      if (!dateBatch.isEmpty()) {
        SQLInsertClause dateInsert = new SQLInsertClause(connection, configuration, qDates);
        for (Date date : dateBatch) {
          dateInsert.set(qDates.date, date).addBatch();
        }
        dateInsert.execute();
      }
    }
  }

  public void modifyDateRange(final long dateRangeId, final Date startDate,
      final Date endDateExcluded) {

    QDateRange dateRange = QDateRange.dateRange;
    new SQLUpdateClause(connection, configuration, dateRange)
        .set(dateRange.startDate, startDate)
        .set(dateRange.endDateExcluded, endDateExcluded)
        .where(dateRange.dateRangeId.eq(dateRangeId)).execute();

    vacuumDates();
    fillDateSequenceFromRange(startDate, endDateExcluded);

  }

  public void removeDateRange(final Long... dateRangeId) {
    QDateRange dateRange = QDateRange.dateRange;
    if (dateRangeId.length == 1) {
      new SQLDeleteClause(connection, configuration, dateRange)
          .where(dateRange.dateRangeId.eq(dateRangeId[0])).execute();
    } else {
      new SQLDeleteClause(connection, configuration, dateRange)
          .where(dateRange.dateRangeId.in(dateRangeId)).execute();
    }

    vacuumDates();
  }

  private void vacuumDates() {
    QDateSequence qDates = QDateSequence.dateSequence;
    QDateRange qDateRange = QDateRange.dateRange;
    new SQLDeleteClause(connection, configuration, qDates)
        .where(new SQLQuery<Boolean>()
            .from(qDateRange)
            .where(qDateRange.startDate.loe(qDates.date)
                .and(qDateRange.endDateExcluded.gt(qDates.date)))
            .notExists())
        .execute();
  }
}
