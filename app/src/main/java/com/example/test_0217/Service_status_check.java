package com.example.test_0217;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.example.test_0217.DBHelper.TB3;
import static com.example.test_0217.MainActivity.DH;
import static com.example.test_0217.MainActivity.TAG;


public class Service_status_check extends JobService {
    public Service_status_check() {
//        Log.e(TAG,"Service_status_check");
    }
    static SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd,HH:mm");
    @Override
    public boolean onStartJob(JobParameters params) {
        Calendar a = Calendar.getInstance();
        String time = format.format(a.getTime());
        add(time);
        this.jobFinished(params,true);
        JobScheduler scheduler = (JobScheduler)getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName1 = new ComponentName(this, Service_status_check.class);
        JobInfo job1 = new JobInfo.Builder(1001, componentName1)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true) // 重開機後是否執行
                .setMinimumLatency(1000*60*5)
                .build();
//调用schedule
        scheduler.schedule(job1);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    public void add(String time) {
        SQLiteDatabase db = DH.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("time",time);
        db.insert(TB3,null,values);
//        show(db);
    }

    public static void show(SQLiteDatabase db){
        Cursor cursor = db.query(TB3,new String[]{"time"},
                null,null,null,null,null);
        while(cursor.moveToNext()){
            String time = cursor.getString(0);

            Log.e(TAG,"TB3: "+ time );
        }
        cursor.close();
    }

    public static void clearTable3(){
        SQLiteDatabase db = DH.getWritableDatabase();
        db.execSQL("delete from " + TB3);
        show(db);
    }
}
