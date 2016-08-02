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

var openNewPublicHolidayDialog = function() {
  $("#public-holiday-dataform-action").val('newPublicHoliday');
  $("#public-holiday-dataform-publicholiday-id").val('');
  $("#public-holiday-dataform-date").val('');
  $("#public-holiday-dataform-replacement-date").val('');
  $("#public-holiday-dataform-description").val('');
  AJS.dialog2("#public-holiday-data-dialog-template").show();
}

var openEditPublicHolidayDialog = function(publicHolidayId) {
  var dataRow = $("#publicHoliday_" + publicHolidayId);

  $("#public-holiday-dataform-action").val('updatePublicHoliday');
  $("#public-holiday-dataform-publicholiday-id").val(publicHolidayId);
  $("#public-holiday-dataform-date").val(dataRow.attr('data-public-holiday-date'));
  $("#public-holiday-dataform-replacement-date").val(
      dataRow.attr('data-public-holiday-replacement-date'));
  $("#public-holiday-dataform-description").val(dataRow.attr('data-public-holiday-description'));

  AJS.dialog2("#public-holiday-data-dialog-template").show();
}

var savePublicHoliday = function(event) {
  var form = $('#public-holiday-dataform')[0];
  var formValid = form.checkValidity();
  if (!formValid) {
    return;
  }

  event.preventDefault();

  var formdata = {
    "schemeId" : $("#scheme-selector").prop("value"),
    "action" : $("#public-holiday-dataform-action").val(),
    "publicHolidayId" : $("#public-holiday-dataform-publicholiday-id").val(),
    "date" : $("#public-holiday-dataform-date").val(),
    "replacementDate" : $("#public-holiday-dataform-replacement-date").val(),
    "description" : $("#public-holiday-dataform-description").val()
  }

  $.ajax({
    url : '#',
    type : 'POST',
    data : formdata
  }).success(function(content) {
    everit.partialresponse.process(content);
    AJS.dialog2("#public-holiday-data-dialog-template").hide();
    processRuntimeAlerts();
  }).error(function(resp) {
    if (resp.status == 400) {
      everit.partialresponse.process(resp.responseText);
      processRuntimeAlerts();
    }
  });
}

var publicHolidayYearChange = function(event) {
  event.preventDefault();
  
  var formdata = {
      "schemeId" : $("#scheme-selector").prop("value"),
      "action" : "publicHolidayYearChange",
      "year" : $("#public-holiday-year-selector").prop('value')
    }
  
  $.ajax({
    url : '#',
    type : 'GET',
    data : formdata
  }).success(function(content) {
    everit.partialresponse.process(content);
    processRuntimeAlerts();
  });
}

var deletePublicHoliday = function(publicHolidayId) {
  var formdata = {
      "schemeId" : $("#scheme-selector").prop("value"),
      "action" : "publicHolidayDelete",
      "year" : $("#public-holiday-year-selector").prop('value'),
      "publicHolidayId" : publicHolidayId
    }
  
  $.ajax({
    url : '#',
    type : 'POST',
    data : formdata
  }).success(function(content) {
    everit.partialresponse.process(content);
    processRuntimeAlerts();
  });
}