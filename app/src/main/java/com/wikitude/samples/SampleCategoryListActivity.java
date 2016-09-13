package com.wikitude.samples;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.wikitude.nativesdksampleapp.R;
import com.wikitude.samples.camera.CameraControlsActivity;
import com.wikitude.samples.plugins.BarcodePluginActivity;
import com.wikitude.samples.plugins.CustomCameraActivity;
import com.wikitude.samples.plugins.FaceDetectionPluginActivity;
import com.wikitude.samples.recognition.client.ClientTracking3DActivity;
import com.wikitude.samples.recognition.client.ExtendedClientTrackingActivity;
import com.wikitude.samples.recognition.client.SimpleClientTrackingActivity;
import com.wikitude.samples.recognition.cloud.ContinuousCloudTrackingActivity;
import com.wikitude.samples.recognition.cloud.OnClickCloudTrackingActivity;
import com.wikitude.samples.rendering.external.ExternalRenderingActivity;
import com.wikitude.samples.rendering.internal.InternalRenderingActivity;


public class SampleCategoryListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String EXTRAS_CATEGORY_POSITION = "categoryPosition";
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    private ListView _listView;
    private Class _activityClass = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_main);
//        _listView = (ListView) findViewById(R.id.sample_list);

        ArrayAdapter<CharSequence> adapter = null;
        switch (getIntent().getExtras().getInt(EXTRAS_CATEGORY_POSITION)) {
            case 0:
                adapter = ArrayAdapter.createFromResource(this, R.array.clientRecognition_samples, android.R.layout.simple_list_item_1);
                break;
            case 1:
                adapter = ArrayAdapter.createFromResource(this, R.array.cloudRecognition_samples, android.R.layout.simple_list_item_1);
                break;
            case 2:
                adapter = ArrayAdapter.createFromResource(this, R.array.rendering_samples, android.R.layout.simple_list_item_1);
                break;
            case 3:
                adapter = ArrayAdapter.createFromResource(this, R.array.plugins_samples, android.R.layout.simple_list_item_1);
                break;
            case 4:
                adapter = ArrayAdapter.createFromResource(this, R.array.cameraControl_samples, android.R.layout.simple_list_item_1);
                break;

        }

        _listView.setAdapter(adapter);
        _listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView_, final View view_, final int position, final long id) {
        String itemValue = (String) _listView.getItemAtPosition(position);
        itemValue = itemValue.substring(0,3);
        switch (itemValue) {
            case "1.1":
                _activityClass = SimpleClientTrackingActivity.class;
                break;
            case "1.2":
                _activityClass = ExtendedClientTrackingActivity.class;
                break;
            case "1.3":
                _activityClass = ClientTracking3DActivity.class;
                break;
            case "2.1":
                _activityClass = OnClickCloudTrackingActivity.class;
                break;
            case "2.2":
                _activityClass = ContinuousCloudTrackingActivity.class;
                break;
            case "3.1":
                _activityClass = InternalRenderingActivity.class;
                break;
            case "3.2":
                _activityClass = ExternalRenderingActivity.class;
                break;
            case "4.1":
                _activityClass = BarcodePluginActivity.class;
                break;
            case "4.2":
                _activityClass = FaceDetectionPluginActivity.class;
                break;
            case "4.3":
                _activityClass = CustomCameraActivity.class;
                break;
            case "5.1":
                _activityClass = CameraControlsActivity.class;
                break;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            loadExample();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadExample();
                } else {
                    Toast errorMessage = Toast.makeText(this, "Sorry, augmented reality doesn't work, without reality. \n\nPlease grant camera permission.", Toast.LENGTH_LONG);
                    errorMessage.show();
                }
                return;
            }
        }
    }

    private void loadExample() {
        final Intent intent = new Intent( this, _activityClass );
        startActivity(intent);
    }
}
