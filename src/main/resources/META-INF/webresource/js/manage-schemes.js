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

var schemeDataDialog = null;

$(function() {
  schemeDataDialog = ajsDialogFromTemplate("#schemeDataDialog", {
    width : 550,
    height : 140,
    id : "scheme-data-dialog",
    closeOnOutsideClick : true
  });
});

var openNewSchemeDialog = function(event) {
  event.preventDefault();
  $("#scheme-id-input").val("");
  $("#scheme-name-input").val("");
  schemeDataDialog.show();
}

var openEditSchemeDialog = function(event) {
  event.preventDefault();

  var schemeId = $("#scheme-selector").prop("value");
  $("#scheme-id-input").val(schemeId);
  $("#scheme-name-input").val($("#scheme-selector option:selected").text());

  schemeDataDialog.show();
}

var saveNewScheme = function() {
  var schemeName = $("#scheme-name-input").val();
  $.ajax({
    url : '#',
    type : 'POST',
    data : {
      "action" : "saveNewScheme",
      "schemeName" : schemeName
    }
  }).success(function(content) {
    everit.partialresponse.process(content);
    schemeDataDialog.hide();
    processRuntimeAlerts();
  })
}

var updateScheme = function() {
  var schemeId = $("#scheme-id-input").val();
  var schemeName = $("#scheme-name-input").val();
  $.ajax({
    url : '#',
    type : 'POST',
    data : {
      "action" : "updateScheme",
      "schemeId" : schemeId,
      "schemeName" : schemeName
    }
  }).success(function(content) {
    everit.partialresponse.process(content);
    schemeDataDialog.hide();
    processRuntimeAlerts();
  })
}

var saveScheme = function(event) {
  event.preventDefault();
  var schemeId = $("#scheme-id-input").val();

  if (schemeId == "") {
    saveNewScheme();
  } else {
    updateScheme();
  }
}

var deleteScheme = function() {
  var schemeId = $("#scheme-selector").prop("value");
  $.ajax({
    url : '#',
    type : 'POST',
    data : {
      "action" : "deleteScheme",
      "schemeId" : schemeId
    }
  }).success(function(content) {
    everit.partialresponse.process(content);
    schemeDataDialog.hide();
    processRuntimeAlerts();
  })
}

var handleClientSchemeChange = function() {
  var schemeId = $("#scheme-selector").prop("value");

  var formdata = {
    event : "schemeChange"
  };
  if (schemeId == "") {
    $("#scheme-edit-button").prop('disabled', true);
    $("#scheme-delete-button").prop('disabled', true);
  } else {
    $("#scheme-edit-button").prop('disabled', false);
    $("#scheme-delete-button").prop('disabled', false);
    formdata["schemeId"] = schemeId;
  }

  $.ajax({
    url : '#',
    type : 'GET',
    data : formdata
  }).success(function(content) {
    everit.partialresponse.process(content);
    processRuntimeAlerts();
    AJS.tabs.setup();
    JIRA.trigger(JIRA.Events.NEW_CONTENT_ADDED, [$(".scheme-change-target")]);
  })
}
