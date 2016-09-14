package com.wikitude.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.lang.Object;
import com.wikitude.nativesdksampleapp.R;

/**
 * Created by manu on 13/9/16.
 */
public class ScoreView extends AppCompatActivity {
    @Override
    protected void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.score_view);
        TextView textView = (TextView) findViewById(R.id.textView);
        Bundle extras = getIntent().getExtras();
        int score = extras.getInt("Score");
        textView.setText("Your Score :" + score);
        Button button = (Button) findViewById(R.id.button);
//        button.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View view) {
//               final Intent i = new Intent(this , MainActivity.class);
//                startActivity(i);
//            }
//        });
    }
        //@Override
        public void goBack(View view){
        Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
    }


}
