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
 * QWorkSchedule is a Querydsl query type for QWorkSchedule
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QWorkSchedule extends com.querydsl.sql.RelationalPathBase<QWorkSchedule> {

    private static final long serialVersionUID = -1472474554;

    public static final QWorkSchedule workSchedule = new QWorkSchedule("everit_jira_work_schedule");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QWorkSchedule> workSchedulePK = createPrimaryKey(workScheduleId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QUserWorkSchedule> _everitJiraUsWSchWschFk = createInvForeignKey(workScheduleId, "work_schedule_id");

        public final com.querydsl.sql.ForeignKey<QExactWorkSchedule> _everitJiraExWSchWschFk = createInvForeignKey(workScheduleId, "work_schedule_id");

        public final com.querydsl.sql.ForeignKey<QWeekdayWorkSchedule> _everitJiraWdWSchWschFk = createInvForeignKey(workScheduleId, "work_schedule_id");

    }

    public final StringPath name_ = createString("name_");

    public final StringPath scope_ = createString("scope_");

    public final NumberPath<Long> workScheduleId = createNumber("workScheduleId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QWorkSchedule(String variable) {
        super(QWorkSchedule.class, forVariable(variable), "public", "everit_jira_work_schedule");
        addMetadata();
    }

    public QWorkSchedule(String variable, String schema, String table) {
        super(QWorkSchedule.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QWorkSchedule(Path<? extends QWorkSchedule> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_work_schedule");
        addMetadata();
    }

    public QWorkSchedule(PathMetadata metadata) {
        super(QWorkSchedule.class, metadata, "public", "everit_jira_work_schedule");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name_, ColumnMetadata.named("name_").withIndex(2).ofType(Types.VARCHAR).withSize(60).notNull());
        addMetadata(scope_, ColumnMetadata.named("scope_").withIndex(3).ofType(Types.VARCHAR).withSize(60).notNull());
        addMetadata(workScheduleId, ColumnMetadata.named("work_schedule_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

