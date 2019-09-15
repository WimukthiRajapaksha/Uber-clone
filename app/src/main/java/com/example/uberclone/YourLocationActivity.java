package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.util.Util;

public class YourLocationActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    String provider;
    LocationManager locationManager;
    Location location;
    private final int LOCATION_REQUEST_CODE = 102;
    TextView textView;
    Button button;
    Boolean requestCancelState;

    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_location);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        textView = findViewById(R.id.textView);
        button = findViewById(R.id.btnRequestUber);
        linearLayout = findViewById(R.id.linearLayout);
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        requestCancelState = true;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

//        int widthButton = linearLayout.getLayoutParams().width;
//        Log.i("width", Integer.toString(widthButton));
        if (requestCancelState) {
            textView.setVisibility(View.INVISIBLE);
//            int widthButton = linearLayout.getLayoutParams().width;
//            button.getLayoutParams();
//            Log.i("width", Integer.toString(widthButton));
//            button.setWidth(200);
//            button
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        permissionAndLocationUpdate();
    }


    public void permissionAndLocationUpdate() {
        Log.i("permission", "working");
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION }, 1);
        } else {
            locationManager.requestLocationUpdates(provider, 400, 5, this);
            location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
                mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location"));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i("Here", "0");
        if (requestCode == 1 && grantResults[0] == 0) {
            Log.i("Here", "1");
            checkLocation();
        } else {
            Log.i("Here", "2");
        }

    }

    private void fetchLastLocation(){
        Task<Location> task = LocationServices.getFusedLocationProviderClient(this).getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location loc) {
                if (location != null) {
                    Log.i("First", loc.getLongitude() + " " + loc.getLatitude());
                    mMap.addMarker(new MarkerOptions().position(new LatLng(loc.getLatitude(), loc.getLongitude())).title("Location"));
                }else{
                    Toast.makeText(getApplicationContext(),"No Location recorded", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onLocationChanged(final Location location) {
        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
//        onUpdateDatabaseLocation(location);
        DatabaseReference ref = firebaseDatabase.getReference().child("requests").child(firebaseAuth.getUid());
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation("location", new GeoLocation(location.getLongitude(), location.getLatitude()),
                new GeoFire.CompletionListener() {
                    boolean update = false;
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        if (error != null) {
                            Log.i("Error", "There was an error saving the location to GeoFire: " + error);
                        } else {
                            Log.i("Done", "Location saved on server successfully!");
                            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location"));
                        }
                    }
                });
//        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location"));
    }

//    public boolean onUpdateDatabaseLocation(Location location) {
//        boolean updateDB = false;
//        DatabaseReference ref = firebaseDatabase.getReference("path/to/geofire");
//        GeoFire geoFire = new GeoFire(ref);
//        geoFire.setLocation("firebase-hq", new GeoLocation(location.getLongitude(), location.getLatitude()),
//                new GeoFire.CompletionListener() {
//                    boolean update = false;
//                    @Override
//                    public void onComplete(String key, DatabaseError error) {
//                        if (error != null) {
//                            Log.i("Error", "There was an error saving the location to GeoFire: " + error);
//
//                        } else {
//                            Log.i("Done", "Location saved on server successfully!");
//                        }
//                    }
//        });
//        return updateDB;
//    }

    public  boolean checkLocation() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("Location permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions( new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
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

    public void onRequestUber(View view) {
        textView.setText("Finding an uber driver");
        Log.i("Requested", "uber");
        if (requestCancelState) {
            textView.setVisibility(View.VISIBLE);
            firebaseDatabase.getReference().child("requests").child(firebaseAuth.getUid()).child("active_request").setValue(true)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i("Done", "Request completed.");
                            textView.setText("Finding an uber driver");
                            button.setText("Cancel Uber");
                            requestCancelState = false;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Done", "Request Error.");
                            textView.setText("Error Occurred");
                            button.setText("Try Again");
                        }
                    });
        } else {
            firebaseDatabase.getReference().child("requests").child(firebaseAuth.getUid()).child("active_request").setValue(false)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i("Done", "Request completed.");
                            textView.setText("Have a new ride");
                            button.setText("Request Uber");
                            requestCancelState = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("Done", "Request Error.");
                            textView.setText("Error Occurred");
                            button.setText("Try Again to cancel");
                        }
                    });
        }

//        ValueEventListener valueEventListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.i("Done", "Request completed.");
//                textView.setText("Finding a uber driver");
//                button.setText("Cancel Uber");
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.i("Done", "Request Error.");
//                textView.setText("Error Occurred");
//                button.setText("Try Again");
//            }
//        };
//        firebaseDatabase.getReference().addValueEventListener(valueEventListener);
    }
}
