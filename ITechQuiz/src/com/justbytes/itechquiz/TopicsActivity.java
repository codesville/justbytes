package com.justbytes.itechquiz;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.justbytes.itechquiz.data.DbAdapter;

public class TopicsActivity extends BaseActivity {

	public static final String TAG = TopicsActivity.class.getName();
	public static final String TOPIC_NAME_KEY = "TOPIC_NAME";

	private ListView topicsList;
	private String topicTitle;
	private String category;

	private String[] FROM = { DbAdapter.C_TOPIC_TITLE };
	private int[] TO = { R.id.topicRowText };

	@Override
	public void onCreate(Bundle bundle) {
		// Log.d(TAG, "onCreate called");
		super.onCreate(bundle);
		setContentView(R.layout.topics);

		LinearLayout layout = (LinearLayout) findViewById(R.id.topicsAdLayout);
		layout.addView(getAdView());

		topicsList = (ListView) findViewById(R.id.topicsListView);
		Bundle intentBundle = this.getIntent().getExtras();
		category = intentBundle.getString(ITechQuizActivity.CATEGORY_KEY);
		this.setTitle(category + "> Topics");
		// Log.d(TAG, "Selected category=" + category);
		topicsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> listView, View view,
					int pos, long id) {
				// Log.d(TAG, "Clicked=" + pos + " " + ((TextView)
				// view).getText());
				Intent qandaIntent = new Intent(TopicsActivity.this,
						QandAActivity.class);
				Bundle bundle = new Bundle();

				bundle.putString(ITechQuizActivity.CATEGORY_KEY, category);
				bundle.putString(TOPIC_NAME_KEY, ((TextView) view
						.findViewById(R.id.topicRowText)).getText().toString());
				qandaIntent.putExtras(bundle);
				startActivity(qandaIntent);
			}
		});

		setupTopicList(category);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Log.d(TAG, "onResume:" + category);
		// setupTopicList(getCategory());
	}

	private void setupTopicList(String category) {
		Cursor cursor = null;
		try {
			cursor = dbAdapter.getTopics(category);
			startManagingCursor(cursor);
			SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this,
					R.layout.topicrow, cursor, FROM, TO);
			cursorAdapter.setViewBinder(VIEW_BINDER);
			topicsList.setAdapter(cursorAdapter);
		} catch (Exception ex) {
			Log.e(TAG, "Error populating topic list ", ex);
		} finally {
			// closing the cursor causes problems with ListView displaying
			// nothing
			// if(cursor != null)
			// cursor.close();
			// causing db also does when resuming activity
			// dbAdapter.close();
		}

	}

	final static ViewBinder VIEW_BINDER = new ViewBinder() {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			// Log.d(TAG, cursor.getString(columnIndex));
			return false;
		}
	};

	public String getTopicTitle() {
		return topicTitle;
	}

	public void setTopicTitle(String topicTitle) {
		this.topicTitle = topicTitle;
	}

}
