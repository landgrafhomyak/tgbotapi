@file:OptIn(ExperimentalContracts::class)

package com.github.landgrafhomyak.tgbotapi

import com.github.landgrafhomyak.tgbotapi.objects.InlineKeyboardButton
import com.github.landgrafhomyak.tgbotapi.objects.InlineKeyboardMarkup
import com.github.landgrafhomyak.tgbotapi.objects.LoginUrl
import com.github.landgrafhomyak.tgbotapi.objects.PollType
import com.github.landgrafhomyak.tgbotapi.objects.ReplyKeyboardButton
import com.github.landgrafhomyak.tgbotapi.objects.ReplyKeyboardMarkup
import com.github.landgrafhomyak.tgbotapi.objects.WebAppInfo
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

@JvmInline
value class ReplyKeyboardBuilder(val rows: MutableList<Array<ReplyKeyboardButton>>) {
    inline fun row(builder: ReplyKeyboardRowBuilder.() -> Unit) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        val buttons = mutableListOf<ReplyKeyboardButton>()
        ReplyKeyboardRowBuilder(buttons).builder()
        this.rows.add(buttons.toTypedArray())
    }
}

@JvmInline
value class ReplyKeyboardRowBuilder(val rows: MutableList<ReplyKeyboardButton>) {
    inline fun text(text: String) {
        this.rows.add(ReplyKeyboardButton.Text(text))
    }

    inline fun contact(text: String) {
        this.rows.add(ReplyKeyboardButton.ContactRequest(text, true))
    }

    @Suppress("PropertyName")
    val QUIZ
        inline get() = PollType.QUIZ

    @Suppress("PropertyName")
    val REGULAR
        inline get() = PollType.REGULAR

    inline fun poll(text: String, type: PollType? = null) {
        this.rows.add(ReplyKeyboardButton.PollRequest(text, type))
    }

    inline fun webApp(text: String, app: WebAppInfo) {
        this.rows.add(ReplyKeyboardButton.WebApp(text, app))
    }
}

inline fun replyKeyboard(
    resizeKeyboard: Boolean? = null,
    oneTimeKeyboard: Boolean? = null,
    inputFieldPlaceholder: String? = null,
    selective: Boolean? = null,
    builder: ReplyKeyboardBuilder.() -> Unit
): ReplyKeyboardMarkup {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    val rows = mutableListOf<Array<ReplyKeyboardButton>>()
    ReplyKeyboardBuilder(rows).builder()
    return ReplyKeyboardMarkup(
        keyboard = rows.toTypedArray(),
        resizeKeyboard = resizeKeyboard,
        oneTimeKeyboard = oneTimeKeyboard,
        inputFieldPlaceholder = inputFieldPlaceholder,
        selective = selective
    )
}

@JvmInline
value class InlineKeyboardBuilder(val rows: MutableList<Array<InlineKeyboardButton>>) {
    inline fun row(builder: InlineKeyboardRowBuilder.() -> Unit) {
        contract {
            callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
        }

        val buttons = mutableListOf<InlineKeyboardButton>()
        InlineKeyboardRowBuilder(buttons).builder()
        this.rows.add(buttons.toTypedArray())
    }
}

@JvmInline
value class InlineKeyboardRowBuilder(val rows: MutableList<InlineKeyboardButton>) {
    inline fun url(text: String, url: String) {
        this.rows.add(InlineKeyboardButton.Url(text, url))
    }

    inline fun callback(text: String, data: String) {
        this.rows.add(InlineKeyboardButton.CallbackData(text, data))
    }

    inline fun webApp(text: String, webApp: WebAppInfo) {
        this.rows.add(InlineKeyboardButton.WebApp(text, webApp))
    }

    inline fun login(text: String, url: LoginUrl) {
        this.rows.add(InlineKeyboardButton.LoginUrl(text, url))
    }

    inline fun inlineQuery(text: String, query: String) {
        this.rows.add(InlineKeyboardButton.SwitchInlineQuery(text, query))
    }

    inline fun game(text: String, game: String) {
        this.rows.add(InlineKeyboardButton.CallbackGame(text, game))
    }

    inline fun pay(text: String) {
        this.rows.add(InlineKeyboardButton.Pay(text, true))
    }
}

inline fun inlineKeyboard(
    builder: InlineKeyboardBuilder.() -> Unit
): InlineKeyboardMarkup {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    val rows = mutableListOf<Array<InlineKeyboardButton>>()
    InlineKeyboardBuilder(rows).builder()
    return InlineKeyboardMarkup(
        inlineKeyboard = rows.toTypedArray(),
    )
}