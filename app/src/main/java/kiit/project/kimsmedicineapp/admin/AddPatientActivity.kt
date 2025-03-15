package kiit.project.kimsmedicineapp.admin

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import kiit.project.kimsmedicineapp.R
import kiit.project.kimsmedicineapp.database.KIMSApp
import kiit.project.kimsmedicineapp.database.KIMSDao
import kiit.project.kimsmedicineapp.database.PatientEntity
import kiit.project.kimsmedicineapp.databinding.ActivityAddPatientBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddPatientActivity : AppCompatActivity()
{
    private var binding:ActivityAddPatientBinding?=null
    private lateinit var kimsDao:KIMSDao
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityAddPatientBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        binding?.root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
                insets
            }
        }
        actionBar()
        kimsDao=(application as KIMSApp).db.kimsdao()
        binding?.genderEt?.setOnClickListener{
            showGenderPickerPopup(binding?.genderEt!!)
        }
        binding?.dateEt?.setOnClickListener{
            datePicker()
        }
        binding?.saveBtn?.setOnClickListener{
            addDetails(kimsDao)
            finish()
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

    private fun showGenderPickerPopup(editText: EditText)
    {
        // Inflate the popup layout
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_gender_picker, null)

        // Initialize the views in the popup
        val radioGroupGender: RadioGroup = dialogView.findViewById(R.id.radioGroupGender)
        val btnDone: Button = dialogView.findViewById(R.id.btnDone)

        // Create an AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnDone.setOnClickListener {
            val selectedId = radioGroupGender.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Please select a gender.", Toast.LENGTH_SHORT).show()
            } else {
                val selectedRadioButton: RadioButton = dialogView.findViewById(selectedId)
                val selectedGender = selectedRadioButton.text.toString()
                editText.setText(selectedGender)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun datePicker()
    {
        val calendarconstraints = CalendarConstraints.Builder().setValidator(
            DateValidatorPointBackward.now())
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select the date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .setCalendarConstraints(calendarconstraints.build())
            .build()
        datePicker.show(supportFragmentManager, "DATE_PICKER")
        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = selection
            val sdf= SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            val date= Date(selectedDate)
            val formattedDate=sdf.format(date)
            binding?.dateEt?.setText(formattedDate)
        }
    }

    private fun addDetails(kimsDao: KIMSDao)
    {
        val dname=binding?.doctornameEt?.text.toString()
        val pname=binding?.patientnameEt?.text.toString()
        val date=binding?.dateEt?.text.toString()
        val age=binding?.ageEt?.text.toString()
        val gender=binding?.genderEt?.text.toString()
        val weight=binding?.weightEt?.text.toString()
        val symptoms=binding?.symptomsEt?.text.toString()

        if(dname.isNotEmpty()&&pname.isNotEmpty()&&date.isNotEmpty()&&age.isNotEmpty()&&gender.isNotEmpty()&&weight.isNotEmpty()&&symptoms.isNotEmpty())
        {
            val age_int=age.toInt()
            val weight_double=weight.toDouble()
            lifecycleScope.launch {
                kimsDao.insert(PatientEntity(doctorname = dname, patientname = pname, date = date,
                    age = age_int, gender = gender, weight = weight_double, symptons = symptoms))
            }
        }else{
            Toast.makeText(this,"Fill out all the details",Toast.LENGTH_SHORT).show()
        }

    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean
    {
        // Check if the touch event is outside the EditText
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is TextInputEditText) {
                val outRect = android.graphics.Rect()
                view.getGlobalVisibleRect(outRect)
                if (view.id!=R.id.date_et &&!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) // trying the make the date picker not lose focus doesnt work but the logic works for any other edit text
                {
                    // Clear focus and hide the keyboard
                    view.clearFocus()
                    hideKeyboard(view)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun hideKeyboard(view: EditText)
    {
        val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onDestroy()
    {
        binding=null
        super.onDestroy()
    }

}