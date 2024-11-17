package ru.otus.coroutines.first

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeout

class SomePresenter {
    // SharedFlow looks more suitable for that case
    private val _result = MutableStateFlow<Result?>(null)
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    val result = _result.asStateFlow()

    /**
     * 1. Реализуйте получение данных из метода BlockingRepository#getHeavyData
     * 2. Если BlockingRepository#getHeavyData не ответит в течение 5 секунд, необходимо
     * заэмитить в LiveData/StateFlow объект Error
     * 3. При успешном сценарии пробросьте в в LiveData/StateFlow объект Success
     */
    suspend fun populateHeavyData() {
        try {
            withTimeout(TIMEOUT_IN_MS) {
                scope.async {
                    BlockingRepository().getHeavyData()
                }.await()
                _result.value = Success
            }
        } catch (e: Exception) {
            exceptionHandler(e)
        }
    }

    private fun exceptionHandler(exception: Exception) {
        when (exception) {
            is TimeoutCancellationException -> {
                _result.value = Error(exception)
            }

            else -> {
                // do nothing
            }
        }
    }

    sealed interface Result
    data object Success : Result
    data class Error(val exception: Exception) : Result

    private companion object {
        const val TIMEOUT_IN_MS = 5000L
    }
}