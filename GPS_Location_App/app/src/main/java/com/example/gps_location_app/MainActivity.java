package com.example.gps_location_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    private TextView tvLatitude, tvLongitude, tvAccuracy;
    private Button btnGetLocation;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        progressBar = findViewById(R.id.progressBar);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndGetLocation();
            }
        });
    }

    private void checkPermissionsAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        progressBar.setVisibility(View.VISIBLE);
        
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            progressBar.setVisibility(View.GONE);
                            if (location != null) {
                                tvLatitude.setText(getString(R.string.latitude_label, String.valueOf(location.getLatitude())));
                                tvLongitude.setText(getString(R.string.longitude_label, String.valueOf(location.getLongitude())));
                                tvAccuracy.setText(getString(R.string.accuracy_label, String.valueOf(location.getAccuracy())));
                            } else {
                                Toast.makeText(MainActivity.this, R.string.location_not_found, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (SecurityException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
