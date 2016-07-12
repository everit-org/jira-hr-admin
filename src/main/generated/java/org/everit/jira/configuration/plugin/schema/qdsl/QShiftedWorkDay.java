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

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QShiftedWorkDay is a Querydsl query type for QShiftedWorkDay
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QShiftedWorkDay extends com.querydsl.sql.RelationalPathBase<QShiftedWorkDay> {

    private static final long serialVersionUID = -2108924696;

    public static final QShiftedWorkDay shiftedWorkDay = new QShiftedWorkDay("everit_jira_shifted_work_day");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QShiftedWorkDay> shiftedWorkPK = createPrimaryKey(shiftedWorkDayId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QHolidayScheme> holidaySchemeFK = createForeignKey(holidaySchemeId, "holiday_scheme_id");

    }

    public final DatePath<java.sql.Date> date_ = createDate("date_", java.sql.Date.class);

    public final StringPath description_ = createString("description_");

    public final NumberPath<Long> holidaySchemeId = createNumber("holidaySchemeId", Long.class);

    public final NumberPath<Long> shiftedWorkDayId = createNumber("shiftedWorkDayId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QShiftedWorkDay(String variable) {
        super(QShiftedWorkDay.class, forVariable(variable), "public", "everit_jira_shifted_work_day");
        addMetadata();
    }

    public QShiftedWorkDay(String variable, String schema, String table) {
        super(QShiftedWorkDay.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QShiftedWorkDay(Path<? extends QShiftedWorkDay> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_shifted_work_day");
        addMetadata();
    }

    public QShiftedWorkDay(PathMetadata metadata) {
        super(QShiftedWorkDay.class, metadata, "public", "everit_jira_shifted_work_day");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(date_, ColumnMetadata.named("date_").withIndex(3).ofType(Types.DATE).withSize(8).notNull());
        addMetadata(description_, ColumnMetadata.named("description_").withIndex(4).ofType(Types.VARCHAR).withSize(2000));
        addMetadata(holidaySchemeId, ColumnMetadata.named("holiday_scheme_id").withIndex(2).ofType(Types.BIGINT).withSize(19));
        addMetadata(shiftedWorkDayId, ColumnMetadata.named("shifted_work_day_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

