package com.project.medikare.database

import android.app.Application
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class FirestoreRepository(private val application: Application)
{
    private val db = Firebase.firestore

    suspend fun fetchAndStorePrescription(prescriptionID: String, onComplete: () -> Unit) {
        try {
            val document = db.collection("prescriptions")
                .document(prescriptionID)
                .get()
                .await()

            if (document.exists()) {
                val prescriptionEntity = mapToPrescriptionEntity(document.data ?: emptyMap())

                val roomDb = PrescriptionDatabase.getDatabase(application)
                roomDb.prescriptionDao().insertPrescription(prescriptionEntity)

                Log.d("ROOM_INSERT", "Inserted into Room: $prescriptionEntity")
            } else {
                Log.e("FIRESTORE", "No document found for ID: $prescriptionID")
            }
        } catch (e: Exception) {
            Log.e("FIRESTORE_ERROR", "Error fetching prescription: ${e.message}")
            e.printStackTrace()
        } finally {
            // Notify when Firestore fetch & Room insert are complete
            onComplete()
        }
    }

    suspend fun getPrescriptionFromRoom(prescriptionID: String): PrescriptionEntity? {
        val roomDb = PrescriptionDatabase.getDatabase(application)
        return roomDb.prescriptionDao().getPrescriptionById(prescriptionID)
    }

    private fun mapToPrescriptionEntity(document: Map<String, Any>): PrescriptionEntity {
        return PrescriptionEntity(
            prescriptionID = document["prescriptionID"] as? String ?: "",
            createdAt = (document["createdAt"] as? com.google.firebase.Timestamp)?.seconds ?: System.currentTimeMillis(),
            doctorName = (document["doctorInfo"] as? Map<*, *>)?.get("name") as? String ?: "",
            doctorCredentials = (document["doctorInfo"] as? Map<*, *>)?.get("credentials") as? String ?: "",
            doctorPhone = (document["doctorInfo"] as? Map<*, *>)?.get("phone") as? String ?: "",
            consultationDate = (document["consultationDetails"] as? Map<*, *>)?.get("date") as? String ?: "",
            consultationType = (document["consultationDetails"] as? Map<*, *>)?.get("type") as? String ?: "",
            patientName = (document["patientInfo"] as? Map<*, *>)?.get("name") as? String ?: "",
            patientSex = (document["patientInfo"] as? Map<*, *>)?.get("sex") as? String ?: "",
            patientAge = (document["patientInfo"] as? Map<*, *>)?.get("age") as? Int ?: 0,
            patientWeight = (document["patientInfo"] as? Map<*, *>)?.get("weight") as? Double ?: 0.0,
            patientHeight = (document["patientInfo"] as? Map<*, *>)?.get("height") as? Int ?: 0,
            patientBMI = (document["patientInfo"] as? Map<*, *>)?.get("bmi") as? Double ?: 0.0,
            patientBP = (document["patientInfo"] as? Map<*, *>)?.get("bloodPressure") as? String ?: "",
            diagnosis = document["diagnosis"] as? String ?: "",
            medications = (document["medications"] as? List<Map<String, Any>>)?.map {
                Medication(
                    medicine = it["medicine"] as? String ?: "",
                    dosage = it["dosage"] as? String ?: "",
                    details1 = it["details1"] as? String ?: "",
                    details2 = it["details2"] as? String ?: ""
                )
            } ?: emptyList()
        )
    }
}