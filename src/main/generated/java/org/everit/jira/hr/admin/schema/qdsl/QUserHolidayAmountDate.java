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
 * QUserHolidayAmountDate is a Querydsl query type for QUserHolidayAmountDate
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUserHolidayAmountDate extends com.querydsl.sql.RelationalPathBase<QUserHolidayAmountDate> {

    private static final long serialVersionUID = -1447110059;

    public static final QUserHolidayAmountDate userHolidayAmountDate = new QUserHolidayAmountDate("everit_jira_u_hday_am_date");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QUserHolidayAmountDate> userHolidayAmountDatePK = createPrimaryKey(date);

    }

    public final DatePath<java.sql.Date> date = createDate("date", java.sql.Date.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public QUserHolidayAmountDate(String variable) {
        super(QUserHolidayAmountDate.class, forVariable(variable), "public", "everit_jira_u_hday_am_date");
        addMetadata();
    }

    public QUserHolidayAmountDate(String variable, String schema, String table) {
        super(QUserHolidayAmountDate.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUserHolidayAmountDate(Path<? extends QUserHolidayAmountDate> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_u_hday_am_date");
        addMetadata();
    }

    public QUserHolidayAmountDate(PathMetadata metadata) {
        super(QUserHolidayAmountDate.class, metadata, "public", "everit_jira_u_hday_am_date");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(date, ColumnMetadata.named("date_").withIndex(1).ofType(Types.DATE).withSize(8).notNull());
    }

}

