<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.liferay.portal.kernel.portlet.LiferayWindowState"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="portal" uri="http://portal.archivesportaleurope.eu/tags"%>
<%@ taglib prefix="searchresults" uri="http://portal.archivesportaleurope.eu/tags/searchresults"%>
<%@ taglib prefix="ape" uri="http://commons.archivesportaleurope.eu/tags"%> 
<%@ taglib prefix="facets" tagdir="/WEB-INF/tags/facets"%>
<%@ taglib prefix="liferay-portlet" uri="http://liferay.com/tld/portlet" %>
<portlet:defineObjects />

<script type="text/javascript">
	$(document).ready(function() {
        initListTabHandlers();
    });
</script>
<div class="results">
		<div class="tab_header">
			<div id="tabHeaderContent">
			<c:choose>
				<c:when test="${!empty results.errorMessage}">
					<div class="error"><fmt:message key="${results.errorMessage}" /></div>
				</c:when>
				<c:when test="${results.totalNumberOfResults > 0}">
					<div id="numberOfResults">
						<span class="bold"><fmt:message key="advancedsearch.text.results" />:</span>
						<ape:pageDescription numberOfItems="${results.totalNumberOfResults}" pageSize="${results.pageSize}" pageNumber="${eadSearch.pageNumber}" numberFormat="${numberFormat}" />
						<c:if test="${results.partialResults}"><span class="partialresults">(<fmt:message key="search.message.approximately" />)</span></c:if>
					</div>
					<div id="resultPerPageContainer">
						<label for="updateCurrentSearch_resultsperpage" id="resultPerPageLabel" class="bold"><fmt:message key="advancedsearch.text.numberofresults"/></label>
						<form:select path="resultsperpage"  id="updateCurrentSearch_resultsperpage" items="${eadSearch.resultsperpageValues}"/>	
					</div>
					<div id="top-paging" class="paging">
					<ape:paging numberOfItems="${results.totalNumberOfResults}" pageSize="${results.pageSize}" pageNumber="${eadSearch.pageNumber}"
							refreshUrl="javascript:updatePageNumber('');" pageNumberId="pageNumber" maxNumberOfItems="${results.maxTotalNumberOfResults}"/>	
					</div>			
				</c:when>
				<c:otherwise>
					<span id="noResults"><fmt:message key="search.message.notResults" /><c:if test="${results.partialResults}"><span class="partialresults"> (<fmt:message key="search.message.approximately" />)</span></c:if></span>
				</c:otherwise>
			</c:choose>
			</div>
		</div>
		<c:if test="${empty results.errorMessage and (results.totalNumberOfResults > 0  or eadSearch.mode == 'update-search')}">
			<div id="selectedRefinements" class="selectedCriteria">
				<div id="selectedRefinementsTitle"><fmt:message key="advancedsearch.facet.title.choosed" /></div>
				<ul>
					<c:if test="${!empty selectedRefinements }">
						<c:forEach var="selectedRefinement" items="${selectedRefinements}">
						<c:choose>
							<c:when test="${selectedRefinement.date}">
						<li id='${selectedRefinement.fieldName}' class="${selectedRefinement.cssClass}"><a title='${selectedRefinement.longDescription}' href="javascript:removeDateRefinement('${selectedRefinement.fieldName}')">
				${selectedRefinement.longDescription}<span class='close-icon'></span></a></li>
							</c:when>
							<c:otherwise>
						<li id='${selectedRefinement.fieldName}_${selectedRefinement.fieldValue}' class="${selectedRefinement.cssClass}"><a title='${selectedRefinement.longDescription}' href="javascript:removeRefinement('${selectedRefinement.fieldName}','${selectedRefinement.fieldValue}')">
				${selectedRefinement.longDescription}<span class='close-icon'></span></a></li>							
							</c:otherwise>
						</c:choose>

						</c:forEach>
					</c:if>
				</ul>				
			</div>
			
			<div id="resultsContainer">
			<div id="refinements">
				<facets:facets-default facetContainers="${results.facetContainers}"/>
				&nbsp;	
			</div>
			<div  id="searchResultsListContainer">	
				<div id="searchOrder">
					<div id="searchOrderTitle"><fmt:message key="advancedsearch.text.sortsearch" /></div>
					<c:choose>
						<c:when test="${results.totalNumberOfResults > results.maxTotalNumberOfResults}">
							<div id="toManyResults"><fmt:message key="advancedsearch.text.sorting.toomanyresults" /></div>
						</c:when>
						<c:otherwise>
							<searchresults:order currentValue="${eadSearch.order}" value="relevancy" key="advancedsearch.order.relevancy" />
							|
							<searchresults:order currentValue="${eadSearch.order}" value="startDate" key="advancedsearch.text.date" />
							|
							<searchresults:order currentValue="${eadSearch.order}" value="unitTitleSort" key="advancedsearch.text.title2" />
							|
							<searchresults:order currentValue="${eadSearch.order}" value="unitIdSort" key="advancedsearch.text.refcode" />
							|
							<searchresults:order currentValue="${eadSearch.order}" value="unitRecordIDSort" key="advancedsearch.order.eadid" />							
						</c:otherwise>
					</c:choose>
			
				</div>
			<c:if test="${results.totalNumberOfResults > 0}">		
			<div id="searchresultsList">
				<c:forEach var="result" items="${results.items}">
					<div class="list-searchresult" id="list-searchresult-${result.id}">
						<div class="list-searchresult-content list-searchresult-content-${result.level}">
							<div class="list-searchresult-header">
								<c:choose>
									<c:when test="${empty result.title}">
										<c:set var="title"><fmt:message key="advancedsearch.text.notitle" /></c:set>
										<c:set var="titleWithoutHighlighting"><fmt:message key="advancedsearch.text.notitle" /></c:set>
										<c:set var="titleClass" value="notitle"/>
									</c:when>
									<c:otherwise>
										<c:set var="title" value="${result.title}"/>
										<c:set var="titleWithoutHighlighting" value="${result.titleWithoutHighlighting}"/>
										<c:set var="titleClass" value=""/>								
									</c:otherwise>
								</c:choose>
								<portal:eadPersistentLink var="url" xmlTypeName="${result.type}" eadid="${result.eadid}" repoCode="${result.repositoryCode}" unitid="${result.unitidForLink}" searchId="${result.id}" searchFieldsSelectionId="${eadSearch.element}" searchTerms="${eadSearch.term}"/>	
								<a class="unittitle ${titleClass}" target="_blank" title="${titleWithoutHighlighting}"
									href="${url}">${title}
								</a>													
								<c:if test="${!empty result.alterdate}">
									<span class="alterdate" title="${result.alterdateWithoutHighlighting}">${result.alterdate}</span>
								</c:if>
							</div>
							<div class="scopecontent">${result.scopecontent}</div>
							<div class="other">${result.other}</div>
								<c:if test="${result.dao}">
								<div  class="special_info">
								<fmt:message key="advancedsearch.facet.title.dao" />
									<c:forEach var="daoType" items="${result.roledao}">
											<c:if test="${fn:contains(daoType, 'IMAGE')}">
												<fmt:message var="daoTitle" key="advancedsearch.dao.image" />
												<span class="icon_dao_type_small_image" title="${daoTitle}">${daoTitle}</span>
											</c:if>
											<c:if test="${fn:contains(daoType, 'TEXT')}">
												<fmt:message var="daoTitle" key="advancedsearch.dao.text" />
												<span class="icon_dao_type_small_text" title="${daoTitle}">${daoTitle}</span>
											</c:if>
											<c:if test="${fn:contains(daoType, 'VIDEO')}">
												<fmt:message var="daoTitle" key="advancedsearch.dao.video" />
												<span class="icon_dao_type_small_video" title="${daoTitle}">${daoTitle}</span>
											</c:if>
											<c:if test="${fn:contains(daoType, 'SOUND')}">
												<fmt:message var="daoTitle" key="advancedsearch.dao.sound" />
												<span class="icon_dao_type_small_sound" title="${daoTitle}">${daoTitle}</span>
											</c:if>
											<c:if test="${fn:contains(daoType, '3D')}">
												<fmt:message var="daoTitle" key="advancedsearch.dao.3d" />
												<span class="icon_dao_type_small_3d" title="${daoTitle}">${daoTitle}</span>
											</c:if>												
											<c:if test="${fn:contains(daoType, 'UNSPECIFIED')}">
												<fmt:message var="daoTitle" key="advancedsearch.dao.unspecified" />
												<span class="icon_dao_type_small_unspecified" title="${daoTitle}">${daoTitle}</span>
											</c:if>							
									</c:forEach>					
								</div>
								</c:if>	
							<div class="list-searchresult-context">
								<div class="fond">
									<span class="subtitle"><fmt:message key="advancedsearch.message.document" /></span><c:out value="${result.fond}"/>
								</div>
								<div>
								<div class="left">
									<div class="unitid">
										<c:choose>
											<c:when test="${empty result.unitid}">
												<c:if  test="${!empty result.unitIdOfFond}">
													<span class="subtitle"><fmt:message key="advancedsearch.message.eadid" /></span>${result.unitIdOfFond}
												</c:if>	
											</c:when>
											<c:otherwise>
												<span class="subtitle"><fmt:message key="advancedsearch.message.referencecode" /></span>${result.unitid}
												<c:if  test="${!empty result.otherUnitid}">
													${result.otherUnitid}
												</c:if>	
											</c:otherwise>
										</c:choose>		

																																
									</div>
									<div class="countryAndInstitution"><fmt:message key="country.${fn:toLowerCase(result.country)}" />&nbsp;-&nbsp;<c:out value="${result.ai}" /></div>
								</div>
								<div class="list-searchresult-actions hidden">
									<ul>
										<li><a href="javascript:addOnlyThisRefinement('fond','${result.fondId }','${result.escapedFond}','${result.escapedFond}')" title="<fmt:message key="advancedsearch.facet.document.only.help" />"><fmt:message key="advancedsearch.facet.document.only" /></a></li>
									</ul>
								</div>
								</div>
							</div>
													
						</div>
						<portlet:resourceURL var="displayPreviewUrl" id="displayPreview" >
							<portlet:param  name="id" value="${result.id}"/>
							<portlet:param  name="term" value="${eadSearch.encodedTerm}"/>
							<portlet:param  name="element" value="${eadSearch.element}"/>
						</portlet:resourceURL>
						<div class="preview-button-holder"
						data-url="${displayPreviewUrl}">&nbsp;</div>
					</div>
				</c:forEach>

			</div>
			<div id="bottom-paging" class="paging">
				<ape:paging numberOfItems="${results.totalNumberOfResults}" pageSize="${results.pageSize}" pageNumber="${eadSearch.pageNumber}"
					refreshUrl="javascript:updatePageNumber('');" pageNumberId="pageNumber"  maxNumberOfItems="${results.maxTotalNumberOfResults}"/>	
			</div>
			</c:if>
			</div>
			<div class="preview-column">
			<div id="search-preview" class="preview-container search-result-preview-container"></div>
			</div>
			</div>
		</c:if>
</div>