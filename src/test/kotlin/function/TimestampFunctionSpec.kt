package function

import com.akazlou.dynoman.function.TimestampFunction
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it


object TimestampFunctionSpec : Spek({
    describe("timestamp function") {
        val function = TimestampFunction()

        it("should parse text without am/pm into the proper timestamp") {
            val timestamp = function.apply("2018-07-03 22:51:20")
            assertThat(timestamp).isEqualTo(1530658280001L)
        }
    }
})