package org.jfrog.bamboo.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by DimaN on 08/06/2016.
 */
public interface ArtifactoryAdminService {

	ArtifactoryServer addArtifactoryServer(String url, String username, String password, int timeout);

	void deleteArtifactoryServer(int id);

	ArtifactoryServer getArtifactoryServer(int id);

	List<ArtifactoryServer> getAllArtifactoryServers();

	Map<String, String> getServersMap();

	ArtifactoryServer updateArtifactoryServer(int id, String url, String username, String password, int timeout);

	BintrayConfigJson updateBintrayConfig(String bintrayUsername, String bintrayPassword,
	                                      String sonatypeUsername, String sonatypePassword) throws IOException;

	BintrayConfiguration getBintrayConfig(boolean decryptConfig) throws IOException;

	List<String> getDeployableRepos(int serverId);

	public List<String> getResolvingRepos(int serverId, HttpServletRequest req, HttpServletResponse resp);

	List<String> getDeployableRepos(int serverId, HttpServletRequest req, HttpServletResponse resp);

	String substituteVariables(String s);

	String decryptIfNeeded(String s);
}
