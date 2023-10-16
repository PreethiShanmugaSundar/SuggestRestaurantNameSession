package com.suggestions.restaurantname.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.suggestions.restaurantname.model.RestaurantNameSuggestion;
import com.suggestions.restaurantname.model.Session;
import com.suggestions.restaurantname.service.RestaurantNameService;
/**
 * Controller class to create session 
 */
@Controller
public class RestaurantNameController {
	
	@Autowired
	RestaurantNameService restaurantNameService;
	
	/**
	 * Redirect to Home page
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model) {
		return "index";
	}
	
	/***
	 * Redirect to Create Session page 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/CreateSession", method = RequestMethod.GET)
	public String addNewSession(Model model) {
		Session session = new Session();
		model.addAttribute("session", session);
		return "CreateSession";
	}
	
	/**
	 * Create session if all validation all successful
	 * @param session
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/session/create", method = RequestMethod.POST)
	public String createSession(@Validated Session session, Model model) {
		System.out.println("User Page Requested:: Session::"+ session.getCreatedBy() + " : "+ session.getParticipantNames() + " : "+ session.getSessionEndDate()
		+" : "+ session.getSessionStartDate());
		String errMsg = restaurantNameService.validateSessionDetails(session);
		System.out.println("Error Message::"+errMsg);
		if(ObjectUtils.isEmpty(errMsg)) {
			System.out.println("inside if::");
			session = restaurantNameService.saveSessionDetails(session);
			System.out.println("Session id::"+session.getSessionId());
			session.setSessionUrl("http://localhost:8080/session/restaurantNameSuggestion/"+session.getSessionId());
			System.out.println("Session Url::"+session.getSessionUrl());
			model.addAttribute("success", true);
			model.addAttribute("sessionUrl", session.getSessionUrl());
			model.addAttribute(session);
			return "CreateSession";
		}else {
			model.addAttribute("failure", true);
			model.addAttribute("errMsg", errMsg);
			model.addAttribute(session);
			return "CreateSession";
		}
	
		
	}
	
	/**
	 * Check session expired if not redirect search page 
	 * @param sessionId
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/session/restaurantNameSuggestion/{sessionId}", method = RequestMethod.GET)
	public String openRestaurantNamePage(@PathVariable("sessionId") int sessionId,Model model )
	{   
		System.out.println("inside get method::"+sessionId);
		boolean isSessionExpired = restaurantNameService.checkSessionExpired(sessionId);
		if(isSessionExpired) {
			return "sessionExpiry";
		}else {
			RestaurantNameSuggestion restaurantNameSuggestion = new RestaurantNameSuggestion();
			restaurantNameSuggestion.setSessionId(sessionId);
			model.addAttribute(restaurantNameSuggestion);
			return "SessionRestaurantNameSearch";
		}
		
	}
	
	/**
	 * Validate entered EMployee number is valid and belongs to session
	 * @param restaurantNameSuggestion
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/session/restaurantNameSuggestion/searchEmployee", method = RequestMethod.POST)
	public String SearchEmployee(@Validated RestaurantNameSuggestion restaurantNameSuggestion, Model model)
	{   
		System.out.println("inside searchEmployee method::"+restaurantNameSuggestion.getSessionId() + " :: "+ restaurantNameSuggestion.getEmpNo());
		String errMsg = restaurantNameService.validateEmployeeNo(restaurantNameSuggestion);
		if(ObjectUtils.isEmpty(errMsg)) {
			List<RestaurantNameSuggestion> participantList = restaurantNameService.findParticipantList(restaurantNameSuggestion.getSessionId());
			model.addAttribute("participantList" , participantList);
			model.addAttribute(restaurantNameSuggestion);
			model.addAttribute("admin",restaurantNameSuggestion.isAdmin());
			return "SessionRestaurantNameSuggestion";
		}else {
			model.addAttribute("failure", true);
			model.addAttribute("errMsg", errMsg);
			model.addAttribute(restaurantNameSuggestion);
			return "SessionRestaurantNameSearch";
		}
	}
	
	/**
	 * Save restaurant name
	 * @param restaurantNameSuggestion
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/session/restaurantNameSuggestion/save", method = RequestMethod.POST,params="action=save")
	public String saveSuggestion(@Validated RestaurantNameSuggestion restaurantNameSuggestion, Model model)
	{
		restaurantNameService.saveRestaurantNameSuggestion(restaurantNameSuggestion);
		List<RestaurantNameSuggestion> participantList = restaurantNameService.findParticipantList(restaurantNameSuggestion.getSessionId());
		model.addAttribute("participantList" , participantList);
		model.addAttribute(restaurantNameSuggestion);
		model.addAttribute("Success", true);
		model.addAttribute("admin",restaurantNameSuggestion.isAdmin());
		return "SessionRestaurantNameSuggestion";
		
	}
	
	/**
	 * End session and chose one random restaurant name
	 * @param restaurantNameSuggestion
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/session/restaurantNameSuggestion/save", method = RequestMethod.POST, params="action=endSession")
	public String endSession(@Validated RestaurantNameSuggestion restaurantNameSuggestion, Model model)
	{
		String restaurantName = restaurantNameService.endSession(restaurantNameSuggestion);
		System.out.println("restaurantName"+restaurantName);
		model.addAttribute("restaurantName", restaurantName);
		return "SessionExpiry";
		
	}

}
