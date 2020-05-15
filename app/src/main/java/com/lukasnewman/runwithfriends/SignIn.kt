package com.lukasnewman.runwithfriends

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_in.*


class SignIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        var signUpPresses = 0

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        //setup onclick listeners
        buttonSignIn.setOnClickListener(View.OnClickListener {
            signIn(editTextEmail.text.toString(), editTextPassword.text.toString())
        })

        buttonSignUp.setOnClickListener(View.OnClickListener {
            signUpPresses++

            if (signUpPresses > 1)
                createUser(editTextEmail.text.toString(), editTextPassword.text.toString(), editTextUsername.text.toString())
            else
                editTextUsername.isVisible = true
        })

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun createUser(email: String, password: String, username: String) {

        //TODO: check input and show loading bar
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser

                //Push new users details onto the server
                val uID = FirebaseAuth.getInstance().currentUser!!.uid
                var mainUser = User(email, username, uID)

                database.child("Users").child(uID).setValue(mainUser)

                updateUI(user)
            } else {
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
            //Hide loading bar here
        }
    }

    private fun signIn(email: String, password: String) {
        //TODO: check input and loading bar
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                updateUI(user)
            } else {
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_LONG).show()
            }
            //Hide loading bar here
        }
    }

    private fun signOut() {
        auth.signOut()
        //updateUI(null)
    }

    private fun updateUI(user : FirebaseUser?) {
        if (user != null) {
            val goToMain = Intent(this, MainActivity::class.java)
            startActivity(goToMain)
        }
    }
}
