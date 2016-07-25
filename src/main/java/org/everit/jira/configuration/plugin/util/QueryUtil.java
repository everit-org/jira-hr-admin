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
package org.everit.jira.configuration.plugin.util;

import java.sql.Date;

import org.everit.jira.configuration.plugin.schema.qdsl.QDateRange;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;

public final class QueryUtil {

  public static BooleanExpression dateRangeOverlaps(final QDateRange qDateRange,
      final Expression<Date> startDate, final Expression<Date> endDateExcluded) {
    return qDateRange.startDate.loe(startDate)
        .and(qDateRange.endDateExcluded.gt(startDate))
        .or(qDateRange.startDate.lt(endDateExcluded)
            .and(qDateRange.endDateExcluded.goe(endDateExcluded)))
        .or(qDateRange.startDate.goe(startDate).and(qDateRange.startDate.lt(endDateExcluded)));
  }

  private QueryUtil() {
  }

}
