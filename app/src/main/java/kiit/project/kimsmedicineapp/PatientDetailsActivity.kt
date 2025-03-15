package kiit.project.kimsmedicineapp

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kiit.project.kimsmedicineapp.database.KIMSApp
import kiit.project.kimsmedicineapp.database.KIMSDao
import kiit.project.kimsmedicineapp.database.PatientEntity
import kiit.project.kimsmedicineapp.databinding.ActivityPatientDetailsBinding
import kotlinx.coroutines.launch

class PatientDetailsActivity : AppCompatActivity()
{
    private var binding:ActivityPatientDetailsBinding?=null
    private lateinit var kimsDao: KIMSDao
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityPatientDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
                insets
            }
        }
        kimsDao=(application as KIMSApp).db.kimsdao()
        actionBar()
        val patient_id=intent.getIntExtra("ID",0)
        Log.e("Patient ID","$patient_id")
        lifecycleScope.launch {
            kimsDao.fetchPatientbyID(patient_id).collect{
                setupDetails(it)
            }
        }

    }

    private fun actionBar()
    {
        val statusBarHeight = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))
        binding?.actionBar?.setPadding(0, statusBarHeight, 0, 0)
        setSupportActionBar(binding?.actionBar)
        if(supportActionBar!=null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.actionBar?.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    private fun setupDetails(entity: PatientEntity)
    {
        binding?.doctorNameTextView?.append(entity.doctorname)
        binding?.patientNameTextView?.append(entity.patientname)
        binding?.dateTextView?.append(entity.date)
        binding?.ageTextView?.append(entity.age.toString())
        binding?.genderTextView?.append(entity.gender)
        binding?.weightTextView?.append(entity.weight.toString())
        binding?.symptomsTextView?.append(entity.symptons)
    }

    override fun onDestroy()
    {
        binding=null
        super.onDestroy()
    }
}