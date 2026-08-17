package com.maimonthlyhoppinings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/** Same on-disk store as the old book catalog so existing installs keep their journals. */
private val Context.bookDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "book_preferences",
)

class PersonaPreferences(
    private val context: Context,
) {
    private val booksJsonKey = stringPreferencesKey("books_json")
    private val activeIdKey = stringPreferencesKey("active_book_id")

    val catalog: Flow<PersonaCatalog> = context.bookDataStore.data.map { prefs ->
        parse(prefs[booksJsonKey], prefs[activeIdKey])
    }

    suspend fun snapshot(): PersonaCatalog = catalog.first()

    suspend fun ensureDefaultPersona() {
        context.bookDataStore.edit { prefs ->
            val current = parse(prefs[booksJsonKey], prefs[activeIdKey])
            if (current.personas.isEmpty()) {
                val default = Persona.default()
                prefs[booksJsonKey] = encode(listOf(default))
                prefs[activeIdKey] = default.id
                return@edit
            }
            if (current.personas.none { it.id == current.activeId }) {
                prefs[activeIdKey] = current.personas.first().id
            }
        }
    }

    suspend fun setActive(id: String) {
        context.bookDataStore.edit { prefs ->
            val current = parse(prefs[booksJsonKey], prefs[activeIdKey])
            if (current.persona(id) != null) {
                prefs[activeIdKey] = id
            }
        }
    }

    suspend fun upsert(persona: Persona) {
        context.bookDataStore.edit { prefs ->
            val current = parse(prefs[booksJsonKey], prefs[activeIdKey])
            val next = current.personas.filterNot { it.id == persona.id } + persona
            prefs[booksJsonKey] = encode(next.sortedBy { it.createdAtMillis })
            if (current.personas.none { it.id == current.activeId }) {
                prefs[activeIdKey] = persona.id
            }
        }
    }

    suspend fun remove(id: String) {
        context.bookDataStore.edit { prefs ->
            val current = parse(prefs[booksJsonKey], prefs[activeIdKey])
            val next = current.personas.filterNot { it.id == id }
            if (next.isEmpty()) return@edit
            prefs[booksJsonKey] = encode(next)
            if (current.activeId == id) {
                prefs[activeIdKey] = next.first().id
            }
        }
    }

    private fun parse(json: String?, activeId: String?): PersonaCatalog {
        val personas = decode(json)
        val resolvedActive = when {
            activeId != null && personas.any { it.id == activeId } -> activeId
            personas.isNotEmpty() -> personas.first().id
            else -> Persona.DEFAULT_ID
        }
        return PersonaCatalog(personas = personas, activeId = resolvedActive)
    }

    private fun encode(personas: List<Persona>): String {
        val array = JSONArray()
        personas.forEach { persona ->
            array.put(
                JSONObject()
                    .put("id", persona.id)
                    .put("name", persona.name)
                    .put("databaseName", persona.databaseName)
                    .put("createdAtMillis", persona.createdAtMillis),
            )
        }
        return array.toString()
    }

    private fun decode(json: String?): List<Persona> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val obj = array.getJSONObject(index)
                    add(
                        Persona(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            databaseName = obj.getString("databaseName"),
                            createdAtMillis = obj.optLong("createdAtMillis"),
                        ),
                    )
                }
            }
        }.getOrElse { emptyList() }
    }
}
