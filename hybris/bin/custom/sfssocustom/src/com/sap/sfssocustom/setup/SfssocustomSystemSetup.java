/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.sfssocustom.setup;

import static com.sap.sfssocustom.constants.SfssocustomConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import com.sap.sfssocustom.constants.SfssocustomConstants;
import com.sap.sfssocustom.service.SfssocustomService;


@SystemSetup(extension = SfssocustomConstants.EXTENSIONNAME)
public class SfssocustomSystemSetup
{
	private final SfssocustomService sfssocustomService;

	public SfssocustomSystemSetup(final SfssocustomService sfssocustomService)
	{
		this.sfssocustomService = sfssocustomService;
	}

	@SystemSetup(process = SystemSetup.Process.ALL, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		sfssocustomService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return SfssocustomSystemSetup.class.getResourceAsStream("/sfssocustom/sap-hybris-platform.png");
	}
}
