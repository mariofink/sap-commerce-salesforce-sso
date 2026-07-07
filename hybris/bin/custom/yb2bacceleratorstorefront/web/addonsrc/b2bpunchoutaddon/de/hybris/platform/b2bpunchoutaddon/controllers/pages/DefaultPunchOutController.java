/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.b2bpunchoutaddon.controllers.pages;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.b2b.punchout.PunchOutException;
import de.hybris.platform.b2b.punchout.PunchOutSession;
import de.hybris.platform.b2b.punchout.PunchOutUtils;
import de.hybris.platform.b2b.punchout.constants.PunchOutSetupOperation;
import de.hybris.platform.b2b.punchout.enums.PunchOutLevel;
import de.hybris.platform.b2b.punchout.security.PunchOutUserAuthenticationStrategy;
import de.hybris.platform.b2b.punchout.services.PunchOutService;
import de.hybris.platform.b2b.punchout.services.PunchOutSessionService;
import de.hybris.platform.b2bpunchoutaddon.constants.B2bpunchoutaddonConstants;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cxml.CXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Component
public class DefaultPunchOutController extends AbstractPageController implements PunchOutController
{
	private static final Logger LOG = LoggerFactory.getLogger(DefaultPunchOutController.class);

	private static final String CART_CMS_PAGE = "cartPage";
	public static final String REDIRECT_PREFIX = "redirect:";
	public static final String ADDON_PREFIX = "addon:";
	public static final String BASE_ADDON_PAGE_PATH = "/b2bpunchoutaddon/pages";

	@Resource(name = "punchOutSessionService")
	private PunchOutSessionService punchoutSessionService;

	@Resource(name = "punchOutService")
	private PunchOutService punchOutService;

	@Resource(name = "punchOutUserAuthenticationStrategy")
	private PunchOutUserAuthenticationStrategy punchOutUserAuthenticationStrategy;

	@Resource(name = "configurationService")
	private ConfigurationService configurationService;

	@Resource(name = "redirectStrategy")
	private RedirectStrategy redirectStrategy;

	@Resource(name = "productFacade")
	private ProductFacade productFacade;

	/**
	 * Used to create a new punch out session by authenticating a punch out user.
	 *
	 * @param sessionId the hybris session ID
	 */
	@GetMapping(value = "/punchout/cxml/session")
	public void handlePunchOutSession(@RequestParam("sid") final String sessionId,
			final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		final PunchOutSession punchOutSession = punchoutSessionService.loadPunchOutSession(sessionId);
		if (LOG.isDebugEnabled())
		{
			LOG.debug("PunchOut session: {}", printSessionInfo(punchOutSession));
		}

		final String userId = punchoutSessionService.retrieveUserId(punchOutSession);

		final HttpSession session = request.getSession();
		session.removeAttribute(B2bpunchoutaddonConstants.PUNCHOUT_USER);

		punchOutUserAuthenticationStrategy.authenticate(userId, request, response);

		// if user was authenticated (no exception was thrown), set the current cart with the one from the setup request session
		punchoutSessionService.setCurrentCartFromPunchOutSetup(sessionId);

		session.setAttribute(B2bpunchoutaddonConstants.PUNCHOUT_USER, userId);

		final String redirectUrl = getRedirectPage(punchOutSession.getOperation());

		final String targetURL = getTargetPage(punchOutSession, redirectUrl);
		redirectStrategy.sendRedirect(request, response, targetURL);
	}

	/**
	 * Select page that user will be redirected depending on the provided punchout operation.
	 *
	 * @param operation
	 */
	protected String getRedirectPage(final String operation)
	{
		final Configuration configuration = configurationService.getConfiguration();

		if (StringUtils.equalsIgnoreCase(operation, PunchOutSetupOperation.EDIT.toString()))
		{
			return configuration.getString("b2bpunchoutaddon.redirect.edit");
		}
		else if (StringUtils.equalsIgnoreCase(operation, PunchOutSetupOperation.INSPECT.toString()))
		{
			return configuration.getString("b2bpunchoutaddon.redirect.inspect");
		}
		return configuration.getString("b2bpunchoutaddon.redirect.create");
	}

