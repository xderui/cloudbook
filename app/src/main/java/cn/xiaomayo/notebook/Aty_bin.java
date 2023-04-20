package cn.xiaomayo.notebook;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
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

public class Aty_bin extends Activity {

    private GridView gridView;
    private MainWindowList_Adapter adapter;
    private Handler adapter_handler;
    private JSONArray jsonArray;
    private int last_id;
    private String username;
    private OkHttpClient client;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_bin);
        gridView = (GridView)findViewById(R.id.bin_gridview);
        adapter = new MainWindowList_Adapter(this);
        gridView.setAdapter(adapter);

        username = getIntent().getStringExtra("username");
        client = new OkHttpClient();
        registerForContextMenu(gridView);
        op_note(7,-1,null,null,null);


        adapter_handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                try {
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        System.out.println("yes!");
                        JSONObject jsobj = jsonArray.getJSONObject(i);
                        String title = jsobj.getString("title");
                        String date = jsobj.getString("date");
                        //       String content = jsobj.getString("content");
                        int id = jsobj.getInt("id");
                        adapter.add(new MainWindowListCell(id,title, date,null));
                    }
                    adapter.sort_();
                    adapter.notifyDataSetChanged();

                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater=getMenuInflater();
        last_id = (int) gridView.getItemIdAtPosition(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
        System.out.println("item_id:"+last_id);
        inflater.inflate(R.menu.bin_menu,menu);
    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){



            case R.id.delete_forever:
                MainWindowListCell celldata = adapter.getItem(last_id);
                int delete_id = celldata.getId();
                System.out.println("delete_id:"+delete_id);
                op_note(6,delete_id,null,null,null);
                adapter.remove(last_id);
                adapter.notifyDataSetChanged();
                return true;
            case R.id.recover:
                celldata = adapter.getItem(last_id);
                int recover_id = celldata.getId();
                op_note(5,recover_id,null,null,null);
                System.out.println("recover:"+recover_id);
                adapter.remove(last_id);
                adapter.notifyDataSetChanged();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    public void op_note(int op_status,int id,String title,String date,String content){
/*
			5           Reconver
			6           Delete-Deeply
			7          query from bin
 */

        System.out.println(op_status);

        RequestBody post=null;

        if(op_status == 5){

            post = new FormBody.Builder()
                    .add("recover_id",String.valueOf(id))
                    .add("op_status",String.valueOf(op_status))
                    .build();
        }

        if(op_status ==6){
            post = new FormBody.Builder()
                    .add("delete_id",String.valueOf(id))
                    .add("op_status",String.valueOf(op_status))
                    .build();
        }

        if(op_status == 7){
            post = new FormBody.Builder()
                    .add("username",username)
                    .add("op_status",String.valueOf(op_status))
                    .build();
        }

        Request request = new Request.Builder()
                .url("https://xiaomayo.cn/api/op_note.php")
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
                    //  JSONObject jsonObject = new JSONObject(response.body().string());
                    if(op_status ==7) {
                        jsonArray = new JSONArray(response.body().string());
                        adapter_handler.sendMessage(adapter_handler.obtainMessage());
                    }else{
                        String jsonstring = response.body().toString();
                        System.out.println(jsonstring);
                        JSONObject jsonObject = new JSONObject(jsonstring);
                    }
                    //  System.out.println(jsonArray.getJSONObject(0).getString("title"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
