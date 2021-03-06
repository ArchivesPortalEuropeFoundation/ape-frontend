package eu.archivesportaleurope.portal.display.ead;

import java.util.List;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.web.portlet.ModelAndView;

import eu.apenet.commons.types.XmlType;
import eu.apenet.commons.utils.DisplayUtils;
import eu.apenet.persistence.dao.CLevelDAO;
import eu.apenet.persistence.vo.ArchivalInstitution;
import eu.apenet.persistence.vo.CLevel;
import eu.apenet.persistence.vo.Ead;
import eu.apenet.persistence.vo.Ead3;
import eu.apenet.persistence.vo.EadContent;
import eu.archivesportaleurope.portal.common.PortalDisplayUtil;
import eu.archivesportaleurope.portal.common.PropertiesKeys;
import eu.archivesportaleurope.portal.common.PropertiesUtil;
import eu.archivesportaleurope.portal.common.SpringResourceBundleSource;
import eu.archivesportaleurope.portal.display.ead.jsp.EadTag;

public class AbstractEadController {

    private static final int PAGE_SIZE = 10;
    protected static final String DISPLAY_EAD_CLEVEL_UNITID_NOTUNIQUE = "display.ead.clevel.unitid.notunique";
    protected static final String ERROR_USER_SECOND_DISPLAY_NOTEXIST = "error.user.second.display.notexist";
    protected static final String DISPLAY_EAD_CLEVEL_NOTFOUND = "display.ead.clevel.notfound";
    private CLevelDAO clevelDAO;
    private MessageSource messageSource;

