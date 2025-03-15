package kiit.project.kimsmedicineapp.database

import android.app.Application

class KIMSApp: Application() {
    val db by lazy {
        KIMSDatabase.getInstance(this)
    }
}