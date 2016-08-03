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
package org.everit.jira.hr.admin.schema.qdsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QUserHolidayScheme is a Querydsl query type for QUserHolidayScheme
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUserHolidayScheme extends com.querydsl.sql.RelationalPathBase<QUserHolidayScheme> {

    private static final long serialVersionUID = -71114348;

    public static final QUserHolidayScheme userHolidayScheme = new QUserHolidayScheme("everit_jira_user_hday_scheme");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QUserHolidayScheme> userHolidaySchemePK = createPrimaryKey(userHolidaySchemeId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QDateRange> dateRangeFK = createForeignKey(dateRangeId, "date_range_id");

        public final com.querydsl.sql.ForeignKey<QHolidayScheme> holidaySchemeFK = createForeignKey(holidaySchemeId, "holiday_scheme_id");

    }

    public final NumberPath<Long> dateRangeId = createNumber("dateRangeId", Long.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> holidaySchemeId = createNumber("holidaySchemeId", Long.class);

    public final NumberPath<Long> userHolidaySchemeId = createNumber("userHolidaySchemeId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QUserHolidayScheme(String variable) {
        super(QUserHolidayScheme.class, forVariable(variable), "public", "everit_jira_user_hday_scheme");
        addMetadata();
    }

    public QUserHolidayScheme(String variable, String schema, String table) {
        super(QUserHolidayScheme.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUserHolidayScheme(Path<? extends QUserHolidayScheme> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_user_hday_scheme");
        addMetadata();
    }

    public QUserHolidayScheme(PathMetadata metadata) {
        super(QUserHolidayScheme.class, metadata, "public", "everit_jira_user_hday_scheme");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dateRangeId, ColumnMetadata.named("date_range_id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(description, ColumnMetadata.named("description_").withIndex(5).ofType(Types.VARCHAR).withSize(2000));
        addMetadata(holidaySchemeId, ColumnMetadata.named("holiday_scheme_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(userHolidaySchemeId, ColumnMetadata.named("user_hday_scheme_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

