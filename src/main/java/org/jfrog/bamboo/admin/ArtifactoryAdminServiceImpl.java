package org.jfrog.bamboo.admin;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.bamboo.security.EncryptionException;
import com.atlassian.bamboo.security.EncryptionService;
import com.atlassian.bamboo.variable.CustomVariableContext;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.jfrog.bamboo.util.BambooBuildInfoLog;
import org.jfrog.bamboo.util.TaskUtils;
import org.jfrog.build.extractor.clientConfiguration.client.ArtifactoryBuildInfoClient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by DimaN on 08/06/2016.
 */
public class ArtifactoryAdminServiceImpl implements ArtifactoryAdminService {

	private Logger log = Logger.getLogger(ArtifactoryAdminServiceImpl.class);

	// The EncryptionService and ActiveObjects should be initialized by Spring
	private final EncryptionService encryptionService;
	private final ActiveObjects activeObjects;
	private CustomVariableContext customVariableContext;


	public ArtifactoryAdminServiceImpl(ActiveObjects activeObjects, EncryptionService encryptionService) {
		this.activeObjects = activeObjects;
		this.encryptionService = encryptionService;
	}

	public ArtifactoryServer addArtifactoryServer(final String serverUrl, final String username,
	                                              final String password, final int timeout) {
		log.debug("Adding new SharedApiKey with name " + serverUrl);
		return activeObjects.executeInTransaction(new TransactionCallback<ArtifactoryServer>() {
			public ArtifactoryServer doInTransaction() {
				ArtifactoryServer artifactoryServer = activeObjects.create(ArtifactoryServer.class);
				substituteServerData(artifactoryServer, serverUrl, username, password, timeout, true);
				artifactoryServer.save();
				return artifactoryServer;
			}
		});
	}


	public void deleteArtifactoryServer(final int id) {
		log.debug("Deleting server " + id + "-" + getArtifactoryServer(id).getServerUrl());
		activeObjects.executeInTransaction(new TransactionCallback<Void>() {
			public Void doInTransaction() {
				activeObjects.delete(getArtifactoryServer(id));
				return null;
			}
		});
	}

	public ArtifactoryServer getArtifactoryServer(int id) {
		return activeObjects.get(ArtifactoryServer.class, id);
	}

	public List<ArtifactoryServer> getAllArtifactoryServers() {
		return newArrayList(activeObjects.find(ArtifactoryServer.class));
	}

	public ArtifactoryServer updateArtifactoryServer(final int id, final String serverUrl,
	                                                 final String username, final String password, final int timeout) {
		log.debug("Updating Artifactory configuration. "+ id +
				"- (previously was) " + getArtifactoryServer(id).getServerUrl());
		return activeObjects.executeInTransaction(new TransactionCallback<ArtifactoryServer>() {
			public ArtifactoryServer doInTransaction() {
				// Case password was changed it need to be encrypted
				boolean isNeedEncryption = !password.equals(getArtifactoryServer(id).getPassword());
				ArtifactoryServer artifactoryServer = getArtifactoryServer(id);
				substituteServerData(artifactoryServer, serverUrl, username, password, timeout, isNeedEncryption);
				artifactoryServer.save();
				return artifactoryServer;
			}
		});
	}

	//decryptExistingBintrayConfig will use this method now
	public BintrayConfiguration getBintrayConfig(boolean decryptConfig) throws IOException {
		HashMap<String, String> confMap = getDecryptedBintrayConfigMap(decryptConfig);
		return new BintrayConfiguration(confMap.get("bintrayUsername"), confMap.get("bintrayPassword"),
				confMap.get("sonatypeUsername"), confMap.get("sonatypePassword"));
	}

