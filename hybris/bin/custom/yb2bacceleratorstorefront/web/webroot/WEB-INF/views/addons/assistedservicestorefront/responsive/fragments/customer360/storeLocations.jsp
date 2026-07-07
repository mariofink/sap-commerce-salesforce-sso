<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="store" tagdir="/WEB-INF/tags/addons/assistedservicestorefront/store" %>

<store:storeListForm />

<script>
	ASM.storefinder.autoLoad("${fragmentData.address}");
	<c:choose>
		<c:when test="${not empty fragmentData.address}">
			ASM.storefinder.getInitStoreData("${fragmentData.address}", "", "");
		</c:when>
		<c:otherwise>
			ASM.storefinder.getInitStoreData(null,ASM.storefinder.coords.latitude, ASM.storefinder.coords.longitude);
		</c:otherwise>
	</c:choose>
</script>