package com.example.uberclone;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    Switch aSwitch;
    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    Boolean currentRiderState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();

        aSwitch = findViewById(R.id.switchSelect);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (firebaseAuth.getCurrentUser() == null) {
            firebaseAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Log.d("login", "signInAnonymously:success");
                    } else {
                        Log.d("login", "signInAnonymously:failed");
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onGetStarted(View view) {
        currentRiderState = true;
        if (aSwitch.isChecked()) {
            currentRiderState = false;
            databaseReference.child("users").child(firebaseAuth.getUid()).child("driver").setValue(true);
        } else {
            databaseReference.child("users").child(firebaseAuth.getUid()).child("rider").setValue(true);
        }
        redirect();
    }

    public void redirect() {
        Log.i("map", "inside");
        Log.i("map", databaseReference.child("users").child(firebaseAuth.getUid()).child("rider").toString());
//        if (databaseReference.child("users").child(firebaseAuth.getUid()).child("rider").limitToFirst(1).equals(true)) {
//            Intent intent = new Intent(getApplicationContext(), YourLocationActivity.class);
//            Log.i("map", "loading");
//            startActivity(intent);
//        }
//        databaseReference.child("users").child(firebaseAuth.getUid()).child("rider").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Log.i("Data", dataSnapshot.getValue().toString());
//                if (dataSnapshot.getValue().equals(true)) {
//                    Intent intent = new Intent(getApplicationContext(), YourLocationActivity.class);
//                    startActivity(intent);
//                } else {
//                    Intent intent = new Intent(getApplicationContext(), ViewRequestsActivity.class);
//                    startActivity(intent);
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });
        if (currentRiderState == true) {
            Intent intent = new Intent(getApplicationContext(), YourLocationActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), ViewRequestsActivity.class);
            startActivity(intent);
        }
    }
}
