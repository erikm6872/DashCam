package com.erikcmclaughlin.dashcam;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DrivingService extends Service {
    public DrivingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
