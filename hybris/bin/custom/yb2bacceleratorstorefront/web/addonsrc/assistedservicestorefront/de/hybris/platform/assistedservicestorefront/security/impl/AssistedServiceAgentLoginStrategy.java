/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package de.hybris.platform.assistedservicestorefront.security.impl;

import de.hybris.platform.acceleratorstorefrontcommons.security.GUIDCookieStrategy;
import de.hybris.platform.servicelayer.security.spring.HybrisSessionFixationProtectionStrategy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;


/**
 * Implementation of login strategy for assisted service agent.
 */
public class AssistedServiceAgentLoginStrategy
{
	private GUIDCookieStrategy guidCookieStrategy;
	private UserDetailsService userDetailsService;
	private HybrisSessionFixationProtectionStrategy sessionFixationStrategy;

	public void login(final String username, final HttpServletRequest request, final HttpServletResponse response)
	{
		final UserDetails userDetails = getUserDetailsService().loadUserByUsername(username);
		final AssistedServiceAuthenticationToken token = new AssistedServiceAuthenticationToken(
				new AssistedServiceAgentPrincipal(username), userDetails.getAuthorities());
		token.setDetails(new WebAuthenticationDetails(request));
		SecurityContextHolder.getContext().setAuthentication(token);
		getGuidCookieStrategy().setCookie(request, response);
		sessionFixationStrategy.onAuthentication(token, request, response);
	}

	protected GUIDCookieStrategy getGuidCookieStrategy()
	{
		return guidCookieStrategy;
	}

	public void setGuidCookieStrategy(final GUIDCookieStrategy guidCookieStrategy)
	{
		this.guidCookieStrategy = guidCookieStrategy;
	}

	protected UserDetailsService getUserDetailsService()
	{
		return userDetailsService;
	}

	public void setUserDetailsService(final UserDetailsService userDetailsService)
	{
		this.userDetailsService = userDetailsService;
	}

	protected HybrisSessionFixationProtectionStrategy getSessionFixationStrategy()
	{
		return sessionFixationStrategy;
	}

	public void setSessionFixationStrategy(final HybrisSessionFixationProtectionStrategy sessionFixationStrategy)
	{
		this.sessionFixationStrategy = sessionFixationStrategy;
	}
}
