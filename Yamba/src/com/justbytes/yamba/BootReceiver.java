package com.justbytes.yamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context ctx, Intent intent) {
		ctx.startService(new Intent(ctx, UpdaterService.class));
		Log.d("BootReceiver", "onReceive");
	}

}
