package com.socialmedia.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialmedia.model.Opinion;
import com.socialmedia.service.i.SummaryServiceI;
import com.socialmedia.service.impl.SummaryServiceImpl;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/")
public class SummaryController {
	SummaryServiceI summaryService = new SummaryServiceImpl();

	@PostMapping("/summary/{polarity}/{transReq}")
	public ResponseEntity<String> sentimentOpinions(@PathVariable String transReq, @PathVariable String polarity,
			@RequestBody List<Opinion> opinions) {
		System.out.println("yes");
		return ResponseEntity.ok(summaryService.summaryOpinions(opinions, polarity, transReq));
	}
}
