package com.example.wang.backendlessmap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnScooter, btnGo;
    private LocationManager locationManager = null;
    private String provider;
    public static final String MY_PREFS_FILENAME = "com.example.wang.backendlessmap.XY";
    private SharedPreferences get;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        btnScooter.setOnClickListener(this);
        btnGo.setOnClickListener(this);

    }

    private void initView() {
        get = getSharedPreferences(MY_PREFS_FILENAME, MODE_PRIVATE);
        editor = getSharedPreferences(MY_PREFS_FILENAME, MODE_PRIVATE).edit();
        btnGo = findViewById(R.id.btnGo);
        btnScooter = findViewById(R.id.btnScooter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnScooter:
                if (get.getString("X", "").isEmpty() && get.getString("Y", "").isEmpty()) {
                    RecordScooter();
                }else {
                    new AlertDialog.Builder(this).setMessage("使否要重新設定位子").setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RecordScooter();
                        }
                    }).setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            return;
                        }
                    }).show();
                }
                break;

            case R.id.btnGo:
                FindScooter();
                break;
        }
    }

    public void init() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        List<String> providerList = locationManager.getProviders(true);
        if (providerList.contains(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (providerList.contains(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, "沒有位置提供器可使用", Toast.LENGTH_LONG).show();
            return;
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            showLocation(location);
        } else {
            String info = "無法獲得當前位置";
            Toast.makeText(this, info, Toast.LENGTH_SHORT).show();
        }

    }

    public void RecordScooter() {
        if (checkPermissions()) {
            init();
        }
    }

    private void showLocation(Location location) {

        MyLocation myLocation = new MyLocation();

        myLocation.setX(location.getLatitude());
        myLocation.setY(location.getLongitude());

        editor.putString("X", String.valueOf(myLocation.getX()));
        editor.putString("Y", String.valueOf(myLocation.getY()));
        editor.commit();

        //SharedPreferences get = getSharedPreferences(MY_PREFS_FILENAME,MODE_PRIVATE);

        Toast.makeText(MainActivity.this,"succes", Toast.LENGTH_LONG).show();
    }

    public Boolean checkPermissions() {

        if (Build.VERSION.SDK_INT >= 23) {

            String permissions[] = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION};

            List<String> pm_list = new ArrayList<>();

            for (int i = 0; i < permissions.length; i++) {
                int pm = ActivityCompat.checkSelfPermission(this, permissions[i]);
                if (pm != PackageManager.PERMISSION_GRANTED) {
                    pm_list.add(permissions[i]);
                }
            }
            if (pm_list.size() > 0) {
                for (int i = 0; i < pm_list.size(); i++) {
                    Log.d("TAG", pm_list.get(i));
                }
                Log.d("TAG", pm_list.size() + "");
                ActivityCompat.requestPermissions(this, pm_list.toArray(new String[pm_list.size()]), 1);

                return false;
            }
        }
        return true;
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            Log.d("TAG", permissions[i] + "allow");
                            init();
                        } else {
                            checkPermissions();
                            Log.d("TAG", permissions[i] + "not allow");
                        }
                    }
                } else {
                    Log.d("TAG", "no pm allow");
                }
                return;
        }
    }

    public void FindScooter() {

        if(get.getString("X", "").isEmpty() || get.getString("Y", "").isEmpty()){
            Toast.makeText(this,"尚未存取位置",Toast.LENGTH_SHORT).show();
            return;
        }
        String X = get.getString("X", "");
        String Y = get.getString("Y", "");
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + X + "," + Y);
        Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
