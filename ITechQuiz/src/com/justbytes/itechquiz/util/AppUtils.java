package com.justbytes.itechquiz.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.content.Intent;
import android.util.Log;

import com.google.ads.AdRequest;

public class AppUtils {

	public static String[] keywordArr = new String[] { "Java", "C#", "Unix",
			"J2EE", "Dice", "SQL", "Oracle", "Monster jobs", "Hibernate", "IT",
			"jobs", "Spring", "Oracle", "Microsoft", "Silverlight", ".NET",
			"Android", "Google", "Oasis", "Tennis", "Guitar" };

	public static final String AD_ID = "a14f4c6b1e03bee";
	public static final String PLAIN_TEXT = "plain/text";

	public static AdRequest getAdRequest() {
		AdRequest adReq = new AdRequest();
		Set<String> keywords = new HashSet<String>();
		keywords.addAll(Arrays.asList(keywordArr));
		adReq.setKeywords(keywords);
		// adReq.addTestDevice(AdRequest.TEST_EMULATOR);
		return adReq;
	}

	public static Intent createMailIntent(String[] to, String subject,
			String body, String mimeType) {
		Intent mailIntent = new Intent(android.content.Intent.ACTION_SEND);
		mailIntent.setType(mimeType);
		mailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, to);
		mailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		mailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
		// since we are calling from outside main context
		// mailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Log.d("EMAIL", "Sending email to " + to + " with subject=" + subject
				+ " with body = " + body);
		return mailIntent;
	}
}
