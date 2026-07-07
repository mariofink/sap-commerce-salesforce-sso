<%@ taglib prefix="asm" tagdir="/WEB-INF/tags/addons/assistedservicestorefront/asm"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<%-- login --%>
<div id="_asmLogin" class="ASM_login">
	<c:url value="/assisted-service/login" var="loginActionUrl" />
	<asm:login actionNameKey="asm.login" action="${loginActionUrl}" error="" disabledButton="true"/>
</div>
