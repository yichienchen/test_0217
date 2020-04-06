package com.example.test_0217;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.IBinder;
import android.util.Log;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.test_0217.DBHelper.TB1;
import static com.example.test_0217.MainActivity.DH;
import static com.example.test_0217.MainActivity.TAG;
import static com.example.test_0217.MainActivity.notificationManager;
import static com.example.test_0217.MainActivity.sql_Text;

public class Service_http extends JobService {
    OkHttpClient client = new OkHttpClient();
    static ArrayList<Data_infected> infected = new ArrayList<>();

    static ArrayList<String> imei = new ArrayList<>();
    static ArrayList<String> time_1 = new ArrayList<>();
    static ArrayList<String> time_2 = new ArrayList<>();

    public Service_http() {
//        Log.e(TAG,"Service_http");
    }

    @Override
    public boolean onStartJob(JobParameters params) {
//        Log.e(TAG,"onStartJob");
        Request request = new Request.Builder()
                .url("http://140.114.26.89")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.e(TAG,"OKHTTP"+" 失敗");
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String json = response.body().string();
                //Log.e(TAG,"OKHTTP"+json);
                parseJSON(json);
            }
        });
        this.jobFinished(params,true);
        JobScheduler scheduler = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(this, Service_http.class);

        JobInfo job = new JobInfo.Builder(1000, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true) // 重開機後是否執行
                .setMinimumLatency(1000*60*15)
//                .setPeriodic(1000*10)
                .build();
//调用schedule
        scheduler.schedule(job);
        imei.clear();
        time_1.clear();
        time_2.clear();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
//        Log.e(TAG,"onStopJob");
        return true;
    }

    private void parseJSON(String s){
        try {
            JSONArray array = new JSONArray(s);
            for (int i = 0 ; i<array.length() ; i++){
                JSONObject obj = array.getJSONObject(i);
                String user_id = obj.getString("user_id");
//                Log.e(TAG,"JSON: "+user_id);
                Data_infected t =new Data_infected(user_id);
                infected.add(t);
//                Log.e(TAG,user_id+" -someMethod: " + someMethod(user_id));
                time_diff(user_id);
                for (int c =0 ; c < imei.size() ; c++){
                    if(imei.get(c).equals(user_id)){
                        NotificationChannel mChannel = new NotificationChannel("Data_infected" , "接觸史" , NotificationManager.IMPORTANCE_HIGH) ;
                        Notification notification = new Notification.Builder(this,"Data_infected")
                                .setSmallIcon(R.drawable.ble)
                                .setContentTitle("曾與"+imei.get(c)+"接觸")
                                .setContentText("從 "+time_1.get(c)+" 到 "+time_2.get(c))
                                .setWhen(System.currentTimeMillis())
                                .build();
                        mChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                        notificationManager.createNotificationChannel(mChannel) ;
                        notificationManager.notify(c, notification);
                    }
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private boolean someMethod(String s) {
        SQLiteDatabase db = DH.getReadableDatabase();
        String sql = "select count(*) from " + TB1 + " where "
                + "user_id" + " = " + DatabaseUtils.sqlEscapeString(s);
        SQLiteStatement statement = db.compileStatement(sql);

        try {
            return statement.simpleQueryForLong() > 0;
        } finally {
            statement.close();
        }
    }

    private static void time_diff(String IMEI){
        SQLiteDatabase db = DH.getReadableDatabase();
        Cursor cursor = db.query(TB1,new String[]{"_id","user_id","time_first","time_last","rssi_level_1","rssi_level_2","rssi_level_3"},
                null,null,null,null,null);
        String user_id;
        String time_first;
        String time_last;
//        cursor.moveToFirst();

        while(cursor.moveToNext()){
            int id = cursor.getInt(0);
            user_id = cursor.getString(1);
            time_first = cursor.getString(2);
            time_last = cursor.getString(3);
//            Log.e(TAG,id+". user_id:"+user_id);
            if(user_id.equals(IMEI)){
                Log.e(TAG,"contact IMEI: "+ user_id);
                imei.add(user_id);
                time_1.add(time_first);
                time_2.add(time_last);
            }
        }
        cursor.close();
    }


}
