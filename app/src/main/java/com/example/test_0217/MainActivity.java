package com.example.test_0217;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertisingSetCallback;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*
BLE_Scanner(只包含scan 4.0) 和 BLE_Adv(4.0 or 5.0)
 */

public class MainActivity extends AppCompatActivity {
    static int ManufacturerData_size = 24 - 3;  //ManufacturerData長度
    static String TAG = "chien";
    static long contant_time_limit = 15;
    static int num_of_id;

    //static String Data_adv = "CHENYICHIENCHENYI123456sdbjfksdfjsbvjkabksdafs";
    static String Data_adv;
    static boolean version = true;  //true: 4.0 , false:5.0
    static byte[][] adv_seg_packet;
    static int x;
    static byte[] id_byte = new byte[]{0x22, 0x6c, 0x74, 0x52, 0x04a, 0x5f, 0x2d};
    static int pdu_size;  //純data，不包含id跟manufacturer specific data的flags及第幾個packet


    static List<String> list_device = new ArrayList<>();
    static List<String> list_device_detail = new ArrayList<>();


    static ArrayList<ArrayList<Object>> matrix = new ArrayList<>();
    static ArrayList<Integer> num_total = new ArrayList<>();
    static ArrayList<Long> time_previous = new ArrayList<>();
    static ArrayList<Long> mean_total = new ArrayList<>();

    static ArrayList<String> contact_time_imei = new ArrayList<>();
    static ArrayList<Calendar> contact_time_first = new ArrayList<>();
    static ArrayList<Calendar> contact_time_last = new ArrayList<>();


    static Map<Integer, AdvertiseCallback> AdvertiseCallbacks_map;
    static Map<Integer, AdvertisingSetCallback> extendedAdvertiseCallbacks_map;
    //static Map<String , Long> saved_contact_list = new HashMap<>();


    static BluetoothManager mBluetoothManager;
    static BluetoothAdapter mBluetoothAdapter;
    static BluetoothLeScanner mBluetoothLeScanner;
    static AdvertiseCallback mAdvertiseCallback;
    static BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    static Button startScanningButton;
    static Button stopScanningButton;
    static Button scan_list;
    static Button startAdvButton;
    static Button stopAdvButton;
    static TextView peripheralTextView;
    static TextView sql_Text;

    private Receiver_BLE mBLEReceiver;
    private Receiver_contact_history mcontact_history_ReceiverContacthistory;

    static NotificationManager notificationManager;
    static NotificationChannel mChannel;
    Intent intentMainActivity;
    static PendingIntent pendingIntent;
    Notification notification;
    static IntentFilter filter2;
    static Intent received_id;

    Intent adv_service;
    Intent scan_service;
    Intent OKHTTP;
    static ArrayList<String> infected_id = new ArrayList<>();

    public static String mDeviceIMEI = "0";
    TelephonyManager mTelephonyManager = null;

    public static DBHelper DH=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        permission();
        element();
        notice();


        DH = new DBHelper(this,"MYDB",null,2);


        Log.e(TAG,"filter1: "+ Service_OKHttp.ACCESSIBILITY_SERVICE);
        getDeviceImei();
        startService(OKHTTP);


        Log.e(TAG,"mDeviceIMEI: "+mDeviceIMEI);
        Data_adv = mDeviceIMEI;

        //Log.e(TAG,"filter2: "+ Service_OKHttp.ACCESSIBILITY_SERVICE );
        filter2 = new IntentFilter();

        mcontact_history_ReceiverContacthistory = new Receiver_contact_history();
        registerReceiver(mcontact_history_ReceiverContacthistory,filter2);
    }

    @Override
    public void onDestroy() {
        notificationManager.notify(1, notification);
        unregisterReceiver(mBLEReceiver);
        unregisterReceiver(mcontact_history_ReceiverContacthistory);
        stopService(adv_service);
        stopService(scan_service);
        stopService(OKHTTP);

        super.onDestroy();
        Log.e(TAG, "onDestroy() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.e(TAG, "onResume() called");
        permission();
    }

    private void initialize() {
        if (mBluetoothLeScanner == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
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

    public void permission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 1);
        }
    }

    private void element() {
        /*---------------------------------------scan-----------------------------------------*/
        startScanningButton = findViewById(R.id.StartScanButton);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startService(scan_service);
            }
        });
        stopScanningButton = findViewById(R.id.StopScanButton);
        stopScanningButton.setVisibility(View.INVISIBLE);
        scan_list = findViewById(R.id.scan_list);
        scan_list.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (v.getId() == R.id.scan_list) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("device list")
                            .setItems(list_device.toArray(new String[0]), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String list = list_device_detail.get(which);
                                    //Log.d("which",String.valueOf(which));
                                    Toast.makeText(getApplicationContext(), list, Toast.LENGTH_SHORT).show();
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

        /*--------------------------------------advertise----------------------------------------*/
        startAdvButton = findViewById(R.id.StartAdvButton);
        startAdvButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startService(adv_service);
            }
        });
        stopAdvButton = findViewById(R.id.StopAdvButton);
        stopAdvButton.setVisibility(View.INVISIBLE);

        /*--------------------------------------intent----------------------------------------*/
        adv_service = new Intent(MainActivity.this, Service_Adv.class);
        scan_service = new Intent(MainActivity.this, Service_Scan.class);
        OKHTTP = new Intent(MainActivity.this, Service_OKHttp.class);

        /*-------------------------------------Receiver---------------------------------------*/
        received_id = new Intent();

        /*--------------------------------------others----------------------------------------*/
        peripheralTextView = findViewById(R.id.PeripheralTextView);
        peripheralTextView.setMovementMethod(new ScrollingMovementMethod()); //垂直滾動
        AdvertiseCallbacks_map = new TreeMap<>();
        extendedAdvertiseCallbacks_map = new TreeMap<>();
    }

    public void notice() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mChannel = new NotificationChannel("MainActivity", "主畫面", NotificationManager.IMPORTANCE_HIGH);
        intentMainActivity = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, intentMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new Notification.Builder(this, "MainActivity")
                .setSmallIcon(R.drawable.ble)
                .setContentTitle("application")
                .setContentText("test_0217已關閉 請開啟test_0217")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .build();
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        mChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(mChannel);
        notificationManager.cancel(1);

        IntentFilter filter1 = new IntentFilter(mBluetoothAdapter.ACTION_STATE_CHANGED);
        mBLEReceiver = new Receiver_BLE();
        registerReceiver(mBLEReceiver, filter1);

        sql_Text = findViewById(R.id.sql_Text);
    }

    private void getDeviceImei() {
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.e(TAG, "mTelephonyManager: "+mTelephonyManager.getLine1Number());
        String IMSI = mTelephonyManager.getSubscriberId(); //getSimSerialNumber
        String phone_number = mTelephonyManager.getLine1Number();
        Log.e(TAG, "IMSI: "+IMSI);
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                mDeviceIMEI = mTelephonyManager.getImei();
            } else {
                mDeviceIMEI = mTelephonyManager.getDeviceId();
            }
        } catch (SecurityException e) {
            // expected
            if (Build.VERSION.SDK_INT >= 26) {
                Log.d(TAG, "SecurityException e");
            }
        }
    }




}

