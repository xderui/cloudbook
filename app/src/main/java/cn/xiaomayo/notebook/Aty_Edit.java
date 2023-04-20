package cn.xiaomayo.notebook;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Aty_Edit extends Activity {


    public static final int MAX=1024;


    private EditText edt_title,edt_content;
    private ImageView back,undo,redo,accessory;

    private String init_title,init_content;
    private String username;
    private JSONArray jsonArray;
    private JSONObject jsonObject;
    private OkHttpClient client;
    private Handler changed_Handler,getcontent_Handler;
    private AlertDialog.Builder alertDialog_builder;


    private int id,undo_flag,redo_flag,new_id;

    private int new_or_update;

    private Stack<String> undo_stk,redo_stk;

    String[] imgmap = new String[MAX];
    int cur_no=0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aty_edit);
        edt_title = (EditText) findViewById(R.id.edt_title);
        edt_content = (EditText)findViewById(R.id.edt_content);

        back = (ImageView)findViewById(R.id.back);
        undo = (ImageView)findViewById(R.id.undo);
        redo = (ImageView)findViewById(R.id.redo);
        accessory = (ImageView)findViewById(R.id.accessory);
        client = new OkHttpClient();

        Bundle data = getIntent().getExtras();
        String title = data.getString("title");
        id = data.getInt("id");
        username = getIntent().getStringExtra("username");
        new_or_update = getIntent().getIntExtra("new_or_update",0);
        if(new_or_update==0) id=getIntent().getIntExtra("id",0);

        undo_stk = new Stack<String>();
        redo_stk = new Stack<String>();

        System.out.println("idididid:"+id);

        op_note(4,id,null,null,null);

        edt_title.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                init_title=edt_title.getText().toString();
            }
        });

        edt_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                String tmp=edt_content.getText().toString();
                if(TextUtils.isEmpty(edt_content.getText())){
                    System.out.println("空的");
                    tmp=" ";
                }
                if(undo_flag==0&&redo_flag==0){
                    undo_stk.push(tmp);
                    redo_stk = new Stack<String>();
                }

                undo_flag=0;

                System.out.println("发生变化前"+tmp);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!undo_stk.empty()) {
                    undo_flag = 1;
                    String undo_text = undo_stk.peek();
                    undo_stk.pop();
                    System.out.println("undo_text" + undo_text + ",stack size" + undo_stk.size());
                    String now_text = edt_content.getText().toString();
                    System.out.println("now_text" + now_text);
                    redo_stk.push(now_text);
                    System.out.println("now_text:"+now_text);
                    init_content = convert_true_content(undo_text);
                    getcontent_Handler.sendMessage(getcontent_Handler.obtainMessage());
                }
            }
        });

        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!redo_stk.empty()) {
                    redo_flag=1;
                    String redo_text = redo_stk.peek();
                    redo_stk.pop();
                    String now_text = edt_content.getText().toString();
                    undo_stk.push(now_text);
                    init_content = convert_true_content(redo_text);
                    getcontent_Handler.sendMessage(getcontent_Handler.obtainMessage());
                }
            }
        });

        accessory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String write= Manifest.permission.WRITE_EXTERNAL_STORAGE;
                String read=Manifest.permission.READ_EXTERNAL_STORAGE;

                final String[] WriteReadPermission = new String[] {write, read};

                int checkWrite= ContextCompat.checkSelfPermission(Aty_Edit.this,write);
                int checkRead= ContextCompat.checkSelfPermission(Aty_Edit.this,read);
                int ok= PackageManager.PERMISSION_GRANTED;

                if (checkWrite!= ok && checkRead!=ok){
                    //申请权限，读和写都申请一下，不然容易出问题
                    ActivityCompat.requestPermissions(Aty_Edit.this,WriteReadPermission,1);
                }else openAlbum();
            }
        });


        changed_Handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                String title = edt_title.getText().toString();
                String content = edt_content.getText().toString();
                System.out.println("now_content="+content);
                if (title.equals(init_title)==false || content.equals(init_content)==false) {
                    alertDialog_builder = new AlertDialog.Builder(Aty_Edit.this);
                    alertDialog_builder.setTitle("Note").setMessage("内容发送了变动，需要保存吗？").setPositiveButton("保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            save();
                            finish();
                        }
                    }).setNegativeButton("不保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).setNeutralButton("取消", null).show();
                }else finish();
            }
        };

        getcontent_Handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                System.out.println(init_content);

                String content = init_content;

                System.out.println("init_content:"+content);

                System.out.println("new_or_update:"+new_or_update);

                init_title = title;

                //图片数据定义  σp + no + σ + length of base64 + σ + base64   ----     σp+ no + σ
                //example: σp0σ100242σ9jijxa4541xawexz...   σp0σ

                edt_title.setText(title);
                //edt_content.setText(content);
                int cont_len=0;
                if(content==null) cont_len=0;
                else cont_len = content.length();
                System.out.println("length of content:"+cont_len);
                if(cont_len!=0) {
                    int img_no=0,begin=0,end=0;
                    String final_String="";
                    //处理字符串
                    for (int i = 0; i < cont_len; ++i) {
                        if (content.charAt(i) == 'σ' && content.charAt(i + 1) == 'p') {
                            int p = i + 2;
                            while(content.charAt(p)!='σ') p++;
                            p++; //向后移一位到长度上
                            int replace_start = p;
                            int base_len = 0;
                            while (content.charAt(p) != 'σ'){
                                base_len = base_len * 10 + content.charAt(p) - '0';
                                p++;
                            }
                            p++;  //向后移一位到base64码上

                            String basecode = content.substring(p, p + base_len);
                            imgmap[img_no]=basecode;
                            final_String = final_String + content.substring(begin,i) + "σp"+String.valueOf(img_no)+"σ";
                            begin = p+base_len;
                            i=begin;
                            img_no++;
                        }
                    }

                    final_String += content.substring(begin,cont_len);

                    content = final_String;
                    cont_len = content.length();
                    System.out.println("new_content:"+content);
                    SpannableString span = new SpannableString(content);
                    for(int i=0;i<cont_len;++i){
                        if(content.charAt(i)=='σ' && content.charAt(i+1)=='p'){
                            int p=i+2,no=0;
                            while(content.charAt(p)!='σ'){
                                no = no*10+content.charAt(p)-'0';
                                p++;
                            }
                            p++;
                            String basecode = imgmap[no];
                            System.out.println(basecode);
                            byte[] bytes = Base64.decode(basecode, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            BitmapDrawable bd = new BitmapDrawable(bitmap);
                            Drawable img = bd;

                            int width,height;
                            width = edt_content.getWidth();
                            height = width*img.getIntrinsicHeight()/img.getIntrinsicWidth();

                            img.setBounds(0, 0, width, height);
//                            Bitmap bitmap_= BitmapFactory.decodeResource(getResources(),R.drawable.test2);
//                            ImageSpan imagespan = new ImageSpan(Aty_Edit.this,bitmap_);
                            ImageSpan imagespan = new ImageSpan(img, ImageSpan.ALIGN_BASELINE);

                            span.setSpan(imagespan, i,p, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                            System.out.println("success!");
                            i=p;
                        }
                    }
                    edt_content.setText(span);
                    edt_content.setSelection(span.length());
                }
            }
        };

    }


    public String convert_true_content(String content){
        StringBuffer contentBuffer = new StringBuffer(content);
        int index = contentBuffer.indexOf("σp",0);
        int start_index = index;
        int loc=0;
        while( index != -1){
            index = index + 2;
            while(contentBuffer.charAt(index)!='σ') {
                loc = loc*10 + contentBuffer.charAt(index) - '0';
                index++;
            }
            index++;
            //   contentBuffer.replace(start_index,index,imgmap[loc]);
            contentBuffer.insert(index,imgmap[loc].length()+"σ"+imgmap[loc]);
            index = contentBuffer.indexOf("σp",index+1);
        }

        content = contentBuffer.toString();
        return content;
    }


    @Override
    public void onBackPressed() {
        changed_Handler.sendMessage(changed_Handler.obtainMessage());
    }

    public void save() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date(System.currentTimeMillis());
        String date = simpleDateFormat.format(d);

        String title = edt_title.getText().toString();
    //    String content = edt_content.getText().toString();

        //图片数据定义  σp + no + σ + length of base64 + σ + base64   ----     σp+ no + σ
        //example: σp0σ100242σ9jijxa4541xawexz...   σp0σ

        String content = edt_content.getText().toString();
        System.out.println("final:"+content);
        StringBuffer contentBuffer = new StringBuffer(content);
        int index = contentBuffer.indexOf("σp",0);
        int start_index = index;
        int loc=0;
        while( index != -1){
            index = index + 2;
            while(contentBuffer.charAt(index)!='σ') {
                loc = loc*10 + contentBuffer.charAt(index) - '0';
                index++;
            }
            index++;
            System.out.println("loc:"+loc);
         //   contentBuffer.replace(start_index,index,imgmap[loc]);
            contentBuffer.insert(index,imgmap[loc].length()+"σ"+imgmap[loc]);
            index = contentBuffer.indexOf("σp",index+1);
        }

        content = contentBuffer.toString();
        System.out.println(content);

        Intent intent = new Intent();
        if(new_or_update ==0){
            op_note(0,id,title,date,content);
        }
        else{
            op_note(2,id,title,date,content);
        }
        System.out.println("will_id:"+id+"nnnew_id:"+new_id);

        intent.putExtra("title",title);
        intent.putExtra("date",date);
        intent.putExtra("op_status",2);
        intent.putExtra("id",id);
        intent.putExtra("new_or_update",new_or_update);
        setResult(1,intent);
        System.out.println("username:"+username);
        Aty_Edit.this.finish();
    }




    public void openAlbum(){
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        //把所有照片显示出来
        intent.setType("image/*");
        startActivityForResult(intent,123);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1&&grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED)
            openAlbum();
        else Toast.makeText(this, "你拒绝了打开相册的权限", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==123){
            if(data!=null) {

                System.out.println("Yes?");

                Uri uri = data.getData();
                String file_path = getFilePathByUri(Aty_Edit.this, uri);
                System.out.println("file_path:" + file_path);
                InputStream is = null;
                byte[] data_ = null;
                try {
                    is = new FileInputStream(file_path);
                    data_ = new byte[is.available()];
                    is.read(data_);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //用默认的编码格式进行编码
                String result = Base64.encodeToString(data_, Base64.DEFAULT);

                //创建一个字符流大小的数组。
                byte[] bytes = Base64.decode(result, Base64.DEFAULT);
                //new_add
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                options.inSampleSize = calculateInSampleSize(options, 480, 800);
                options.inJustDecodeBounds = false;

                //new_add


                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,options);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 40, baos);
                bytes = baos.toByteArray();
                result=Base64.encodeToString(bytes,Base64.DEFAULT);

                BitmapDrawable bd = new BitmapDrawable(bitmap);
                Drawable img = bd;


                //图片数据定义  /p + no + / + length of base64 + / + base64   ----     /p+ no + /
                //example: /p0/100242/9jijxa4541xawexz...   /p0/
                int len = result.length();
                //System.out.println("length of result:"+len);

                String show_str = "σp" + String.valueOf(cur_no) + "σ";
                imgmap[cur_no++] = result;
                System.out.println("show_str:"+show_str);
                final SpannableString span = new SpannableString(show_str);

                //System.out.println(final_str);
                int width,height;
                width = edt_content.getWidth();
                height = width*img.getIntrinsicHeight()/img.getIntrinsicWidth();

                img.setBounds(0, 0, width, height);
                ImageSpan imagespan = new ImageSpan(img, ImageSpan.ALIGN_BASELINE);

                span.setSpan(imagespan, 0, show_str.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                edt_content.append("\n");
                edt_content.append(span);
                edt_content.append("\n");
                System.out.println("Insert!!");


            }
        }
    }

