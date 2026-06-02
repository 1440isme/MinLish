package com.minlish.core.network

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.minlish.core.network.dto.ApiErrorDto
import com.minlish.core.network.dto.ExistingVocabularyItemDto
import retrofit2.HttpException

object ApiErrorParser {

    private val gson = Gson()

    fun parse(exception: Throwable): ApiErrorDto? {
        if (exception !is HttpException) return null
        val body = exception.response()?.errorBody()?.string() ?: return null
        return parseBody(body)
    }

    fun parseBody(body: String): ApiErrorDto? {
        return try {
            val json = gson.fromJson(body, JsonObject::class.java)
            if (json.has("code") || json.has("existingItems")) {
                return parseJsonObject(json)
            }
            val messageEl = json.get("message") ?: return gson.fromJson(body, ApiErrorDto::class.java)
            when {
                messageEl.isJsonObject -> parseJsonObject(messageEl.asJsonObject)
                messageEl.isJsonPrimitive -> ApiErrorDto(message = messageEl.asString)
                else -> null
            }
        } catch (_: Exception) {
            try {
                gson.fromJson(body, ApiErrorDto::class.java)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun parseJsonObject(obj: JsonObject): ApiErrorDto {
        val code = obj.get("code")?.takeIf { !it.isJsonNull }?.asString
        val message = obj.get("message")?.takeIf { !it.isJsonNull }?.asString
        val existingItems = obj.get("existingItems")?.takeIf { it.isJsonArray }?.asJsonArray?.map { el ->
            val item = el.asJsonObject
            ExistingVocabularyItemDto(
                id = item.get("id").asString,
                word = item.get("word").asString,
                meaning = item.get("meaning").asString,
            )
        }
        return ApiErrorDto(code = code, message = message, existingItems = existingItems)
    }

    fun humanMessage(exception: Throwable, fallback: String = "Something went wrong"): String {
        return parse(exception)?.message ?: exception.message ?: fallback
    }
}
