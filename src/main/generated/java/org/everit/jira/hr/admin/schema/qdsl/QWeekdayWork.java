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
 * QWeekdayWork is a Querydsl query type for QWeekdayWork
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QWeekdayWork extends com.querydsl.sql.RelationalPathBase<QWeekdayWork> {

    private static final long serialVersionUID = -564702373;

    public static final QWeekdayWork weekdayWork = new QWeekdayWork("everit_jira_weekday_work");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QWeekdayWork> weekdayWorkPK = createPrimaryKey(weekdayWorkId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QWorkScheme> workSchemeFK = createForeignKey(workSchemeId, "work_scheme_id");

    }

    public final NumberPath<Integer> duration = createNumber("duration", Integer.class);

    public final TimePath<java.sql.Time> startTime = createTime("startTime", java.sql.Time.class);

    public final NumberPath<Byte> weekday = createNumber("weekday", Byte.class);

    public final NumberPath<Long> weekdayWorkId = createNumber("weekdayWorkId", Long.class);

    public final NumberPath<Long> workSchemeId = createNumber("workSchemeId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QWeekdayWork(String variable) {
        super(QWeekdayWork.class, forVariable(variable), "public", "everit_jira_weekday_work");
        addMetadata();
    }

    public QWeekdayWork(String variable, String schema, String table) {
        super(QWeekdayWork.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QWeekdayWork(Path<? extends QWeekdayWork> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_weekday_work");
        addMetadata();
    }

    public QWeekdayWork(PathMetadata metadata) {
        super(QWeekdayWork.class, metadata, "public", "everit_jira_weekday_work");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(duration, ColumnMetadata.named("duration_").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(startTime, ColumnMetadata.named("start_time").withIndex(3).ofType(Types.TIME).withSize(6).notNull());
        addMetadata(weekday, ColumnMetadata.named("weekday").withIndex(2).ofType(Types.TINYINT).withSize(3).notNull());
        addMetadata(weekdayWorkId, ColumnMetadata.named("weekday_work_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(workSchemeId, ColumnMetadata.named("work_scheme_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
    }

}

