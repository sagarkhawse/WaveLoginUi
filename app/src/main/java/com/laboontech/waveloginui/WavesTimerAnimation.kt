package com.laboontech.waveloginui

import android.view.ViewTreeObserver
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.laboontech.waveloginui.ui.theme.DarkBlue
import com.laboontech.waveloginui.ui.theme.DarkLightBlue
import com.laboontech.waveloginui.ui.theme.Gray
import com.laboontech.waveloginui.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun keyboardAsState(): State<Boolean> {
    val view = LocalView.current
    var isImeVisible by remember { mutableStateOf(false) }

    DisposableEffect(LocalWindowInfo.current) {
        val listener = ViewTreeObserver.OnPreDrawListener {
            isImeVisible = ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            true
        }
        view.viewTreeObserver.addOnPreDrawListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnPreDrawListener(listener)
        }
    }
    return rememberUpdatedState(isImeVisible)
}

private enum class TimerState {
    Started,
    Stopped,
    Paused,
}

private enum class LoginState {
    Success,
    Failed,
    Idle,
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun WavesTimerAnimation() {
    val defaultSpacer = 14.dp
    var valueEmail by remember {
        mutableStateOf("")
    }
    var valuePassword by remember {
        mutableStateOf("")
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val bringIntoViewRequester = remember {
        BringIntoViewRequester()
    }
    val isKeyboardVisible by keyboardAsState()

    var timerDurationInMillis by rememberSaveable { mutableStateOf(3000) }
    var timerState by remember { mutableStateOf(TimerState.Stopped) }

    val timerProgress by timerProgressAsState(
        timerState = timerState,
        timerDurationInMillis = timerDurationInMillis,
    )

    var loginState by remember {
        mutableStateOf(LoginState.Idle)
    }

    LaunchedEffect(timerProgress == 0f) {
        if (timerProgress == 0f) {
            timerState = TimerState.Stopped
        }
    }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Box(
            modifier = Modifier
                .weight(1f, fill = true),
        ) {
            WavesLoadingIndicator(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.primary,
                progress = if (timerState == TimerState.Started) timerProgress else if (isKeyboardVisible) 0.90f else if (timerState==TimerState.Stopped && loginState==LoginState.Success) 0.30f else 0.70f,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(20.dp)
                    .align(Alignment.BottomCenter),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(visible = !isKeyboardVisible) {
                    val string =
                        if (loginState == LoginState.Failed) "Login Failed" else if (loginState == LoginState.Success) "Login Success" else "Login"
                    Text(
                        text = string,
                        fontSize = 60.sp,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }

                Spacer(modifier = Modifier.height(96.dp))

                AnimatedVisibility(visible = timerState == TimerState.Started) {
                    Text(
                        text = "Checking Credential Please wait...",
                        fontSize = 20.sp,
                        color = White,
                    )
                }

                AnimatedVisibility(visible = timerState == TimerState.Stopped && loginState == LoginState.Success) {
                    Text(
                        text = "Go to Dashboard",
                        fontSize = 20.sp,
                        color = White,
                    )
                }


                // Text fields with visibility animations
                AnimatedVisibility(visible = if (loginState == LoginState.Success) false else timerState == TimerState.Stopped) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.welcome_back),
                            fontSize = 35.sp,
                            color = White,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start,
                        )
                        Spacer(modifier = Modifier.height(defaultSpacer))
                        Text(
                            text = stringResource(id = R.string.welcome_back_descsription),
                            fontSize = 18.sp,
                            color = Gray,
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(30.dp),
                            backgroundColor = DarkLightBlue,
                        ) {
                            TextField(
                                value = valueEmail,
                                onValueChange = {
                                    if (it.all { char ->
                                            !char.isWhitespace()
                                        }
                                    ) {
                                        valueEmail = it
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Email,
                                        contentDescription = stringResource(id = R.string.type_your_email),
                                        tint = Gray,
                                    )
                                },
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.type_your_email),
                                        color = Gray,
                                        fontSize = 16.sp,
                                    )
                                },
                                modifier = Modifier
                                    .padding(6.dp)
                                    .bringIntoViewRequester(bringIntoViewRequester),
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = White,
                                    backgroundColor = DarkLightBlue,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Next,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                }),
                                shape = RoundedCornerShape(30.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(defaultSpacer))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(30.dp),
                            backgroundColor = DarkLightBlue,
                        ) {
                            TextField(
                                value = valuePassword,
                                onValueChange = {
                                    if (it.all { char ->
                                            !char.isWhitespace()
                                        }
                                    ) {
                                        valuePassword = it
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = stringResource(id = R.string.type_password),
                                        tint = Gray,
                                    )
                                },
                                placeholder = {
                                    Text(
                                        text = stringResource(id = R.string.type_password),
                                        color = Gray,
                                        fontSize = 16.sp,
                                    )
                                },
                                modifier = Modifier.padding(6.dp),
                                colors = TextFieldDefaults.textFieldColors(
                                    textColor = White,
                                    backgroundColor = DarkLightBlue,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                ),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    imeAction = ImeAction.Done,
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    keyboardController?.hide()
                                }),
                                visualTransformation = PasswordVisualTransformation(),
                                shape = RoundedCornerShape(30.dp),
                            )
                        }

                        Text(
                            text = stringResource(id = R.string.forget_password),
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 2.dp),
                            color = Gray,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))


                // Login Button
                var loading by remember { mutableStateOf(false) }
                if (timerState == TimerState.Stopped) loading = false
                LoginButton(
                    onClick = {
                        if (loginState==LoginState.Success) return@LoginButton
                        loading = !loading
                        timerState = TimerState.Started
                        coroutineScope.launch {
                            delay(2000)
                            loginState =
                                if (valueEmail.isEmpty() || valuePassword.isEmpty()) LoginState.Failed else LoginState.Success
                        }
                    },
                    loading = loading,
                    text = if (loginState == LoginState.Success) "Done" else "Login",
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Signup Button
                AnimatedVisibility(visible = if (loginState == LoginState.Success) false else timerState == TimerState.Stopped) {
                    Button(
                        onClick = { /*TODO*/ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MaterialTheme.colors.primary,
                        ),
                        border = BorderStroke(width = 1.dp, color = White),
                    ) {
                        Text(
                            text = stringResource(id = R.string.sign_up),
                            fontSize = 20.sp,
                            color = White,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}


/*
*  Login button and Loading animation code
* */
@Composable
fun LoginButton(
    onClick: () -> Unit,
    loading: Boolean,
    text: String,
) {
    val transition = updateTransition(
        targetState = loading,
        label = "master transition",
    )
    val horizontalContentPadding by transition.animateDp(
        transitionSpec = {
            spring(
                stiffness = Spring.StiffnessMediumLow,
            )
        },
        targetValueByState = { toLoading -> if (toLoading) 12.dp else 24.dp },
        label = "button's content padding",
    )

    Button(
        onClick = { onClick() },
        shape = RoundedCornerShape(30.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = White,
        ),
        modifier = Modifier.height(60.dp),
        contentPadding = PaddingValues(
            horizontal = horizontalContentPadding,
            vertical = 8.dp,
        ),
    ) {
        Box(contentAlignment = Alignment.Center) {
            LoadingContent(
                loadingStateTransition = transition,
            )
            PrimaryContent(
                text = text,
                loadingStateTransition = transition,
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun LoadingContent(
    loadingStateTransition: Transition<Boolean>,
) {
    loadingStateTransition.AnimatedVisibility(
        visible = { loading -> loading },
        enter = fadeIn(),
        exit = fadeOut(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = 0.10f,
            ),
        ),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = DarkBlue,
            strokeWidth = 1.5f.dp,
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun PrimaryContent(
    text: String,
    loadingStateTransition: Transition<Boolean>,
) {
    loadingStateTransition.AnimatedVisibility(
        visible = { loading -> !loading },
        enter = fadeIn() + expandHorizontally(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                dampingRatio = Spring.DampingRatioMediumBouncy,
                visibilityThreshold = IntSize.VisibilityThreshold,
            ),
            expandFrom = Alignment.CenterHorizontally,
        ),
        exit = fadeOut(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = 0.10f,
            ),
        ) + shrinkHorizontally(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                // dampingRatio is not applicable here, size cannot become negative
                visibilityThreshold = IntSize.VisibilityThreshold,
            ),
            shrinkTowards = Alignment.CenterHorizontally,
        ),
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            color = DarkBlue,
            modifier = Modifier.padding(horizontal = 130.dp, vertical = 8.dp),
        )
    }
}




/*
*
* Code for wave timer
* */
@Composable
private fun timerProgressAsState(
    timerState: TimerState,
    timerDurationInMillis: Int,
): State<Float> {
    val animatable = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(timerState) {
        val animateToStartOrStopState = timerState == TimerState.Stopped ||
            (timerState == TimerState.Started && animatable.value == 0f)

        if (animateToStartOrStopState) {
            animatable.animateTo(
                targetValue = if (timerState == TimerState.Started) 1f else 0f,
                animationSpec = spring(stiffness = 100f),
            )
        }

        if (timerState == TimerState.Started) {
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = timerDurationInMillis,
                    easing = LinearEasing,
                ),
            )
        }
    }

    return remember(animatable) {
        derivedStateOf { animatable.value }
    }
}
