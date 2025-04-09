package kiit.project.kimsmedicineapp.user

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kiit.project.kimsmedicineapp.PatientDetailsActivity
import kiit.project.kimsmedicineapp.R
import kiit.project.kimsmedicineapp.database.KIMSApp
import kiit.project.kimsmedicineapp.database.KIMSDao
import kiit.project.kimsmedicineapp.database.PatientEntity
import kiit.project.kimsmedicineapp.databinding.ActivityUserMainBinding
import kiit.project.kimsmedicineapp.utils.OCRUploader
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class UserMainActivity : AppCompatActivity()
{
    private var binding:ActivityUserMainBinding?=null
    private lateinit var kimsDao: KIMSDao
    private var id:Int?=null
    private lateinit var intent: Intent
    private var fullSiedImageUri:Uri?=null
    private lateinit var imageUri: Uri
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityUserMainBinding.inflate(layoutInflater)
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
        binding?.cameraBtn?.setOnClickListener {
            fullSiedImageUri=null
            val pictureAlertDialogue=AlertDialog.Builder(this)
            pictureAlertDialogue.setTitle("Select image")
            val dialogueOptionArray= arrayOf("Select photo from your gallery","Capture image from camera")
            pictureAlertDialogue.setItems(dialogueOptionArray){
                    dialogue,which ->
                when(which){
                    1-> addImgfromCamera()
                    0-> addImagefromGallery()
                }
            }
            pictureAlertDialogue.show()
        }

        intent= Intent(this,PatientDetailsActivity::class.java)
        binding?.verifyBtn?.setOnClickListener{
            if (binding?.idET?.text?.isNotEmpty() == true) {
                id=binding?.idET?.text?.toString()?.toInt()
                Log.e("ID","$id")
                checkID(id!!)
            }
            else{
                val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
                binding?.verifyBtn?.startAnimation(shakeAnimation)
                binding?.patientDetailView?.visibility=View.GONE
                Toast.makeText(this,"Enter ID",Toast.LENGTH_SHORT).show()
            }
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

    private fun checkID(id:Int)
    {
        var entity:PatientEntity?=null
        lifecycleScope.launch {
            kimsDao.fetchPatientbyID(id).collect{
                entity=it
                Log.e("Patient","$entity")
                if (entity!=null)
                {
                    binding?.patientDetailView?.visibility=View.GONE
                    binding?.successAnimation?.playAnimation()
                    binding?.idNumber?.text= entity!!.id.toString()
                    binding?.date?.text= entity!!.date
                    binding?.patientName?.text=entity!!.patientname
                    binding?.successAnimation?.addAnimatorListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            // Animation ended, make the patientDetailView visible
                            val slideUpAnimation = AnimationUtils.loadAnimation(this@UserMainActivity, R.anim.slide_up)
                            binding?.patientDetailView?.visibility=View.VISIBLE
                            binding?.patientDetailView?.startAnimation(slideUpAnimation)
                        }
                    })
                    binding?.patientDetailView?.setOnClickListener {
                        intent.putExtra("ID",id)
                        startActivity(intent)
                    }
                }
                else
                {
                    binding?.patientDetailView?.visibility=View.GONE
                    val shakeAnimation = AnimationUtils.loadAnimation(this@UserMainActivity, R.anim.shake)
                    binding?.verifyBtn?.startAnimation(shakeAnimation)
                }
            }
        }

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
                        imageUri = FileProvider.getUriForFile(this@UserMainActivity, "kiit.project.kimsmedicineapp.fileprovider", imageFile)
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

    private fun runOCR()
    {
        runOnUiThread {
            binding?.cameraBtn?.visibility=View.GONE
            binding?.circularProgressBar?.visibility=View.VISIBLE
        }
        val imagePath = fullSiedImageUri
        Log.e("image ocr path","$fullSiedImageUri")
        val serverUrl = "http://13.61.147.216:5000/ocr"
        val ocrUploader = OCRUploader()
        ocrUploader.uploadImage(imagePath.toString(), serverUrl) { response, error ->
            if (error != null) {
                Log.e("Error","Error: ${error.message}")
                runOnUiThread {
                    Toast.makeText(this,"Sorry no ID is found",Toast.LENGTH_SHORT).show()
                    binding?.cameraBtn?.visibility=View.VISIBLE
                    binding?.circularProgressBar?.visibility=View.GONE
                }
            } else {
                val jsonObject = JSONObject(response)
                val patient_id = jsonObject.getString("ID") // Extracts the value of the "ID" key
                Log.e("OCR","Response from server: $response")
                // Update UI or perform actions that depend on responseET here
                runOnUiThread {
                    binding?.idET?.setText(patient_id)
                    binding?.cameraBtn?.visibility=View.VISIBLE
                    binding?.circularProgressBar?.visibility=View.GONE
                }
            }
        }
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
                        runOCR()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this, "Image selection failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if(requestCode== CAMERA)
            {
                try {
                    val fullSizeBitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                    fullSiedImageUri=saveImagetoInternalStorage(fullSizeBitmap)
                    Log.e("Camera Img :","Path : $fullSiedImageUri")
                    runOCR()
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
        return Uri.parse(file.absolutePath)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean
    {
        // Check if the touch event is outside the EditText
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is TextInputEditText) {
                val outRect = android.graphics.Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) // trying to the make the date picker not lose focus doesnt work but the logic works for any other edit text
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

    companion object
    {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "Medikare"

    }
}