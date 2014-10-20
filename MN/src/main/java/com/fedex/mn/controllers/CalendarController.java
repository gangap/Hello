package com.fedex.mn.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fedex.mn.app.cache.MemCache;
import com.fedex.mn.models.impls.CalEvent;
import com.fedex.mn.models.impls.MNView;
import com.fedex.mn.services.MNService;

@Controller
@RequestMapping("/calendar")
public class CalendarController
{
	@Autowired
	private MNService mnServ;
	
	private static Logger log = Logger.getLogger(CalendarController.class);
	
	@RequestMapping(value="/{status}/{dest}", method=RequestMethod.GET)
	public String getCalendar(@PathVariable("status") String status, @PathVariable("dest") String dest, ModelMap model)
	{
		model.addAttribute("status", status);
		model.addAttribute("dest", dest);
		
		return "calendar";
	}
	
	@RequestMapping(value="/events/{status}/{dest}", method=RequestMethod.GET)
	public @ResponseBody List<CalEvent> getCalendarEvents(@PathVariable("status") String status, @PathVariable("dest") String dest)
	{
		List<CalEvent> events = new ArrayList<CalEvent>();
		List<MNView> views = mnServ.getAllMNs();
		
		for(MNView mn : views)
		{
			if((dest.equalsIgnoreCase("all") || dest.equalsIgnoreCase(mn.getDest())) &&
				(status.equalsIgnoreCase("all") || status.equalsIgnoreCase(mn.getStatus())))
			{
				events.add(new CalEvent(mn));
			}
				
		}
		
		return events;
	}
}
