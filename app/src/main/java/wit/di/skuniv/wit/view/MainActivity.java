package wit.di.skuniv.wit.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import wit.di.skuniv.wit.R;
import wit.di.skuniv.wit.model.Permission;

public class MainActivity extends AppCompatActivity {
    //private Permission permissions; //나중에 나눌것
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_CAMERA = 3;
    private Uri photoUri;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    private static final int MULTIPLE_PERMISSIONS = 101;

    private String mCurrentPhotoPath;

    private Button m_cam_btn;
    private Button m_gal_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
    private boolean checkPermission() {
        int result;
        //permissions = new Permission();
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) { //사용자가 해당 권한을 가지고 있지 않을 경우 리스트에 해당 권한명 추가
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) { //권한이 추가되었으면 해당 리스트가 empty가 아니므로 request 즉 권한을 요청합니다.
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }
    private void initView(){
         m_cam_btn = (Button)findViewById(R.id.camera_btn);
         m_gal_btn = (Button)findViewById(R.id.gallary_btn);

        m_cam_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        m_gal_btn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                goToAlbum();
            }
        });
    }
    private void takePhoto(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        }catch (IOException e){
            Toast.makeText(MainActivity.this,"이미지  처리 오류! 다시시도해주세요",Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if(photoFile !=null){
            photoUri = FileProvider.getUriForFile(MainActivity.this,"dongster.cameranostest.provider",photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
            startActivityForResult(intent,PICK_FROM_CAMERA);
        }
    }
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "nostest_"+timeStamp+"_";
        File storageDir = new File(Environment.getExternalStorageDirectory()+"/NOSTest");
        if(!storageDir.exists()){
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName,".jpg",storageDir);
        mCurrentPhotoPath ="file:"+image.getAbsolutePath();
        return image;
    }
    private void goToAlbum(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent,PICK_FROM_ALBUM);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        } else if (permissions[i].equals(this.permissions[2])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }
                return;
            }
        }
    }
    private void showNoPermissionToastAndFinish(){
        Toast.makeText(this,"권한요청 동의해야 사용가능",Toast.LENGTH_SHORT).show();
        finish();
    }
}
