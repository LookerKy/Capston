package wit.di.skuniv.wit;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import wit.di.skuniv.wit.model.PhotoVO;
import wit.di.skuniv.wit.model.SharedMemory;


public class MainActivity extends AppCompatActivity{
    private static final int MY_PERMISSION_CAMERA=1111;
    private static final int REQUEST_TAKE_PHOTO=2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP=4444;
    private TextView result;
    private ImageView img;
    private String imgPath="";
    Uri imgUri  ;
    Uri photoUri, albumUri;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img=(ImageView)findViewById(R.id.img_test);
        result = (TextView)findViewById(R.id.result);
        findViewById(R.id.camera_btn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showcamera();
            }
        });
        findViewById(R.id.gallary_btn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                getAlbum();
            }
        });
        checkPremission();
    }

    private void showcamera(){
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if(cam.resolveActivity(getPackageManager())!=null){
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                }catch (IOException ie){
                    Log.e("camera Error",ie.toString());
                }
                if(photoFile !=null){
                    Uri providerUri =FileProvider.getUriForFile(this, getPackageName()+".fileprovider",photoFile);
                    imgUri=providerUri;
                    cam.putExtra(MediaStore.EXTRA_OUTPUT,providerUri);

                    startActivityForResult(cam,REQUEST_TAKE_PHOTO);
                }
            }
        }else{
            Toast.makeText(this,"저장공간이 접근 불가합니다.",Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public File createImageFile()throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPGE_"+timeStamp+".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory()+"/Pictures","WIT");

        if(!storageDir.exists()){
            storageDir.mkdirs();
        }
        imageFile = new File(storageDir, imageFileName);
        imgPath = imageFile.getAbsolutePath();
        Log.d("-------storageDir","create file path : "+imgPath);
        return imageFile;
    }

    private void savePicture(){
        Log.i("savePicture","Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(imgPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(this,"사진이 앨범에 저장되었습니다.",Toast.LENGTH_SHORT).show();

    }

    private void getAlbum(){
        Log.i("getAlbum","Call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent,REQUEST_TAKE_ALBUM);

    }

    public void cropImage(){
        Log.i("cropImage","Call");
        Log.i("cropImage","photoURI:"+photoUri+" /albumURI : "+albumUri);

        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoUri,"image/*");
        cropIntent.putExtra("aspectX",1);
        cropIntent.putExtra("aspectY",1);
        cropIntent.putExtra("output",albumUri);
        startActivityForResult(cropIntent,REQUEST_IMAGE_CROP);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       switch (requestCode){
           case REQUEST_TAKE_PHOTO:
               if(resultCode== Activity.RESULT_OK){
                   try{
                        Log.i("REQUEST_TAKE_PHOTO","OK");
                        savePicture();

                       img.setImageURI(imgUri);
                       uploadFile(imgPath);

                       Gson gson = new GsonBuilder().disableHtmlEscaping().create();
                       SharedMemory sharedMemory = SharedMemory.getInstance();
                       String line = null;
                       do {
                           line = sharedMemory.getResultString();
                       }
                       while(line == null);
                       Log.d("sisisi", "line : " + line);
                        PhotoVO p[] = gson.fromJson(line, PhotoVO[].class);
                       List<PhotoVO> list = Arrays.asList(p);
                       for (PhotoVO l : list) {
                           result.append("name : " + l.getName() + " score : " + l.getScore()+"\n");
                           Log.d("sibal", "name : " + l.getName() + " score : " + l.getScore());
                       }
                   }catch (Exception e){
                       Log.e("REQUEST_TAKE_PHOTO",e.toString());
                   }
               }else{
                   Toast.makeText(MainActivity.this,"취소하였습니다.",Toast.LENGTH_SHORT).show();
               }
               break;
           case REQUEST_TAKE_ALBUM:
               if(resultCode==Activity.RESULT_OK){
                   if(data.getData()!=null){
                       try{
                           File albumFile = null;
                           albumFile = createImageFile();
                           photoUri=data.getData();
                           albumUri=Uri.fromFile(albumFile);
                           String splitUri = albumUri.toString();
                           splitUri = splitUri.substring(7, splitUri.length());
                           Log.d("split", "split : " + splitUri);
                           uploadFile(splitUri);
                           cropImage();
                       }catch (Exception e){
                            Log.e("Take_ALBUM_SINGLE ERROR",e.toString());
                       }
                   }
               }
               break;
           case REQUEST_IMAGE_CROP:
                if(resultCode==Activity.RESULT_OK){
                    //savePicture();
                    img.setImageURI(albumUri);
                }
                break;
       }

    }



    private void checkPremission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            if((ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE))||
                    (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA))){
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한 거부")
                        .setNegativeButton("설정",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                               Intent i =new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                i.setData(Uri.parse("package"+getPackageName()));
                                startActivity(i);
                            }
                        })
                        .setPositiveButton("확인",new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();

            }else {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA},MY_PERMISSION_CAMERA);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_CAMERA:
                for(int i=0; i<grantResults.length;i++){
                    if(grantResults[i]<0){
                        Toast.makeText(MainActivity.this,"해당 권한을 활성화 해야합니다.",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                break;
        }
    }
    public void uploadFile(String filePath){
        String url = "http://172.20.10.2:8888/uploadFile";
        try {
            UploadFile uploadFile = new UploadFile(MainActivity.this);
            uploadFile.setPath(filePath);
            uploadFile.execute(url);
        } catch (Exception e){

        }
    }
}
/**I dont know **/
//    private Uri getFileUri(){
//        File dir = new File(getFilesDir(),"/Pictures/Wit");
//        if (!dir.exists()){
//            dir.mkdirs();
//            Log.d("mkdir:","complete");
//        }
//        File file = new File(dir,System.currentTimeMillis()+".jpg");
//        imgPath=file.getAbsolutePath();
//        return FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);
//
//    }