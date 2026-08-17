package com.maimonthlyhoppinings.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class PersonaManager(
    private val context: Context,
    private val store: PersonaPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutex = Mutex()
    private val session = MutableStateFlow<OpenPersona?>(null)

    val catalog: StateFlow<PersonaCatalog> = store.catalog.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = PersonaCatalog(personas = listOf(Persona.default()), activeId = Persona.DEFAULT_ID),
    )

    val personas: StateFlow<List<Persona>> = catalog
        .map { it.personas }
        .stateIn(scope, SharingStarted.Eagerly, catalog.value.personas)

    val activePersona: StateFlow<Persona> = catalog
        .map { it.active }
        .stateIn(scope, SharingStarted.Eagerly, catalog.value.active)

    val database: AppDatabase
        get() = session.value?.database ?: error("Personas have not been started")

    val databaseFlow = session
        .map { it?.database }
        .filterNotNull()
        .distinctUntilChanged()

    suspend fun start() {
        mutex.withLock {
            store.ensureDefaultPersona()
            openLocked(store.snapshot().active)
        }
    }

    suspend fun switchTo(id: String) {
        mutex.withLock {
            val persona = store.snapshot().persona(id) ?: return
            if (session.value?.persona?.id == id) return
            openLocked(persona)
            store.setActive(id)
        }
    }

    suspend fun create(name: String): Persona {
        return mutex.withLock {
            val catalog = store.snapshot()
            val resolvedName = Persona.sanitizeName(
                name,
                fallback = nextUntitledName(catalog.personas),
            )
            val id = UUID.randomUUID().toString()
            val persona = Persona(
                id = id,
                name = resolvedName,
                databaseName = "mai_book_${id.replace("-", "")}.db",
                createdAtMillis = System.currentTimeMillis(),
            )
            store.upsert(persona)
            openLocked(persona)
            store.setActive(persona.id)
            persona
        }
    }

    suspend fun rename(id: String, name: String) {
        mutex.withLock {
            val existing = store.snapshot().persona(id) ?: return
            val resolved = Persona.sanitizeName(name, fallback = existing.name)
            store.upsert(existing.copy(name = resolved))
        }
    }

    suspend fun delete(id: String) {
        mutex.withLock {
            val catalog = store.snapshot()
            if (catalog.personas.size <= 1) return
            val doomed = catalog.persona(id) ?: return
            val remaining = catalog.personas.filterNot { it.id == id }
            val nextActive = if (catalog.activeId == id) remaining.first() else catalog.active
            if (session.value?.persona?.id == id) {
                openLocked(nextActive)
            }
            store.remove(id)
            store.setActive(nextActive.id)
            AppDatabase.release(doomed.databaseName)
            context.deleteDatabase(doomed.databaseName)
        }
    }

    private fun openLocked(persona: Persona) {
        val previous = session.value
        val database = AppDatabase.open(context, persona.databaseName)
        session.value = OpenPersona(persona, database)
        if (previous != null && previous.persona.databaseName != persona.databaseName) {
            AppDatabase.release(previous.persona.databaseName)
        }
    }

    private fun nextUntitledName(existing: List<Persona>): String {
        val used = existing.map { it.name }.toSet()
        var index = existing.size + 1
        while (used.contains("Persona $index")) {
            index += 1
        }
        return "Persona $index"
    }

    private data class OpenPersona(
        val persona: Persona,
        val database: AppDatabase,
    )
}
