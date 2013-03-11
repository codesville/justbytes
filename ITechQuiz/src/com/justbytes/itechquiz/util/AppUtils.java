package com.justbytes.itechquiz.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultProxyAuthenticationHandler;
import org.apache.http.message.BasicNameValuePair;

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

	public static int postHttpRequest(String url, Map<String, String> params)
			throws Exception {
		DefaultHttpClient client = new DefaultHttpClient();
		// TODO: Proxy setting only needed within firewall.Comment out before
		// pushing out
//		client.getCredentialsProvider().setCredentials(
//				new AuthScope("webproxy.bankofamerica.com", 8080),
//				new UsernamePasswordCredentials("nbkgl14", "happy0be"));
//		HttpHost proxy = new HttpHost("webproxy.bankofamerica.com", 8080);
//		client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

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

}
