package com.wikitude.samples.plugins;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
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
import android.os.Vibrator;


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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import android.content.SharedPreferences;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import android.os.CountDownTimer;
import android.widget.TextView;

public class FaceDetectionPluginActivity extends AppCompatActivity implements ClientTrackerEventListener, ExternalRendering, View.OnClickListener {

    private SharedPreferences gamePrefs;
    public static final String GAME_PREFS = "ArithmeticFile";


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
    int f=1;boolean flag = false; int index=0,index3=0;
    LayoutInflater inflater;
    LinearLayout barcodeLayout;
    TextView textView;
    String timerValue="Time left:";
    MediaPlayer mp;
    int index2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _wikitudeSDK = new WikitudeSDK(this);

        WikitudeSDKStartupConfiguration startupConfiguration = new WikitudeSDKStartupConfiguration(WikitudeSDKConstants.WIKITUDE_SDK_KEY, CameraSettings.CameraPosition.BACK, CameraSettings.CameraFocusMode.CONTINUOUS);
        _wikitudeSDK.onCreate(getApplicationContext(), this, startupConfiguration);
        ClientTracker tracker = _wikitudeSDK.getTrackerManager().create2dClientTracker("file:///android_asset/magazine.wtc");
        tracker.registerTrackerEventListener(this);
        _wikitudeSDK.getPluginManager().registerNativePlugins("wikitudePlugins", "face_detection");
        mp = MediaPlayer.create(this, R.raw.gun_shot1);
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

        // declared an object of CounterClass which is a class that extends the CountDownTimer class

        final CounterClass timer = new CounterClass(30000,1000);
        timer.start();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //save state

        int exScore = getScore();
        savedInstanceState.putInt("score", exScore);
        super.onSaveInstanceState(savedInstanceState);
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
        setHighScore();
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
        textView=(TextView)barcodeLayout.findViewById(R.id.textView2);
        viewHolder.addView(barcodeLayout);

        gamePrefs = getSharedPreferences(GAME_PREFS, 0);

    }
    public class CounterClass extends CountDownTimer {
        public CounterClass(long millisInFuture,long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }
        @Override
        public void onTick(long millisUntilFinished) {
            long millis = millisUntilFinished;
            String s = String.valueOf(millis/1000);
            //String hms = String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(millis);
            //textView.setText(timerValue+millisUntilFinished/1000);
            //System.out.println(hms);
            textView.setText("Time left:"+s);
        }
        @Override
        public void onFinish() {
            done();
        }
    }

    public void done(){
        //called after the timer finishes counting
        setHighScore();

        Intent i = new Intent(this,ScoreView.class);
        i.putExtra("Score",gamescore);
        startActivity(i);
    }


    private void setHighScore(){
        //set high score
        int exScore = getScore();




        if(exScore>0) {

            SharedPreferences.Editor scoreEdit = gamePrefs.edit();
            DateFormat dateForm = new SimpleDateFormat("dd MMMM yyyy");
            String dateOutput = dateForm.format(new Date());
            String scores = gamePrefs.getString("highScores", "");

            List<Score> scoreStrings = new ArrayList<Score>();
            String[] exScores = scores.split("\\|");
            for(String eSc : exScores){




                String[] parts = eSc.split(" - ");
                if(index<parts.length-1) {
                    scoreStrings.add(new Score(parts[index], Integer.parseInt(parts[++index])));
                }
            }



            Score newScore = new Score(dateOutput, exScore);
            scoreStrings.add(newScore);
            Collections.sort(scoreStrings);


            StringBuilder scoreBuild = new StringBuilder("");
            for(int s=0; s<scoreStrings.size(); s++){
                if(s>=10) break;//only want ten
                if(s>0) scoreBuild.append("|");//pipe separate the score strings
                scoreBuild.append(scoreStrings.get(s).getScoreText());
            }
//write to prefs
            scoreEdit.putString("highScores", scoreBuild.toString());
            scoreEdit.commit();

            if(scores.length()>0){
                //we have existing scores
            }
            else{
                //no existing scores
                scoreEdit.putString("highScores", ""+dateOutput+" - "+exScore);
                scoreEdit.commit();
            }
        }
    }


    public class Score implements Comparable<Score> {
        private String scoreDate;
        public int scoreNum;

        public Score(String date, int num){
            scoreDate=date;
            scoreNum=num;
        }

        public int compareTo(Score sc){
            //return 0 if equal
            //1 if passed greater than this
            //-1 if this greater than passed
            return sc.scoreNum>scoreNum? 1 : sc.scoreNum<scoreNum? -1 : 0;
        }

        public String getScoreText()
        {
            return scoreDate+" - "+scoreNum;
        }
    }


    int getScore(){
        return gamescore;
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
        mp.start();
        Context context=this;
        Vibrator vibrator = (Vibrator)this.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(250);
                Animation hypejump = AnimationUtils.loadAnimation(this,R.anim.animation_xml);
                hypejump.setRepeatCount(Animation.INFINITE);
                if(flag && f%2==0)
                {
                    image.setVisibility(View.VISIBLE);
                    image.startAnimation(hypejump);
                    image.clearAnimation();
                    gamescore++;

                    //before the line in which the score textview is updated to zero to show that the user has entered a wrong answer

        //            setHighScore();

                }
                else{
                    image.setVisibility(View.INVISIBLE);
                }
        if (flag)
            f++;
        flag = false;
            }
        }
//        if (flag)
//            f++;
//        flag = false;



//    protected void onDestroy(){
//        setHighScore();
//        super.onDestroy();
//    }
