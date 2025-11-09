package com.matrix.autoreply.network

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseConfigService {
    
    fun getTutorialUrl(callback: (String?) -> Unit) {
        FirebaseDatabase.getInstance()
            .getReference("app_config")
            .child("tutorial_url")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.getValue(String::class.java))
                }
                
                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }
}