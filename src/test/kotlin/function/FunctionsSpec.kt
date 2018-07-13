package function

import com.akazlou.dynoman.function.Functions
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class FunctionsSpec : StringSpec({
    "should get all available functions" {
        val functions = Functions.getAvailableFunctions()
        functions.size shouldBe 3
    }
})