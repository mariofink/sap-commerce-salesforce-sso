<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="actionNameKey" required="true" type="java.lang.String" %>
<%@ attribute name="action" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<spring:htmlEscape defaultHtmlEscape="true"/>
<div class="headline">
    <spring:theme code="login.title"/>
</div>

<form:form action="${action}" method="post" modelAttribute="loginForm">
    <c:if test="${not empty message}">
        <span class="has-error"> <spring:theme code="${message}"/>
        </span>
    </c:if>

    <c:choose>
        <c:when test="${loginForm.otpVerificationTokenEnabled}">
            <label class="control-label" for="otpUserName">
                <spring:theme code="login.email" />
            </label>
            <form:input name="otpUserName" id="otpUserName" path="otpUserName" class="form-control"
                        value="${loginForm.otpUserName}"/>
            <form:input name="lastOtpUserName" id="lastOtpUserName" path="lastOtpUserName" type="hidden"/>

            <label class="control-label" for="otpPassword">
                <spring:theme code="login.password" />
            </label>
            <form:password name="otpPassword" id="otpPassword" path="otpPassword" class="form-control"/>

            <div class="forgotten-password">
                <ycommerce:testId code="login_forgotPassword_link">
                    <spring:url value="/login/pw/request" var="forgottenPwdUrl" htmlEscape="false"/>
                    <a href="#" data-link="${fn:escapeXml(forgottenPwdUrl)}" class="js-password-forgotten"
                       data-cbox-title="<spring:theme code="forgottenPwd.title"/>">
                        <spring:theme code="login.link.forgottenPwd"/>
                    </a>
                </ycommerce:testId>
            </div>
            <input name="secondsForLogin" id="secondsForLogin" path="secondsForLogin" type="hidden"
                value="${secondsForLogin}"/>
            <form:input name="j_username" id="j_username" path="j_username" type="hidden"
                        value="${loginForm.j_username}"/>
            <label class="control-label" for="j_password">
                <spring:theme code="login.otp.token" />
            </label>
            <div class="send-verification-token">
                <div class="row">
                    <div class="col-xs-11">
                        <form:input name="j_password" id="j_password" path="j_password" class="form-control"/>
                        <div class="send_otp_token_for_error_info">
                            <span class="help-block"><spring:theme code="otp.token.rate.limit.error" /></span>
                        </div>
                    </div>
                    <div class="col-xs-1 send-button-container">
                        <a href="#" class="send-verification-token-link disabled-link">
                           <spring:theme code="login.button.send.otp.token" />
                        </a>
                        <a href="#" class="resend-verification-token-link disabled-link">
                            <spring:theme code="login.button.resend.otp.token" />
                        </a>
                    </div>
                </div>
                <div class="sent_otp_token_info">
                    <div class="green-circle">
                      <span class="glyphicon glyphicon-ok"></span>
                    </div>
                    <span class="info-text"><spring:theme code="login.info.sent.otp.token"/></span>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <formElement:formInputBox idKey="j_username" labelKey="login.email" path="j_username" mandatory="true"/>
            <formElement:formPasswordBox idKey="j_password" labelKey="login.password" path="j_password" inputCSS="form-control"
                                         mandatory="true"/>

            <div class="forgotten-password">
                <ycommerce:testId code="login_forgotPassword_link">
                    <spring:url value="/login/pw/request" var="forgottenPwdUrl" htmlEscape="false"/>
                    <a href="#" data-link="${fn:escapeXml(forgottenPwdUrl)}" class="js-password-forgotten"
                       data-cbox-title="<spring:theme code="forgottenPwd.title"/>">
                        <spring:theme code="login.link.forgottenPwd"/>
                    </a>
                </ycommerce:testId>
            </div>
        </c:otherwise>
    </c:choose>


    <div class="row login-form-action">
        <c:set var="loginBtnClasses" value="col-sm-12 col-md-6 col-md-push-6"/>

        <c:if test="${enableRegistration}">
            <c:set var="loginBtnClasses" value="col-sm-6 col-sm-push-6"/>
        </c:if>

        <div class="${fn:escapeXml(loginBtnClasses)}">
            <ycommerce:testId code="login_Login_button">
                <button type="submit" class="btn btn-primary btn-block">
                    <spring:theme code="${actionNameKey}"/>
                </button>
            </ycommerce:testId>
        </div>

        <div class="col-sm-6 col-sm-pull-6">
            <c:if test="${enableRegistration}">
                <spring:url value="/register" var="registerUrl" htmlEscape="false"/>
                <a href="${fn:escapeXml(registerUrl)}">
                    <button type="button" class="btn btn-default btn-block">
                        <spring:theme code="text.secureportal.link.createAccount"/>
                    </button>
                </a>
            </c:if>
        </div>
    </div>
</form:form>

