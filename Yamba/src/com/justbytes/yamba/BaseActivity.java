package com.justbytes.yamba;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends Activity {

	protected YambaApplication yamba;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		yamba = (YambaApplication) this.getApplication();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemPrefs:
			startActivity(new Intent(this, PrefsActivity.class)
					.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		case R.id.itemToggleService:
			if (yamba.isSvcRunning()) {
				stopService(new Intent(this, UpdaterService.class));
			} else {
				startService(new Intent(this, UpdaterService.class));
			}
			break;
		case R.id.itemPurge:
			//TODO
			//yamba.getStatusData().delete();
			Toast.makeText(this, R.string.msgAllDataPurged, Toast.LENGTH_LONG);
			break;
		case R.id.itemTimeline:
			startActivity(new Intent(this, TimelineActivity.class).addFlags(
					Intent.FLAG_ACTIVITY_SINGLE_TOP).addFlags(
					Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		case R.id.itemStatus:
			startActivity(new Intent(this, StatusActivity.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
			break;
		}
		return true;
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		MenuItem item = menu.findItem(R.id.itemToggleService);
		if(yamba.isSvcRunning()){
			item.setIcon(android.R.drawable.ic_media_pause);
			item.setTitle(R.string.titleServiceStop);
		}else{
			item.setIcon(android.R.drawable.ic_media_play);
			item.setTitle(R.string.titleServiceStart);
		}
		return super.onMenuOpened(featureId, menu);
	}
	
	

}