	/**
	 * get Target page that user will be redirected depending on the provided punchout getTargetId().
	 *
	 * @param punchoutSession,redirectUrl
	 */
	protected String getTargetPage(final PunchOutSession punchoutSession, final String redirectUrl)
	{
		if (PunchOutLevel.PRODUCT.equals(punchoutSession.getPunchoutLevel()))
		{
			try
			{
				final ProductData productData = productFacade.getProductForCodeAndOptions(punchoutSession.getSelectedItem(), Arrays.asList(ProductOption.URL));
				return redirectUrl + productData.getUrl();
			}
			catch (UnknownIdentifierException e)
			{
				return "/404";
			}
		}
		return redirectUrl;
	}

	/**
	 * Cancels a requisition (POST) to the punch out provider sending a cancel message.
	 *
	 * @return Redirect to cart page.
	 * @throws CMSItemNotFoundException
	 */
	@GetMapping(value = "/punchout/cxml/cancel")
	public String cancelRequisition(final Model model)
			throws CMSItemNotFoundException
	{
		final CXML cXML = punchOutService.processCancelPunchOutOrderMessage();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("CXML from processCancelPunchOutOrderMessage(): {}", PunchOutUtils.marshallFromBeanTree(cXML));
		}
		processRequisitionMessage(cXML, model);

		return ADDON_PREFIX + BASE_ADDON_PAGE_PATH + "/punchout/punchoutSendOrderPage";
	}


	protected String printSessionInfo(final PunchOutSession punchoutSession)
	{

		return new ToStringBuilder(punchoutSession, ToStringStyle.SHORT_PREFIX_STYLE).append("operation",
						punchoutSession.getOperation()).append("browserFormPostUrl", punchoutSession.getBrowserFormPostUrl())
				.append("buyerCookie", punchoutSession.getBuyerCookie()).append("time", punchoutSession.getTime())
				.append("initiatedBy",
						ToStringBuilder.reflectionToString(punchoutSession.getInitiatedBy(), ToStringStyle.SHORT_PREFIX_STYLE))
				.append("targetedTo",
						ToStringBuilder.reflectionToString(punchoutSession.getTargetedTo(), ToStringStyle.SHORT_PREFIX_STYLE))
				.append("sentBy", ToStringBuilder.reflectionToString(punchoutSession.getSentBy(), ToStringStyle.SHORT_PREFIX_STYLE))
				.toString();
	}

	/**
	 * Places a requisition (POST) to the punchout provider sending the information of the cart.
	 *
	 * @return Redirect to cart page.
	 * @throws CMSItemNotFoundException
	 */
	@GetMapping(value = "/punchout/cxml/requisition")
	public String placeRequisition(final Model model, final HttpServletRequest request)
			throws CMSItemNotFoundException
	{
		final CXML cXML = punchOutService.processPunchOutOrderMessage();
		if (LOG.isDebugEnabled())
		{
			LOG.debug("CXML input:{}", PunchOutUtils.marshallFromBeanTree(cXML));
		}
		processRequisitionMessage(cXML, model);

		return ADDON_PREFIX + BASE_ADDON_PAGE_PATH + "/punchout/punchoutSendOrderPage";
	}


	protected void processRequisitionMessage(final CXML cXML, final Model model)
			throws CMSItemNotFoundException
	{
		final String cXMLContents = PunchOutUtils.transformCXMLToBase64(cXML);
		model.addAttribute("orderAsCXML", cXMLContents);
		model.addAttribute("browseFormPostUrl", punchoutSessionService.getCurrentPunchOutSession().getBrowserFormPostUrl());

		GlobalMessages.addConfMessage(model, "punchout.message.redirecting");

		storeCmsPageInModel(model, getContentPageForLabelOrId(CART_CMS_PAGE));

		punchOutUserAuthenticationStrategy.logout();
	}

	@ExceptionHandler(Exception.class)
	public String handleException(final Exception e, final HttpServletRequest request)
	{
		LOG.error(e.getMessage(), e);
		request.setAttribute("error", e);
		request.setAttribute("message", PunchOutException.PUNCHOUT_EXCEPTION_MESSAGE);

		return ADDON_PREFIX + BASE_ADDON_PAGE_PATH + "/error/punchoutError";
	}
}
