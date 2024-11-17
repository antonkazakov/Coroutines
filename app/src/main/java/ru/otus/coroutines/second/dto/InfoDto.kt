package ru.otus.coroutines.second.dto

import kotlinx.serialization.Serializable

@Serializable
data class InfoDto(
    val fact: String,
)