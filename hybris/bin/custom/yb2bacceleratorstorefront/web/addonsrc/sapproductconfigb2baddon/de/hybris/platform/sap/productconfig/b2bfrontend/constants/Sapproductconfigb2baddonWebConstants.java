/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.sap.productconfig.b2bfrontend.constants;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.AbstractController;


/**
 * Global class for all Sapproductconfigb2baddon web constants.
 */
public final class Sapproductconfigb2baddonWebConstants
{
	/**
	 * view name to trigger a re-direct to the chekcout page
	 */
	public static final String REDIRECT_TO_CHECKOUT = AbstractController.REDIRECT_PREFIX + "/checkout/multi/summary/view";
	/**
	 * view name to trigger a re-direct to the cart page
	 */
	public static final String REDIRECT_TO_CART = AbstractController.REDIRECT_PREFIX + "/cart";

	private Sapproductconfigb2baddonWebConstants()
	{
		//empty to avoid instantiating this constant class
	}
}
