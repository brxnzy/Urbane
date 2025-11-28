package com.example.urbane.data.local


import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")
