package com.fedex.mn.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/ssologin")
public class SSOController
{
	@RequestMapping(method=RequestMethod.GET)
	public String getSSOlogin()
	{
		return "ssologin";
	}
}
