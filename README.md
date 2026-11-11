# SAP Commerce SSO with Salesforce Identity Provider

This project demonstrates how to configure SAP Commerce (Hybris) to use Salesforce as an Identity Provider (IdP) for Single Sign-On (SSO) using SAML 2.0.

## Prerequisites
- SAP Commerce (Hybris) installed and running
- Salesforce account with access to Identity Provider settings (e.g. your scratch org)

## Setup
- Follow the steps here: https://wiki.goodyear.eu/x/xBUbG
- Copy local.sso.properties to local.properties.
- Restore non-durable SSO artifacts with the content below:

## Non-durable SSO artifacts — NOT tracked in git

hybris/bin/modules/platform/samlsinglesignon/web/webroot/WEB-INF/security/metadata.xml

```
<?xml version="1.0" encoding="UTF-8"?><md:EntityDescriptor xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata" entityID="https://your-salesforce-org.my.salesforce.com" validUntil="2036-07-04T14:01:00.589Z" xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
   <md:IDPSSODescriptor protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
      <md:KeyDescriptor use="signing">
         <ds:KeyInfo>
            <ds:X509Data>
               <ds:X509Certificate><!-- Add certificate downloaded from Salesforce here --></ds:X509Certificate>
            </ds:X509Data>
         </ds:KeyInfo>
      </md:KeyDescriptor>
      <md:SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" Location="https://your-salesforce-org.my.salesforce.com/services/auth/idp/saml2/logout"/>
      <md:SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect" Location="https://your-salesforce-org.my.salesforce.com/services/auth/idp/saml2/logout"/>
      <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</md:NameIDFormat>
      <md:SingleSignOnService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" Location="https://your-salesforce-org.my.salesforce.com/idp/endpoint/HttpPost"/>
      <md:SingleSignOnService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect" Location="https://your-salesforce-org.my.salesforce.com/idp/endpoint/HttpRedirect"/>
   </md:IDPSSODescriptor>
</md:EntityDescriptor>

```

