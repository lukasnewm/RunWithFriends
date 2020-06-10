package com.lukasnewman.runwithfriends

class User(var email: String, var username: String, val userID: String) {

    override fun toString(): String {
        return "Email: " + email + " Username: " + username + " User ID: " + userID
    }
}