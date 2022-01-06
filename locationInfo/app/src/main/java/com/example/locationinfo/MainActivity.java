package com.example.locationinfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private GpsTracker gpsTracker;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!checkLocationServicesStatus()){
            showDialogForLocationServiceSetting();
        }else{
            checkRunTimePermission();
        }
        final TextView textView_address = (TextView)findViewById(R.id.textview);

        Button ShowLocationButton = (Button) findViewById(R.id.button);
        ShowLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gpsTracker = new GpsTracker(MainActivity.this);

                double latitude = gpsTracker.getLatitude();
                double longitude = gpsTracker.getLongitude();

                String address = getCurrentAddress(latitude, longitude);
                textView_address.setText(address);

                Toast.makeText(MainActivity.this, "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();
            }
        });
    }
    /*ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드*/

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                ;
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 애븡ㄹ 다시 실행하여 퍼미션을 허용해주세요", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야합니다 ", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    void checkRunTimePermission(){
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarselLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_COARSE_LOCATION);
        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarselLocationPermission == PackageManager.PERMISSION_GRANTED){

        }else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,REQUIRED_PERMISSIONS[0])){
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,PERMISSIONS_REQUEST_CODE);
            }else{
                ActivityCompat.requestPermissions(MainActivity.this,REQUIRED_PERMISSIONS,PERMISSIONS_REQUEST_CODE);
            }
        }
    }
    public String getCurrentAddress(double latitude,double longitude){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try{
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        }catch (IOException ioException){
            Toast.makeText(this,"지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        }catch (IllegalArgumentException illegalArgumentException){
            Toast.makeText(this,"잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if(addresses == null || addresses.size() == 0){
            Toast.makeText(this,"주소 미발견",Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }
        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";
    }
    // GPS 활성화를 위한 메소드
    private void showDialogForLocationServiceSetting(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"+"위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
                startActivityForResult(callGPSSettingIntent,GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)|| locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}