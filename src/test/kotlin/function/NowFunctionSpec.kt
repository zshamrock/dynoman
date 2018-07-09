package function

import com.akazlou.dynoman.function.NowFunction
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.time.Clock
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZoneOffset

class NowFunctionSpec : StringSpec({
    "now should return the current timestamp" {
        val function = NowFunction(
                Clock.fixed(
                        LocalDateTime.of(2018, Month.JULY, 9, 23, 15, 21).toInstant(ZoneOffset.UTC), ZoneId.of("UTC")))
        val timestamp = function.parse("now()")
        timestamp shouldBe 1531178121000L
    }
})