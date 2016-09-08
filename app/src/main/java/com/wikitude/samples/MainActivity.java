package com.wikitude.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.wikitude.nativesdksampleapp.R;
import com.wikitude.samples.plugins.FaceDetectionPluginActivity;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView _listView = null;
    private Map<Integer, ArrayAdapter<CharSequence>> _sampleLists = new HashMap<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Intent intent = new Intent( this, FaceDetectionPluginActivity.class );
        startActivity(intent);
    }


}
