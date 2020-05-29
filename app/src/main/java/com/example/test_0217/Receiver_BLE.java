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

import java.util.Calendar;

import static com.example.test_0217.Service_scan_function.format;
import static com.example.test_0217.Activity.TAG;
import static com.example.test_0217.Activity.mBluetoothAdapter;
import static com.example.test_0217.Activity.startAdvButton;
import static com.example.test_0217.Activity.startScanningButton;
import static com.example.test_0217.Activity.stopAdvButton;
import static com.example.test_0217.Activity.stopScanningButton;
import static com.example.test_0217.Activity.time_bluetooth_off;
import static com.example.test_0217.Activity.time_bluetooth_on;

public class Receiver_BLE extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intentBLE = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        PendingIntent pendingIntent_BLE = PendingIntent.getActivity(context,0,intentBLE,PendingIntent.FLAG_UPDATE_CURRENT);

        final String action = intent.getAction();
        if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(mBluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
            switch(state) {
                case BluetoothAdapter.STATE_TURNING_OFF: //STATE_TURNING_ON
//                    Log.e(TAG,"STATE_OFF");
                    time_bluetooth_off = Calendar.getInstance();
                    Log.e(TAG,"time_bluetooth_off: "+format.format(time_bluetooth_off.getTime()));
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
                    notificationManager.notify(1001, notification);


                    break;
                case BluetoothAdapter.STATE_TURNING_ON: //STATE_TURNING_ON
//                    Log.e(TAG,"STATE_ON");
                    time_bluetooth_on = Calendar.getInstance();
                    Log.e(TAG,"time_bluetooth_on: "+format.format(time_bluetooth_on.getTime()));
                    if(time_bluetooth_off!=null){
//                        Log.e(TAG,"time_bluetooth: " + time_difference_(time_bluetooth_off,time_bluetooth_on)/1000);  // 單位: 秒
                    }
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
