package com.fedex.mn.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fedex.mn.models.impls.ClientMsg;

@Controller
@RequestMapping("/msg")
public class MessageController
{
	@RequestMapping
	public @ResponseBody List<ClientMsg> sendMsg(@ModelAttribute("title") String title, 
			@ModelAttribute("type") String type, @ModelAttribute("msg") String msg)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		msgs.add(new ClientMsg(type, title, msg));
		return msgs;		
	}
}
