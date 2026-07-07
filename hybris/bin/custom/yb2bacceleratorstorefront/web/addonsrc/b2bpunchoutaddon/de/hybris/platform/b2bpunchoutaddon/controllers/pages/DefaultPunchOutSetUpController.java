/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.b2bpunchoutaddon.controllers.pages;

import de.hybris.platform.b2b.punchout.PunchOutException;
import de.hybris.platform.b2b.punchout.PunchOutResponseCode;
import de.hybris.platform.b2b.punchout.PunchOutUtils;
import de.hybris.platform.b2b.punchout.aop.annotation.PunchOutAuthentication;
import de.hybris.platform.b2b.punchout.services.CXMLBuilder;
import de.hybris.platform.b2b.punchout.services.PunchOutService;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cxml.CXML;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Controller that handles the Punch Out Setup request.
 * @deprecated Please use #de.hybris.platform.b2bpunchoutocc.controllers instead
 */
@Deprecated (since="2205", forRemoval=true)
@Component
public class DefaultPunchOutSetUpController implements PunchOutController
{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultPunchOutSetUpController.class);

	@Resource(name = "punchOutService")
	private PunchOutService punchOutService;

	/**
	 * Receives a request from the punchout provider and sends it the information to access the hybris site.
	 *
	 * @param requestBody
	 *           The cXML file with the punchout user requisition.
	 * @return A cXML file with the access information.
	 */
	@PostMapping(value = "/punchout/cxml/setup")
	@ResponseBody
	@PunchOutAuthentication
	public CXML handlePunchOutSetUpRequest(@RequestBody final CXML requestBody)
	{
		if (LOG.isDebugEnabled()) {
			LOG.debug("cXML received: {}" , PunchOutUtils.marshallFromBeanTree(requestBody));
		}
		final CXML punchoutSetUpResponse = punchOutService.processPunchOutSetUpRequest(requestBody);
		if (LOG.isDebugEnabled()) {
			LOG.debug("CXML output: {}" , PunchOutUtils.marshallFromBeanTree(punchoutSetUpResponse));
		}
		return punchoutSetUpResponse;
	}

}
