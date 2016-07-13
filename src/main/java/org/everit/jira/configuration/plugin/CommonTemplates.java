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

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.everit.expression.ExpressionCompiler;
import org.everit.expression.ParserConfiguration;
import org.everit.expression.jexl.JexlExpressionCompiler;
import org.everit.jira.configuration.plugin.util.TemplatingUtil;
import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;
import org.everit.templating.html.HTMLTemplateCompiler;

public class CommonTemplates {

  private static final String TEMPLATE_BASE_PAGINATION = "META-INF/templates/pagination";

  private static final CompiledTemplate TEMPLATE_PAGINATION;

  static {
    ExpressionCompiler expressionCompiler = new JexlExpressionCompiler();
    TemplateCompiler htmlTemplateCompiler = new HTMLTemplateCompiler(expressionCompiler);

    ClassLoader classLoader = CommonTemplates.class.getClassLoader();

    try {
      TEMPLATE_PAGINATION =
          htmlTemplateCompiler.compile(
              IOUtils.toString(classLoader.getResource(TEMPLATE_BASE_PAGINATION + ".html"), "UTF8"),
              new ParserConfiguration(classLoader));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public String renderPagination(final int currentPage, final int pageCount,
      final String pageSwitchJSFunction, final Locale locale) {
    Map<String, Object> vars = new HashMap<>();
    vars.put("messages", TemplatingUtil.getMessages(TEMPLATE_BASE_PAGINATION,
        CommonTemplates.class.getClassLoader(), locale));
    vars.put("currentPage", currentPage);
    vars.put("pageCount", pageCount);

    int firstSelectablePage = currentPage;
    int lastSelectablePage = currentPage;

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
    vars.put("pageSwitchJSFunction", pageSwitchJSFunction);

    StringWriter sw = new StringWriter();
    TEMPLATE_PAGINATION.render(sw, vars, "content");
    return sw.toString();
  }
}
