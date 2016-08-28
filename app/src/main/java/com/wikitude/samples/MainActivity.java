package com.wikitude.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.wikitude.nativesdksampleapp.R;
import com.wikitude.samples.recorder.TrackingMapRecorderActivity;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView _listView = null;
    private Map<Integer, ArrayAdapter<CharSequence>> _sampleLists = new HashMap<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _listView = (ListView) findViewById(R.id.sample_list);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sample_categories, android.R.layout.simple_list_item_1);
        

        _listView.setAdapter(adapter);
        _listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(final AdapterView<?> adapterView_, final View view_, final int position, final long id) {
        final Intent intent = new Intent( this, SampleCategoryListActivity.class );
        intent.putExtra(SampleCategoryListActivity.EXTRAS_CATEGORY_POSITION, position);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.tracking_map_recorder_start_button:
                final Intent intent = new Intent( MainActivity.this, TrackingMapRecorderActivity.class );
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
