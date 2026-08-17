package com.maimonthlyhoppinings

import com.maimonthlyhoppinings.data.DebugSampleDataSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Debug process only. Release keeps [MaiMonthlyHoppiningsApp] with no sample data. */
class DebugMaiMonthlyHoppiningsApp : MaiMonthlyHoppiningsApp() {
    private val debugScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        debugScope.launch {
            DebugSampleDataSeeder.seedOnce(this@DebugMaiMonthlyHoppiningsApp, eventRepository)
        }
    }
}
