package com.wikitude.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.wikitude.nativesdksampleapp.R;
import com.wikitude.samples.HelpActivity;
import com.wikitude.samples.plugins.FaceDetectionPluginActivity;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView _listView = null;
    private Map<Integer, ArrayAdapter<CharSequence>> _sampleLists = new HashMap<>();
    Button playBtn,helpBtn,highBtn;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playBtn=(Button)findViewById(R.id.play_btn);
        highBtn=(Button)findViewById(R.id.high_btn);
        helpBtn=(Button)findViewById(R.id.help_btn);
//        playBtn.setOnClickListener(this);
//        helpBtn.setOnClickListener(this);
//        highBtn.setOnClickListener(this);
//        final Intent intent = new Intent( this, FaceDetectionPluginActivity.class );
//        startActivity(intent);
    }

//    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.play_btn){
            final Intent intent1 = new Intent( this, FaceDetectionPluginActivity.class );
            startActivity(intent1);        }
        else if(view.getId()==R.id.help_btn){
            final Intent intent2 = new Intent(this, HelpActivity.class);
            startActivity(intent2);
        }
        else if(view.getId()==R.id.high_btn){
            //high scores button
        }
    }

}
