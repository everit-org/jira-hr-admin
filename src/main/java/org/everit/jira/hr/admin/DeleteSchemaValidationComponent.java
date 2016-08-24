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
package org.everit.jira.hr.admin;

import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.everit.jira.hr.admin.util.LocalizedTemplate;

public class DeleteSchemaValidationComponent {
  public static final DeleteSchemaValidationComponent INSTANCE;

  private static final LocalizedTemplate TEMPLATE_ALERT;

  private static final String TEMPLATE_ALERT_BASENAME = "META-INF/component/delete_scheme_validation";

  static {
    ClassLoader classLoader = PaginationComponent.class.getClassLoader();
    TEMPLATE_ALERT = new LocalizedTemplate(TEMPLATE_ALERT_BASENAME, classLoader);
    INSTANCE = new DeleteSchemaValidationComponent();
  }

  public String render(final Locale locale, final Long schemeUserCount) {
    StringWriter sw = new StringWriter();
    render(sw, locale, schemeUserCount);
    return sw.toString();
  }

  public void render(final Writer sw, final Locale locale, final Long schemeUserCount) {
    Map<String, Object> vars = new HashMap<>();
    vars.put("schemeUserCount", schemeUserCount);
    TEMPLATE_ALERT.render(sw, vars, locale, "body");
  }
}
