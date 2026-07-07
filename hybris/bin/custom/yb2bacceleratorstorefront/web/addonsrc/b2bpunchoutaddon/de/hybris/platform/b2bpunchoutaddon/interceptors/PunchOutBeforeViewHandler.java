/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.b2bpunchoutaddon.interceptors;

import de.hybris.platform.acceleratorstorefrontcommons.interceptors.BeforeViewHandler;
import de.hybris.platform.b2b.punchout.PunchOutException;
import de.hybris.platform.b2b.punchout.PunchOutResponseCode;
import de.hybris.platform.b2bpunchoutaddon.constants.B2bpunchoutaddonConstants;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;


public class PunchOutBeforeViewHandler implements BeforeViewHandler
{

	public static final String VIEW_NAME_MAP_KEY = "viewName";
	private static final String ERROR_PAGE = "pages/error/errorNotFoundPage";
	private Map<String, Map<String, String>> viewMap;
	private Set<String> punchOutAllowListUrlSet = new HashSet<>();

	@Override
	public void beforeView(final HttpServletRequest request, final HttpServletResponse response, final ModelAndView modelAndView)
			throws PunchOutException
	{
		final String viewName = modelAndView.getViewName();
		try
		{
			if (StringUtils.isNotBlank((String) request.getSession().getAttribute(B2bpunchoutaddonConstants.PUNCHOUT_USER)))
			{
				final String punchoutViewName = getPunchoutView(viewName);
				if (ERROR_PAGE.equals(punchoutViewName))
				{
					response.sendRedirect(ERROR_PAGE);
				}
				else
				{
					modelAndView.setViewName(punchoutViewName);
					setPunchoutModeInModel(modelAndView.getModelMap());
				}
			}
		}
		catch (final Exception e)
		{
			throw new PunchOutException(PunchOutResponseCode.INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
	}

	protected String getPunchoutView(final String viewName)
	{
		if (viewMap.containsKey(viewName))
		{
			return B2bpunchoutaddonConstants.VIEW_PAGE_PREFIX + viewMap.get(viewName).get(VIEW_NAME_MAP_KEY);
		}
		else if (getPunchOutAllowListUrlSet().contains(viewName))
		{
			return viewName;
		}
		else
		{
			return ERROR_PAGE;
		}
	}

	protected void setPunchoutModeInModel(final ModelMap model)
	{
		model.addAttribute("punchoutMode", Boolean.TRUE);
	}

	public Map<String, Map<String, String>> getViewMap()
	{
		return viewMap;
	}

	public void setViewMap(final Map<String, Map<String, String>> viewMap)
	{
		this.viewMap = viewMap;
	}


	protected Set<String> getPunchOutAllowListUrlSet()
	{
		return punchOutAllowListUrlSet;
	}

	public void setPunchOutAllowListUrlSet(final Set<String> punchOutAllowListUrlSet)
	{
		this.punchOutAllowListUrlSet = punchOutAllowListUrlSet;
	}
}
