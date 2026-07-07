/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.acceleratorstorefrontcommons.forms;

import de.hybris.platform.acceleratorstorefrontcommons.util.XSSFilterUtil;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public class VoucherForm implements Serializable
{
	@Serial
	private static final long serialVersionUID = 3734178553292263688L;

	@NotNull(message = "{text.voucher.apply.invalid.error}")
	@Size(min = 1, max = 255, message = "{text.voucher.apply.invalid.error}")
	private String voucherCode;

	public String getVoucherCode()
	{
		return voucherCode;
	}

	public void setVoucherCode(final String voucherCode)
	{
		this.voucherCode = XSSFilterUtil.filter(voucherCode);
	}
}
