package cn.xiaomayo.notebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Aty_register extends Activity {

    private EditText reg_username,reg_passwd,reg_passwd_again,reg_email,reg_nickname,reg_validation;
    private Button btn_reg,btn_send;

    private Handler reg_handler;
    private OkHttpClient client;


    private  String username,password,password_again,email,nickname,validation,user_validation;

    private int status;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        reg_username = (EditText)findViewById(R.id.reg_username);
        reg_passwd = (EditText)findViewById(R.id.reg_password);
        reg_passwd_again = (EditText)findViewById(R.id.reg_password_again);
        reg_email = (EditText)findViewById(R.id.reg_email);
        reg_nickname = (EditText)findViewById(R.id.reg_nickname);
        reg_validation=(EditText)findViewById(R.id.reg_valiation);
        btn_reg = (Button)findViewById(R.id.register_login);
        btn_send = (Button)findViewById(R.id.send_valiation);


        client = new OkHttpClient();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=reg_email.getText().toString();
                System.out.println(email);
                if(!check_email(email)){
                    System.out.println("NOnoNO");
                    status = -3;
                    reg_handler.sendMessage(reg_handler.obtainMessage());
                }else{
                    System.out.println("yESYEYS");

                    RequestBody post = new FormBody.Builder()
                            .add("Email", email)
                            .build();


                    Request request = new Request.Builder()
                            .url("https://xiaomayo.cn/api/mail_valiation.php")
                            .post(post)
                            .build();

                    Call call=client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response.body().string());
                                validation = jsonObject.getString("valiation");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }
            }
        });


        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = reg_username.getText().toString();
                password = reg_passwd.getText().toString();
                password_again = reg_passwd_again.getText().toString();
                email = reg_email.getText().toString();
                nickname = reg_nickname.getText().toString();
                user_validation=reg_validation.getText().toString();

                System.out.println(password+" "+password_again);

                if(username.length()==0 || password.length()==0 || password_again.length()==0 || email.length()==0 || nickname.length()==0){
                    status = -4;
                    reg_handler.sendMessage(reg_handler.obtainMessage());
                }else if(!password.equals(password_again)){
                    status = -2;   //密码不一致标识
                    reg_passwd.setText("");
                    reg_passwd_again.setText("");
                    reg_handler.sendMessage(reg_handler.obtainMessage());
                }else if(!check_email(email)){
                    status = -3;
                    reg_handler.sendMessage(reg_handler.obtainMessage());
                }else if(!user_validation.equals(validation)){
                    //邮箱验证码错误
                    status=-5;
                    reg_handler.sendMessage(reg_handler.obtainMessage());
                }else{
                    RequestBody post = new FormBody.Builder()
                            .add("username", username)
                            .add("password", password)
                            .add("email", email)
                            .add("nickname", nickname)
                            .build();


                    Request request = new Request.Builder()
                            .url("https://xiaomayo.cn/api/register.php")
                            .post(post)
                            .build();

                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            //
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().string());
                                status = jsonObject.getInt("status");
                                reg_handler.sendMessage(reg_handler.obtainMessage());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            }
        });


        reg_handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if(status == 0)
                    new AlertDialog.Builder(Aty_register.this).setMessage("用户名已存在！").setPositiveButton("确定", null).show();
                if(status == 1) {
                    new AlertDialog.Builder(Aty_register.this).setMessage("注册成功！").setPositiveButton("确定", null).show();
                    Intent intent = new Intent(Aty_register.this,MainWindow.class);
                    Bundle data = new Bundle();
                    data.putString("username",username);
                    intent.putExtras(data);
                    startActivity(intent);
                    Aty_register.this.finish();
                    MainActivity.mainacti.finish();
                }
                if(status == -2)
                    new AlertDialog.Builder(Aty_register.this).setMessage("两次密码不一致！").setPositiveButton("确定",null).show();
                if(status == -3)
                    new AlertDialog.Builder(Aty_register.this).setMessage("请输入正确的邮箱格式！").setPositiveButton("确定",null).show();
                if(status == -4)
                    new AlertDialog.Builder(Aty_register.this).setMessage("请输入完整的信息！").setPositiveButton("确定",null).show();
                if(status == -5)
                    new AlertDialog.Builder(Aty_register.this).setMessage("验证码错误！").setPositiveButton("确定",null).show();

            }
        };


    }


    private boolean check_email(String email){
        int len = email.length();
        boolean appear_at = false,appear_point = false;
        email = email.toLowerCase();
        for(int i = 0;i < len; ++i){
            if(!appear_at && !appear_point){
                if(email.charAt(i)>='a'&&email.charAt(i)<='z' || email.charAt(i)>='0'&&email.charAt(i)<='9') continue;
                else if(email.charAt(i) == '@'){
                    appear_at = true;
                    continue;
                }else return false;
            }

            if(appear_at && !appear_point){
                if(email.charAt(i)>='a'&&email.charAt(i)<='z' || email.charAt(i)>='0'&&email.charAt(i)<='9') continue;
                else if(email.charAt(i) == '.'&&email.charAt(i-1)!='@') {
                    appear_point = true;
                    continue;
                } else return false;
            }

            if(appear_at && appear_point){
                if(email.charAt(i)>='a'&&email.charAt(i)<='z') continue;
                else return false;
            }
        }

        if(appear_at&&appear_point&&email.charAt(email.length()-1)!='.') return true;
        else return false;

    }

}
