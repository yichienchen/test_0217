package com.example.test_0217;

import android.app.Service;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY;
import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_POWER;
import static com.example.test_0217.BLE_Scanner.received_time;
import static com.example.test_0217.BLE_Scanner.received_time_Calendar;
import static com.example.test_0217.BLE_Scanner.received_time_interval;
import static com.example.test_0217.BLE_Scanner.rssi_level_1;
import static com.example.test_0217.BLE_Scanner.rssi_level_2;
import static com.example.test_0217.BLE_Scanner.rssi_level_3;
import static com.example.test_0217.MainActivity.ManufacturerData_size;
import static com.example.test_0217.MainActivity.TAG;
import static com.example.test_0217.MainActivity.id_byte;
import static com.example.test_0217.MainActivity.list_device;
import static com.example.test_0217.MainActivity.list_device_detail;
import static com.example.test_0217.MainActivity.mBluetoothLeScanner;
import static com.example.test_0217.MainActivity.matrix;
import static com.example.test_0217.MainActivity.mean_total;
import static com.example.test_0217.MainActivity.num_total;
import static com.example.test_0217.MainActivity.peripheralTextView;
import static com.example.test_0217.MainActivity.scan_mode;
import static com.example.test_0217.MainActivity.startScanningButton;
import static com.example.test_0217.MainActivity.stopScanningButton;
import static com.example.test_0217.MainActivity.time_previous;
import static com.example.test_0217.Function.byte2HexStr;
import static com.example.test_0217.BLE_Scanner.leScanCallback;

public class Service_Scan extends Service {

    public Service_Scan() {
        Log.e(TAG,"Service_Scan start");
        startScanning();
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
                stopSelf();
            }
        });
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void startScanning() {
        received_time.clear();
        received_time_interval.clear();
        received_time_Calendar.clear();

        Log.e(TAG,"start scanning");


        list_device.clear();
        list_device_detail.clear();

        num_total.clear();
        time_previous.clear();
        mean_total.clear();
        matrix.clear();

        long zero=0;
        for (int j=0;j<100;j++){  //100 : mac address數量上限
            num_total.add(1);
            time_previous.add(zero);
            mean_total.add(zero);
        }

        //add six row
        matrix.add(new ArrayList<>());
        matrix.add(new ArrayList<>());
        matrix.add(new ArrayList<>());
        matrix.add(new ArrayList<>());
        matrix.add(new ArrayList<>());
        matrix.add(new ArrayList<>());
        matrix.add(new ArrayList<>());


        peripheralTextView.setText("");
        startScanningButton.setVisibility(View.INVISIBLE);
        stopScanningButton.setVisibility(View.VISIBLE);

        StringBuilder data = new StringBuilder("0");
        for(int j=data.length();(j+id_byte.length)%ManufacturerData_size!=0;j++){
            data.append("0");
        }

        byte[] data_all = new byte[id_byte.length + data.toString().getBytes().length];
        System.arraycopy(id_byte, 0, data_all, 1, id_byte.length);
        System.arraycopy(data.toString().getBytes(), 0, data_all, id_byte.length, data.toString().getBytes().length);
        // ManufacturerData : packet編號(1) + id(4) + data(19)

        byte[] data_mask = new byte[] {0x00,0x11,0x11,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
//        Log.e(TAG,"data_all: "+ byte2HexStr(data_all)+"\n"
//                +"data_mask: "+byte2HexStr(data_mask));
        ScanFilter UUID_Filter_M = new ScanFilter.Builder().setManufacturerData(0xffff,data_all,data_mask).build();
        ArrayList<ScanFilter> filters = new ArrayList<>();
        filters.add(UUID_Filter_M);


        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(scan_mode)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
//                .setCallbackType(ScanSettings.CALLBACK_TYPE_MATCH_LOST)  //Fails to start power optimized scan as this feature is not supported
//                .setMatchMode()
//                .setNumOfMatches()
//                .setReportDelay()
                .build();
//        btScanner.flushPendingScanResults(leScanCallback);
        mBluetoothLeScanner.startScan(filters, settings, leScanCallback);
    }

    public void stopScanning() {
        Log.e(TAG,"stopping scanning");
        peripheralTextView.append("Stopped Scanning");
        startScanningButton.setVisibility(View.VISIBLE);
        stopScanningButton.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mBluetoothLeScanner.stopScan(leScanCallback);
            }
        });
    }
}
