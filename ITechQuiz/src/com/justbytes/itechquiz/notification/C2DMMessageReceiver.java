package com.justbytes.itechquiz.notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.justbytes.itechquiz.ITechQuizActivity;
import com.justbytes.itechquiz.R;

public class C2DMMessageReceiver extends BroadcastReceiver {
	public static final String C2DM_MSG_NOTIFICATION = "com.google.android.c2dm.intent.RECEIVE";
	public static final String TAG = C2DMMessageReceiver.class.getName();

	public static final int NOTIFICATION_ID = 1;
	NotificationCompat.Builder builder;
	Context ctx;

	@Override
	public void onReceive(Context context, Intent intent) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
		ctx = context;
		String messageType = gcm.getMessageType(intent);
		if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
			Log.w(TAG, "Send error: " + intent.getExtras().toString());
		} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
				.equals(messageType)) {
			Log.w(TAG, "Deleted messages on server: "
					+ intent.getExtras().toString());
		} else {
			sendNotification(intent.getExtras().getString("message"));
		}
		setResultCode(Activity.RESULT_OK);

	}

	private void sendNotification(String payLoad) {
		try {
			NotificationManager notfMgr = (NotificationManager) ctx
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Log.i(TAG, "Creating notification...");
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					ctx)
					.setSmallIcon(R.drawable.launchicon)
					.setContentTitle(ctx.getString(R.string.app_name))
					.setStyle(
							new NotificationCompat.BigTextStyle()
									.bigText(payLoad))
					.setWhen(System.currentTimeMillis()).setAutoCancel(true)
					.setContentText(payLoad);

			Intent intent = new Intent(ctx, ITechQuizActivity.class);
			intent.putExtra("fetchNotification", payLoad);

			PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
					intent, 0);
			mBuilder.setContentIntent(contentIntent);
			Notification notification = mBuilder.build();
			notfMgr.notify(1, notification);
		} catch (Exception ex) {
			Log.e(TAG, "Error posting notification:", ex);
		}
	}

}
