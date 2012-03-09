//package com.justbytes.yamba;
//
//import java.text.MessageFormat;
//
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteDatabase.CursorFactory;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.provider.BaseColumns;
//import android.util.Log;
//
//public class DbHelper extends SQLiteOpenHelper {
//
//	private static final String TAG = "DbHelper";
//	static final int DB_VERSION = 1;
//	static final String DB_NAME = "timeline.db";
//	static final String TABLE = "timeline";
//	static final String C_ID = BaseColumns._ID;
//	static final String C_CREATED_AT = "created_at";
//	static final String C_SOURCE = "source";
//	static final String C_TEXT = "text";
//	static final String C_USER = "user";
//
//	public DbHelper(Context context) {
//		super(context, DB_NAME, null, DB_VERSION);
//	}
//
//	@Override
//	public void onCreate(SQLiteDatabase db) {
//		String createStmt = String
//				.format("create table %s (%s int primary key, %s int, %s text, %s text, %s text)",
//						new Object[] { TABLE, C_ID, C_CREATED_AT, C_SOURCE,
//								C_USER, C_TEXT });
//		db.execSQL(createStmt);
//		Log.d(TAG, "onCreate sql=" + createStmt);
//
//	}
//
//	@Override
//	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
//		db.execSQL("drop table if exists " + TABLE);
//		onCreate(db);
//	}
//
//}
