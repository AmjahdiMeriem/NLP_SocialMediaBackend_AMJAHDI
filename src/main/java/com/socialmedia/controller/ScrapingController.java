package com.socialmedia.controller;

import com.socialmedia.model.Opinion;
import com.socialmedia.service.i.ScrapingServiceI;
import com.socialmedia.service.impl.ScrapingServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@CrossOrigin(origins = "http://localhost:4200")
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/")
public class ScrapingController {

	ScrapingServiceI scrapingService = new ScrapingServiceImpl();

	@GetMapping("/opinions/{req}/{site}/{nbr}")
	public ResponseEntity<Opinion> getOpinions(@PathVariable String req, @PathVariable String site,
			@PathVariable int nbr) throws InterruptedException {

		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\hp\\Desktop\\EMI-PFE\\chromedriver_win32\\chromedriver.exe");

		if (site.equals("Twitter")) {
			return ResponseEntity.ok(scrapingService.getOpinionsTwitter(req, nbr));
		} else if (site.equals("Youtube")) {
			return ResponseEntity.ok(scrapingService.getOpinionsYoutube(req, nbr));
		} else if (site.equals("Facebook")) {
			return ResponseEntity.ok(scrapingService.getOpinionsFacebook(req, nbr));
		} else {
			return ResponseEntity.ok(scrapingService.getOpinionsHespress(req, nbr));
		}

	}

	// download Opinions
	@PostMapping("/opinions/{req}")
	public ResponseEntity<Map<String, Boolean>> downloadOpinions2(@PathVariable String req,
			@RequestBody List<Opinion> opinions) {
		scrapingService.downloadOpinions(req, opinions);
		Map<String, Boolean> response = new HashMap<>();
		response.put("download", Boolean.TRUE);
		return ResponseEntity.ok(response);

	}

}
