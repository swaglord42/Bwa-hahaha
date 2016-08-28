package com.wikitude.samples.plugins;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wikitude.WikitudeSDK;
import com.wikitude.WikitudeSDKStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.common.tracking.RecognizedTarget;
import com.wikitude.nativesdksampleapp.R;
import com.wikitude.rendering.ExternalRendering;
import com.wikitude.samples.WikitudeSDKConstants;
import com.wikitude.samples.rendering.external.CustomSurfaceView;
import com.wikitude.samples.rendering.external.Driver;
import com.wikitude.samples.rendering.external.GLRenderer;
import com.wikitude.tracker.ClientTracker;
import com.wikitude.tracker.ClientTrackerEventListener;
import com.wikitude.tracker.Tracker;

public class BarcodePluginActivity extends Activity implements ClientTrackerEventListener, ExternalRendering {

    private static final String TAG = "BarcodePlugin";
    private static String _codeContent;

    private WikitudeSDK _wikitudeSDK;
    private CustomSurfaceView _customSurfaceView;
    private Driver _driver;
    private GLRenderer _glRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _wikitudeSDK = new WikitudeSDK(this);
        WikitudeSDKStartupConfiguration startupConfiguration = new WikitudeSDKStartupConfiguration(WikitudeSDKConstants.WIKITUDE_SDK_KEY, CameraSettings.CameraPosition.BACK, CameraSettings.CameraFocusMode.CONTINUOUS);
        _wikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);
        ClientTracker tracker = _wikitudeSDK.getTrackerManager().create2dClientTracker("file:///android_asset/magazine.wtc");
        tracker.registerTrackerEventListener(this);
        _wikitudeSDK.getPluginManager().registerNativePlugins("wikitudePlugins", "barcode");
        initNative();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _wikitudeSDK.onResume();
        _customSurfaceView.onResume();
        _driver.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _wikitudeSDK.onPause();
        _customSurfaceView.onPause();
        _driver.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _wikitudeSDK.onDestroy();
    }

    @Override
    public void onRenderExtensionCreated(final RenderExtension renderExtension_) {
        _glRenderer = new GLRenderer(renderExtension_);
        _customSurfaceView = new CustomSurfaceView(getApplicationContext(), _glRenderer);
        _driver = new Driver(_customSurfaceView, 30);
        setContentView(_customSurfaceView);
        FrameLayout viewHolder = new FrameLayout(getApplicationContext());
        setContentView(viewHolder);

        viewHolder.addView(_customSurfaceView);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        LinearLayout barcodeLayout = (LinearLayout) inflater.inflate(R.layout.activity_barcode_plugin, null);
        viewHolder.addView(barcodeLayout);
    }

    @Override
    public void onErrorLoading(final ClientTracker clientTracker_, final String errorMessage_) {
        Log.v(TAG, "onErrorLoading: " + errorMessage_);
    }

    @Override
    public void onTrackerFinishedLoading(final ClientTracker clientTracker_, final String trackerFilePath_) {

    }

    @Override
    public void onTargetRecognized(final Tracker tracker_, final String targetName_) {

    }

    @Override
    public void onTracking(final Tracker tracker_, final RecognizedTarget recognizedTarget_) {
        _glRenderer.setCurrentlyRecognizedTarget(recognizedTarget_);
    }

    @Override
    public void onTargetLost(final Tracker tracker_, final String targetName_) {
        _glRenderer.setCurrentlyRecognizedTarget(null);
    }

    @Override
    public void onExtendedTrackingQualityUpdate(final Tracker tracker_, final String targetName_, final int oldTrackingQuality_, final int newTrackingQuality_) {

    }

    public void onBarcodeDetected(final String codeContent_) {
        _codeContent = codeContent_;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EditText targetInformationTextField = (EditText) findViewById(R.id.barcode_plugin_info_field);
                targetInformationTextField.setText("Scan result: " + codeContent_, TextView.BufferType.NORMAL);
            }
        });
    }

    private native void initNative();
}