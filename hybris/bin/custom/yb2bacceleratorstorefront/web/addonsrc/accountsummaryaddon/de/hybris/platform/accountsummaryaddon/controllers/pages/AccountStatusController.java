/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.accountsummaryaddon.controllers.pages;

import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractSearchPageController;
import de.hybris.platform.accountsummaryaddon.breadcrumb.impl.AccountSummaryMyCompanyBreadcrumbBuilder;
import de.hybris.platform.accountsummaryaddon.constants.AccountsummaryaddonConstants;
import de.hybris.platform.b2bacceleratorfacades.document.data.B2BDocumentData;
import de.hybris.platform.b2bacceleratorfacades.company.B2BAccountSummaryFacade;
import de.hybris.platform.b2b.company.B2BCommerceUnitService;
import de.hybris.platform.b2bacceleratorfacades.company.CompanyB2BCommerceFacade;
import de.hybris.platform.b2bacceleratorfacades.query.QueryParameters;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;

import jakarta.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller for organization management.
 */
@Controller
public class AccountStatusController extends AbstractSearchPageController
{
	protected static final Logger LOG = Logger.getLogger(AccountStatusController.class);

	@Resource(name = "accountSummaryMyCompanyBreadcrumbBuilder")
	protected AccountSummaryMyCompanyBreadcrumbBuilder myCompanyBreadcrumbBuilder;

	@Resource(name = "b2bCommerceFacade")
	protected CompanyB2BCommerceFacade companyB2BCommerceFacade;

	@Resource(name = "b2bCommerceUnitService")
	protected B2BCommerceUnitService b2bCommerceUnitService;

	@Resource(name = "b2bCustomerFacade")
	protected CustomerFacade customerFacade;

	@Resource(name = "b2bAccountSummaryFacade")
	protected B2BAccountSummaryFacade b2bAccountSummaryFacade;


	@GetMapping("/my-company/organization-management/accountstatus")
	@RequireHardLogIn
	public String accountStatus(final Model model) throws CMSItemNotFoundException
	{
		storeCmsPageInModel(model, getContentPageForLabelOrId(AccountsummaryaddonConstants.ACCOUNT_STATUS_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(AccountsummaryaddonConstants.ACCOUNT_STATUS_CMS_PAGE));
		model.addAttribute("breadcrumbs", myCompanyBreadcrumbBuilder.createAccountSummaryBreadcrumbs());
		model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
		return getViewForPage(model);
	}

	@GetMapping("/my-company/organization-management/accountstatus/details")
	@RequireHardLogIn
	public String accountStatus(@RequestParam final String unit, final Model model)
			throws CMSItemNotFoundException
	{
		storeCmsPageInModel(model, getContentPageForLabelOrId(AccountsummaryaddonConstants.ACCOUNT_STATUS_DETAIL_CMS_PAGE));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(AccountsummaryaddonConstants.ACCOUNT_STATUS_DETAIL_CMS_PAGE));
		model.addAttribute("breadcrumbs", myCompanyBreadcrumbBuilder.createAccountSummaryDetailsBreadcrumbs(unit));
		model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);

		return getViewForPage(model);
	}

	@GetMapping(value = "/my-company/organization-management/accountstatus/details/documents", produces = "application/json")
	@RequireHardLogIn
	public String search(final Model model, @RequestParam final String unit,
			@RequestParam(value = "page", defaultValue = AccountsummaryaddonConstants.DEFAULT_PAGE) final Integer currentPage,
			@RequestParam(defaultValue = AccountsummaryaddonConstants.DEFAULT_PAGE_SIZE) final Integer pageSize,
			@RequestParam final String searchBy, @RequestParam final String searchValue,
			@RequestParam final String searchRangeMin,
			@RequestParam final String searchRangeMax,
			@RequestParam(defaultValue = AccountsummaryaddonConstants.SEARCH_STATUS_OPEN) final String status,
			@RequestParam(defaultValue = AccountsummaryaddonConstants.DEFAULT_SORT) final String sort)
			throws CMSItemNotFoundException
	{
		final SearchPageData<B2BDocumentData> searchResult = b2bAccountSummaryFacade.findDocuments(QueryParameters
				.with("searchBy", searchBy).and("searchValue", searchValue).and("searchRangeMin", searchRangeMin)
				.and("searchRangeMax", searchRangeMax).and("unit", unit).and("status", status).and("sort", sort)
				.and("pageSize", pageSize.toString()).and("currentPage", currentPage.toString()).buildMap());

		populateModel(model, searchResult, ShowMode.Page);

		model.addAttribute("searchBy", searchBy);
		return AccountsummaryaddonConstants.ACCOUNT_STATUS_DOCUMENTLIST;
	}

}
