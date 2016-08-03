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
 * QPublicHoliday is a Querydsl query type for QPublicHoliday
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPublicHoliday extends com.querydsl.sql.RelationalPathBase<QPublicHoliday> {

    private static final long serialVersionUID = -1900185039;

    public static final QPublicHoliday publicHoliday = new QPublicHoliday("everit_jira_public_holiday");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QPublicHoliday> publicHolidayPK = createPrimaryKey(publicHolidayId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QHolidayScheme> holidaySchemeFK = createForeignKey(holidaySchemeId, "holiday_scheme_id");

    }

    public final DatePath<java.sql.Date> date = createDate("date", java.sql.Date.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> holidaySchemeId = createNumber("holidaySchemeId", Long.class);

    public final NumberPath<Long> publicHolidayId = createNumber("publicHolidayId", Long.class);

    public final DatePath<java.sql.Date> replacementDate = createDate("replacementDate", java.sql.Date.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QPublicHoliday(String variable) {
        super(QPublicHoliday.class, forVariable(variable), "public", "everit_jira_public_holiday");
        addMetadata();
    }

    public QPublicHoliday(String variable, String schema, String table) {
        super(QPublicHoliday.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPublicHoliday(Path<? extends QPublicHoliday> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_public_holiday");
        addMetadata();
    }

    public QPublicHoliday(PathMetadata metadata) {
        super(QPublicHoliday.class, metadata, "public", "everit_jira_public_holiday");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(date, ColumnMetadata.named("date_").withIndex(3).ofType(Types.DATE).withSize(8).notNull());
        addMetadata(description, ColumnMetadata.named("description_").withIndex(5).ofType(Types.VARCHAR).withSize(2000));
        addMetadata(holidaySchemeId, ColumnMetadata.named("holiday_scheme_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(publicHolidayId, ColumnMetadata.named("public_holiday_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(replacementDate, ColumnMetadata.named("replacement_date").withIndex(4).ofType(Types.DATE).withSize(8));
    }

}

