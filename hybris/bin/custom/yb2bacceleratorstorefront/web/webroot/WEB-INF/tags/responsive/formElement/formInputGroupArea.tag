<%@ tag body-content="scriptless" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%@ attribute name="CSSClass" required="false" type="java.lang.String" %>

<div class="inputArea ${fn:escapeXml(CSSClass)}">
	<jsp:doBody />
</div>
 