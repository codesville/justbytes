package com.justbytes.yamba;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity extends BaseActivity implements OnClickListener,
		TextWatcher// , OnSharedPreferenceChangeListener
{

	private static final String TAG = "StatusActivity";

	EditText editText;
	TextView textCounter;
	Button updateButton;

	// SharedPreferences prefs;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);

		editText = (EditText) findViewById(R.id.editText1);
		updateButton = (Button) findViewById(R.id.buttonUpdate);
		textCounter = (TextView) findViewById(R.id.textCounter);
		textCounter.setText("140");
		textCounter.setTextColor(Color.GREEN);
		// add listeners
		editText.addTextChangedListener(this);
		updateButton.setOnClickListener(this);
		
		// prefs = PreferenceManager.getDefaultSharedPreferences(this);
		// prefs.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onClick(View v) {
		Log.d(TAG, "On click executed");
		new PostToTwitter().execute(editText.getText().toString());
	}

	class PostToTwitter extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... statuses) {
			Log.i(TAG, "Posting to twitter...." + statuses[0]);
			YambaApplication myApp = (YambaApplication) getApplication();
			myApp.getCredentials();
			return "Successfully posted your message.";
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG)
					.show();
		}

	}

	@Override
	public void afterTextChanged(Editable text) {
		int count = 140 - text.length();
		textCounter.setText(count + "");
		textCounter.setTextColor(Color.GREEN);
		if (count < 10)
			textCounter.setTextColor(Color.YELLOW);
		else if (count < 0)
			textCounter.setTextColor(Color.RED);

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// Log.i(TAG, "Text changed");
	}
	//Moved to BaseActivity
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.menu, menu);
//		return true;
//	}
//Moved to BaseActivity
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.itemPrefs:
//			startActivity(new Intent(this, PrefsActivity.class));
//			break;
//		case R.id.itemServiceStart:
//			startService(new Intent(this, UpdaterService.class));
//			break;
//		case R.id.itemServiceStop:
//			stopService(new Intent(this, UpdaterService.class));
//			break;
//		}
//		return true;
//	}

	// @Override
	// public void onSharedPreferenceChanged(SharedPreferences
	// sharedPreferences,
	// String key) {
	// Log.i(TAG,
	// "Prefs changed..reset username and password.New Value for key"+key+":"+sharedPreferences.getString(key,
	// ""));
	//
	// }

}