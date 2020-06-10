package com.lukasnewman.runwithfriends.ui.workout

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lukasnewman.runwithfriends.R
import com.lukasnewman.runwithfriends.Workout
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
    private var time = 0
    private var locList = ArrayList<LatLng>()
    private lateinit var lastLocation: Location

    //Phone GPS Variables (My First Attempt)
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    //Google Maps ui features
    private lateinit var mPolyline: Polyline

    //Firebase Stuff
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

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
        editTextWorkoutTitle.isVisible = false
        buttonCancel.isVisible = false
        buttonPost.isVisible = false

        getLastKnownLocation()
        getLocationUpdates()

        //Start the workout button press
        buttonStart.setOnClickListener(View.OnClickListener { v: View ->

            if (buttonStart.text == "Start") {
                workingOut = true
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
            //endLocation = lastLocation
            stopLocationUpdates()
            mPolyline = googleMap!!.addPolyline(PolylineOptions().clickable(true).addAll(locList))

            buttonPost.isVisible = true
            buttonCancel.isVisible = true
            buttonStart.isVisible = false
            buttonStop.isVisible = false
            editTextWorkoutTitle.isVisible = true
        })

        //Post the workout
        buttonPost.setOnClickListener(View.OnClickListener { v: View ->

            if (editTextWorkoutTitle.text.isNotEmpty()) {

                var workout = Workout(distance, locList, time, editTextWorkoutTitle.text.toString(), "CaptainNovak", FirebaseAuth.getInstance().currentUser!!.uid)

                firebaseAuth = FirebaseAuth.getInstance()
                val user = firebaseAuth.currentUser
                database = FirebaseDatabase.getInstance().reference

                database.child("Workouts").child(user!!.uid).push().setValue(workout)
                database.child("AllWorkouts").push().setValue(workout)


            } else {
                Toast.makeText(requireContext(), "Please Enter A Title!", Toast.LENGTH_SHORT).show()
            }
            resetWorkout()

        })

        //Cancel the workout
        buttonCancel.setOnClickListener(View.OnClickListener { v: View ->
            resetWorkout()
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
            locList.add(loc)
            displacement = currentLocation.distanceTo(lastLocation).toDouble()
            displacement *= 0.00062137
            distance += displacement
            textViewDistanceRecycle.text = "Distance: " + distance + " miles"

        }

        lastLocation = currentLocation

    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
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
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
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

    private fun resetWorkout() {
        //Reset the workout
        mPolyline.remove()

        buttonPost.isVisible = false
        buttonCancel.isVisible = false
        buttonStart.text = "Start"
        buttonStart.isVisible = true
        editTextWorkoutTitle.isVisible = false
        editTextWorkoutTitle.setText("")

        textViewDistanceRecycle.text = "Distance: "
        textViewTimeRecycle.text = "Time: "

        distance = 0.0
        displacement = 0.0
        time = 0
        locList.clear()
        startLocationUpdates()
    }
}