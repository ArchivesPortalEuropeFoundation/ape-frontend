package eu.archivesportaleurope.portal.search.ead;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ResourceRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Collation;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import eu.apenet.commons.solr.SolrField;
import eu.apenet.commons.solr.SolrFields;
import eu.apenet.persistence.vo.EadSavedSearch;
import eu.archivesportaleurope.portal.common.PortalDisplayUtil;
import eu.archivesportaleurope.portal.common.SpringResourceBundleSource;
import eu.archivesportaleurope.portal.search.common.AbstractSearchController;
import eu.archivesportaleurope.portal.search.common.SearchUtil;
import eu.archivesportaleurope.portal.search.common.FacetType;
import eu.archivesportaleurope.portal.search.common.ListResults;
import eu.archivesportaleurope.portal.search.common.Results;
import eu.archivesportaleurope.portal.search.common.SolrDocumentListHolder;
import eu.archivesportaleurope.portal.search.common.SolrQueryParameters;
import eu.archivesportaleurope.portal.search.ead.tree.ContextResults;
import eu.archivesportaleurope.portal.search.ead.tree.TreeFacetValue;
import eu.archivesportaleurope.portal.search.saved.SavedSearchService;
import eu.archivesportaleurope.util.ApeUtil;

@Controller(value = "eadSearchController")
@RequestMapping(value = "VIEW")
public class EadSearchController extends AbstractSearchController{


	private final static Logger LOGGER = Logger.getLogger(EadSearchController.class);


	private ResourceBundleMessageSource messageSource;
	private SavedSearchService savedSearchService;

