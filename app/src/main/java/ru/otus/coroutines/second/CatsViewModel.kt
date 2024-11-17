package ru.otus.coroutines.second

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.otus.coroutines.first.SomePresenter
import ru.otus.coroutines.second.data.CatsData
import ru.otus.coroutines.second.dto.InfoDto
import ru.otus.coroutines.second.dto.SearchDto

internal class CatsViewModel : ViewModel() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _result = MutableStateFlow<Result<CatsData>?>(null)


    val result = _result.asStateFlow()

    /**
     * Реализуйте функцию populateCats следующим образом:
     * 1. Реализуйте 2 параллельных http запроса на ендпоинты https://api.thecatapi.com/v1/images/search и https://catfact.ninja/fact
     * 2. Обработайте возможные исключения:
     *    а) если выпал SocketTimeoutException - повторите запрос, количество попыток = 3,покажите Toast с текстом "Something went wrong"
     *    b) если выпало другое исключение - покажите Toast с текстом "Something went wrong"
     * 3. Успешный результат необходим прокинуь дальше через LiveData/StateFlow и отрендерить полученные данные
     */
    suspend fun populateCats() {
        withContext(Dispatchers.IO) {
            val httpClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    })
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
            }
            try {
                var searchResult: List<SearchDto>? = null//Result<HttpResponse>? = null
                var factResult: InfoDto? = null//Result<HttpResponse>? = null
                scope.async {
                    searchResult = makeRequest(httpClient, SEARCH_URL)?.body()
                    factResult = makeRequest(httpClient, FACT_URL)?.body()
                }.await()
                safeLet(searchResult, factResult) { search, fact ->
                    _result.value = Result.success(
                        CatsData(
                            searchResult = search.map { it.searchModelMapper() },
                            fact = fact.fact,
                        )
                    )
                }
            } catch (e: CancellationException) {
                println("Canceled")
            } catch (e: Exception) {
                _result.value = Result.failure(e)
            } finally {
                httpClient.close()
            }
        }
    }

    private inline fun <S, R> S.runOperationCatching(block: S.() -> R):/* Result<R>*/ R? {
        return try {
            block()
        } catch (e: SocketTimeoutException) {
            null
        }
    }

    private inline fun <T1: Any, T2: Any, R: Any> safeLet(p1: T1?, p2: T2?, block: (T1, T2)->R?): R? {
        return if (p1 != null && p2 != null) block(p1, p2) else null
    }

    private suspend fun makeRequest(httpClient: HttpClient, url: String): HttpResponse? {
        var result: HttpResponse?
        var count = 0
        do {
            currentCoroutineContext().ensureActive()
            count++
            result = runOperationCatching {
                httpClient.get(url)
            }
            println("makeRequest $url $count")
        } while (count < 3 && result/*.isFailure*/ == null)

        return result
    }

    private companion object {
        const val SEARCH_URL = "https://api.thecatapi.com/v1/images/search"
        const val FACT_URL = "https://catfact.ninja/fact"
    }
}


