[#-- @ftlvariable name="action" type="org.jfrog.bamboo.admin.ConfigureBintrayAction" --]
[#-- @ftlvariable name="" type="org.jfrog.bamboo.admin.ConfigureBintrayAction" --]
<html>
<head>
    [@ui.header pageKey="config.bintray.title" title=true/]
    <meta name="adminCrumb" content="configureBintray">
</head>
<body>
    [@ww.form action='saveBintrayConf'
              titleKey='bintray.config.heading'
              descriptionKey='bintray.config.description'
              submitLabelKey='global.buttons.update'
              cancelUri='/admin/manageArtifactoryServers.action'
    ]
    [@ww.param name='buttons'][@ww.submit value=action.getText('global.buttons.test') name="bintrayTest" /][/@ww.param]
        [@ui.bambooSection]
            [@ww.textfield labelKey='bintray.username' name="bintrayUsername" required="false" /]
            [@ww.password labelKey='bintray.apikey' name="bintrayApiKey" showPassword="true" required="false" /]
            [@ww.textfield labelKey='bintray.sonatype.username' name="sonatypeOssUsername" required="false" /]
            [@ww.password labelKey='bintray.sonotype.password' name="sonatypeOssPassword" showPassword="true" required="false" /]
        [/@ui.bambooSection]
[/@ww.form]

</body>
</html>
