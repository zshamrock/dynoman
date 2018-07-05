package function

import com.akazlou.dynoman.function.TimestampFunction


object TimestampFunctionSpec : StringSpek({
    describe("timestamp function") {
        val function = TimestampFunction()

        it("should parse text without am/pm into the proper timestamp") {
            val timestamp = function.apply("2018-07-03 22:51:20")
            assertThat(timestamp).isEqualTo(1530658280001L)
        }
    }
})