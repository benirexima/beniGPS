package com.example.benigps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

         Button btnAdmin,btnUser,retrofit;
         private static final int REQ_PERMISSION = 1;
        private static final String TAG = MainActivity.class.getSimpleName();
        LocationManager locationManager;
        private AlertDialog mInternetDialog;
        private AlertDialog mGPSDialog;
        private static final int GPS_ENABLE_REQUEST = 0x1001;
        private static final int WIFI_ENABLE_REQUEST = 0x1006;

        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            btnAdmin = (Button) findViewById(R.id.Admin);
            btnUser = (Button) findViewById(R.id.User);
            ;

            //   TurnOnGPS();
//            btnAdmin.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(getApplicationContext(), Register_class.class);
//                    startActivity(intent);
//                }
//            });
            btnUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), User_Login.class);
                    startActivity(intent);
                }
            });

        }

        private boolean checkPermission() {
            Log.d(TAG, "checkPermission()");
            // Ask for permission if it wasn't granted yet
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED);
        }

        // Asks for permission
        private void askPermission() {
            Log.d(TAG, "askPermission()");
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQ_PERMISSION
            );
        }

        // Verify user's response of the permission requested
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            Log.d(TAG, "onRequestPermissionsResult()");
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            switch (requestCode) {
                case REQ_PERMISSION: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Permission granted


                    } else {
                        // Permission denied
                        permissionsDenied();
                    }
                    break;
                }
            }
        }

        // App cannot work without the permissions
        private void permissionsDenied() {
            Log.w(TAG, "permissionsDenied()");
            // TODO close app and warn user
        }

        private void checkInternetConnection() {
            ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();

            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {

            } else {
                showNoInternetDialog();
            }
        }
        private void showNoInternetDialog() {

            if (mInternetDialog != null && mInternetDialog.isShowing()) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.internet_disabled);
            builder.setMessage(R.string.internet_disabled_msg);
            builder.setPositiveButton(R.string.turn_on, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                    startActivityForResult(gpsOptionsIntent, WIFI_ENABLE_REQUEST);
                }
            }).setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            mInternetDialog = builder.create();
            mInternetDialog.show();
        }

        @Override
        protected void onStart() {
            super.onStart();
            checkInternetConnection();
            if (checkPermission())
                Log.v(TAG,"Permission grandted");
            else {
                askPermission();
            }

        }
        @Override
        public void onBackPressed() {

            new AlertDialog.Builder(this)
                    .setTitle("Really Exit?")
                    .setMessage("Are you sure you want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface arg0, int arg1) {
                            MainActivity.super.onBackPressed();
                        }
                    }).create().show();
        }


    }




