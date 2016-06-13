package org.jfrog.bamboo.admin;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

/**
 * Created by DimaN on 08/06/2016.
 */
@Preload
@Table("ArtifactoryServer")
public interface ArtifactoryServer extends Entity {

	String getServerUrl();

	void setServerUrl(String url);

	String getUsername();

	void setUsername(String username);

	String getPassword();

	void setPassword(String password);

	int getTimeout();

	void setTimeout(int timeout);
}
