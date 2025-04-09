package com.project.medikare.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity)

    @Query("SELECT * FROM prescriptions WHERE prescriptionID = :id")
    suspend fun getPrescriptionById(id: String): PrescriptionEntity?

    @Query("SELECT * FROM prescriptions")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    @Query("DELETE FROM prescriptions")
    suspend fun deleteAll()
}
