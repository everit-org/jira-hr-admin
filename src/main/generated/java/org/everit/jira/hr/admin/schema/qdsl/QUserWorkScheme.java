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
 * QUserWorkScheme is a Querydsl query type for QUserWorkScheme
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUserWorkScheme extends com.querydsl.sql.RelationalPathBase<QUserWorkScheme> {

    private static final long serialVersionUID = 1008314879;

    public static final QUserWorkScheme userWorkScheme = new QUserWorkScheme("everit_jira_user_work_scheme");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QUserWorkScheme> userWorkSchemePK = createPrimaryKey(userWorkSchemeId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QWorkScheme> workSchemeFK = createForeignKey(workSchemeId, "work_scheme_id");

        public final com.querydsl.sql.ForeignKey<QDateRange> everitJiraUserWSchDrFk = createForeignKey(dateRangeId, "date_range_id");

    }

    public final NumberPath<Long> dateRangeId = createNumber("dateRangeId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final NumberPath<Long> userWorkSchemeId = createNumber("userWorkSchemeId", Long.class);

    public final NumberPath<Long> workSchemeId = createNumber("workSchemeId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QUserWorkScheme(String variable) {
        super(QUserWorkScheme.class, forVariable(variable), "public", "everit_jira_user_work_scheme");
        addMetadata();
    }

    public QUserWorkScheme(String variable, String schema, String table) {
        super(QUserWorkScheme.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUserWorkScheme(Path<? extends QUserWorkScheme> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_user_work_scheme");
        addMetadata();
    }

    public QUserWorkScheme(PathMetadata metadata) {
        super(QUserWorkScheme.class, metadata, "public", "everit_jira_user_work_scheme");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dateRangeId, ColumnMetadata.named("date_range_id").withIndex(4).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(userWorkSchemeId, ColumnMetadata.named("user_work_sched_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(workSchemeId, ColumnMetadata.named("work_scheme_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
    }

}

