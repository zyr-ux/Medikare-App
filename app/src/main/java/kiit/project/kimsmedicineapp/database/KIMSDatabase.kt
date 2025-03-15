package kiit.project.kimsmedicineapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PatientEntity::class], version = 1)
abstract class KIMSDatabase:RoomDatabase()
{
    abstract fun kimsdao():KIMSDao
    companion object {
        @Volatile
        private var INSTANCE: KIMSDatabase? = null
        fun getInstance(context: Context): KIMSDatabase
        {
            synchronized(this)
            {
                var instance = INSTANCE
                if (instance == null)
                {
                    instance = Room.databaseBuilder(context.applicationContext, KIMSDatabase::class.java,
                        "happyPlaces_database")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}