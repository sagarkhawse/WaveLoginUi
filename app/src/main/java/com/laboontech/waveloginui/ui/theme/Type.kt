package com.laboontech.waveloginui.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.laboontech.waveloginui.R

private val futura = FontFamily(
    Font(R.font.futurabook),
    Font(R.font.futuramedium, FontWeight.W500),
    Font(R.font.futurabold, FontWeight.Bold))


val typography = Typography(defaultFontFamily = futura)