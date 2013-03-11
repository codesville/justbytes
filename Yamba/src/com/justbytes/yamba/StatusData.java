package com.justbytes.yamba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class StatusData {

	private static final String TAG = "StatusData";

	static final int DB_VERSION = 1;
	static final String DB_NAME = "timeline.db";
	static final String TABLE = "timeline";
	static final String C_ID = BaseColumns._ID;
	static final String C_CREATED_AT = "created_at";
	static final String C_SOURCE = "source";
	static final String C_TEXT = "text";
	static final String C_USER = "user";

	private static final String GET_ALL_ORDER_BY = C_CREATED_AT + " DESC";
	private static final String[] MAX_CREATED_AT = { "max(" + C_CREATED_AT
			+ ")" };
	private static final String[] TEXT_COLS = { C_TEXT };

	public DbHelper dbHelper;

	public StatusData(Context ctx) {
		dbHelper = new DbHelper(ctx);
		Log.i(TAG, "Inited data");
	}

	public void insertOrIgnore(ContentValues vals) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		try {
			db.insertWithOnConflict(TABLE, null, vals,
					SQLiteDatabase.CONFLICT_IGNORE);
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			db.close();
		}
	}

	public void close() {
		dbHelper.close();
	}

	public Cursor getStatusUpdates() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		return db.query(TABLE, null, null, null, null, null, GET_ALL_ORDER_BY);
	}

	public long getLatestStatusCreatedTime() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(TABLE, MAX_CREATED_AT, null, null, null,
					null, null);
			try {
				return cursor.moveToNext() ? cursor.getLong(0) : Long.MIN_VALUE;
			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
	}

	public String getStatusTextById(long id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(TABLE, TEXT_COLS, C_ID + "= ?",
					new String[] { id + "" }, null, null, null);
			try {
				return cursor.moveToNext() ? cursor.getString(0) : null;
			} finally {
				cursor.close();
			}
		} finally {
			db.close();
		}
	}

	public class DbHelper extends SQLiteOpenHelper {

		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String createStmt = String
					.format("create table %s (%s int primary key, %s int, %s text, %s text, %s text)",
							new Object[] { TABLE, C_ID, C_CREATED_AT, C_SOURCE,
									C_USER, C_TEXT });
			db.execSQL(createStmt);
			Log.d(TAG, "onCreate sql=" + createStmt);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
			db.execSQL("drop table if exists " + TABLE);
			onCreate(db);
		}

	}
}
