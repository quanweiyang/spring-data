package com.nixmash.springdata.mvc.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import com.nixmash.springdata.jpa.common.ApplicationSettings;
import com.nixmash.springdata.jpa.exceptions.ContactNotFoundException;
import com.nixmash.springdata.jpa.exceptions.UnknownResourceException;
import com.nixmash.springdata.jpa.model.CurrentUser;
import com.nixmash.springdata.jpa.model.UserConnection;
import com.nixmash.springdata.mvc.common.WebUI;
import com.nixmash.springdata.solr.exceptions.GeoLocationException;


@ControllerAdvice
public class GlobalController {

	private static final Logger logger = LoggerFactory.getLogger(GlobalController.class);

	protected static final String ERROR_404_VIEW = "errors/404";
	protected static final String ERROR_GENERIC_VIEW = "errors/generic";

	private static final String PRODUCT_MAP_VIEW = "products/map";
	private static final String LOCATION_ERROR_MESSAGE_KEY = "product.map.page.feedback.error";
	public static final String LOCATION_ERROR_ATTRIBUTE = "mappingError";
	public static final String SESSION_ATTRIBUTE_USER_CONNECTION = "MY_USER_CONNECTION";

	@Autowired
	WebUI webUI;

	@Autowired
	private ApplicationSettings applicationSettings;

	@ModelAttribute("currentUser")
	public CurrentUser getCurrentUser(Authentication authentication) {
		CurrentUser currentUser = null;
		if (authentication == null)
			return null;
		else {
			currentUser = (CurrentUser) authentication.getPrincipal();
			
			// Old approach follows. 
			//
			// 	Since Spring Security retains authentication object, 
			// 	which I store as CurrentUser object, CurrentUser is always available as Principal
			//
			// 	currentUser = userDetailsService.loadUserByUsername(authentication.getName());
		}
		return currentUser;
		
	}

	@ModelAttribute("currentUserConnection")
	public UserConnection getUserConnection(WebRequest request)
	{
		return  (UserConnection) request.getAttribute(SESSION_ATTRIBUTE_USER_CONNECTION, RequestAttributes.SCOPE_SESSION);
	}
	
//	@ModelAttribute("currentUserConnection")
//    public UserConnection getUserConnection(HttpServletRequest request, Principal currentUser, Model model) {
//
//        // SecurityContext ctx = (SecurityContext) session.getAttribute("SPRING_SECURITY_CONTEXT");
//
//        String userId = currentUser == null ? null : currentUser.getName();
//        String path = request.getRequestURI();
//        HttpSession session = request.getSession();
//
//        UserConnection connection = null;
//        UserProfile profile = null;
//        String displayName = null;
//        String data = null;
//
//        // Collect info if the user is logged in, i.e. userId is set
//        if (userId != null) {
//
//            // Get the current UserConnection from the http session
//            connection = getUserConnection(session, userId);
//
//            // Get the current UserProfile from the http session
//            profile = getUserProfile(session, userId);
//
//            // Compile the best display name from the connection and the profile
//            displayName = getDisplayName(connection, profile);
//
//            // Get user data from persistence storage
//            data = dataDao.getData(userId);
//        }
//
//        Throwable exception = (Throwable)session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
//
//        // Update the model with the information we collected
//        model.addAttribute("exception",              exception == null ? null : exception.getMessage());
//        model.addAttribute("currentUserId",          userId);
//        model.addAttribute("currentUserProfile",     profile);
//        model.addAttribute("currentUserConnection",  connection);
//        model.addAttribute("currentUserDisplayName", displayName);
//        model.addAttribute("currentData",            data);
//
//        if (LOG.isDebugEnabled()) {
//            logInfo(request, model, userId, path, session);
//        }
//    }
    
	@ModelAttribute("appSettings")
	public ApplicationSettings getApplicationSettings() {
		return applicationSettings;
	}

	@ExceptionHandler(ContactNotFoundException.class)
	public ModelAndView handleContactNotFoundException() {
		logger.info("In ContactNotFound Exception Handler");

		ModelAndView mav = new ModelAndView();
		mav.addObject("errortitle", "Contact Missing in Action!");
		mav.addObject("errorbody", "We'll find the rascal, don't you worry");
		mav.setViewName(ERROR_GENERIC_VIEW);
		return mav;
	}

	@ExceptionHandler(UnknownResourceException.class)
	public String handleUnknownResourceException(HttpServletRequest req) {
		if (req.getRequestURI().indexOf("favicon") == 0)
			logger.info("404:" + req.getRequestURI());
		return ERROR_404_VIEW;
	}

	@ExceptionHandler(GeoLocationException.class)
	public ModelAndView handleGeoLocationException(HttpServletRequest request) {
		ModelAndView mav = new ModelAndView();
		String location = (String) request.getAttribute("location");
		String msg = webUI.getMessage(LOCATION_ERROR_MESSAGE_KEY, location);
		mav.addObject(LOCATION_ERROR_ATTRIBUTE, msg);
		mav.setViewName(PRODUCT_MAP_VIEW);
		return mav;
	}

}
