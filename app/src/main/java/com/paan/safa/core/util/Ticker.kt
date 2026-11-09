package com.paan.safa.core.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Emits once immediately, then runs [action] every [intervalMs] for as long as someone collects.
 * With collectAsStateWithLifecycle it stops by itself when the app goes to the background.
 */
fun ticker(intervalMs: Long, action: suspend () -> Unit): Flow<Unit> = flow {
    emit(Unit)
    while (true) {
        action()
        delay(intervalMs)
    }
}