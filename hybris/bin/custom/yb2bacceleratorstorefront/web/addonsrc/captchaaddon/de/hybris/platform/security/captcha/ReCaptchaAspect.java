/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.security.captcha;

import de.hybris.platform.acceleratorservices.config.SiteConfigService;
import de.hybris.platform.store.services.BaseStoreService;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An aspect which uses Google ReCaptcha api to validate captcha answer on the storefront Registration form.
 * @deprecated since 2211. Google ReCaptcha is not supported anymore.
 */

public class ReCaptchaAspect
{
	private static final Logger LOG = LoggerFactory.getLogger(ReCaptchaAspect.class);

	private static final String RECAPTCHA_SITE_KEY_PROPERTY = "recaptcha.publickey";
	private static final String RECAPTCHA_SECRET_KEY_PROPERTY = "recaptcha.privatekey";
	private static final String RECAPTCHA_RESPONSE_PARAM = "g-recaptcha-response";
	private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

	private SiteConfigService siteConfigService;
	private BaseStoreService baseStoreService;

	public Object prepare(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		throw new UnsupportedOperationException("Google ReCaptcha is not supported.");
	}

	public Object advise(final ProceedingJoinPoint joinPoint) throws Throwable
	{
		throw new UnsupportedOperationException("Google ReCaptcha is not supported.");
	}

	protected static HttpServletRequest getRequestFromArgs(final List<Object> args)
	{
		throw new UnsupportedOperationException("Google ReCaptcha is not supported.");
	}

	protected void setCaptchaAttributesInRequest(final boolean captchaEnabledForCurrentStore, final List<Object> args,
			final HttpServletRequest request)
	{
		throw new UnsupportedOperationException("Google ReCaptcha is not supported.");
	}

	protected boolean checkAnswer(final String recaptchaResponse)
	{
		return false;
	}

	protected boolean isCaptchaEnabledForCurrentStore()
	{
		return false;
	}

	protected SiteConfigService getSiteConfigService()
	{
		return siteConfigService;
	}

	public void setSiteConfigService(final SiteConfigService siteConfigService)
	{
		this.siteConfigService = siteConfigService;
	}

	protected BaseStoreService getBaseStoreService()
	{
		return baseStoreService;
	}

	public void setBaseStoreService(final BaseStoreService baseStoreService)
	{
		this.baseStoreService = baseStoreService;
	}
}
