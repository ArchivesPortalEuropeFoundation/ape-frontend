<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet_2_0"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ page contentType="text/html" isELIgnored="false"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="portal" uri="http://portal.archivesportaleurope.eu/tags"%>
<%@ taglib prefix="ape" uri="http://commons.archivesportaleurope.eu/tags"%>
<!-- <link rel="stylesheet" href="//code.jquery.com/ui/1.11.2/themes/smoothness/jquery-ui.css">
<script src="//code.jquery.com/jquery-1.11.2.js"></script>
<script src="//code.jquery.com/ui/1.11.2/jquery-ui.js"></script> -->

<portlet:resourceURL var="seeAvaiableCollectionsUrl" id="seeAvaiableCollections"/>
<portlet:resourceURL var="CollectionUrl" id="newCollection"/>

<portlet:resourceURL var="addBookmarksToUrl" id="addBookmarksTo" >
	<portlet:param name="bookmarkId"  value="${bookmarkId}" />
</portlet:resourceURL>

<script>
	$(document).ready(function() {
		loadDialogShow('${bookmarkId}','${seeAvaiableCollectionsUrl}');
	});
</script>

<div id="mycollectionPortletDiv" class="hidden" title="<fmt:message key="collections.message.addToCollection"/>">
	<c:if test="${!loggedIn}">
		<div class="error">
			<fmt:message key="bookmarks.logged.ko"/>
		</div>
	</c:if>
	<c:if test="${loggedIn}">
		<form name="frm" id="frm" method="post" action="${addBookmarksToUrl}">
			<c:choose>
			<c:when test="${hasNotCollections}">
				<div class="error">
					<fmt:message key="collections.no"/>
				</div>
				<hr>
			</c:when>
			<c:when test="${hasFreeCollections}">
				<table>
					<tr>
						<td>
							<input id="searchTerm" tabindex="1" maxlength="30"/>
						</td>
						<td>
							<div id="searchCollectionsButton" class="linkButton">
					 			<a href="javascript:showCollections('${bookmarkId}','${seeAvaiableCollectionsUrl}')"><fmt:message key="advancedsearch.message.search" /></a>
							</div>
							<div id="searchButtonGrey" class="disableBookmarkButton hidden">
					 			<fmt:message key="advancedsearch.message.search" />
							</div>
						</td>
					</tr>
				</table>
				<hr>
				<c:if test="${not empty collections}">
					<table class="defaultlayout textSize">
						<tr>
							<th class="collectionSearchCheckbox"><fmt:message key="collections.signal"/></th>
							<th class="collectionSearchTerms"><fmt:message key="advancedsearch.text.title2"/></th>
						</tr>
						<c:forEach var="collection" items="${collections}" varStatus="counter">
							<tr id="collectionField_${counter}">
								<td class="collectionSearchCheckbox"><input type="checkbox" name="collectionToAdd_${collection.id}" id="collectionToAdd_${newSearch.id}" /></td>
								<td class="collectionSearchTerms"><c:out value="${collection.title}" /></td>
							</tr>
						</c:forEach>
					</table>
					<hr>
					<div id="mycollectionCreateAction" class="linkButton" style="display: inline-flex">
			 			<a href="javascript:saveBookmarkInCollections('${addBookmarksToUrl}','<fmt:message key="collections.bookmarks.error.neededatleastone" />')"><fmt:message key="collections.bookmarks.add" /></a>
					</div>
				</c:if>
				<c:if test="${empty collections}">
					<div class="error">
						<fmt:message key="collections.searches.no.results"/>
					</div>
					<hr>
					<div id="searchButton" class="linkButton" style="display: inline-flex">
			 			<a href="javascript:showAllCollections('${bookmarkId}','${seeAvaiableCollectionsUrl}')"><fmt:message key="collections.back" /></a>
					</div>
				</c:if>
			</c:when>
			<c:otherwise>
				<div class="error">
					<fmt:message key="collections.bookmarks.no.free"/>
				</div>
				<hr>
			</c:otherwise>
			</c:choose>
			<div id="searchButton" class="linkButton" style="display: inline-flex">
	 			<a href="javascript:createCollections('${bookmarkId}','${CollectionUrl}')"><fmt:message key="collections.create.new" /></a>
			</div>
	 	</form>
	</c:if>
</div>