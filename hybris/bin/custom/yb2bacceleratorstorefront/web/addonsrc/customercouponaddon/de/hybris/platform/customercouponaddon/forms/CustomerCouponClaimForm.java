/*
 * Copyright (c) 2024 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.customercouponaddon.forms;

import de.hybris.platform.acceleratorstorefrontcommons.util.XSSFilterUtil;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.io.Serializable;


public class CustomerCouponClaimForm implements Serializable
{

	@Serial
	private static final long serialVersionUID = 1L;
	@NotNull(message = "{text.coupon.claim.invalid.error}")
	@Size(min = 1, max = 255, message = "{text.coupon.claim.invalid.error}")
	private String couponCode;

	public String getCouponCode()
	{
		return couponCode;
	}

	public void setCouponCode(final String couponCode)
	{
		this.couponCode = XSSFilterUtil.filter(couponCode);
	}
}
