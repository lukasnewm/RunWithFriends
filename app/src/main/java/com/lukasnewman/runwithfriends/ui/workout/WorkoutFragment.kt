package com.lukasnewman.runwithfriends.ui.workout

import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.lukasnewman.runwithfriends.R
import kotlinx.android.synthetic.main.fragment_workout.*
import kotlinx.android.synthetic.main.fragment_workout.view.*

class WorkoutFragment : Fragment(), OnMapReadyCallback {

    private lateinit var workoutViewModel: WorkoutViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    //private lateinit var googleMap: GoogleMap
    private var mapView: MapView = requireView().findViewById(R.id.mapView)
    private var mapsSupported = true;

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
            mapView.onCreate(null)
            mapView.onResume()
            mapView.getMapAsync(this)
        }

        //Figure out what his means for getting constant location
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    //Update UI with location data
                }
            }
        }

        //Start the workout
        buttonStart.setOnClickListener(View.OnClickListener { v: View ->

        })

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Get users current location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity().applicationContext)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                Toast.makeText(activity, location.toString(), Toast.LENGTH_LONG).show()

                //Users Location on the map

            } else {
                //Toast.makeText(activity, "Location Unsuccesful", Toast.LENGTH_SHORT).show()



            }
        }
    }

    override fun onMapReady(p0: GoogleMap?) {
        MapsInitializer.initialize(context)
        //googleMap = p0
    }

    //private fun startLocationUpdates() {
    //    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    //}

    //private fun stopLocationUpdates() {
    //    fusedLocationClient.removeLocationUpdates(locationCallback)
    //}
}
