package kiit.project.kimsmedicineapp

import android.content.Intent
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kiit.project.kimsmedicineapp.admin.AdminMainActivity
import kiit.project.kimsmedicineapp.databinding.ActivityMainBinding
import kiit.project.kimsmedicineapp.databinding.ActivityUserMainBinding
import kiit.project.kimsmedicineapp.user.UserMainActivity

class MainActivity : AppCompatActivity()
{
    private var binding:ActivityMainBinding?=null
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
                insets
            }
        }
        changeStatusBarIcons()
        binding?.USERBtn?.setOnClickListener{
            val intent=Intent(this,UserMainActivity::class.java)
            startActivity(intent)
        }

        binding?.ADMINBtn?.setOnClickListener {
            val intent=Intent(this,AdminMainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun changeStatusBarIcons()
    {
        val insetsController = window.insetsController
        insetsController?.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
    }

}