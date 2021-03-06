package eu.archivesportaleurope.portal.search.eag;

import eu.apenet.commons.solr.SearchUtil;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ResourceRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Collation;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.RenderMapping;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import eu.apenet.commons.solr.SolrField;
import eu.apenet.commons.solr.SolrFields;
import eu.apenet.commons.solr.SolrQueryParameters;
import eu.apenet.commons.solr.SolrValues;
import eu.apenet.commons.solr.facet.FacetType;
import eu.apenet.commons.solr.facet.ListFacetSettings;
import eu.archivesportaleurope.portal.common.PortalDisplayUtil;
import eu.archivesportaleurope.portal.common.SpringResourceBundleSource;
import eu.archivesportaleurope.portal.search.common.AbstractSearchController;
import eu.archivesportaleurope.portal.search.common.AbstractSearchForm;
import eu.archivesportaleurope.portal.search.common.ListResults;
import eu.archivesportaleurope.portal.search.common.Results;
import eu.archivesportaleurope.portal.search.common.SolrDocumentListHolder;
import java.io.IOException;

/**
 * 
 * This is display eag controller
 * 
 * @author bverhoef
 * 
 */
@Controller(value = "searchEagContoller")
@RequestMapping(value = "VIEW")
public class EagSearchContoller extends AbstractSearchController {
	private final static Logger LOGGER = Logger.getLogger(EagSearchContoller.class);
	private MessageSource messageSource;

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@RenderMapping
	public ModelAndView searchEag(RenderRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		PortalDisplayUtil.setPageTitle(request, PortalDisplayUtil.TITLE_EAG_SEARCH);
		modelAndView.setViewName("index");
		return modelAndView;
	}

	@RenderMapping(params = "myaction=simpleSearch")
	public ModelAndView searchSimple(@ModelAttribute(value = "eagSearch") EagSearch eagSearch, RenderRequest request)
			throws SolrServerException, ParseException {
		ModelAndView modelAndView = new ModelAndView();
		eagSearch.setMode(EagSearch.MODE_NEW_SEARCH);
		modelAndView.setViewName("index");
		modelAndView.getModelMap().addAttribute("eagSearch", eagSearch);
		if (StringUtils.isNotBlank(eagSearch.getTerm())) {
			ListResults results = performNewSearch(request, eagSearch);
			modelAndView.getModelMap().addAttribute("results", results);
		}
		PortalDisplayUtil.setPageTitle(request, PortalDisplayUtil.TITLE_EAG_SEARCH);
		return modelAndView;
	}

	@RenderMapping(params = "myaction=eagSearch")
	public ModelAndView search(@ModelAttribute(value = "eagSearch") EagSearch eagSearch, RenderRequest request)
			throws SolrServerException, ParseException {
		ModelAndView modelAndView = new ModelAndView();
		if (StringUtils.isNotBlank(eagSearch.getTerm())) {
			ListResults results = performNewSearch(request, eagSearch);
			modelAndView.getModelMap().addAttribute("results", results);
		}
		PortalDisplayUtil.setPageTitle(request, PortalDisplayUtil.TITLE_EAG_SEARCH);
		modelAndView.setViewName("index");

		return modelAndView;
	}

	@ResourceMapping(value = "eagSearch")
	public ModelAndView searchAjax(@ModelAttribute(value = "eagSearch") EagSearch eagSearch,
			BindingResult bindingResult, ResourceRequest request) throws SolrServerException, ParseException {
		Results results = null;
		if (AbstractSearchForm.MODE_NEW_SEARCH.equalsIgnoreCase(eagSearch.getMode())) {
			results = performNewSearch(request, eagSearch);
		} else if (AbstractSearchForm.MODE_UPDATE_SEARCH.equalsIgnoreCase(eagSearch.getMode())) {
			results = updateCurrentSearch(request, eagSearch);

		}
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("results");
		modelAndView.getModelMap().addAttribute("eagSearch", eagSearch);
		modelAndView.getModelMap().addAttribute("results", results);
		return modelAndView;
	}

	public ListResults performNewSearch(PortletRequest request, EagSearch eagSearch) {
		ListResults results = null;
		try {
			String error = validate(eagSearch, request);
			if (error == null) {
				SolrQueryParameters solrQueryParameters = handleSearchParameters(request, eagSearch);
				results = performNewSearchForListView(request, solrQueryParameters, eagSearch);
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
				countOtherSearchResults(request, eagSearch, results);
			} else {
				results = new ListResults();
				results.setErrorMessage(error);
			}
			// request.setAttribute("results", results);

		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			results = new ListResults();
			results.setErrorMessage( "search.message.internalerror");			
		}
		return results;
	}

