package com.justbytes.itechquiz.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.util.Log;

import com.justbytes.itechquiz.QAndA;
import com.justbytes.itechquiz.R;

public class RemoteDataFetcher {
	AndroidHttpClient httpClient;

	public static String URI = "http://www.justbytes.info/ITechQuiz/getLatestQandA?ver=";

	public List<QAndA> fetchLatestQandA(Context ctx) throws Exception {
		List<QAndA> qandaList = new ArrayList<QAndA>();

		URI = ctx.getString(R.string.fetchURL);

		DbAdapter dbAdapter = new DbAdapter(ctx);
		int ver = dbAdapter.getCurrentVersion();
		Log.i("VERSION", "Current version=" + ver);

		
		
		
		HttpClient httpClient = new DefaultHttpClient();
		
		//TODO: COMMENT IT OUT BEFORE PUSHING OUT !!!
		final HttpHost PROXY_HOST = new HttpHost("webproxy.bankofamerica.com", 8080);
		HttpParams httpParameters = new BasicHttpParams();
		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, PROXY_HOST);
		
		StringBuilder jsonString = new StringBuilder();

		try {
			Log.d("STATUS_CODE", "Sending request to remote server: " + URI
					+ ver);
			HttpResponse response = httpClient.execute(new HttpGet(URI + ver));
			StatusLine statusLine = response.getStatusLine();
			Log.d("STATUS_CODE",
					"Response code from server=" + statusLine.getStatusCode());

			if (statusLine.getStatusCode() == 200) {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
				String line = "";

				while ((line = reader.readLine()) != null) {
					jsonString.append(line);
				}
				Log.d("REMOTEDATA:", jsonString.toString());

			} else {
				throw new Exception("Failed to fetch new questions");
			}
		} catch (Exception ex) {
			Log.e(this.getClass().getName(),
					"Error fetching latest questions.", ex);
			throw new Exception("Failed to fetch new questions");
		} finally {
			//httpClient.close();
		}

		if (jsonString != null && jsonString.length() > 0) {
			JsonAdapter jsonAdapter = new JsonAdapter();
			qandaList = jsonAdapter.parseJsonString(jsonString.toString());
		}

		return qandaList;
	}

}
