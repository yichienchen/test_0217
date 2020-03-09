package com.example.test_0217;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import static com.example.test_0217.DBHelper.TB;
import static com.example.test_0217.MainActivity.DH;
import static com.example.test_0217.MainActivity.TAG;
import static com.example.test_0217.MainActivity.infected_id;
import static com.example.test_0217.MainActivity.notificationManager;
import static com.example.test_0217.MainActivity.pendingIntent;

public class Receiver_contact_history extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e(TAG,"onReceive");


        NotificationChannel mChannel = new NotificationChannel("Data_infected" , "接觸史" , NotificationManager.IMPORTANCE_HIGH ) ;
                Notification notification = new Notification.Builder(context,"Data_infected")
                        .setSmallIcon(R.drawable.ble)
                        .setContentTitle("Receiver_contact_history")
                        .setContentText("曾與")
                        .setWhen(System.currentTimeMillis())
                        .build();

                notification.flags |= Notification.FLAG_ONGOING_EVENT;
                mChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(mChannel) ;
                notificationManager.notify(2, notification);


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
