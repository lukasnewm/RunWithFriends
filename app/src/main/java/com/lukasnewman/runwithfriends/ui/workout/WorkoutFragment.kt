package com.lukasnewman.runwithfriends.ui.workout

import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.lukasnewman.runwithfriends.R
import kotlinx.android.synthetic.main.fragment_workout.*

class WorkoutFragment : Fragment(), OnMapReadyCallback {

    private lateinit var workoutViewModel: WorkoutViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private var requestingLocationUpdates = false

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

        mapView = view.findViewById(R.id.mapView)
        if (mapView != null) {
            mapView!!.onCreate(null)
            mapView!!.onResume()
            mapView!!.getMapAsync(this)
        }

        //Start the workout button press
        buttonStart.setOnClickListener(View.OnClickListener { v: View ->

        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //getTheLastLocation()
    }

    override fun onMapReady(p0: GoogleMap?) {
        MapsInitializer.initialize(context)
        googleMap = p0
    }

    private fun getTheLastLocation() {
        //Get users last known location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity().applicationContext)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                //Users Location on the map
                var loc = LatLng(location.latitude, location.longitude)
                googleMap!!.animateCamera(CameraUpdateFactory.newLatLng(loc))
                googleMap!!.addMarker(MarkerOptions().position(loc))
            } else {
                //Toast.makeText(activity, "Location Unsuccesful", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) startLocationUpdates()
    }
}
