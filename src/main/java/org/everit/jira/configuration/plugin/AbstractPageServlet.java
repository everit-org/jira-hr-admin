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
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.everit.expression.ExpressionCompiler;
import org.everit.expression.ParserConfiguration;
import org.everit.expression.jexl.JexlExpressionCompiler;
import org.everit.jira.configuration.plugin.util.ResourceBundleMap;
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
      ClassLoader classLoader = this.getClass().getClassLoader();
      pageTemplate = htmlTemplateCompiler.compile(IOUtils
          .toString(classLoader.getResource(getPageId() + ".html"), "UTF8"),
          new ParserConfiguration(classLoader));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  protected boolean checkWebSudo(final HttpServletRequest req, final HttpServletResponse resp) {
    if (!isWebSudoNecessary()) {
      return true;
    }

    WebSudoManager webSudoManager =
        ComponentAccessor.getOSGiComponentInstanceOfType(WebSudoManager.class);

    if (!webSudoManager.canExecuteRequest(req)) {
      webSudoManager.enforceWebSudoProtection(req, resp);
      return false;
    }
    return true;
  }

  protected Map<String, Object> createCommonVars(final HttpServletRequest req,
      final HttpServletResponse resp) throws IOException {
    EveritWebResourceManager webResourceManager = new EveritWebResourceManager(resp.getWriter());
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("webResourceManager", webResourceManager);
    vars.put("request", req);
    vars.put("response", resp);
    vars.put("messages", getMessages(resp));
    return vars;
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {

    if (!checkWebSudo(req, resp)) {
      return;
    }

    Map<String, Object> vars = createCommonVars(req, resp);
    doGetInternal(req, resp, vars);
  }

  protected void doGetInternal(final HttpServletRequest req, final HttpServletResponse resp,
      final Map<String, Object> vars) throws ServletException, IOException {

    pageTemplate.render(resp.getWriter(), vars);
  }

  /**
   * Gets the current pages localization properties to be able to use it on child pages ajax
   * responses.
   */
  protected ResourceBundleMap getMessages(final HttpServletResponse resp) {
    ClassLoader lClassLoader = this.getClass().getClassLoader();
    URL baseResourceURL = lClassLoader.getResource(getPageId() + ".properties");
    ResourceBundle resourceBundle;
    if (baseResourceURL != null) {
      resourceBundle = ResourceBundle.getBundle(
          getPageId(), resp.getLocale(), lClassLoader);
    } else {
      try {
        resourceBundle = new PropertyResourceBundle(new StringReader(""));
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    return new ResourceBundleMap(resourceBundle);
  }

  protected abstract String getPageId();

  protected boolean isWebSudoNecessary() {
    return false;
  }
}
