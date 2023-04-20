package cn.xiaomayo.notebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

public class MainWindow extends Activity implements AdapterView.OnItemClickListener {

    private TextView delete;
    private MainWindowList_Adapter adapter;

    private String username;
    private JSONArray jsonArray;
    private GridView gridView;
    private FloatingActionButton addbtn,binbtn;


    private OkHttpClient client;
    private Handler adapter_handler,delete_handler;


    private int last_id;

    private int new_or_update;
    private int last_idx;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.win_grid);

        username = getIntent().getExtras().getString("username");

        gridView = (GridView)findViewById(R.id.main_gridview);
        delete = (TextView)findViewById(R.id.delete);
        adapter = new MainWindowList_Adapter(this);

        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);

        addbtn = (FloatingActionButton)findViewById(R.id.add);
        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                last_idx=last_idx+1;
                System.out.println("last_idx:"+last_idx);
                Intent intent = new Intent(MainWindow.this,Aty_Edit.class);
                intent.putExtra("new_or_update",0);
                intent.putExtra("username",username);
                intent.putExtra("id",last_idx);
                startActivityForResult(intent,1);

            }
        });

        binbtn = (FloatingActionButton)findViewById(R.id.bin);
        binbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainWindow.this,Aty_bin.class);
                intent.putExtra("username",username);
                startActivityForResult(intent,2);
            }
        });

        gridView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                System.out.println("uyes!");
            }
        });

        registerForContextMenu(gridView);

        client = new OkHttpClient();
//
//        String sql_create = "CREATE TABLE note(" +
//                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
//                "username text DEFAULT \"\","+
//                "title text DEFAULT \"\"," +
//                "date text DEFAULT \"\"," +
//                "content text DEFAULT \"\")";

        op_note(3,-1,null,null,null);
        op_note(8,-1,null,null,null);




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
                        Intent intent = new Intent(MainWindow.this,Aty_Edit.class);
                        Bundle data = new Bundle();
                //        data.putString("content",content);
                        data.putString("title",title);
                        data.putString("date",date);
                        data.putInt("id",id);
                        intent.putExtras(data);
                        adapter.add(new MainWindowListCell(id,title, date,intent));
                    }
                    adapter.sort_();
                    adapter.notifyDataSetChanged();

                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        delete_handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {

            }
        };

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.option_menu,menu);
        return true;

    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_search:
                return true;
            case R.id.menu_share:
                return true;
            case R.id.menu_collect:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater=getMenuInflater();
        last_id = (int) gridView.getItemIdAtPosition(((AdapterView.AdapterContextMenuInfo) menuInfo).position);
        System.out.println("item_id:"+last_id);
        inflater.inflate(R.menu.long_click_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.context_option_delete:

                MainWindowListCell celldata = adapter.getItem(last_id);
                int delete_id = celldata.getId();
                System.out.println("delete_id:"+delete_id);
                op_note(1,delete_id,null,null,null);
                adapter.remove(last_id);
                adapter.notifyDataSetChanged();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        last_id = position;
        MainWindowListCell celldata = adapter.getItem(position);
        System.out.println("postion:"+position);
        Intent intent = celldata.getIntent();
        intent.putExtra("new_or_update",1);
        intent.putExtra("username",username);
        startActivityForResult(intent,1);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null) {



            //int status = data.getIntExtra("status", 0);
            if (resultCode == 1) {
                String date = data.getStringExtra("date");
                String title = data.getStringExtra("title");
                String content=data.getStringExtra("content");
        //        String content = data.getStringExtra("content");
                int id = data.getIntExtra("id",0);
                new_or_update = data.getIntExtra("new_or_update",0);
                System.out.println("return_id:"+id);

                //更新本地
                if(new_or_update ==1) adapter.remove(last_id);
                Intent intent = new Intent(MainWindow.this,Aty_Edit.class);
                Bundle data_ = new Bundle();
     //           data_.putString("content",content);
                data_.putString("title",title);
                data_.putString("date",date);
                data_.putInt("id",id);
                intent.putExtras(data_);
                adapter.add_front(new MainWindowListCell(id,title,date,intent));
                adapter.notifyDataSetChanged();

                //更新数据库
//                System.out.println("new_or_uuuupdate:"+new_or_update);
//                if(new_or_update ==0) op_note(0,id,title,date,content);
//                if(new_or_update ==1) op_note(2,id,title,date,content);

                //     int op_status = data.getIntExtra("op_status",3);
//            if(last_id!=-1){
//                SqliteDB db_write;
//                adapter.remove(last_id);
//                String sql_delete = "delete from boy_box where id = (select id from boy_box Limit "+last_id+",1)";
//                db_write = .getWritableDatabase();
//                db_write.execSQL(sql_delete);
//            }
                System.out.println("111");
            }
            System.out.println("11123123");
        }
    }






    public void op_note(int op_status,int id,String title,String date,String content){
/*
        0           Insert
        1           Delete
        2           update
        3           select
        4           query the last id
 */

        System.out.println(op_status);

        RequestBody post=null;

        if(op_status == 0){

            post = new FormBody.Builder()
                    .add("username",username)
                    .add("title",title)
                    .add("content",content)
                    .add("date",date)
                    .add("op_status",String.valueOf(op_status))
                    .build();
        }

        if(op_status ==1){
            post = new FormBody.Builder()
                    .add("delete_id",String.valueOf(id))
                    .add("op_status",String.valueOf(op_status))
                    .build();
        }

        if(op_status == 2){
            System.out.println("id:"+id);
            post = new FormBody.Builder()
                    .add("username",username)
                    .add("update_id",String.valueOf(id))
                    .add("title",title)
                    .add("content",content)
                    .add("date",date)
                    .add("op_status",String.valueOf(op_status))
                    .build();
        }

        if(op_status == 3){
            System.out.println(username);
            post = new FormBody.Builder()
                    .add("username",username)
                    .add("op_status",String.valueOf(op_status))
                    .build();
        }

        if(op_status ==8){
            post=new FormBody.Builder()
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

                    if(op_status==8){
                        String jsonstring = response.body().string();
                        JSONObject jsonobj = new JSONObject(jsonstring);
                        last_idx=jsonobj.getInt("id");

                    }

                    if(op_status ==3) {
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
