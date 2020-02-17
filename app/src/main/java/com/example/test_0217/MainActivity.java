package com.example.test_0217;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.AdvertisingSetParameters;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.PeriodicAdvertisingParameters;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


import static android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_POWER;
import static com.example.test_0217.advertiser.buildAdvertiseData;
import static com.example.test_0217.advertiser.buildAdvertiseData_extended;
import static com.example.test_0217.advertiser.buildAdvertiseData_periodicData;
import static com.example.test_0217.advertiser.buildAdvertiseData_scan_response;
import static com.example.test_0217.advertiser.buildAdvertiseSettings;
import static com.example.test_0217.advertiser.buildAdvertisingSetParameters;
import static com.example.test_0217.advertiser.buildperiodicParameters;
import static com.example.test_0217.advertiser.data_seg;
import static com.example.test_0217.function.byte2HexStr;
import static com.example.test_0217.scanner.leScanCallback;

/*
scanner(只包含scan 4.0) 和 advertiser(4.0 or 5.0)
 */

public class MainActivity extends AppCompatActivity {
    static int ManufacturerData_size = 24;  //ManufacturerData長度
    static String TAG = "chien";

    static String Data = "CHENYICHIENCHENYI123456sdbjfksdfjsbvjkabksdafs";
    boolean version = true;  //true: 4.0 , false:5.0
    static byte[][] adv_seg_packet;
    static int x;
    static byte[] id_byte = new byte[] {0x22,0x6c,0x74,0x52,0x04a,0x5f,0x2d};
    static int pdu_size;  //純data，不包含id跟manufacturer specific data的flags及第幾個packet


    static List<String> list_device = new ArrayList<>();
    static List<String> list_device_detail = new ArrayList<>();

    static ArrayList<ArrayList<Object>> matrix = new ArrayList<>();
    static ArrayList<Integer> num_total = new ArrayList<>();
    static ArrayList<Long> time_previous = new ArrayList<>();
    static ArrayList<Long> mean_total = new ArrayList<>();

    static Map<Integer, AdvertiseCallback> AdvertiseCallbacks_map;
    static Map<Integer, AdvertisingSetCallback> extendedAdvertiseCallbacks_map;
    //static Map<String , Long> saved_contact_list = new HashMap<>();


    static BluetoothManager mBluetoothManager;
    static BluetoothAdapter mBluetoothAdapter;
    static BluetoothLeScanner mBluetoothLeScanner;
    static AdvertiseCallback mAdvertiseCallback;
    static BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    Button startScanningButton;
    Button stopScanningButton;
    Button scan_list;
    Button startAdvButton;
    Button stopAdvButton;
    static TextView peripheralTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        permission();

