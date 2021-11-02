package springmvcactuator.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MyController {
	@RequestMapping(value="/home")
	public String showHome(){
		return "home";
	}
}
