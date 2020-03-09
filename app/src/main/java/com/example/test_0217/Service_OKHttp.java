package com.example.test_0217;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
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

import static com.example.test_0217.DBHelper.TB;
import static com.example.test_0217.MainActivity.DH;
import static com.example.test_0217.MainActivity.TAG;
import static com.example.test_0217.MainActivity.notificationManager;

public class Service_OKHttp extends Service {
    OkHttpClient client = new OkHttpClient();
    static ArrayList<Data_infected> infected = new ArrayList<>();
    public Service_OKHttp() {
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



        stopSelf();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void parseJSON(String s){
        try {
            JSONArray array = new JSONArray(s);
            for (int i = 0 ; i<array.length() ; i++){
                JSONObject obj = array.getJSONObject(i);
                String user_id = obj.getString("user_id");
                //Log.e(TAG,"JSON: "+user_id);
                Data_infected t =new Data_infected(user_id);
                infected.add(t);
                Log.e(TAG,"someMethod: " + someMethod(user_id));
                NotificationChannel mChannel = new NotificationChannel("Data_infected" , "接觸史" , NotificationManager.IMPORTANCE_HIGH ) ;
                Notification notification = new Notification.Builder(this,"Data_infected")
                        .setSmallIcon(R.drawable.ble)
                        .setContentTitle("application")
                        .setContentText("曾與"+user_id)
                        .setWhen(System.currentTimeMillis())
                        .build();

                notification.flags |= Notification.FLAG_ONGOING_EVENT;
                mChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                if(someMethod(user_id)){
                    notificationManager.createNotificationChannel(mChannel) ;
                    notificationManager.notify(3, notification);
                }
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "start onDestroy~~~");
    }

    private boolean someMethod(String s) {
        SQLiteDatabase db = DH.getReadableDatabase();
        String sql = "select count(*) from " + TB + " where "
                + "user_id" + " = " + DatabaseUtils.sqlEscapeString(s);
        SQLiteStatement statement = db.compileStatement(sql);

        try {
            return statement.simpleQueryForLong() > 0;
        } finally {
            statement.close();
        }
    }

}
