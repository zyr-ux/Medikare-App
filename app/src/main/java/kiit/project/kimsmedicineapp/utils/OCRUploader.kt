package kiit.project.kimsmedicineapp.utils

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException

class OCRUploader
{
    private val client = OkHttpClient()
    fun uploadImage(imagePath: String, serverUrl: String, callback: (String?, Exception?) -> Unit) {
        // Create the file object
        val imageFile = File(imagePath)
        Log.e("Image file","$imageFile")

        if (!imageFile.exists()) {
            callback(null, Exception("Image file does not exist at path: $imagePath"))
            return
        }

        // Create the RequestBody for the image
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image", imageFile.name,
                RequestBody.create("image/png".toMediaTypeOrNull(), imageFile)
            )
            .build()

        // Build the POST request
        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        // Send the request asynchronously
        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback(null, e)
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    callback(response.body?.string(), null)
                } else {
                    callback(null, Exception("Server responded with error: ${response.code}"))
                }
            }
        })
    }
}