	public BintrayConfigJson updateBintrayConfig(String bintrayUsername, String bintrayPassword,
	                                             String sonatypeUsername, String sonatypePassword) throws IOException {
		log.debug("Updating Bintray configuration.");
		String oldConfJson = getBintrayConfigJson().getBintrayConfigJson();
		final Map<String, String> confMap =
				createEncriptedConfMap(bintrayUsername, bintrayPassword,
				sonatypeUsername, sonatypePassword, oldConfJson);
		return activeObjects.executeInTransaction(new TransactionCallback<BintrayConfigJson>() {
			public BintrayConfigJson doInTransaction() {
				String confJson = "";
				try {
					confJson = new ObjectMapper().writeValueAsString(confMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				BintrayConfigJson bintrayConf = getBintrayConfigJson();
				bintrayConf.setBintrayConfigJson(confJson);
				bintrayConf.save();
				return bintrayConf;
			}
		});
	}

	private HashMap<String, String> getDecryptedBintrayConfigMap(boolean decryptConfig)
			throws EncryptionException, IOException {
		BintrayConfigJson bintrayConfigJson = getBintrayConfigJson();
		HashMap<String, String> map =
				new ObjectMapper().readValue(bintrayConfigJson.getBintrayConfigJson(), HashMap.class);
		if (decryptConfig) {
			map.put("bintrayPassword", TaskUtils.decryptIfNeeded(map.get("bintrayPassword"))) ;
			map.put("sonatypePassword", TaskUtils.decryptIfNeeded(map.get("sonatypePassword")));
		}
		return map;
	}

	public List<String> getDeployableRepos(int serverId) {
		return getDeployableRepos(serverId, null, null);
	}

	public List<String> getResolvingRepos(int serverId, HttpServletRequest req, HttpServletResponse resp) {
		ArtifactoryServer artifactoryServer = getArtifactoryServer(serverId);
		if (artifactoryServer == null) {
			log.error("Error while retrieving resolving repository list: Could not find Artifactory server " +
					"configuration by the ID " + serverId);
			return Lists.newArrayList();
		}
		ArtifactoryBuildInfoClient client;

		String serverUrl = substituteVariables(artifactoryServer.getServerUrl());
		String username;
		String password;
		if (StringUtils.isNotBlank(req.getParameter("user")) && StringUtils.isNotBlank(req.getParameter("password"))) {
			username = substituteVariables(req.getParameter("user"));
			password = substituteVariables(TaskUtils.decryptIfNeeded(req.getParameter("password")));
		} else {
			username = substituteVariables(artifactoryServer.getUsername());
			password = substituteVariables(artifactoryServer.getPassword());
		}

		if (StringUtils.isBlank(username)) {
			client = new ArtifactoryBuildInfoClient(serverUrl, new BambooBuildInfoLog(log));
		} else {
			client = new ArtifactoryBuildInfoClient(serverUrl, username, password,
					new BambooBuildInfoLog(log));
		}

		client.setConnectionTimeout(artifactoryServer.getTimeout());

		try {
			return client.getVirtualRepositoryKeys();
		} catch (IOException ioe) {
			log.error("Error while retrieving resolving repository list from: " + serverUrl, ioe);
			try {
				if (ioe.getMessage().contains("401"))
					resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				if (ioe.getMessage().contains("404"))
					resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IOException e) {
				log.error("Error while sending error to response", e);
			}
			return Lists.newArrayList();
		}
	}

	private BintrayConfigJson getBintrayConfigJson() {
		BintrayConfigJson bintrayConfigJson = activeObjects.get(BintrayConfigJson.class, 1);
		if (bintrayConfigJson == null) {
			initBintrayConig();
			return activeObjects.get(BintrayConfigJson.class, 1);
		}
		return bintrayConfigJson;
	}

	private BintrayConfigJson initBintrayConig() {
		log.debug("Initialyzing Bintray config.");
		return activeObjects.executeInTransaction(new TransactionCallback<BintrayConfigJson>() {
			public BintrayConfigJson doInTransaction() {
				BintrayConfigJson bintrayConfigJson = activeObjects.create(BintrayConfigJson.class);
				bintrayConfigJson.setBintrayConfigJson("{}");
				bintrayConfigJson.save();
				return bintrayConfigJson;
			}
		});
	}

	public List<String> getDeployableRepos(int serverId, HttpServletRequest req, HttpServletResponse resp) {
		ArtifactoryServer artifactoryServer = getArtifactoryServer(serverId);
		if (artifactoryServer == null) {
			log.error("Error while retrieving target repository list: Could not find Artifactory server " +
					"configuration by the ID " + serverId);
			return Lists.newArrayList();
		}
		ArtifactoryBuildInfoClient client;

		String serverUrl = substituteVariables(artifactoryServer.getServerUrl());
		String username = null;
		String password = null;
		if (req != null) {
			username = req.getParameter("user");
			password = req.getParameter("password");
		}
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			password = TaskUtils.decryptIfNeeded(password);
		} else {
			username = artifactoryServer.getUsername();
			password = artifactoryServer.getPassword();
		}
		username = substituteVariables(username);
		password = substituteVariables(password);

		if (StringUtils.isBlank(username)) {
			client = new ArtifactoryBuildInfoClient(serverUrl, new BambooBuildInfoLog(log));
		} else {
			client = new ArtifactoryBuildInfoClient(serverUrl, username, password,
					new BambooBuildInfoLog(log));
		}

		client.setConnectionTimeout(artifactoryServer.getTimeout());
		try {
			return client.getLocalRepositoriesKeys();

		} catch (IOException ioe) {
			log.error("Error while retrieving target repository list from: " + serverUrl, ioe);
			try {
				if (resp != null && ioe.getMessage().contains("401"))
					resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				if (resp != null && ioe.getMessage().contains("404"))
					resp.sendError(HttpServletResponse.SC_NOT_FOUND);
			} catch (IOException e) {
				log.error("Error while sending error to response", e);
			}
			return Lists.newArrayList();
		}
	}


	/**
	 * Sets the given values into the provided ArtifactoryServer Object.
	 * If isNeedEncription is true the password will be encrypted, the password will remain as given
	 * if isNeedEncription is false.
	 *
	 * @param artifactoryServer the ArtifactoryServer Object to set the values to
	 * @param serverUrl the server url to set
	 * @param username the username to set
	 * @param password the password to set, will be encrypted if isNeedEncription is true or set same as given otherwise
	 * @param timout the timeout to set
	 * @param isNeedEncription determinants if the password will be encrypted or not
	 */
	private void substituteServerData(ArtifactoryServer artifactoryServer, String serverUrl, String username,
	                                  String password, int timout, boolean isNeedEncription) {
		artifactoryServer.setServerUrl(serverUrl);
		artifactoryServer.setUsername(username);
		artifactoryServer.setTimeout(timout);
		if(isNeedEncription) {
			artifactoryServer.setPassword(encryptionService.encrypt(password));
		} else {
			artifactoryServer.setPassword(password);
		}
	}

	private Map<String, String> createEncriptedConfMap(String bintrayUsername, String bintrayPassword,
	                                                  String sonatypeUsername, String sonatypePassword,
	                                                  String oldConfJson) throws IOException {
		HashMap<String,String> newMap =
				convertToMap(bintrayUsername, bintrayPassword, sonatypeUsername, sonatypePassword);
		HashMap<String, String> oldMap = new ObjectMapper().readValue(oldConfJson, HashMap.class);
		for (String key : newMap.keySet()) {
			// If the Key contains password and the new value not blank and (the old map was null or the key was changed)
			// then the value is new/changed and need to be encrypted
			if (key.matches(".*[Pp]assword.*") &&
					newMap.get(key) != null &&
					!"".equals(newMap.get(key)) &&
					(oldMap == null || !newMap.get(key).equals(oldMap.get(key)))) {
				newMap.put(key, encriptPassword(newMap.get(key)));
			}
		}
		return newMap;
	}

	private static HashMap<String, String> convertToMap(String bintrayUsername, String bintrayPassword,
	                                                    String sonatypeUsername, String sonatypePassword) {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("bintrayUsername", bintrayUsername);
		map.put("bintrayPassword", bintrayPassword);
		map.put("sonatypeUsername", sonatypeUsername);
		map.put("sonatypePassword", sonatypePassword);
		return map;
	}

	public String encriptPassword (String password) {
		return encryptionService.encrypt(password);
	}

	public CustomVariableContext setCustomVariableContext() {
		return null;
	}

	/**
	 * Substitute (replace) Bamboo variable names with their defined values
	 */
	public String substituteVariables(String s) {
		return s != null ? customVariableContext.substituteString(s) : null;
	}

	public void setCustomVariableContext(CustomVariableContext customVariableContext) {
		this.customVariableContext = customVariableContext;
	}
}
