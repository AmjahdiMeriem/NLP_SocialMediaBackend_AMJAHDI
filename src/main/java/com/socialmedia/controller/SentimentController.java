package com.socialmedia.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.socialmedia.model.Opinion;
import com.socialmedia.service.i.SentimentServiceI;
import com.socialmedia.service.impl.SentimentServiceImpl;

@CrossOrigin(origins = "http://localhost:4200")
//@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/")
public class SentimentController {
	SentimentServiceI sentimentService = new SentimentServiceImpl();

	@PostMapping("/sentiment")
	public ResponseEntity<List<Float>> calculateScores(@RequestBody List<Opinion> opi) {
		System.out.println("yes");
		return ResponseEntity.ok(sentimentService.calculateScores(opi));
	}

	@PostMapping("/sentiment/comments")
	public ResponseEntity<List<Opinion>> sentimentOpinions2(@RequestBody List<Opinion> opi) {
		System.out.println("yes");
		return ResponseEntity.ok(sentimentService.sentimentOpinion(opi));
	}

	@GetMapping("/sentiment")
	public ResponseEntity<String> sentimentOpinions() {
		System.out.println("yes");
		return ResponseEntity.ok(sentimentService.sentimentOpinions());
	}

}
