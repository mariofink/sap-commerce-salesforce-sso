/*
 * Copyright (c) 2025 SAP SE or an SAP affiliate company. All rights reserved.
 */
package de.hybris.platform.acceleratorstorefrontcommons.forms.validation;

import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.apache.commons.beanutils2.PropertyUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;


/**
 * Validator for equal attributes
 */
public class EqualAttributesValidator implements ConstraintValidator<EqualAttributes, Object>
{
	private static final Logger LOG = Logger.getLogger(EqualAttributesValidator.class);

	private String firstAttribute;
	private String secondAttribute;

	@Override
	public void initialize(final EqualAttributes constraintAnnotation)
	{
		Assert.notEmpty(constraintAnnotation.value(), "must not be empty");
		Assert.isTrue(constraintAnnotation.value().length == WebConstants.ARRAY_LENGTH_TWO, "must be true");
		firstAttribute = constraintAnnotation.value()[0];
		secondAttribute = constraintAnnotation.value()[1];
		Assert.hasText(firstAttribute, "must have text; it must not be null, empty, or blank");
		Assert.hasText(secondAttribute, "must have text; it must not be null, empty, or blank");
		Assert.isTrue(!firstAttribute.equals(secondAttribute), "must be true");
	}

	@Override
	public boolean isValid(final Object object, final ConstraintValidatorContext constraintContext)
	{
		if (object == null)
		{
			return true;
		}
		try
		{
			final Object first = PropertyUtils.getProperty(object, firstAttribute);
			final Object second = PropertyUtils.getProperty(object, secondAttribute);
			return new EqualsBuilder().append(first, second).isEquals();
		}
		catch (final Exception e)
		{
			LOG.error("Could not validate", e);
			throw new IllegalArgumentException(e);
		}
	}
}
