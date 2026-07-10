/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.yb2bacceleratorstorefront.security;

import de.hybris.platform.acceleratorstorefrontcommons.security.GUIDCookieStrategy;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.jalo.user.CookieBasedLoginToken;
import de.hybris.platform.jalo.user.LoginToken;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.util.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;


/**
 * Consumes the SAML "pass-through" login-token cookie that the {@code samlsinglesignon} extension writes after a
 * successful SAML assertion (see {@code Saml2UserFilter}/{@code DefaultSamlLoginService#storeLoginToken}). The
 * {@code samlsinglesignon} webapp only creates/updates the Hybris user and drops a platform login-token cookie on
 * path {@code /}; it does not establish a storefront session. This filter decodes that cookie, resolves the user
 * and logs them into the storefront session (Spring Security context + storefront login/GUID/remember-me), mirroring
 * the pattern used by the assisted-service {@code AssistedServiceFilter}. The cookie is single-use and is cleared
 * once consumed.
 */
public class SamlSsoLoginFilter extends OncePerRequestFilter
{
	private static final Logger LOG = LoggerFactory.getLogger(SamlSsoLoginFilter.class);

	private static final String SSO_COOKIE_NAME_PROPERTY = "sso.cookie.name";
	private static final String SSO_DEFAULT_COOKIE_NAME = "samlPassThroughToken";

	private UserDetailsService userDetailsService;
	private UserService userService;
	private CustomerFacade customerFacade;
	private GUIDCookieStrategy guidCookieStrategy;
	private RememberMeServices rememberMeServices;

	@Override
	protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
			final FilterChain chain) throws ServletException, IOException
	{
		final String cookieName = getSsoCookieName();
		final Cookie cookie = getCookie(request, cookieName);

		if (cookie != null && StringUtils.isNotBlank(cookie.getValue()))
		{
			LOG.debug("SAML SSO cookie '{}' present on request to '{}' - attempting storefront login", cookieName,
					request.getRequestURI());
			try
			{
				final LoginToken token = new CookieBasedLoginToken(cookie);
				final String uid = token.getUser().getUid();

				if (isAlreadyLoggedIn(uid))
				{
					LOG.debug("User '{}' already authenticated in storefront session - skipping SSO login", uid);
				}
				else
				{
					ssoLogin(uid, request, response);
					LOG.info("Established storefront SSO session for user '{}'", uid);
				}
			}
			catch (final RuntimeException e)
			{
				LOG.warn("Could not consume SAML SSO login token cookie: {}", e.getMessage());
				LOG.debug(e.getMessage(), e);
			}
			finally
			{
				// the token is single-use; remove it so it cannot be replayed
				eraseCookie(response, cookieName);
			}
		}
		else
		{
			LOG.debug("No SAML SSO cookie '{}' on request to '{}'", cookieName, request.getRequestURI());
		}

		chain.doFilter(request, response);
	}

	protected void ssoLogin(final String uid, final HttpServletRequest request, final HttpServletResponse response)
	{
		// Resolve authorities from the platform user details; the principal itself must be the
		// username String (mirrors CoreAuthenticationProvider.createSuccessAuthentication), because the
		// storefront's AcceleratorRememberMeServices reads authentication.getPrincipal().toString() as the uid.
		final UserDetails principal = getUserDetailsService().loadUserByUsername(uid);
		final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				principal.getUsername(), null, principal.getAuthorities());
		authentication.setDetails(new WebAuthenticationDetails(request));

		// Establish the Hybris session user explicitly (the AuthenticationManager is bypassed for SSO), then
		// wire up the Spring Security context and the storefront session (cart, GUID cookie, remember-me).
		getUserService().setCurrentUser(getUserService().getUserForUID(principal.getUsername()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		getCustomerFacade().loginSuccess();
		getGuidCookieStrategy().setCookie(request, response);
		getRememberMeServices().loginSuccess(request, response, authentication);
	}

	protected boolean isAlreadyLoggedIn(final String uid)
	{
		final Authentication current = SecurityContextHolder.getContext().getAuthentication();
		return current != null && current.isAuthenticated() && !(current instanceof AnonymousAuthenticationToken)
				&& StringUtils.equalsIgnoreCase(uid, current.getName());
	}

	protected String getSsoCookieName()
	{
		return Config.getString(SSO_COOKIE_NAME_PROPERTY, SSO_DEFAULT_COOKIE_NAME);
	}

	protected Cookie getCookie(final HttpServletRequest request, final String cookieName)
	{
		if (request.getCookies() == null)
		{
			return null;
		}
		for (final Cookie cookie : request.getCookies())
		{
			if (cookieName.equals(cookie.getName()))
			{
				return cookie;
			}
		}
		return null;
	}

	protected void eraseCookie(final HttpServletResponse response, final String cookieName)
	{
		final Cookie cookie = new Cookie(cookieName, "");
		cookie.setPath("/");
		cookie.setMaxAge(0);
		cookie.setHttpOnly(true);
		cookie.setSecure(true);
		response.addCookie(cookie);
	}

	protected UserDetailsService getUserDetailsService()
	{
		return userDetailsService;
	}

	public void setUserDetailsService(final UserDetailsService userDetailsService)
	{
		this.userDetailsService = userDetailsService;
	}

	protected UserService getUserService()
	{
		return userService;
	}

	public void setUserService(final UserService userService)
	{
		this.userService = userService;
	}

	protected CustomerFacade getCustomerFacade()
	{
		return customerFacade;
	}

	public void setCustomerFacade(final CustomerFacade customerFacade)
	{
		this.customerFacade = customerFacade;
	}

	protected GUIDCookieStrategy getGuidCookieStrategy()
	{
		return guidCookieStrategy;
	}

	public void setGuidCookieStrategy(final GUIDCookieStrategy guidCookieStrategy)
	{
		this.guidCookieStrategy = guidCookieStrategy;
	}

	protected RememberMeServices getRememberMeServices()
	{
		return rememberMeServices;
	}

	public void setRememberMeServices(final RememberMeServices rememberMeServices)
	{
		this.rememberMeServices = rememberMeServices;
	}
}
