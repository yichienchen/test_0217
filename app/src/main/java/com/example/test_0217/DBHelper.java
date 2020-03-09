package com.example.test_0217;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    final static String DB="MYDB.db";
    final static String TB="MYTB";
    final static int VS=2;
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        //super(context, name, factory, version);
        super(context, DB, null, VS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL = "CREATE TABLE IF NOT EXISTS "+ TB +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT , " +
                "user_id VARCHAR(15) NOT NULL , " +
                "time_first DATETIME NOT NULL , "+
                "time_last DATETIME NOT NULL )";
        db.execSQL(SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL = "DROP TABLE " + TB;
        db.execSQL(SQL);
    }
}
