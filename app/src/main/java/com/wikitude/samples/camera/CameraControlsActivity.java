package com.wikitude.samples.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;

import com.wikitude.WikitudeSDK;
import com.wikitude.WikitudeSDKStartupConfiguration;
import com.wikitude.camera.CameraManagerListener;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.nativesdksampleapp.R;
import com.wikitude.rendering.ExternalRendering;
import com.wikitude.samples.WikitudeSDKConstants;
import com.wikitude.samples.rendering.external.CustomSurfaceView;
import com.wikitude.samples.rendering.external.Driver;
import com.wikitude.samples.rendering.external.GLRenderer;

public class CameraControlsActivity extends Activity implements ExternalRendering, AdapterView.OnItemSelectedListener, CameraManagerListener {

    private static final String TAG = "CameraControlsActivity";

    private WikitudeSDK _wikitudeSDK;
    private GLRenderer _glRenderer;
    private CustomSurfaceView _customSurfaceView;
    private Driver _driver;

    private boolean _isCameraOpen;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _wikitudeSDK = new WikitudeSDK(this);
        _wikitudeSDK.getCameraManager().setListener(this);
        WikitudeSDKStartupConfiguration startupConfiguration = new WikitudeSDKStartupConfiguration(WikitudeSDKConstants.WIKITUDE_SDK_KEY, CameraSettings.CameraPosition.BACK, CameraSettings.CameraFocusMode.CONTINUOUS);
        _wikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);
    }

    @Override
    protected void onPause() {
        super.onPause();
        _wikitudeSDK.onPause();
        _customSurfaceView.onPause();
        _driver.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        _wikitudeSDK.onResume();
        _customSurfaceView.onResume();
        _driver.start();
    }

    @Override
    public void onRenderExtensionCreated(final RenderExtension renderExtension_) {
        _glRenderer = new GLRenderer(renderExtension_);
        _customSurfaceView = new CustomSurfaceView(getApplicationContext(), _glRenderer);
        _driver = new Driver(_customSurfaceView, 30);
    }

    @Override
    public void onItemSelected(final AdapterView<?> adapterView_, final View view_, final int position_, final long id_) {
        if (_isCameraOpen) {
            switch (adapterView_.getId()) {
                case R.id.focusMode:
                    if (position_ == 0) {
                        _wikitudeSDK.getCameraManager().setFocusMode(CameraSettings.CameraFocusMode.CONTINUOUS);
                    } else {
                        _wikitudeSDK.getCameraManager().setFocusMode(CameraSettings.CameraFocusMode.ONCE);
                    }
                    break;
                case R.id.cameraPosition:
                    if (position_ == 0) {
                        _wikitudeSDK.getCameraManager().setCameraPosition(CameraSettings.CameraPosition.BACK);
                    } else {
                        _wikitudeSDK.getCameraManager().setCameraPosition(CameraSettings.CameraPosition.FRONT);
                    }
                    break;
            }
        } else {
            Log.e("CAMERA_OPEN", "camera is not open");
        }
    }

    @Override
    public void onNothingSelected(final AdapterView<?> adapterView_) {

    }

    @Override
    public void onCameraOpen(Camera camera) {

        if (!_isCameraOpen) {
            FrameLayout viewHolder = new FrameLayout(getApplicationContext());
            setContentView(viewHolder);

            viewHolder.addView(_customSurfaceView);

            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            LinearLayout controls = (LinearLayout) inflater.inflate(R.layout.activity_camera_control, null);
            viewHolder.addView(controls);

            Spinner cameraPositionSpinner = (Spinner) findViewById(R.id.cameraPosition);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.camera_positions, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            cameraPositionSpinner.setAdapter(adapter);
            cameraPositionSpinner.setOnItemSelectedListener(this);

            Spinner focusModeSpinner = (Spinner) findViewById(R.id.focusMode);
            adapter = ArrayAdapter.createFromResource(this, R.array.focus_mode, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            focusModeSpinner.setAdapter(adapter);
            focusModeSpinner.setOnItemSelectedListener(this);

            Switch flashToggleButton = (Switch) findViewById(R.id.flashlight);
            flashToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                    if (isChecked) {
                        _wikitudeSDK.getCameraManager().enableCameraFlashLight();
                    } else {
                        _wikitudeSDK.getCameraManager().disableCameraFlashLight();
                    }

                }
            });

            SeekBar zoomSeekBar = (SeekBar) findViewById(R.id.zoomSeekBar);
            zoomSeekBar.setMax(((int) _wikitudeSDK.getCameraManager().getMaxZoomLevel()) * 100);
            zoomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                    if (progress > 0) {
                        _wikitudeSDK.getCameraManager().setZoomLevel((float) progress / 100.0f);
                    }
                }

                @Override
                public void onStartTrackingTouch(final SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(final SeekBar seekBar) {

                }
            });
        }
        _isCameraOpen = true;
    }

    @Override
    public void onCameraReleased() {
    }

    @Override
    public void onCameraOpenFailure() {
    }
}
