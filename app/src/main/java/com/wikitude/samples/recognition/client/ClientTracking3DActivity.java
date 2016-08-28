package com.wikitude.samples.recognition.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wikitude.WikitudeSDK;
import com.wikitude.WikitudeSDKStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.common.tracking.RecognizedTarget;
import com.wikitude.common.tracking.TrackingMapRecorderEventListener;
import com.wikitude.nativesdksampleapp.R;
import com.wikitude.rendering.ExternalRendering;
import com.wikitude.samples.MainActivity;
import com.wikitude.samples.WikitudeSDKConstants;
import com.wikitude.samples.rendering.external.CustomSurfaceView;
import com.wikitude.samples.rendering.external.Driver;
import com.wikitude.samples.rendering.external.GLRenderer;
import com.wikitude.samples.rendering.external.StrokedRectangle;
import com.wikitude.tracker.ClientTracker;
import com.wikitude.tracker.ClientTrackerEventListener;
import com.wikitude.tracker.Tracker;

import java.io.File;

public class ClientTracking3DActivity extends Activity implements ClientTrackerEventListener, ExternalRendering {

    private static final String TAG = "ClientTracking3D";
    private static final String TRACKING_MAP_FILENAME = "client_tracking_3d_sample_map";

    private WikitudeSDK _wikitudeSDK = null;
    private CustomSurfaceView _view = null;
    private Driver _driver = null;
    private GLRenderer _glRenderer = null;
    private int _currentTrackingQuality;
    private TextView _trackingQualityIndicator;
    private Button _stopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _wikitudeSDK = new WikitudeSDK(this);
        WikitudeSDKStartupConfiguration startupConfiguration = new WikitudeSDKStartupConfiguration(WikitudeSDKConstants.WIKITUDE_SDK_KEY, CameraSettings.CameraPosition.BACK, CameraSettings.CameraFocusMode.CONTINUOUS);
        _wikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);
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
        deleteTemporaryTrackingMap();
    }

    @Override
    public void onRenderExtensionCreated(final RenderExtension renderExtension_) {
        _glRenderer = new GLRenderer(renderExtension_);
        _glRenderer.setStrokedRectangleType(StrokedRectangle.Type.TRACKING_3D);
        _view = new CustomSurfaceView(getApplicationContext(), _glRenderer);
        _driver = new Driver(_view, 30);

        final FrameLayout viewHolder = new FrameLayout(getApplicationContext());
        setContentView(viewHolder);
        viewHolder.addView(_view);

        final LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        final RelativeLayout controls = (RelativeLayout) inflater.inflate(R.layout.activity_client_tracking_3d, null);
        viewHolder.addView(controls);

        final TextView saveMessage = (TextView) findViewById(R.id.client_tracking_3d_save_message);
        _trackingQualityIndicator = (TextView) findViewById(R.id.client_tracking_3d_quality_indicator);

        showStartDialog();

        _wikitudeSDK.getTrackingMapRecorder().registerTrackingMapRecorderEventListener(new TrackingMapRecorderEventListener() {
            @Override
            public void onFinishedSavingTrackingMap(File file) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        saveMessage.setVisibility(View.INVISIBLE);
                        _stopButton.setVisibility(View.INVISIBLE);
                    }
                });
                _wikitudeSDK.getTrackerManager().create3dClientTracker(file.getAbsolutePath()).registerTrackerEventListener(ClientTracking3DActivity.this);
            }

            @Override
            public void onErrorSavingTrackingMap(String errorMessage) {
                Log.v(TAG, errorMessage);
            }

            @Override
            public void onTrackingMapRecordingQualityChanged(final int oldTrackingQuality, final int newTrackingQuality) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _currentTrackingQuality = newTrackingQuality;
                        switch (newTrackingQuality) {
                            case -1:
                                _trackingQualityIndicator.setBackgroundColor(Color.parseColor("#FF3420"));
                                _trackingQualityIndicator.setText(R.string.tracking_quality_indicator_bad);
                                break;
                            case 0:
                                _trackingQualityIndicator.setBackgroundColor(Color.parseColor("#FFD900"));
                                _trackingQualityIndicator.setText(R.string.tracking_quality_indicator_average);
                                break;
                            default:
                                _trackingQualityIndicator.setBackgroundColor(Color.parseColor("#6BFF00"));
                                _trackingQualityIndicator.setText(R.string.tracking_quality_indicator_good);
                        }
                    }
                });
            }

            @Override
            public void onTrackingMapRecordingCanceled() {
                Log.v("MyLog", "onTrackingMapRecordingCanceled");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showStartDialog();
                    }
                });
            }

            @Override
            public void onTrackingMapRecordingUpdate(RecognizedTarget recognizedTarget) {

            }

            @Override
            public void onTrackingMapRecordingStateChanged(boolean b) {

            }
        });

        _stopButton = (Button) findViewById(R.id.client_tracking_3d_stop_recording);
        _stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _stopButton.setVisibility(View.INVISIBLE);
                _trackingQualityIndicator.setVisibility(View.INVISIBLE);
                if (_currentTrackingQuality > 0) {
                    saveMessage.setVisibility(View.VISIBLE);
                    deleteTemporaryTrackingMap();
                    _wikitudeSDK.getTrackingMapRecorder().stopRecording(TRACKING_MAP_FILENAME);
                } else {
                    showConfirmStopDialog();
                }
            }
        });
    }

    private void showStartDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClientTracking3DActivity.this);
        builder.setNegativeButton("Leave Example", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Intent intent = new Intent(ClientTracking3DActivity.this, MainActivity.class );
                startActivity(intent);
                finish();
            }
        });
        builder.setPositiveButton("Start Recording", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                _wikitudeSDK.getTrackingMapRecorder().startRecording();
                _trackingQualityIndicator.setVisibility(View.VISIBLE);
                _stopButton.setVisibility(View.VISIBLE);
                _stopButton.setEnabled(true);
            }
        });
        builder.setMessage("For this example to work, a Tracking Map needs to be recorded. \n\nPlease tap 'Start' below to start recording a Tracking Map.\n\nNote: Point the device to the center of the scene that you're about to record.")
                .setTitle("Tracking Map Required");
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }


    private void showConfirmStopDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClientTracking3DActivity.this);
        builder.setNegativeButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _wikitudeSDK.getTrackingMapRecorder().cancelRecording();
            }
        });
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteTemporaryTrackingMap();
                _wikitudeSDK.getTrackingMapRecorder().stopRecording(TRACKING_MAP_FILENAME);
            }
        });
        builder.setMessage("In order to experience a well working 3d tracking example, please continue recording until the quality indicator says 'Good'.")
                .setTitle("Tracking Map quality not sufficient");
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    private void deleteTemporaryTrackingMap() {
        File file = new File(_wikitudeSDK.getTrackingMapRecorder().getTrackingMapStorageLocation(), TRACKING_MAP_FILENAME);
        if (file.isFile()) {
            file.delete();
        }
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

}