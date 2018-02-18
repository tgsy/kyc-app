package com.example.tessa.kyc;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    static Camera camera = null;
    FrameLayout frameLayout;
    CameraPreview showCamera;
    Button captureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        frameLayout = (FrameLayout) findViewById(R.id.camera_view);
        camera = Camera.open();

        showCamera = new CameraPreview(CameraActivity.this,camera);
        frameLayout.addView(showCamera);
        captureButton = (Button) findViewById(R.id.captureButton);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        captureImage(showCamera);
                    }
                });

            }

        });

       /* //Create an instance of Camera
        mCamera = getCameraInstance();

        //Create our Preview view and set it as the content of our activity
        mCameraView = new CameraPreview(CameraActivity.this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_view);
        preview.addView(mCameraView);

       /* try {
            mCamera = Camera.open();
        }catch (Exception e) {
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }*/

    }
    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File picture_file = getOutputMediaFile();
            if (picture_file == null){
                return;
            }
            else {
                try {
                    Log.i("Norman","hi");
                    FileOutputStream fos = new FileOutputStream(picture_file);
                    fos.write(data);
                    fos.close();

                    camera.startPreview();
                }catch (FileNotFoundException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    };

    private File getOutputMediaFile() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)){
            return null;
        }
        else {
            File folder_gui = new File(Environment.getExternalStorageDirectory() + File.separator + "GUI");

            if (!folder_gui.exists()){
                folder_gui.mkdirs();
            }
            File outputFile = new File(folder_gui,"temp.jpg");
            return outputFile;
        }
    }

    //get an instance of the Camera object
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }
        return c;
    }

    public void captureImage(View v){
        if (camera != null) {
            camera.takePicture(null,null, mPictureCallback);
        }
    }

}
