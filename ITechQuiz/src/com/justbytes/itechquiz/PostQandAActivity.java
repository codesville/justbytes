package com.justbytes.itechquiz;

import java.util.Hashtable;
import java.util.Map;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.justbytes.itechquiz.data.DbAdapter;
import com.justbytes.itechquiz.util.AppUtils;

public class PostQandAActivity extends Activity implements OnClickListener,
		OnItemSelectedListener {
	Button submitButton;
	EditText questionText;
	EditText answerText;
	EditText usernameText;
	Spinner categorySpinner;
	Spinner topicSpinner;
	AdView adView;
	DbAdapter dbAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.postqanda);
		setTitle("Post your question and answer");

		LinearLayout layout = (LinearLayout) findViewById(R.id.postQandaAdLayout);
		adView = new AdView(this, AdSize.BANNER, AppUtils.AD_ID);
		LayoutParams layoutParams = new LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		adView.setLayoutParams(layoutParams);
		adView.loadAd(AppUtils.getAdRequest());
		layout.addView(adView);

		usernameText = (EditText) findViewById(R.id.postUsernameText);
		questionText = (EditText) findViewById(R.id.postQuestionText);
		answerText = (EditText) findViewById(R.id.postAnswerText);
		categorySpinner = (Spinner) findViewById(R.id.categorySpinner);
		topicSpinner = (Spinner) findViewById(R.id.topicSpinner);
		submitButton = (Button) findViewById(R.id.postQandASubmit);
		submitButton.setOnClickListener(this);
		categorySpinner.setOnItemSelectedListener(this);

		dbAdapter = new DbAdapter(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.postQandASubmit:
			String userName = usernameText.getText().toString().trim();
			String question = questionText.getText().toString().trim();
			String answer = answerText.getText().toString().trim();
			long topicId = topicSpinner.getSelectedItemId();

			if (TextUtils.isEmpty(userName))
				userName = "Anonymous";
			if (!TextUtils.isEmpty(question) && !TextUtils.isEmpty(answer)) {
				new PostQandAAsyncTask().execute(new String[] { userName,
						question, answer, topicId + "" });
			} else {
				Toast.makeText(this, "Please enter valid question/answer",
						Toast.LENGTH_LONG).show();
			}

			break;
		}

	}

	class PostQandAAsyncTask extends AsyncTask<String, Void, Integer> {
		ProgressDialog progDiag = null;

		@Override
		protected void onPreExecute() {
			progDiag = ProgressDialog.show(PostQandAActivity.this,
					"Submitting QandA", "Please wait...", true, true);
		}

		@Override
		protected Integer doInBackground(String... params) {
			int sc = HttpStatus.SC_OK;
			try {
				Map<String, String> paramMap = new Hashtable<String, String>();
				paramMap.put("username", params[0]);
				paramMap.put("question", params[1]);
				paramMap.put("answer", params[2]);
				paramMap.put("topicId", params[3]);
				publishProgress(new Void[0]);
				sc = AppUtils.postHttpRequest(getString(R.string.postQandAURL),
						paramMap);

			} catch (Exception ex) {
				Log.e("PostQandA", "Error:", ex);
			}

			return sc;
		}

		@Override
		protected void onPostExecute(Integer result) {

			if (progDiag != null)
				progDiag.dismiss();
			if (result == HttpStatus.SC_OK) {
				Toast.makeText(PostQandAActivity.this,
						"Successfully posted your question/answer.Thanks!",
						Toast.LENGTH_LONG).show();
				Intent intent = new Intent(getApplicationContext(),
						ITechQuizActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

				startActivity(intent);
			}

		}

		@Override
		protected void onProgressUpdate(Void... values) {

		}

	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view,
			int position, long id) {
		String category = adapterView.getSelectedItem().toString();
		Spinner topicSpinner = (Spinner) findViewById(R.id.topicSpinner);

		Cursor topicSpinnerCursor = dbAdapter.getTopics(category);
		startManagingCursor(topicSpinnerCursor);
		SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_spinner_item, topicSpinnerCursor,
				new String[] { DbAdapter.C_TOPIC_TITLE },
				new int[] { android.R.id.text1 });
		topicSpinner.setAdapter(cursorAdapter);

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}

}
