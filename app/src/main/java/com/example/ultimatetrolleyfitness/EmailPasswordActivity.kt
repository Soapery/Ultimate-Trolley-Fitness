package com.example.ultimatetrolleyfitness

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ultimatetrolleyfitness.databinding.ActivityEmailPasswordBinding
import com.example.ultimatetrolleyfitness.ui.theme.UltimateTrolleyFitnessTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class EmailPasswordActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var binding: ActivityEmailPasswordBinding
    private val viewModel: EmailPasswordViewModel by viewModels()

    fun isValidEmail(target: CharSequence?): Boolean {
        return if (TextUtils.isEmpty(target)) {
            false
        } else {
            Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()

            if (viewModel.isSignInState.value == true) {
                signIn(email, password)
            } else {
                editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
                val confirmPassword = binding.editTextConfirmPassword.text.toString()
                val emailValid = isValidEmail(email)

                if(password == confirmPassword && emailValid) {
                    createAccount(email, password)
                } else {
                    if(!emailValid) {
                        Toast.makeText(
                            baseContext,
                            "Email is Invalid!",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    if(password !== confirmPassword) {
                        Toast.makeText(
                            baseContext,
                            "Passwords don't match!",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }
            }
        }

        binding.buttonToggle.setOnClickListener {
            viewModel.toggleState()
        }
    }

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            reload()
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Account creation failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Login failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                // Email Verification sent
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        // Launches main view, but should implement passing user object through to it.
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun reload() {

    }

    companion object {
        private const val TAG = "EmailPassword"
    }
}

@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    UltimateTrolleyFitnessTheme {
        Greeting2("Android")
    }
}