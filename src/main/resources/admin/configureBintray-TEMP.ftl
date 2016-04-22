[#-- @ftlvariable name="action" type="org.jfrog.bamboo.admin.ConfigureBintrayAction" --]
[#-- @ftlvariable name="" type="org.jfrog.bamboo.admin.ConfigureBintrayAction" --]
[#if bintrayConfig?exists]
    [#assign editMode = true /]
    [#assign cancelUri='/admin/viewBintray.action' /]
[#else]
    [#assign editMode = false /]
    [#assign cancelUri='/admin/administer.action' /]
[/#if]
<html>
<head>
    <title>[@ww.text name='config.bintray.title' /]</title>
    <meta name="adminCrumb" content="configureEmail">
</head>
<body>
<h1>[@ww.text name='config.bintray.title' /]</h1>
[@ww.form action='saveBintrayConfigr.action'
titleKey='config.bintray.title'
descriptionKey='config.bintray.description'
submitLabelKey='global.buttons.update'
cancelUri=cancelUri
]
    [@ww.param name='buttons'][@ww.submit value=action.getText('global.buttons.test') name="sendTest" /][/@ww.param]

    [@ww.textfield labelKey='config.bintray.name' name="bintrayUserName" required="false" /]
    [#if editMode && bintrayUserName?has_content]
        [@ww.checkbox labelKey='config.bintray.password.change' toggle='true' id='passwordChange' name='passwordChange' /]
        [@ui.bambooSection dependsOn='passwordChange' showOn='true']
            [@ww.password labelKey='config.bintray.password' name="password" showPassword="true" required="false" /]
        [/@ui.bambooSection]
    [#else]
        [@ww.password labelKey='config.bintray.password' name="password" showPassword="true" required="false" /]
    [/#if]

    [@ww.textfield labelKey='config.bintray.sonotype.name' name="userName" required="false" /]
    [#if editMode && sonatypeOssUsername?has_content]
        [@ww.checkbox labelKey='config.sonotype.password.change' toggle='true' id='passwordChange' name='passwordChange' /]
        [@ui.bambooSection dependsOn='passwordChange' showOn='true']
            [@ww.password labelKey='config.bintray.sonotype.password' name="password" showPassword="true" required="false" /]
        [/@ui.bambooSection]
    [#else]
        [@ww.password labelKey='config.bintray.sonotype.password' name="password" showPassword="true" required="false" /]
    [/#if]

[/@ww.form]

</body>
</html>
