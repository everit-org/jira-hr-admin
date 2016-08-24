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
package org.everit.jira.hr.admin.util;

import java.sql.Date;

import org.everit.jira.hr.admin.schema.qdsl.QDateRange;
import org.everit.jira.hr.admin.schema.qdsl.QDateSequence;
import org.everit.jira.hr.admin.schema.qdsl.QExactWork;
import org.everit.jira.hr.admin.schema.qdsl.QPublicHoliday;
import org.everit.jira.hr.admin.schema.qdsl.QUserHolidayScheme;
import org.everit.jira.hr.admin.schema.qdsl.QUserWorkScheme;
import org.everit.jira.hr.admin.schema.qdsl.QWeekdayWork;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Coalesce;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.SQLQuery;

public final class QueryUtil {

  public static BooleanExpression dateRangeOverlaps(final QDateRange qDateRange,
      final Expression<Date> startDate, final Expression<Date> endDateExcluded) {
    return qDateRange.startDate.loe(startDate)
        .and(qDateRange.endDateExcluded.gt(startDate))
        .or(qDateRange.startDate.lt(endDateExcluded)
            .and(qDateRange.endDateExcluded.goe(endDateExcluded)))
        .or(qDateRange.startDate.goe(startDate).and(qDateRange.startDate.lt(endDateExcluded)));
  }

  private static Expression<Long> exactWorkSubSelect(final NumberPath<Long> workSchemeId,
      final DatePath<Date> date) {
    QExactWork qExactWork = new QExactWork("exp_work_exact_work");
    SQLQuery<Long> query = new SQLQuery<Long>();
    query.select(qExactWork.duration.sum()).from(qExactWork)
        .where(qExactWork.workSchemeId.eq(workSchemeId).and(qExactWork.date.eq(date)));
    return query;
  }

  /**
   * Getting the expected work amount for a date period. The calculation for each day in the range
   * is done in the following order:
   * <ol>
   * <li>if there is an exact work day specified for a specific date, that is used. Otherwise</li>
   * <li>if there is a replacement day of a public holiday and there is a weekday specified for the
   * replaced holiday, that is used. Otherwise</li>
   * <li>If there is a non holiday weekday, that is used. Otherwise</li>
   * <li>zero is used.</li>
   * </ol>
   *
   * @param userId
   *          The id of the user who should have done the work.
   * @param startDate
   *          The date when we start calculating the work that should be done from.
   * @param endDateExcluded
   *          The date until we calculate the work that should be done (excluded).
   * @return The amount of work that should be done by the specified user in the specific time
   *         period.
   */
  public static SQLQuery<Long> expectedWorkAmount(final Expression<Long> userId,
      final Expression<Date> startDate, final Expression<Date> endDateExcluded) {

    QDateSequence qDates = new QDateSequence("exp_work_dates");
    QDateRange qDateRange = new QDateRange("exp_work_date_range");
    QUserWorkScheme qUserWorkScheme = new QUserWorkScheme("exp_work_user_work_scheme");

    SQLQuery<Long> query = new SQLQuery<>();
    query.select(
        new Coalesce<Long>(Long.class,
            exactWorkSubSelect(qUserWorkScheme.workSchemeId, qDates.date),
            replacementWeekdaySubSelect(qUserWorkScheme.workSchemeId, userId, qDates.date),
            nonHolidayWeekdaySubSelect(qUserWorkScheme.workSchemeId, userId, qDates.date),
            Expressions.ZERO)
                .asNumber().sum());
    query
        .from(qDates)
        .innerJoin(qDateRange)
        .on(qDateRange.startDate.loe(qDates.date).and(qDateRange.endDateExcluded.gt(qDates.date)))
        .innerJoin(qUserWorkScheme).on(qUserWorkScheme.dateRangeId.eq(qDateRange.dateRangeId))
        .where(qDates.date.goe(startDate).and(qDates.date.lt(endDateExcluded))
            .and(qUserWorkScheme.userId.eq(userId)));

    return query;
  }

  private static Predicate noHolidayExistsSubSelect(final Expression<Long> userId,
      final DatePath<Date> date) {
    QPublicHoliday qPublicHoliday = new QPublicHoliday("exp_work_nh_ph");
    QUserHolidayScheme qUserHolidayScheme = new QUserHolidayScheme("exp_work_nh_uhsch");
    QDateRange qDateRange = new QDateRange("exp_work_nh_data_range");

    return new SQLQuery<>().select(qPublicHoliday.publicHolidayId).from(qPublicHoliday)
        .innerJoin(qUserHolidayScheme)
        .on(qUserHolidayScheme.holidaySchemeId.eq(qPublicHoliday.holidaySchemeId))
        .innerJoin(qDateRange).on(qDateRange.dateRangeId.eq(qUserHolidayScheme.dateRangeId))
        .where(qUserHolidayScheme.userId.eq(userId)
            .and(qDateRange.startDate.loe(date))
            .and(qDateRange.endDateExcluded.gt(date))
            .and(qPublicHoliday.date.eq(date)))
        .notExists();
  }

  private static Expression<Long> nonHolidayWeekdaySubSelect(final NumberPath<Long> workSchemeId,
      final Expression<Long> userId, final DatePath<Date> date) {

    QWeekdayWork qWeekdayWork = new QWeekdayWork("exp_work_nh_wdw");

    SQLQuery<Long> query = new SQLQuery<>();
    query.select(qWeekdayWork.duration.sum()).from(qWeekdayWork)
        .where(qWeekdayWork.workSchemeId.eq(workSchemeId)
            .and(date.dayOfWeek()
                .eq(Expressions.path(Integer.class, qWeekdayWork.weekday.getMetadata())))
            .and(noHolidayExistsSubSelect(userId, date)));
    return query;
  }

  private static Expression<Long> replacementWeekdaySubSelect(final NumberPath<Long> workSchemeId,
      final Expression<Long> userId, final DatePath<Date> date) {

    QPublicHoliday qPublicHoliday = new QPublicHoliday("exp_work_repl_pubhday");
    QUserHolidayScheme qUserHolidayScheme = new QUserHolidayScheme("exp_work_repl_uhsch");
    QDateRange qDateRange = new QDateRange("exp_work_repl_uhschdr");

    SQLQuery<Long> query = new SQLQuery<Long>();
    query.select(weekdaySumForReplacementDay(workSchemeId, qPublicHoliday.date.dayOfWeek()))
        .from(qPublicHoliday)
        .innerJoin(qUserHolidayScheme)
        .on(qUserHolidayScheme.holidaySchemeId.eq(qPublicHoliday.holidaySchemeId))
        .innerJoin(qDateRange).on(qDateRange.dateRangeId.eq(qUserHolidayScheme.dateRangeId))
        .where(qPublicHoliday.replacementDate.eq(date)
            .and(qUserHolidayScheme.userId.eq(userId))
            .and(qDateRange.startDate.loe(date))
            .and(qDateRange.endDateExcluded.gt(date)));
    return query;
  }

  private static Expression<Long> weekdaySumForReplacementDay(final NumberPath<Long> workSchemeId,
      final NumberExpression<Integer> dayOfWeek) {
    QWeekdayWork qWeekdayWork = new QWeekdayWork("exp_work_repl_wdw");
    SQLQuery<Long> query = new SQLQuery<>();
    query.select(qWeekdayWork.duration.sum())
        .from(qWeekdayWork)
        .where(qWeekdayWork.workSchemeId.eq(workSchemeId)
            .and(
                Expressions.path(Integer.class, qWeekdayWork.weekday.getMetadata()).eq(dayOfWeek)));
    return query;
  }

}
