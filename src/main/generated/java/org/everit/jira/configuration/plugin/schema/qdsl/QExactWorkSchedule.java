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
 * QExactWorkSchedule is a Querydsl query type for QExactWorkSchedule
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QExactWorkSchedule extends com.querydsl.sql.RelationalPathBase<QExactWorkSchedule> {

    private static final long serialVersionUID = -1631502679;

    public static final QExactWorkSchedule exactWorkSchedule = new QExactWorkSchedule("everit_jira_exact_work_sched");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QExactWorkSchedule> exactWorkSchedulePK = createPrimaryKey(exactWorkSchedId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QWorkSchedule> workScheduleFK = createForeignKey(workScheduleId, "work_schedule_id");

    }

    public final DatePath<java.sql.Date> date_ = createDate("date_", java.sql.Date.class);

    public final NumberPath<Integer> duration_ = createNumber("duration_", Integer.class);

    public final NumberPath<Long> exactWorkSchedId = createNumber("exactWorkSchedId", Long.class);

    public final TimePath<java.sql.Time> startTime = createTime("startTime", java.sql.Time.class);

    public final NumberPath<Long> workScheduleId = createNumber("workScheduleId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QExactWorkSchedule(String variable) {
        super(QExactWorkSchedule.class, forVariable(variable), "public", "everit_jira_exact_work_sched");
        addMetadata();
    }

    public QExactWorkSchedule(String variable, String schema, String table) {
        super(QExactWorkSchedule.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QExactWorkSchedule(Path<? extends QExactWorkSchedule> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_exact_work_sched");
        addMetadata();
    }

    public QExactWorkSchedule(PathMetadata metadata) {
        super(QExactWorkSchedule.class, metadata, "public", "everit_jira_exact_work_sched");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(date_, ColumnMetadata.named("date_").withIndex(2).ofType(Types.DATE).withSize(8).notNull());
        addMetadata(duration_, ColumnMetadata.named("duration_").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(exactWorkSchedId, ColumnMetadata.named("exact_work_sched_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(startTime, ColumnMetadata.named("start_time").withIndex(3).ofType(Types.TIME).withSize(6).notNull());
        addMetadata(workScheduleId, ColumnMetadata.named("work_schedule_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
    }

}

