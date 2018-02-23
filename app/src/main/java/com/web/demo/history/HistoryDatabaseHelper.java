package com.web.demo.history;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryDatabaseHelper extends SQLiteOpenHelper {
	private static final int DATABASE_VERSION = 1;
	public static final String TABLE_NAME = "TABLE_HISTORY";
	public static final String ROW_URL_TITLE = "TITLE";
	public static final String ROW_URL_LINK = "LINK";
	private static final String DATABASE_NAME = "HISTORY";

	private static final String DICTIONARY_TABLE_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
			+ " (historyid INTEGER NOT NULL PRIMARY KEY UNIQUE, " + ROW_URL_TITLE + " TEXT, " + ROW_URL_LINK + " TEXT);";

	public HistoryDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DICTIONARY_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
}
