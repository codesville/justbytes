package com.justbytes.itechquiz.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.justbytes.itechquiz.QAndA;

public class DbAdapter extends SQLiteOpenHelper {

	public static final String TAG = DbAdapter.class.getName();

	public static String DB_PATH = "/data/data/com.justbytes.itechquiz/databases/";
	public static final String DB_NAME = "itechquiz.db";
	// Upgrade this version for every change in schema
	public static final int DB_VER = 3;
	public static final String Q_A_TABLE_NAME = "q_and_a";
	public static final String TOPICS_TABLE_NAME = "topics";

	private SQLiteDatabase db;
	private Context ctx;
	private String dbAbsoluteFileName = DB_PATH + DB_NAME;

	public static final String C_ID = BaseColumns._ID;
	public static final String C_TOPIC_TITLE = "title";
	public static final String C_TOPIC_CATEGORY = "category";
	public static final String C_Q_A_QUESTION = "question";
	public static final String C_Q_A_ANSWER = "answer";
	public static final String C_Q_A_TOPIC_ID = "topic_id";
	public static final String C_Q_A_VERSION = "version";
	public static final String C_Q_A_POSTED_TIME = "posted_time";
	public static final String C_Q_A_POSTED_BY = "posted_by";

	public DbAdapter(Context context) {
		super(context, DB_NAME, null, DB_VER);
		DB_PATH = context.getDatabasePath(DB_NAME).getAbsolutePath();
		ctx = context;

	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "OnUpgrade called.Deleting old DB version(" + oldVersion
				+ ").Upgrading to new version(" + newVersion + ")");

		try {
			File dbFile = new File(dbAbsoluteFileName);
			if (dbFile.exists()) {
				boolean outcome = dbFile.delete();
				Log.i(TAG, "Old db file deleted:" + outcome);
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error deleting old DB:", ex);
		}

	}

