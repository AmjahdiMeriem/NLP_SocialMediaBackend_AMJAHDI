package com.socialmedia.service.impl;

import com.darkprograms.speech.translator.GoogleTranslate;
import com.socialmedia.service.i.RequestServiceI;
import java.io.IOException;

public class RequestServiceImpl implements RequestServiceI {
	@Override
	public String translate(String req, String lang) {
		String t = null;
		try {
			t = GoogleTranslate.translate(lang, req);
			return t;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return t;
	}

}
