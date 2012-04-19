package com.justbytes.itechquiz;

import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.justbytes.itechquiz.data.DbAdapter;
import com.justbytes.itechquiz.data.RemoteDataFetcher;
import com.justbytes.itechquiz.util.AppUtils;

public class BaseActivity extends Activity {

	DbAdapter dbAdapter;

	AdView adView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createAdView();
		dbAdapter = new DbAdapter(this);
	}

	protected void createAdView() {
		adView = new AdView(this, AdSize.BANNER, AppUtils.AD_ID);
		LayoutParams layoutParams = new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		adView.setLayoutParams(layoutParams);
		adView.loadAd(AppUtils.getAdRequest());

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemRefresh:
			// Log.d(TAG, "Refresh questions menu item clicked...");
			new FetchQuestionsTask().execute(new Void[] {});
			break;
		case R.id.itemContact:
			String body = "\n\n\n\n\n\n\n\n\n\n\n\nDevice: "
					+ Build.MANUFACTURER + " " + Build.MODEL + "\nVersion: "
					+ Build.VERSION.SDK_INT + " " + Build.VERSION.RELEASE;
			Intent mailIntent = AppUtils
					.createMailIntent(
							new String[] { getString(R.string.emailTo) },
							getString(R.string.emailSubject), body,
							AppUtils.PLAIN_TEXT);
			startActivity(Intent
					.createChooser(mailIntent, "Send mail using..."));
			break;
		case R.id.itemShare:
			String shareMsg = getString(R.string.shareMsg);
			Intent shareIntent = AppUtils.createMailIntent(new String[] {},
					getString(R.string.shareSubject), shareMsg,
					AppUtils.PLAIN_TEXT);
			startActivity(Intent.createChooser(shareIntent, "Share using..."));
			break;
		case R.id.itemPost:
			Intent postIntent = new Intent(this, PostQandAActivity.class);
			startActivity(postIntent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	class FetchQuestionsTask extends AsyncTask<Void, Void, Integer> {
		RemoteDataFetcher dataFetcher;
		ProgressDialog progDiag = null;

		@Override
		protected void onPreExecute() {
			progDiag = ProgressDialog.show(BaseActivity.this,
					"Fetching latest QandA", "Please wait...", true, true);
		}

		@Override
		protected Integer doInBackground(Void... params) {
			int fetchCount = 0;
			try {
				publishProgress(new Void[0]);
				dataFetcher = new RemoteDataFetcher();
				// fetch latest
				List<QAndA> qandaList = dataFetcher
						.fetchLatestQandA(getApplicationContext());
				fetchCount = qandaList.size();
				// insert into q_and_a
				Map<Integer, Integer> topicVersionMap = dbAdapter
						.insertQandA(qandaList);
				// upgrade topic version
				dbAdapter.updateTopicVersion(topicVersionMap);
			} catch (Exception ex) {
				Log.e("BaseActivity", "Error retrieving new questions", ex);
				fetchCount = 0;
			}
			return fetchCount;
		}

		@Override
		protected void onPostExecute(Integer result) {
			Log.d("POSTExec", result + " rows fetched");
			String message = "";
			if (progDiag != null)
				progDiag.dismiss();
			if (result == 0) {
				message = "Questions are up-to-date.Nothing new to fetch.";
			} else {
				message = String.format("Fetched %s latest questions/answers!",
						new Object[] { result });
			}
			Toast.makeText(BaseActivity.this, message, Toast.LENGTH_LONG)
					.show();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbAdapter.close();
	}

	public AdView getAdView() {
		return adView;
	}
}
