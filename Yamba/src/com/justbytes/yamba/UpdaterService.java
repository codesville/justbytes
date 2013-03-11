package com.justbytes.yamba;
import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;


public class UpdaterService extends Service {
	static final String TAG = "UpdaterService";
	
	private Updater updater;
	private static final int DELAY = 60000; //1 minute
	private boolean runFlag = false;
	private YambaApplication app;
	//private SQLiteDatabase db;
	Intent intent;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		this.updater = new Updater();
		this.app = (YambaApplication)getApplication();
		Log.d(TAG,"onCreate");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.runFlag = false;
		this.updater.interrupt();
		this.app.setSvcRunning(false);
		this.updater = null;
		Log.d(TAG,"onDestroy");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if(!runFlag){
			this.runFlag = true;
			this.updater.start(); //start polling thread
			this.app.setSvcRunning(true);
			Log.d(TAG,"onStartCommand");
		}
		return START_STICKY;
	}
	
	public class Updater extends Thread {
		final String RECEIVE_TIMELINE_NOTIFICATIONS = "com.justbytes.yamba.RECEIVE_TIMELINE_NOTIFICATIONS";
		@Override
		public synchronized void start() {
			while(runFlag){
				Log.d(TAG, "Running updater task...");
				try {
					//work goes here
					YambaApplication app = (YambaApplication) getApplication();
					int updateCount = app.loadStatusUpdates();
					if(updateCount > 0){
						Log.i(TAG, "We have a new status");
						intent = new Intent("com.justbytes.yamba.NEW_STATUS");
						intent.putExtra("com.justbytes.yamba.UPDATE_COUNT", updateCount);
						UpdaterService.this.sendBroadcast(intent, RECEIVE_TIMELINE_NOTIFICATIONS);
					}
					Log.d(TAG, "Done with updater task...sleeping");
					Thread.sleep(DELAY);
				} catch (InterruptedException e) {
					e.printStackTrace();
					runFlag = false;
				} 
			}
			
		}
		
	}

}
