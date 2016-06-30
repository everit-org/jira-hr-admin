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
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.everit.expression.ExpressionCompiler;
import org.everit.expression.ParserConfiguration;
import org.everit.expression.jexl.JexlExpressionCompiler;
import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;
import org.everit.templating.html.HTMLTemplateCompiler;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.sal.api.websudo.WebSudoManager;

/**
 * Abstract servlet that others can inherit from to implement a page.
 */
public abstract class AbstractPageServlet extends HttpServlet {

  private static final long serialVersionUID = -3661512830445457663L;

  protected final CompiledTemplate pageTemplate;

  /**
   * Initializes the compiled page template.
   */
  public AbstractPageServlet() {
    ExpressionCompiler expressionCompiler = new JexlExpressionCompiler();
    TemplateCompiler htmlTemplateCompiler = new HTMLTemplateCompiler(expressionCompiler);

    try {
      pageTemplate = htmlTemplateCompiler.compile(IOUtils
          .toString(GlobalPermissionsServlet.class
              .getResource(getPageTemplateResourceURL()), "UTF8"),
          new ParserConfiguration(GlobalPermissionsServlet.class.getClassLoader()));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {

    if (isWebSudoNecessary()) {
      WebSudoManager webSudoManager =
          ComponentAccessor.getOSGiComponentInstanceOfType(WebSudoManager.class);

      if (!webSudoManager.canExecuteRequest(req)) {
        webSudoManager.enforceWebSudoProtection(req, resp);
        return;
      }
    }

    EveritWebResourceManager webResourceManager = new EveritWebResourceManager(resp.getWriter());
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("webResourceManager", webResourceManager);
    vars.put("request", req);
    vars.put("response", resp);

    doGetInternal(req, resp, vars);
  }

  protected void doGetInternal(final HttpServletRequest req, final HttpServletResponse resp,
      final Map<String, Object> vars) throws ServletException, IOException {

    pageTemplate.render(resp.getWriter(), vars);
  }

  protected abstract String getPageTemplateResourceURL();

  protected boolean isWebSudoNecessary() {
    return false;
  }
}
