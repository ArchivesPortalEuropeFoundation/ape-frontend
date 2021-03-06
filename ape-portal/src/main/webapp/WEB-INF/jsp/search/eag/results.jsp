<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib prefix="portal" uri="http://portal.archivesportaleurope.eu/tags"%>
<%@ taglib prefix="ape" uri="http://commons.archivesportaleurope.eu/tags"%> 
<%@ taglib prefix="searchresults" uri="http://portal.archivesportaleurope.eu/tags/searchresults"%>
<%@ taglib prefix="facets" tagdir="/WEB-INF/tags/facets"%>
<portlet:defineObjects />
<portal:friendlyUrl var="directoryDisplayUrl" type="directory-institution-code"/>
<script type="text/javascript">
	$(document).ready(function() {
        initListTabHandlers();
    });
</script>
<c:if test="${results.showSuggestions}">
	<div class="hidden" id="NEWsuggestionSearch">
		<span class="suggestionText"> <c:choose>
				<c:when test="${results.totalNumberOfResults > 0}">
					<fmt:message key="advancedsearch.message.suggestion.results" />
				</c:when>
				<c:otherwise>
					<fmt:message key="advancedsearch.message.suggestion.noresults" />
				</c:otherwise>
			</c:choose>
		</span> <br />
		<portal:autosuggestion spellCheckResponse="${results.spellCheckResponse}" styleClass="suggestionLink"
			numberOfResultsStyleClass="suggestionNumberOfHits" misSpelledStyleClass="suggestionMisspelled" />
	</div>
</c:if>
<portal:sourceTabs results="${results}" ajax="true" type="eag"/>
<form:form id="updateCurrentSearch" name="eagSearchForm" commandName="eagSearch" method="post">
		<form:hidden id="updateCurrentSearch_term" path="term"/>
		<form:hidden id="updateCurrentSearch_method" path="method"/>
		<form:hidden id="updateCurrentSearch_mode" path="mode" value="update-search"/>
		<form:hidden id="updateCurrentSearch_country" path="country"/>		
		<form:hidden id="updateCurrentSearch_aiGroupsFacet" path="aiGroupsFacet"/>
		<form:hidden id="updateCurrentSearch_repositoryTypeFacet" path="repositoryTypeFacet"/>		
		<form:hidden id="updateCurrentSearch_repositoryType" path="repositoryType"/>	
		<form:hidden id="updateCurrentSearch_element" path="element"/>					
		<form:hidden id="updateCurrentSearch_facetSettings" path="facetSettings"/>
		<form:hidden id="updateCurrentSearch_order" path="order"/>
		

		<form:hidden id="updateCurrentSearch_pageNumber" path="pageNumber"/>
		<form:hidden id="updateCurrentSearch_publishedFromDate" path="publishedFromDate"/>
		<form:hidden id="updateCurrentSearch_publishedToDate" path="publishedToDate"/>
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
							<ape:pageDescription numberOfItems="${results.totalNumberOfResults}" pageSize="${results.pageSize}" pageNumber="${eagSearch.pageNumber}" numberFormat="${numberFormat}" />
							<c:if test="${results.partialResults}"><span class="partialresults">(<fmt:message key="search.message.approximately" />)</span></c:if>
						</div>
						<div id="resultPerPageContainer">
							<label for="updateCurrentSearch_resultsperpage" id="resultPerPageLabel" class="bold"><fmt:message key="advancedsearch.text.numberofresults"/></label>
							<form:select path="resultsperpage"  id="updateCurrentSearch_resultsperpage" items="${eagSearch.resultsperpageValues}"/>
						</div>						
						<div id="top-paging" class="paging">
						<ape:paging numberOfItems="${results.totalNumberOfResults}" pageSize="${results.pageSize}" pageNumber="${eagSearch.pageNumber}"
								refreshUrl="javascript:updatePageNumber('');" pageNumberId="pageNumber" maxNumberOfItems="${results.maxTotalNumberOfResults}"/>	
						</div>
				</c:when>
				<c:otherwise>
					<span id="noResults"><fmt:message key="search.message.notResults" /><c:if test="${results.partialResults}"><span class="partialresults"> (<fmt:message key="search.message.approximately" />)</span></c:if></span>
				</c:otherwise>
			</c:choose>	
			</div>
		</div>
		<c:if test="${empty results.errorMessage and (results.totalNumberOfResults > 0  or eagSearch.mode == 'update-search')}">
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
							<searchresults:order currentValue="${eagSearch.order}" value="relevancy" key="advancedsearch.order.relevancy" />
							|
							<searchresults:order currentValue="${eagSearch.order}" value="namesort" key="advancedsearch.eag.name" />	
						</c:otherwise>
					</c:choose>
				</div>
	
		<div id="searchresultsList">	
				<c:forEach var="result" items="${results.items}">
				<portal:generateSearchWords var="encodedTerm" term="${eagSearch.term}" element="${eagSearch.element}"/>
					<div class="list-searchresult" id="list-searchresult-${result.id}">
						<div class="list-searchresult-content list-searchresult-content-eag">
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
								<c:choose>
									<c:when test="${empty encodedTerm}">
										<c:set var="url" value="${directoryDisplayUrl}/${result.repositoryCode}"/>
									</c:when>
									<c:otherwise>
										<c:set var="url" value="${directoryDisplayUrl}/${result.repositoryCode}"/>
									</c:otherwise>
								</c:choose>	
								<a class="unittitle ${titleClass}" target="_blank" title="${titleWithoutHighlighting}"
									href="${url}">${title}
								</a>
																		
							</div>
						
							<c:if test="${!empty result.otherNames}"><div class="otherName">${result.otherNames}</div></c:if>
							<c:if test="${!empty result.repositories}"><div class="other"><span class="subtitle"><fmt:message key="advancedsearch.eag.repositories" />:</span><div class="subGroup">${result.repositories}</div></div></c:if>
							
							<c:if test="${!empty result.other}"><div class="other">${result.other}"</div></c:if>
							<div class="list-searchresult-context">
							<c:if test="${!empty result.address}"><div class="other"><span class="subtitle"><fmt:message key="eag2012.portal.visitorsaddress" />:</span>${result.address}</div></c:if>
							<c:if test="${!empty result.repositoryTypes}"><div class="other"><span class="subtitle"><fmt:message key="eag2012.portal.typeofarchive" />:</span>${result.repositoryTypes}</div></c:if>
							<div class="countryAndInstitution"><fmt:message key="country.${fn:toLowerCase(result.country)}" />${result.context}</div>
							</div>
						</div>
						<portlet:resourceURL var="displayPreviewUrl" id="displayPreview" >
							<portlet:param  name="repositoryCode" value="${result.repositoryCode}"/>
							<portlet:param  name="term" value="${encodedTerm}"/>
							<portlet:param  name="element" value="${eacCpfSearch.element}"/>
						</portlet:resourceURL>
						<div class="preview-button-holder"
						data-url="${displayPreviewUrl}">&nbsp;</div>
					</div>
					</c:forEach>
				</div>
				<div id="bottom-paging" class="paging">
					<ape:paging numberOfItems="${results.totalNumberOfResults}" pageSize="${results.pageSize}" pageNumber="${eagSearch.pageNumber}"
					refreshUrl="javascript:updatePageNumber('');" pageNumberId="pageNumber" maxNumberOfItems="${results.maxTotalNumberOfResults}"/>	
				</div>
			</div>
			<div class="preview-column">
			<div id="search-preview" class="preview-container search-result-preview-container"></div>
			</div>
</div>
</c:if>
</div>
</form:form>