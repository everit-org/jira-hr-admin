package org.everit.jira.configuration.plugin.schema.qdsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDateRange is a Querydsl query type for QDateRange
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDateRange extends com.querydsl.sql.RelationalPathBase<QDateRange> {

    private static final long serialVersionUID = -1906417295;

    public static final QDateRange dateRange = new QDateRange("everit_jira_date_range");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QDateRange> dateRangePK = createPrimaryKey(dateRangeId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QUserWorkScheme> _everitJiraUserWSchDrFk = createInvForeignKey(dateRangeId, "date_range_id");

        public final com.querydsl.sql.ForeignKey<QUserHolidayScheme> _everitJiraUsHdaySchDrFk = createInvForeignKey(dateRangeId, "date_range_id");

        public final com.querydsl.sql.ForeignKey<QUserHolidayAmount> _everitJiraUserHdayADrFk = createInvForeignKey(dateRangeId, "date_range_id");

    }

    public final NumberPath<Long> dateRangeId = createNumber("dateRangeId", Long.class);

    public final DatePath<java.sql.Date> endDateExcluded = createDate("endDateExcluded", java.sql.Date.class);

    public final DatePath<java.sql.Date> startDate = createDate("startDate", java.sql.Date.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QDateRange(String variable) {
        super(QDateRange.class, forVariable(variable), "public", "everit_jira_date_range");
        addMetadata();
    }

    public QDateRange(String variable, String schema, String table) {
        super(QDateRange.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDateRange(Path<? extends QDateRange> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_date_range");
        addMetadata();
    }

    public QDateRange(PathMetadata metadata) {
        super(QDateRange.class, metadata, "public", "everit_jira_date_range");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dateRangeId, ColumnMetadata.named("date_range_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(endDateExcluded, ColumnMetadata.named("end_date_excluded").withIndex(3).ofType(Types.DATE).withSize(8));
        addMetadata(startDate, ColumnMetadata.named("start_date").withIndex(2).ofType(Types.DATE).withSize(8));
    }

}

