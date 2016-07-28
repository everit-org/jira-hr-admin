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

if (typeof $ === 'undefined') {
  $ = AJS.$;
}

if (!$.parseHTML) {
  $.parseHTML = function(str) {
    var tmp = document.implementation.createHTMLDocument("Everit Jira Configuration Plugin");
    tmp.body.innerHTML = str;
    return tmp.body.children;
  }
}

var processRuntimeAlerts = function() {
  $('.everit-jira-runtimealert').each(function() {
    var thisObj = $(this);
    var alertType = thisObj.attr('data-everit-jira-alert-type');
    var alertMessage = thisObj.attr('data-everit-jira-alert-message');
    thisObj.remove();
    if (alertType == 'info') {
      AJS.messages.generic({
        body : alertMessage
      });
    } else if (alertType == 'error') {
      AJS.messages.error({
        body : alertMessage
      });
    }
  });
}

var ajsDialogFromTemplate = function(templateElementSelector, configuration) {
  var dialog = new AJS.Dialog(configuration);
  var template = $(templateElementSelector);

  dialog.addHeader(template.attr('data-jira-modal-title'));
  dialog.addPanel("Panel", template.text(), "panel-body");

  template.remove();
  var dialogId = configuration["id"];

  AJS.$('#' + dialogId + ' .aui-date-picker').each(function() {
    new AJS.DatePicker(this, {
      'overrideBrowserDefault' : true
    });
  });

  JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [ $('#' + dialogId) ]);

  var dialogElementObj = $('#' + dialogId);
  $('select.aui-ss-select', dialogElementObj).each(function() {
    var thisObj = $(this);
    var thisObjRequired = thisObj.prop('required');
    if (thisObjRequired) {
      thisObj.prop('required', false);
      $('#' + thisObj.prop('id') + '-field').prop('required', true);
    }
  });

  return dialog;
}
