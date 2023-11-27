package com.example.ultimatetrolleyfitness.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import com.example.ultimatetrolleyfitness.MainActivity
import com.example.ultimatetrolleyfitness.R
import com.example.ultimatetrolleyfitness.ui.theme.UltimateTrolleyFitnessTheme
import com.google.firebase.auth.FirebaseAuth

/**
 * The authentication activity of the app.
 */
class EmailPasswordActivity : ComponentActivity() {
    private val viewModel: EmailPasswordViewModel by viewModels {
        EmailPasswordViewModelFactory(this, application)
    }

    /**
     * Handles starting behavior when this activity is created.
     *
     * @param savedInstanceState the Bundle passed into the onCreate method, or null if none currently exists.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.auth = FirebaseAuth.getInstance()

        setContent {
            UltimateTrolleyFitnessTheme {
                LoginScreen(viewModel)
            }
        }
    }


    /**
     * Executed when the activity is started.
     */
    public override fun onStart() {
        super.onStart()

        val currentUser = viewModel.auth.currentUser
        if (currentUser != null) {
            viewModel.reload()
        }
    }

    /**
     * Function to update the UI after successful authentication.
     */
    fun updateUI() {
        // User passthrough is unneeded, main activity can access user inherently.
        val intent = Intent(this@EmailPasswordActivity, MainActivity::class.java)
        startActivity(intent)
    }
}

/**
 * Composable function representing the login screen of the app.
 *
 * @param viewModel The [EmailPasswordViewModel] used for managing authentication.
 */
@Composable
fun LoginScreen(viewModel: EmailPasswordViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ConstraintLayout {
            val (image, loginForm) = createRefs()

            val isSignInState by viewModel.isSignInState.observeAsState(true)
            val email: String by viewModel.email.observeAsState("")
            val password: String by viewModel.password.observeAsState("")
            val confirmPassword: String by viewModel.confirmPassword.observeAsState("")
            val isPasswordState by viewModel.isPasswordState.observeAsState(true)
            val isConfirmPasswordState by viewModel.isConfirmPasswordState.observeAsState(true)

            // Banner
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(top = 350.dp)
                    .constrainAs(image) {
                        top.linkTo(loginForm.top)
                        bottom.linkTo(loginForm.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }) {
                WelcomeSplash()
            }
            Card(
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                colors = CardDefaults.cardColors(),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 350.dp)
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
                        Icons.Rounded.Email,
                        null,
                        null,
                        viewModel
                    )
                    InputField(
                        password,
                        "Password",
                        KeyboardType.Password,
                        if (isPasswordState) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        Icons.Rounded.Lock,
                        isPasswordState,
                        null,
                        viewModel
                    )

                    if (!isSignInState) {
                        InputField(
                            confirmPassword,
                            "Confirm Password",
                            KeyboardType.Password,
                            if (isConfirmPasswordState) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            Icons.Rounded.Lock,
                            null,
                            isConfirmPasswordState,
                            viewModel
                        )
                    }

                    Button(
                        modifier = Modifier
                            .padding(top = 30.dp, bottom = 34.dp)
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth(),
                        onClick = {
                            if (isSignInState) {
                                viewModel.signIn(email, password)
                            } else {
                                viewModel.createAccount(email, password, confirmPassword)
                            }
                        },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                            text = if (isSignInState) "Submit" else "Register"
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                        text = if (isSignInState) "Don't have an account?" else "Returning user?"
                    )
                    OutlinedButton(
                        onClick = { viewModel.toggleAuthState() },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = if (isSignInState) "Create Account" else "Login")
                    }
                }
            }
        }
    }
}

/**
 * Composable function representing an input field for the login screen.
 *
 * @param value The value of the input field.
 * @param label The label for the input field.
 * @param keyboardType The keyboard type for the input field.
 * @param visualTransformation The visual transformation for the input field.
 * @param icon The icon associated with the input field.
 * @param isPasswordState The state of the password field.
 * @param isConfirmPasswordState The state of the confirm password field.
 * @param viewModel The [EmailPasswordViewModel] used for managing authentication.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun InputField(
    value: String,
    label: String,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation,
    icon: ImageVector,
    isPasswordState: Boolean?,
    isConfirmPasswordState: Boolean?,
    viewModel: EmailPasswordViewModel
) {
    val isPassword = label.contains("Password")
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
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
        ),
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = {
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    icon,
                    "$label icon",
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .size(18.dp)
                )

                // Line between Icon and field
                Canvas(
                    modifier = Modifier.height(24.dp)
                ) {
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 2.0F
                    )
                }
            }
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = {
                    if (label.contains("Confirm")) {
                        viewModel.toggleConfirmPasswordState()
                    } else {
                        viewModel.togglePasswordState()
                    }
                }) {
                    Icon(
                        imageVector = if ((label.contains("Confirm") && isConfirmPasswordState == true) || isPasswordState == true) {
                            Icons.Default.Visibility
                        } else {
                            Icons.Default.VisibilityOff
                        },
                        contentDescription = if ((label.contains("Confirm") && isConfirmPasswordState == true) || isPasswordState == true) {
                            "Visibility Icon"
                        } else {
                            "Visibility Off Icon"
                        }
                    )
                }
            }
        }
    )
}

/**
 * Composable function displaying a welcome splash on the login screen.
 */
@Composable
fun WelcomeSplash() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = ImageBitmap.imageResource(id = R.drawable.ultimatetrolleyfitnesslogo),
            contentDescription = "Ultimate Trolley Fitness Logo"
        )
        Text(
            text = "Ultimate Trolley",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Fitness",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )
    }
}