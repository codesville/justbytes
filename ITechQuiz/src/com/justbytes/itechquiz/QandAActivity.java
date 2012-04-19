package com.justbytes.itechquiz;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.Toast;
import android.widget.SimpleCursorTreeAdapter.ViewBinder;
import android.widget.TextView;

import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.justbytes.itechquiz.BaseActivity.FetchQuestionsTask;
import com.justbytes.itechquiz.data.DbAdapter;
import com.justbytes.itechquiz.data.RemoteDataFetcher;
import com.justbytes.itechquiz.util.AppUtils;

public class QandAActivity extends ExpandableListActivity {

	public static String TAG = QandAActivity.class.getName();
	private SimpleCursorTreeAdapter cursorTreeAdapter;

	private DbAdapter dbAdapter;
	AdView adView;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.qanda);

		LinearLayout layout = (LinearLayout) findViewById(R.id.qandaAdLayout);
		adView = new AdView(this, AdSize.BANNER, AppUtils.AD_ID);
		LayoutParams layoutParams = new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		adView.setLayoutParams(layoutParams);
		adView.loadAd(AppUtils.getAdRequest());
		layout.addView(adView);

		Bundle intentBundle = this.getIntent().getExtras();
		// get keys from prev screen
		String topicName = intentBundle
				.getString(TopicsActivity.TOPIC_NAME_KEY);
		String category = intentBundle
				.getString(ITechQuizActivity.CATEGORY_KEY);
		dbAdapter = new DbAdapter(this);
		this.setTitle(category + "> " + topicName + "> Questions");
		// get related q and a
		Cursor cursor = dbAdapter.getQandA(category, topicName);
		startManagingCursor(cursor);
		// bind to adapter
		cursorTreeAdapter = new QandACursorAdapter(this, cursor,
				R.layout.qandagroup, R.layout.qandagroup, new String[] {
						DbAdapter.C_Q_A_QUESTION, DbAdapter.C_Q_A_POSTED_BY,
						DbAdapter.C_ID }, new int[] { R.id.questionText,
						R.id.questionComment }, R.layout.qandachild,
				R.layout.qandachild, new String[] { DbAdapter.C_Q_A_ANSWER },
				new int[] { R.id.answertext });
		cursorTreeAdapter.setViewBinder(VIEW_BINDER);
		setListAdapter(cursorTreeAdapter);
		// getExpandableListView();
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

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbAdapter.close();
	}

	static ViewBinder VIEW_BINDER = new ViewBinder() {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

			if (DbAdapter.C_Q_A_POSTED_BY.equalsIgnoreCase(cursor
					.getColumnName(columnIndex))) {
				TextView questionComment = (TextView) view
						.findViewById(R.id.questionComment);
				questionComment
						.setText("By: "
								+ cursor.getString(columnIndex)
								+ "\tOn: "
								+ cursor.getString(
										cursor.getColumnIndex(DbAdapter.C_Q_A_POSTED_TIME))
										.substring(0, 10));
				return true;
			}

			return false;
		}
	};

	class QandACursorAdapter extends SimpleCursorTreeAdapter {

		public QandACursorAdapter(Context context, Cursor cursor,
				int collapsedGroupLayout, int expandedGroupLayout,
				String[] groupFrom, int[] groupTo, int childLayout,
				int lastChildLayout, String[] childFrom, int[] childTo) {

			super(context, cursor, collapsedGroupLayout, expandedGroupLayout,
					groupFrom, groupTo, childLayout, lastChildLayout,
					childFrom, childTo);

		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			Cursor cursor = dbAdapter.getAnswers(groupCursor.getInt(0));
			startManagingCursor(cursor);
			return cursor;
		}

	}

	class FetchQuestionsTask extends AsyncTask<Void, Void, Integer> {
		RemoteDataFetcher dataFetcher;
		ProgressDialog progDiag = null;

		@Override
		protected void onPreExecute() {
			progDiag = ProgressDialog.show(QandAActivity.this,
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
			Toast.makeText(QandAActivity.this, message, Toast.LENGTH_LONG)
					.show();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

	}

}
