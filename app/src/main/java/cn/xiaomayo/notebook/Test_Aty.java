package cn.xiaomayo.notebook;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
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
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class Test_Aty extends Activity {

    private TextView tv;
    private EditText edt;
    private Button btn,testbtn;
    private ImageView imageView;
    Bitmap bitmap;
    Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

      //  tv = (TextView)findViewById(R.id.testtv);
        edt = (EditText)findViewById(R.id.testedt);
        btn = (Button)findViewById(R.id.testbtn);
      //  testbtn = (Button)findViewById(R.id.testbtn1);
        imageView = (ImageView)findViewById(R.id.imageView);




        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                //申请权限部分
                String write= Manifest.permission.WRITE_EXTERNAL_STORAGE;
                String read=Manifest.permission.READ_EXTERNAL_STORAGE;

                final String[] WriteReadPermission = new String[] {write, read};

                int checkWrite= ContextCompat.checkSelfPermission(Test_Aty.this,write);
                int checkRead= ContextCompat.checkSelfPermission(Test_Aty.this,read);
                int ok= PackageManager.PERMISSION_GRANTED;

                if (checkWrite!= ok && checkRead!=ok){
                    //申请权限，读和写都申请一下，不然容易出问题
                    ActivityCompat.requestPermissions(Test_Aty.this,WriteReadPermission,1);
                }else openAlbum();

            }
        });





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
        if (requestCode==1&&grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED)
            openAlbum();
        else Toast.makeText(this, "你拒绝了打开相册的权限", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //当选择完相片，就会回到这里，然后相片的相关信息会保存在data中，后面想办法取出来
        if (requestCode==123){

            System.out.println("Yes?");

            Uri uri = data.getData();
            String file_path = getFilePathByUri(Test_Aty.this,uri);
            System.out.println("file_path:"+file_path);
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
            //创建一个字符流大小的数组。

            //用默认的编码格式进行编码
            String result = Base64.encodeToString(data_,Base64.DEFAULT);
            System.out.println("length of base64:"+result.length());

            byte[] bytes = Base64.decode(result, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            BitmapDrawable bd = new BitmapDrawable(bitmap);
            Drawable img = bd;

            String string = "今天晚上下雨啦！";
            SpannableString spanColor = new SpannableString(string+result);
            System.out.println(string.length() + "  ?" + spanColor.length());
            bd.setBounds(0, 0, bd.getIntrinsicWidth(), bd.getIntrinsicHeight());
            ImageSpan span = new ImageSpan(bd, ImageSpan.ALIGN_BASELINE);
            spanColor.setSpan(span, string.length(),string.length()+result.length(), 0);
          //  edt.append(spanColor);
            edt.append("\n\n\n\n\nhello!");
            System.out.println(edt.getText());
            System.out.println("hello\nisme");
/*
            //将base64编码还原成图片
            byte[] decode = Base64.decode(result, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(decode, 0, decode.length);
            //handler.sendMessage(handler.obtainMessage());


 */
            handler = new Handler(){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    System.out.println("Okokokoko");
                    imageView.setImageBitmap(bitmap);
                }
            };


            /*
            try {
                FileInputStream fis = new FileInputStream(file_path);
                ByteArrayOutputStream bfos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len=0;
                while((len=fis.read(buffer))!=-1){
                    System.out.println(len);
                    bfos.write(buffer,0,len);
                }
                fis.close();
                bfos.close();
                System.out.println(file_path);
                System.out.println(bfos.toByteArray());
                byte[] tmp = bfos.toByteArray();
                Bitmap bitmap = BitmapFactory.decodeByteArray(tmp, 0, tmp.length);
            //    BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
                imageView.setImageBitmap(bitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


             */

/*
            //通过getData方法取得它的图片地址，后面的操作都是对这个地址的解析
            Uri uri=data.getData();
            //选择了一张在相册中id为26的照片，它的uri地址如下：
            //uri=content://com.android.providers.media.documents/document/image%3A26

            if (DocumentsContract.isDocumentUri(this,uri)){

                //判断是document类型的图片，所以获取它的doc id
                String docId=DocumentsContract.getDocumentId(uri);//docId=image:26
                //docId是将该资源的关键信息提取出来，比如该资源是一张id为26的image

                //获取它的uri的已解码的授权组成部分，来判断这张图片是在相册文件夹下还是下载文件夹下
                String uri_getAuthority=uri.getAuthority();
                //在相册文件夹的照片标识字段如下
                //uri_getAuthority=com.android.providers.media.documents

                //注意这里的字符串很容易写错，providers和documents都是有带s的
                if ("com.android.providers.media.documents".equals(uri_getAuthority)){
                    //当判断该照片在相册文件夹下时，使用字符串的分割方法split将它id取出来
                    String id=docId.split(":")[1];//id="26"
                    Uri baseUri=Uri.parse("content://media/external/images/media");
                    imageView.setImageURI(Uri.withAppendedPath(baseUri, id));
                    //直接传入Uri地址，该地址为content://media/external/images/media/26

                }
            }
            */


        }
    }

//  Uri 转 Path

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


}
