package org.everit.jira.configuration.plugin.schema.qdsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QExactWork is a Querydsl query type for QExactWork
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QExactWork extends com.querydsl.sql.RelationalPathBase<QExactWork> {

    private static final long serialVersionUID = -1992795662;

    public static final QExactWork exactWork = new QExactWork("everit_jira_exact_work");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QExactWork> exactWorkPK = createPrimaryKey(exactWorkSchemeId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QWorkScheme> workSchemeFK = createForeignKey(workSchemeId, "work_scheme_id");

    }

    public final DatePath<java.sql.Date> date_ = createDate("date_", java.sql.Date.class);

    public final NumberPath<Integer> duration_ = createNumber("duration_", Integer.class);

    public final NumberPath<Long> exactWorkSchemeId = createNumber("exactWorkSchemeId", Long.class);

    public final TimePath<java.sql.Time> startTime = createTime("startTime", java.sql.Time.class);

    public final NumberPath<Long> workSchemeId = createNumber("workSchemeId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QExactWork(String variable) {
        super(QExactWork.class, forVariable(variable), "public", "everit_jira_exact_work");
        addMetadata();
    }

    public QExactWork(String variable, String schema, String table) {
        super(QExactWork.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QExactWork(Path<? extends QExactWork> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_exact_work");
        addMetadata();
    }

    public QExactWork(PathMetadata metadata) {
        super(QExactWork.class, metadata, "public", "everit_jira_exact_work");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(date_, ColumnMetadata.named("date_").withIndex(2).ofType(Types.DATE).withSize(8).notNull());
        addMetadata(duration_, ColumnMetadata.named("duration_").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(exactWorkSchemeId, ColumnMetadata.named("exact_work_scheme_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(startTime, ColumnMetadata.named("start_time").withIndex(3).ofType(Types.TIME).withSize(6).notNull());
        addMetadata(workSchemeId, ColumnMetadata.named("work_scheme_id").withIndex(5).ofType(Types.BIGINT).withSize(19));
    }

}

