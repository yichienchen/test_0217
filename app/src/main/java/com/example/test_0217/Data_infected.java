package com.example.test_0217;

import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import static com.example.test_0217.DBHelper.TB;
import static com.example.test_0217.MainActivity.DH;
import static com.example.test_0217.MainActivity.infected_id;
import static com.example.test_0217.MainActivity.num_of_id;


public class Data_infected {
    String user_id;

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public Data_infected(String user_id) {
        infected_id.add(user_id);
        num_of_id = infected_id.indexOf(user_id);
//        Log.e(TAG,"Data_infected: "+user_id);

        this.user_id = user_id;
    }

    private boolean someMethod(String s) {
        SQLiteDatabase db = DH.getReadableDatabase();
        String sql = "select count(*) from " + TB + " where "
                + "user_id" + " = " + DatabaseUtils.sqlEscapeString(s);
        SQLiteStatement statement = db.compileStatement(sql);

        try {
            return statement.simpleQueryForLong() > 0;
        } finally {
            statement.close();
        }
    }

}
