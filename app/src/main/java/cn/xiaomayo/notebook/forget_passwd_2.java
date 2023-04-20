package cn.xiaomayo.notebook;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class forget_passwd_2 extends Activity {
    private EditText edt_passwd1,edt_passwd2;
    private Button btn_modify;
    private OkHttpClient client;
    private String username;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_passwd_2);

        edt_passwd1=(EditText) findViewById(R.id.modify_passwd);
        edt_passwd2=(EditText) findViewById(R.id.modify_passwd_again);
        btn_modify=(Button) findViewById(R.id.btn_modify2);
        client=new OkHttpClient();
        username=getIntent().getExtras().getString("username");
        System.out.println(username);
        btn_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String passwd1=edt_passwd1.getText().toString();
                String passwd2=edt_passwd2.getText().toString();

                if(passwd1.equals(passwd2)){
                    RequestBody post=new FormBody.Builder()
                            .add("username",username)
                            .add("password",passwd1)
                            .build();

                    Request request=new Request.Builder()
                            .url("https://xiaomayo.cn/api/modify_passwd.php")
                            .post(post)
                            .build();

                    Call call=client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Toast.makeText(forget_passwd_2.this,"网络开小差了....",Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            Toast.makeText(forget_passwd_2.this,"修改成功！",Toast.LENGTH_SHORT).show();
                            forget_passwd_2.this.finish();
                        }
                    });

                }else Toast.makeText(forget_passwd_2.this,"两次密码不一致，请重新输入",Toast.LENGTH_SHORT).show();
            }
        });


    }
}
