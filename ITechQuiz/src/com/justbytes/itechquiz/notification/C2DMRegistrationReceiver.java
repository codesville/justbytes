package com.justbytes.itechquiz.notification;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.justbytes.itechquiz.R;
import com.justbytes.itechquiz.util.AppConstants;
import com.justbytes.itechquiz.util.AppUtils;

public class C2DMRegistrationReceiver extends BroadcastReceiver {
	public static final String C2DM_REG_NOTIFICATION = "com.google.android.c2dm.intent.REGISTRATION";
	public static int BACKOFF_COUNT = 1;
	private Context ctx;
	private GoogleCloudMessaging gcm;

	public static final String TAG = C2DMRegistrationReceiver.class.getName();

	@Override
	public void onReceive(Context ctx, Intent intent) {
		String action = intent.getAction();
		this.ctx = ctx;
		Log.i(TAG, "Received registration intent");
		if (C2DM_REG_NOTIFICATION.equals(action)) {
			String registrationId = intent.getStringExtra("registration_id");
			String error = intent.getStringExtra("error");
			Log.d(TAG, "registrationId=" + registrationId + " error:" + error);
			new RegisterAsyncTask().execute(new String[] { registrationId,
					error });

		}

	}

	class RegisterAsyncTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			String registrationId = params[0];
			String error = params[1];
			if (registrationId == null) {
				int BACKOFF_TIME = (int) Math.pow(2, BACKOFF_COUNT++);
				if ("SERVICE_NOT_AVAILABLE".equals(error)) {

					Log.i(TAG, "Retrying registration after " + BACKOFF_TIME
							+ " secs");
					SystemClock.sleep(BACKOFF_TIME * 1000);

					registerDevice(ctx);
				}

			} else {
				try {

					// set in SharedPrefs and GAE server
					setRegistrationId(ctx, registrationId);
				} catch (Exception ex) {
					Log.e(TAG, "Error:", ex);
				}
			}
			return null;
		}

	}

	public void registerDevice(Context ctx) {
		if (gcm == null) {
			gcm = GoogleCloudMessaging.getInstance(ctx);
		}
		// register with GCM
		try {
			String regid = gcm.register(ctx.getString(R.string.gcmSenderId));
		} catch (IOException ex) {
			Log.e("C2DM", "Error:", ex);
		}
	}

	/**
	 * Stores the registration id, app versionCode, and expiration time in the
	 * application's {@code SharedPreferences}.And sends request to save the
	 * same in GAE server
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration id
	 */
	private void setRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = AppUtils.getGCMPreferences(context);
		int appVersion = AppUtils.getAppVersion(context);
		Log.v(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(AppConstants.PROPERTY_REG_ID, regId);
		editor.putInt(AppConstants.PROPERTY_APP_VERSION, appVersion);
		long expirationTime = System.currentTimeMillis()
				+ AppConstants.REGISTRATION_EXPIRY_TIME_MS;

		Log.v(TAG, "Setting registration expiry time to "
				+ new Timestamp(expirationTime));
		editor.putLong(AppConstants.PROPERTY_ON_SERVER_EXPIRATION_TIME,
				expirationTime);
		editor.commit();

		// send the regId to appengine server
		String deviceId = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
		Log.i(TAG, "DeviceId=" + deviceId);
		try {
			Map<String, String> paramMap = new Hashtable<String, String>();
			paramMap.put("deviceId", deviceId);
			paramMap.put("regId", regId);
			AppUtils.postHttpRequest(
					context.getString(R.string.notfRegisterURL), paramMap);
		} catch (Exception ex) {

			Log.e(TAG, "Error:", ex);
		}
	}

}