	public void setSavedSearchService(SavedSearchService savedSearchService) {
		this.savedSearchService = savedSearchService;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	// --maps the incoming portlet request to this method
	@RenderMapping
	public String showeadSearch(RenderRequest request) {
		PortalDisplayUtil.setPageTitle(request, PortalDisplayUtil.TITLE_EAD_SEARCH);
		return "home";
	}

	@RenderMapping(params = "myaction=showSavedSearch")
	public ModelAndView showSavedSearch(RenderRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		PortalDisplayUtil.setPageTitle(request, PortalDisplayUtil.TITLE_EAD_SEARCH_SAVED);
		String errorMessage = null;
		try {
			String id = request.getParameter("savedSearchId");
			String publishedFromDate = request.getParameter("publishedFromDate");
			String publishedToDate = request.getParameter("publishedToDate");
			String showOnlyNew = request.getParameter("showOnlyNew");
			Long savedSearchId = Long.parseLong(id);
			Long liferayUserId = null;
			if (request.getUserPrincipal() != null) {
				liferayUserId = Long.parseLong(request.getUserPrincipal().toString());
			}
			EadSavedSearch eadSavedSearch = savedSearchService.getEadSavedSearch(liferayUserId, savedSearchId);
			if (eadSavedSearch.isPublicSearch()){
				PortalDisplayUtil.setPageTitle(request, PortalDisplayUtil.TITLE_EAD_SEARCH_PUBLIC_SAVED);
			}else {
				PortalDisplayUtil.setPageTitle(request, PortalDisplayUtil.TITLE_EAD_SEARCH_MY_SAVED);
			}
			if (eadSavedSearch != null) {
				EadSearch eadSearch = savedSearchService.convert(eadSavedSearch);
				if (StringUtils.isNotEmpty(publishedFromDate)
						&& !SearchUtil.isValidPublishedDate(publishedFromDate)) {
					errorMessage= "savedsearch.publisheddates.wrong";					

				}
				if (StringUtils.isNotEmpty(publishedToDate)
						&& !SearchUtil.isValidPublishedDate(publishedToDate)) {
					errorMessage= "savedsearch.publisheddates.wrong";					

				}
				if (errorMessage == null){
					if ("true".equals(showOnlyNew)){
						eadSearch.setPublishedFromDate(SearchUtil.getFullDateTimePublishedDate(eadSavedSearch.getModifiedDate()));
					}else {
						eadSearch.setPublishedFromDate(publishedFromDate);
						eadSearch.setPublishedToDate(publishedToDate);
					}
					if (eadSavedSearch.isTemplate()){
						eadSearch.setMode(EadSearch.MODE_NEW);
					}else{
						Results results = updateCurrentSearch(request, eadSearch);
						eadSearch.setMode(EadSearch.MODE_NEW_SEARCH);
						modelAndView.getModelMap().addAttribute("selectedRefinements", savedSearchService.convertToRefinements(request, eadSearch, eadSavedSearch));
						modelAndView.getModelMap().addAttribute("results", results);
					}
					modelAndView.getModelMap().addAttribute("eadSearch", eadSearch);
					
					modelAndView.setViewName("home");
					return modelAndView;
				}
			}
		} catch (Exception e) {

		}
		if (errorMessage == null){
			errorMessage = "savedsearch.notexist";
		}
		modelAndView.getModelMap().addAttribute("errorMessage", errorMessage);
		modelAndView.setViewName("savedsearch-error");
		return modelAndView;
	}

	@RenderMapping(params = "myaction=simpleSearch")
	public ModelAndView search(@ModelAttribute(value = "eadSearch") EadSearch eadSearch,
			RenderRequest request) {
		eadSearch.setMode(EadSearch.MODE_NEW_SEARCH);
		Results results = performNewSearch(request, eadSearch);
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("home");
		modelAndView.getModelMap().addAttribute("eadSearch", eadSearch);
		modelAndView.getModelMap().addAttribute("results", results);
		PortalDisplayUtil.setPageTitle(request, PortalDisplayUtil.TITLE_SIMPLE_SEARCH);
		return modelAndView;
	}

	@ResourceMapping(value = "advancedSearch")
	public ModelAndView searchAjax(@ModelAttribute(value = "eadSearch") EadSearch eadSearch,
			BindingResult bindingResult, ResourceRequest request) throws SolrServerException, ParseException {
		Results results = null;
		if (EadSearch.MODE_NEW_SEARCH.equalsIgnoreCase(eadSearch.getMode())) {
			results = performNewSearch(request, eadSearch);
		} else if (EadSearch.MODE_UPDATE_SEARCH.equalsIgnoreCase(eadSearch.getMode())) {
			results = updateCurrentSearch(request, eadSearch);

		}
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("results");
		modelAndView.getModelMap().addAttribute("eadSearch", eadSearch);
		modelAndView.getModelMap().addAttribute("results", results);
		return modelAndView;
	}

	@ModelAttribute("eadSearch")
	public EadSearch getCommandObject() {
		return new EadSearch();
	}

	public Results performNewSearch(PortletRequest request, EadSearch eadSearch) {
		Results results = null;
		try {
			String error = validate(eadSearch);
			if (error == null) {
				SolrQueryParameters solrQueryParameters = handleSearchParameters(request, eadSearch);
				if (EadSearch.VIEW_HIERARCHY.equals(eadSearch.getView())) {
					results = performNewSearchForContextView(request, solrQueryParameters, eadSearch);
				} else {
					results = performNewSearchForListView(request, solrQueryParameters, eadSearch);
				}
				boolean showSuggestions = false;
				if (results.getSpellCheckResponse() != null) {
					List<Collation> suggestions = results.getSpellCheckResponse().getCollatedResults();
					if (suggestions != null) {
						for (Collation collation : suggestions) {
							showSuggestions = showSuggestions
									|| (collation.getNumberOfHits() > results.getTotalNumberOfResults());
						}
					}
				}
				results.setShowSuggestions(showSuggestions);
				countOtherSearchResults(request, eadSearch, results);
			} else {
				if (EadSearch.VIEW_HIERARCHY.equals(eadSearch.getView())) {
					results = new ContextResults();
				} else {
					results = new ListResults();
				}
				results.setErrorMessage(error);
			}
			// request.setAttribute("results", results);

		} catch (Exception e) {
			LOGGER.error("There was an error during the execution of the advanced search: Error: " + ApeUtil.generateThrowableLog(e));
		}
		return results;
	}

	public Results updateCurrentSearch(PortletRequest request, EadSearch eadSearch) {
		Results results = null;
		try {
				if (EadSearch.VIEW_HIERARCHY.equals(eadSearch.getView())) {
					SolrQueryParameters solrQueryParameters = handleSearchParametersForContextUpdate(request, eadSearch);
					results = performNewSearchForContextView(request, solrQueryParameters, eadSearch);
				} else {
					SolrQueryParameters solrQueryParameters = handleSearchParametersForListUpdate(request, eadSearch);
					results = performUpdateSearchForListView(request, solrQueryParameters, eadSearch);
				}
		} catch (Exception e) {
			LOGGER.error("There was an error during the execution of the advanced search: Error: " + ApeUtil.generateThrowableLog(e));
		}
		return results;
	}

	protected ListResults performUpdateSearchForListView(PortletRequest request,
			SolrQueryParameters solrQueryParameters, EadSearch eadSearch) throws SolrServerException,
			ParseException {
		ListResults results = new ListResults();
		if (solrQueryParameters != null){
			results.setPageSize(Integer.parseInt(eadSearch.getResultsperpage()));
			Integer pageNumber = Integer.parseInt(eadSearch.getPageNumber());
			QueryResponse solrResponse = getEadSearcher().updateListView(solrQueryParameters, results.getPageSize()
					* (pageNumber - 1), results.getPageSize(), eadSearch.getFacetSettingsList(),
					eadSearch.getOrder(), eadSearch.getStartdate(), eadSearch.getEnddate());
			request.setAttribute("numberFormat", NumberFormat.getInstance(request.getLocale()));
			results.init(solrResponse, eadSearch.getFacetSettingsList(), eadSearch,
					new SpringResourceBundleSource(messageSource, request.getLocale()));
			updatePagination(results);
			if (results.getTotalNumberOfResults() > 0) {
				results.setItems(new SolrDocumentListHolder(solrResponse, true));
			} else {
				results.setItems(new SolrDocumentListHolder());
			}
		}
		return results;
	}

	protected ListResults performNewSearchForListView(PortletRequest request, SolrQueryParameters solrQueryParameters,
			EadSearch eadSearch) throws SolrServerException, ParseException {
		ListResults results = new ListResults();
		if (solrQueryParameters != null){
			results.setPageSize(Integer.parseInt(eadSearch.getResultsperpage()));
			QueryResponse solrResponse = getEadSearcher().performNewSearchForListView(solrQueryParameters, results.getPageSize(),
					eadSearch.getFacetSettingsList());
			request.setAttribute("numberFormat", NumberFormat.getInstance(request.getLocale()));
			results.init(solrResponse, eadSearch.getFacetSettingsList(), eadSearch,
					new SpringResourceBundleSource(messageSource, request.getLocale()));
			updatePagination(results);
			if (results.getTotalNumberOfResults() > 0) {
				results.setItems(new SolrDocumentListHolder(solrResponse, true));
			} else {
				results.setItems(new SolrDocumentListHolder());
			}
		}
		return results;
	}

	protected ContextResults performNewSearchForContextView(PortletRequest request,
			SolrQueryParameters solrQueryParameters, EadSearch eadSearch) throws SolrServerException {
		ContextResults results = new ContextResults();
		if (solrQueryParameters != null){
			QueryResponse solrResponse = getEadSearcher().performNewSearchForContextView(solrQueryParameters);
			NumberFormat numberFormat = NumberFormat.getInstance(request.getLocale());
			results.init(solrResponse, numberFormat);
			List<Count> countries = solrResponse.getFacetField(FacetType.COUNTRY.getName()).getValues();
			if (countries != null) {
				for (Count country : countries) {
					results.getCountries().add(
							new TreeFacetValue(country, TreeFacetValue.Type.COUNTRY, request.getLocale()));
				}
			}
		}
		return results;

	}

	protected SolrQueryParameters handleSearchParameters(PortletRequest portletRequest, EadSearch eadSearch) {
		SolrQueryParameters solrQueryParameters = getSolrQueryParametersByForm(eadSearch, portletRequest);
		if (solrQueryParameters != null){
			SearchUtil.setParameter(solrQueryParameters.getAndParameters(), SolrFields.TYPE,
					eadSearch.getTypedocument());
			SearchUtil.setFromDate(solrQueryParameters.getAndParameters(), eadSearch.getFromdate(),
					eadSearch.hasExactDateSearch());
			SearchUtil.setToDate(solrQueryParameters.getAndParameters(), eadSearch.getTodate(),
					eadSearch.hasExactDateSearch());
	
			SearchUtil.addSelectedNodesToQuery(eadSearch.getSelectedNodesList(), solrQueryParameters);
			SearchUtil.addPublishedDates(eadSearch.getPublishedFromDate(), eadSearch.getPublishedToDate(), solrQueryParameters);
			solrQueryParameters.setSolrFields(SolrField.getSolrFieldsByIdString(eadSearch.getElement()));
			SearchUtil.setParameter(solrQueryParameters.getAndParameters(), FacetType.DAO.getName(), eadSearch.getSimpleSearchDao());
		}
		return solrQueryParameters;
	}

	protected SolrQueryParameters handleSearchParametersForListUpdate(PortletRequest portletRequest, EadSearch eadSearch) {
		SolrQueryParameters solrQueryParameters = handleSearchParameters(portletRequest, eadSearch);
		SearchUtil.addRefinement(solrQueryParameters, FacetType.COUNTRY, eadSearch.getCountryList());
		SearchUtil.addRefinement(solrQueryParameters, FacetType.AI, eadSearch.getAiList());
		SearchUtil.addRefinement(solrQueryParameters, FacetType.TYPE, eadSearch.getTypeList());
		SearchUtil.addRefinement(solrQueryParameters, FacetType.DATE_TYPE, eadSearch.getDateTypeList());
		SearchUtil.addRefinement(solrQueryParameters, FacetType.DAO, eadSearch.getDaoList());
		SearchUtil.addRefinement(solrQueryParameters, FacetType.ROLEDAO, eadSearch.getRoledaoList());
		SearchUtil.addRefinement(solrQueryParameters, FacetType.FOND, eadSearch.getFondList());
		SearchUtil.addRefinement(solrQueryParameters, FacetType.LEVEL, eadSearch.getLevelList());
		return solrQueryParameters;
	}


	protected SolrQueryParameters handleSearchParametersForContextUpdate(PortletRequest portletRequest, EadSearch eadSearch) {
		return handleSearchParameters(portletRequest, eadSearch);

	}


	protected void countOtherSearchResults(PortletRequest request, 
			EadSearch eadSearch, Results results) throws SolrServerException, ParseException{
		SolrQueryParameters solrQueryParameters = getSolrQueryParametersByForm(eadSearch, request);
		if (solrQueryParameters != null){ 
			results.setEacCpfNumberOfResults(getEacCpfSearcher().getNumberOfResults(solrQueryParameters));
			results.setEadNumberOfResults(results.getTotalNumberOfResults());
			results.setEagNumberOfResults(getEagSearcher().getNumberOfResults(solrQueryParameters));
		}
	}
}