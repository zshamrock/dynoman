package com.akazlou.dynoman.function

abstract class Function<T> {
    fun parse(text: String): T {
        return run(*parseArgs(text))
    }

    private fun parseArgs(text: String): Array<String> {
        val symbols = text.trim().drop(name().length).trim('(', ')')
        val tokens = mutableListOf<Char>()
        val args = mutableListOf<String>()
        var arg = ""
        symbols.forEach { ch ->
            val token = if (tokens.isEmpty()) null else tokens[tokens.size - 1]
            when (ch) {
                '\'', '"' -> {
                    if (ch == token) {
                        arg += ch
                    }
                }
                '(' -> {

                }
                ')' -> {

                }
                ',' -> {

                }
                else -> arg += ch
            }
        }
        return emptyArray()
    }

    abstract fun name(): String
    protected abstract fun run(vararg args: String): T
}