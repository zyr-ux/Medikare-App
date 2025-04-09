package com.project.medikare

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.medikare.database.PrescriptionDao
import com.project.medikare.databinding.ActivityHistoryBinding
import com.project.medikare.utils.HistoryAdapter
import com.project.medikare.utils.MedikareApp
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var prescriptionDao: PrescriptionDao
    private lateinit var historyAdapter: HistoryAdapter
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        prescriptionDao=(application as MedikareApp).db.prescriptionDao()
        setUpHistoryRV()
        lifecycleScope.launch {
            prescriptionDao.getAllPrescriptions().collect { prescriptions ->
                historyAdapter.updateData(ArrayList(prescriptions)) // Update the existing adapter
            }
        }

        binding.clearBtn.setOnClickListener {
            lifecycleScope.launch {
                for (i in historyAdapter.itemCount - 1 downTo 0) { // Animate from last item to first
                    val holder = binding.RVHistory.findViewHolderForAdapterPosition(i)
                    holder?.itemView?.animate()
                        ?.translationX(holder.itemView.width.toFloat()) // Slide out to right
                        ?.alpha(0f) // Fade out
                        ?.setDuration(300)
                        ?.setStartDelay(((historyAdapter.itemCount - i) * 50).toLong()) // Stagger effect
                        ?.withEndAction {
                            if (i == 0) { // After last animation, clear the database
                                lifecycleScope.launch {
                                    prescriptionDao.deleteAll()
                                }
                            }
                        }
                        ?.start()
                }
            }
        }

    }

    private fun setUpHistoryRV()
    {
        historyAdapter = HistoryAdapter(this, ArrayList())
        binding.RVHistory.layoutManager = LinearLayoutManager(this)
        binding.RVHistory.adapter = historyAdapter
    }
}