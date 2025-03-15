package kiit.project.kimsmedicineapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "KIMS Database")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val doctorname:String,
    val patientname:String,
    val date:String,
    val age:Int,
    val gender:String,
    val weight:Double,
    val symptons:String
) :Serializable