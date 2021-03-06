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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.everit.jira.hr.admin.util.LocalizedTemplate;

public class PaginationComponent {

  private static final String TEMPLATE_BASE_PAGINATION = "META-INF/component/pagination";

  private static final LocalizedTemplate TEMPLATE_PAGINATION;

  static {
    ClassLoader classLoader = PaginationComponent.class.getClassLoader();
    TEMPLATE_PAGINATION = new LocalizedTemplate(TEMPLATE_BASE_PAGINATION, classLoader);
  }

  public String render(final int pageIndex, final int pageCount,
      final String pageSwitchJavascriptFunction, final Locale locale) {
    Map<String, Object> vars = new HashMap<>();
    vars.put("currentPage", pageIndex);
    vars.put("pageCount", pageCount);

    int firstSelectablePage = pageIndex;
    int lastSelectablePage = pageIndex;

    final int maxSelectablePageCount = 5;
    int i = maxSelectablePageCount - 1;

    while (i > 0 && (firstSelectablePage > 1 || lastSelectablePage < pageCount)) {
      if (firstSelectablePage > 1) {
        firstSelectablePage--;
        i--;
      }
      if (lastSelectablePage < pageCount) {
        lastSelectablePage++;
        i--;
      }
    }

    vars.put("firstSelectablePage", firstSelectablePage);
    vars.put("lastSelectablePage", lastSelectablePage);
    vars.put("pageSwitchJSFunction", pageSwitchJavascriptFunction);

    StringWriter sw = new StringWriter();
    TEMPLATE_PAGINATION.render(sw, vars, locale, "content");
    return sw.toString();
  }
}
