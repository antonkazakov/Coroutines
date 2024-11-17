package ru.otus.coroutines.first

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        val job = Job()
        val scope = CoroutineScope(Dispatchers.IO + job)
        scope.launch {
            with (SomePresenter()) {
                populateHeavyData()
                println("Result is ${result.value}")
            }
        }
        job.join()
    }
}
