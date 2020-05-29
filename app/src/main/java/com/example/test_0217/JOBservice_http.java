package com.example.test_0217;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.test_0217.DBHelper.TB1;
import static com.example.test_0217.Activity.DH;
import static com.example.test_0217.Activity.TAG;
import static com.example.test_0217.Activity.notificationManager;
import static com.example.test_0217.DBHelper.TB3;

public class JOBservice_http extends JobService {
    OkHttpClient client = new OkHttpClient();

    static ArrayList<String> imei = new ArrayList<>();
    static ArrayList<String> time_1 = new ArrayList<>();
    static ArrayList<String> time_2 = new ArrayList<>();
    static SQLiteDatabase db = DH.getReadableDatabase();

    public JOBservice_http() {
        Log.e(TAG,"JOBservice_http");
    }

    @Override
    public boolean onStartJob(JobParameters params) {

//        Log.e(TAG,"onStartJob");
        Request request = new Request.Builder()
                .url("http://140.114.26.89/user_id")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"OKHTTP"+" 失敗 "+e.getLocalizedMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
//                Log.e(TAG,"response"+response.toString());
//                Log.e(TAG,"response"+response.toString().length());
//                Log.e(TAG,"message"+response.message());
//                Log.e(TAG,"message"+response.message().length());
//                Log.e(TAG,"body"+response.body().contentLength());
//                Log.e(TAG,"OKHTTP: "+json.length());
                parseJSON(json);
            }
        });
        this.jobFinished(params,false);
//        JobScheduler scheduler = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        ComponentName componentName = new ComponentName(this, JOBservice_http.class);
//
//        JobInfo job = new JobInfo.Builder(1, componentName)
//                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//                .setPersisted(true) // 重開機後是否執行
//                .build();
////调用schedule
//        scheduler.schedule(job);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void parseJSON(String s){
        try {
            JSONArray array = new JSONArray(s);
            for (int i = 0 ; i<array.length() ; i++){
                JSONObject obj = array.getJSONObject(i);
                String user_id = obj.getString("user_id");
                String time = obj.getString("time");
//                Log.e(TAG,"user_id: "+user_id);
//                Log.e(TAG,"time: "+time);

                Intent intent1 = new Intent(this,Activity_history.class);
                PendingIntent pendingIntent1 = PendingIntent.getActivity(this,0,intent1,PendingIntent.FLAG_UPDATE_CURRENT);
                ArrayList<Integer> index = new ArrayList<>();
                compare_database(user_id);
                for (int c =0 ; c < imei.size() ; c++){
                    if(imei.get(c).equals(user_id)){
                        index.add(c);
                    }
                }
                if(!index.isEmpty()){
                    String text = "";
                    for (int c = 0;c<index.size();c++){
                        text = text + imei.get(index.get(c)) + ",從" + time_1.get(index.get(c)) + " 到" + time_2.get(index.get(c)) ;
                        if(c<index.size()-1){
                            text = text + "\n";
                        }
                    }

                    NotificationChannel mChannel = new NotificationChannel("Data_infected" , "接觸史" , NotificationManager.IMPORTANCE_HIGH) ;
                    Notification notification = new NotificationCompat.Builder(this,"Data_infected")
                            .setSmallIcon(R.drawable.test)
                            .setContentTitle("曾與確診者接觸")
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                            .setContentText(text)
                            .setWhen(System.currentTimeMillis())
                            .setShowWhen(true)
                            .setContentIntent(pendingIntent1)
//                            .addAction(R.drawable.pig32,"save",pendingIntent1)
                            .build();

                    //TODO 新增activity,查看接觸紀錄,是否會傳
                    mChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(mChannel) ;
                    notificationManager.notify(1, notification);
                }

            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static void compare_database(String IMEI){
        Cursor cursor = db.query(TB1,new String[]{"_id","user_id","time_first","time_last","rssi_level_1","rssi_level_2","rssi_level_3","is_contact"},
                null,null,null,null,null);
        String user_id;
        String time_first;
        String time_last;
        while(cursor.moveToNext()){
            String id = cursor.getString(0);
            user_id = cursor.getString(1);
            time_first = cursor.getString(2);
            time_last = cursor.getString(3);
//            Log.e(TAG,id+". user_id:"+user_id);
            if(user_id.equals(IMEI)){
                update(id);
                Log.e(TAG,"contact IMEI: "+ user_id);
                imei.add(user_id);
                time_1.add(time_first);
                time_2.add(time_last);
            }
        }
        cursor.close();
    }

    private static void update(String id) { // 更新指定的資料
        SQLiteDatabase db = DH.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_contact ", 1);
        db.update(TB1,values,"_id=? " , new String[]{id});
//        show(db);
    }
}
