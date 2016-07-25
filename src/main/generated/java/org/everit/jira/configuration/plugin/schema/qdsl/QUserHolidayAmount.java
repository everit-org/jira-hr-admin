package org.everit.jira.configuration.plugin.schema.qdsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QUserHolidayAmount is a Querydsl query type for QUserHolidayAmount
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QUserHolidayAmount extends com.querydsl.sql.RelationalPathBase<QUserHolidayAmount> {

    private static final long serialVersionUID = 871682567;

    public static final QUserHolidayAmount userHolidayAmount = new QUserHolidayAmount("everit_jira_user_hday_amount");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QUserHolidayAmount> userHolidayAmountPK = createPrimaryKey(userHolidayAmountId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QDateRange> dateRangeFK = createForeignKey(dateRangeId, "date_range_id");

    }

    public final NumberPath<Integer> amount = createNumber("amount", Integer.class);

    public final NumberPath<Long> dateRangeId = createNumber("dateRangeId", Long.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> userHolidayAmountId = createNumber("userHolidayAmountId", Long.class);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QUserHolidayAmount(String variable) {
        super(QUserHolidayAmount.class, forVariable(variable), "public", "everit_jira_user_hday_amount");
        addMetadata();
    }

    public QUserHolidayAmount(String variable, String schema, String table) {
        super(QUserHolidayAmount.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUserHolidayAmount(Path<? extends QUserHolidayAmount> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_user_hday_amount");
        addMetadata();
    }

    public QUserHolidayAmount(PathMetadata metadata) {
        super(QUserHolidayAmount.class, metadata, "public", "everit_jira_user_hday_amount");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(amount, ColumnMetadata.named("amount_").withIndex(4).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(dateRangeId, ColumnMetadata.named("date_range_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(description, ColumnMetadata.named("description_").withIndex(5).ofType(Types.VARCHAR).withSize(2000));
        addMetadata(userHolidayAmountId, ColumnMetadata.named("user_hday_amount_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(userId, ColumnMetadata.named("user_id").withIndex(3).ofType(Types.BIGINT).withSize(19).notNull());
    }

}

