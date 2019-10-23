package com.akazlou.dynoman.function

import java.util.Deque
import java.util.LinkedList

abstract class Function<T> {
    fun parse(text: String): T {
        return run(*parseArgs(text))
    }

    private fun parseArgs(text: String): Array<Any> {
        val symbols = text.trim().drop(name().length).trim('(', ')')
        val tokens: Deque<Char> = LinkedList()
        val args = mutableListOf<Any>()
        var arg = ""
        symbols.forEach { ch ->
            val token = tokens.peek()
            val isInQuote = token == '\'' || token == '"'
            if (isInQuote) {
                if (ch == token) {
                    tokens.pop()
                }
                arg += ch
                return@forEach
            }
            when (ch) {
                '\'', '"', '(' -> {
                    tokens.push(ch)
                    if (ch == '\'' || ch == '"') {
                        arg += ch
                    }
                }
                ')' -> {
                    if (token != '(') {
                        throw IllegalArgumentException("Can't parse the function statement '$text'. "
                                + "Please, check it is syntactically correct.")
                    }
                    tokens.pop()
                }
                ',' -> {
                    args += cast(arg.trim())
                    arg = ""
                }
                else -> arg += ch
            }
        }
        if (arg.isNotEmpty()) {
            args += cast(arg.trim())
        }
        if (tokens.isNotEmpty()) {
            throw IllegalArgumentException("Can't parse the function statement '$text'. "
                    + "Please, check it is syntactically correct.")
        }
        return args.toTypedArray()
    }

    private fun cast(arg: String): Any {
        return if (arg.startsWith('\'') || arg.startsWith('"')) {
            // String type
            arg.trim('\'', '"')
        } else if (arg == "true" || arg == "false") {
            // Boolean type
            arg.toBoolean()
        } else {
            // Double type
            arg.toDouble()
        }
    }

    abstract fun name(): String
    protected open fun desc(): String = ""
    abstract fun run(vararg args: Any): T
}