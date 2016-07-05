package org.everit.jira.configuration.plugin.schema.qdsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QWeekdayWorkSchedule is a Querydsl query type for QWeekdayWorkSchedule
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QWeekdayWorkSchedule extends com.querydsl.sql.RelationalPathBase<QWeekdayWorkSchedule> {

    private static final long serialVersionUID = 2036540626;

    public static final QWeekdayWorkSchedule weekdayWorkSchedule = new QWeekdayWorkSchedule("everit_jira_wd_work_sched");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QWeekdayWorkSchedule> weekdayWorkSchedulePK = createPrimaryKey(wdWorkSchedId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QWorkSchedule> workScheduleFK = createForeignKey(workScheduleId, "work_schedule_id");

    }

    public final NumberPath<Integer> duration_ = createNumber("duration_", Integer.class);

    public final TimePath<java.sql.Time> startTime = createTime("startTime", java.sql.Time.class);

    public final NumberPath<Long> wdWorkSchedId = createNumber("wdWorkSchedId", Long.class);

    public final NumberPath<Byte> weekday = createNumber("weekday", Byte.class);

    public final NumberPath<Long> workScheduleId = createNumber("workScheduleId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QWeekdayWorkSchedule(String variable) {
        super(QWeekdayWorkSchedule.class, forVariable(variable), "public", "everit_jira_wd_work_sched");
        addMetadata();
    }

    public QWeekdayWorkSchedule(String variable, String schema, String table) {
        super(QWeekdayWorkSchedule.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QWeekdayWorkSchedule(Path<? extends QWeekdayWorkSchedule> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_wd_work_sched");
        addMetadata();
    }

    public QWeekdayWorkSchedule(PathMetadata metadata) {
        super(QWeekdayWorkSchedule.class, metadata, "public", "everit_jira_wd_work_sched");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(duration_, ColumnMetadata.named("duration_").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(startTime, ColumnMetadata.named("start_time").withIndex(3).ofType(Types.TIME).withSize(6).notNull());
        addMetadata(wdWorkSchedId, ColumnMetadata.named("wd_work_sched_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(weekday, ColumnMetadata.named("weekday").withIndex(2).ofType(Types.TINYINT).withSize(3).notNull());
        addMetadata(workScheduleId, ColumnMetadata.named("work_schedule_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
    }

}