    public void setClevelDAO(CLevelDAO clevelDAO) {
        this.clevelDAO = clevelDAO;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    protected CLevelDAO getClevelDAO() {
        return clevelDAO;
    }

    protected ModelAndView fillCDetails(CLevel currentCLevel, PortletRequest portletRequest, Integer pageNumber, ModelAndView modelAndView, boolean noscript) {
        Integer pageNumberInt = 1;
        if (pageNumber != null) {
            pageNumberInt = pageNumber;
        }
        modelAndView.getModelMap().addAttribute("type", EadTag.CDETAILS_XSLT);
        int orderId = (pageNumberInt - 1) * PAGE_SIZE;
        List<CLevel> children = clevelDAO.findChildCLevels(currentCLevel.getId(), orderId, PAGE_SIZE);
        Long totalNumberOfChildren = clevelDAO.countChildCLevels(currentCLevel.getId());
        if (noscript) {
            modelAndView.getModelMap().addAttribute("children", children);
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("<c xmlns=\"urn:isbn:1-931666-22-9\">");
            for (CLevel child : children) {
                builder.append(child.getXml());
            }
            builder.append("</c>");
            modelAndView.getModelMap().addAttribute("childXml", builder.toString());
        }
        modelAndView.getModelMap().addAttribute("totalNumberOfChildren", totalNumberOfChildren);
        modelAndView.getModelMap().addAttribute("pageNumber", pageNumberInt);
        modelAndView.getModelMap().addAttribute("pageSize", PAGE_SIZE);
        ArchivalInstitution archivalInstitution = currentCLevel.getEadContent().getEad().getArchivalInstitution();
        modelAndView.getModelMap().addAttribute("c", currentCLevel);

        SpringResourceBundleSource source = new SpringResourceBundleSource(this.getMessageSource(),
                portletRequest.getLocale());
        String localizedName = DisplayUtils.getLocalizedCountryName(source, archivalInstitution.getCountry());
        modelAndView.getModelMap().addAttribute("localizedCountryName", localizedName);
        String documentTitle = currentCLevel.getUnittitle();
        EadContent eadContent = currentCLevel.getEadContent();
        String pageTitle = PortalDisplayUtil.getEadDisplayTitle(eadContent.getEad(), getMessageSource().getMessage("advancedsearch.text.notitle", null, portletRequest.getLocale()));
        if (StringUtils.isNotBlank(documentTitle)) {
            pageTitle = PortalDisplayUtil.getEadDisplayPageTitle(eadContent.getEad(), documentTitle);
        }
        PortalDisplayUtil.setPageTitle(portletRequest, pageTitle);
        modelAndView.getModelMap().addAttribute("pageTitle", pageTitle);
        documentTitle = PortalDisplayUtil.getEadDisplayTitle(eadContent.getEad(), documentTitle);
        modelAndView.getModelMap().addAttribute("documentTitle", documentTitle);
        modelAndView.getModelMap().addAttribute("aiId", archivalInstitution.getAiId());
        modelAndView.getModelMap().addAttribute("archivalInstitution", archivalInstitution);
        modelAndView.getModelMap().addAttribute("eadContent", eadContent);
        XmlType xmlType = XmlType.getContentType(eadContent.getEad());
        modelAndView.getModelMap().addAttribute("xmlTypeName", xmlType.getResourceName());
        modelAndView.getModel().put("recaptchaAjaxUrl", PropertiesUtil.get(PropertiesKeys.APE_RECAPTCHA_AJAX_URL));
        modelAndView.getModelMap().addAttribute("recaptchaPubKey",
                PropertiesUtil.get(PropertiesKeys.LIFERAY_RECAPTCHA_PUB_KEY));
        return modelAndView;
    }

    protected ModelAndView fillEad3CDetails(CLevel currentCLevel, PortletRequest portletRequest, Integer pageNumber, ModelAndView modelAndView, boolean noscript) {
        Integer pageNumberInt = 1;
        if (pageNumber != null) {
            pageNumberInt = pageNumber;
        }
        modelAndView.getModelMap().addAttribute("type", EadTag.EAD3_CDETAILS_XSLT);
        int orderId = (pageNumberInt - 1) * PAGE_SIZE;
        List<CLevel> children = clevelDAO.findChildCLevels(currentCLevel.getId(), orderId, PAGE_SIZE);
        Long totalNumberOfChildren = clevelDAO.countChildCLevels(currentCLevel.getId());

        if (noscript) {
            modelAndView.getModelMap().addAttribute("children", children);
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("<c xmlns=\"urn:isbn:1-931666-22-9\">");
            for (CLevel child : children) {
                builder.append(child.getXml());
            }
            builder.append("</c>");
            modelAndView.getModelMap().addAttribute("childXml", builder.toString());
        }

        modelAndView.getModelMap().addAttribute("totalNumberOfChildren", totalNumberOfChildren);
        modelAndView.getModelMap().addAttribute("pageNumber", pageNumberInt);
        modelAndView.getModelMap().addAttribute("pageSize", PAGE_SIZE);
        ArchivalInstitution archivalInstitution = currentCLevel.getEad3().getArchivalInstitution();
        modelAndView.getModelMap().addAttribute("c", currentCLevel);
        SpringResourceBundleSource source = new SpringResourceBundleSource(this.getMessageSource(),
                portletRequest.getLocale());
        String localizedName = DisplayUtils.getLocalizedCountryName(source, archivalInstitution.getCountry());
        modelAndView.getModelMap().addAttribute("localizedCountryName", localizedName);
        String documentTitle = currentCLevel.getUnittitle();
        EadContent eadContent = currentCLevel.getEad3().getEadContent();
        String pageTitle = PortalDisplayUtil.getEad3DisplayTitle(eadContent.getEad3(), getMessageSource().getMessage("advancedsearch.text.notitle", null, portletRequest.getLocale()));
        if (StringUtils.isNotBlank(documentTitle)) {
            pageTitle = PortalDisplayUtil.getEad3DisplayPageTitle(eadContent.getEad3(), documentTitle);
        }
        PortalDisplayUtil.setPageTitle(portletRequest, pageTitle);
        modelAndView.getModelMap().addAttribute("pageTitle", pageTitle);
        documentTitle = PortalDisplayUtil.getEad3DisplayTitle(eadContent.getEad3(), documentTitle);
        modelAndView.getModelMap().addAttribute("documentTitle", documentTitle);
        modelAndView.getModelMap().addAttribute("aiId", archivalInstitution.getAiId());
        modelAndView.getModelMap().addAttribute("archivalInstitution", archivalInstitution);
        modelAndView.getModelMap().addAttribute("eadContent", eadContent);
        XmlType xmlType = XmlType.getContentType(eadContent.getEad3());
        if (modelAndView.getModelMap().containsAttribute("xmlTypeName")) {
            modelAndView.getModelMap().remove("xmlTypeName");
        }
        modelAndView.getModelMap().addAttribute("xmlTypeName", xmlType.getResourceName());
        modelAndView.getModel().put("recaptchaAjaxUrl", PropertiesUtil.get(PropertiesKeys.APE_RECAPTCHA_AJAX_URL));
        modelAndView.getModelMap().addAttribute("recaptchaPubKey",
                PropertiesUtil.get(PropertiesKeys.LIFERAY_RECAPTCHA_PUB_KEY));

        return modelAndView;
    }

    protected ModelAndView fillEadDetails(EadContent eadContent, PortletRequest portletRequest, Integer pageNumber, ModelAndView modelAndView, boolean noscript) {
        Ead ead = eadContent.getEad();
        modelAndView.getModelMap().addAttribute("type", EadTag.FRONTPAGE_XSLT);
        SpringResourceBundleSource source = new SpringResourceBundleSource(this.getMessageSource(),
                portletRequest.getLocale());
        ArchivalInstitution archivalInstitution = ead.getArchivalInstitution();
        String localizedName = DisplayUtils.getLocalizedCountryName(source, archivalInstitution.getCountry());
        modelAndView.getModelMap().addAttribute("localizedCountryName", localizedName);
        String documentTitle = eadContent.getUnittitle();
        String pageTitle = PortalDisplayUtil.getEadDisplayTitle(eadContent.getEad(), getMessageSource().getMessage("advancedsearch.text.notitle", null, portletRequest.getLocale()));
        if (StringUtils.isNotBlank(documentTitle)) {
            pageTitle = PortalDisplayUtil.getEadDisplayPageTitle(eadContent.getEad(), documentTitle);
        }
        PortalDisplayUtil.setPageTitle(portletRequest, pageTitle);
        modelAndView.getModelMap().addAttribute("pageTitle", pageTitle);
        documentTitle = PortalDisplayUtil.getEadDisplayTitle(ead, documentTitle);
        modelAndView.getModelMap().addAttribute("documentTitle", documentTitle);
        modelAndView.getModelMap().addAttribute("eadContent", eadContent);
        XmlType xmlType = XmlType.getContentType(ead);
        modelAndView.getModelMap().addAttribute("aiId", archivalInstitution.getAiId());
        modelAndView.getModelMap().addAttribute("archivalInstitution", archivalInstitution);
        modelAndView.getModelMap().addAttribute("xmlTypeName", xmlType.getResourceName());
        modelAndView.getModel().put("recaptchaAjaxUrl", PropertiesUtil.get(PropertiesKeys.APE_RECAPTCHA_AJAX_URL));
        modelAndView.getModelMap().addAttribute("recaptchaPubKey",
                PropertiesUtil.get(PropertiesKeys.LIFERAY_RECAPTCHA_PUB_KEY));
        if (noscript) {
            Integer pageNumberInt = 1;
            if (pageNumber != null) {
                pageNumberInt = pageNumber;
            }
            int orderId = (pageNumberInt - 1) * PAGE_SIZE;
            Long totalNumberOfChildren = getClevelDAO().countTopCLevels(eadContent.getEcId());
            List<CLevel> children = getClevelDAO().findTopCLevels(eadContent.getEcId(), orderId, PAGE_SIZE);
            modelAndView.getModelMap().addAttribute("totalNumberOfChildren", totalNumberOfChildren);
            modelAndView.getModelMap().addAttribute("pageNumber", pageNumberInt);
            modelAndView.getModelMap().addAttribute("pageSize", PAGE_SIZE);
            modelAndView.getModelMap().addAttribute("children", children);
        }

        return modelAndView;
    }

    protected ModelAndView fillEad3Details(Ead3 ead3, EadContent eadContent, PortletRequest portletRequest, Integer pageNumber, ModelAndView modelAndView, boolean noscript) {

        modelAndView.getModelMap().addAttribute("type", EadTag.EAD3_FRONTPAGE_XSLT);
        SpringResourceBundleSource source = new SpringResourceBundleSource(this.getMessageSource(),
                portletRequest.getLocale());
        ArchivalInstitution archivalInstitution = ead3.getArchivalInstitution();
        String localizedName = DisplayUtils.getLocalizedCountryName(source, archivalInstitution.getCountry());
        modelAndView.getModelMap().addAttribute("localizedCountryName", localizedName);
        String documentTitle = eadContent.getUnittitle();
        String pageTitle = PortalDisplayUtil.getEad3DisplayTitle(eadContent.getEad3(), getMessageSource().getMessage("advancedsearch.text.notitle", null, portletRequest.getLocale()));
        if (StringUtils.isNotBlank(documentTitle)) {
            pageTitle = PortalDisplayUtil.getEad3DisplayPageTitle(eadContent.getEad3(), documentTitle);
        }
        PortalDisplayUtil.setPageTitle(portletRequest, pageTitle);
        modelAndView.getModelMap().addAttribute("pageTitle", pageTitle);
        documentTitle = PortalDisplayUtil.getEad3DisplayTitle(ead3, documentTitle);
        modelAndView.getModelMap().addAttribute("documentTitle", documentTitle);
        modelAndView.getModelMap().addAttribute("eadContent", eadContent);
        XmlType xmlType = XmlType.getContentType(ead3);
        modelAndView.getModelMap().addAttribute("aiId", archivalInstitution.getAiId());
        modelAndView.getModelMap().addAttribute("archivalInstitution", archivalInstitution);
        if (modelAndView.getModelMap().containsAttribute("xmlTypeName")) {
            modelAndView.getModelMap().remove("xmlTypeName");
        }
        modelAndView.getModelMap().addAttribute("xmlTypeName", xmlType.getResourceName());
        modelAndView.getModel().put("recaptchaAjaxUrl", PropertiesUtil.get(PropertiesKeys.APE_RECAPTCHA_AJAX_URL));
        modelAndView.getModelMap().addAttribute("recaptchaPubKey",
                PropertiesUtil.get(PropertiesKeys.LIFERAY_RECAPTCHA_PUB_KEY));
        if (noscript) {
            Integer pageNumberInt = 1;
            if (pageNumber != null) {
                pageNumberInt = pageNumber;
            }
            int orderId = (pageNumberInt - 1) * PAGE_SIZE;
            Long totalNumberOfChildren = getClevelDAO().countTopCLevels(eadContent.getEcId());
            List<CLevel> children = getClevelDAO().findTopCLevels(eadContent.getEcId(), orderId, PAGE_SIZE);
            modelAndView.getModelMap().addAttribute("totalNumberOfChildren", totalNumberOfChildren);
            modelAndView.getModelMap().addAttribute("pageNumber", pageNumberInt);
            modelAndView.getModelMap().addAttribute("pageSize", PAGE_SIZE);
            modelAndView.getModelMap().addAttribute("children", children);
        }

        return modelAndView;
    }

}
