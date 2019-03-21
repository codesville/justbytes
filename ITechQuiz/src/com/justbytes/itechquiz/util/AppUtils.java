package com.justbytes.itechquiz.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.google.ads.AdRequest;
import com.justbytes.itechquiz.ITechQuizActivity;

public class AppUtils {

	public static String[] keywordArr = new String[] { "Java", "C#", "Unix",
			"J2EE", "Dice", "SQL", "Oracle", "Monster jobs", "Hibernate", "IT",
			"jobs", "Spring", "Oracle", "Microsoft", "Silverlight", ".NET",
			"Android", "Google", "Oasis", "Tennis", "Guitar", "Deals",
			"Groupon", "Careers", "Information Technology", "IT", "Vacation",
			"women", "money", "trading", "parttime", "jQuery" };

	public static final String AD_ID = "a1519648ebada10"; // mr.
	// "a14f4c6b1e03bee"; //codes
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

	public static int postHttpRequest(String url, Map<String, String> params)
			throws Exception {
		DefaultHttpClient client = new DefaultHttpClient();
		

		HttpPost post = new HttpPost(url);
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		for (String key : params.keySet())
			paramList.add(new BasicNameValuePair(key, params.get(key)));
		post.setEntity(new UrlEncodedFormEntity(paramList));
		Log.i("POST", "Sending request to " + url);
		HttpResponse response = client.execute(post);
		if (response != null)
			Log.i("POST", response.getStatusLine().getStatusCode() + "");
		return response.getStatusLine().getStatusCode();
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	public static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	public static SharedPreferences getGCMPreferences(Context context) {
		return context.getSharedPreferences(
				ITechQuizActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

}
