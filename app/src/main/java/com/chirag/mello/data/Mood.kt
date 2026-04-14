package com.chirag.mello.data

import androidx.compose.ui.graphics.Color
import com.chirag.mello.R

data class Mood(val key: String, val label: String, val drawableRes: Int, val color: Color)

val ALL_MOODS = listOf(
    Mood("happy",   "Happy",   R.drawable.mood_happy,   Color(0xFFFFD166)),
    Mood("excited", "Excited", R.drawable.mood_excited, Color(0xFFCDB4DB)),
    Mood("angry",   "Angry",   R.drawable.mood_angry,   Color(0xFFFF6B6B)),
    Mood("neutral", "Neutral", R.drawable.mood_neutral, Color(0xFFA8DADC)),
    Mood("sad",     "Sad",     R.drawable.mood_sad,     Color(0xFF74B9FF)),
    Mood("sleepy",  "Sleepy",  R.drawable.mood_sleepy,  Color(0xFFB2B5E0))
)
