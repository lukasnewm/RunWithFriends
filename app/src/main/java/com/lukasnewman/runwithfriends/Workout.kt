package com.lukasnewman.runwithfriends

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline

class Workout(var distance: Double, var locList: ArrayList<LatLng>, var time: Int, var title: String, var Username: String, var uID: String) {

    override fun toString(): String {
        return "Distance: " + distance + " Poly Line: " + locList + " Time: " + time + " Title: " + title + " Uploaded by: " + Username + "UID: " + uID
    }
}