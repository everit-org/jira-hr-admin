package org.everit.jira.configuration.plugin.schema.qdsl;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

import java.sql.Types;

import javax.annotation.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;

/**
 * QUserWorkSchedule is a Querydsl query type for QUserWorkSchedule
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUserWorkSchedule extends com.querydsl.sql.RelationalPathBase<QUserWorkSchedule> {

  public class ForeignKeys {

    public final com.querydsl.sql.ForeignKey<QWorkSchedule> workScheduleFK =
        createForeignKey(workScheduleId, "work_schedule_id");

  }

  public class PrimaryKeys {

    public final com.querydsl.sql.PrimaryKey<QUserWorkSchedule> userWorkSchedulePK =
        createPrimaryKey(userWorkScheduleId);

  }

  private static final long serialVersionUID = -794244431;

  public static final QUserWorkSchedule userWorkSchedule =
      new QUserWorkSchedule("everit_jira_user_work_sched");

  public final ForeignKeys fk = new ForeignKeys();

  public final PrimaryKeys pk = new PrimaryKeys();

  public final NumberPath<Long> userId = createNumber("userId", Long.class);

  public final NumberPath<Long> userWorkScheduleId = createNumber("userWorkScheduleId", Long.class);

  public final NumberPath<Long> workScheduleId = createNumber("workScheduleId", Long.class);

  public QUserWorkSchedule(final Path<? extends QUserWorkSchedule> path) {
    super(path.getType(), path.getMetadata(), "public", "everit_jira_user_work_sched");
    addMetadata();
  }

  public QUserWorkSchedule(final PathMetadata metadata) {
    super(QUserWorkSchedule.class, metadata, "public", "everit_jira_user_work_sched");
    addMetadata();
  }

  public QUserWorkSchedule(final String variable) {
    super(QUserWorkSchedule.class, forVariable(variable), "public", "everit_jira_user_work_sched");
    addMetadata();
  }

  public QUserWorkSchedule(final String variable, final String schema, final String table) {
    super(QUserWorkSchedule.class, forVariable(variable), schema, table);
    addMetadata();
  }

  public void addMetadata() {
    addMetadata(userId,
        ColumnMetadata.named("user_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
    addMetadata(userWorkScheduleId, ColumnMetadata.named("user_work_sched_id").withIndex(1)
        .ofType(Types.BIGINT).withSize(19).notNull());
    addMetadata(workScheduleId,
        ColumnMetadata.named("work_schedule_id").withIndex(3).ofType(Types.BIGINT).withSize(19));
  }

}
