package com.example.test_0217;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static com.example.test_0217.MainActivity.DH;
import static com.example.test_0217.MainActivity.contact_time_first;
import static com.example.test_0217.MainActivity.contact_time_imei;
import static com.example.test_0217.MainActivity.contact_time_last;
import static com.example.test_0217.MainActivity.contant_time_limit;
import static com.example.test_0217.MainActivity.received_id;
import static com.example.test_0217.MainActivity.sql_Text;
import static com.example.test_0217.Function.byte2HexStr;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static com.example.test_0217.MainActivity.TAG;
import static com.example.test_0217.MainActivity.list_device;
import static com.example.test_0217.MainActivity.list_device_detail;
import static com.example.test_0217.MainActivity.matrix;
import static com.example.test_0217.MainActivity.mean_total;
import static com.example.test_0217.MainActivity.num_total;
import static com.example.test_0217.MainActivity.peripheralTextView;
import static com.example.test_0217.MainActivity.time_previous;
import static com.example.test_0217.Function.hexToAscii;

public class BLE_Scanner {
    static String received_imei;
    static SimpleDateFormat f = new SimpleDateFormat("YYYY-MM-dd HH:mm");





    public static ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result){
            /*----------------------------------------------------------------contact time----------------------------------------------------------------*/
            received_imei = byte2HexStr(result.getScanRecord().getManufacturerSpecificData(0xffff));
            received_imei = hexToAscii(received_imei).subSequence(8,23).toString();
            Calendar a = Calendar.getInstance();
            Log.e(TAG,"Calendar " + f.format(a.getTime()));
            String currentTime = f.format(a.getTime());

            int indexx;
            if(!contact_time_imei.contains(received_imei)){
                contact_time_imei.add(received_imei);
                contact_time_first.add(a);
                contact_time_last.add(a);
            }else {
                indexx = contact_time_imei.indexOf(received_imei);
                if(time_difference(contact_time_last.get(indexx),a)){  //判斷事件是否結束
                    contact_time_last.set(indexx,a);
                    Log.e(TAG,"繼續");
                } else {
                      //是否超過10分鐘
                        if(!time_difference(contact_time_first.get(indexx),contact_time_last.get(indexx))){
                            Log.e(TAG,"事件結束，儲存起來 " + contact_time_imei.get(indexx)+contact_time_first.get(indexx).getTime()+contact_time_last.get(indexx).getTime());
                            add(contact_time_imei.get(indexx),contact_time_first.get(indexx),contact_time_last.get(indexx));
                            received_id.setAction("contact_time_imei.get(i)");
                        }
                        contact_time_imei.remove(indexx);
                        contact_time_first.remove(indexx);
                        contact_time_last.remove(indexx);
                        Log.e(TAG,"事件結束");
                }




                for(int i = 0; i< contact_time_imei.size(); i++){
                    if(!time_difference(contact_time_last.get(i),a)) {  //是否超過10分鐘
                        if(!time_difference(contact_time_first.get(i),contact_time_last.get(i))){
                            Log.e(TAG,"事件結束，儲存起來!!" +contact_time_imei.get(i)+contact_time_first.get(i).getTime()+contact_time_last.get(i).getTime());
                            add(contact_time_imei.get(i),contact_time_first.get(i),contact_time_last.get(i));
                            received_id.setAction("contact_time_imei.get(i)");
                        }
                        contact_time_imei.remove(i);
                        contact_time_first.remove(i);
                        contact_time_last.remove(i);
                        Log.e(TAG,"事件結束!!");
                    }
                }

            }
            Log.e(TAG,"contact_time: "+"\n"
                    +contact_time_imei +"\n"
                    +contact_time_first +"\n"
                    +contact_time_last );


            /*--------------------------------------------------------------------------------------------------------------------------------------------*/


            /*------------------------------------------------------------message-------------------------------------------------------------------------*/
            String msg;

            msg="Device Name: " + result.getDevice().getName() +"\n"+ "rssi: " + result.getRssi() +"\n" + "add: " + result.getDevice().getAddress() +"\n"
                    + "time: " + currentTime +"\n" + "imei: " + received_imei +"\n\n";

            //Log.e(TAG,"name: "+result.getDevice().getName() + " result: "+ result);

            peripheralTextView.append(msg);
            //list_device.add(result.getDevice().getName());
            Log.e(TAG,"list: "+ Arrays.toString(list_device.toArray()));
            Log.e(TAG,msg);

            // auto scroll for text view
            final int scrollAmount = peripheralTextView.getLayout().getLineTop(peripheralTextView.getLineCount()) - peripheralTextView.getHeight();
            // if there is no need to scroll, scrollAmount will be <=0
            if (scrollAmount > 0)
                peripheralTextView.scrollTo(0, scrollAmount);

            /*--------------------------------------------------------------------------------------------------------------------------------------------*/



            /*-------------------------------------------------------interval-----------------------------------------------------------------------------*/

            String address = result.getDevice().getAddress();
            if(!list_device.contains(address)){
                list_device.add(address);
                list_device_detail.add(msg);
            }

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
            long TimestampMillis = result.getTimestampNanos()/1000000; //單位:ms
            int index = list_device.indexOf(address);
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

            SQLiteDatabase db = DH.getReadableDatabase();
            show(db);
//            Log.e(TAG,"matrix: "+"\n"
//                    +matrix.get(0).toString()+"\n"
//                    +matrix.get(1).toString()+"\n"
//                    +matrix.get(2).toString()+"\n"
//                    +matrix.get(3).toString()+"\n"
//                    +matrix.get(4).toString()+"\n"
//                    +matrix.get(5).toString()+"\n"
//                    +matrix.get(6).toString()+"\n");
        }
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d("onScanFailed: " , String.valueOf(errorCode));
        }
    };

    public static boolean time_difference(Calendar first, Calendar last){
        Date first_time = first.getTime();
        Date last_time = last.getTime();

        long different = last_time.getTime() - first_time.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;
        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;
        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;
        long elapsedSeconds = different / secondsInMilli;
        Log.e(TAG,"different: "+elapsedDays +"days, " + elapsedHours + "hours, " + elapsedMinutes +"minutes, " + elapsedSeconds +"seconds. ");

        return elapsedSeconds<contant_time_limit;
    }

    private static void add(String s,Calendar first,Calendar last) {
        SQLiteDatabase db = DH.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id",s);
        values.put("time_first",f.format(first.getTime()));
        values.put("time_last",f.format(last.getTime()));
        db.insert("MYTB",null,values);
        show(db);
    }

    public static void delete(String _id){
        SQLiteDatabase db = DH.getWritableDatabase();
        db.delete("MYTB","_id=?",new String[]{_id});
        show(db);
    }

    public static void show(SQLiteDatabase db){
        Cursor cursor = db.query("MYTB",new String[]{"_id","user_id","time_first","time_last"},
                null,null,null,null,null);

        StringBuilder resultData = new StringBuilder("RESULT: \n");
        while(cursor.moveToNext()){
            int id = cursor.getInt(0);
            String user_id = cursor.getString(1);
            String time_first = cursor.getString(2);
            String time_last = cursor.getString(3);

            resultData.append(id).append(": ");
            resultData.append(user_id).append(", ");
            resultData.append(time_first).append(", ");
            resultData.append(time_last).append(". ");
            resultData.append("\n");
        }
        sql_Text.setText(resultData);
    }



}
