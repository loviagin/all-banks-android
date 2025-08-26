package com.lovigin.android.allbanks.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "allbanks_prefs")

object Prefs {
    private val KEY_HAS_SEEN_WELCOME = booleanPreferencesKey("hasSeenWelcome")

    fun hasSeenWelcomeFlow(context: Context) =
        context.dataStore.data.map { it[KEY_HAS_SEEN_WELCOME] ?: false }

    suspend fun setHasSeenWelcome(context: Context, value: Boolean) {
        context.dataStore.edit { it[KEY_HAS_SEEN_WELCOME] = value }
    }
}