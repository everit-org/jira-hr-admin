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
 * QWeekdayWorkScheme is a Querydsl query type for QWeekdayWorkScheme
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QWeekdayWorkScheme extends com.querydsl.sql.RelationalPathBase<QWeekdayWorkScheme> {

    private static final long serialVersionUID = -574416224;

    public static final QWeekdayWorkScheme weekdayWorkScheme = new QWeekdayWorkScheme("everit_jira_wd_work_scheme");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QWeekdayWorkScheme> weekdayWorkSchemePK = createPrimaryKey(wdWorkSchemeId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QWorkScheme> workSchemeFK = createForeignKey(workSchemeId, "work_scheme_id");

    }

    public final NumberPath<Integer> duration_ = createNumber("duration_", Integer.class);

    public final TimePath<java.sql.Time> startTime = createTime("startTime", java.sql.Time.class);

    public final NumberPath<Long> wdWorkSchemeId = createNumber("wdWorkSchemeId", Long.class);

    public final NumberPath<Byte> weekday = createNumber("weekday", Byte.class);

    public final NumberPath<Long> workSchemeId = createNumber("workSchemeId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QWeekdayWorkScheme(String variable) {
        super(QWeekdayWorkScheme.class, forVariable(variable), "public", "everit_jira_wd_work_scheme");
        addMetadata();
    }

    public QWeekdayWorkScheme(String variable, String schema, String table) {
        super(QWeekdayWorkScheme.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QWeekdayWorkScheme(Path<? extends QWeekdayWorkScheme> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_wd_work_scheme");
        addMetadata();
    }

    public QWeekdayWorkScheme(PathMetadata metadata) {
        super(QWeekdayWorkScheme.class, metadata, "public", "everit_jira_wd_work_scheme");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(duration_, ColumnMetadata.named("duration_").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(startTime, ColumnMetadata.named("start_time").withIndex(3).ofType(Types.TIME).withSize(6).notNull());
        addMetadata(wdWorkSchemeId, ColumnMetadata.named("wd_work_scheme_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(weekday, ColumnMetadata.named("weekday").withIndex(2).ofType(Types.TINYINT).withSize(3).notNull());
        addMetadata(workSchemeId, ColumnMetadata.named("work_scheme_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
    }

}

