package eu.archivesportaleurope.portal.tagcloud;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.portlet.RenderRequest;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.RenderMapping;

import eu.apenet.persistence.dao.TopicDAO;
import eu.apenet.persistence.vo.Topic;
import eu.archivesportaleurope.portal.common.SpringResourceBundleSource;
import eu.archivesportaleurope.portal.search.common.FacetType;
import eu.archivesportaleurope.portal.search.common.SolrQueryParameters;
import eu.archivesportaleurope.portal.search.ead.EadSearcher;
import eu.archivesportaleurope.portal.search.ead.list.ListFacetSettings;

@Controller(value = "tagCloudController")
@RequestMapping(value = "VIEW")
public class TagCloudController {

	private static final String TAGS_KEY = "tags";
	private static final int NUMBER_OF_GROUPS = 5;
	private final static Logger LOGGER = Logger.getLogger(TagCloudController.class);
	private final static int MAX_NUMBER_OF_TAGS = 15;
	//private final static Cache<String, List<TagCloudItem>> CACHE = CacheManager.getInstance().<String, List<TagCloudItem>>initCache("topicCache");
	private TopicDAO topicDAO;
	
	private ResourceBundleMessageSource messageSource;
	private EadSearcher eadSearcher;

	public void setEadSearcher(EadSearcher eadSearcher) {
		this.eadSearcher = eadSearcher;
	}

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}


	public void setTopicDAO(TopicDAO topicDAO) {
		this.topicDAO = topicDAO;
	}

	@RenderMapping
	public String showTagCloud(RenderRequest request) {
		List<TagCloudItem> tags = null;
		if (tags == null){
			tags= new ArrayList<TagCloudItem>();
			SolrQueryParameters solrQueryParameters = new SolrQueryParameters();
			solrQueryParameters.setTerm("*");
			List<ListFacetSettings> facetSettings = new ArrayList<ListFacetSettings>();
			facetSettings.add(new ListFacetSettings(FacetType.TOPIC, true, null, MAX_NUMBER_OF_TAGS));
	
			try {
				QueryResponse response = eadSearcher.performNewSearchForListView(solrQueryParameters, 0, facetSettings);
				FacetField facetField = response.getFacetFields().get(0);
				for (Count count : facetField.getValues()) {
					tags.add(new TagCloudItem(count.getCount(), count.getName()));
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage());
			}
			int numberOfTagsWithResults = tags.size();
			List<Topic> topics = topicDAO.getFirstTopics();
			for (int i = 0; i < topics.size() && tags.size() < MAX_NUMBER_OF_TAGS; i++) {
				TagCloudItem tagCloudItem = new TagCloudItem(0l, topics.get(i).getPropertyKey());
				if (!tags.contains(tagCloudItem)) {
					tags.add(tagCloudItem);
				}
			}
			int[] groups = numberPerGroup(numberOfTagsWithResults, tags.size() - numberOfTagsWithResults);
			int tagCloudItemIndex = 0;
			for (int i = 0; i < NUMBER_OF_GROUPS; i++) {
				int maxNumber = groups[i];
				for (int j = 0; j < maxNumber; j++) {
					tags.get(tagCloudItemIndex).setTagNumber((i + 1));
					tagCloudItemIndex++;
				}
			}
			//CACHE.put(TAGS_KEY, tags);
		}
		List<TagCloudItem> translatedTags = new ArrayList<TagCloudItem>();
		SpringResourceBundleSource source = new SpringResourceBundleSource(messageSource, request.getLocale());
		for (TagCloudItem notTranslatedItem : tags){
			String translatedName = source.getString("topic." + notTranslatedItem.getKey());
			translatedTags.add(new TagCloudItem(notTranslatedItem, translatedName));
		}
		Collections.sort(translatedTags, new TagCloudComparator());
		request.setAttribute("url", "/search/-/s/n/topic/");
		request.setAttribute(TAGS_KEY, translatedTags);
		return "index";
	}

	public static int[] numberPerGroup(int numberOfItemsWithResults, int numberOfItemsWithoutResults) {
		int numberOfGroups = NUMBER_OF_GROUPS;
		int numberOfItems = numberOfItemsWithResults + numberOfItemsWithoutResults;
		int[] groups = new int[numberOfGroups];
		int extra = numberOfItems % numberOfGroups;
		int minimalNumber = numberOfItems / numberOfGroups;
		for (int i = 0; i < groups.length; i++) {
			if (extra > 0) {
				groups[i] = minimalNumber + 1;
			} else {
				groups[i] = minimalNumber;
			}
			extra--;
		}
		if (numberOfItemsWithResults > 0 && numberOfItemsWithoutResults > 0) {
			int value = groups[0] + groups[1];
			boolean first = true;
			if (numberOfItemsWithResults <= value && value > 2) {
				while (numberOfItemsWithResults <= value && value > 2) {
					if (first && groups[0] > 1) {
						groups[0] = groups[0] - 1;
						groups[4] = groups[4] + 1;
					} else if (!first && groups[1] > 1) {
						groups[1] = groups[1] - 1;
						groups[3] = groups[3] + 1;
					}
					first = !first;
					value = groups[0] + groups[1];
				}
			}

		}
		return groups;
	}

	private static class TagCloudComparator implements Comparator<TagCloudItem> {

		@Override
		public int compare(TagCloudItem o1, TagCloudItem o2) {
			return o1.getName().compareTo(o2.getName());
		}

	}
}
