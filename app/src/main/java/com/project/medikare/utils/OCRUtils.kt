package com.project.medikare.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.File
import java.io.IOException

object OCRUtils {

    private val client = OkHttpClient()

    fun runOCR_ID(context: Context, imageUri: Uri, onResult: (String?) -> Unit) {
        val file = File(imageUri.path ?: return)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.name, RequestBody.create("image/jpeg".toMediaTypeOrNull(), file))
            .build()

        val request = Request.Builder()
            .url("http://192.46.209.105/id")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                (context as? android.app.Activity)?.runOnUiThread {
                    Toast.makeText(context, "OCR ID failed", Toast.LENGTH_SHORT).show()
                    onResult(null) // Ensure callback is triggered
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string()
                Log.d("OCR_ID", "Response: $responseString")

                try {
                    val json = JSONObject(responseString)
                    val id = json.optString("Prescription ID", "Not Found")

                    (context as? android.app.Activity)?.runOnUiThread {
                        onResult(id)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    (context as? android.app.Activity)?.runOnUiThread {
                        Toast.makeText(context, "OCR ID parsing failed", Toast.LENGTH_SHORT).show()
                        onResult(null) // Fallback
                    }
                }
            }
        })
    }

    fun runOCR_Medicines(context: Context, imageUri: Uri, onResult: (List<String>?) -> Unit) {
        val file = File(imageUri.path ?: return)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.name, RequestBody.create("image/jpeg".toMediaTypeOrNull(), file))
            .build()

        val request = Request.Builder()
            .url("http://192.46.209.105/medi")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                (context as? android.app.Activity)?.runOnUiThread {
                    Toast.makeText(context, "OCR Medicines failed", Toast.LENGTH_SHORT).show()
                    onResult(null)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string()
                Log.d("OCR_MEDS", "Response: $responseString")

                try {
                    val json = JSONObject(responseString)
                    val medicinesArray = json.optJSONArray("Medicines")

                    val meds = mutableListOf<String>()
                    for (i in 0 until (medicinesArray?.length() ?: 0)) {
                        meds.add(medicinesArray?.getString(i) ?: "")
                    }

                    (context as? android.app.Activity)?.runOnUiThread {
                        onResult(meds)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    (context as? android.app.Activity)?.runOnUiThread {
                        Toast.makeText(context, "OCR Medicines parsing failed", Toast.LENGTH_SHORT).show()
                        onResult(null)
                    }
                }
            }
        })
    }
}
