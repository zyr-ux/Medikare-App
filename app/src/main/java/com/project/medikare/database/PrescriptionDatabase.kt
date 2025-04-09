package com.project.medikare.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [PrescriptionEntity::class], version = 3)
@TypeConverters(MedicationConverter::class)
abstract class PrescriptionDatabase : RoomDatabase() {
    abstract fun prescriptionDao(): PrescriptionDao

    companion object {
        @Volatile
        private var INSTANCE: PrescriptionDatabase? = null

        fun getDatabase(context: Context): PrescriptionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrescriptionDatabase::class.java,
                    "prescription_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
