package org.jfrog.bamboo.admin;

import com.atlassian.bamboo.core.BambooObject;

/**
 * Created by DimaN on 19/04/2016.
 */
public interface BintrayConfigDefinition extends Cloneable, BambooObject {

	@Override
	boolean equals(Object o);

	int compareTo(Object obj);

	@Override
	int hashCode();

	long getId();

	void setId(long id);

	String getBintrayUsername();

	void setBintrayUsername(String bintrayUsername);

	String getBintrayApiKey();

	void setBintrayApiKey(String bintrayApiKey);

	String getSonatypeOssUsername();

	void setSonatypeOssUsername(String sonatypeOssUsername);

	String getSonatypeOssPassword();

	void setSonatypeOssPassword(String sonatypeOssPassword);
}
