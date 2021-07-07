define('plugin/prnfb/admin', [
 'jquery',
 '@atlassian/aui',
 'plugin/prnfb/utils',
 'wrm/context-path'
], function($, AJS, utils, contextPath) {
 const settingsAdminUrlPostUrl = contextPath() + "/rest/prnfb-admin/1.0/settings";
 let settingsAdminUrl = settingsAdminUrlPostUrl;

 const notificationsAdminUrlPostUrl = contextPath() + "/rest/prnfb-admin/1.0/settings/notifications";
 let notificationsAdminUrl = notificationsAdminUrlPostUrl;

 const buttonsAdminUrlPostUrl = contextPath() + "/rest/prnfb-admin/1.0/settings/buttons";
 let buttonsAdminUrl = buttonsAdminUrlPostUrl;

 let projectKey;
 if ($('#prnfbRepositorySlug').length !== 0) {
  projectKey = $('#prnfbProjectKey').val();
  const repositorySlug = $('#prnfbRepositorySlug').val();

  notificationsAdminUrl = notificationsAdminUrlPostUrl + '/projectKey/' + projectKey + '/repositorySlug/' + repositorySlug;
  buttonsAdminUrl = buttonsAdminUrlPostUrl + '/projectKey/' + projectKey + '/repositorySlug/' + repositorySlug;
 } else if ($('#prnfbProjectKey').length !== 0) {
  projectKey = $('#prnfbProjectKey').val();

  notificationsAdminUrl = notificationsAdminUrlPostUrl + '/projectKey/' + projectKey;
  buttonsAdminUrl = buttonsAdminUrlPostUrl + '/projectKey/' + projectKey;
 }

 $(document)
  .ajaxStart(function() {
   $('.prnfb button').attr('aria-disabled', 'true');
  })
  .ajaxStop(function() {
   $('.prnfb button').attr('aria-disabled', 'false');
  });

 $(document).ready(function() {
  utils.setupForm('#prnfbsettingsadmin', settingsAdminUrl, settingsAdminUrlPostUrl);
  utils.setupForms('#prnfbbuttonadmin', buttonsAdminUrl, buttonsAdminUrlPostUrl);
  utils.setupForms('#prnfbnotificationadmin', notificationsAdminUrl, notificationsAdminUrlPostUrl);
 });
});

AJS.$(document).ready(function() {
 require('plugin/prnfb/admin');
});
