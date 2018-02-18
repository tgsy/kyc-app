package com.example.tessa.kyc;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import java.io.IOException;

/**
 * Created by tessa on 12/2/2018.
 */

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the underlying surface is created and destroyed
        mHolder = getHolder();
        mHolder.addCallback(this);

    }

    public void surfaceCreated(SurfaceHolder holder) {
        Camera.Parameters params = mCamera.getParameters();

        //change the orientation of the camera
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE){
            params.set("orientation", "portrait");
            mCamera.setDisplayOrientation(90);
            params.setRotation(90);
        }
        else {
            params.set("orientaations", "landscape");
            mCamera.setDisplayOrientation(0);
            params.setRotation(0);
        }

        mCamera.setParameters(params);
        try {
            mCamera.setPreviewDisplay(holder);
        }catch (IOException e){
            e.printStackTrace();
        }
       /* mCamera.setPreviewDisplay(holder);
        //The Surface has been created, now tell the camera where to draw the preview
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();;
        }catch (IOException e) {
            Log.d("Norman", "Error setting camera preview: " + e.getMessage());
        }*/
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        //before changing the application orientation, you need to stop the preview, rotate and then start it

        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        }catch (IOException e){
            Log.d("ERROR", "Camera error on surfaceChanged " + e.getMessage());
        }
    }

}
