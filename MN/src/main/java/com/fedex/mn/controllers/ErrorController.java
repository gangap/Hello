package com.fedex.mn.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/error")
public class ErrorController
{	
	@RequestMapping(value="/{type}", method={RequestMethod.GET, RequestMethod.POST})
	public String getErrorPage(@PathVariable("type") String type, ModelMap model)
	{
		model.addAttribute("type", type);				
		
		return "error";
	}
}
