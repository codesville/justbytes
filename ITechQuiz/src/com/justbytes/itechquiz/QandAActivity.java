package com.justbytes.itechquiz;

import java.text.SimpleDateFormat;

import android.app.ExpandableListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.SimpleCursorTreeAdapter.ViewBinder;
import android.widget.TextView;

import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.justbytes.itechquiz.data.DbAdapter;
import com.justbytes.itechquiz.util.AppUtils;

public class QandAActivity extends ExpandableListActivity {

	public static String TAG = QandAActivity.class.getName();
	private SimpleCursorTreeAdapter cursorTreeAdapter;

	private DbAdapter dbAdapter;
	AdView adView;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.qanda);

		LinearLayout layout = (LinearLayout) findViewById(R.id.qandaAdLayout);
		adView = new AdView(this, AdSize.BANNER, AppUtils.AD_ID);
		LayoutParams layoutParams = new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		adView.setLayoutParams(layoutParams);
		adView.loadAd(AppUtils.getAdRequest());
		layout.addView(adView);

		Bundle intentBundle = this.getIntent().getExtras();
		// get keys from prev screen
		String topicName = intentBundle
				.getString(TopicsActivity.TOPIC_NAME_KEY);
		String category = intentBundle
				.getString(ITechQuizActivity.CATEGORY_KEY);
		dbAdapter = new DbAdapter(this);
		this.setTitle(category + "> " + topicName + "> Questions");
		// get related q and a
		Cursor cursor = dbAdapter.getQandA(category, topicName);
		startManagingCursor(cursor);
		// bind to adapter
		cursorTreeAdapter = new QandACursorAdapter(this, cursor,
				R.layout.qandagroup, R.layout.qandagroup, new String[] {
						DbAdapter.C_Q_A_QUESTION, DbAdapter.C_Q_A_POSTED_BY,
						DbAdapter.C_ID }, new int[] { R.id.questionText,
						R.id.questionComment }, R.layout.qandachild,
				R.layout.qandachild, new String[] { DbAdapter.C_Q_A_ANSWER },
				new int[] { R.id.answertext });
		cursorTreeAdapter.setViewBinder(VIEW_BINDER);
		setListAdapter(cursorTreeAdapter);
		// getExpandableListView();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbAdapter.close();
	}

	static ViewBinder VIEW_BINDER = new ViewBinder() {

		@Override
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

			if (DbAdapter.C_Q_A_POSTED_BY.equalsIgnoreCase(cursor
					.getColumnName(columnIndex))) {
				TextView questionComment = (TextView) view
						.findViewById(R.id.questionComment);
				questionComment
						.setText("By: "
								+ cursor.getString(columnIndex)
								+ "\nOn: "
								+ cursor.getString(
										cursor.getColumnIndex(DbAdapter.C_Q_A_POSTED_TIME))
										.substring(0, 10));
				return true;
			}

			return false;
		}
	};

	class QandACursorAdapter extends SimpleCursorTreeAdapter {

		public QandACursorAdapter(Context context, Cursor cursor,
				int collapsedGroupLayout, int expandedGroupLayout,
				String[] groupFrom, int[] groupTo, int childLayout,
				int lastChildLayout, String[] childFrom, int[] childTo) {

			super(context, cursor, collapsedGroupLayout, expandedGroupLayout,
					groupFrom, groupTo, childLayout, lastChildLayout,
					childFrom, childTo);

		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			Cursor cursor = dbAdapter.getAnswers(groupCursor.getInt(0));
			startManagingCursor(cursor);
			return cursor;
		}

	}

}
