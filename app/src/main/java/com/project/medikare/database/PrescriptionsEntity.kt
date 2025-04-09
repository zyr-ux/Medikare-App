package com.project.medikare.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey val prescriptionID: String = "",
    val createdAt: Long = System.currentTimeMillis(),  // Converted from Firestore Timestamp
    val doctorName: String = "",
    val doctorCredentials: String = "",
    val doctorPhone: String = "",
    val consultationDate: String = "",
    val consultationType: String = "",
    val patientName: String = "",
    val patientSex: String = "",
    val patientAge: Int = 0,
    val patientWeight: Double = 0.0,
    val patientHeight: Int = 0,
    val patientBMI: Double = 0.0,
    val patientBP: String = "",
    val diagnosis: String = "",
    var isExpanded: Boolean = false, // Track expansion state
    @TypeConverters(MedicationConverter::class) val medications: List<Medication> = emptyList()
)

data class Medication(
    val medicine: String = "",
    val dosage: String = "",
    val details1: String = "",
    val details2: String = ""
)
