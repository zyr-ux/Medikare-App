package kiit.project.kimsmedicineapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface KIMSDao {

    @Insert
    suspend fun insert(patientEntity: PatientEntity)

    @Update
    suspend fun update(patientEntity: PatientEntity)

    @Delete
    suspend fun delete(patientEntity: PatientEntity)

    @Query("SELECT * FROM `KIMS Database`")
    fun fetchAllPatients():Flow<List<PatientEntity>>

    @Query("SELECT * FROM `KIMS Database` WHERE id=:id")
    fun fetchPatientbyID(id:Int):Flow<PatientEntity>

    @Query("SELECT COUNT(*) FROM `KIMS Database` WHERE id = :userId")
    suspend fun isIdExists(userId: Int): Int
}