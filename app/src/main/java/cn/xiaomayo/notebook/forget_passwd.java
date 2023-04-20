package cn.xiaomayo.notebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

public class forget_passwd extends Activity {

    private Button btn_send,btn_submit;
    private EditText edt_username,edt_validation;
    private String username=null,email,validation=null;

    private OkHttpClient client;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_passwd);

        btn_send=(Button) findViewById(R.id.send_find_valiation);
        btn_submit=(Button) findViewById(R.id.btn_modify);
        edt_username=(EditText) findViewById(R.id.find_username);
        edt_validation=(EditText) findViewById(R.id.find_valiation);
        client=new OkHttpClient();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username=edt_username.getText().toString();
                RequestBody post = new FormBody.Builder()
                        .add("username",username)
                        .build();

                Request request = new Request.Builder()
                        .url("https://xiaomayo.cn/api/modify_passwd.php")
                        .post(post)
                        .build();

                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {

                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body().string());
                            email=jsonObject.getString("email");
                            System.out.println(email);

                            RequestBody post = new FormBody.Builder()
                                    .add("Email",email)
                                    .build();

                            Request request=new Request.Builder()
                                    .url("https://xiaomayo.cn/api/mail_valiation.php")
                                    .post(post)
                                    .build();
                            call=client.newCall(request);
                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body().string());
                                        validation = jsonObject.getString("valiation");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });


        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user_validation = edt_validation.getText().toString();
                if(username==null||validation==null){
                    Toast.makeText(forget_passwd.this,"请输入完整！",Toast.LENGTH_SHORT).show();
                }else if(!user_validation.equals(validation)){
                    Toast.makeText(forget_passwd.this,"验证码错误...",Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent=new Intent(forget_passwd.this,forget_passwd_2.class);
                    Bundle bundle=new Bundle();
                    bundle.putString("username",username);
                    intent.putExtras(bundle);
                    startActivity(intent);
                    forget_passwd.this.finish();
                }
            }
        });


    }
}
