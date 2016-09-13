package com.wikitude.samples.plugins;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wikitude.WikitudeSDK;
import com.wikitude.WikitudeSDKStartupConfiguration;
import com.wikitude.common.camera.CameraSettings;
import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.common.tracking.RecognizedTarget;
import com.wikitude.nativesdksampleapp.R;
import com.wikitude.rendering.ExternalRendering;
import com.wikitude.samples.ScoreView;
import com.wikitude.samples.WikitudeSDKConstants;
import com.wikitude.samples.rendering.external.CustomSurfaceView;
import com.wikitude.samples.rendering.external.Driver;
import com.wikitude.samples.rendering.external.GLRenderer;
import com.wikitude.samples.rendering.external.GLRendererFaceDetectionPlugin;
import com.wikitude.tracker.ClientTracker;
import com.wikitude.tracker.ClientTrackerEventListener;
import com.wikitude.tracker.Tracker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.os.CountDownTimer;
import android.widget.TextView;

public class FaceDetectionPluginActivity extends Activity implements ClientTrackerEventListener, ExternalRendering, View.OnClickListener {

    private static final String TAG = "FaceDetectionPlugin";
    ImageView image;
    private WikitudeSDK _wikitudeSDK;
    private CustomSurfaceView _customSurfaceView;
    private Driver _driver;
    private GLRendererFaceDetectionPlugin _glRenderer;
    private GLRendererFaceDetectionPlugin _glRenderer1;
    Button kill;
    int gamescore=0;
    private File _cascadeFile;
    private RecognizedTarget _faceTarget = new RecognizedTarget();
    private int _defaultOrientation;
    private WikitudeCamera2 _wikitudeCamera2;
    private WikitudeCamera _wikitudeCamera;
    int f=1;boolean flag = false;
    LayoutInflater inflater;
    LinearLayout barcodeLayout;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textView = (TextView)findViewById(R.id.textView2);
        kill = (Button)findViewById(R.id.takepicture);
        _wikitudeSDK = new WikitudeSDK(this);

        WikitudeSDKStartupConfiguration startupConfiguration = new WikitudeSDKStartupConfiguration(WikitudeSDKConstants.WIKITUDE_SDK_KEY, CameraSettings.CameraPosition.BACK, CameraSettings.CameraFocusMode.CONTINUOUS);
        _wikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);
        ClientTracker tracker = _wikitudeSDK.getTrackerManager().create2dClientTracker("file:///android_asset/magazine.wtc");
        tracker.registerTrackerEventListener(this);
        _wikitudeSDK.getPluginManager().registerNativePlugins("wikitudePlugins", "face_detection");

        try {
            // load cascade file from application resources
            InputStream is = getResources().openRawResource(R.raw.high_database);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            _cascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
            FileOutputStream os = new FileOutputStream(_cascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            initNative(_cascadeFile.getAbsolutePath());

            cascadeDir.delete();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }

        _faceTarget.setViewMatrix(new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        _faceTarget.setProjectionMatrix(new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        evaluateDeviceDefaultOrientation();
        if (_defaultOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setIsBaseOrientationLandscape(true);
        }
        final CountDownTimer timer = new CountDownTimer(30000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            textView.setText("Time Remaining: "+millisUntilFinished/1000);
            }

            @Override
            public void onFinish() {
            textView.setText("TIME OVER !!");
                done();
            }
        }.start();
    }
//    @Override
    public void done() {
        Intent i = new Intent(this,ScoreView.class);
        i.putExtra("Score",gamescore);
        startActivity(i);
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

    public void evaluateDeviceDefaultOrientation() {
        WindowManager windowManager =  (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Configuration config = getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        if ( ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) &&
                config.orientation == Configuration.ORIENTATION_LANDSCAPE)
                || ((rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) &&
                config.orientation == Configuration.ORIENTATION_PORTRAIT)) {
            _defaultOrientation = Configuration.ORIENTATION_LANDSCAPE;
        } else {
            _defaultOrientation = Configuration.ORIENTATION_PORTRAIT;
        }
    }

    public void onFaceDetected(float[] modelViewMatrix) {
        _faceTarget.setViewMatrix(modelViewMatrix);
        _glRenderer.setCurrentlyRecognizedFace(_faceTarget);
        flag = true;

    }

    public void onFaceLost() {
        _glRenderer.setCurrentlyRecognizedFace(null);
    }

    public void onProjectionMatrixChanged(float[] projectionMatrix) {
        _faceTarget.setProjectionMatrix(projectionMatrix);
        _glRenderer.setCurrentlyRecognizedFace(_faceTarget);
    }

    @Override
    public void onRenderExtensionCreated(final RenderExtension renderExtension_) {
        _glRenderer = new GLRendererFaceDetectionPlugin(renderExtension_);

        _customSurfaceView = new CustomSurfaceView(getApplicationContext(), _glRenderer);
        _driver = new Driver(_customSurfaceView, 30);
        setContentView(_customSurfaceView);
        FrameLayout viewHolder = new FrameLayout(getApplicationContext());
        setContentView(viewHolder);
        viewHolder.addView(_customSurfaceView);

        inflater = LayoutInflater.from(getApplicationContext());
        barcodeLayout = (LinearLayout) inflater.inflate(R.layout.control, null);
        kill = (Button)barcodeLayout.findViewById(R.id.takepicture);
        kill.setOnClickListener(this);
        image = (ImageView)barcodeLayout.findViewById(R.id.imagview);
        image.setVisibility(View.INVISIBLE);
        viewHolder.addView(barcodeLayout);
    }

    @Override
    public void onErrorLoading(final ClientTracker clientTracker_, final String errorMessage_) {
        Log.v(TAG, "onErrorLoading: " + errorMessage_);
    }

    @Override
    public void onTrackerFinishedLoading(final ClientTracker clientTracker_, final String trackerFilePath_) {
        flag = true;
    }

    @Override
    public void onTargetRecognized(final Tracker tracker_, final String targetName_) {
        flag = true;
    }

    @Override
    public void onTracking(final Tracker tracker_, final RecognizedTarget recognizedTarget_) {
        _glRenderer.setCurrentlyRecognizedTarget(recognizedTarget_);
        flag = true;
    }

    @Override
    public void onTargetLost(final Tracker tracker_, final String targetName_) {
        _glRenderer.setCurrentlyRecognizedTarget(null);
    }

    @Override
    public void onExtendedTrackingQualityUpdate(final Tracker tracker_, final String targetName_, final int oldTrackingQuality_, final int newTrackingQuality_) {

    }

    private native void initNative(String casecadeFilePath);
    private native void setIsBaseOrientationLandscape(boolean isBaseOrientationLandscape_);

    @Override
    public void onClick(View v) {

        Animation hypejump = AnimationUtils.loadAnimation(this,R.anim.animation_xml);
        hypejump.setRepeatCount(Animation.INFINITE);
        if(flag && f%2==0)
        {
            image.setVisibility(View.VISIBLE);
            gamescore++;
            image.startAnimation(hypejump);
            image.clearAnimation();
        }
        else{
            image.setVisibility(View.INVISIBLE);
        }
        if (flag)
            f++;
        flag = false;

    }
}