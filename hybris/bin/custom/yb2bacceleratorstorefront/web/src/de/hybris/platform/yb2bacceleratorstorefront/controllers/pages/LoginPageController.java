/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.yb2bacceleratorstorefront.controllers.pages;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractLoginPageController;
import de.hybris.platform.acceleratorstorefrontcommons.forms.RegisterForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.AbstractPageModel;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.yb2bacceleratorstorefront.controllers.ControllerConstants;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * Login Controller. Handles login and register for the account flow.
 */
@Controller
@RequestMapping(value = "/login")
public class LoginPageController extends AbstractLoginPageController
{
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("[\r\n]");

	private static final String REDIRECT_PREFIX = "redirect:";
	private static final String SSO_LOGIN_URL_PROPERTY = "storefront.sso.login.url";
	private static final String DEFAULT_SSO_LOGIN_URL = "/samlsinglesignon/saml2/authenticate/registrationId";

	private HttpSessionRequestCache httpSessionRequestCache;

	@Override
	protected String getView()
	{
		return ControllerConstants.Views.Pages.Account.AccountLoginPage;
	}

	@Override
	protected String getSuccessRedirect(final HttpServletRequest request, final HttpServletResponse response)
	{
		if (httpSessionRequestCache.getRequest(request, response) != null)
		{
			return httpSessionRequestCache.getRequest(request, response).getRedirectUrl();
		}
		return "/";
	}

	@Override
	protected AbstractPageModel getCmsPage() throws CMSItemNotFoundException
	{
		return getContentPageForLabelOrId("login");
	}


	@Resource(name = "httpSessionRequestCache")
	public void setHttpSessionRequestCache(final HttpSessionRequestCache accHttpSessionRequestCache)
	{
		this.httpSessionRequestCache = accHttpSessionRequestCache;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String doLogin(@RequestHeader(value = "referer", required = false) final String referer,
			@RequestParam(value = "error", defaultValue = "false") final boolean loginError, final Model model,
			final HttpServletRequest request, final HttpServletResponse response, final HttpSession session)
			throws CMSItemNotFoundException
	{
		// SSO-only: a user returning from Salesforce is already authenticated by the SamlSsoLoginFilter, so
		// send them straight on. Anonymous users are bounced to the SAML SP-initiated login (no native form).
		if (!getUserFacade().isAnonymousUser())
		{
			return REDIRECT_PREFIX + getSuccessRedirect(request, response);
		}

		if (!loginError)
		{
			storeReferer(referer, request, response);
		}
		return REDIRECT_PREFIX + buildAbsoluteSsoLoginUrl(request);
	}

	/**
	 * Builds an absolute, server-root URL for the SAML SP-initiated login endpoint. The endpoint lives in a
	 * different webapp ({@code /samlsinglesignon}), so a context-relative {@code redirect:/...} cannot be used
	 * (Spring would prepend the storefront context path, causing a redirect loop back to {@code /login}).
	 */
	protected String buildAbsoluteSsoLoginUrl(final HttpServletRequest request)
	{
		final String ssoPath = getSsoLoginUrl();
		if (ssoPath.startsWith("http://") || ssoPath.startsWith("https://"))
		{
			return ssoPath;
		}
		return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + ssoPath;
	}

	protected String getSsoLoginUrl()
	{
		return getSiteConfigService().getProperty(SSO_LOGIN_URL_PROPERTY) != null
				? getSiteConfigService().getProperty(SSO_LOGIN_URL_PROPERTY)
				: DEFAULT_SSO_LOGIN_URL;
	}

	protected void storeReferer(final String referer, final HttpServletRequest request, final HttpServletResponse response)
	{
		final String sanitizedReferer = referer == null ? null : NEWLINE_PATTERN.matcher(referer).replaceAll("");
		if (StringUtils.isNotEmpty(sanitizedReferer) && !sanitizedReferer.endsWith("/login")
				&& sanitizedReferer.contains(request.getServerName()))
		{
			httpSessionRequestCache.saveRequest(request, response);
		}
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public String doRegister(@RequestHeader(value = "referer", required = false) final String referer, final RegisterForm form,
			final BindingResult bindingResult, final Model model, final HttpServletRequest request,
			final HttpServletResponse response, final RedirectAttributes redirectModel) throws CMSItemNotFoundException
	{
		getRegistrationValidator().validate(form, bindingResult);
		return processRegisterUserRequest(referer, form, bindingResult, model, request, response, redirectModel);
	}

	@RequestMapping(value = "/register/termsandconditions", method = RequestMethod.GET)
	public String getTermsAndConditions(final Model model) throws CMSItemNotFoundException
	{
		final ContentPageModel pageForRequest = getCmsPageService().getPageForLabel("/termsAndConditions");
		storeCmsPageInModel(model, pageForRequest);
		setUpMetaDataForContentPage(model, pageForRequest);
		return ControllerConstants.Views.Fragments.Checkout.TermsAndConditionsPopup;
	}
}
