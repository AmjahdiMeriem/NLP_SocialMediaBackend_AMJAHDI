package com.socialmedia.service.i;

import java.util.List;

import com.socialmedia.model.Opinion;

public interface SummaryServiceI {
	public String summaryOpinions(List<Opinion> opinions, String polarity, String transReq);
}
