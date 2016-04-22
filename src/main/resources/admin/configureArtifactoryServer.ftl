[#if mode == 'edit' ]
    [#assign targetAction = 'updateServer']
    [#assign titleText = 'artifactory.server.edit' /]
<html>
<head><title>Update Artifactory Server</title></head>
<body>
[#else]
    [#assign targetAction = 'createArtifactoryServer']
<html>
<head><title>Create Artifactory Server</title></head>
<body>
[/#if]

[#assign cancelUri = '/admin/manageArtifactoryServers.action' /]

<div class="paddedClearer"></div>
[@ww.form action=targetAction submitLabelKey='global.buttons.update'
titleKey='artifactory.server.details'
cancelUri=cancelUri
descriptionKey='artifactory.server.description'
showActionErrors='true']

    [@ww.param name='buttons']
        [@ww.submit value="Test" name="artifactoryTest" theme='simple' /]
    [/@ww.param]

    [@ww.hidden name='serverId'/]
    [@ww.textfield labelKey="artifactory.server.url" name="url" required="true"/]
    [@ww.textfield labelKey='artifactory.server.username' name="username"/]
    [@ww.password labelKey='artifactory.server.password' name="password" showPassword='true'/]
    [@ww.textfield labelKey='artifactory.server.timeout' name="timeout" required="true"/]
[/@ww.form]
</body>
