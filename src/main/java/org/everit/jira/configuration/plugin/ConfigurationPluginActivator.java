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

import java.sql.Connection;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.atlassian.jira.ofbiz.DefaultOfBizConnectionFactory;

import liquibase.Liquibase;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.osgi.OSGiResourceAccessor;

/**
 * The activator class tries upgrading the database schema.
 */
public class ConfigurationPluginActivator implements BundleActivator {

  @Override
  public void start(final BundleContext context) throws Exception {
    try (Connection connection = DefaultOfBizConnectionFactory.getInstance().getConnection()) {
      DatabaseConnection databaseConnection = new JdbcConnection(connection);
      Liquibase liquibase =
          new Liquibase("META-INF/liquibase/org.everit.jira.configuration.changelog.xml",
              new OSGiResourceAccessor(context.getBundle()), databaseConnection);

      liquibase.update((String) null);
    }
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    // TODO Auto-generated method stub

  }

}
