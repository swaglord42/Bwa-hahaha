package com.wikitude.samples.plugins;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.wikitude.WikitudeSDK;
import com.wikitude.WikitudeSDKStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.common.tracking.RecognizedTarget;
import com.wikitude.rendering.ExternalRendering;
import com.wikitude.samples.WikitudeSDKConstants;
import com.wikitude.samples.rendering.external.CustomSurfaceView;
import com.wikitude.samples.rendering.external.Driver;
import com.wikitude.samples.rendering.external.GLRenderer;
import com.wikitude.tracker.ClientTracker;
import com.wikitude.tracker.ClientTrackerEventListener;
import com.wikitude.tracker.Tracker;

import java.nio.ByteBuffer;

public class CustomCameraActivity extends Activity implements ClientTrackerEventListener, ExternalRendering {

    private static final String TAG = "CustomCamera";

    private WikitudeSDK _wikitudeSDK;
    private CustomSurfaceView _view;
    private Driver _driver;
    private GLRenderer _glRenderer;
    private WikitudeCamera2 _wikitudeCamera2;
    private WikitudeCamera _wikitudeCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _wikitudeSDK = new WikitudeSDK(this);
        WikitudeSDKStartupConfiguration startupConfiguration = new WikitudeSDKStartupConfiguration(WikitudeSDKConstants.WIKITUDE_SDK_KEY, CameraSettings.CameraPosition.BACK, CameraSettings.CameraFocusMode.CONTINUOUS);
        _wikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);
        ClientTracker tracker = _wikitudeSDK.getTrackerManager().create2dClientTracker("file:///android_asset/magazine.wtc");
        tracker.registerTrackerEventListener(this);

        _wikitudeSDK.getPluginManager().registerNativePlugins("wikitudePlugins", "customcamera");
        initNative();
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
        _glRenderer = new GLRenderer(renderExtension_);
        _view = new CustomSurfaceView(getApplicationContext(), _glRenderer);
        _driver = new Driver(_view, 30);
        setContentView(_view);
    }

    @Override
    public void onErrorLoading(final ClientTracker clientTracker_, final String errorMessage_) {
        Log.v(TAG, "onErrorLoading: " + errorMessage_);
    }

    @Override
    public void onTrackerFinishedLoading(final ClientTracker clientTracker_, final String trackerFilePath_) {
        Log.v(TAG, "onTrackerFinishedLoading: " + trackerFilePath_);
    }

    @Override
    public void onTargetRecognized(final Tracker tracker_, final String targetName_) {
        Log.v(TAG, "onTargetRecognized: " + targetName_);
    }

    @Override
    public void onTracking(final Tracker tracker_, final RecognizedTarget recognizedTarget_) {
    }

    @Override
    public void onTargetLost(final Tracker tracker_, final String targetName_) {
    }

    @Override
    public void onExtendedTrackingQualityUpdate(final Tracker tracker_, final String targetName_, final int oldTrackingQuality_, final int newTrackingQuality_) {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onInputPluginInitialized() {
        Log.v(TAG, "onInputPluginInitialized");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    _wikitudeCamera2 = new WikitudeCamera2(CustomCameraActivity.this, 640, 480);
                    setFrameSize(_wikitudeCamera2.getFrameWidth(), _wikitudeCamera2.getFrameHeight());
                    setCameraFieldOfView((_wikitudeCamera2.getCameraFieldOfView()));

                    int imageSensorRotation = _wikitudeCamera2.getImageSensorRotation();
                    if (imageSensorRotation != 0) {
                        setImageSensorRotation(imageSensorRotation);
                    }
                }
                else*/
                {
                    _wikitudeCamera = new WikitudeCamera(640, 480);
                    setFrameSize(_wikitudeCamera.getFrameWidth(), _wikitudeCamera.getFrameHeight());

                    if(isCameraLandscape()) {
                        setDefaultDeviceOrientationLandscape(true);
                    }

                    int imageSensorRotation = _wikitudeCamera.getImageSensorRotation();
                    if (imageSensorRotation != 0) {
                        setImageSensorRotation(imageSensorRotation);
                    }
                }
            }
        });
    }

    public void onInputPluginPaused() {
        Log.v(TAG, "onInputPluginPaused");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
                {
                    _wikitudeCamera2.close();
                }
                else*/
                {
                    _wikitudeCamera.close();
                }
            }
        });
    }

    public void onInputPluginResumed() {
        Log.v(TAG, "onInputPluginResumed");


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    CustomCameraActivity.this._wikitudeCamera2.start(new ImageReader.OnImageAvailableListener() {
                        @Override
                        public void onImageAvailable(ImageReader reader) {
                            Image image = reader.acquireLatestImage();

                            if (null != image && null != image.getPlanes()) {
                                Image.Plane[] planes = image.getPlanes();

                                int widthLuminance = image.getWidth();

                                // 4:2:0 format -> chroma planes have half the width of luma channel
                                int widthChrominance = widthLuminance / 2;

                                int pixelStrideLuminance = planes[0].getPixelStride();
                                int rowStrideLuminance = planes[0].getRowStride();

                                int pixelStrideBlue = planes[1].getPixelStride();
                                int rowStrideBlue = planes[1].getRowStride();

                                int pixelStrideRed = planes[2].getPixelStride();
                                int rowStrideRed = planes[2].getRowStride();

                                notifyNewCameraFrame(
                                        widthLuminance,
                                        getPlanePixelPointer(planes[0].getBuffer()),
                                        pixelStrideLuminance,
                                        rowStrideLuminance,
                                        widthChrominance,
                                        getPlanePixelPointer(planes[1].getBuffer()),
                                        pixelStrideBlue,
                                        rowStrideBlue,
                                        getPlanePixelPointer(planes[2].getBuffer()),
                                        pixelStrideRed,
                                        rowStrideRed
                                );

                                image.close();
                            }
                        }
                    });
                }
                else*/
                {
                    _wikitudeCamera.start(new Camera.PreviewCallback() {
                        @Override
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            notifyNewCameraFrameN21(data);
                        }
                    });
                    setCameraFieldOfView(_wikitudeCamera.getCameraFieldOfView());
                }
            }
        });
    }

    public void onInputPluginDestroyed() {
        Log.v(TAG, "onInputPluginDestroyed");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                    _wikitudeCamera2.close();
                }
                else*/
                {
                    _wikitudeCamera.close();
                }
            }
        });
    }

    private byte[] getPlanePixelPointer(ByteBuffer pixelBuffer) {
        byte[] bytes;
        if (pixelBuffer.hasArray()) {
            bytes = pixelBuffer.array();
        } else {
            bytes = new byte[pixelBuffer.remaining()];
            pixelBuffer.get(bytes);
        }

        return bytes;
    }

    public boolean isCameraLandscape(){
        final Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final DisplayMetrics dm = new DisplayMetrics();
        final int rotation = display.getRotation();

        display.getMetrics(dm);

        final boolean is90off = rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270;
        final boolean isLandscape = dm.widthPixels > dm.heightPixels;

        return is90off ^ isLandscape;
    }

    private native void initNative();
    private native void notifyNewCameraFrame(int widthLuminance, byte[] pixelPointerLuminance, int pixelStrideLuminance, int rowStrideLuminance, int widthChrominance, byte[] pixelPointerChromaBlue, int pixelStrideBlue, int rowStrideBlue, byte[] pixelPointerChromaRed, int pixelStrideRed, int rowStrideRed);
    private native void notifyNewCameraFrameN21(byte[] frameData);
    private native void setCameraFieldOfView(double fieldOfView);
    private native void setFrameSize(int frameWidth, int frameHeight);
    private native void setDefaultDeviceOrientationLandscape(boolean isLandscape);
    private native void setImageSensorRotation(int rotation);
}
