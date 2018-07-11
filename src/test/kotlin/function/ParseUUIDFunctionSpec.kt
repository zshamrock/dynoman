package function

import com.akazlou.dynoman.function.ParseUUIDFunction
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ParseUUIDFunctionSpec : StringSpec({
    "parse upper cased UUID without -" {
        val function = ParseUUIDFunction()
        val uuid = function.parse("parse_uuid('45406448C4AB4F6CB827048CB6D9B52F')")
        uuid shouldBe "45406448-c4ab-4f6c-b827-048cb6d9b52f"
    }

    "parse lower cased UUID" {
        val function = ParseUUIDFunction()
        val uuid = function.parse("parse_uuid(\"45406448-c4ab-4f6c-b827-048cb6d9b52f\")")
        uuid shouldBe "45406448-c4ab-4f6c-b827-048cb6d9b52f"
    }
})