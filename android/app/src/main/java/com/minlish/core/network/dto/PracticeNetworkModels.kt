package com.minlish.core.network.dto

import com.google.gson.annotations.SerializedName

data class CreatePracticeSessionRequest(
    @SerializedName("deckId") val deckId: String,
    @SerializedName("practiceTypes") val practiceTypes: List<String>?,
    @SerializedName("totalQuestions") val totalQuestions: Int?,
    @SerializedName("scope") val scope: String? = "LEARNED_ONLY"
)

data class SubmitAnswerRequest(
    @SerializedName("questionIndex") val questionIndex: Int,
    @SerializedName("userAnswer") val userAnswer: String?
)

data class PracticeSessionDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("deckId") val deckId: String?,
    @SerializedName("deckName") val deckName: String? = null,
    @SerializedName("practiceType") val practiceType: String,
    @SerializedName("totalQuestions") val totalQuestions: Int,
    @SerializedName("correctAnswers") val correctAnswers: Int,
    @SerializedName("wrongAnswers") val wrongAnswers: Int,
    @SerializedName("accuracy") val accuracy: Float,
    @SerializedName("status") val status: String,
    @SerializedName("startedAt") val startedAt: String,
    @SerializedName("finishedAt") val finishedAt: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

data class PracticeQuestionDto(
    @SerializedName("index") val index: Int,
    @SerializedName("questionType") val questionType: String,
    @SerializedName("questionText") val questionText: String,
    @SerializedName("options") val options: List<String>?,
    @SerializedName("vocabularyId") val vocabularyId: String?,
    @SerializedName("answered") val answered: Boolean
)

data class PracticeAnswerDto(
    @SerializedName("id") val id: String,
    @SerializedName("sessionId") val sessionId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("vocabularyId") val vocabularyId: String?,
    @SerializedName("questionType") val questionType: String,
    @SerializedName("questionText") val questionText: String,
    @SerializedName("optionsJson") val optionsJson: List<String>?,
    @SerializedName("userAnswer") val userAnswer: String?,
    @SerializedName("correctAnswer") val correctAnswer: String,
    @SerializedName("isCorrect") val isCorrect: Boolean,
    @SerializedName("answeredAt") val answeredAt: String,
    @SerializedName("createdAt") val createdAt: String
)

data class PracticeSessionSummaryDto(
    @SerializedName("totalQuestions") val totalQuestions: Int,
    @SerializedName("correctAnswers") val correctAnswers: Int,
    @SerializedName("wrongAnswers") val wrongAnswers: Int,
    @SerializedName("unanswered") val unanswered: Int,
    @SerializedName("accuracy") val accuracy: Float,
    @SerializedName("timeTakenSeconds") val timeTakenSeconds: Int
)

data class CreateSessionResponse(
    @SerializedName("session") val session: PracticeSessionDto,
    @SerializedName("questions") val questions: List<PracticeQuestionDto>
)

data class FinishSessionResponse(
    @SerializedName("session") val session: PracticeSessionDto,
    @SerializedName("answers") val answers: List<PracticeAnswerDto>,
    @SerializedName("summary") val summary: PracticeSessionSummaryDto
)
