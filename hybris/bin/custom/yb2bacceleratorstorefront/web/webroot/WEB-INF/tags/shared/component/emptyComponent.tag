<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:if test="${cmsPageRequestContextData.liveEdit}">
	<div class="yCmsComponentEmpty">
		Empty ${fn:escapeXml(component.itemtype)}: ${fn:escapeXml(component.name)}
	</div>
</c:if>
