/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.b2bpunchoutaddon.controllers.pages;

import de.hybris.platform.b2b.punchout.PunchOutException;
import de.hybris.platform.b2b.punchout.PunchOutResponseCode;
import de.hybris.platform.b2b.punchout.PunchOutUtils;
import de.hybris.platform.b2b.punchout.aop.annotation.PunchOutAuthentication;
import de.hybris.platform.b2b.punchout.security.PunchOutUserAuthenticationStrategy;
import de.hybris.platform.b2b.punchout.services.CXMLBuilder;
import de.hybris.platform.b2b.punchout.services.PunchOutService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.cxml.CXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


/**
 * Purchase order controller default implementation.
 * @deprecated Please use #de.hybris.platform.b2bpunchoutocc.controllers instead
 */
@Deprecated (since="2205", forRemoval=true)
@Component
public class DefaultPurchaseOrderController implements PunchOutController
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultPurchaseOrderController.class);

	@Resource(name = "punchOutService")
	private PunchOutService punchOutService;

	@Resource(name = "punchOutUserAuthenticationStrategy")
	private PunchOutUserAuthenticationStrategy punchOutUserAuthenticationStrategy;

	/**
	 * Handles a Order Request from the Punch Out Provider.
	 *
	 * @param requestBody
	 *           The cXML containing the order to be processed.
	 * @param request
	 *           The servlet request.
	 * @param response
	 *           The servlet response.
	 * @return A cXML with the Order Response, containing the status of the processing of the order.
	 */
	@PostMapping(value = "/punchout/cxml/order")
	@ResponseBody
	@PunchOutAuthentication
	public CXML handlePunchOutPurchaseOrderRequest(@RequestBody final CXML requestBody, final HttpServletRequest request,
			final HttpServletResponse response)
	{
		CXML cxml = null;
		final String identity = punchOutService.retrieveIdentity(requestBody);
		if (LOG.isDebugEnabled()) {
			LOG.debug("CXML received:{}",PunchOutUtils.marshallFromBeanTree(requestBody));
		}
		LOG.info("Order Identity: {}", identity);

		// if user exists
		if (identity != null)
		{
			punchOutUserAuthenticationStrategy.authenticate(identity, request, response);

			cxml = punchOutService.processPurchaseOrderRequest(requestBody);
		}
		else
		{
			final String message = "Unable to find user " + identity;
			LOG.error(message);
			cxml = CXMLBuilder.newInstance().withResponseCode(PunchOutResponseCode.ERROR_CODE_AUTH_FAILED)
					.withResponseMessage(message).create();
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("CXML output:{}" , PunchOutUtils.marshallFromBeanTree(cxml));
		}
		return cxml;
	}
}
