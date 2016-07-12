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
 * QWorkScheme is a Querydsl query type for QWorkScheme
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QWorkScheme extends com.querydsl.sql.RelationalPathBase<QWorkScheme> {

    private static final long serialVersionUID = -368012012;

    public static final QWorkScheme workScheme = new QWorkScheme("everit_jira_work_scheme");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QWorkScheme> workSchemePK = createPrimaryKey(workSchemeId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QUserWorkScheme> _everitJiraUsWSchWschFk = createInvForeignKey(workSchemeId, "work_scheme_id");

        public final com.querydsl.sql.ForeignKey<QExactWorkScheme> _everitJiraExWSchWschFk = createInvForeignKey(workSchemeId, "work_scheme_id");

        public final com.querydsl.sql.ForeignKey<QWeekdayWorkScheme> _everitJiraWdWSchWschFk = createInvForeignKey(workSchemeId, "work_scheme_id");

    }

    public final StringPath name_ = createString("name_");

    public final StringPath scope_ = createString("scope_");

    public final NumberPath<Long> workSchemeId = createNumber("workSchemeId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QWorkScheme(String variable) {
        super(QWorkScheme.class, forVariable(variable), "public", "everit_jira_work_scheme");
        addMetadata();
    }

    public QWorkScheme(String variable, String schema, String table) {
        super(QWorkScheme.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QWorkScheme(Path<? extends QWorkScheme> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_work_scheme");
        addMetadata();
    }

    public QWorkScheme(PathMetadata metadata) {
        super(QWorkScheme.class, metadata, "public", "everit_jira_work_scheme");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name_, ColumnMetadata.named("name_").withIndex(2).ofType(Types.VARCHAR).withSize(60).notNull());
        addMetadata(scope_, ColumnMetadata.named("scope_").withIndex(3).ofType(Types.VARCHAR).withSize(60).notNull());
        addMetadata(workSchemeId, ColumnMetadata.named("work_scheme_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

