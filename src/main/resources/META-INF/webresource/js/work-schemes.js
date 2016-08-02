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

var openNewWeekdayDialog = function() {
  $("#workday-dataform-action").val('newWeekday');
  $("#weekday-dataform-weekday-selector").prop('value', '1');
  $("#weekday-dataform-start-time").val('');
  $("#weekday-dataform-duration").val('');
  AJS.dialog2("#weekday-data-dialog").show();
}

var saveNewWeekday = function(event) {
  event.preventDefault();
  var formdata = {
    "schemeId" : $("#scheme-selector").prop('value'),
    "action" : $("#workday-dataform-action").val(),
    "weekday" : $("#weekday-dataform-weekday-selector").prop('value'),
    "start-time" : $("#weekday-dataform-start-time").val(),
    "duration" : $("#weekday-dataform-duration").val()
  }

  $.ajax({
    url : '#',
    type : 'POST',
    data : formdata
  }).success(function(content) {
    everit.partialresponse.process(content);
    AJS.dialog2("#weekday-data-dialog").hide();
    processRuntimeAlerts();
  }).error(function(resp) {
    if (resp.status == 400) {
      everit.partialresponse.process(resp.responseText);
      processRuntimeAlerts();
    }
  });
}

var deleteWeekdayRecord = function(recordId) {
  var formdata = {
      "schemeId" : $("#scheme-selector").prop('value'),
      "action" : 'deleteWeekday',
      "weekdayRecordId" : recordId
    }

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
