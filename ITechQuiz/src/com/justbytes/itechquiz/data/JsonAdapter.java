package com.justbytes.itechquiz.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.justbytes.itechquiz.QAndA;
import com.justbytes.itechquiz.util.AppConstants;

public class JsonAdapter {

	public List<QAndA> parseFile(Context ctx, String fileName) {
		List<QAndA> qandaList = new ArrayList<QAndA>();
		InputStream inputStr = null;
		BufferedReader buffReader = null;
		try {
			inputStr = ctx.getAssets().open(fileName,
					AssetManager.ACCESS_STREAMING);
			// String fileString = new String(inputStr);
			buffReader = new BufferedReader(new InputStreamReader(inputStr));
			String jsonLine = null;
			StringBuilder fileStr = new StringBuilder();
			// how will this behave if the file is big...reading into one big
			// string?
			while ((jsonLine = buffReader.readLine()) != null) {
				fileStr.append(jsonLine);
			}
			qandaList = parseJsonString(fileStr.toString());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStr != null)
					inputStr.close();
				if (buffReader != null)
					buffReader.close();
			} catch (IOException ex) {
				Log.e("JsonAdapter", "Failed to close file streams");
			}
		}
		return qandaList;
	}

	public List<QAndA> parseJsonString(String jsonStringArray)
			throws JSONException {
		List<QAndA> qandaList = new ArrayList<QAndA>();

		JSONObject qandaJson = new JSONObject(jsonStringArray);
		JSONArray jsonArray = qandaJson.getJSONArray("QandAList");

		for (int row = 0; row < jsonArray.length(); row++) {
			JSONObject jsonObj = jsonArray.getJSONObject(row);
			qandaList.add(buildQandA(jsonObj));
		}
		return qandaList;
	}

	private QAndA buildQandA(final JSONObject jsonObj) throws JSONException {
		final String JSON_POSTED_BY_COL = "username"; // TODO make them the same
														// as SQLlite column
														// name (posted_by)
		QAndA qanda = new QAndA();
		qanda.setQuestion(jsonObj.getString(DbAdapter.C_Q_A_QUESTION));
		qanda.setAnswer(jsonObj.getString(DbAdapter.C_Q_A_ANSWER));
		// qanda.setCategory(jsonObj.getString(DbAdapter.C_TOPIC_CATEGORY));
		qanda.setTopicId(jsonObj.getInt(DbAdapter.C_Q_A_TOPIC_ID));
		try {
			qanda.setPostedBy(jsonObj.getString(JSON_POSTED_BY_COL));

		} catch (Exception ex) {
			qanda.setPostedBy(AppConstants.ADMIN);
		}
		try {
			qanda.setPostedTime(new SimpleDateFormat("yyyy-MM-dd")
					.format(jsonObj.getString(DbAdapter.C_Q_A_POSTED_TIME)));
		} catch (Exception ex) {
			qanda.setPostedTime(new SimpleDateFormat("yyyy-MM-dd")
					.format(new Date()));
		}

		try {
			qanda.setVersion(jsonObj.getInt(DbAdapter.C_Q_A_VERSION));
		} catch (Exception ex) {
			// do nothing.some files may not have version
		}

		return qanda;
	}
}
