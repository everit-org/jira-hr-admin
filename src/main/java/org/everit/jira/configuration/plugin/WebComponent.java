package org.everit.jira.configuration.plugin;

import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.web.partialresponse.PartialResponseBuilder;

/**
 * Web component.
 */
public interface WebComponent {

  Set<String> getSupportedActions();

  Set<String> getSupportedFragments();

  void processAction(HttpServletRequest request, HttpServletResponse response);

  void render(HttpServletRequest request, HttpServletResponse response);

  void render(HttpServletRequest request, PartialResponseBuilder prb, Locale locale);
}
