package com.project.medikare.utils

import android.app.Application
import android.os.Build
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.project.medikare.database.PrescriptionDatabase

class MedikareApp: Application()
{
    val db by lazy {
        PrescriptionDatabase.Companion.getDatabase(this)
    }

    // Add this for Firestore (optional but recommended)
    val firestore by lazy{
        FirebaseFirestore.getInstance()
    }

    override fun onCreate()
    {

        FirebaseApp.initializeApp(this)
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            DynamicColors.applyToActivitiesIfAvailable(this)// Android 12+
        }
    }
}