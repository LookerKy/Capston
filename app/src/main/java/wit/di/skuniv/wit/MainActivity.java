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
import android.widget.Toast;
import android.Manifest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity{
    private static final int MY_PERMISSION_CAMERA=1111;
    private static final int REQUEST_TAKE_PHOTO=2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP=4444;

    private ImageView img;
    private String imgPath="";
    Uri imgUri  ;
    Uri photoUri, albumUri;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img=(ImageView)findViewById(R.id.img_test);

        findViewById(R.id.camera_btn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                showcamera();
            }
        });
        checkPremission();
    }
    private Uri getFileUri(){
        File dir = new File(getFilesDir(),"/Pictures/Wit");
        if (!dir.exists()){
            dir.mkdirs();
            Log.d("mkdir:","complete");
        }
        File file = new File(dir,System.currentTimeMillis()+".jpg");
        imgPath=file.getAbsolutePath();
        return FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       switch (requestCode){
           case REQUEST_TAKE_PHOTO:
               if(resultCode== Activity.RESULT_OK){
                   try{
                        Log.i("REQUEST_TAKE_PHOTO","OK");
                        savePicture();

                       img.setImageURI(imgUri);

                   }catch (Exception e){
                       Log.e("REQUEST_TAKE_PHOTO",e.toString());
                   }
               }else{
                   Toast.makeText(MainActivity.this,"취소하였습니다.",Toast.LENGTH_SHORT).show();
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
}
