package com.example.test_0217;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.test_0217.Activity.DH;
import static com.example.test_0217.Activity.TAG;
import static com.example.test_0217.Activity.mDeviceIMEI;
import static com.example.test_0217.DBHelper.TB1;
import static com.example.test_0217.JOBservice_http.db;

public class Activity_history extends AppCompatActivity {

    static ListView SQL_list;


    Button btn_show;
    static final ArrayList<String> check_list_id = new ArrayList<>();
    static final ArrayList<Boolean> check_list_ = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Log.e(TAG,"Activity_history");
        check_list_id.clear();
        check_list_.clear();

        SQL_list = findViewById(R.id.SQL_list);
        btn_show = findViewById(R.id.btn_show);
        show(db);

        listview();
        botton();

    }

    private void botton(){
        final String arr[] = new String[check_list_id.size()];
        final boolean boo[] = new boolean[check_list_.size()];
        for(int i=0 ; i< check_list_id.size();i++){
            arr[i] = check_list_id.get(i);
            boo[i] = check_list_.get(i);
            //getProductName or any suitable method
        }
        btn_show.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(Activity_history.this);
                dialog.setTitle("接觸史");
                dialog.setMultiChoiceItems(arr, boo, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        boo[which] =isChecked;
                        Toast.makeText(Activity_history.this , display(db , arr[which]),Toast.LENGTH_SHORT).show();
//                        Log.e(TAG,"onClick: "+ display(db , arr[which]));
                    }
                });
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i = 0; i<boo.length; i++){
                            if(boo[i]){
                                http_header(arr[i]);
                                update(arr[i]);
//                                show(db);
                            }
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    private void listview(){
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                check_list_id );
        SQL_list.setAdapter(arrayAdapter);
        SQL_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(Activity_history.this , display(db , check_list_id.get(position)),Toast.LENGTH_SHORT).show();

            }
        });

    }

    private static void show(SQLiteDatabase db) {
        Cursor cursor = db.query(TB1,new String[]{"_id","user_id","time_first","time_last","rssi_level_1","rssi_level_2","rssi_level_3","is_contact"},
                null,null,null,null,null);

        StringBuilder resultData = new StringBuilder("RESULT: \n");
        while(cursor.moveToNext()){
            String id = cursor.getString(0);
            String user_id = cursor.getString(1);
            String time_first = cursor.getString(2);
            String time_last = cursor.getString(3);
            int rssi_1 = cursor.getInt(4);
            int rssi_2 = cursor.getInt(5);
            int rssi_3 = cursor.getInt(6);
            int is_contact = cursor.getInt(7);

            if(is_contact!=0){
                check_list_id.add(id);
                check_list_.add(false);
                resultData.append(id).append(": ");
                resultData.append(user_id).append("\n ");
                resultData.append(time_first).append(", ");
                resultData.append(time_last).append("\n");
                resultData.append("RSSI level: ").append(rssi_1).append(", ");
                resultData.append(rssi_2).append(", ");
                resultData.append(rssi_3).append("\n ");
                resultData.append("is check: ").append(is_contact);
                resultData.append("\n");
            }
        }
//        SQL_text.setText(resultData);
//        SQL_text.setMovementMethod(new ScrollingMovementMethod()); //垂直滾動

        cursor.close();
    }

    private static String display(SQLiteDatabase db,String ID) {
        Cursor cursor = db.query(TB1,new String[]{"_id","user_id","time_first","time_last","rssi_level_1","rssi_level_2","rssi_level_3","is_contact"},
                null,null,null,null,null);
        String data = ID;
        String check="";
        while(cursor.moveToNext()){
            String id = cursor.getString(0);
            String user_id = cursor.getString(1);
            String time_first = cursor.getString(2);
            String time_last = cursor.getString(3);
            int rssi_1 = cursor.getInt(4);
            int rssi_2 = cursor.getInt(5);
            int rssi_3 = cursor.getInt(6);
            int is_contact = cursor.getInt(7);

            if (is_contact==1){
                check = "had contacted";
            }else if(is_contact==2) {
                check = "had returned to server";
            }

            if(ID.equals(id)){
                data =   "ID: " + data + "\n" +
                        "USER ID: "+ user_id + "\n" +
                        "TIME: "+time_first +" ~ " + time_last + "\n" +
                        "RSSI: " + rssi_1 + "," + rssi_2 + "," + rssi_3 +  "\n" +
                        "STATUS: "+ check;
            }
        }
        cursor.close();
        return data;
    }

    private void update(String id) { // 更新指定的資料
        SQLiteDatabase db = DH.getReadableDatabase();
        ContentValues values = new ContentValues();

        values.put("is_contact ", 2);
        db.update(TB1,values,"_id=? " , new String[]{id});
        show(db);
    }

    private void http_header(final String id){
//        Log.e(TAG,"http_header:" + id);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://140.114.26.89/")
                .header("userid",mDeviceIMEI)
                .header("contactid", get_detail(db,id,0))
                .header("starttime",get_detail(db,id,1))
                .header("endtime",get_detail(db,id,2))
                .header("rssi",get_detail(db,id,3))
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"http_header" + " 失敗" + e.getLocalizedMessage());
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
//                Log.e(TAG,"http_header result" + json);
                Log.e(TAG,"http_header 成功 "+id);
            }
        });
    }

    private static String get_detail(SQLiteDatabase db,String ID,int x) {
        //x=0 他人的id; x=1 starttime; x=2 endtime; x=3 rssi
        Cursor cursor = db.query(TB1,new String[]{"_id","user_id","time_first","time_last","rssi_level_1","rssi_level_2","rssi_level_3","is_contact"},
                null,null,null,null,null);
        String data = "";
        while(cursor.moveToNext()){
            String id = cursor.getString(0);
            String user_id = cursor.getString(1);
            String time_first = cursor.getString(2);
            String time_last = cursor.getString(3);
            String rssi_1 = cursor.getString(4);
            String rssi_2 = cursor.getString(5);
            String rssi_3 = cursor.getString(6);

            if(ID.equals(id)){
                switch (x){
                    case 0:
                        data=user_id;
                        break;
                    case 1:
                        data = time_first;
                        break;
                    case 2:
                        data = time_last;
                        break;
                    case 3:
                        data = rssi_1 + " , " +rssi_2 + " , " + rssi_3 ;
                }

            }
        }
        cursor.close();
        return data;
    }

}
