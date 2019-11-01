package com.summer.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText mIP, mPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        mIP = findViewById(R.id.editText);
        mPort = findViewById(R.id.editText2);

        final Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                button.setText("Connecting.......");
                String ip = mIP.getText().toString().trim();
                int port = Integer.parseInt(mPort.getText().toString().trim());
                Intent intent = new Intent(MainActivity.this, ScreenMirrorActivity.class);
                intent.putExtra("USER_NAME","VIRTOP");
                intent.putExtra("USER_IP",ip);
                intent.putExtra("USER_PORT",port);
                startActivity(intent);
            }
        });

    }
}
