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
package org.everit.jira.configuration.org.everit.jira.configuration.plugin;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.expression.ExpressionCompiler;
import org.everit.expression.ParserConfiguration;
import org.everit.expression.jexl.JexlExpressionCompiler;
import org.everit.templating.CompiledTemplate;
import org.everit.templating.TemplateCompiler;
import org.everit.templating.html.HTMLTemplateCompiler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleReference;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.sal.api.transaction.TransactionTemplate;

/**
 * Testing the addition of a servlet.
 */
public class HelloWorldServlet extends HttpServlet {

  private static final CompiledTemplate TEMPLATE;

  static {
    ExpressionCompiler expressionCompiler = new JexlExpressionCompiler();
    TemplateCompiler htmlTemplateCompiler = new HTMLTemplateCompiler(expressionCompiler);

    TEMPLATE = htmlTemplateCompiler.compile(
        "<html><head><meta name=\"decorator\" content=\"atl.general\">"
            + "<script data-eht-text='webResourceManager.requireResource(\"org.everit.web.partialresponse.jira:org.everit.web.partialresponse\")' data-eht-render=\"'content'\"></script>"
            + "</head><body>"
            + "<div id=\"helloworld\">Hello world</div>"
            + "<script>if (everit.partialresponse){$('#helloworld').css('color', 'red');}</script></body></html>",
        new ParserConfiguration(HelloWorldServlet.class.getClassLoader()));
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {

    StringWriter writer = new StringWriter();
    EveritWebResourceManager webResourceManager = new EveritWebResourceManager(writer);
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("webResourceManager", webResourceManager);
    TEMPLATE.render(writer, vars);
    resp.getWriter().write(writer.toString());

    TransactionTemplate transactionTemplate =
        ComponentAccessor.getOSGiComponentInstanceOfType(TransactionTemplate.class);

    System.out.println(transactionTemplate);

    ClassLoader classLoader = HelloWorldServlet.class.getClassLoader();
    BundleReference bundleReference = (BundleReference) classLoader;

    BundleContext bundleContext = bundleReference.getBundle().getBundleContext();
    try (FileWriter out = new FileWriter("C:/tmp/services.txt")) {
      ServiceReference<?>[] allServiceReferences =
          bundleContext.getAllServiceReferences(null, null);

      for (ServiceReference<?> serviceReference : allServiceReferences) {
        out.write(serviceReference.toString() + " - "
            + serviceReference.getBundle().getSymbolicName() + "\n");
      }
    } catch (InvalidSyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
