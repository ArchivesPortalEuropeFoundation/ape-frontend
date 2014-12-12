package eu.archivesportaleurope.portal.common;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.liferay.portal.util.PortalUtil;

import eu.apenet.persistence.vo.ArchivalInstitution;
import eu.apenet.persistence.vo.Country;
import eu.apenet.persistence.vo.EacCpf;
import eu.apenet.persistence.vo.Ead;

public class PortalDisplayUtil {
    private static final String END_CHARACTER = ")";
	private static final String START_CHARACTER = " (";
	private static final Logger LOGGER = Logger.getLogger(PortalDisplayUtil.class);
    public static final String TITLE_HOME = "HOME";
    public static final String TITLE_DIRECTORY = "DIRECTORY";
    public static final String TITLE_EAD_SEARCH = "ARCHIVES SEARCH";
    public static final String TITLE_EAD_SEARCH_SAVED = "ARCHIVES SEARCH (SAVED SEARCH)";
    public static final String TITLE_EAD_SEARCH_PUBLIC_SAVED = "ARCHIVES SEARCH (PUBLIC SAVED SEARCH)";
    public static final String TITLE_EAD_SEARCH_MY_SAVED = "ARCHIVES SEARCH (MY SAVED SEARCH)";
    public static final String TITLE_SIMPLE_SEARCH = "ARCHIVES SEARCH";
    public static final String TITLE_FEATURED_DOCUMENT = "FEATURED DOCUMENTS";
    public static final String TITLE_SAVED_SEARCH = "SAVED SEARCHES";
    public static final String TITLE_EAC_CPF_SEARCH = "NAME SEARCH";
    public static final String TITLE_EAG_SEARCH = "INSTITUTION SEARCH";
	public static final String TITLE_SAVED_BOOKMARK = "SAVE BOOKMARK";
	public static final String TITLE_SAVED_COLLECTIONS = "SAVED COLLECTIONS";
    public static final String TITLE_TOPICS = "SHOW ALL TOPICS";
    /**
     * Method to replace whitespaces, carriage returns and all the problematic
     * characters.
     *
     * @param string String to be cleaned.
     * @return Cleaned string.
     */
	public static String replaceQuotesAndReturns(String string) {
		String result = string;
		if (result != null) {
			result = result.replaceAll("[/]", "");
			result = replaceQuotesAndReturnsForTree(result);
		}
		return result;
	}

	/** 
     * Method to replace whitespaces, carriage returns and all the problematic
     * characters except the slash "/".
     *
     * This method should only be directly called from the tree constructors
     * classes.
     *
     * @param string String to be cleaned.
     * @return Cleaned string.
	 */
	public static String replaceQuotesAndReturnsForTree(String string) {
		String result = string;
		if (result != null) {
			result = result.replaceAll("\"", "'");
			result = result.replaceAll("[\n\t\r\\\\%;]", "");
			result = result.trim();
		}
		return result;
	}
	
	public static String replaceSingleQuotes(String quotedText){
		if(quotedText!=null){
			if(quotedText.contains("'")){
				quotedText = quotedText.replace("'","&#x27;");
			}
		}
		return quotedText;
	}
	
	public static String replaceHTMLSingleQuotes(String quotedText){
		if(quotedText!=null){
			if(quotedText.contains("&#x27;")){
				quotedText = quotedText.replace("&#x27;","'");
			}
		}
		return quotedText;
	}

	public static String replaceLessThan(String string){
		if (string != null){
			return string.replaceAll("<", "&lt;");
		}else {
			return null;
		}
	}
	public static ReadableUserAgent getUserAgent(PortletRequest portletRequest){
		HttpServletRequest request = PortalUtil.getHttpServletRequest(portletRequest);
		String header = request.getHeader("User-Agent");
		if (StringUtils.isBlank(header)){
			return null;
		}else {
			return CachedUserAgentStringParser.getInstance().parse(header);
		}
	}
	public static boolean isNotDesktopBrowser(PortletRequest portletRequest){
		ReadableUserAgent agent = getUserAgent(portletRequest);
		return agent == null || !UserAgentType.BROWSER.equals(agent.getType());
	}
	public static boolean isNotNormalBrowser(PortletRequest portletRequest){
		ReadableUserAgent agent = getUserAgent(portletRequest);
		return agent == null || !(UserAgentType.BROWSER.equals(agent.getType()) || UserAgentType.MOBILE_BROWSER.equals(agent.getType()));
	}
	public static void setPageTitle(PortletRequest portletRequest, String title){
		String documentTitle = PortalDisplayUtil.replaceQuotesAndReturns(title);
		HttpServletRequest request = PortalUtil. getHttpServletRequest(portletRequest);
		PortalUtil.setPageTitle(documentTitle, request);
	}
	public static String getFeaturedExhibitionTitle(String title){
		if (StringUtils.isBlank(title)){
			return TITLE_FEATURED_DOCUMENT;
		}else {
			return PortalDisplayUtil.replaceQuotesAndReturns( title + START_CHARACTER + TITLE_FEATURED_DOCUMENT + END_CHARACTER);
		}
	}
	public static String getEacCpfDisplayPageTitle(EacCpf eacCpf){
		return PortalDisplayUtil.replaceQuotesAndReturns( eacCpf.getTitle() + START_CHARACTER + eacCpf.getArchivalInstitution().getRepositorycode() + " - " + eacCpf.getIdentifier() + END_CHARACTER);
	}
	public static String getEacCpfDisplayTitle(EacCpf eacCpf){
		return PortalDisplayUtil.replaceSingleQuotes(PortalDisplayUtil.replaceQuotesAndReturns( eacCpf.getTitle() + START_CHARACTER + eacCpf.getArchivalInstitution().getRepositorycode() + " - " + eacCpf.getIdentifier() + END_CHARACTER));
	}
	public static String getEadDisplayPageTitle(Ead ead, String title){
		return PortalDisplayUtil.replaceQuotesAndReturns( title + START_CHARACTER + ead.getArchivalInstitution().getRepositorycode() + " - " + ead.getEadid() + END_CHARACTER);
	}
	public static String getEadDisplayTitle(Ead ead, String title){
		return PortalDisplayUtil.replaceSingleQuotes(PortalDisplayUtil.replaceQuotesAndReturns( title + START_CHARACTER + ead.getArchivalInstitution().getRepositorycode() + " - " + ead.getEadid() + END_CHARACTER));
	}
	public static String getArchivalInstitutionDisplayTitle(ArchivalInstitution institution){
		if (institution.isGroup()){
			return PortalDisplayUtil.replaceQuotesAndReturns(institution.getAiname() + START_CHARACTER + institution.getCountry().getIsoname() + END_CHARACTER);
		}else {
			return PortalDisplayUtil.replaceQuotesAndReturns(institution.getAiname() + START_CHARACTER + institution.getRepositorycode() + END_CHARACTER);
		}
		
	}
	public static String getCountryDisplayTitle(Country country){
		return PortalDisplayUtil.replaceQuotesAndReturns(country.getCname() + START_CHARACTER + country.getIsoname() + END_CHARACTER);
	}
}
