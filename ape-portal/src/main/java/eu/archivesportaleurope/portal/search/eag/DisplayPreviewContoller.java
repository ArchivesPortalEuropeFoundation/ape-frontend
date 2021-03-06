package eu.archivesportaleurope.portal.search.eag;

import java.io.IOException;
import java.util.List;

import javax.portlet.ResourceRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

import eu.apenet.commons.utils.APEnetUtilities;
import eu.apenet.persistence.dao.ArchivalInstitutionDAO;
import eu.apenet.persistence.dao.CoordinatesDAO;
import eu.apenet.persistence.vo.ArchivalInstitution;
import eu.apenet.persistence.vo.Coordinates;
import eu.archivesportaleurope.portal.common.NotExistInDatabaseException;

/**
 *
 * This is preview ead controller
 *
 * @author bverhoef
 *
 */
@Controller(value = "displayPreviewController")
@RequestMapping(value = "VIEW")
public class DisplayPreviewContoller {

	private final static Logger LOGGER = Logger.getLogger(DisplayPreviewContoller.class);

	private ArchivalInstitutionDAO archivalInstitutionDAO;
	private CoordinatesDAO coordinatesDAO;

	/***
	 * This function shows a static map to preview the eag file
	 * 
	 * @param resourceRequest {@link ResourceRequest}
	 * 
	 * @return modelAndView {@link ModelAndView}
	 */
	@ResourceMapping(value = "displayPreview")
	public ModelAndView displayPreview(ResourceRequest resourceRequest) {
		String repositoryCode = resourceRequest.getParameter("repositoryCode");
		try {
			if (StringUtils.isNotBlank(repositoryCode)) {
				return fillAiDetails(repositoryCode);
			}else {
				throw new NotExistInDatabaseException();
			}
		}catch (NotExistInDatabaseException e){
			ModelAndView modelAndView = new ModelAndView();
			modelAndView.getModelMap().addAttribute("errorMessage", "error.user.second.display.notexist");
			modelAndView.setViewName("preview/indexError");
			return modelAndView;
		}catch (Exception e) {
			LOGGER.error(e.getMessage(),e);
		}

		return null;
	}

	/***
	 * This function gets the code of the repository and shows the preview of the file
	 * 
	 * @param repositoryCode {@link String}
	 * @return modelAndView {@link ModelAndView}
	 * 
	 * @throws IOException
	 * @throws NotExistInDatabaseException
	 */
	private ModelAndView fillAiDetails(String repositoryCode) throws IOException, NotExistInDatabaseException {
		ModelAndView modelAndView = new ModelAndView();
		
		modelAndView.setViewName("preview/aidetails");
		ArchivalInstitution archivalInstitution = archivalInstitutionDAO.getArchivalInstitutionByRepositoryCode(repositoryCode);
		if (archivalInstitution == null) {
			throw new NotExistInDatabaseException();
		}
		List<Coordinates> coordinates = coordinatesDAO.findCoordinatesByArchivalInstitution(archivalInstitution);
		String eagPath = APEnetUtilities.getApePortalConfig().getRepoDirPath() + archivalInstitution.getEagPath();
		modelAndView.getModelMap().addAttribute("ai", archivalInstitution);
		if (coordinates.size() > 0){
			modelAndView.getModelMap().addAttribute("coordinates", coordinates.get(0));
		}
		modelAndView.getModelMap().addAttribute("eagUrl", eagPath);
		return modelAndView;
	}

	public void setArchivalInstitutionDAO(ArchivalInstitutionDAO archivalInstitutionDAO) {
		this.archivalInstitutionDAO = archivalInstitutionDAO;
	}

	public void setCoordinatesDAO(CoordinatesDAO coordinatesDAO) {
		this.coordinatesDAO = coordinatesDAO;
	}




}
