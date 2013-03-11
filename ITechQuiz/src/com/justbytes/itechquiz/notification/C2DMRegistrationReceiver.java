package com.justbytes.itechquiz.notification;

import java.util.Hashtable;
import java.util.Map;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.util.Log;

import com.justbytes.itechquiz.R;
import com.justbytes.itechquiz.util.AppUtils;

public class C2DMRegistrationReceiver extends BroadcastReceiver {
	public static final String C2DM_REG_NOTIFICATION = "com.google.android.c2dm.intent.REGISTRATION";
	public static int BACKOFF_COUNT = 1;
	private Context ctx;

	@Override
	public void onReceive(Context ctx, Intent intent) {
		String action = intent.getAction();
		this.ctx = ctx;
		Log.i("C2DM", "Received registration intent");
		if (C2DM_REG_NOTIFICATION.equals(action)) {
			String registrationId = intent.getStringExtra("registration_id");
			String error = intent.getStringExtra("error");
			Log.d("C2DM", "registrationId=" + registrationId + " error:"
					+ error);
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

					Log.d("C2DM", "Retrying registration after " + BACKOFF_TIME
							+ " secs");
					SystemClock.sleep(BACKOFF_TIME * 1000);

					registerDevice(ctx);
				}

			} else {
				String deviceId = Secure.getString(ctx.getContentResolver(),
						Secure.ANDROID_ID);
				Log.i("C2DM", "DeviceId=" + deviceId);
				try {
					Map<String, String> paramMap = new Hashtable<String, String>();
					paramMap.put("deviceId", deviceId);
					paramMap.put("regId", registrationId);
					AppUtils.postHttpRequest(
							ctx.getString(R.string.notfRegisterURL), paramMap);
				} catch (Exception ex) {
					Log.e("C2DM", "Error:", ex);
				}
			}
			return null;
		}

	}

	public void registerDevice(Context ctx) {
		Intent intent = new Intent("com.google.android.c2dm.intent.REGISTER");
		intent.putExtra("app",
				PendingIntent.getBroadcast(ctx, 0, new Intent(), 0));
		intent.putExtra("sender", ctx.getString(R.string.emailTo));
		Log.i("C2DM", "Registering with C2DM server");
		ctx.startService(intent);
	}

}
