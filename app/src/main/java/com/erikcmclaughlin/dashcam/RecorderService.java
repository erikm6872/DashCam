package com.erikcmclaughlin.dashcam;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.hardware.camera2.*;

public class RecorderService extends Service {
    public static CameraManager cameraManager;
    public static CameraDevice cameraDevice;

    public boolean recStatus;

    public RecorderService() {
    }

    @Override
    public void onCreate(){
        recStatus = false;
        /*mServiceCamera = CameraRecorder.mCamera;
        cameraManager.openCamera();
        mServiceCamera = android.hardware.camera2.open(1);
        mSurfaceView = CameraRecorder.mSurfaceView;
        mSurfaceHolder = CameraRecorder.mSurfaceHolder;

        super.onCreate();
        if (mRecordingStatus == false)
            startRecording();
            */
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
