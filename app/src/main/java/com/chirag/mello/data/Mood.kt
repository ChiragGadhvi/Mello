package com.chirag.mello.data

import com.chirag.mello.R

data class Mood(val key: String, val label: String, val drawableRes: Int)

val ALL_MOODS = listOf(
    Mood("happy",   "Happy",   R.drawable.mood_happy),
    Mood("excited", "Excited", R.drawable.mood_excited),
    Mood("angry",   "Angry",   R.drawable.mood_angry),
    Mood("neutral", "Neutral", R.drawable.mood_neutral),
    Mood("sad",     "Sad",     R.drawable.mood_sad),
    Mood("sleepy",  "Sleepy",  R.drawable.mood_sleepy)
)
