package org.everit.jira.configuration.plugin.schema.qdsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QHolidayScheme is a Querydsl query type for QHolidayScheme
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QHolidayScheme extends com.querydsl.sql.RelationalPathBase<QHolidayScheme> {

    private static final long serialVersionUID = 1827250783;

    public static final QHolidayScheme holidayScheme = new QHolidayScheme("everit_jira_holiday_scheme");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QHolidayScheme> holidaySchemePK = createPrimaryKey(holidaySchemeId);

    }

    public class ForeignKeys {

        public final com.querydsl.sql.ForeignKey<QPublicHoliday> _everitJiraPubHdHschFk = createInvForeignKey(holidaySchemeId, "holiday_scheme_id");

        public final com.querydsl.sql.ForeignKey<QShiftedWorkDay> _everitJiraShiftedWdHsFk = createInvForeignKey(holidaySchemeId, "holiday_scheme_id");

        public final com.querydsl.sql.ForeignKey<QUserHolidayScheme> _everitJiraUsHdSchmHsFk = createInvForeignKey(holidaySchemeId, "holiday_scheme_id");

    }

    public final NumberPath<Long> holidaySchemeId = createNumber("holidaySchemeId", Long.class);

    public final StringPath name_ = createString("name_");

    public final PrimaryKeys pk = new PrimaryKeys();

    public final ForeignKeys fk = new ForeignKeys();

    public QHolidayScheme(String variable) {
        super(QHolidayScheme.class, forVariable(variable), "public", "everit_jira_holiday_scheme");
        addMetadata();
    }

    public QHolidayScheme(String variable, String schema, String table) {
        super(QHolidayScheme.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QHolidayScheme(Path<? extends QHolidayScheme> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_holiday_scheme");
        addMetadata();
    }

    public QHolidayScheme(PathMetadata metadata) {
        super(QHolidayScheme.class, metadata, "public", "everit_jira_holiday_scheme");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(holidaySchemeId, ColumnMetadata.named("holiday_scheme_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(name_, ColumnMetadata.named("name_").withIndex(2).ofType(Types.VARCHAR).withSize(60).notNull());
    }

}

