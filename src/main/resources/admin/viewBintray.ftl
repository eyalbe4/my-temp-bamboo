
<html>
<head>
    <title>[@ww.text name='config.bintray.title' /]</title>
    <meta name="adminCrumb" content="configureBintrayPage">
</head>
<body>
    [@ui.header pageKey="bintray.config.heading" descriptionKey="bintray.config.description"/]
    [@ww.form action="configureBintray.action"
              namespace="/admin"
              id="viewBintrayConfigurationForm"
              submitLabelKey='global.buttons.edit'
    ]

    [@ui.bambooSection]
        [@ww.textfield labelKey='bintray.username' name="bintrayUsername" required="false" disabled=true  /]
        [@ww.textfield labelKey='bintray.sonatype.username' name="sonatypeOssUsername" required="false" disabled=true  /]
    [/@ui.bambooSection]
    [/@ww.form]
</body>
</html>
