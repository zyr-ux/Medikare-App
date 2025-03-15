package kiit.project.kimsmedicineapp.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kiit.project.kimsmedicineapp.adapters.AdminPageAdapter
import kiit.project.kimsmedicineapp.database.KIMSApp
import kiit.project.kimsmedicineapp.database.KIMSDao
import kiit.project.kimsmedicineapp.database.PatientEntity
import kiit.project.kimsmedicineapp.databinding.ActivityAdminMainBinding
import kiit.project.kimsmedicineapp.utils.SwipeToDeleteCallback
import kotlinx.coroutines.launch

class AdminMainActivity : AppCompatActivity()
{
    private var binding: ActivityAdminMainBinding?=null
    private lateinit var kimsDao: KIMSDao
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityAdminMainBinding.inflate(layoutInflater)
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
        lifecycleScope.launch {
            kimsDao.fetchAllPatients().collect{ list->
                val patientList=ArrayList(list)
                setupRV(patientList)
            }
        }
        binding?.fabBtn?.setOnClickListener{
            val intent=Intent(this,AddPatientActivity::class.java)
            startActivity(intent)
        }
    }

    private fun actionBar()
    {
        val statusBarHeight = resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))
        binding?.actionBarAddPlace?.setPadding(0, statusBarHeight, 0, 0)
        setSupportActionBar(binding?.actionBarAddPlace)
        if(supportActionBar!=null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        binding?.actionBarAddPlace?.setNavigationOnClickListener{
            onBackPressed()
        }
    }

    fun setupRV(patientList:ArrayList<PatientEntity>)
    {
        if(patientList.isNotEmpty())
        {
            binding?.recyclerView?.visibility= View.VISIBLE
            binding?.defaultView?.visibility=View.GONE
            val adminPageAdapter=AdminPageAdapter(this@AdminMainActivity,patientList)
            binding?.recyclerView?.layoutManager=LinearLayoutManager(this)
            binding?.recyclerView?.adapter=adminPageAdapter
        }
        else
        {
            binding?.recyclerView?.visibility=View.GONE
            binding?.defaultView?.visibility=View.VISIBLE
        }

        val deleteSwipeHandler=object:SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter=binding?.recyclerView?.adapter as AdminPageAdapter
                lifecycleScope.launch {
                    adapter.deleteItem(kimsDao,viewHolder.adapterPosition)
                }
            }
        }
        val deleteTouchHelper=ItemTouchHelper(deleteSwipeHandler)
        deleteTouchHelper.attachToRecyclerView(binding?.recyclerView)
    }

    override fun onDestroy()
    {
        binding=null
        super.onDestroy()
    }

}