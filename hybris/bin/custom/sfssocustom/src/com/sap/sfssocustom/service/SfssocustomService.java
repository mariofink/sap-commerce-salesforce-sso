/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.sfssocustom.service;

public interface SfssocustomService
{
	String getHybrisLogoUrl(String logoCode);

	void createLogo(String logoCode);
}
