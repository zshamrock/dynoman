package com.akazlou.dynoman.domain

import java.util.EnumSet

data class ForeignSearchName(val table: String, val name: String, val flags: EnumSet<Flag>) {
    companion object {
        private const val SEPARATOR = "@"
        private const val NAME_FLAGS_SEPARATOR = "/"
        @JvmField
        val EMPTY_FLAGS: EnumSet<Flag> = EnumSet.noneOf(Flag::class.java)
        @JvmField
        val HIDDEN_FLAGS: EnumSet<Flag> = EnumSet.of(Flag.ENVIRONMENT_STRIPPED)


        fun of(fullName: String): ForeignSearchName {
            val parts = fullName.split(SEPARATOR)
            val flags = parts[1].toInt()
            return ForeignSearchName(
                    parts[0],
                    parts[2],
                    if (flags == 0) {
                        EMPTY_FLAGS
                    } else {
                        EnumSet.copyOf(Flag.values().filter { it.value and flags != 0 })
                    })
        }

        fun of(table: String, nameWithFlags: String): ForeignSearchName {
            val parts = nameWithFlags.split(NAME_FLAGS_SEPARATOR)
            return ForeignSearchName(
                    table,
                    parts[0],
                    if (parts.size == 1) {
                        EMPTY_FLAGS
                    } else {
                        EnumSet.copyOf(parts[1].toCharArray().map { Flag.fromMnemonic(it) })
                    })
        }
    }

    // SS, NS, [L], M, #{Sessions}*/ #{Sessions}* {Sessions.[Name]*}
    // #{set}
    // [vec/list]
    // {map}
    enum class DataType {
        LIST,
        MAP,
        SET
    }

    enum class Flag(val value: Int, val mnemonic: Char) {
        QUESTION(1 shl 0, '?'),
        ENVIRONMENT_STRIPPED(1 shl 1, Character.MIN_VALUE),
        EXPAND_COLLECTION(1 shl 2, '*'), ;

        companion object {
            fun fromMnemonic(mnemonic: Char): Flag {
                return Flag.values().find { it.mnemonic == mnemonic }
                        ?: throw UnsupportedOperationException("No flag contains mnemonic '$mnemonic'")
            }
        }
    }

    fun getFullName(): String {
        var acc = 0
        flags.forEach { acc = acc or it.value }
        return "$table$SEPARATOR$acc$SEPARATOR$name"
    }

    fun getNameWithFlags(): String {
        if (flags.isEmpty() || (flags - HIDDEN_FLAGS).isEmpty()) {
            return name
        }
        return name + NAME_FLAGS_SEPARATOR + flags.map(Flag::mnemonic).joinToString("")
    }

    fun matches(table: String): Boolean {
        return this.table == table
    }
}