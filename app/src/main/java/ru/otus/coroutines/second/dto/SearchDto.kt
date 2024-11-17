package ru.otus.coroutines.second.dto

import kotlinx.serialization.Serializable
import ru.otus.coroutines.second.data.SearchModel

@Serializable
data class SearchDto (
    val id: String,
    val url: String,
    val width: Int,
    val height: Int,
) {
    fun searchModelMapper() = SearchModel(
        url = url,
        width = width,
        height = height,
    )
}