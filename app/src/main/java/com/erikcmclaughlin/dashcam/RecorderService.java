package com.erikcmclaughlin.dashcam;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RecorderService extends Service {
    public static android.hardware.camera2.CameraManager cameraManager;
    public static android.hardware.camera2.CameraDevice cameraDevice;

    public RecorderService() {
    }

    @Override
    public void onCreate(){

    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
