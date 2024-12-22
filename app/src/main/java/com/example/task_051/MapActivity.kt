package com.example.task_051

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.task_051.databinding.ActivityMapBinding
import com.google.android.gms.location.LocationRequest
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.runtime.image.ImageProvider

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private val permissionId = 52

    private var startLocation: Point? = null
    private val zoomValue: Float = 16.5f

    private lateinit var mapObjectCollection: MapObjectCollection
    private lateinit var placemarkMapObject: PlacemarkMapObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setApiKey(savedInstanceState)
        MapKitFactory.initialize(this)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.openMapBTN.setOnClickListener(){
            getLocation()
        }

        binding.fabBTN.setOnClickListener(){
            var mapKit:MapKit = MapKitFactory.getInstance()
            var probki = mapKit.createTrafficLayer(binding.mapView.mapWindow)
            probki.isTrafficVisible = true
        }
    }

    private fun createBitmapFromVector(art: Int): Bitmap? {
        val drawable = ContextCompat.getDrawable(this, art) ?: return null
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return  bitmap
    }

    private fun setMarkerInStartLocation() {
        val marker = createBitmapFromVector(R.drawable.ic_pin_red_png)
        mapObjectCollection = binding.mapView.map.mapObjects
        placemarkMapObject = mapObjectCollection.addPlacemark(
            startLocation!!,
            ImageProvider.fromBitmap(marker)
        )
        placemarkMapObject.opacity = 1.0f
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            startLocation = Point(location.latitude, location.longitude)
                            binding.mapView.map.move(
                                CameraPosition(startLocation!!, 11.0f, 0.0f, 0.0f),
                                Animation(Animation.Type.SMOOTH, 10f),
                                null
                            )
                            setMarkerInStartLocation()
                        } else {
                            Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            permissionId
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    private fun setApiKey(savedInstanceState: Bundle?) {
        val haveApiKey = savedInstanceState?.getBoolean("haveApiKey") ?: false
        if (!haveApiKey) MapKitFactory.setApiKey(Utils.MAPKIT_API_KEY)
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        binding.mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}