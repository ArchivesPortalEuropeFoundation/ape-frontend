<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="portal" uri="http://portal.archivesportaleurope.eu/tags"%>
<%@ taglib prefix="liferay-portlet" uri="http://liferay.com/tld/portlet" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<portlet:defineObjects />
<c:set var="element"><c:out value="${param['element']}" /></c:set>
<c:set var="id"><c:out value="${param['id']}" /></c:set>
<c:set var="term"><c:out value="${param['term']}" /></c:set>
<portal:friendlyUrl var="eadDisplaySearchUrl" type="eaddisplay-search"/>
<portal:friendlyUrl var="eadDisplayDirectUrl" type="eaddisplay-frontpage"/>

<div id="content">	
<portal:ead type="frontpage" xml="${eadContent.xml}" searchTerms="${term}"  searchFieldsSelectionId="${element}" />
</div>
<div id="more-line" class="hide-more-line">&nbsp;</div>
<div id="viewFullFond" class="linkButton">
<portal:generateSearchWords var="encodedTerm" term="${advancedSearch.term}" element="${advancedSearch.element}"/>		
	<c:choose>
		<c:when test="${empty encodedTerm }">
			<c:set var="url" value="${eadDisplayDirectUrl}/${aiRepoCode}/${xmlTypeName}/${eadid}"/>
		</c:when>
		<c:otherwise>
			<c:set var="url" value="${eadDisplaySearchUrl}/${id}/${element}/${encodedTerm}"/>
		</c:otherwise>
	</c:choose>	
	<a href="${url}" target="_blank"><fmt:message key="seconddisplay.view.${xmlType.resourceName}" /><span class="icon_new_window">&nbsp;</span></a>
</div>