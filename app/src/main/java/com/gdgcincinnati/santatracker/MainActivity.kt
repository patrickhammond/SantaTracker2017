package com.gdgcincinnati.santatracker

import android.media.SoundPool
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var map: GoogleMap? = null
    private var marker: Marker? = null

    private var locationRef: DatabaseReference? = null
    private var hohohoRef: DatabaseReference? = null

    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        soundPool = SoundPool.Builder().build()
        soundId = soundPool.load(this, R.raw.hohoho, 1)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        startTrackingSanta()
    }

    private fun startTrackingSanta() {
        val database = FirebaseDatabase.getInstance()
        locationRef = database.getReference("current_location")
        locationRef?.addValueEventListener(locationListener)

        hohohoRef = database.getReference("ho_ho_hoing")
        hohohoRef?.addValueEventListener(hohohoListener)
    }

    override fun onPause() {
        locationRef?.removeEventListener(locationListener)
        hohohoRef?.removeEventListener(hohohoListener)
        super.onPause()
    }

    private val hohohoListener = object: ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val hohohoing = dataSnapshot.getValue(Boolean::class.java) as Boolean
            if (hohohoing) {
                soundPool.play(soundId, 1f, 1f, 10, 0, 1f)
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    private val locationListener = object: ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val latitude = dataSnapshot.child("lat").getValue(Double::class.java) as Double
            val longitude = dataSnapshot.child("lng").getValue(Double::class.java) as Double
            val position = LatLng(latitude, longitude)

            updateMapAndMarker(position)
        }

        override fun onCancelled(error: DatabaseError) {
            // ignoring
        }
    }

    private fun updateMapAndMarker(position: LatLng) {
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 9f))

        if (marker == null) {
            val options = MarkerOptions()
                    .position(position)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.santa))
            marker = map?.addMarker(options)
        } else {
            marker?.position = position
        }
    }
}
