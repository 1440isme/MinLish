package com.minlish.core.network.dto

import com.google.gson.annotations.SerializedName

data class ApiErrorDto(
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("existingItems") val existingItems: List<ExistingVocabularyItemDto>? = null,
)
