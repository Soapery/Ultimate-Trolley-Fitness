package com.example.ultimatetrolleyfitness

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
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
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ConstraintLayout {
            val (image, loginForm) = createRefs()

            val isSignInState by viewModel.isSignInState.observeAsState(true)
            val email: String by viewModel.email.observeAsState("")
            val password: String by viewModel.password.observeAsState("")
            val confirmPassword: String by viewModel.confirmPassword.observeAsState("")


            Card(
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                colors = CardDefaults.cardColors(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp)
                    .constrainAs(loginForm) {
                        bottom.linkTo(parent.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp)
                ) {
                    InputField(
                        email,
                        "Email Address",
                        KeyboardType.Email,
                        VisualTransformation.None,
                        viewModel
                    )
                    InputField(
                        password,
                        "Password",
                        KeyboardType.Password,
                        PasswordVisualTransformation(),
                        viewModel
                    )

                    if (!isSignInState) {
                        InputField(
                            password,
                            "Confirm Password",
                            KeyboardType.Password,
                            PasswordVisualTransformation(),
                            viewModel
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isSignInState) {
                                    viewModel.signIn(email, password)
                                } else {
                                    viewModel.createAccount(email, password, confirmPassword)
                                }
                            }
                        ) {
                            Text(text = if (isSignInState) "Submit" else "Register")
                        }

                        OutlinedButton(
                            onClick = { viewModel.toggleState() }
                        ) {
                            Text(text = if (isSignInState) "Create Account" else "Login")
                        }
                    }
                }

            }
        }

    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun InputField(
    value: String,
    label: String,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation,
    viewModel: EmailPasswordViewModel
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            if (label.contains("Email")) {
                viewModel.onEmailChanged(it)
            } else if (label.contains("Confirm")) {
                viewModel.onConfirmPasswordChanged(it)
            } else {
                viewModel.onPasswordChanged(it)
            }
        },
        singleLine = true,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType
        ),
        textStyle = TextStyle.Default.copy(fontSize = 16.sp),
        visualTransformation = visualTransformation
    )
}