package com.justbytes.itechquiz;

import java.util.concurrent.atomic.AtomicInteger;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.justbytes.itechquiz.data.MainListArrayAdapter;
import com.justbytes.itechquiz.util.AppConstants;
import com.justbytes.itechquiz.util.AppUtils;

public class ITechQuizActivity extends BaseActivity {

	public static final String TAG = ITechQuizActivity.class.getName();

	GoogleCloudMessaging gcm;
	AtomicInteger msgId = new AtomicInteger();
	SharedPreferences prefs;
	Context context;
	// String regid;
	public static int BACKOFF_COUNT = 1;

	ImageButton postQandAButton;

	ListView mainListView;

	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

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

				// ***** TEMP: use to load JSON files into the db.
				// Intent intent = new Intent(ITechQuizActivity.this,
				// LoadJsonFilesActivity.class);

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

		context = getApplicationContext();
		String regid = getRegistrationId(context);

		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			if (regid.length() == 0) {
				registerDevice();
			}
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}

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

	/**
	 * Gets the current registration id for application on GCM service.
	 * <p>
	 * If result is empty, the registration has failed.
	 * 
	 * @return registration id, or empty string if the registration is not
	 *         complete.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = AppUtils.getGCMPreferences(context);
		String registrationId = prefs.getString(AppConstants.PROPERTY_REG_ID,
				"");
		if (registrationId == null || registrationId.length() == 0) {
			Log.v(TAG, "Registration not found.");
			return "";
		}
		// check if app was updated; if so, it must clear registration id to
		// avoid a race condition if GCM sends a message
		int registeredVersion = prefs.getInt(AppConstants.PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = AppUtils.getAppVersion(context);
		if (registeredVersion != currentVersion
				|| isRegistrationExpired(context)) {
			Log.v(TAG, "App version changed or registration expired.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Checks if the registration has expired.
	 * 
	 * <p>
	 * To avoid the scenario where the device sends the registration to the
	 * server but the server loses it, the app developer may choose to
	 * re-register after REGISTRATION_EXPIRY_TIME_MS.
	 * 
	 * @return true if the registration has expired.
	 */
	private boolean isRegistrationExpired(Context context) {
		final SharedPreferences prefs = AppUtils.getGCMPreferences(context);
		// checks if the information is not stale
		long expirationTime = prefs.getLong(
				AppConstants.PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
		return System.currentTimeMillis() > expirationTime;
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

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	public void registerDevice() {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {

				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(context);
					}
					// register with GCM
					gcm.register(getString(R.string.gcmSenderId));

					// Log.v(TAG, "Device registered, registration id=" +
					// regid);
				} catch (Exception ex) {
					Log.e(TAG, "Error while registering device:", ex);
				}

				return null;
			}

		}.execute(null, null, null);
	}

}