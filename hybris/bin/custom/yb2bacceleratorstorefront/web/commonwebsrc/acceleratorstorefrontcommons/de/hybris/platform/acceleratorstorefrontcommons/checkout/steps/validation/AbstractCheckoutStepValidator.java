/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.validation;

import de.hybris.platform.acceleratorfacades.flow.CheckoutFlowFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;


public abstract class AbstractCheckoutStepValidator implements CheckoutStepValidator
{
	private CheckoutFacade checkoutFacade;
	private CheckoutFlowFacade checkoutFlowFacade;

	@Override
	public abstract ValidationResults validateOnEnter(final RedirectAttributes redirectAttributes);

	@Override
	public ValidationResults validateOnExit()
	{
		return ValidationResults.SUCCESS;
	}

	public CheckoutFacade getCheckoutFacade()
	{
		return checkoutFacade;
	}

	public void setCheckoutFacade(final CheckoutFacade checkoutFacade)
	{
		this.checkoutFacade = checkoutFacade;
	}

	public CheckoutFlowFacade getCheckoutFlowFacade()
	{
		return checkoutFlowFacade;
	}

	public void setCheckoutFlowFacade(final CheckoutFlowFacade checkoutFlowFacade)
	{
		this.checkoutFlowFacade = checkoutFlowFacade;
	}

}
