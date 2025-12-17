package cl.duoc.visso.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import cl.duoc.visso.data.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constants.DATASTORE_NAME
)

class SessionManager(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val USER_ID = longPreferencesKey(Constants.KEY_USER_ID)
        private val USER_EMAIL = stringPreferencesKey(Constants.KEY_USER_EMAIL)
        private val USER_NAME = stringPreferencesKey(Constants.KEY_USER_NAME)
        private val USER_ROLE = stringPreferencesKey(Constants.KEY_USER_ROLE)
        private val IS_LOGGED_IN = booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)
    }

    suspend fun saveUserSession(usuario: Usuario) {
        dataStore.edit { prefs ->
            prefs[USER_ID] = usuario.id ?: 0L
            prefs[USER_EMAIL] = usuario.email
            prefs[USER_NAME] = "${usuario.nombre} ${usuario.apellido}"
            prefs[USER_ROLE] = usuario.rol ?: "USER"
            prefs[IS_LOGGED_IN] = true
        }
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN] ?: false
    }

    val userId: Flow<Long> = dataStore.data.map { prefs ->
        prefs[USER_ID] ?: 0L
    }

    val userName: Flow<String> = dataStore.data.map { prefs ->
        prefs[USER_NAME] ?: ""
    }

    val userEmail: Flow<String> = dataStore.data.map { prefs ->
        prefs[USER_EMAIL] ?: ""
    }

    val userRole: Flow<String> = dataStore.data.map { prefs ->
        prefs[USER_ROLE] ?: "USER"
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}