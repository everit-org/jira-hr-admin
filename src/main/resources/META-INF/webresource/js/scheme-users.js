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
  userSchemeDataDialog = ajsDialogFromTemplate("#userSchemeDataDialogTemplate", {
    width : 750,
    height : 340,
    id : "user-scheme-data-dialog",
    closeOnOutsideClick : true
  });
  
  userSchemeDataDialogUserPicker = new AJS.SingleSelect({
    element : $("#userscheme-dataform-user-selector"),
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

  $('#userscheme-dataform-action').val('new');
  $('#userscheme-dataform-userholidayamount-id').val('');
  $('#userscheme-dataform-start-date').val('');
  $('#userscheme-dataform-end-date').val('');
  $('#userscheme-dataform-amount').val('');
  $('#userscheme-dataform-description').val('');
  $('#userscheme-dataform-messages').empty();
  userSchemeDataDialog.show();
}