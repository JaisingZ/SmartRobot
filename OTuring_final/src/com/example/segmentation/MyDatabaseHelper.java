package com.example.segmentation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDatabaseHelper extends SQLiteOpenHelper{
	
	public static final String CREATE_TABLE = "create table chat("
			+"_id integer primary key,"
			+"ask text,"
			+"ans text,"
			+"key1 varchar(10),"
			+"key2 varchar(10),"
			+"key3 varchar(10),"
			+"key4 varchar(10),"
			+"key5 varchar(10),"
			+"key6 varchar(10),"
			+"key7 varchar(10),"
			+"key8 varchar(10),"
			+"key9 varchar(10),"
			+"key10 varchar(10))";
	

	public MyDatabaseHelper(Context context, String name, int version) {
		super(context, name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
