package cn.xiaomayo.notebook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    public static Activity mainacti;


    private String username = "";
    private String password = "";
    private int status;

    private Handler login_res_handler;
    private OkHttpClient client;
    private SQLiteDatabase db_read,db_write;
    private SqliteDB DB_user;



    private EditText edt_username,edt_password;
    private Button btn_login;
    private TextView forget_passwd,register;

    private long last_backpressed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainacti = this;

        String sql_create = "CREATE TABLE user(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username text DEFAULT \"\"," +
                "password text DEFAULT \"\"," +
                "status int DEFAULT \"\")";

        DB_user = new SqliteDB(this,"user",null,1,sql_create);

        db_read = DB_user.getReadableDatabase();
        Cursor c = db_read.query("user",new String[]{"username","status"},null,null,null,null,"id asc");
        while (c.moveToNext()){
            String local_username = c.getString(c.getColumnIndex("username"));
            int local_status = c.getInt(c.getColumnIndex("status"));
            if(local_status == 1){
                Intent intent =  new Intent(MainActivity.this,MainWindow.class);
                Bundle data = new Bundle();
                data.putString("username",local_username);
                intent.putExtras(data);
                startActivity(intent);
                db_read.close();
                MainActivity.this.finish();
            }
        }


        edt_username = (EditText)findViewById(R.id.edt_username);
        edt_password = (EditText)findViewById(R.id.edt_password);
        btn_login = (Button)findViewById(R.id.login);
        forget_passwd = (TextView)findViewById(R.id.forget_password);
        register = (TextView)findViewById(R.id.register);


        String login_url = "https://xiaomayo.cn/api/login.php";

        //登录按钮
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {

                username = edt_username.getText().toString();
                password = edt_password.getText().toString();

                if(username.length() ==0 || password.length() == 0){
                    status = -2;
                    login_res_handler.sendMessage(login_res_handler.obtainMessage());
                }else {

                    RequestBody post = new FormBody.Builder()
                            .add("username", username)
                            .add("password", password)
                            .build();

                    client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(login_url)
                            .post(post)
                            .build();

                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            try {
                               // System.out.println(username + " " + password);
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                status = jsonObject.getInt("status");
                                login_res_handler.sendMessage(login_res_handler.obtainMessage());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }
        });


        //注册账号

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,Aty_register.class);
                startActivity(intent);
            }
        });

        //忘记密码
        forget_passwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, forget_passwd.class);
                startActivity(intent);
            }
        });


        login_res_handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                String hint_message = "";
                if(status == -1 || status == 0 || status == -2){
                    if(status == -2) hint_message = "请输入账号和密码！";
                    if(status == -1) hint_message = "账号密码错误，请重新输入！";
                    if(status == 0) hint_message = "服务器内部错误！";
                    new AlertDialog.Builder(MainActivity.this).setMessage(hint_message).setPositiveButton("确定",null).show();
                }else{
                    hint_message = "登录成功！";
                    new AlertDialog.Builder(MainActivity.this).setMessage(hint_message).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ContentValues cv = new ContentValues();
                            cv.put("username",username);
                            cv.put("password",password);
                            cv.put("status",1);
                            db_write = DB_user.getWritableDatabase();
                            db_write.insert("user",null,cv);
                            db_write.close();



                            Intent intent =  new Intent(MainActivity.this,MainWindow.class);
                            Bundle data = new Bundle();
                            data.putString("username",username);
                            intent.putExtras(data);
                            startActivity(intent);
                            MainActivity.this.finish();
                        }
                    }).show();
                }



            }
        };



    }


    public void onBackPressed() {
        if(last_backpressed==0){
            last_backpressed=System.currentTimeMillis();
            Toast.makeText(MainActivity.this,"再按一次退出",Toast.LENGTH_SHORT).show();
        }
        else{
            long current_backpressed=System.currentTimeMillis();
            if(current_backpressed-last_backpressed<=1000) finish();
            else{
                last_backpressed=current_backpressed;
                Toast.makeText(MainActivity.this,"再按一次退出",Toast.LENGTH_SHORT).show();
            }
        }
    }
}


