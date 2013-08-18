package com.justbytes.itechquiz;

import com.justbytes.itechquiz.data.DbAdapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoadJsonFilesActivity extends Activity {

	Button loadButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loadjsonfiles);
		final DbAdapter dbAdapter = new DbAdapter(this);
		loadButton = (Button) findViewById(R.id.loadJsonFilesButton);
		loadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dbAdapter.getWritableDatabase();
				dbAdapter.loadDataFromJsonFiles(getApplicationContext());

			}
		});
	}

}