//  Uri 转 Path

    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public static String getFilePathByUri(Context context, Uri uri) {
        String path = null;
        // 4.4及之后的 是以 content:// 开头的，比如 content://com.android.providers.media.documents/document/image%3A235700
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (isExternalStorageDocument(uri)) {
                    // ExternalStorageProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        path = Environment.getExternalStorageDirectory() + "/" + split[1];
                        return path;
                    }
                } else if (isDownloadsDocument(uri)) {
                    // DownloadsProvider
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(id));
                    path = getDataColumn(context, contentUri, null, null);
                    return path;
                } else if (isMediaDocument(uri)) {
                    // MediaProvider
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};
                    path = getDataColumn(context, contentUri, selection, selectionArgs);
                    return path;
                }
            }
        }else {
            // 以 file:// 开头的
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                path = uri.getPath();
                return path;
            }
            // 以 content:// 开头的，比如 content://media/extenral/images/media/17766
            if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme()) && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        if (columnIndex > -1) {
                            path = cursor.getString(columnIndex);
                        }
                    }
                    cursor.close();
                }
                return path;
            }
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public void op_note(int op_status,int id,String title,String date,String content){
/*
        0           Insert
        1           Delete
        2           update
        3           select
 */

        System.out.println(op_status);

        RequestBody post=null;

        if(op_status == 0){
            System.out.println("username:"+username);
            System.out.println("title:"+title);
            System.out.println("content:"+content);
            System.out.println("date:"+date);

            post = new FormBody.Builder()
                    .add("username",username)
                    .add("title",title)
                    .add("content",content)
                    .add("date",date)
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

        if(op_status == 4){
            post = new FormBody.Builder()
                    .add("id",String.valueOf(id))
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
                    System.out.println("what");
                    //  JSONObject jsonObject = new JSONObject(response.body().string());
                    if (op_status==0){
                        String jsonstring = response.body().string();
                        JSONObject jsonobj = new JSONObject(jsonstring);
                        new_id=jsonobj.getInt("id");
                        System.out.println("new_id:"+new_id);
                    }else if(op_status ==3) {
                        jsonArray = new JSONArray(response.body().string());
                    }else if(op_status ==4){
                        String jsonstring = response.body().string();
                        jsonObject = new JSONObject(jsonstring);
                        init_content = jsonObject.getString("content");
                        if(init_content.equals("null")) init_content="";
                        getcontent_Handler.sendMessage(getcontent_Handler.obtainMessage());
                    }else{
                        String jsonstring = response.body().string();
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
