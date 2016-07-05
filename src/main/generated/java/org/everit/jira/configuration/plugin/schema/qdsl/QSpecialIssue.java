package org.everit.jira.configuration.plugin.schema.qdsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSpecialIssue is a Querydsl query type for QSpecialIssue
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSpecialIssue extends com.querydsl.sql.RelationalPathBase<QSpecialIssue> {

    private static final long serialVersionUID = -1655129826;

    public static final QSpecialIssue specialIssue = new QSpecialIssue("everit_jira_spec_issue");

    public class PrimaryKeys {

        public final com.querydsl.sql.PrimaryKey<QSpecialIssue> specialIssuePK = createPrimaryKey(specIssueId);

    }

    public final NumberPath<Long> issueId = createNumber("issueId", Long.class);

    public final StringPath specialty = createString("specialty");

    public final NumberPath<Long> specIssueId = createNumber("specIssueId", Long.class);

    public final PrimaryKeys pk = new PrimaryKeys();

    public QSpecialIssue(String variable) {
        super(QSpecialIssue.class, forVariable(variable), "public", "everit_jira_spec_issue");
        addMetadata();
    }

    public QSpecialIssue(String variable, String schema, String table) {
        super(QSpecialIssue.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSpecialIssue(Path<? extends QSpecialIssue> path) {
        super(path.getType(), path.getMetadata(), "public", "everit_jira_spec_issue");
        addMetadata();
    }

    public QSpecialIssue(PathMetadata metadata) {
        super(QSpecialIssue.class, metadata, "public", "everit_jira_spec_issue");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(issueId, ColumnMetadata.named("issue_id").withIndex(2).ofType(Types.BIGINT).withSize(19).notNull());
        addMetadata(specialty, ColumnMetadata.named("specialty").withIndex(3).ofType(Types.VARCHAR).withSize(60).notNull());
        addMetadata(specIssueId, ColumnMetadata.named("spec_issue_id").withIndex(1).ofType(Types.BIGINT).withSize(19).notNull());
    }

}
