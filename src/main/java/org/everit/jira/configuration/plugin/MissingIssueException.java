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
package org.everit.jira.configuration.plugin;

/**
 * Exception that is thrown when an issue is not available in the database.
 */
public class MissingIssueException extends RuntimeException {

  private static final long serialVersionUID = 6622349179982902319L;
  private final String issue;

  public MissingIssueException(final String issue) {
    this.issue = issue;
  }

  public String getIssue() {
    return issue;
  }
}
