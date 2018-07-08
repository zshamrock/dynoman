package function

import com.akazlou.dynoman.function.TimestampFunction
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec


class TimestampFunctionSpec : StringSpec({
    "should parse text without am/pm into the proper timestamp" {
        val function = TimestampFunction()
        val timestamp = function.parse("timestamp(\"2018-07-03 22:51:20\")")
        timestamp shouldBe 1530658280000L
    }
})
