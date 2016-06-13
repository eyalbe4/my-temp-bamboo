package org.jfrog.bamboo.admin;

import net.java.ao.Entity;
import net.java.ao.Preload;
import net.java.ao.schema.Table;

/**
 * Created by DimaN on 08/06/2016.
 */
@Preload
@Table("BintrayConfigJson")
public interface BintrayConfigJson extends Entity {

	String getBintrayConfigJson();

	void setBintrayConfigJson(String bintrayConfigJson);

}
