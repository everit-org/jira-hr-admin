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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.jira.hr.admin.util.LocalizedTemplate;
import org.everit.web.partialresponse.PartialResponseBuilder;

public class ManageSchemeComponent {

  public static class SchemeDTO {
    public String name;

    public long schemeId;
  }

  public static interface SchemeSelectionChangeProcessor {
    void apply(HttpServletRequest request, Long schemeId, PartialResponseBuilder prb,
        Locale locale);
  }

  private static final String ACTION_DELETE_SCHEME = "deleteScheme";

  private static final String ACTION_SAVE_NEW_SCHEME = "saveNewScheme";

  private static final String ACTION_UPDATE_SCHEME = "updateScheme";

  private static final Set<String> SUPPORTED_ACTIONS;

  private static final LocalizedTemplate TEMPLATE = new LocalizedTemplate(
      "/META-INF/component/manage_schemes",
      ManageSchemeComponent.class.getClassLoader());

  static {
    Set<String> supportedActions = new HashSet<>();
    supportedActions.add(ACTION_SAVE_NEW_SCHEME);
    supportedActions.add(ACTION_UPDATE_SCHEME);
    supportedActions.add(ACTION_DELETE_SCHEME);
    SUPPORTED_ACTIONS = Collections.unmodifiableSet(supportedActions);
  }

  private final Consumer<Long> deleteSchemeFunction;

  private final Supplier<Collection<SchemeDTO>> schemeCollectionSupplier;

  private final Function<String, Long> schemeSaveFunction;

  private final SchemeSelectionChangeProcessor schemeSelectionChangeConsumer;

  private final Consumer<SchemeDTO> schemeUpdateFunction;

  public ManageSchemeComponent(final Supplier<Collection<SchemeDTO>> schemeCollectionSupplier,
      final Function<String, Long> schemeSaveFunction,
      final Consumer<SchemeDTO> schemeUpdateFunction,
      final Consumer<Long> deleteSchemeFunction,
      final SchemeSelectionChangeProcessor schemeSelectionChangeConsumer) {
    this.schemeCollectionSupplier = schemeCollectionSupplier;
    this.schemeSaveFunction = schemeSaveFunction;
    this.schemeUpdateFunction = schemeUpdateFunction;
    this.deleteSchemeFunction = deleteSchemeFunction;
    this.schemeSelectionChangeConsumer = schemeSelectionChangeConsumer;
  }

  public Set<String> getSupportedActions() {
    return SUPPORTED_ACTIONS;
  }

  public void processAction(final HttpServletRequest req, final HttpServletResponse resp) {
    String action = req.getParameter("action");
    if (ACTION_SAVE_NEW_SCHEME.equals(action)) {
      processSaveNewSchemeAction(req, resp);
    } else if (ACTION_DELETE_SCHEME.equals(action)) {
      processDeleteSchemeAction(req, resp);
    } else if (ACTION_UPDATE_SCHEME.equals(action)) {
      processUpdateSchemeAction(req, resp);
    }
  }

  private void processDeleteSchemeAction(final HttpServletRequest req,
      final HttpServletResponse resp) {

    String schemeIdParam = req.getParameter("schemeId");
    Long schemeId = Long.parseLong(schemeIdParam);
    deleteSchemeFunction.accept(schemeId);
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      prb.append("#aui-message-bar",
          (writer) -> AlertComponent.INSTANCE.render(writer, "Scheme deleted", "info",
              resp.getLocale()));

      prb.replace("#scheme-selector-form",
          (writer) -> renderFragment(null, writer, resp.getLocale(), "scheme-selector-form"));

      schemeSelectionChangeConsumer.apply(req, null, prb, resp.getLocale());
    }
  }

  private void processSaveNewSchemeAction(final HttpServletRequest req,
      final HttpServletResponse resp) {
    String schemeName = req.getParameter("schemeName");
    long schemeId = schemeSaveFunction.apply(schemeName);
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      prb.append("#aui-message-bar",
          (writer) -> AlertComponent.INSTANCE.render(writer, "New Scheme created", "info",
              resp.getLocale()));

      prb.replace("#scheme-selector-form",
          (writer) -> renderFragment(schemeId, writer, resp.getLocale(), "scheme-selector-form"));

      schemeSelectionChangeConsumer.apply(req, schemeId, prb, resp.getLocale());
    }
  }

  private void processUpdateSchemeAction(final HttpServletRequest req,
      final HttpServletResponse resp) {
    SchemeDTO schemeDTO = new SchemeDTO();
    schemeDTO.name = req.getParameter("schemeName");
    schemeDTO.schemeId = Long.parseLong(req.getParameter("schemeId"));
    schemeUpdateFunction.accept(schemeDTO);
    try (PartialResponseBuilder prb = new PartialResponseBuilder(resp)) {
      prb.append("#aui-message-bar",
          (writer) -> AlertComponent.INSTANCE.render(writer, "Scheme updated", "info",
              resp.getLocale()));

      prb.replace("#scheme-selector-form",
          (writer) -> renderFragment(schemeDTO.schemeId, writer, resp.getLocale(),
              "scheme-selector-form"));

    }
  }

  public String render(final HttpServletRequest request, final Locale locale) {
    StringWriter sw = new StringWriter();

    String schemeIdParam = request.getParameter("schemeId");
    Long schemeId = (schemeIdParam != null) ? Long.parseLong(schemeIdParam) : null;

    return renderFragment(schemeId, sw, locale, "body");
  }

  private String renderFragment(final Long schemeId, final Writer sw, final Locale locale,
      final String fragment) {
    Map<String, Object> vars = new HashMap<>();
    vars.put("schemeCollection", schemeCollectionSupplier.get());
    vars.put("selectedSchemeId", schemeId);
    TEMPLATE.render(sw, vars, locale, fragment);
    return sw.toString();
  }

}
