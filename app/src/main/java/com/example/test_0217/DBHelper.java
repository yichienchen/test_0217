package com.example.test_0217;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    final static String TB1="MYTB1";
    final static String TB3="MYTB3";
    final static int VS=2;
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        //super(context, name, factory, version);
        super(context, name, null, VS);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL1 = "CREATE TABLE IF NOT EXISTS "+ TB1 +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT , " +
                "user_id VARCHAR(15) NOT NULL , " +
                "time_first DATETIME NOT NULL , "+
                "time_last DATETIME NOT NULL , "+
                "rssi_level_1 INTEGER NOT NULL , "+
                "rssi_level_2 INTEGER NOT NULL , "+
                "rssi_level_3 INTEGER NOT NULL )";


        String SQL3 = "CREATE TABLE IF NOT EXISTS "+ TB3 +
                "(time DATETIME PRIMARY KEY )";

        db.execSQL(SQL1);
        db.execSQL(SQL3);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String SQL = "DROP TABLE " + TB1;
        db.execSQL(SQL);
    }
}
