package com.justbytes.yamba;

import java.util.Date;
import java.util.Random;

import android.app.Application;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class YambaApplication extends Application implements
		OnSharedPreferenceChangeListener {
	private static final String TAG = "YambaApplication";

	SharedPreferences prefs;
	boolean isSvcRunning;
	StatusData statusData;

	@Override
	public void onCreate() {
		super.onCreate();
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.prefs.registerOnSharedPreferenceChangeListener(this);
		Log.d(TAG, "onCreate");
	}

	public StatusData getStatusData() {
		if (statusData == null) {
			this.statusData = new StatusData(this);
		}
		return statusData;
	}

	public int loadStatusUpdates() {
		int count = 0;
		// In real life..need to loop over status updates from Twitter instead
		// of hardcoding this each time
		long lastUpdated = statusData.getLatestStatusCreatedTime();
		Random ran = new Random(1111);
		ContentValues vals = new ContentValues();
		vals.put(StatusData.C_ID, ran.nextInt());
		vals.put(StatusData.C_CREATED_AT, new Date().getDate());
		vals.put(StatusData.C_TEXT, "Hi...this is tweety bird speaking");
		vals.put(StatusData.C_USER, "basuso");
		statusData.insertOrIgnore(vals);
		if (new Date().getDate() > lastUpdated)
			count++;
		Log.i(TAG, "Refreshed " + count + " status updates");
		return count;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.d(TAG, "onTerminate");
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		this.prefs = null;
	}

	public void getCredentials() {
		Log.i(TAG, "Prefs Username=" + prefs.getString("username", "blah"));
		Log.i(TAG, "Prefs Password=" + prefs.getString("password", "blah"));
	}

	public boolean isSvcRunning() {
		return isSvcRunning;
	}

	public void setSvcRunning(boolean isSvcRunning) {
		this.isSvcRunning = isSvcRunning;
	}

	public SharedPreferences getPrefs() {
		return prefs;
	}

	public void setPrefs(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public void setStatusData(StatusData statusData) {
		this.statusData = statusData;
	}

}
