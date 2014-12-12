package com.intel.crashtoolserver.bean;

import java.util.List;

/**
 * Interface that provides informations to generate a Build Key
 * @author glivon
 */
public interface BuildKey {

	String getBuildUserHostname();

	String getFingerPrint();

	String getBuildId();

	String getKernelVersion();

	List<String> getUniqueKeyComponentsList();

	IngredientsKey getIngredientsKey();
}