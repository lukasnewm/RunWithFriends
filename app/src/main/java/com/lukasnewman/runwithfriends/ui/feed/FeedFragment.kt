package com.lukasnewman.runwithfriends.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.lukasnewman.runwithfriends.R

class FeedFragment : Fragment() {

    private lateinit var feedViewModel: FeedViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        feedViewModel =
                ViewModelProviders.of(this).get(FeedViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_feed, container, false)
        //val textView: TextView = root.findViewById(R.id.text_dashboard)
        //dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            //textView.text = it
        //})
        return root
    }

    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance().getReference()

    }
}
