package com.github.landgrafhomyak.telegrambotapi.serialization

import java.io.File
import java.io.OutputStream
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

object Logger {
    private val out: OutputStream by lazy {
        val l = File("C:\\projets\\itmo-fb-bot\\ksp.log")
        l.createNewFile()
        l.outputStream()
    }
    private var depth: Int = 0

    private fun println(msg: String) {
        out.write("$msg\n".toByteArray(Charsets.UTF_8))
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun entry(msg: String) {
        this.println("  ".repeat(this.depth) + msg)
    }

    @OptIn(ExperimentalContracts::class)
    fun <T> scope(name: String, scope: () -> T): T {
        contract {
            callsInPlace(scope, InvocationKind.EXACTLY_ONCE)
        }

        this.entry("{ $name")
        this.depth++
        val r = scope()
        this.depth--
        this.entry("} $name")
        return r
    }
}