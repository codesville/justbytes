package com.justbytes.itechquiz;

import com.justbytes.itechquiz.data.MainListArrayAdapter;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ITechQuizActivity extends BaseActivity {

	public static final String TAG = ITechQuizActivity.class.getName();

	ImageButton postQandAButton;

	ListView mainListView;

	public static final String CATEGORY_KEY = "CATEGORY";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setTitle("ITechQuiz: All in one IT prep app");

		LinearLayout layout = (LinearLayout) findViewById(R.id.mainAdLayout);
		layout.addView(getAdView());

		mainListView = (ListView) findViewById(R.id.mainListView);
		mainListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> listView, View view,
					int pos, long id) {

				Intent intent = new Intent(ITechQuizActivity.this,
						TopicsActivity.class);
				Bundle bundle = new Bundle();
				String category = ((TextView) view
						.findViewById(R.id.mainListText)).getText().toString();
				final Category cat = Category.valueOf(Category.DotNet
						.toString().equals(category) ? Category.DotNet.name()
						: category);

				if (!(cat == Category.Java || cat == Category.DotNet)) {
					intent = new Intent(ITechQuizActivity.this,
							QandAActivity.class);
					bundle.putString(TopicsActivity.TOPIC_NAME_KEY, "All");
				}

				bundle.putString(CATEGORY_KEY, category.toString());
				intent.putExtras(bundle);
				startActivity(intent);
			}

		});

		postQandAButton = (ImageButton) findViewById(R.id.postQandAButton);
		postQandAButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ITechQuizActivity.this,
						PostQandAActivity.class);
				startActivity(intent);

			}
		});

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			String msgNotification = bundle.getString("fetchNotification");
			if (msgNotification != null) {
				Log.i(TAG, "onCreate::Received notification:" + msgNotification);
				new FetchQuestionsTask().execute(new Void[] {});
				this.getIntent().removeExtra("fetchNotification");
			}
		}

		setupListView();

		// load pre-baked DB from assets folder
		new DbLoadTask().execute(new Void[] {});

	}

	private void setupListView() {
		String[] categories = getResources().getStringArray(R.array.categories);
		ArrayAdapter<String> arrayAdapter = new MainListArrayAdapter(this,
				R.layout.mainlistrow, categories);
		mainListView.setAdapter(arrayAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			String msgNotification = bundle.getString("fetchNotification");
			if (msgNotification != null) {
				Log.i(TAG, "onResume::Received notification:" + msgNotification);
				new FetchQuestionsTask().execute(new Void[] {});
				this.getIntent().removeExtra("fetchNotification");
			}
		}

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			String msgNotification = bundle.getString("fetchNotification");
			if (msgNotification != null) {
				Log.i(TAG, "onRestart::Received notification:"
						+ msgNotification);
				new FetchQuestionsTask().execute(new Void[] {});
				this.getIntent().removeExtra("fetchNotification");
			}
		}

	}

	public void onClick(View v) {
		Category category = Category.Java;
		Intent intent = new Intent(this, TopicsActivity.class);
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		case R.id.javaImgButton:
			category = Category.Java;
			break;
		case R.id.netImgButton:
			category = Category.DotNet;
			break;
		case R.id.sqlImgButton:
			category = Category.Sql;
			intent = new Intent(this, QandAActivity.class);
			bundle.putString(TopicsActivity.TOPIC_NAME_KEY, "All");
			break;
		case R.id.unixImgButton:
			category = Category.Unix;
			intent = new Intent(this, QandAActivity.class);
			bundle.putString(TopicsActivity.TOPIC_NAME_KEY, "All");
			break;
		case R.id.hiberImgButton:
			category = Category.Hibernate;
			intent = new Intent(this, QandAActivity.class);
			bundle.putString(TopicsActivity.TOPIC_NAME_KEY, "All");
			break;
		case R.id.springImgButton:
			category = Category.Spring;
			intent = new Intent(this, QandAActivity.class);
			bundle.putString(TopicsActivity.TOPIC_NAME_KEY, "All");
			break;
		case R.id.postQandAButton:
			intent = new Intent(this, PostQandAActivity.class);
			break;
		}

		bundle.putString(CATEGORY_KEY, category.toString());
		intent.putExtras(bundle);
		startActivity(intent);
	}

	class DbLoadTask extends AsyncTask<Void, Void, Void> {
		// ProgressBar progBar = (ProgressBar)
		// findViewById(R.id.mainProgressBar);
		ProgressDialog progDiag = null;

		@Override
		protected void onPreExecute() {
			progDiag = ProgressDialog.show(ITechQuizActivity.this,
					"Loading...", "Please wait...", true, true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			publishProgress(new Void[0]);
			try {
				dbAdapter.createDatabase();
			} catch (Exception ex) {
				Log.e(TAG, ex.getMessage());
				throw new Error("Unable to create database");
			}
			try {
				registerDevice();
			} catch (Exception ex) {
				Log.e(TAG, "Error registering device:", ex);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// progBar.setVisibility(View.GONE);
			if (progDiag != null)
				progDiag.dismiss();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// progBar.bringToFront();
		}

	}

	public void registerDevice() {
		Intent intent = new Intent("com.google.android.c2dm.intent.REGISTER");
		intent.putExtra("app",
				PendingIntent.getBroadcast(this, 0, new Intent(), 0));
		intent.putExtra("sender", getString(R.string.c2dmSenderEmail));
		Log.i(TAG, "Registering with C2DM server");
		startService(intent);
	}

}