hybris/bin/modules/platform/samlsinglesignon/web/webroot/WEB-INF/security/spring-security-config.xml
```
<?xml version="1.0" encoding="UTF-8" ?>
<!--
 Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
-->
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:security="http://www.springframework.org/schema/security"
       xmlns:util="http://www.springframework.org/schema/util"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd
              http://www.springframework.org/schema/security https://www.springframework.org/schema/security/spring-security.xsd
              http://www.springframework.org/schema/util https://www.springframework.org/schema/util/spring-util.xsd
			  http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd">

    <!-- Enable auto-wiring -->
    <context:annotation-config/>
    <context:component-scan base-package="de.hybris.platform.samlsinglesignon.security"/>

    <!-- Unsecured pages -->

    <security:http security="none" pattern="/logout.jsp"/>
    <security:http security="none" pattern="/misconfiguration.jsp"/>
    <security:http security="none" pattern="/favicon.ico"/>

    <!-- Secured pages -->
    <security:http entry-point-ref="samlEntryPoint" authentication-manager-ref="samlAuthenticationManager"
                   use-expressions="false" use-authorization-manager="false" security-context-explicit-save="false" >
        <security:intercept-url pattern="/**" access="IS_AUTHENTICATED_FULLY"/>
        <security:custom-filter before="FIRST" ref="samlMetadataFilter"/>
        <security:custom-filter before="BASIC_AUTH_FILTER" ref="saml2Filter"/>
        <security:custom-filter before="PRE_AUTH_FILTER" ref="saml2RequestFilter"/>
        <security:custom-filter after="CORS_FILTER" ref="saml2LogoutResponseFilter"/>
        <security:custom-filter before="CSRF_FILTER" ref="saml2LogoutRequestFilter"/>
        <security:custom-filter after="CSRF_FILTER" ref="samlLogoutFilter"/>
        <security:custom-filter ref="samlLoginFormGenerationFilter" position="FORM_LOGIN_FILTER"/>
        <security:csrf disabled="true"/>
        <security:access-denied-handler ref="samlAccessDeniedHandler"/>
    </security:http>

    <bean id="samlLoginFormGenerationFilter"
          class="de.hybris.platform.samlsinglesignon.security.SAMLLoginPageGeneratingFilter">
        <constructor-arg name="repository" ref="relyingPartyRegistrationRepository"/>
    </bean>

    <bean id="saml2LogoutResponseFilter"
          class="org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutResponseFilter">
        <constructor-arg name="relyingPartyRegistrationResolver" ref="defaultRelyingPartyRegistrationResolver"/>
        <constructor-arg name="logoutResponseValidator" ref="openSamlLogoutResponseValidator"/>
        <constructor-arg name="logoutSuccessHandler" ref="successLogoutHandler"/>
        <property name="logoutRequestMatcher" ref="singleLogoutRequestMatcher"/>
    </bean>

    <bean id="samlLogoutFilter"
          class="de.hybris.platform.samlsinglesignon.security.SAMLLogoutFilter">
        <constructor-arg name="singleLogoutSuccessHandler" ref="saml2RelyingPartyInitiatedLogoutSuccessHandler"/>
        <constructor-arg name="localLogoutSuccessHandler" ref="successLogoutHandler"/>
        <constructor-arg name="localLogoutHandlers">
            <list>
                <ref bean="logoutHandler"/>
                <ref bean="logoutSuccessEventPublishingLogoutHandler"/>
            </list>
        </constructor-arg>
        <constructor-arg name="singleLogoutHandlers">
            <list>
                <ref bean="logoutHandler"/>
            </list>
        </constructor-arg>
        <property name="filterProcessesUrl" value="/saml/logout/**"/>
    </bean>

    <bean id="saml2RelyingPartyInitiatedLogoutSuccessHandler"
          class="org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2RelyingPartyInitiatedLogoutSuccessHandler">
        <constructor-arg ref="openSaml5LogoutRequestResolver"/>
    </bean>

    <bean id="logoutSuccessEventPublishingLogoutHandler"
          class="org.springframework.security.web.authentication.logout.LogoutSuccessEventPublishingLogoutHandler">
    </bean>

    <bean id="saml2LogoutRequestFilter"
          class="org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestFilter">
        <constructor-arg name="relyingPartyRegistrationResolver" ref="defaultRelyingPartyRegistrationResolver"/>
        <constructor-arg name="logoutRequestValidator" ref="openSamlLogoutRequestValidator"/>
        <constructor-arg name="logoutResponseResolver" ref="openSaml5LogoutResponseResolver"/>
        <constructor-arg name="handlers">
            <list>
                <ref bean="logoutHandler"/>
                <ref bean="singleLogOutSuccessLogoutHandler"/>
            </list>
        </constructor-arg>
        <property name="logoutRequestMatcher" ref="singleLogoutRequestMatcher"/>
    </bean>

    <bean id="openSamlLogoutRequestValidator"
          class="org.springframework.security.saml2.provider.service.authentication.logout.OpenSaml5LogoutRequestValidator"/>

    <bean id="openSamlLogoutResponseValidator"
          class="org.springframework.security.saml2.provider.service.authentication.logout.OpenSaml5LogoutResponseValidator"/>

    <bean id="openSaml5LogoutResponseResolver"
          class="org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml5LogoutResponseResolver">
        <constructor-arg ref="defaultRelyingPartyRegistrationResolver"/>
    </bean>

    <bean id="samlEntryPoint" class="de.hybris.platform.samlsinglesignon.security.SAMLEntryPoint">
        <constructor-arg name="repository" ref="relyingPartyRegistrationRepository"/>
    </bean>

    <security:authentication-manager alias="samlAuthenticationManager">
        <security:authentication-provider ref="samlAuthenticationProvider"/>
    </security:authentication-manager>

    <util:constant id="DEFAULT_FILTER_PROCESSES_URI"
                   static-field="org.springframework.security.saml2.provider.service.web.authentication.Saml2WebSsoAuthenticationFilter.DEFAULT_FILTER_PROCESSES_URI"/>

    <bean id="saml2Filter"
          class="org.springframework.security.saml2.provider.service.web.authentication.Saml2WebSsoAuthenticationFilter">
        <constructor-arg type="org.springframework.security.web.authentication.AuthenticationConverter"
                         ref="authenticationConverter"/>
        <constructor-arg ref="DEFAULT_FILTER_PROCESSES_URI"/>
        <property name="authenticationManager" ref="samlAuthenticationManager"/>
        <property name="authenticationSuccessHandler" ref="successRedirectHandler"/>
        <property name="filterProcessesUrl" ref="filterProcessesUrl"/>
    </bean>

    <bean id="saml2RequestFilter"
          class="org.springframework.security.saml2.provider.service.web.Saml2WebSsoAuthenticationRequestFilter">
        <constructor-arg name="authenticationRequestResolver" ref="authenticationRequestResolver"/>
    </bean>

    <bean id="authenticationRequestResolver"
          class="org.springframework.security.saml2.provider.service.web.authentication.OpenSaml5AuthenticationRequestResolver">
        <constructor-arg name="relyingPartyRegistrationResolver" ref="defaultRelyingPartyRegistrationResolverIdAware"/>
    </bean>

    <bean id="samlAuthenticationProvider"
          class="de.hybris.platform.samlsinglesignon.security.SamlObjectsFactory"
          factory-method="getAuthenticationProvider"
    />

    <!-- Generation of the Service Provider metadata.xml file -->
    <bean id="samlMetadataFilter" class="org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter">
        <constructor-arg ref="defaultRelyingPartyRegistrationResolverIdAware"/>
        <constructor-arg ref="openSamlMetadataResolver"/>
        <property name="requestMatcher" ref="metadataRequestMatcher"/>
    </bean>

    <bean id="saml2MetadataAfterBeanCreationInterceptor" lazy-init="false"
          class="de.hybris.platform.samlsinglesignon.security.Saml2MetadataFilterAfterBeanCreationInterceptor">
        <constructor-arg name="saml2MetadataFilter" ref="samlMetadataFilter"/>
    </bean>

    <bean id="metadataRequestMatcher"
          class="de.hybris.platform.samlsinglesignon.security.SamlObjectsFactory"
          factory-method="getMetadataRequestMatcher"/>

    <bean id="singleLogoutRequestMatcher"
          class="de.hybris.platform.samlsinglesignon.security.SamlObjectsFactory"
          factory-method="getSingleLogoutRequestMatcher"/>

    <bean id="filterProcessesUrl"
          class="de.hybris.platform.samlsinglesignon.security.SamlObjectsFactory"
          factory-method="getFilterProcessesUrl"/>

    <bean id="openSamlMetadataResolver"
          class="org.springframework.security.saml2.provider.service.metadata.OpenSaml5MetadataResolver"/>

    <bean id="defaultRelyingPartyRegistrationResolver"
          class="org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver">
        <constructor-arg ref="relyingPartyRegistrationRepository"/>
    </bean>

    <bean id="authenticationConverter"
          class="org.springframework.security.saml2.provider.service.web.Saml2AuthenticationTokenConverter">
        <constructor-arg name="relyingPartyRegistrationResolver" ref="defaultRelyingPartyRegistrationResolverIdAware"/>
    </bean>

    <bean id="defaultRelyingPartyRegistrationResolverIdAware"
          class="de.hybris.platform.samlsinglesignon.security.DefaultRelayingPartyRegistrationResolverIdAware">
        <constructor-arg ref="defaultRelyingPartyRegistrationResolver"/>
    </bean>
    <!-- Representation of configured Service Provider and Identity Provider  -->
    <bean id="relyingPartyRegistrationRepository"
          class="de.hybris.platform.samlsinglesignon.security.SamlObjectsFactory"
          factory-method="getRelyingPartyRegistrationRepository">
        <constructor-arg>
            <value type="java.io.File">${sso.metadata.location:./WEB-INF/security/metadata.xml}</value>
        </constructor-arg>
        <constructor-arg value="${sso.entity.id:urn:ssoextension:hybris:de}"/>
        <constructor-arg ref="saml2CredentialsProvider"/>
        <constructor-arg ref="signatureValidatorFilter"/>
    </bean>

    <bean id="saml2CredentialsProvider" class="de.hybris.platform.samlsinglesignon.security.Saml2CredentialsProvider">
        <constructor-arg value="${sso.keystore.location:/WEB-INF/security/samlKeystore.jks}"/>
        <constructor-arg type="java.lang.String" value="${sso.keystore.password:changeit}"/>
        <constructor-arg>
            <map>
                <entry key="${sso.keystore.privatekey.alias:hybris}"
                       value="${sso.keystore.privatekey.password:changeit}"/>
            </map>
        </constructor-arg>
        <constructor-arg type="java.lang.String" value="${sso.keystore.default.certificate.alias:hybris}"/>
        <constructor-arg type="java.lang.String" value="${sso.keystore.type:JKS}"/>
    </bean>

    <bean id="signatureValidatorFilter"
          class="de.hybris.platform.samlsinglesignon.security.SignatureValidationFilterFactory"
          factory-method="getSignatureValidationFilter">
        <constructor-arg ref="x509Certificates"/>
    </bean>

    <bean id="x509Certificates" class="de.hybris.platform.samlsinglesignon.utils.JKSUtil"
          factory-method="getAllCertificates">
        <constructor-arg value="${sso.keystore.location:/WEB-INF/security/samlKeystore.jks}"/>
        <constructor-arg type="java.lang.String" value="${sso.keystore.password:changeit}"/>
        <constructor-arg type="java.lang.String" value="${sso.keystore.type:JKS}"/>
    </bean>

    <!-- Firewall mechanism to prevent a variety of web attacks -->
    <bean id="allowSemicolonHttpFirewall" class="org.springframework.security.web.firewall.StrictHttpFirewall">
        <property name="allowSemicolon" value="true"/>
    </bean>
    <security:http-firewall ref="allowSemicolonHttpFirewall"/>

    <!-- Handler deciding where to redirect user after successful login -->
    <bean id="successRedirectHandler"
          class="org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler">
        <property name="defaultTargetUrl" value="${sso.success.redirect.url:/}"/>
    </bean>

    <!-- Handler for successful local logout -->
    <bean id="successLogoutHandler"
          class="org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler">
        <property name="defaultTargetUrl" value="${samlsinglesignon.logout.redirect.url}"/>
    </bean>

    <bean id="openSaml5LogoutRequestResolver"
          class="de.hybris.platform.samlsinglesignon.security.SamlObjectsFactory"
          factory-method="getOpenSaml5LogoutRequestResolver">
        <constructor-arg ref="defaultRelyingPartyRegistrationResolver"/>
    </bean>

    <!-- Logout handler terminating local session -->
    <bean id="logoutHandler"
          class="org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler">
        <property name="invalidateHttpSession" value="false"/>
    </bean>

    <bean id="singleLogOutSuccessLogoutHandler"
          class="de.hybris.platform.samlsinglesignon.security.SingleLogOutSuccessLogoutHandler">
        <property name="tokenService" ref="tokenService"/>
    </bean>

    <!-- Handler which will be invoked when unauthorized user tries access secured resources -->
    <bean id="samlAccessDeniedHandler" class="de.hybris.platform.samlsinglesignon.security.SAMLAccessDeniedHandler">
        <constructor-arg ref="redirectionControllerBase"/>
    </bean>
</beans>

```