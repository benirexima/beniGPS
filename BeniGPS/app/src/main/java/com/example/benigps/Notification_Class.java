package com.example.benigps;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class Notification_Class  extends Activity {

    SharedPreferences GetNotify;
    SharedPreferences.Editor Geteditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_layout);
        TextView t1 = (TextView) findViewById(R.id.Name);
        TextView t2 = (TextView) findViewById(R.id.Time);
        GetNotify = getApplicationContext().getSharedPreferences("Notification", Context.MODE_PRIVATE);
        Geteditor = GetNotify.edit();
        GetNotify = getSharedPreferences("Notification", Context.MODE_PRIVATE);
        String title = GetNotify.getString("notify_title", "t");
        String body = GetNotify.getString("notify_body", "s");
        Toast.makeText(getApplicationContext(), title + body, Toast.LENGTH_SHORT).show();

        t1.setText(title);
        t2.setText(body);

    }
    }