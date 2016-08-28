package com.wikitude.samples.recognition.cloud;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import com.wikitude.tracker.CloudTracker;
import com.wikitude.tracker.CloudTrackerEventListener;
import com.wikitude.tracker.Tracker;

import org.json.JSONObject;

public class OnClickCloudTrackingActivity extends Activity implements CloudTrackerEventListener, ExternalRendering {

    private static final String TAG = "OnClickCloudTracking";

    private WikitudeSDK _wikitudeSDK;
    private CustomSurfaceView _customSurfaceView;
    private Driver _driver;
    private GLRenderer _glRenderer;
    private CloudTracker _cloudTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _wikitudeSDK = new WikitudeSDK(this);
        WikitudeSDKStartupConfiguration startupConfiguration = new WikitudeSDKStartupConfiguration(WikitudeSDKConstants.WIKITUDE_SDK_KEY, CameraSettings.CameraPosition.BACK, CameraSettings.CameraFocusMode.CONTINUOUS);
        _wikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);
        _cloudTracker = _wikitudeSDK.getTrackerManager().create2dCloudTracker("b277eeadc6183ab57a83b07682b3ceba", "54e4b9fe6134bb74351b2aa3");
        _cloudTracker.registerTrackerEventListener(this);
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

        FrameLayout viewHolder = new FrameLayout(getApplicationContext());
        setContentView(viewHolder);

        viewHolder.addView(_customSurfaceView);

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        LinearLayout controls = (LinearLayout) inflater.inflate(R.layout.activity_on_click_cloud_tracking, null);
        viewHolder.addView(controls);

        Button recognizeButton = (Button) findViewById(R.id.on_click_cloud_tracking_recognize_button);
        recognizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view_) {
                _cloudTracker.recognize();
            }
        });

    }

    @Override
    public void onTrackerFinishedLoading(final CloudTracker cloudTracker_) {

    }

    @Override
    public void onTrackerLoadingError(final CloudTracker cloudTracker_, final String errorMessage_) {
        Log.d(TAG, "onTrackerLoadingError: " + errorMessage_);
    }

    @Override
    public void onTargetRecognized(final Tracker cloudTracker_, final String targetName_) {

    }

    @Override
    public void onTracking(final Tracker cloudTracker_, final RecognizedTarget recognizedTarget_) {
        _glRenderer.setCurrentlyRecognizedTarget(recognizedTarget_);
    }

    @Override
    public void onTargetLost(final Tracker cloudTracker_, final String targetName_) {
        _glRenderer.setCurrentlyRecognizedTarget(null);
    }

    @Override
    public void onExtendedTrackingQualityUpdate(final Tracker tracker_, final String targetName_, final int oldTrackingQuality_, final int newTrackingQuality_) {

    }

    @Override
    public void onRecognitionFailed(final CloudTracker cloudTracker_, final int errorCode_, final String errorMessage_) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EditText targetInformationTextField = (EditText) findViewById(R.id.on_click_cloud_tracking_info_field);
                targetInformationTextField.setText("Recognition failed - Error code: " + errorCode_ + " Message: " + errorMessage_);
                targetInformationTextField.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onRecognitionSuccessful(final CloudTracker cloudTracker_, boolean recognized_, final JSONObject jsonObject_) {
        if (recognized_) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        EditText targetInformationTextField = (EditText) findViewById(R.id.on_click_cloud_tracking_info_field);
                        targetInformationTextField.setText(jsonObject_.toString(), TextView.BufferType.NORMAL);
                        targetInformationTextField.setVisibility(View.VISIBLE);
                    }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EditText targetInformationTextField = (EditText) findViewById(R.id.on_click_cloud_tracking_info_field);
                    targetInformationTextField.setText("Recognition failed - Please try again", TextView.BufferType.NORMAL);
                    targetInformationTextField.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    @Override
    public void onRecognitionInterruption(final CloudTracker cloudTracker_, final double suggestedInterval_) {

    }

}