[#-- @ftlvariable name="action" type="org.jfrog.bamboo.admin.ExistingArtifactoryServerAction" --]
[#-- @ftlvariable name="" type="org.jfrog.bamboo.admin.ExistingArtifactoryServerAction" --]

<div class="toolbar">
    <div class="aui-toolbar inline">
        <ul class="toolbar-group">
            <li class="toolbar-item">
                <a class="toolbar-trigger"
                   href="[@s.url action='createArtifactoryServer' namespace='/admin' /]">
                [@s.text name='artifactory.server.add' /]</a>
            </li>
        </ul>
    </div>
</div>
[@ui.header pageKey="artifactory.server.manage.heading" descriptionKey="artifactory.server.manage.description"/]

[@ui.bambooPanel]

<div>
<table id="existingArtifactoryServer" class="aui">
    <thead>
    <tr>
        <th>Artifactory Server URL</th>
        <th>Username</th>
        <th>Timeout</th>
        <th class="operations">Operations</th>
    </tr>
    </thead>
    [#if action.getServerConfigs()?has_content]
        [#foreach serverConfig in serverConfigs]
            <tr>
                <td>
                ${serverConfig.url}
                </td>
                <td>
                ${serverConfig.username}
                </td>
                <td>
                ${serverConfig.timeout}
                </td>
                <td class="operations">
                    <a id="editServer-${serverConfig.id}" href="[@ww.url action='editServer' serverId=serverConfig.id/]">
                        Edit
                    </a>
                    |
                    <a id="deleteServer-${serverConfig.id}"
                       href="[@ww.url action='confirmDeleteServer' namespace="admin" serverId=serverConfig.id returnUrl=currentUrl/]">
                        Delete
                    </a>
                </td>
            </tr>
        [/#foreach]
    [#else]
        <tr>
            <td class="labelPrefixCell" colspan="4">
                [@ww.text name="artifactory.server.manage.none"/]
            </td>
        </tr>
    [/#if]
</table>
</div>
[/@ui.bambooPanel]

[@dj.simpleDialogForm triggerSelector=".delete" width=560 height=400 headerKey="artifactory.server.delete" submitCallback="reloadThePage"/]

[#--[@cp.entityPagination actionUrl='${req.contextPath}/admin/manageArtifactoryServers.action?' paginationSupport=paginationSupport /]--]
