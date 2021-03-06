var autocompletionEagUrl, newEagSearchUrl;
function setUrls(necUrl, aUrl){
	autocompletionEagUrl = aUrl;
	newEagSearchUrl = necUrl;
}

function clearSearch(){
	commonClearSearch();
	$("#repositoryType").val("");
	$("#element").val("");
}

function init(){
	activateAutocompletion(autocompletionEagUrl, "#searchTerms", "eag");
	$("#searchTerms").focus();
	$("#searchTerms").keypress(function(event) {
		if (event.keyCode == 13) {
			$(this).data("autocomplete").destroy();
			activateAutocompletion(autocompletionEagUrl, "#searchTerms", "eag");
			performNewSearch();
		}
	});
	$("#searchButton").click(function(event) {
		event.preventDefault();
		performNewSearch();
	});	
	initCommon();
}


function performNewSearch() {
	var documentTitle = document.title;
	blockSearch();
	$("#mode").val("new-search");
	$.post(newEagSearchUrl, $("#newSearchForm").serialize(), function(data) {
		$("#tabs-list").html(data);
		updateSuggestions();
		updateSourceTabs();
		
		$("#searchResultsContainer").removeClass("hidden");
		//document.getElementById("searchResultsContainer").scrollIntoView(true);
	});
	logAction(documentTitle, newSearchUrl);
}

function updateCurrentSearchResults(addRemoveRefinement) {
	$("#searchResultsContainer").removeClass("hidden");
	blockSearch();
	$.post(newEagSearchUrl, $("#updateCurrentSearch").serialize(), function(data) {
		var refinementsHtml = $("#selectedRefinements > ul").html();
		// keep the selected refinements
		$("#tabs-list").html(data);
		$("#selectedRefinements > ul").html(refinementsHtml);
		if (addRemoveRefinement != undefined) {
			$("#selectedRefinements > ul").append(addRemoveRefinement);
		}
		document.getElementById("searchResultsContainer").scrollIntoView(true);
	});
	logAction(document.title + " (update-current-list-search)", newSearchUrl);
}