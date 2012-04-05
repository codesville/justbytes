package com.justbytes.itechquiz.notification;

import java.util.Hashtable;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Secure;
import android.util.Log;

import com.justbytes.itechquiz.R;
import com.justbytes.itechquiz.util.AppUtils;

public class C2DMRegistrationReceiver extends BroadcastReceiver {
	public static final String C2DM_REG_NOTIFICATION = "com.google.android.c2dm.intent.REGISTRATION";

	@Override
	public void onReceive(Context ctx, Intent intent) {
		String action = intent.getAction();
		if (C2DM_REG_NOTIFICATION.equals(action)) {
			String registrationId = intent.getStringExtra("registration_id");
			String error = intent.getStringExtra("error");
			Log.d("C2DM", "registrationId=" + registrationId + " error:" + error);
			String deviceId = Secure.getString(ctx.getContentResolver(),
					Secure.ANDROID_ID);
			try {
				Map<String,String> params = new Hashtable<String, String>();
				params.put("deviceId", deviceId);
				params.put("regId", registrationId);
				AppUtils.postHttpRequest(ctx.getString(R.string.notfRegisterURL), params);
			} catch (Exception ex) {
				Log.e("C2DM", "Error:", ex);
			}
		}

	}

}
