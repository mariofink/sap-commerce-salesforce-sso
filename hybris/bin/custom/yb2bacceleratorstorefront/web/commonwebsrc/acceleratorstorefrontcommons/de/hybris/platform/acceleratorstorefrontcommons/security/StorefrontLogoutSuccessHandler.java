/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.acceleratorstorefrontcommons.security;

import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.servicelayer.session.SessionService;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import static de.hybris.platform.commercefacades.constants.CommerceFacadesConstants.USER_CONSENTS;


public class StorefrontLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler
{
	public static final String CLOSE_ACCOUNT_PARAM = "&closeAcc=true";

	private GUIDCookieStrategy guidCookieStrategy;
	private List<String> restrictedPages;
	private SessionService sessionService;

	protected GUIDCookieStrategy getGuidCookieStrategy()
	{
		return guidCookieStrategy;
	}

	public void setGuidCookieStrategy(final GUIDCookieStrategy guidCookieStrategy)
	{
		this.guidCookieStrategy = guidCookieStrategy;
	}

	protected List<String> getRestrictedPages()
	{
		return restrictedPages;
	}

	public void setRestrictedPages(final List<String> restrictedPages)
	{
		this.restrictedPages = restrictedPages;
	}

	@Override
	public void onLogoutSuccess(final HttpServletRequest request, final HttpServletResponse response,
			final Authentication authentication) throws IOException, ServletException
	{
		getGuidCookieStrategy().deleteCookie(request, response);
		getSessionService().removeAttribute(USER_CONSENTS);

		// Delegate to default redirect behaviour
		super.onLogoutSuccess(request, response, authentication);
	}

	@Override
	protected String determineTargetUrl(final HttpServletRequest request, final HttpServletResponse response)
	{
		String targetUrl = super.determineTargetUrl(request, response);

		for (final String restrictedPage : getRestrictedPages())
		{
			// When logging out from a restricted page, return user to homepage.
			if (targetUrl.contains(restrictedPage))
			{
				targetUrl = super.getDefaultTargetUrl();
			}
		}

		// For closing an account, we need to append the closeAcc query string to the target url to display the close account message in the homepage.
		if (StringUtils.isNotBlank(request.getParameter(WebConstants.CLOSE_ACCOUNT)))
		{
			targetUrl = targetUrl + CLOSE_ACCOUNT_PARAM;
		}
		return targetUrl;
	}

	protected SessionService getSessionService()
	{
		return sessionService;
	}

	public void setSessionService(final SessionService sessionService)
	{
		this.sessionService = sessionService;
	}
}
