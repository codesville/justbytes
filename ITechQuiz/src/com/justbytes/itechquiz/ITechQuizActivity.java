package com.justbytes.itechquiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class ITechQuizActivity extends BaseActivity implements OnClickListener {

	public static final String TAG = ITechQuizActivity.class.getName();

	ImageButton netButton;
	ImageButton javaButton;
	ImageButton sqlButton;
	ImageButton unixButton;
	ImageButton hibernateButton;
	ImageButton springButton;

	public static final String CATEGORY_KEY = "CATEGORY";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		LinearLayout layout = (LinearLayout) findViewById(R.id.mainAdLayout);
		layout.addView(getAdView());

		javaButton = (ImageButton) findViewById(R.id.javaImgButton);
		netButton = (ImageButton) findViewById(R.id.netImgButton);
		sqlButton = (ImageButton) findViewById(R.id.sqlImgButton);
		unixButton = (ImageButton) findViewById(R.id.unixImgButton);
		hibernateButton = (ImageButton) findViewById(R.id.hiberImgButton);
		springButton = (ImageButton) findViewById(R.id.springImgButton);

		javaButton.setOnClickListener(this);
		netButton.setOnClickListener(this);
		sqlButton.setOnClickListener(this);
		unixButton.setOnClickListener(this);
		hibernateButton.setOnClickListener(this);
		springButton.setOnClickListener(this);

		// move to AsyncTask
		try {
			dbAdapter.createDatabase();
		} catch (Exception ex) {
			Log.e(TAG, ex.getMessage());
			throw new Error("Unable to create database");
		}
		// try{
		// dbAdapter.openDatabase();
		// }catch(Exception ex){
		// Log.e(TAG, ex.getMessage());
		// }
	}

	@Override
	public void onClick(View v) {
		Category category = Category.Java;
		Intent intent = new Intent(this, TopicsActivity.class);
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		case R.id.javaImgButton:
			// Log.i(TAG, "Java image button clicked");
			category = Category.Java;
			break;
		case R.id.netImgButton:
			// Log.i(TAG, ".NET image button clicked");
			category = Category.DotNet;
			break;
		case R.id.sqlImgButton:
			// Log.i(TAG, "sql image button clicked");
			category = Category.Sql;
			intent = new Intent(this, QandAActivity.class);
			bundle.putString(TopicsActivity.TOPIC_NAME_KEY, "All");
			break;
		case R.id.unixImgButton:
			// Log.i(TAG, "Unix image button clicked");
			category = Category.Unix;
			intent = new Intent(this, QandAActivity.class);
			bundle.putString(TopicsActivity.TOPIC_NAME_KEY, "All");
			break;
		case R.id.hiberImgButton:
			category = Category.Hibernate;
			intent = new Intent(this, QandAActivity.class);
			bundle.putString(TopicsActivity.TOPIC_NAME_KEY, "All");
			break;
		case R.id.springImgButton:
			// Log.i(TAG, "Unix image button clicked");
			category = Category.Spring;
			intent = new Intent(this, QandAActivity.class);
			bundle.putString(TopicsActivity.TOPIC_NAME_KEY, "All");
			break;
		}

		bundle.putString(CATEGORY_KEY, category.toString());
		intent.putExtras(bundle);
		startActivity(intent);
	}

}