	public Results updateCurrentSearch(PortletRequest request, EagSearch eagSearch) {
		Results results = null;
		try {
			SolrQueryParameters solrQueryParameters = handleSearchParametersForListUpdate(request, eagSearch);
			results = performUpdateSearchForListView(request, solrQueryParameters, eagSearch);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return results;
	}

	protected ListResults performNewSearchForListView(PortletRequest request, SolrQueryParameters solrQueryParameters,
			EagSearch eagSearch) throws SolrServerException, ParseException, IOException {
		ListResults results = new ListResults();
		if (solrQueryParameters != null) {
			results.setPageSize(Integer.parseInt(eagSearch.getResultsperpage()));
			List<ListFacetSettings> list = eagSearch.getFacetSettingsList();
			QueryResponse solrResponse = getEagSearcher().performNewSearchForListView(solrQueryParameters,
					results.getPageSize(), list);
			request.setAttribute("numberFormat", NumberFormat.getInstance(request.getLocale()));
			SpringResourceBundleSource springResourceBundleSource = new SpringResourceBundleSource(messageSource,
					request.getLocale());
			results.init(solrResponse, list, eagSearch, springResourceBundleSource);
			updatePagination(results);
			if (results.getTotalNumberOfResults() > 0) {
				results.setItems(new SolrDocumentListHolder(solrResponse, EagSearchResult.class,
						springResourceBundleSource));
			} else {
				results.setItems(new SolrDocumentListHolder());
			}

		}
		return results;
	}

	protected ListResults performUpdateSearchForListView(PortletRequest request,
			SolrQueryParameters solrQueryParameters, EagSearch eagSearch) throws SolrServerException, ParseException, IOException {
		ListResults results = new ListResults();
		if (solrQueryParameters != null) {
			results.setPageSize(Integer.parseInt(eagSearch.getResultsperpage()));
			Integer pageNumber = Integer.parseInt(eagSearch.getPageNumber());
			QueryResponse solrResponse = getEagSearcher().updateListView(solrQueryParameters,
					results.getPageSize() * (pageNumber - 1), results.getPageSize(), eagSearch.getFacetSettingsList(),
					eagSearch.getOrder(), eagSearch.getStartDate(), eagSearch.getEndDate());
			request.setAttribute("numberFormat", NumberFormat.getInstance(request.getLocale()));
			SpringResourceBundleSource springResourceBundleSource = new SpringResourceBundleSource(messageSource,
					request.getLocale());
			results.init(solrResponse, eagSearch.getFacetSettingsList(), eagSearch, springResourceBundleSource);
			updatePagination(results);
			if (results.getTotalNumberOfResults() > 0) {
				results.setItems(new SolrDocumentListHolder(solrResponse, EagSearchResult.class,
						springResourceBundleSource));
			} else {
				results.setItems(new SolrDocumentListHolder());
			}
		}
		return results;
	}

	protected SolrQueryParameters handleSearchParameters(PortletRequest portletRequest, EagSearch eagSearch) {
		SolrQueryParameters solrQueryParameters = getSolrQueryParametersByForm(eagSearch, portletRequest);
		if (solrQueryParameters != null) {
			SearchUtil.setParameter(solrQueryParameters.getAndParameters(), SolrFields.EAG_REPOSITORY_TYPE,
					eagSearch.getRepositoryType());
			if (SolrField.EAG_NAME.toString().equals(eagSearch.getElement())
					|| SolrField.EAG_PLACES.toString().equals(eagSearch.getElement())) {
				List<SolrField> solrFields = new ArrayList<SolrField>();
				if (SolrField.EAG_NAME.toString().equals(eagSearch.getElement())) {
					solrFields.add(SolrField.EAG_NAME);
					solrFields.add(SolrField.EAG_OTHER_NAMES);
				} else if (SolrField.EAG_PLACES.toString().equals(eagSearch.getElement())) {
					solrFields.add(SolrField.EAG_PLACES);
					solrFields.add(SolrField.EAG_ADDRESS);
				}
				solrQueryParameters.setSolrFields(solrFields);
			}

		}
		return solrQueryParameters;
	}

	protected SolrQueryParameters handleSearchParametersForListUpdate(PortletRequest portletRequest, EagSearch eagSearch) {
		SolrQueryParameters solrQueryParameters = handleSearchParameters(portletRequest, eagSearch);
		if (solrQueryParameters != null) {
			SearchUtil.addRefinement(solrQueryParameters, FacetType.COUNTRY, eagSearch.getCountryList());
			SearchUtil.addRefinement(solrQueryParameters, FacetType.EAG_AI_GROUPS, eagSearch.getAiGroupsFacetList());
			SearchUtil.addRefinement(solrQueryParameters, FacetType.EAG_REPOSITORY_TYPE,
					eagSearch.getRepositoryTypeFacetList());
		}
		return solrQueryParameters;
	}

	@ModelAttribute("eagSearch")
	public EagSearch getCommandObject(PortletRequest portletRequest) {
		SpringResourceBundleSource source = new SpringResourceBundleSource(messageSource, portletRequest.getLocale());
		EagSearch eagSearch = new EagSearch();
		eagSearch.getRepositoryTypeValues().put("", source.getString("advancedsearch.text.noselection"));
		for (String type : SolrValues.EAG_REPOSITORY_TYPES) {
			eagSearch.getRepositoryTypeValues().put(type, source.getString("eag2012.options.institutionType." + type));
		}
		eagSearch.getElementValues().put("", source.getString("advancedsearch.text.noselection"));
		eagSearch.getElementValues().put(SolrField.EAG_NAME.toString(),
				source.getString("advancedsearch.eag.name"));
		eagSearch.getElementValues().put(SolrField.EAG_PLACES.toString(),
				source.getString("advancedsearch.eag.place"));
		return eagSearch;
	}

	protected void countOtherSearchResults(PortletRequest request, EagSearch eagSearch, Results results)
			throws SolrServerException, ParseException, IOException {
		SolrQueryParameters solrQueryParameters = getSolrQueryParametersByForm(eagSearch, request);
		if (solrQueryParameters != null && !eagSearch.isAdvancedSearch()) {
			results.setEacCpfNumberOfResults(getEacCpfSearcher().getNumberOfResults(solrQueryParameters));
			results.setEadNumberOfResults(getEadSearcher().getNumberOfResults(solrQueryParameters));
			results.setEagNumberOfResults(results.getTotalNumberOfResults());
		}
	}
}
