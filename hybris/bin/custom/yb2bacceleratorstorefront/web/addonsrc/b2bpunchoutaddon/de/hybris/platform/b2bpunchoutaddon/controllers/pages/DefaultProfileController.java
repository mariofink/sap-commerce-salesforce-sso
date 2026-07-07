/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.b2bpunchoutaddon.controllers.pages;

import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.b2b.punchout.PunchOutException;
import de.hybris.platform.b2b.punchout.PunchOutResponseCode;
import de.hybris.platform.b2b.punchout.PunchOutUtils;
import de.hybris.platform.b2b.punchout.aop.annotation.PunchOutAuthentication;
import de.hybris.platform.b2b.punchout.services.CXMLBuilder;
import de.hybris.platform.b2b.punchout.services.PunchOutService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.site.BaseSiteService;

import java.util.HashMap;
import java.util.Map;

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
 * Controller to handle a punchout profile transaction.
 * @deprecated Please use #de.hybris.platform.b2bpunchoutocc.controllers instead
 */
@Deprecated (since="2205", forRemoval=true)
@Component
public class DefaultProfileController implements PunchOutController
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultProfileController.class);

	@Resource(name = "punchOutService")
	private PunchOutService punchOutService;

	@Resource(name = "baseSiteService")
	private BaseSiteService baseSiteService;

	@Resource(name = "siteBaseUrlResolutionService")
	private SiteBaseUrlResolutionService siteBaseUrlResolutionService;

	@Resource(name = "supportedTransactionURLPaths")
	private Map<String, String> supportedTransactionURLPaths;

	@Resource(name = "sessionService")
	private SessionService sessionService;


	/**
	 * Handles a profile request from the punch out provider.
	 *
	 * @param request
	 *           The cXML with the request information.
	 * @return A cXML with the profile response.
	 */
	@PostMapping(value = "/punchout/cxml/profile")
	@ResponseBody
	@PunchOutAuthentication
	public CXML handleProfileRequest(@RequestBody final CXML request)
	{
		if (LOG.isDebugEnabled()) {
			LOG.debug("CXML received:{}",PunchOutUtils.marshallFromBeanTree(request));
		}
		setSupportedURLS();
		CXML result =  getPunchOutService().processProfileRequest(request);
		if (LOG.isDebugEnabled()) {
			LOG.debug("CXML output:{}",PunchOutUtils.marshallFromBeanTree(result));
		}
		return result;
	}

	// Generate profile list for populator
	private void setSupportedURLS()
	{
		final Map<String, String> profilePathsMap = new HashMap<>();
		for (final Map.Entry<String, String> entry : supportedTransactionURLPaths.entrySet())
		{
			profilePathsMap.put(entry.getKey(), formCompleteURL(entry.getValue()));
		}
		sessionService.setAttribute(PunchOutUtils.SUPPORTED_TRANSACTION_URL_PATHS, profilePathsMap);
	}

	/**
	 * Forms a complete URL by using the {@link SiteBaseUrlResolutionService} and the given path.
	 *
	 * @param urlPath the URL path value
	 * @return full secure URL for current baseSite
	 */
	private String formCompleteURL(final String urlPath)
	{	
		return siteBaseUrlResolutionService.getWebsiteUrlForSite(baseSiteService.getCurrentBaseSite(), "", true, urlPath);
	}

	public PunchOutService getPunchOutService()
	{
		return punchOutService;
	}

	public void setPunchOutService(final PunchOutService punchoutService)
	{
		this.punchOutService = punchoutService;
	}

	protected SiteBaseUrlResolutionService getSiteBaseUrlResolutionService()
	{
		return siteBaseUrlResolutionService;
	}

	public void setSiteBaseUrlResolutionService(final SiteBaseUrlResolutionService siteBaseUrlResolutionService)
	{
		this.siteBaseUrlResolutionService = siteBaseUrlResolutionService;
	}

	protected BaseSiteService getBaseSiteService()
	{
		return baseSiteService;
	}

	public void setBaseSiteService(final BaseSiteService baseSiteService)
	{
		this.baseSiteService = baseSiteService;
	}


	protected SessionService getSessionService()
	{
		return sessionService;
	}

	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}

	protected Map<String, String> getSupportedTransactionURLPaths()
	{
		return supportedTransactionURLPaths;
	}

	public void setSupportedTransactionURLPaths(final Map<String, String> supportedTransactionURLPaths)
	{
		this.supportedTransactionURLPaths = supportedTransactionURLPaths;
	}

}
