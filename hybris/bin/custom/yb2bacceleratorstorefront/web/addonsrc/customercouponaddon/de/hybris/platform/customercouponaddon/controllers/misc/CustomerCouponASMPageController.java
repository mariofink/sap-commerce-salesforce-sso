/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.customercouponaddon.controllers.misc;

import de.hybris.platform.commercefacades.voucher.exceptions.VoucherOperationException;
import de.hybris.platform.customercouponaddon.controllers.CustomercouponaddonControllerConstants;
import de.hybris.platform.customercouponfacades.CustomerCouponFacade;
import de.hybris.platform.customercouponfacades.customercoupon.data.CustomerCouponData;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


/**
 * Controller for customer coupons in ASM
 */
@Controller
@Scope("tenant")
@RequestMapping("/asm")
public class CustomerCouponASMPageController
{

	private static final String COUPON_CODE_PATH_VARIABLE = "{code:.*}";
	private static final String COUPON_TYPE_AVAILABLE = "available";
	private static final String COUPON_ACTION_SEND = "send";
	private static final String COUPON_ACTION_REMOVE = "remove";


	@Resource(name = "customerCouponFacade")
	private CustomerCouponFacade customerCouponFacade;

	@GetMapping("/customer-coupons")
	public String loadCoupons(@RequestParam final String type, @RequestParam final String text, final Model model)
	{
		List<CustomerCouponData> customerCoupons = null;
		if (COUPON_TYPE_AVAILABLE.equalsIgnoreCase(type))
		{
			customerCoupons = getCustomerCouponFacade().getAssignableCustomerCoupons(text);
			model.addAttribute("action", COUPON_ACTION_SEND);
		}
		else
		{
			customerCoupons = getCustomerCouponFacade().getAssignedCustomerCoupons(text);
			model.addAttribute("action", COUPON_ACTION_REMOVE);
		}
		model.addAttribute("type", type);
		model.addAttribute("customerCoupons", customerCoupons);

		return CustomercouponaddonControllerConstants.Views.Fragments.Coupons.CustomerCouponSubPage;
	}

	@ResponseBody
	@PostMapping("/customer-coupon/" + COUPON_CODE_PATH_VARIABLE)
	public boolean handleCouponActions(@PathVariable("code") final String couponCode, @RequestParam final String action,
			final Model model)
	{
		boolean flag = true;
		try
		{
			if (COUPON_ACTION_SEND.equalsIgnoreCase(action))
			{
				getCustomerCouponFacade().grantCouponAccessForCurrentUser(couponCode);
			}
			else
			{
				getCustomerCouponFacade().releaseCoupon(couponCode);
			}
		}
		catch (final VoucherOperationException e)
		{
			flag = false;
		}

		return flag;
	}


	protected CustomerCouponFacade getCustomerCouponFacade()
	{
		return customerCouponFacade;
	}

}
