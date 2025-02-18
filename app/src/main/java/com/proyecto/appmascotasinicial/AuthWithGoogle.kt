package com.proyecto.appmascotasinicial

import android.app.Activity
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task

/**
 * Se encarga de la autenticación con google mediante Firebase Authentication
 *
 * @param activity Activity desde donde se instancia a la clase
 */
class AuthWithGoogle(private val activity: AppCompatActivity) {

    private val googleSignInLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleSignInResult(task)
            }
        }


    /**
     * Maneja el resultado de inicio de sesión después de que el usuario haya completado el proceso
     *
     * @param completedTask resultado de la operación de inicio de sesión
     */
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            //compruebo si el usuario ya existe, podré obtener su documento de firestore
                            Comun.db.collection("usuarios").document(user.uid).get()
                                .addOnSuccessListener { documento ->
                                    //si no existe el documento, hay que crear al usuario
                                    if (!documento.exists() || documento.metadata.isFromCache) {
                                        val nombreUsuario = account.displayName.toString()
                                        val email = account.email.toString()
                                        val provider = "GOOGLE"

                                        Comun.db.collection("usuarios").document(user.uid).set(
                                            hashMapOf(
                                                "nombre" to nombreUsuario,
                                                "email" to email,
                                                "provider" to provider
                                            )
                                        ).addOnSuccessListener {
                                            Log.d("SIGN UP GOOGLE", "Datos guardados correctamente")
                                        }
                                    }

                                    //te lleva a la activity main
                                    Comun.goToActivity(activity, MainActivity :: class.java)

                                }

                        }
                    }
                }
            }
        } catch (e: ApiException) {
            Log.w("AuthWithGoogle", "signInResult:failed code=" + e.statusCode)
        }
    }

    /**
     * Configura el inicio de sesión con google
     */
    fun setupGoogle() {
        val googleConf = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleClient = GoogleSignIn.getClient(activity, googleConf)
        googleSignInLauncher.launch(googleClient.signInIntent)
    }

}
