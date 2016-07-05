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
package org.everit.jira.configuration.plugin.schema.qdsl;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;

/**
 * QUserHolidayAmount is a Querydsl query type for QUserHolidayAmount
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUserHolidayAmount extends com.querydsl.sql.RelationalPathBase<QUserHolidayAmount> {

  public class ForeignKeys {

    public final com.querydsl.sql.ForeignKey<QDateRange> dateRangeFK =
        createForeignKey(dateRangeId, "date_range_id");

  }

  public class PrimaryKeys {

    public final com.querydsl.sql.PrimaryKey<QUserHolidayAmount> userHolidayAmountPK =
        createPrimaryKey(userHolidayAmountId);

  }

  private static final long serialVersionUID = 871682567;

  public static final QUserHolidayAmount userHolidayAmount =
      new QUserHolidayAmount("everit_jira_user_hday_amount");

  public final NumberPath<Long> dateRangeId = createNumber("dateRangeId", Long.class);

  public final ForeignKeys fk = new ForeignKeys();

  public final PrimaryKeys pk = new PrimaryKeys();

  public final NumberPath<Long> userHolidayAmountId =
      createNumber("userHolidayAmountId", Long.class);

  public final NumberPath<Long> userId = createNumber("userId", Long.class);

  public QUserHolidayAmount(final Path<? extends QUserHolidayAmount> path) {
    super(path.getType(), path.getMetadata(), "public", "everit_jira_user_hday_amount");
    addMetadata();
  }

  public QUserHolidayAmount(final PathMetadata metadata) {
    super(QUserHolidayAmount.class, metadata, "public", "everit_jira_user_hday_amount");
    addMetadata();
  }

  public QUserHolidayAmount(final String variable) {
    super(QUserHolidayAmount.class, forVariable(variable), "public",
        "everit_jira_user_hday_amount");
    addMetadata();
  }

  public QUserHolidayAmount(final String variable, final String schema, final String table) {
    super(QUserHolidayAmount.class, forVariable(variable), schema, table);
    addMetadata();
  }

  public void addMetadata() {
    addMetadata(dateRangeId, ColumnMetadata.named("date_range_id").withIndex(2).ofType(Types.BIGINT)
        .withSize(19).notNull());
    addMetadata(userHolidayAmountId, ColumnMetadata.named("user_hday_amount_id").withIndex(1)
        .ofType(Types.BIGINT).withSize(19).notNull());
    addMetadata(userId,
        ColumnMetadata.named("user_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
  }

}
