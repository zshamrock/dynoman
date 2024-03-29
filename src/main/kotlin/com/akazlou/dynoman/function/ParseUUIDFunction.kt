package com.akazlou.dynoman.function

import java.util.*

class ParseUUIDFunction : Function<String>() {

    override fun name(): String {
        return "parse_uuid"
    }

    override fun run(vararg args: Any): String {
        val text = args[0].toString()
        val uuid = text.lowercase(Locale.ROOT).replace("-", "")
        return UUID.fromString(uuid.substring(0, 8) + "-"
                + uuid.substring(8, 12) + "-"
                + uuid.substring(12, 16) + "-"
                + uuid.substring(16, 20) + "-"
                + uuid.substring(20)).toString()
    }

    override fun args(): List<Arg> =
            listOf(Arg("UUID", ArgType.STRING, "UUID as the raw string, i.e. 3A2DD5E0D2C04F13A3E2F600C9530793"))
}