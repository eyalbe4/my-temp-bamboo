
<div class="toolbar">
    <div class="aui-toolbar inline">
        <ul class="toolbar-group">
            <li class="toolbar-item">
                <a class="toolbar-trigger"
                   href="[@s.url action='configureArtifactoryServer' namespace='/admin' /]">
                [@s.text name='artifactory.server.add' /]</a>
            </li>
        </ul>
    </div>
</div>
[@ui.header pageKey="artifactory.server.manage.heading"/]

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
    [#if artifactroryServers!?size>0]
        [#foreach artifactroryServer in artifactroryServers]
            <tr>
                <td>
                    <a href="${artifactroryServer.serverUrl}" target="_blank" >${artifactroryServer.serverUrl}</a>
                </td>
                <td>
                ${artifactroryServer.username}
                </td>
                <td>
                ${artifactroryServer.timeout}
                </td>
                <td class="operations">
                    <a id="editServer-${artifactroryServer.ID}"
                       href="[@ww.url action='editServer' serverId=artifactroryServer.ID/]">
                        Edit
                    </a>
                    |
                    <a id="deleteServer-${artifactroryServer.ID}"
                       href="[@ww.url action='confirmDeleteServer' serverId=artifactroryServer.ID returnUrl=currentUrl/]"
                       href="[@ww.url action='confirmDeleteServer' serverId=artifactroryServer.ID returnUrl=currentUrl/]"
                       class="delete" title="[@ww.text name='artifactory.server.delete' /]">
                        [@ww.text name="global.buttons.delete" /]
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

[@dj.simpleDialogForm triggerSelector=".delete" width=560 height=400 headerKey="artifactory.server.delete"
submitCallback="reloadThePage"/]

