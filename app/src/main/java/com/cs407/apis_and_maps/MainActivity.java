package com.cs407.apis_and_maps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 12;
    private final LatLng mDestinationLatLng = new LatLng(43.07573726450124, -89.40400907105436); // Bascom Hall
    private GoogleMap mMap;
    private Marker mCurrentLocationMarker;
    private Marker mDestinationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_map);

        mapFragment.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                .position(mDestinationLatLng)
                .title("Bascom Hall"));

        mMap.setOnMapClickListener(latLng -> {
            if (mCurrentLocationMarker != null) {
                updateMarkerAndPolyline(latLng);
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {
            }

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {
            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                if (marker.equals(mCurrentLocationMarker)) {
                    updateMarkerAndPolyline(marker.getPosition());
                }
            }
        });

        displayMyLocation();
    }

    private void updateMarkerAndPolyline(LatLng newLocation) {
        mCurrentLocationMarker.setPosition(newLocation);
        // Clear the previous polyline
        mMap.clear();
        // Re-add markers
        mDestinationMarker = mMap.addMarker(new MarkerOptions()
                .position(mDestinationLatLng)
                .title("Bascom Hall"));
        mCurrentLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(newLocation)
                .title("Current Location")
                .draggable(true));
        mMap.addPolyline(new PolylineOptions().add(
                newLocation,
                mDestinationLatLng
        ));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15));
    }

    private void displayMyLocation() {
        int permission = ActivityCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            mFusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(this, task -> {
                        Location mLastKnownLocation = task.getResult();
                        if (task.isSuccessful() && mLastKnownLocation != null) {
                            LatLng currentLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

                            if (mCurrentLocationMarker != null) {
                                mCurrentLocationMarker.setPosition(currentLatLng);
                            } else {
                                mCurrentLocationMarker = mMap.addMarker(new MarkerOptions()
                                        .position(currentLatLng)
                                        .title("Current Location"));
                            }

                            mMap.clear();

                            mDestinationMarker = mMap.addMarker(new MarkerOptions()
                                    .position(mDestinationLatLng)
                                    .title("Bascom Hall"));
                            mMap.addMarker(new MarkerOptions()
                                    .position(currentLatLng)
                                    .title("Current Location"));
                            mMap.addPolyline(new PolylineOptions().add(
                                    currentLatLng,
                                    mDestinationLatLng
                            ));

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                        }
                    });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayMyLocation();
            }
        }
    }
}
