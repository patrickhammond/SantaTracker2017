package com.gdgcincinnati.santatracker

import android.media.SoundPool
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {
    private var map: GoogleMap? = null
    private var marker: Marker? = null

    private var locationRef: DatabaseReference? = null
    private var locationListener: ValueEventListener? = null
    private var hohohoRef: DatabaseReference? = null
    private var hohohoListener: ValueEventListener? = null

    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            map = googleMap
            startTrackingSanta()
        }

        soundPool = SoundPool.Builder().build()
        soundId = soundPool.load(this, R.raw.hohoho, 1)
    }

    private fun startTrackingSanta() {
        val database = FirebaseDatabase.getInstance()
        locationRef = database.getReference("current_location").apply {
            locationListener = locationListener ?: ValueEventListenerAdapter { dataSnapshot ->
                updateMapAndMarker(with(dataSnapshot) {
                    val latitude = dataSnapshot.child("lat").getNonNullValue(Double::class.java)
                    val longitude = dataSnapshot.child("lng").getNonNullValue(Double::class.java)
                    LatLng(latitude, longitude)
                })
            }
            addValueEventListener(locationListener)
        }

        hohohoRef = database.getReference("ho_ho_hoing").apply {
            hohohoListener = hohohoListener ?: ValueEventListenerAdapter { dataSnapshot ->
                val hohohoing = dataSnapshot.getNonNullValue(Boolean::class.java)
                playSantaIfHohohoing(hohohoing)
            }
            addValueEventListener(hohohoListener)
        }
    }

    override fun onPause() {
        locationRef?.removeEventListener(locationListener)
        hohohoRef?.removeEventListener(hohohoListener)
        super.onPause()
    }

    private fun updateMapAndMarker(position: LatLng) {
        map?.let { map ->
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 9f))

            if (marker == null) {
                val options = MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.santa))
                        .position(position)
                marker = map.addMarker(options)
            } else {
                marker?.position = position
            }
        }
    }

    private fun playSantaIfHohohoing(hohohoing: Boolean) {
        if (hohohoing) {
            soundPool.play(soundId, 1f, 1f, 10, 0, 1f)
        }
    }

    private class ValueEventListenerAdapter(val lambda: (DataSnapshot) -> Unit) : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) = lambda(dataSnapshot)
        override fun onCancelled(error: DatabaseError) = Unit
    }

    private fun <T> DataSnapshot.getNonNullValue(type: Class<T>): T = getValue(type) ?: throw IllegalStateException()
}
