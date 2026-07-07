/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */

package de.hybris.platform.yb2bacceleratorstorefront.controllers.misc;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.misc.UrlUtils;
import de.hybris.platform.servicelayer.i18n.I18NService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.ui.context.Theme;
import org.springframework.ui.context.ThemeSource;
import org.springframework.web.servlet.ThemeResolver;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class FavIconControllerTest
{
	private static final String FAVICON_THEME_CODE = "img.favIcon";
	private static final String ORIGINAL_CONTEXT = "originalContextPath";
	private static final String THEME_NAME = "testTheme";
	private static final String ICON_PATH = "/images/favicon.ico";
	private static final String HOST_URL = "http://localhost:8080";
	private static final String CONTEXT_PATH = "/myApp";

	@InjectMocks
	private FavIconController favIconController;

	@Mock
	private ThemeResolver themeResolver;

	@Mock
	private ThemeSource themeSource;

	@Mock
	private I18NService i18nService;

	@Mock
	private HttpServletRequest request;

	@Mock
	private Theme theme;

	@Mock
	private MessageSource messageSource;


	@Before
	public void setUp()
	{
		when(themeResolver.resolveThemeName(request)).thenReturn(THEME_NAME);
		when(themeSource.getTheme(THEME_NAME)).thenReturn(theme);
		when(theme.getMessageSource()).thenReturn(messageSource);
		when(messageSource.getMessage(eq(FAVICON_THEME_CODE), any(), any())).thenReturn(ICON_PATH);
		when(i18nService.getCurrentLocale()).thenReturn(java.util.Locale.ENGLISH);
		when(request.getAttribute(ORIGINAL_CONTEXT)).thenReturn(CONTEXT_PATH);

		// Mocking necessary methods for UrlUtils
		when(request.getScheme()).thenReturn("http");
		when(request.getServerName()).thenReturn("localhost");
		when(request.getServerPort()).thenReturn(8080);
	}

	@Test
	public void testGetFavIcon()
	{
		final String result = favIconController.getFavIcon(request);

		assertEquals("redirect:" + HOST_URL + CONTEXT_PATH + ICON_PATH, result);

		verify(themeResolver).resolveThemeName(request);
		verify(themeSource).getTheme(THEME_NAME);
		verify(theme).getMessageSource();
		verify(i18nService).getCurrentLocale();
		verify(request).getAttribute(ORIGINAL_CONTEXT);
		verify(request, times(1)).getAttribute(ORIGINAL_CONTEXT);
	}

	@Test
	public void testGetFavIconWithNullTheme()
	{
		when(themeSource.getTheme(THEME_NAME)).thenReturn(null);

		final String result = favIconController.getFavIcon(request);

		assertEquals("redirect:" + HOST_URL + CONTEXT_PATH, result);

		verify(themeResolver).resolveThemeName(request);
		verify(themeSource).getTheme(THEME_NAME);
		verify(request).getAttribute(ORIGINAL_CONTEXT);
	}
}
