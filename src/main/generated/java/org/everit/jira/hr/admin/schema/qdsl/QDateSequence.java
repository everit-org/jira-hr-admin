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
 * QDateSequence is a Querydsl query type for QDateSequence
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDateSequence extends com.querydsl.sql.RelationalPathBase<QDateSequence> {

    private static final long serialVersionUID = -1088134963;

    public static final QDateSequence dateSequence = new QDateSequence("everit_jira_date_sequence");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QDateSequence> dateSequencePK = createPrimaryKey(date);

    }

    public final DatePath<java.sql.Date> date = createDate("date", java.sql.Date.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public QDateSequence(String variable) {
        super(QDateSequence.class, forVariable(variable), "public", "everit_jira_date_sequence");
        addMetadata();
    }

    public QDateSequence(String variable, String schema, String table) {
        super(QDateSequence.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDateSequence(Path<? extends QDateSequence> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_date_sequence");
        addMetadata();
    }

    public QDateSequence(PathMetadata metadata) {
        super(QDateSequence.class, metadata, "public", "everit_jira_date_sequence");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(date, ColumnMetadata.named("date_").withIndex(1).ofType(Types.DATE).withSize(8).notNull());
    }

}

