package com.example.memorableplaceapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;

    public void centerMapOnLocation(Location location, String title) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, locationListener);
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Intent intent =  getIntent();
        if(intent.getIntExtra("Name", 0) ==0 ) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    centerMapOnLocation(location, "You Are Here!");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation, "You Were Here!");
            }

        } else {

            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("Name",0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("Name",0)).longitude);

            centerMapOnLocation(placeLocation, MainActivity.places.get(intent.getIntExtra("Name", 0)));

        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                String address = " ";

                try {

                    List<Address> listAdddresses = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);

                    if (listAdddresses != null && listAdddresses.size() > 0) {
                        if (listAdddresses.get(0).getThoroughfare() != null) {
                            if (listAdddresses.get(0).getSubThoroughfare() != null) {
                                address += listAdddresses.get(0).getSubThoroughfare() ;
                            }
                            address += listAdddresses.get(0).getThoroughfare();
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();;
                }

                if (address.equals(" ")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyy-MM-dd");
                    address += sdf.format(new Date());
                }

                mMap.addMarker(new MarkerOptions().position(latLng).title(address));

                MainActivity.places.add(address);
                MainActivity.locations.add(latLng);

                MainActivity.arrayAdapter.notifyDataSetChanged();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.memorableplaceapp", Context.MODE_PRIVATE);
                ArrayList<String> latitude = new ArrayList<>();
                ArrayList<String> longitude = new ArrayList<>();

                try {


                    for (LatLng coord : MainActivity.locations) {
                        latitude.add(Double.toString(coord.latitude));
                        longitude.add(Double.toString(coord.longitude));
                    }


                    sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
                    sharedPreferences.edit().putString("lats", ObjectSerializer.serialize(latitude)).apply();
                    sharedPreferences.edit().putString("longs", ObjectSerializer.serialize(longitude)).apply();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), "Your Place Added", Toast.LENGTH_SHORT).show();
            }
        });

    }


}
