package org.everit.jira.configuration.plugin;

/**
 * Exception noting that the syntax of the provided issue key is wrong.
 */
public class IssueKeySyntaxException extends RuntimeException {

  private static final long serialVersionUID = -7227854099251128913L;

  private final String issueKey;

  public IssueKeySyntaxException(final String issueKey) {
    this.issueKey = issueKey;
  }

  public String getIssueKey() {
    return issueKey;
  }
}
