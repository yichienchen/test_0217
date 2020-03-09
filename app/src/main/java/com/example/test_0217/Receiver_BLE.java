package com.example.test_0217;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import static com.example.test_0217.MainActivity.TAG;
import static com.example.test_0217.MainActivity.mBluetoothAdapter;
import static com.example.test_0217.MainActivity.startAdvButton;
import static com.example.test_0217.MainActivity.startScanningButton;
import static com.example.test_0217.MainActivity.stopAdvButton;
import static com.example.test_0217.MainActivity.stopScanningButton;

public class Receiver_BLE extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intentMainActivity = new Intent(context,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intentMainActivity,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentBLE = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        PendingIntent pendingIntent_BLE = PendingIntent.getActivity(context,0,intentBLE,PendingIntent.FLAG_UPDATE_CURRENT);

        final String action = intent.getAction();
        if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(mBluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
            switch(state) {
                case 10: //STATE_OFF
                    Log.e(TAG,"STATE_OFF");
                    NotificationChannel mChannel = new NotificationChannel("BLE" , "藍芽" , NotificationManager.IMPORTANCE_HIGH ) ;
                    Notification notification = new Notification.Builder(context,"BLE")
                            .setSmallIcon(R.drawable.ble)
                            .setContentTitle("application")
                            .setContentText("藍芽已關閉 請開啟藍芽")
                            .setWhen(System.currentTimeMillis())
                            .setContentIntent(pendingIntent_BLE)
                            .build();

                    notification.flags |= Notification.FLAG_ONGOING_EVENT;
                    mChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(mChannel) ;
                    notificationManager.notify(0, notification);


                    break;
                case 12: //STATE_ON
                    Log.e(TAG,"STATE_ON");
                    notificationManager.cancel(0);
                    stopAdvButton.setVisibility(View.INVISIBLE);
                    stopScanningButton.setVisibility(View.INVISIBLE);
                    startScanningButton.setVisibility(View.VISIBLE);
                    startAdvButton.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }
}
