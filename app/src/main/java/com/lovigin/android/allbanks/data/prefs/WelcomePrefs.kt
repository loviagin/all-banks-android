package com.lovigin.android.allbanks.data.prefs

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val PREFS = "prefs"
private const val KEY_SEEN_WELCOME = "hasSeenWelcome"

fun hasSeenWelcome(context: Context): Boolean =
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getBoolean(KEY_SEEN_WELCOME, false)

// если хочешь реактивность:
val hasSeenWelcomeFlow = MutableStateFlow(false)

fun setHasSeenWelcome(context: Context, seen: Boolean) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(KEY_SEEN_WELCOME, seen)
        .apply()
    hasSeenWelcomeFlow.value = seen
}