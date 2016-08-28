package com.wikitude.samples.recognition.client;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

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
import com.wikitude.samples.rendering.external.GLRendererExtendedTracking;
import com.wikitude.tracker.ClientTracker;
import com.wikitude.tracker.ClientTrackerEventListener;
import com.wikitude.tracker.Tracker;

public class ExtendedClientTrackingActivity extends Activity implements ClientTrackerEventListener, ExternalRendering {

    private static final String TAG = "ExtendedTracking";

    private WikitudeSDK _wikitudeSDK;
    private CustomSurfaceView _view;
    private Driver _driver;
    private GLRendererExtendedTracking _glRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _wikitudeSDK = new WikitudeSDK(this);
        WikitudeSDKStartupConfiguration startupConfiguration = new WikitudeSDKStartupConfiguration(WikitudeSDKConstants.WIKITUDE_SDK_KEY, CameraSettings.CameraPosition.BACK, CameraSettings.CameraFocusMode.CONTINUOUS);
        _wikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);
        ClientTracker tracker = _wikitudeSDK.getTrackerManager().create2dClientTracker("file:///android_asset/stones.wtc", new String[]{"*"});
        tracker.registerTrackerEventListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _wikitudeSDK.onResume();
        _view.onResume();
        _driver.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _wikitudeSDK.onPause();
        _view.onPause();
        _driver.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _wikitudeSDK.onDestroy();
    }

    @Override
    public void onRenderExtensionCreated(final RenderExtension renderExtension_) {
        _glRenderer = new GLRendererExtendedTracking(renderExtension_);
        _view = new CustomSurfaceView(getApplicationContext(), _glRenderer);
        _driver = new Driver(_view, 30);

        FrameLayout viewHolder = new FrameLayout(getApplicationContext());
        setContentView(viewHolder);

        viewHolder.addView(_view);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        RelativeLayout trackingQualityIndicator = (RelativeLayout) inflater.inflate(R.layout.activity_extended_tracking, null);
        viewHolder.addView(trackingQualityIndicator);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EditText trackingQualityIndicator = (EditText) findViewById(R.id.tracking_quality_indicator);
                trackingQualityIndicator.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onExtendedTrackingQualityUpdate(final Tracker tracker_, final String targetName_, final int oldTrackingQuality_, final int newTrackingQuality_) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EditText trackingQualityIndicator = (EditText) findViewById(R.id.tracking_quality_indicator);
                switch (newTrackingQuality_) {
                    case -1:
                        trackingQualityIndicator.setBackgroundColor(Color.parseColor("#FF3420"));
                        trackingQualityIndicator.setText(R.string.tracking_quality_indicator_bad);
                        break;
                    case 0:
                        trackingQualityIndicator.setBackgroundColor(Color.parseColor("#FFD900"));
                        trackingQualityIndicator.setText(R.string.tracking_quality_indicator_average);
                        break;
                    default:
                        trackingQualityIndicator.setBackgroundColor(Color.parseColor("#6BFF00"));
                        trackingQualityIndicator.setText(R.string.tracking_quality_indicator_good);
                }
                trackingQualityIndicator.setVisibility(View.VISIBLE);
            }
        });
    }

}