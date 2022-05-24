package com.github.landgrafhomyak.telegrambotapi.objects

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Poll(
    val id: String,
    val question: String,
    val options: Array<PollOption>,
    @SerialName("total_voter_count")
    val totalVoterCount: Long,
    @SerialName("is_closed")
    val isClosed: Boolean,
    @SerialName("is_anonymous")
    val isAnonymous: Boolean,
    val type: PollType,
    @SerialName("allows_multiple_answers")
    val allowsMultipleAnswers: Boolean,
    val correctOptionId: Long? = null,
    val explanation: String? = null,
    @SerialName("explanation_entities")
    val explanationEntities: Array<MessageEntity>,
    @SerialName("open_period")
    val openPeriod: Long? = null,
    @SerialName("close_date")
    val closeDate: Long? = null
)


@Serializable
enum class PollType {
    @SerialName("quiz")
    QUIZ,

    @SerialName("regular")
    REGULAR
}

@Serializable
class PollOption(
    val text: String,
    @SerialName("voter_count")
    val voterCount: Long
)

@Serializable
class PollAnswer(
    val pollId: String,
    val user: User,
    @SerialName("option_ids")
    val optionIds: Array<Long>
)