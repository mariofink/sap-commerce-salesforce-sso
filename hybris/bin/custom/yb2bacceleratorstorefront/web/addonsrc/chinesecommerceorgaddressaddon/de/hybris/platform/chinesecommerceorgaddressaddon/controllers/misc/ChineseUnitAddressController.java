/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.chinesecommerceorgaddressaddon.controllers.misc;

import de.hybris.platform.addressfacades.address.AddressFacade;
import de.hybris.platform.addressfacades.data.CityData;
import de.hybris.platform.addressfacades.data.DistrictData;

import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Handle address request for regions and cities
 */
@Controller
@Scope("tenant")
@RequestMapping("/my-company/organization-management/manage-units")
public class ChineseUnitAddressController
{
	@Resource(name = "chineseAddressFacade")
	private AddressFacade chineseAddressFacade;

	@GetMapping(value = "/region/{code}", produces = "application/json")
	public @ResponseBody List<CityData> getRegionAddressForm(@PathVariable("code") final String region)
	{
		return chineseAddressFacade.getCitiesForRegion(region);
	}

	@GetMapping(value = "/city/{code}", produces = "application/json")
	public @ResponseBody List<DistrictData> getCityAddressForm(@PathVariable("code") final String city)
	{
		return chineseAddressFacade.getDistrictsForCity(city);
	}
}
