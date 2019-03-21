/**
 * 
 */
package com.justbytes.yamba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;


public class TimelineActivity extends BaseActivity {
	
	private static final String TAG = "TimelineActivity";
	static final String SEND_TIMELINE_NOTIFICATIONS = "com.justbytes.yamba.SEND_TIMELINE_NOTIFICATIONS";
	
	//StatusData.DbHelper dbHelper;
	StatusData statusData;
	//SQLiteDatabase db;
	//TextView timelineText;
	ListView timelineListView;
	SimpleCursorAdapter cursorAdapter;
	//TimelineAdapter cursorAdapter;
	final String[] FROM = {StatusData.C_CREATED_AT, StatusData.C_USER, StatusData.C_TEXT};
	final int[] TO = {R.id.textCreatedAt, R.id.textUser, R.id.textText};
	//YambaApplication yamba;
	Cursor cursor;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);
		//yamba = (YambaApplication)this.getApplication();
		
		if(yamba.getPrefs().getString("username", null) == null) {
			startActivity(new Intent(this, PrefsActivity.class));
			Toast.makeText(this, R.string.msgSetupPrefs, Toast.LENGTH_LONG).show();
		}
		
		//timelineText = (TextView)findViewById(R.id.timelineText);
		timelineListView = (ListView) findViewById(R.id.timelineListView);
		statusData = ((YambaApplication)getApplication()).getStatusData();
		//dbHelper = statusData.new DbHelper(this);
		//db = dbHelper.getReadableDatabase();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//db.close();
		statusData.close();
		
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		setupList();
	}
	
	private void setupList() {
		cursor = statusData.getStatusUpdates();
		startManagingCursor(cursor); //deprecated
//		while(cursor.moveToNext()){
//			String post = cursor.getString(cursor.getColumnIndex(StatusData.C_TEXT));
//			String user = cursor.getString(cursor.getColumnIndex(StatusData.C_USER));
//			timelineText.append(String.format("%s: %s\n", user, post));
//		}
		
		//cursorAdapter = new SimpleCursorAdapter(this,R.layout.row, cursor, FROM, TO);
		//cursorAdapter = new TimelineAdapter(this, cursor);
		cursorAdapter = new SimpleCursorAdapter(this, R.layout.row, cursor, FROM, TO);
		cursorAdapter.setViewBinder(VIEW_BINDER);
		timelineListView.setAdapter(cursorAdapter);
		
		//register broadcast receiver for updating status when viewing TImeline activity
		TimelineReceiver receiver = new TimelineReceiver();
		IntentFilter filter = new IntentFilter("com.justbytes.yamba.NEW_STATUS");
		registerReceiver(receiver, filter, SEND_TIMELINE_NOTIFICATIONS, null);
	}
	
	final static ViewBinder VIEW_BINDER = new ViewBinder() {
		
		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			if(view.getId() != R.id.textCreatedAt){
				return false;
			}
			long createdAt = cursor.getLong(columnIndex);
			((TextView)view).setText(DateUtils.getRelativeTimeSpanString(view.getContext(),createdAt));
			return true;
		}
	};
	
	class TimelineReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			cursor = statusData.getStatusUpdates();
			cursorAdapter.changeCursor(cursor);
			cursorAdapter.notifyDataSetChanged();
			Log.d(TAG, "timeline on receive");
		}
		
	}

}
