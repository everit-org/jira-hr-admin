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
 * QSpecialProject is a Querydsl query type for QSpecialProject
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSpecialProject extends com.querydsl.sql.RelationalPathBase<QSpecialProject> {

    private static final long serialVersionUID = 920345566;

    public static final QSpecialProject specialProject = new QSpecialProject("everit_jira_spec_proj");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QSpecialProject> specialProjectPK = createPrimaryKey(specProjId);

    }

    public final NumberPath<Long> projectId = createNumber("projectId", Long.class);

    public final StringPath specialty = createString("specialty");

    public final NumberPath<Long> specProjId = createNumber("specProjId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public QSpecialProject(String variable) {
        super(QSpecialProject.class, forVariable(variable), "public", "everit_jira_spec_proj");
        addMetadata();
    }

    public QSpecialProject(String variable, String schema, String table) {
        super(QSpecialProject.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSpecialProject(Path<? extends QSpecialProject> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_spec_proj");
        addMetadata();
    }

    public QSpecialProject(PathMetadata metadata) {
        super(QSpecialProject.class, metadata, "public", "everit_jira_spec_proj");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(projectId, ColumnMetadata.named("project_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(specialty, ColumnMetadata.named("specialty").withIndex(3).ofType(Types.VARCHAR).withSize(60).notNull());
        addMetadata(specProjId, ColumnMetadata.named("spec_proj_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

