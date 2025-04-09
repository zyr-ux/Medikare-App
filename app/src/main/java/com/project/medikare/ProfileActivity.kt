package com.project.medikare

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.project.medikare.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity()
{

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            if (idToken != null)
            {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                showProgressOverlay()
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful)
                        {
                            val user = auth.currentUser
                            val userMap = hashMapOf(
                                "email" to user?.email,
                                "uid" to user?.uid,
                                "createdAt" to System.currentTimeMillis()
                            )
                            FirebaseFirestore.getInstance().collection("users")
                                .document(user!!.uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    hideProgressOverlay()
                                    fadeInProfile()
                                }
                                .addOnFailureListener {
                                    hideProgressOverlay()
                                    Toast.makeText(
                                        this,
                                        "Failed to save user data",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    fadeInProfile()
                                }
                        }
                        else
                        {
                            hideProgressOverlay()
                            showLogin()
                            Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        setupGoogleSignIn()
        binding.linkingProviderCard.visibility= View.GONE
        binding.btnEmailLogin.setOnClickListener { handleEmailLogin() }
        binding.btnGoogle.setOnClickListener { startGoogleSignIn() }
        binding.btnSignOut.setOnClickListener { signOut() }
        binding.btnDelete.setOnClickListener {
            deleteAccount()
        }
        profileImage()
        if (auth.currentUser != null) {
            showProfile()
        } else {
            showLogin()
        }
        val provider = getCurrentSignInProvider()
        if (provider == "google.com")
        {
            // Ask user: "Do you want to add a password so you can also login with email?"
            // If yes, show a form to enter password and call linkPasswordToCurrentGoogleAccount()
            binding.btnLink.visibility=View.VISIBLE
        }
        binding.btnLink.setOnClickListener {
            binding.linkingProviderCard.visibility = View.VISIBLE
            val animation = AnimationUtils.loadAnimation(this, R.anim.fade)
            binding.linkingProviderCard.startAnimation(animation)
        }

        binding.btnLinkPass.setOnClickListener {
            val email = binding.etLinkEmail.text.toString().trim()
            val password = binding.etLinkPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            linkEmailPasswordToGoogle(email, password,
                onSuccess = {
                    val animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {
                            binding.linkingProviderCard.visibility = View.GONE
                        }
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationRepeat(animation: Animation?) {}
                    })
                    binding.linkingProviderCard.startAnimation(animation)
                },
                onFailure = {
                    Toast.makeText(this, "Linking Failed", Toast.LENGTH_SHORT).show()
                    val animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                    animation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationEnd(animation: Animation?) {
                            binding.linkingProviderCard.visibility = View.GONE
                        }
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationRepeat(animation: Animation?) {}
                    })
                    binding.linkingProviderCard.startAnimation(animation)
                }
            )
        }
    }

    private fun getCurrentSignInProvider(): String?
    {
        val user = FirebaseAuth.getInstance().currentUser
        return user?.providerData
            ?.firstOrNull { it.providerId != "firebase" }  // <- this is the correct check
            ?.providerId
    }

    override fun onResume()
    {
        profileImage()
        super.onResume()
    }

    private fun setupGoogleSignIn()
    {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    private fun profileImage()
    {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Load the profile picture using Glide
            val photoUrl = user.photoUrl
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl)
                    .circleCrop()
                    .into(binding.ivUserProfile)
            }
        }
    }

    private fun handleEmailLogin()
    {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        showProgressOverlay()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { signInTask ->
            if (signInTask.isSuccessful) {
                hideProgressOverlay()
                fadeInProfile()
            } else {
                // Try registering
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { registerTask ->
                    if (registerTask.isSuccessful) {
                        val user = auth.currentUser
                        val userMap = hashMapOf(
                            "email" to user?.email,
                            "uid" to user?.uid,
                            "createdAt" to System.currentTimeMillis()
                        )
                        FirebaseFirestore.getInstance().collection("users")
                            .document(user!!.uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                hideProgressOverlay()
                                fadeInProfile()
                            }
                            .addOnFailureListener {
                                hideProgressOverlay()
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                fadeInProfile()
                            }
                    } else {
                        hideProgressOverlay()
                        showLogin()
                        Toast.makeText(this, "Auth Failed: ${registerTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun startGoogleSignIn()
    {
        showProgressOverlay()
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                googleSignInLauncher.launch(
                    IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                )
            }
            .addOnFailureListener {
                hideProgressOverlay()
                Toast.makeText(this, "Google Sign-In Error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fadeInProfile()
    {
        binding.apply {
            showProfile()
            profileCard.visibility = View.VISIBLE
            val animation = AnimationUtils.loadAnimation(this@ProfileActivity, R.anim.fade)
            profileCard.startAnimation(animation)
            val user = FirebaseAuth.getInstance().currentUser
            user?.photoUrl?.let { uri ->
                Glide.with(this@ProfileActivity)
                    .load(uri)
                    .placeholder(R.drawable.ic_baseline_account_circle_24)
                    .error(R.drawable.ic_baseline_account_circle_24)
                    .circleCrop()
                    .into(binding.ivUserProfile)
            }
        }
    }

    private fun showProfile()
    {
        binding.apply {
            loginLayout.visibility = View.GONE
            profileCard.visibility = View.VISIBLE
            val user = auth.currentUser
            tvUserDetails.text = "${user?.email}\n\nUID: ${user?.uid}"
        }
    }

    private fun showLogin()
    {
        binding.profileCard.visibility = View.GONE
        binding.loginLayout.visibility = View.VISIBLE
    }

    private fun signOut()
    {
        auth.signOut()
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        binding.profileCard.startAnimation(fadeOut)
        binding.profileCard.visibility = View.GONE
        binding.loginLayout.visibility = View.VISIBLE
    }

    private fun linkEmailPasswordToGoogle(email: String, password: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    {
        val credential = EmailAuthProvider.getCredential(email, password)
        val user = FirebaseAuth.getInstance().currentUser

        user?.linkWithCredential(credential)
            ?.addOnSuccessListener {
                Toast.makeText(this, "Password linked to Google account", Toast.LENGTH_SHORT).show()
                onSuccess()
            }
            ?.addOnFailureListener { e ->
                Toast.makeText(this, "Linking failed: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("Linking","Linking failed: ${e.message}")
                onFailure(e)
            }
    }



    private fun deleteAccount()
    {
        val user = auth.currentUser
        user?.delete()
            ?.addOnSuccessListener {
                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
                val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
                binding.profileCard.startAnimation(fadeOut)
                showLogin()
            }
            ?.addOnFailureListener {
                Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showProgressOverlay() {
        binding.progressOverlay.visibility = View.VISIBLE
    }

    private fun hideProgressOverlay() {
        binding.progressOverlay.visibility = View.GONE
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean
    {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is TextInputEditText) {
                val outRect = android.graphics.Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    view.clearFocus()
                    hideKeyboard(view)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun hideKeyboard(view: EditText)
    {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

}