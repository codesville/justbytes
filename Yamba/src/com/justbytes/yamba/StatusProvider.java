package com.justbytes.yamba;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class StatusProvider extends ContentProvider {

	private static final String TAG = "StatusProvider";

	// content://authority/data/id
	public static final Uri uri = Uri
			.parse("content://com.justbytes.yamba.statusprovider/status/47");
	public static final String SINGLE_RECORD_MIME_TYPE = "vnd.android.cursor.item/vnd.justbytes.yamba.status";
	public static final String MULTIPLE_RECORD_MIME_TYPE = "vnd.android.cursor.dir/vnd.marakana.yamba.mstatus";

	StatusData statusData;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		long id = ContentUris.parseId(uri);
		SQLiteDatabase db = statusData.dbHelper.getWritableDatabase();
		try {

			if (id < 0) {
				return db.delete(StatusData.TABLE, selection, selectionArgs);
			} else {
				return db.delete(StatusData.TABLE, StatusData.C_ID + "=" + id,
						null);
			}
		} finally {
			db.close();
		}
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = statusData.dbHelper.getWritableDatabase();
		try {
			long id = db.insertOrThrow(StatusData.TABLE, null, values);
			if (id == -1) {
				throw new RuntimeException(
						String.format(
								"%s: Failed to insert [%s] to [%s] for unknown reasons.",
								TAG, values, uri));
			} else {
				return ContentUris.withAppendedId(uri, id);
			}
		} finally {
			db.close();
		}

	}

	@Override
	public boolean onCreate() {
		statusData = new StatusData(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		long id = ContentUris.parseId(uri);
		SQLiteDatabase db = statusData.dbHelper.getReadableDatabase();
		try {

			if (id < 0) {
				return db.query(StatusData.TABLE, projection, selection, selectionArgs,null,null,sortOrder);
			} else {
				return db.query(StatusData.TABLE, projection, StatusData.C_ID + "=" + id, null,null,null,sortOrder);
			}
		} finally {
			//not closing otherwise cursor will be destroyed
			//db.close();
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		long id = ContentUris.parseId(uri);
		SQLiteDatabase db = statusData.dbHelper.getWritableDatabase();
		try {

			if (id < 0) {
				return db.update(StatusData.TABLE, values, selection, selectionArgs);
			} else {
				return db.update(StatusData.TABLE, values, StatusData.C_ID + "=" + id,
						null);
			}
		} finally {
			db.close();
		}
		
	}

}
