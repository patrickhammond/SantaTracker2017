package com.gdgcincinnati.santatracker;

import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JavaMainActivity extends AppCompatActivity {
    @Nullable private GoogleMap map;
    @Nullable private Marker marker;

    @Nullable private DatabaseReference locationRef;
    @Nullable private ValueEventListener locatonListener;
    @Nullable private DatabaseReference hohohoRef;
    @Nullable private ValueEventListener hohohoListener;

    private SoundPool soundPool;
    private int soundId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                startTrackingSanta();
            }
        });

        soundPool = new SoundPool.Builder().build();
        soundId = soundPool.load(this, R.raw.hohoho, 1);
    }

    private void startTrackingSanta() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        locationRef = database.getReference("current_location");
        if (locatonListener == null) {
            locatonListener = new ValueEventListenerAdapter() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    double latitude = getNonNullValue(dataSnapshot.child("lat"), Double.class);
                    double longitude = getNonNullValue(dataSnapshot.child("lng"), Double.class);
                    updateMapAndMarker(new LatLng(latitude, longitude));
                }
            };
        }
        //noinspection ConstantConditions
        locationRef.addValueEventListener(locatonListener);


        hohohoRef = database.getReference("ho_ho_hoing");
        if (hohohoListener == null) {
            hohohoListener = new ValueEventListenerAdapter() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean hohohoing = getNonNullValue(dataSnapshot, Boolean.class);
                    playSantaIfHohohoing(hohohoing);
                }
            };
        }
        //noinspection ConstantConditions
        hohohoRef.addValueEventListener(hohohoListener);
    }

    @Override
    protected void onPause() {
        if (locationRef != null && locatonListener != null) {
            locationRef.removeEventListener(locatonListener);
        }

        if (hohohoRef != null && hohohoListener != null) {
            hohohoRef.removeEventListener(hohohoListener);
        }

        super.onPause();
    }

    private void updateMapAndMarker(LatLng position) {
        if (map != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 9f));

            if (marker == null) {
                MarkerOptions options = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.santa))
                        .position(position);
                marker = map.addMarker(options);
            } else {
                marker.setPosition(position);
            }
        }
    }

    private void playSantaIfHohohoing(boolean hohohoing) {
        if (hohohoing) {
            soundPool.play(soundId, 1f, 1f, 10, 0, 1f);
        }
    }

    private static abstract class ValueEventListenerAdapter implements ValueEventListener {
        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Ignored
        }
    }

    private static <T> T getNonNullValue(DataSnapshot dataSnapshot, Class<T> type) {
        T value = dataSnapshot.getValue(type);
        if (value == null) {
            throw new IllegalStateException();
        } else {
            return value;
        }
    }
}
