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

var userSchemeDataDialog = null;
var userSchemeDataDialogUserPicker = null;

$(function() {
  userSchemeDataDialog = ajsDialogFromTemplate(
      "#scheme-user-data-dialog-template", {
        width : 500,
        height : 280,
        id : "scheme-user-data-dialog",
        closeOnOutsideClick : true
      });

  userSchemeDataDialogUserPicker = new AJS.SingleSelect({
    element : $("#scheme-user-dataform-user-selector"),
    submitInputVal : true,
    showDropdownButton : false,
    errorMessage : AJS.format("There is no such user \'\'{0}\'\'.", "'{0}'"),
    ajaxOptions : {
      url : AJS.contextPath() + "/rest/api/1.0/users/picker",
      query : true, // keep going back to the sever for each keystroke
      data : {
        showAvatar : true
      },
      formatResponse : JIRA.UserPickerUtil.formatResponse
    }
  });

});

var openAddSchemeUserDialog = function() {
  userSchemeDataDialogUserPicker.clear();

  $('#scheme-user-dataform-action').val('scheme-user-savenew');
  $('#scheme-user-dataform-record-id').val('');
  $('#scheme-user-dataform-start-date').val('');
  $('#scheme-user-dataform-end-date').val('');
  $('#scheme-user-dataform-messages').empty();
  userSchemeDataDialog.show();
}

var createUserSchemeFormDataWithSearchFields = function() {
  var table = $("#scheme-user-table");
  var userFilter = table.attr("data-scheme-user-filter-user");
  var currentTimeRangesFilter = table
      .attr("data-scheme-user-filter-currentTimeRanges");
  var pageIndex = table.attr("data-scheme-user-page-index");
  var schemeId = $("#scheme-selector").prop('value');

  var formdata = {
    'schemeUsersCurrentFilter' : currentTimeRangesFilter,
    'pageIndex' : pageIndex
  };

  if (userFilter) {
    formdata['schemeUsersUserFilter'] = userFilter;
  }

  if (schemeId) {
    formdata['schemeId'] = schemeId;
  }

  return formdata;
}

var deleteSchemeUserRecord = function(userSchemeId) {
  var formdata = createUserSchemeFormDataWithSearchFields();
  formdata['action'] = 'scheme-user-delete';
  formdata['scheme-user-userscheme-id'] = userSchemeId;

  $.ajax({
    url : '#',
    type : 'POST',
    data : formdata
  }).success(function(content) {
    everit.partialresponse.process(content);
    processRuntimeAlerts();
  }).error(function(resp) {
    if (resp.status == 400) {
      everit.partialresponse.process(resp.responseText);
      processRuntimeAlerts();
    }
  });
}

var saveSchemeUserRecord = function(event) {
  var form = $('#scheme-user-dataform')[0];
  var formValid = form.checkValidity();
  if (!formValid) {
    return;
  }

  event.preventDefault();

  var formdata = createUserSchemeFormDataWithSearchFields();
  formdata['action'] = $('#scheme-user-dataform-action').val();
  formdata['record-id'] = $('#scheme-user-dataform-record-id').val();
  formdata['user'] = $('#scheme-user-dataform-user-selector').prop('value');
  formdata['start-date'] = $('#scheme-user-dataform-start-date').val();
  formdata['end-date'] = $('#scheme-user-dataform-end-date').val();

  AJS.$('#scheme-user-dataform-savebutton').spin();
  $.ajax({
    url : '#',
    type : 'POST',
    data : formdata
  }).success(function(content) {
    everit.partialresponse.process(content);
    userSchemeDataDialog.hide();
    processRuntimeAlerts();
  }).error(function(resp) {
    if (resp.status == 400) {
      everit.partialresponse.process(resp.responseText);
      processRuntimeAlerts();
    }
  }).complete(function() {
    $('#scheme-user-dataform-savebutton').spinStop();
  });
}

var filterSchemeUsers = function(event) {
  event.preventDefault();

  var schemeId = $("#scheme-selector").prop('value');
  
  
  var formdata = {
    'action' : 'scheme-user-filter',
    'schemeUsersCurrentFilter' : $('#scheme-user-current-checkbox').is(":checked")
  };

  var userFilter = $('#scheme-user-user-selector').val();
  if (userFilter) {
    formdata['schemeUsersUserFilter'] = userFilter;
  }
  
  if (schemeId) {
    formdata['schemeId'] = schemeId;
  }

  $.ajax({
    url : '#',
    type : 'GET',
    data : formdata
  }).success(function(content) {
    everit.partialresponse.process(content);
    processRuntimeAlerts();
  }).error(function(resp) {
    if (resp.status == 400) {
      everit.partialresponse.process(resp.responseText);
      processRuntimeAlerts();
    }
  });
}
