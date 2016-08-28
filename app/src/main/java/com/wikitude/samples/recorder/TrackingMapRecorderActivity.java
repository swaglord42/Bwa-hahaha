package com.wikitude.samples.recorder;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.wikitude.WikitudeSDK;
import com.wikitude.WikitudeSDKStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.common.tracking.RecognizedTarget;
import com.wikitude.common.tracking.TrackingMapRecorderEventListener;
import com.wikitude.nativesdksampleapp.R;
import com.wikitude.rendering.InternalRendering;
import com.wikitude.samples.MainActivity;
import com.wikitude.samples.WikitudeSDKConstants;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrackingMapRecorderActivity extends Activity implements InternalRendering {

    private static final String TAG = "TrackingMapRecorder";
    private WikitudeSDK _wikitudeSDK;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _wikitudeSDK = new WikitudeSDK(this);
        WikitudeSDKStartupConfiguration startupConfiguration = new WikitudeSDKStartupConfiguration(WikitudeSDKConstants.WIKITUDE_SDK_KEY, CameraSettings.CameraPosition.BACK, CameraSettings.CameraFocusMode.CONTINUOUS);
        _wikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);

        final FrameLayout viewHolder = new FrameLayout(getApplicationContext());
        setContentView(viewHolder);

        viewHolder.addView(_wikitudeSDK.setupWikitudeGLSurfaceView());

        final LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        final RelativeLayout controls = (RelativeLayout) inflater.inflate(R.layout.activity_tracking_map_recorder, null);
        viewHolder.addView(controls);

        final EditText filenameEditText = (EditText) findViewById(R.id.tracking_map_recorder_file_name);
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_kk:mm");
        String filename = "wikitude_tracking_map_" + dateFormat.format(date);
        filenameEditText.setText(filename);

        final TableLayout startDialog = (TableLayout) findViewById(R.id.tracking_map_recorder_start_dialog);
        final Button stopButton = (Button) findViewById(R.id.tracking_map_recorder_stop_recording);
        final Button startButton = (Button) findViewById(R.id.tracking_map_recorder_start_recording);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDialog.setVisibility(View.INVISIBLE);
                _wikitudeSDK.getTrackingMapRecorder().startRecording();
                stopButton.setVisibility(View.VISIBLE);
            }
        });

        final TableLayout stopDialog = (TableLayout) findViewById(R.id.tracking_map_recorder_stop_dialog);
        final TextView saveMessage = (TextView) findViewById(R.id.tracking_map_recorder_save_message);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopButton.setVisibility(View.INVISIBLE);
                saveMessage.setVisibility(View.VISIBLE);
                _wikitudeSDK.getTrackingMapRecorder().stopRecording(filenameEditText.getText().toString());
            }
        });

        final File[] trackingMapRecording = new File[1];

        _wikitudeSDK.getTrackingMapRecorder().registerTrackingMapRecorderEventListener(new TrackingMapRecorderEventListener() {
            @Override
            public void onFinishedSavingTrackingMap(File file) {
                trackingMapRecording[0] = file;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        saveMessage.setVisibility(View.INVISIBLE);
                        stopDialog.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onErrorSavingTrackingMap(String errorMessage) {
                Log.v(TAG, errorMessage);
            }

            @Override
            public void onTrackingMapRecordingQualityChanged(int i, int i1) {
            }

            @Override
            public void onTrackingMapRecordingCanceled() {
            }

            @Override
            public void onTrackingMapRecordingUpdate(RecognizedTarget recognizedTarget) {

            }

            @Override
            public void onTrackingMapRecordingStateChanged(boolean b) {

            }
        });

        final Button discardButton = (Button) findViewById(R.id.tracking_map_recorder_discard_recording);

        discardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackingMapRecording[0].delete();
                final Intent intent = new Intent(TrackingMapRecorderActivity.this, MainActivity.class );
                startActivity(intent);
                finish();
            }
        });

        final Button shareButton = (Button) findViewById(R.id.tracking_map_recorder_share_recording);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);

                Uri uri = FileProvider.getUriForFile(TrackingMapRecorderActivity.this, "com.wikitude.nativesdksampleapp.fileprovider", trackingMapRecording[0]);

                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setType("application/json");
                startActivityForResult(Intent.createChooser(shareIntent, getResources().getText(R.string.send_tracking_map_recording)), 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            final Intent intent = new Intent(TrackingMapRecorderActivity.this, MainActivity.class );
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        _wikitudeSDK.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        _wikitudeSDK.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        _wikitudeSDK.onDestroy();
    }

    @Override
    public RenderExtension provideRenderExtension() {
        return null;
    }

}
