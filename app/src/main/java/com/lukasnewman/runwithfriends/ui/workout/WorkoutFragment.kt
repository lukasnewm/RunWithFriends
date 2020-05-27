package com.lukasnewman.runwithfriends.ui.workout

import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.lukasnewman.runwithfriends.R
import kotlinx.android.synthetic.main.fragment_workout.*

class WorkoutFragment : Fragment(), OnMapReadyCallback {

    private lateinit var workoutViewModel: WorkoutViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Google Map Variables
    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null

    //Variables for Calculations
    private var distance = 0.0
    private var displacement = 0.0
    private var workingOut = false
    private lateinit var lastLocation: Location
    private lateinit var startLocation: Location
    private lateinit var endLocation: Location

    //Phone GPS Variables (My First Attempt)
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        workoutViewModel =
                ViewModelProviders.of(this).get(WorkoutViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_workout, container, false)
        workoutViewModel.text.observe(viewLifecycleOwner, Observer {

        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        mapView = view.findViewById(R.id.mapView)
        if (mapView != null) {
            mapView!!.onCreate(null)
            mapView!!.onResume()
            mapView!!.getMapAsync(this)
        }

        buttonStop.isVisible = false
        getLastKnownLocation()
        getLocationUpdates()

        //Start the workout button press
        buttonStart.setOnClickListener(View.OnClickListener { v: View ->

            if (buttonStart.text == "Start") {
                workingOut = true
                startLocation = lastLocation
                buttonStart.text = "Pause"
                buttonStop.isVisible = true
            }
            else {
                workingOut = false
                buttonStart.text = "Start"
            }

        })

        //Stop the workout
        buttonStop.setOnClickListener(View.OnClickListener { v: View ->
            workingOut = false
            endLocation = lastLocation
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //startLocationUpdates()
        //getTheLastLocation()
    }

    override fun onMapReady(p0: GoogleMap?) {
        MapsInitializer.initialize(context)
        googleMap = p0
    }

    private fun updateStats(currentLocation: Location) {

        //Update the map view
        var loc = LatLng(currentLocation.latitude, currentLocation.longitude)
        googleMap!!.clear()
        googleMap!!.addMarker(MarkerOptions().position(loc)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        googleMap!!.animateCamera(CameraUpdateFactory.newLatLng(loc))
        googleMap!!.setMaxZoomPreference(18f)
        googleMap!!.setMinZoomPreference(18f)

        //update the distance
        if (workingOut) {
            displacement = currentLocation.distanceTo(lastLocation).toDouble()
            displacement *= 0.00062137
            distance += displacement
            textViewDistance.text = "Distance: " + distance + " miles"
        }

        lastLocation = currentLocation

    }

    private fun getLastKnownLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                lastLocation = location
            }
        }
    }

    private fun getLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest()
        locationRequest.interval = 1000 //1 second in miliseconds
        locationRequest.fastestInterval = 1000
        locationRequest.smallestDisplacement = 0f //100f = 100 meters
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationCallback = object : LocationCallback() {
            override  fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return

                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation
                    updateStats(location)
                }
            }
        }
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }
}