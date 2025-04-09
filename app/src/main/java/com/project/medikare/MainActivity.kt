package com.project.medikare

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.project.medikare.database.PrescriptionDao
import com.project.medikare.database.PrescriptionViewModel
import com.project.medikare.databinding.ActivityMainBinding
import com.project.medikare.utils.MedikareApp
import com.project.medikare.utils.OCRUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.UUID
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth
import com.project.medikare.utils.MedicationTableBuilder
import kotlin.math.abs
import androidx.core.graphics.toColorInt
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityMainBinding
    val viewModel: PrescriptionViewModel by viewModels()
    private lateinit var prescriptionDao: PrescriptionDao
    private var fullSiedImageUri:Uri?=null
    private lateinit var imageUri: Uri
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.patientDetailView.visibility=View.GONE
        prescriptionDao=(application as MedikareApp).db.prescriptionDao()

        binding.verifyBtn.setOnClickListener {
            val prescriptionID = binding.patientIdInput.text.toString().trim()
            fetchFirestore(prescriptionID)
            hideKeyboardwhenever()
        }

        binding.historyBtn.setOnClickListener {
            val intent= Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        binding.iconOCR.setOnClickListener {
            ocrDialogue()
        }

        binding.patientIdInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?)
            {
                binding.iconClear.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.iconClear.setOnClickListener {
            binding.patientIdInput.text?.clear()
        }

        binding.profileBtn.setOnClickListener {
            val intent= Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume()
    {
        updateLoginStatus()
        super.onResume()
    }


    private fun updateLoginStatus()
    {
        val user = auth.currentUser
        if (user!=null){
            binding.loginStatus.setTextColor(Color.GREEN)
            binding.loginStatus.text="You are logged in!"
        }
        else{
            binding.loginStatus.setTextColor(Color.RED)
            binding.loginStatus.text="You are not logged in!"
        }
    }

    private fun fetchFirestore(prescriptionID:String)
    {
        val user = FirebaseAuth.getInstance().currentUser
        if(user!=null){
            if (prescriptionID.isNotEmpty())
            {
                binding.iconClear.isEnabled=false
                // Fetch from Firestore and store in Room first
                viewModel.fetchAndStorePrescription(prescriptionID)
                {
                    // After Firestore fetch completes, now check Room
                    viewModel.getPrescriptionFromRoom(prescriptionID) { prescription ->
                        if (prescription != null)
                        {
                            binding.patientDetailView.visibility=View.GONE
                            Log.d("ROOM_DB", "Retrieved from Room: $prescription")
                            Toast.makeText(this, "Prescription Retrieved: ${prescription.prescriptionID}", Toast.LENGTH_SHORT).show()
                            binding.checkAnimation.visibility= View.VISIBLE
                            binding.checkAnimation.setAnimation(R.raw.success_check)
                            binding.checkAnimation.playAnimation()
                            binding.checkAnimation.addAnimatorListener(object : AnimatorListenerAdapter(){
                                override fun onAnimationEnd(animation: Animator) {
                                    binding.tvDate.text="Consultation Date: ${prescription.consultationDate}"
                                    binding.tvPatientName.text="Patient: ${prescription.patientName}"
                                    binding.tvDoctorName.text="Doctor: ${prescription.doctorName}"
                                    MedicationTableBuilder.build(this@MainActivity,binding.medTable,prescription.medications)
                                    val fadeAnimation= AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade)
                                    with(binding.patientDetailView) {
                                        visibility = View.VISIBLE
                                        alpha = 1f
                                        translationX = 0f
                                        scaleX = 1f
                                        scaleY = 1f
                                    }
                                    binding.patientDetailView.visibility=View.VISIBLE
                                    binding.patientDetailView.startAnimation(fadeAnimation)
                                    dismissCard {
                                        binding.iconClear.isEnabled=true
                                    }
                                }
                            })
                        }
                        else
                        {
                            binding.patientDetailView.visibility = View.GONE
                            binding.checkAnimation.removeAllAnimatorListeners()
                            Log.e("ROOM_DB", "No prescription found in Room for ID: $prescriptionID")
                            Toast.makeText(this, "Prescription Not Found", Toast.LENGTH_SHORT).show()
                            binding.checkAnimation.visibility= View.VISIBLE
                            binding.checkAnimation.setAnimation(R.raw.error_check_2)
                            binding.checkAnimation.playAnimation()
                            binding.checkAnimation.addAnimatorListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    binding.checkAnimation.animate()
                                        .alpha(0f)
                                        .setDuration(500)
                                        .withEndAction {
                                            binding.checkAnimation.visibility = View.GONE
                                            binding.checkAnimation.alpha = 1f // Reset alpha in case it's reused
                                            binding.iconClear.isEnabled=true
                                        }
                                }
                            })
                        }
                    }
                }
            }
            else
            {
                val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
                binding.verifyBtn.startAnimation(shakeAnimation)
            }
        }
        else{
            Toast.makeText(this,"User Not Logged in", Toast.LENGTH_SHORT).show()
        }

    }

    private fun ocrDialogue()
    {
        val dialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_dialogue,null)

        val btnPhotoOCR = view.findViewById<LinearLayout>(R.id.btnPhotoOCR)
        val btnImageOCR = view.findViewById<LinearLayout>(R.id.btnImageOCR)

        btnPhotoOCR.setOnClickListener {
            dialog.dismiss()
            addImgfromCamera()

        }

        btnImageOCR.setOnClickListener {
            dialog.dismiss()
            addImagefromGallery()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun showRationalDialogueForPermissions()
    {
        AlertDialog.Builder(this).setMessage("Looks like you have not granted the required permissions. " +
                "Please grant those").setPositiveButton("Go to settings") { _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel"){dialog,_->
            dialog.dismiss()
        }.show()
    }

    private fun addImagefromGallery()
    {
        if(Build.VERSION.SDK_INT>=33)
        {
            Dexter.withContext(this).withPermissions(Manifest.permission.READ_MEDIA_IMAGES)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            val galleryIntent =
                                Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                )
                            startActivityForResult(galleryIntent, GALLERY)
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>,
                        token: PermissionToken
                    ) {
                        showRationalDialogueForPermissions()
                    }
                }).onSameThread().check()
        }
        else
        {
            Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted()) {
                            val galleryIntent =
                                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                            startActivityForResult(galleryIntent, GALLERY)
                        }
                    }
                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                        showRationalDialogueForPermissions()
                    }
                }).onSameThread().check()
        }
    }

    private fun addImgfromCamera()
    {
        Dexter.withContext(this).withPermissions(Manifest.permission.CAMERA)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val imageFile = createImageFile()
                        imageUri = FileProvider.getUriForFile(this@MainActivity, "com.project.medikare.fileprovider", imageFile)
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                        startActivityForResult(cameraIntent, CAMERA)
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
                    showRationalDialogueForPermissions()
                }
            }).onSameThread().check()
    }

    private fun createImageFile(): File
    {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${System.currentTimeMillis()}_", ".jpg", storageDir)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val selectedImgBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        fullSiedImageUri = saveImagetoInternalStorage(selectedImgBitmap)
                        Log.e("Gallery Img :", "Path : $fullSiedImageUri")
                        runOCR_ID()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if(requestCode == CAMERA)
            {
                try {
                    val fullSizeBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                    fullSiedImageUri=saveImagetoInternalStorage(fullSizeBitmap)
                    Log.e("Camera Img :","Path : $fullSiedImageUri")
                    runOCR_ID()
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Image capture failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveImagetoInternalStorage(bitmap: Bitmap):Uri
    {
        val wrapper= ContextWrapper(applicationContext)
        var file=wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file= File(file,"${UUID.randomUUID()}.jpg")
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
        return file.absolutePath.toUri()
    }

    private fun runOCR_ID()
    {
        runOnUiThread {
            binding.iconOCR.visibility=View.GONE
            binding.circularProgressBar.visibility=View.VISIBLE
        }
        fullSiedImageUri?.let { it ->
            // Get ID as string and do whatever you want with it
            OCRUtils.runOCR_ID(this, it) { id ->

                if(id!=null)
                {
                    if (id!="Not Found")
                    {
                        binding.patientIdInput.setText(id)
                        binding.patientIdInput.setSelection(id.length)
                        runOnUiThread{
                            binding.iconOCR.visibility=View.VISIBLE
                            binding.circularProgressBar.visibility=View.GONE
                        }
                    }
                    else
                    {
                        runOnUiThread{
                            binding.iconOCR.visibility=View.VISIBLE
                            binding.circularProgressBar.visibility=View.GONE
                        }
                        Toast.makeText(this,"Not Found",Toast.LENGTH_SHORT).show()
                    }

                }
                else
                {
                    runOnUiThread{
                        binding.iconOCR.visibility=View.VISIBLE
                        binding.circularProgressBar.visibility=View.GONE
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun dismissCard(onDismissed: () -> Unit)
    {
        binding.patientDetailView.setOnTouchListener(object : View.OnTouchListener {
            private var downX = 0f
            private var isSwiping = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        downX = event.rawX
                        isSwiping = false
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = event.rawX - downX
                        if (abs(deltaX) > 10) isSwiping = true

                        if (isSwiping) {
                            v.translationX = deltaX
                            v.alpha = 1 - (abs(deltaX) / v.width.toFloat()).coerceIn(0f, 1f)
                        }
                        return true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        val deltaX = event.rawX - downX
                        val threshold = v.width * 0.35

                        if (abs(deltaX) > threshold) {
                            val direction = if (deltaX > 0) 1 else -1
                            v.animate()
                                .translationX(direction * v.width.toFloat())
                                .alpha(0f)
                                .setDuration(200)
                                .withEndAction {
                                    v.visibility = View.GONE
                                    onDismissed() //  Callback here
                                }
                                .start()
                        } else {
                            v.animate()
                                .translationX(0f)
                                .alpha(1f)
                                .setDuration(200)
                                .start()
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean
    {
        // Check if the touch event is outside the EditText
        if (ev.action == MotionEvent.ACTION_DOWN)
        {
            val view = currentFocus
            if (view is EditText) {
                val outRect = android.graphics.Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt()))
                {
                    // Clear focus and hide the keyboard
                    view.clearFocus()
                    hideKeyboard(view)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun hideKeyboard(view: View)
    {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun hideKeyboardwhenever()
    {
        val view = this.currentFocus
        if (view != null)
        {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
    }

    companion object
    {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "Medikare"

    }
}