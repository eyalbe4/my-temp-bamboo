package org.jfrog.bamboo.bintray;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jfrog.bamboo.admin.ServerConfig;
import org.jfrog.bamboo.bintray.client.BintrayClient;
import org.jfrog.bamboo.util.ActionLog;
import org.jfrog.bamboo.util.BambooBuildInfoLog;
import org.jfrog.build.api.release.BintrayUploadInfoOverride;
import org.jfrog.build.client.ArtifactoryVersion;
import org.jfrog.build.client.bintrayResponse.BintrayResponse;
import org.jfrog.build.extractor.clientConfiguration.client.ArtifactoryBuildInfoClient;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;


/**
 * Push to Bintray Runnable to pass in to a Thread that will preform this task on Bamboo
 *
 * @author Aviad Shikloshi
 */
public class PushToBintrayRunnable implements Runnable {

    private static final String MINIMAL_SUPPORTED_VERSION = "3.6";
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PushToBintrayRunnable.class);

    private BintrayClient bintrayClient;
    private ServerConfig serverConfig;
    private PushToBintrayAction action;
    private ActionLog bintrayLog;
    private String buildName;
    private String buildNumber;


    public PushToBintrayRunnable(PushToBintrayAction pushToBintrayAction, ServerConfig serverConfig, BintrayClient bintrayClient) {
        this.action = pushToBintrayAction;
        this.serverConfig = serverConfig;
        this.bintrayClient = bintrayClient;
//        this.bintrayLog = PushToBintrayAction.context.getActionLog();
//        this.buildName = PushToBintrayAction.context.getBuildKey();
//        this.buildNumber = Integer.toString(PushToBintrayAction.context.getBuildNumber());
    }

    /**
     * Run method to perform Push to Bintray action
     * This method sets the isSuccessfullyDone in the action object to use later in the action view
     */
//    @Override
    public void run() {
        ArtifactoryBuildInfoClient artifactoryClient = null;
        try {
            bintrayLog.logMessage("Starting Push to Bintray action.");
//            PushToBintrayAction.context.getLock().lock();
//            PushToBintrayAction.context.setDone(false);
            artifactoryClient = getArtifactoryBuildInfoClient();
            if (!isValidArtifactoryVersion(artifactoryClient)) {
                bintrayLog.logError("Push to Bintray supported from Artifactory version " + MINIMAL_SUPPORTED_VERSION);
//                PushToBintrayAction.context.setDone(true);
                return;
            }
            performPushToBintray(artifactoryClient);
            if (action.isMavenSync()) {
                bintrayLog.logMessage("Starting MavenSync.");
                mavenCentralSync();
            }
        } catch (Exception e) {
            bintrayLog.logError("Error while trying to Push build to Bintray.", e);
        } finally {
            if (artifactoryClient != null) {
                artifactoryClient.shutdown();
            }
//            PushToBintrayAction.context.setDone(true);
//            PushToBintrayAction.context.getLock().unlock();
        }
    }

    /**
     * Create the relevant objects from input and send it to build info artifactoryClient that will preform the actual push
     * Set the result of the action to true if successful to use in the action view.
     */
    private void performPushToBintray(ArtifactoryBuildInfoClient artifactoryClient) {

        String subject = action.getSubject(),
                repoName = action.getRepository(),
                packageName = action.getPackageName(),
                versionName = action.getVersion(),
                vcsUrl = action.getVcsUrl(),
                signMethod = action.getSignMethod(),
                passphrase = action.getGpgPassphrase();
        List<String> licenses = createLicensesListFromString(action.getLicenses());

        BintrayUploadInfoOverride uploadInfoOverride = new BintrayUploadInfoOverride(subject, repoName, packageName,
                versionName, licenses, vcsUrl);

        BintrayResponse response;
        try {
            response = artifactoryClient.pushToBintray(buildName, buildNumber, signMethod, passphrase, uploadInfoOverride);
            bintrayLog.logMessage(response.toString());
            log.info("Push to Bintray finished: " + response.toString());
        } catch (Exception e) {
            throw new RuntimeException("Push to Bintray failed with Exception.", e);
        }

        if (!response.isSuccessful()) {
            throw new RuntimeException("Push to Bintray failed with Exception");
        }
    }

    /**
     * Trigger's Bintray MavenCentralSync API
     */
    private void mavenCentralSync() {

        if (!Boolean.valueOf(action.isOverrideDescriptorFile())) {
            prepareMavenCentralSync();
        }

        try {
            bintrayLog.logMessage("Syncing version to Maven Central.");
            String response = bintrayClient.mavenCentralSync(action.getSubject(), action.getRepository(),
                    action.getPackageName(), action.getVersion());
            bintrayLog.logMessage(response);
        } catch (Exception e) {
            bintrayLog.logError("Error while trying to sync with Maven Central", e);
        }
    }

    // When using descriptor file with MavenCentralSync we don't have any of the details Bintray is expecting for (package name,
    // version, subject etc. So we must read it from the bintray-info.json file frmo Artifactory.
    private void prepareMavenCentralSync() {
        // fetch the location of the file from Artifactory
        Map<String, Object> response = bintrayClient.getBintrayJsonFileLocation(serverConfig, buildName, buildNumber);
        // parse Artifactory response and get the download URI
        String fileUrl = MavenSyncHelper.getBintrayDescriptorFileUrl(response);
        // download the file to memory
        Map<String, Object> bintrayJsonMap = bintrayClient.downloadBintrayInfoDescriptor(serverConfig, fileUrl);
        // populate the properties we now have to the action context - subject, repo name, package name and version.
        MavenSyncHelper.updateBintrayActionContext(action, bintrayJsonMap);
    }

    private boolean isValidArtifactoryVersion(ArtifactoryBuildInfoClient client) {
        boolean validVersion = false;
        try {
            ArtifactoryVersion version = client.verifyCompatibleArtifactoryVersion();
            validVersion = version.isAtLeast(new ArtifactoryVersion(MINIMAL_SUPPORTED_VERSION));
        } catch (Exception e) {
            bintrayLog.logError("Error while checking Artifactory version", e);
        }
        return validVersion;
    }

    private ArtifactoryBuildInfoClient getArtifactoryBuildInfoClient() {
        String username = serverConfig.getUsername();
        String password = serverConfig.getPassword();
        String artifactoryUrl = serverConfig.getUrl();
        return new ArtifactoryBuildInfoClient(artifactoryUrl, username, password, new BambooBuildInfoLog(log));
    }

    private List<String> createLicensesListFromString(String licenses) {
        String[] licensesArray = StringUtils.split(licenses, ",");
        for (int i = 0; i < licensesArray.length; i++) {
            licensesArray[i] = licensesArray[i].trim();
        }
        return Lists.newArrayList(licensesArray);
    }

}
