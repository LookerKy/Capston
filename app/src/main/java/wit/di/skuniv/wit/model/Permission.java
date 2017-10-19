package wit.di.skuniv.wit.model;

import android.Manifest;
import android.net.Uri;

/**
 * Created by Ky on 2017-10-19.
 */

public class Permission {
    private static final int PICK_FROM_CAMERA = 1; //카메라 촬영으로 사진 가져오기
    private static final int PICK_FROM_ALBUM = 2; //앨범에서 사진 가져오기
    private static final int CROP_FROM_CAMERA = 3; //가져온 사진을 자르기 위한 변수

    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private static final int  MULTIPLE_PERMISSIONS = 101;
    private Uri photouri;

    public void setPhotouri(Uri photouri) {
        this.photouri = photouri;
    }

    public Uri getPhotouri() {
        return photouri;
    }

    public static int getPickFromCamera() {return PICK_FROM_CAMERA;}

    public static int getPickFromAlbum() {return PICK_FROM_ALBUM;}

    public static int getCropFromCamera() {return CROP_FROM_CAMERA;}

    public String[] getPermissions() {return permissions;}

    public static int getMultiplePermissions() {return MULTIPLE_PERMISSIONS;}
}
