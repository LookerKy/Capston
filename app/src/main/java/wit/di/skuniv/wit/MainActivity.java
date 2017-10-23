package wit.di.skuniv.wit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity{
    private ImageView img;
    private String imgPath;
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

    }
    private Uri getFileUri(){
        File dir = new File(getFilesDir(),"/Pictures/Wit");
        if (!dir.exists()){
            dir.mkdirs();
            Log.d("mkdir:","complete");
        }
        File file = new File(dir,System.currentTimeMillis()+".jpg");
        imgPath=file.getAbsolutePath();
        return FileProvider.getUriForFile(this,getApplicationContext().getPackageName()+".fileprovider",file);

    }
    private void showcamera(){
        Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cam.putExtra(MediaStore.EXTRA_OUTPUT,getFileUri());
        startActivityForResult(cam,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case 1:
                    Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                    img.setImageBitmap(bitmap);
                    break;
            }
        }
    }
}
