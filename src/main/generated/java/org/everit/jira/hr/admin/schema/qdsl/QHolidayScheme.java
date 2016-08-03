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
 * QHolidayScheme is a Querydsl query type for QHolidayScheme
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QHolidayScheme extends com.querydsl.sql.RelationalPathBase<QHolidayScheme> {

    private static final long serialVersionUID = 457174175;

    public static final QHolidayScheme holidayScheme = new QHolidayScheme("everit_jira_holiday_scheme");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QHolidayScheme> holidaySchemePK = createPrimaryKey(holidaySchemeId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QPublicHoliday> _everitJiraPubHdHschFk = createInvForeignKey(holidaySchemeId, "holiday_scheme_id");

        public final com.querydsl.sql.ForeignKey<QUserHolidayScheme> _everitJiraUsHdSchmHsFk = createInvForeignKey(holidaySchemeId, "holiday_scheme_id");

    }

    public final NumberPath<Long> holidaySchemeId = createNumber("holidaySchemeId", Long.class);

    public final StringPath name = createString("name");

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QHolidayScheme(String variable) {
        super(QHolidayScheme.class, forVariable(variable), "public", "everit_jira_holiday_scheme");
        addMetadata();
    }

    public QHolidayScheme(String variable, String schema, String table) {
        super(QHolidayScheme.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QHolidayScheme(Path<? extends QHolidayScheme> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_holiday_scheme");
        addMetadata();
    }

    public QHolidayScheme(PathMetadata metadata) {
        super(QHolidayScheme.class, metadata, "public", "everit_jira_holiday_scheme");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(holidaySchemeId, ColumnMetadata.named("holiday_scheme_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name, ColumnMetadata.named("name_").withIndex(2).ofType(Types.VARCHAR).withSize(60).notNull());
    }

}

