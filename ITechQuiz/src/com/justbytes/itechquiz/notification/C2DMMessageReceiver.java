package com.justbytes.itechquiz.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.justbytes.itechquiz.ITechQuizActivity;
import com.justbytes.itechquiz.R;

public class C2DMMessageReceiver extends BroadcastReceiver {
	public static final String C2DM_MSG_NOTIFICATION = "com.google.android.c2dm.intent.RECEIVE";
	public static final String TAG = C2DMMessageReceiver.class.getName();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.i(TAG, "Message Receiver called");
		if (C2DM_MSG_NOTIFICATION.equals(action)) {
			final String payLoad = intent.getStringExtra("message");
			Log.d(TAG, "dmControl: message = " + payLoad);
			createNotification(context, payLoad);
		}

	}

	private void createNotification(Context context, String payLoad) {
		try {
			NotificationManager notfMgr = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Log.i(TAG, "Creating notification...");
			Notification notification = new Notification(R.drawable.launchicon,
					payLoad, System.currentTimeMillis());
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.defaults |= Notification.DEFAULT_SOUND;
			Intent intent = new Intent(context, ITechQuizActivity.class);
			intent.putExtra("fetchNotification", payLoad);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, 0);
			notification.setLatestEventInfo(context, "ITechQuiz", payLoad,
					pendingIntent);
			notfMgr.notify(0, notification);
		} catch (Exception ex) {
			Log.e(TAG, "Error posting notification:", ex);
		}
	}

}
