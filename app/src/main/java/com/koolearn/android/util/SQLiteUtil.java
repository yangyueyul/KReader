package com.koolearn.android.util;

import android.database.sqlite.SQLiteStatement;

public abstract class SQLiteUtil {
	public static void bindString(SQLiteStatement statement, int index, String value) {
		if (value != null) {
			statement.bindString(index, value);
		} else {
			statement.bindNull(index);
		}
	}

	public static void bindLong(SQLiteStatement statement, int index, Long value) {
		if (value != null) {
			statement.bindLong(index, value);
		} else {
			statement.bindNull(index);
		}
	}
}
