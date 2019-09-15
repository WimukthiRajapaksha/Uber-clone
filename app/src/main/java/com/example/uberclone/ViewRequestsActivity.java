package com.example.uberclone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestsActivity extends AppCompatActivity implements LocationListener {

    ListView listView;
    ArrayList<String> listViewContent;
    ArrayAdapter arrayAdapter;
    FirebaseDatabase firebaseDatabase;
    LocationManager locationManager;
    String provider;
    Location location;
    GeoFire geoFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseDatabase = FirebaseDatabase.getInstance();
        geoFire = new GeoFire(firebaseDatabase.getReference());

        listView = findViewById(R.id.listView);
        listViewContent = new ArrayList<String>();

        listViewContent.add("Finding nearby requests....");
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, listViewContent);

        listView.setAdapter(arrayAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        getLastKnownLocation();
        updateListView(location);
    }

    public void getLastKnownLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 5, this);
        location = locationManager.getLastKnownLocation(provider);
        if (location != null) {
            updateLocation(location);
        }
    }

    public void updateLocation(Location location) {

    }

    public void updateListView(final Location location) {
        DatabaseReference allRequests = firebaseDatabase.getReference().child("requests");
        allRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLongitude(), location.getLatitude()), 10);
                    Log.i("geofire", String.valueOf(geoQuery.getRadius()));
//                    postSnapshot.child("location").child("l");
//                    geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
//                        @Override
//                        public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
//
//                        }
//
//                        @Override
//                        public void onDataExited(DataSnapshot dataSnapshot) {
//
//                        }
//
//                        @Override
//                        public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {
//
//                        }
//
//                        @Override
//                        public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {
//
//                        }
//
//                        @Override
//                        public void onGeoQueryReady() {
//                            if (geoQuery)
//                                Log.i("Data", geoQuery.)
//                        }
//
//                        @Override
//                        public void onGeoQueryError(DatabaseError error) {
//
//                        }
//                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(location.getLongitude(), location.getLatitude()), 10);
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
//                if (geoQuery)
//                Log.i("Data", geoQuery.)
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
