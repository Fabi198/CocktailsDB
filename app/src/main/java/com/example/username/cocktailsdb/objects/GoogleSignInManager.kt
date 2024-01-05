package com.example.username.cocktailsdb.objects

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.username.cocktailsdb.MainActivity
import com.example.username.cocktailsdb.R
import com.example.username.cocktailsdb.objects.Preferences.saveGoogleUserData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class GoogleSignInManager private constructor() {

    private var context: Context? = null
    private var activity: Activity? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    val GOOGLE_SIGN_IN = 100
    private var mAuth: FirebaseAuth? = null
    private var providerType = ProviderType()
    private fun init(context: Context) {
        this.context = context
        activity = context as Activity
    }

    companion object {
        private var instance: GoogleSignInManager? = null
        fun getInstance(context: Context): GoogleSignInManager? {
            if (instance == null) {
                instance = GoogleSignInManager()
            }
            instance!!.init(context)
            return instance
        }
    }

    fun setupGoogleSignInOptions() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context!!.resources.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(context!!, gso)
        mAuth = FirebaseAuth.getInstance()
    }

    val isUserAlreadySignIn: Boolean
        get() {
            val currentUser = mAuth!!.currentUser
            return currentUser != null
        }

    fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        activity!!.startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        mGoogleSignInClient!!.signOut()
        Toast.makeText(context, "Signed Out", Toast.LENGTH_SHORT).show()
        val prefsEd = activity!!.getSharedPreferences("Preferences", Context.MODE_PRIVATE).edit()
        prefsEd.clear()
        prefsEd.apply()
        activity!!.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    val profileInfo: FirebaseUser?
        get() {
            val account = FirebaseAuth.getInstance().currentUser
            if (account != null) {

            } else {
                Toast.makeText(context, "No account info found", Toast.LENGTH_SHORT).show()
            }
            return account
        }

    fun handleSignInResult(data: Intent?) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken)
        } catch (e: ApiException) {
            Log.w("PortetGoogle", "signInResult:failed code=${e.statusCode}")
        }
    }

    fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("PortetGoogle", "signInWithCredential:success")
                    val user = mAuth!!.currentUser
                    saveGoogleUserData(activity!!.getSharedPreferences("Preferences", Context.MODE_PRIVATE), user!!.email!!, providerType.GOOGLE)
                    Toast.makeText(context, "Signed in successfully", Toast.LENGTH_SHORT).show()
                    activity!!.startActivity(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))

                } else {
                    Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show()
                    Log.w("PortetGoogle", "signInWithCredential:failure:${task.exception}")
                }
            }
    }
}