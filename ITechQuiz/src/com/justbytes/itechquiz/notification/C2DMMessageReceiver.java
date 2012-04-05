package com.justbytes.itechquiz.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.justbytes.itechquiz.ITechQuizActivity;
import com.justbytes.itechquiz.R;

public class C2DMMessageReceiver extends BroadcastReceiver {
	public static final String C2DM_MSG_NOTIFICATION = "com.google.android.c2dm.intent.RECEIVE";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (C2DM_MSG_NOTIFICATION.equals(action)) {
			final String payLoad = intent.getStringExtra("payload");
			createNotification(context, payLoad);
		}

	}

	private void createNotification(Context context, String payLoad) {
		NotificationManager notfMgr = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification.Builder notfBuilder = new Notification.Builder(context);
		notfBuilder.setAutoCancel(true);
		notfBuilder.setContentTitle(context.getString(R.string.app_name));
		notfBuilder.setContentText(payLoad);
		notfBuilder.setWhen(System.currentTimeMillis());
		Intent intent = new Intent(context, ITechQuizActivity.class);
		intent.putExtra("fetchNotification", payLoad);
		notfBuilder.setContentIntent(PendingIntent.getActivity(context, 0,
				intent, 0));
		Notification notification = notfBuilder.getNotification();
		notfMgr.notify(0, notification);
	}

}
