package com.aura.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MainDispatcherRule(
    // Nous utilisons un dispatcher simple qui exécute les tâches immédiatement
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {

    // Avant chaque test, on dit aux coroutines d'utiliser notre faux dispatcher
    override fun starting(description: Description) = Dispatchers.setMain(testDispatcher)

    // Après chaque test, on le retire pour ne pas perturber les autres tests
    override fun finished(description: Description) = Dispatchers.resetMain()
}