	public void createDatabase() throws IOException {
		if (isDbExists()) {

			db = openDatabase();
			Log.i(TAG, "Existing DB version(" + db.getVersion()
					+ ").New version(" + DB_VER + ")");
			try {
				if (db.getVersion() < DB_VER) {
					boolean deleteFlag = false;
					File dbFile = new File(dbAbsoluteFileName);
					if (dbFile.exists()) {
						deleteFlag = dbFile.delete();
						Log.i(TAG, "Old db file deleted:" + deleteFlag);
					}

					Log.i(TAG, "Copying prebaked database from assets");
					db = this.getReadableDatabase();
					copyDatabase();

				}
			} catch (Exception ex) {
				Log.e(TAG, "Error deleting/recreating DB:", ex);
			}

		} else {
			Log.i(TAG, "Copying prebaked database from assets");
			db = this.getReadableDatabase();
			try {
				copyDatabase();
				// !!!!!!!!! WARNING: hack to load json files.Comment it out
				// before pushing to market
				//loadDataFromJsonFiles(ctx);
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}

	}

	public SQLiteDatabase openDatabase() {
		return SQLiteDatabase.openDatabase(dbAbsoluteFileName, null,
				SQLiteDatabase.OPEN_READONLY);
	}

	private boolean isDbExists() {
		try {
			Log.i(TAG, "DB path+name=" + dbAbsoluteFileName);
			db = SQLiteDatabase.openDatabase(dbAbsoluteFileName, null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (Exception ex) {

		} finally {
			if (db != null)
				db.close();
		}

		return (db != null);
	}

	private void copyDatabase() throws IOException {

		InputStream dbInputStr = ctx.getAssets().open(DB_NAME);
		OutputStream dbOut = new FileOutputStream(dbAbsoluteFileName);
		int len = 0;
		byte[] buffer = new byte[1024];
		while ((len = dbInputStr.read(buffer)) > 0) {
			Log.i(TAG, "Writing " + len + " bytes to output");
			dbOut.write(buffer, 0, len);
		}

		dbOut.flush();
		dbOut.close();
		dbInputStr.close();

	}

	public Cursor getTopics(String category) {
		db = getReadableDatabase();
		Cursor cursor = db.query(TOPICS_TABLE_NAME, new String[] { C_ID,
				C_TOPIC_TITLE }, C_TOPIC_CATEGORY + " = ?",
				new String[] { category.toString() }, null, null, null);
		// Log.d(TAG, cursor.getColumnCount() + " topics found");

		return cursor;
	}

	public Cursor getQandA(String category, String topic) {
		db = getReadableDatabase();
		Cursor cursor = db
				.rawQuery(
						"select q_and_a._id as _id, q_and_a.question,q_and_a.answer,q_and_a.posted_by,q_and_a.posted_time from q_and_a inner join topics on"
								+ " q_and_a.topic_id=topics._id where topics.category = ? and topics.title=?",
						new String[] { category, topic });

		// Log.d(TAG, cursor.getCount() + " questions found");
		// while (cursor.move(1)) {
		// Log.i(TAG, cursor.getString(1));
		// }
		// cursor.moveToFirst();

		return cursor;
	}

	public Cursor getAnswers(int qId) {
		db = getReadableDatabase();
		Cursor cursor = db
				.rawQuery(
						"select q_and_a.answer,q_and_a._id from q_and_a where q_and_a._id = ?",
						new String[] { qId + "" });

		// Log.d(TAG, cursor.getCount() + " answer found");

		return cursor;
	}

	public int getCurrentVersion() {
		db = getReadableDatabase();
		Cursor cursor = db.rawQuery("select max(topics.version) from topics",
				null);
		int maxVersion = 1;
		try {
			cursor.moveToFirst();
			// Log.d(TAG, cursor.getInt(0) + " max(version)");
			maxVersion = cursor.getInt(0);
		} finally {
			if (cursor != null)
				cursor.close();
			if (db != null)
				db.close();

		}
		return maxVersion;
	}

	public void updateTopicVersion(Map<Integer, Integer> topicVersionMap) {
		db = getWritableDatabase();
		try {
			for (Integer topicId : topicVersionMap.keySet()) {
				ContentValues values = new ContentValues();
				values.put(C_Q_A_VERSION, topicVersionMap.get(topicId));
				db.update(TOPICS_TABLE_NAME, values, C_ID + " = ? ",
						new String[] { topicId + "" });
			}
		} catch (Exception ex) {
			Log.e(TAG, "Error updating topic versions:", ex);
		} finally {
			if (db != null)
				db.close();
		}
	}

	public void close() {

		if (db != null)
			db.close();

	}

	public void loadDataFromJsonFiles(Context ctx) {
		JsonAdapter jsonAdapter = new JsonAdapter();
		final String[][] fileNames = { { "dotnet_ado.json", "dotnet_asp.json",
				"dotnet_patterns.json", "dotnet_basics.json",
				"dotnet_linq.json", "dotnet_oops.json",
				"dotnet_silverlight.json", "dotnet_wcf.json",

				"java_class.json", "java_collections.json", "java_fundas.json",
				"java_exceptions.json", "java_jms.json", "java_patterns.json",
				"java_gc.json", "java_io_networking.json",
				"java_persistance.json", "java_servlets.json",
				"java_threads.json", "java_struts.json", "hibernate_all.json",
				"spring_all.json", "sql_all.json", "unix_all.json",
				"soa_all.json", "xml_all.json", "javascript_all.json", } };

		for (int catId = 0; catId < fileNames.length; catId++) {
			for (int topicId = 0; topicId < fileNames[catId].length; topicId++) {
				Log.d(TAG, "Loading JSON file:" + fileNames[catId][topicId]);
				List<QAndA> qandaList = jsonAdapter.parseFile(ctx,
						fileNames[catId][topicId]);
				insertQandA(qandaList);
			}
		}

	}

	public Map<Integer, Integer> insertQandA(List<QAndA> qandaList) {
		if (qandaList.size() == 0)
			return Collections.EMPTY_MAP;

		Map<Integer, Integer> topicVersionMap = new HashMap<Integer, Integer>();
		db = getWritableDatabase();
		try {
			for (QAndA qanda : qandaList) {
				ContentValues rowValues = new ContentValues();
				rowValues.put(C_Q_A_QUESTION, qanda.getQuestion());
				rowValues.put(C_Q_A_ANSWER, qanda.getAnswer());
				rowValues.put(C_Q_A_TOPIC_ID, qanda.getTopicId());
				rowValues.put(C_Q_A_POSTED_BY, qanda.getPostedBy());
				rowValues.put(C_Q_A_POSTED_TIME, qanda.getPostedTime());

				// when inserting questions from assets folder, version may
				// default to 0 if the files don't have version element.However
				// the map is not used in that case. The returned map is only
				// used when fetching questions remotely
				topicVersionMap.put(qanda.getTopicId(), qanda.getVersion());

				db.insertWithOnConflict(Q_A_TABLE_NAME, null, rowValues,
						SQLiteDatabase.CONFLICT_IGNORE);
			}
			Log.d(TAG, "Finished inserting " + qandaList.size()
					+ " rows to SQLiteDB from JsonList");
		} catch (Exception ex) {
			Log.e(TAG, "Error updating topic versions:", ex);
		} finally {
			if (db != null)
				db.close();
		}
		return topicVersionMap;
	}

}
