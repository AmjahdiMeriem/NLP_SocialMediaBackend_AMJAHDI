package com.socialmedia.controller;

import com.socialmedia.service.i.RequestServiceI;
import com.socialmedia.service.impl.RequestServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@CrossOrigin(origins = "http://localhost:4200")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/")
public class RequestController {

	RequestServiceI requestService = new RequestServiceImpl();

	@GetMapping("/requests/{req}/{lang}")
	public ResponseEntity<String> translateRequest(@PathVariable String req, @PathVariable String lang) {
		String tr = requestService.translate(req, lang);
		System.out.println(tr);
		return ResponseEntity.ok(tr);
	}

}
