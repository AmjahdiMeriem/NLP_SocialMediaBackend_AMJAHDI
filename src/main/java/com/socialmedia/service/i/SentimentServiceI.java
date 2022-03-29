package com.socialmedia.service.i;

import java.util.List;

import com.socialmedia.model.Opinion;

public interface SentimentServiceI {
	
	public List<Float> calculateScores(List<Opinion> opinions);
	
	public List<Opinion> sentimentOpinion(List<Opinion> opinions);
	
	public String sentimentOpinions();

}
