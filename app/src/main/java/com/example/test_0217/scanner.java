package com.example.test_0217;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static com.example.test_0217.MainActivity.list_device;
import static com.example.test_0217.MainActivity.list_device_detail;
import static com.example.test_0217.MainActivity.matrix;
import static com.example.test_0217.MainActivity.mean_total;
import static com.example.test_0217.MainActivity.num_total;
import static com.example.test_0217.MainActivity.peripheralTextView;
import static com.example.test_0217.MainActivity.time_previous;

public class scanner {
    public static ScanCallback leScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            String msg;
            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
            msg="Device Name: " + result.getDevice().getName() +"\n"+ "rssi: " + result.getRssi() +"\n" + "add: " + result.getDevice().getAddress() +"\n"
                    + "time: " + currentTime +"\n" + "getTimestampNanos: " + result.getTimestampNanos() +"\n\n";

            Log.e(TAG,"name: "+result.getDevice().getName() + " result: "+result.toString());
            String address = result.getDevice().getAddress();
            if(!list_device.contains(address)){
                list_device.add(address);
                list_device_detail.add(msg);
            }
//            if(list_device.contains(address)){
//                list_device.set(list_device.indexOf(address),address);
//                list_device_detail.set(list_device.indexOf(address),msg);
//            }
        /*
        -----------|---------------------
        address    |
        -----------|---------------------
        time_pre   |
        -----------|---------------------
        time_now   |
        -----------|---------------------
        interval   |
        -----------|---------------------
        num        |
        -----------|---------------------
        mean_total |
        -----------|---------------------
        mean       |
        -----------|---------------------
        */

            long TimestampMillis = result.getTimestampNanos() / 1000000; //單位:ms

            int index = list_device.indexOf(address);
            Log.e(TAG,"index: "+index);
            int initial = 0;

            if(!matrix.get(0).contains(index)){
                matrix.get(0).add(index);                 //address
                matrix.get(1).add(initial);               //time_pre
                matrix.get(2).add(TimestampMillis);       //time_now
                matrix.get(3).add(initial);               //interval
                matrix.get(4).add(num_total.get(index));  //num
                matrix.get(5).add(mean_total.get(index));                  //mean_total
                matrix.get(6).add(mean_total.get(index)/num_total.get(index));     //mean
                time_previous.set(index,TimestampMillis);
                num_total.set(index,num_total.get(index)+1);
                mean_total.set(index,TimestampMillis-time_previous.get(index));
            }else {
                long interval = TimestampMillis-time_previous.get(index);
                mean_total.set(index,mean_total.get(index)+interval);
                matrix.get(1).set(index,time_previous.get(index));
                matrix.get(2).set(index,TimestampMillis);
                matrix.get(3).set(index,interval);
                matrix.get(4).set(index,num_total.get(index));
                matrix.get(5).set(index,mean_total.get(index));
                matrix.get(6).set(index,mean_total.get(index)/num_total.get(index));
                time_previous.set(index,TimestampMillis);
                num_total.set(index,num_total.get(index)+1);
            }
//            time_previous.set(index,TimestampMillis);
//            num_total.set(index,num_total.get(index)+1);
//            mean_total.set(index,mean_total.get(index)+TimestampMillis-time_previous.get(index));

            Log.e(TAG,"matrix: "+"\n"
                    +matrix.get(0).toString()+"\n"
                    +matrix.get(1).toString()+"\n"
                    +matrix.get(2).toString()+"\n"
                    +matrix.get(3).toString()+"\n"
                    +matrix.get(4).toString()+"\n"
                    +matrix.get(5).toString()+"\n"
                    +matrix.get(6).toString()+"\n");

//            if (num_total>1){
//                Log.e(TAG,"rxTimestampMillis_mean: "+ mean);
//                long time_interval = rxTimestampMillis-time_previous;
//                mean_total=mean_total+time_interval;
//                mean = mean_total/(num_total-1);
//                Log.e(TAG,"rxTimestampMillis_mean: "+ mean );
//            }
//            num_total++;
//
//            Log.e(TAG,"rxTimestampMillis: "+rxTimestampMillis);

            peripheralTextView.append(msg);
            //list_device.add(result.getDevice().getName());
            Log.e(TAG,"list: "+ Arrays.toString(list_device.toArray()));
            Log.e(TAG,msg);

            // auto scroll for text view
            final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0)
                peripheralTextView.scrollTo(0, scrollAmount);
        }
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("onScanFailed: " , String.valueOf(errorCode));
        }
    };
}
