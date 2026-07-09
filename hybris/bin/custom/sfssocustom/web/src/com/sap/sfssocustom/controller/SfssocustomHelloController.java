/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.sfssocustom.controller;

import static com.sap.sfssocustom.constants.SfssocustomConstants.PLATFORM_LOGO_CODE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import com.sap.sfssocustom.service.SfssocustomService;


@Controller
public class SfssocustomHelloController
{
	@Autowired
	private SfssocustomService sfssocustomService;

	@GetMapping("/")
	public String printWelcome(final ModelMap model)
	{
		model.addAttribute("logoUrl", sfssocustomService.getHybrisLogoUrl(PLATFORM_LOGO_CODE));
		return "welcome";
	}
}
