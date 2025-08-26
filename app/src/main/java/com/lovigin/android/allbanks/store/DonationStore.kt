package com.lovigin.android.allbanks.store

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// File-level DataStore delegate bound to Context
private val Context.donationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "donation_prefs"
)

/**
 * Хранилище флага "donation allowed" на базе Preferences DataStore.
 * Это КЛАСС (не интерфейс), чтобы Hilt мог его провайдить.
 */
class DonationStore(private val context: Context) {

    private object Keys {
        val DONATION_ALLOWED = booleanPreferencesKey("donation_allowed")
    }

    /** Поток с текущим значением флага (по умолчанию false). */
    val isAllowed: Flow<Boolean> = context.donationDataStore.data.map { prefs ->
        prefs[Keys.DONATION_ALLOWED] ?: false
    }

    /** Сохранить флаг. */
    suspend fun setAllowed(allowed: Boolean) {
        context.donationDataStore.edit { prefs ->
            prefs[Keys.DONATION_ALLOWED] = allowed
        }
    }
}