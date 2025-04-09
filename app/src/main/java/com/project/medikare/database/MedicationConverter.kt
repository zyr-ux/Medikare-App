package com.project.medikare.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MedicationConverter {
    
    @TypeConverter
    fun fromMedicationList(value: List<Medication>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toMedicationList(value: String): List<Medication> {
        val type = object : TypeToken<List<Medication>>() {}.type
        return Gson().fromJson(value, type)
    }
}
