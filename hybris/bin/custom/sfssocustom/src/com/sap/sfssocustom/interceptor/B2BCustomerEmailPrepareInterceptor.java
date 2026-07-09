package com.sap.sfssocustom.interceptor;

import de.hybris.platform.b2b.model.B2BCustomerModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;

import org.apache.commons.lang3.StringUtils;

/**
 * Ensures a JIT-provisioned {@link B2BCustomerModel} coming from SAML SSO has a mandatory email set.
 * The OOTB {@code DefaultSSOService} only sets uid/name/password/groups and never populates the
 * mandatory {@code email} attribute, causing a MissingMandatoryAttributesException on save.
 * Here we default the email to the uid (which is the SSO login / email address).
 */
public class B2BCustomerEmailPrepareInterceptor implements PrepareInterceptor<B2BCustomerModel>
{
	@Override
	public void onPrepare(final B2BCustomerModel customer, final InterceptorContext context) throws InterceptorException
	{
		if (StringUtils.isBlank(customer.getEmail()) && StringUtils.isNotBlank(customer.getUid()))
		{
			customer.setEmail(customer.getUid());
		}
	}
}
