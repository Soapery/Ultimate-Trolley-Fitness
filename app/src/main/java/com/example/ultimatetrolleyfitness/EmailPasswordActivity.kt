package com.example.ultimatetrolleyfitness

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ultimatetrolleyfitness.ui.theme.UltimateTrolleyFitnessTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class EmailPasswordActivity : ComponentActivity() {
    private val viewModel: EmailPasswordViewModel by viewModels {
        EmailPasswordViewModelFactory(this, application)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.auth = FirebaseAuth.getInstance()

        setContent {
            UltimateTrolleyFitnessTheme {
                LoginScreen(viewModel)
            }
        }
    }

    public override fun onStart() {
        super.onStart()

        val currentUser = viewModel.auth.currentUser
        if (currentUser != null) {
            viewModel.reload()
        }
    }

    fun updateUI(user: FirebaseUser?) {
        // Launches main view, but should implement passing user object through to it.
        val intent = Intent(this@EmailPasswordActivity, MainActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun LoginScreen(viewModel: EmailPasswordViewModel) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val isSignInState by viewModel.isSignInState.observeAsState(true)
        val email: String by viewModel.email.observeAsState("")
        val password: String by viewModel.password.observeAsState("")
        val confirmPassword: String by viewModel.confirmPassword.observeAsState("")

        EmailField(email, viewModel)
        PasswordField(password, viewModel)

        if (!isSignInState) {
            ConfirmPasswordField(confirmPassword, viewModel)
        }

        Button(
            onClick = {
                if (isSignInState) {
                    viewModel.signIn(email, password)
                } else {
                    viewModel.createAccount(email, password, confirmPassword)
                }
            }
        ) {
            Text(text = if (isSignInState) "Login" else "Create Account")
        }

        Button(
            onClick = { viewModel.toggleState() }
        ) {
            Text(text = if (isSignInState) "Don't have an account? Create one!" else "Have an account? Login")
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun EmailField(email: String, viewModel: EmailPasswordViewModel) {
    OutlinedTextField(
        value = email,
        onValueChange = { viewModel.onEmailChanged(it) },
        singleLine = true,
        label = { Text("Email Address") },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Email
        ),
        textStyle = TextStyle.Default.copy(fontSize = 16.sp)
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PasswordField(password: String, viewModel: EmailPasswordViewModel) {
    OutlinedTextField(
        value = password,
        onValueChange = { viewModel.onPasswordChanged(it) },
        label = { Text("Password") },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password
        ),
        textStyle = TextStyle.Default.copy(fontSize = 16.sp),
        visualTransformation = PasswordVisualTransformation()
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ConfirmPasswordField(confirmPassword: String, viewModel: EmailPasswordViewModel) {
    OutlinedTextField(
        value = confirmPassword,
        onValueChange = { viewModel.onConfirmPasswordChanged(it) },
        singleLine = true,
        label = { Text("Confirm Password") },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Password
        ),
        textStyle = TextStyle.Default.copy(fontSize = 16.sp),
        visualTransformation = PasswordVisualTransformation()
    )
}