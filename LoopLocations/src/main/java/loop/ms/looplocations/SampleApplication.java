package loop.ms.looplocations;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import ms.loop.loopsdk.core.ILoopSDKCallback;
import ms.loop.loopsdk.core.LoopSDK;
import ms.loop.loopsdk.core.LoopServiceManager;
import ms.loop.loopsdk.processors.KnownLocationProcessor;
import ms.loop.loopsdk.signal.SignalConfig;
import ms.loop.loopsdk.util.LoopError;

/**
 * Created on 6/1/16.
 */
public class SampleApplication extends Application implements ILoopSDKCallback{

    private KnownLocationProcessor knownLocationProcessor;

    @Override
    public void onCreate()
    {
        super.onCreate();

        // initialize the Loop SDK. create an account to get your appId and appToken
        String appId = "YOUR APP ID";
        String appToken = "YOUR APP TOKEN";
        String userId = "YOUR USER ID";
        String deviceId = "YOUR DEVICE ID";

        LoopSDK.initialize(this, appId, appToken, userId, deviceId);
    }

    @Override
    public void onInitialized() {
        Intent i = new Intent("android.intent.action.onInitialized").putExtra("status", "initialized");
        this.sendBroadcast(i);

        LoopServiceManager.startLocationProvider(SignalConfig.SIGNAL_SEND_MODE_BATCH);
        knownLocationProcessor = new KnownLocationProcessor();
        knownLocationProcessor.initialize();
    }
    @Override
    public void onInitializeFailed(LoopError loopError) {}

    @Override
    public void onServiceStatusChanged(String provider, String status, Bundle bundle) {}

    @Override
    public void onDebug(String output) {}
}
