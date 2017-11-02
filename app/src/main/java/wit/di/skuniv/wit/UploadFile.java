package wit.di.skuniv.wit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import wit.di.skuniv.wit.model.PhotoVO;
import wit.di.skuniv.wit.model.SharedMemory;

/**
 * Created by Ky on 2017-10-24.
 */

public class UploadFile extends AsyncTask<String, String, String> {

    Context context; // 생성자 호출 시
    ProgressDialog mProgressDialog; // 진행 상태 다이얼로그
    String fileName; // 파일 위치

    HttpURLConnection conn = null; // 네트워크 연결 객체
    DataOutputStream dos = null; // 서버 전송 시 데이터 작성한 뒤 전송

    String lineEnd = "\r\n"; // 구분자
    String twoHyphens = "--";
    String boundary = "*****";

    int bytesRead, bytesAvailable, bufferSize;
    byte[] buffer;
    int maxBufferSize = 1024;
    File sourceFile;
    int serverResponseCode;
    String TAG = "FileUpload";

    public AsyncResponse delegate =null;

    public UploadFile(Context context,AsyncResponse asyncResponse) {
        this.context = context;
        delegate = asyncResponse;
    }

    public void setPath(String uploadFilePath){
        this.fileName = uploadFilePath;
        this.sourceFile = new File(uploadFilePath);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle("Loading...");
        mProgressDialog.setMessage("이미지 분석중...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.show();
    }
    @Override
    protected String doInBackground(String... strings) {
        Log.d("lgy", "filename : " +fileName);

        if(!sourceFile.isFile()){ // 해당 위치의 파일이 있는지 검사\
            Log.d("lgy", "not file "
            );
            Log.e(TAG, "sourceFile(" + fileName + ") is Not A File");

            return null;
        }else {
            String success = "";
            String line = null;
            Log.i(TAG, "sourceFile(" + fileName + ") is A File");
            try {
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(strings[0]);
                Log.i("strings[0]", strings[0]);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST"); // 전송 방식
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary); // boundary 기준으로 인자를 구분함
                conn.setRequestProperty("photo", fileName);
                Log.i(TAG, "fileName: " + fileName);

                // dataoutput은 outputstream이란 클래스를 가져오며, outputStream는 FileOutputStream의 하위 클래스이다.
                // output은 쓰기, input은 읽기, 데이터를 전송할 때 전송할 내용을 적는 것으로 이해할 것
                dos = new DataOutputStream(conn.getOutputStream());

                // 사용자 이름으로 폴더를 생성하기 위해 사용자 이름을 서버로 전송한다. 하나의 인자 전달 data1 = newImage
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"id\"" + lineEnd); // name으 \ \ 안 인자가 php의 key
                dos.writeBytes(lineEnd);
                dos.writeBytes("`"); // newImage라는 값을 넘김
                dos.writeBytes(lineEnd);


                // 이미지 전송, 데이터 전달 uploadded_file라는 php key값에 저장되는 내용은 fileName
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"photo\"; filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                // send multipart form data necesssary after file data..., 마지막에 two~~ lineEnd로 마무리 (인자 나열이 끝났음을 알림)
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i(TAG, "[UploadImageToServer] HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {
//                    mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                            Log.d(TAG, "onDismissed() ");
//
//
//                            //task.cancel(false);
//                        }
//                    });
                }


                // 결과 확인
                BufferedReader rd = null;

                rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                line = null;
                while ((line = rd.readLine()) != null) {
                   Log.i("Upload State", line);
                   SharedMemory sharedMemory = SharedMemory.getInstance();
                    sharedMemory.setResultString(line);
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();
            } catch(Exception e){
                Log.e(TAG + " Error", e.toString());
            }
            return line;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        SharedMemory sharedMemory = SharedMemory.getInstance();
        String line = s;
        line = sharedMemory.getResultString();
//        Log.d("line:: ", "onPostExecute: "+line);
        Log.d(TAG, "onPostExecute: "+line);
        delegate.processFinish(line);
        mProgressDialog.dismiss();
        super.onPostExecute(s);
    }
}