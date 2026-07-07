<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="actionNameKey" required="true" type="java.lang.String" %>
<%@ attribute name="action" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<spring:htmlEscape defaultHtmlEscape="true"/>
<div class="headline">
    <spring:theme code="text.secureportal.register.new.customer"/>
</div>
<p>
    <spring:theme code="text.secureportal.register.description"/>
</p>

<div>
    <form:form method="post" id="registerForm" modelAttribute="registrationForm" action="${action}">
        <formElement:formSelectBox idKey="address.country_del" labelKey="address.country"
                                   path="companyAddressCountryIso" mandatory="true"
                                   skipBlank="false" skipBlankMessageKey="address.selectCountry" items="${countries}"
                                   itemValue="isocode" selectCSSClass="form-control"/>
        <formElement:formSelectBoxDefaultEnabled idKey="register.title" labelKey="register.title" path="titleCode" mandatory="true"
                                   skipBlank="false"
                                   skipBlankMessageKey="form.select.none" items="${titles}"
                                   selectCSSClass="form-control"/>

        <formElement:formInputBox idKey="text.secureportal.register.firstName"
                                  labelKey="text.secureportal.register.firstName" path="firstName"
                                  inputCSS="form-control" mandatory="true"/>
        <formElement:formInputBox idKey="text.secureportal.register.lastName"
                                  labelKey="text.secureportal.register.lastName" path="lastName"
                                  inputCSS="form-control" mandatory="true"/>
        <formElement:formInputBox idKey="text.secureportal.register.companyName"
                                  labelKey="text.secureportal.register.companyName" path="companyName"
                                  inputCSS="form-control" mandatory="true"/>
        <formElement:formInputBox idKey="address.line1" labelKey="address.line1" path="companyAddressStreet"
                                  inputCSS="form-control" mandatory="true"/>
        <formElement:formInputBox idKey="address.line2" labelKey="address.line2" path="companyAddressStreetLine2"
                                  inputCSS="form-control" mandatory="false"/>
        <formElement:formInputBox idKey="address.townCity" labelKey="address.townCity" path="companyAddressCity"
                                  inputCSS="form-control" mandatory="true"/>
        <formElement:formInputBox idKey="address.postcode" labelKey="address.postcode" path="companyAddressPostalCode"
                                  inputCSS="form-control" mandatory="true"/>
        <formElement:formInputBox idKey="text.secureportal.register.position"
                                  labelKey="text.secureportal.register.position" path="position" inputCSS="form-control"
                                  mandatory="true"/>

        <div class="row">
            <div class="col-sm-10 phone">
                <formElement:formInputBox idKey="storeDetails.table.telephone" labelKey="storeDetails.table.telephone"
                                          path="telephone" inputCSS="form-control" mandatory="true"/>
            </div>
            <div class="col-sm-2 extension">
                <formElement:formInputBox idKey="text.secureportal.register.extension"
                                          labelKey="text.secureportal.register.extension" path="telephoneExtension"
                                          inputCSS="form-control"/>
            </div>
        </div>
        <formElement:formInputBox idKey="register.email" labelKey="register.email" path="email"
                                  inputCSS="form-control js-secureportal-orignal-register-email" mandatory="true"/>

        <c:choose>
            <c:when test="${registrationForm.otpForRegistrationEnabled}">
                <div class="form-group">
                    <label class="control-label" for="register.confirm.email"> <spring:theme
                            code="guest.confirm.email"/></label>
                    <input class="form-control js-secureportal-confirm-register-email" id="register.confirm.email"/>
                    <div name="helpMessage" id="helpMessage" path="helpMessage" class="help-block display-none">
                        <spring:theme code="register.email.invalid" />
                    </div>

                    <div class="js-secureportal-email-not-match-message has-error" style="display:none">
                        <span class="help-block">
                            <spring:theme code="text.secureportal.register.email.not.match"/>
                        </span>
                    </div>
                    <div class='send-verification-token-for-registration'>
                        <div class='row'>
                            <div class='col-xs-11'>
                                <form:input name="otpForRegistrationEnabled" id="otpForRegistrationEnabled" path="otpForRegistrationEnabled" type="hidden"
                                            value="${registrationForm.otpForRegistrationEnabled}"/>
                                <input name="secondsForRegistration" id="secondsForRegistration" path="secondsForRegistration" type="hidden"
                                    value="${secondsForRegistration}"/>
                                <form:input name="verificationTokenId" id="verificationTokenId" path="verificationTokenId" type="hidden"
                                            value="${registrationForm.verificationTokenId}"/>
                                <formElement:formInputBox idKey="verificationTokenCode"
                                            labelKey="register.otp.token" path="verificationTokenCode" inputCSS="form-control" mandatory="true" />
                                <div class="send_otp_token_for_registration_error_info">
                                    <span class="help-block"><spring:theme code="otp.token.rate.limit.error" /></span>
                                </div>
                            </div>
                            <div class='col-xs-1 send-button-container'>
                                <a href="#" class="send-verification-token-for-registration-link disabled-link">
                                    <spring:theme code="register.button.send.otp.token" />
                                </a>
                                <a href="#" class="resend-verification-token-for-registration-link disabled-link">
                                    <spring:theme code="register.button.resend.otp.token" />
                                </a>
                            </div>
                        </div>
                    <div class="sent_otp_token_for_registration_info">
                        <div class="green-circle">
                            <span class="glyphicon glyphicon-ok"></span>
                        </div>
                        <span class="info-text"><spring:theme code="register.info.sent.otp.token"/></span>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="form-group">
                    <label class="control-label" for="register.confirm.email"> <spring:theme
                            code="guest.confirm.email"/></label>
                    <input class="form-control js-secureportal-confirm-register-email" id="register.confirm.email"/>

                    <div class="js-secureportal-email-not-match-message has-error" style="display:none">
                        <span class="help-block">
                            <spring:theme code="text.secureportal.register.email.not.match"/>
                        </span>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

        <formElement:formTextArea idKey="text.secureportal.register.message"
                                  labelKey="text.secureportal.register.message" path="message"
                                  areaCSS="textarea form-control" mandatory="false"/>

        <div class="register-form-action row">
            <div class="col-xs-12 col-md-6 pull-right">
                <ycommerce:testId code="register_Register_button">
                    <button data-theme="b" class="js-secureportal-register-button btn btn-primary btn-block">
                        <spring:theme code='${actionNameKey}'/>
                    </button>
                </ycommerce:testId>
            </div>
        </div>
    </form:form>
</div>
