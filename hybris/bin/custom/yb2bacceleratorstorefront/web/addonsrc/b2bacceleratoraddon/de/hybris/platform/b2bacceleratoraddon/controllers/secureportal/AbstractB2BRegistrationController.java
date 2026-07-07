/*
 * Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.b2bacceleratoraddon.controllers.secureportal;

import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractLoginPageController;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commerceservices.config.SiteConfigService;
import de.hybris.platform.converters.Converters;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.b2bacceleratoraddon.forms.RegistrationForm;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.services.BaseStoreService;

import jakarta.annotation.Resource;

import java.util.List;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;


/**
 * This class provides an implementation skeleton for a registration page controller for the B2B accelerator
 */
public abstract class AbstractB2BRegistrationController extends AbstractLoginPageController
{
	@Resource(name = "baseSiteService")
	private BaseSiteService baseSiteService;

	@Resource(name = "commerceSiteConfigService")
	private SiteConfigService siteConfigService;

	@Resource(name = "commonI18NService")
	private CommonI18NService commonI18NService;

	@Resource(name = "baseStoreService")
	private BaseStoreService baseStoreService;

	@Resource(name = "countryConverter")
	private Converter<CountryModel, CountryData> countryConverter;

	/**
	 * Method that ensures the model is populated with all required CMS components in order to properly render the
	 * registration page.
	 */
	protected abstract void populateModelCmsContent(Model model, ContentPageModel contentPageModel);

	/**
	 * Return the name of the view to be used for the registration page
	 */
	protected abstract String getRegistrationView();


	/**
	 * Return the name of the cms page to be used for the registration page
	 */
	protected abstract String getRegistrationCmsPage();

	/**
	 * Calls the implementation of both other methods declared above and adds the required registration form to the
	 * model.
	 */
	protected String getDefaultRegistrationPage(final Model model, final ContentPageModel contentPageModel)
	{
		final RegistrationForm registrationForm = new RegistrationForm();
		populateModelCmsContent(model, contentPageModel);
		String currentBaseSite = null;
		if (baseSiteService.getCurrentBaseSite() != null)
		{
			currentBaseSite = baseSiteService.getCurrentBaseSite().getUid();
		}
		final boolean otpForRegistrationEnabled = Boolean.parseBoolean(siteConfigService.getProperty(currentBaseSite,
				WebConstants.OTP_CUSTOMER_REGISTRATION_ENABLED));
		final int secondsForRegistration = getSiteConfigService().getInt("otp.customer.registration.token.ttlseconds", 300);
		model.addAttribute("secondsForRegistration",secondsForRegistration);
		registrationForm.setOtpForRegistrationEnabled(otpForRegistrationEnabled);
		model.addAttribute(registrationForm);
		return getRegistrationView();
	}

	/**
	 * Spring MVC Model attribute that holds the list of countries used to populate the "Country" dropdown.
	 */
	@ModelAttribute("countries")
	public List<CountryData> getCountries()
	{
		return Converters.convertAll(commonI18NService.getAllCountries(), countryConverter);
	}

}