        startScanningButton = findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });

        stopScanningButton = findViewById(R.id.StopScanButton);
        stopScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });
        stopScanningButton.setVisibility(View.INVISIBLE);

        scan_list = findViewById(R.id.scan_list);
        scan_list.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v.getId()==R.id.scan_list){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("device list")
                            .setItems(list_device.toArray(new String[0]) , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String list = list_device_detail.get(which);
                                    //Log.d("which",String.valueOf(which));
                                    Toast.makeText(getApplicationContext(), list , Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setPositiveButton("close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
            }

        });

        startAdvButton = findViewById(R.id.StartAdvButton);
        startAdvButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startAdvertising();
            }
        });

        stopAdvButton = findViewById(R.id.StopAdvButton);
        stopAdvButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopAdvertising();
            }
        });
        stopAdvButton.setVisibility(View.INVISIBLE);

        peripheralTextView = findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod()); //垂直滾動

        AdvertiseCallbacks_map = new TreeMap<>();
        extendedAdvertiseCallbacks_map = new TreeMap<>();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy() called");
        stopAdvertising();
        stopScanning();
    }

    private void initialize() {
        if (mBluetoothLeScanner == null) {
            mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager != null) {
                BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
                if (bluetoothAdapter != null) {
                    mBluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                }
            }
        }
        if (mBluetoothLeAdvertiser == null) {
            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null) {
                BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
                if (bluetoothAdapter != null) {
                    mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
                }
            }
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void startScanning() {
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

        byte[] data_mask = new byte[] {0x00,0x11,0x11,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        Log.e(TAG,"data_all: "+ byte2HexStr(data_all)+"\n"
                +"data_mask: "+byte2HexStr(data_mask));
        ScanFilter UUID_Filter_M = new ScanFilter.Builder().setManufacturerData(0xffff,data_all,data_mask).build();
        ArrayList<ScanFilter> filters = new ArrayList<>();
        filters.add(UUID_Filter_M);


        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(SCAN_MODE_LOW_POWER)
//                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
//                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)  //Fails to start power optimized scan as this feature is not supported
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

    public void startAdvertising(){
        Log.e(TAG, "Service: Starting Advertising");
        if (!version) {
            pdu_size = 255-3-4-1-id_byte.length;
            adv_seg_packet=data_seg(255);
        }else {
            pdu_size = 31-3-4-1-id_byte.length;
            adv_seg_packet = data_seg(31);
        }
        x=(Data.length()/pdu_size)+1;
        if (mAdvertiseCallback == null) {
            if (mBluetoothLeAdvertiser != null) {
                for (int q=1;q<x;q++){
                    startBroadcast(q);
                }
            }
        }
        startAdvButton.setVisibility(View.INVISIBLE);
        stopAdvButton.setVisibility(View.VISIBLE);
    }

    public void stopAdvertising(){
        if (mBluetoothLeAdvertiser != null) {
            for (int q=1;q<x;q++){
                stopBroadcast(q);
            }
            mAdvertiseCallback = null;
        }
        stopAdvButton.setVisibility(View.INVISIBLE);
        startAdvButton.setVisibility(View.VISIBLE);
    }

    private void startBroadcast(Integer order) {
        String localName =  String.valueOf(order) ;
        BluetoothAdapter.getDefaultAdapter().setName(localName);

        //BLE4.0
        if (version) {
            AdvertiseSettings settings = buildAdvertiseSettings();
            AdvertiseData advertiseData = buildAdvertiseData(order);
            AdvertiseData scanResponse = buildAdvertiseData_scan_response(order);
            mBluetoothLeAdvertiser.startAdvertising(settings, advertiseData, new advertiser.MyAdvertiseCallback(order));  //包含 scan response  BLE4.0
        } else {
            //BLE 5.0
            AdvertiseData advertiseData_extended = buildAdvertiseData_extended();
            AdvertiseData periodicData = buildAdvertiseData_periodicData();
            AdvertisingSetParameters parameters = buildAdvertisingSetParameters();
            PeriodicAdvertisingParameters periodicParameters = buildperiodicParameters();
            mBluetoothLeAdvertiser.startAdvertisingSet(parameters,advertiseData_extended,null,
                    null,null,0,0,new advertiser.ExtendedAdvertiseCallback(order));
        }

    }

    private void stopBroadcast(Integer order) {
        final AdvertiseCallback adCallback = AdvertiseCallbacks_map.get(order);
        final AdvertisingSetCallback exadCallback = extendedAdvertiseCallbacks_map.get(order);
        if (!version) {
            //BLE 5.0
            if (exadCallback != null) {
                try {
                    if (mBluetoothLeAdvertiser != null) {
                        mBluetoothLeAdvertiser.stopAdvertisingSet(exadCallback);
                    }
                    else {
                        Log.w(TAG,"Not able to stop broadcast; mBtAdvertiser is null");
                    }
                }
                catch(RuntimeException e) { // Can happen if BT adapter is not in ON state
                    Log.w(TAG,"Not able to stop broadcast; BT state: {}");
                }
                AdvertiseCallbacks_map.remove(order);
            }
            //Log.e(TAG,order +" Advertising successfully stopped.");
        }else {
            //BLE 4.0
            if (adCallback != null) {
                try {
                    if (mBluetoothLeAdvertiser != null) {
                        mBluetoothLeAdvertiser.stopAdvertising(adCallback);
                    }
                    else {
                        Log.w(TAG,"Not able to stop broadcast; mBtAdvertiser is null");
                    }
                }
                catch(RuntimeException e) { // Can happen if BT adapter is not in ON state
                    Log.w(TAG,"Not able to stop broadcast; BT state: {}");
                }
                AdvertiseCallbacks_map.remove(order);
            }
            Log.e(TAG,order +" Advertising successfully stopped");
        }
    }



    public void permission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
    }

    public void contact_time(){

    }
}

