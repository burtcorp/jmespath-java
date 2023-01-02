package io.burt.jmespath.kotlinx

import io.burt.jmespath.JmesPathComplianceTest
import io.burt.jmespath.Adapter
import io.burt.jmespath.RuntimeConfiguration
import kotlinx.serialization.json.JsonElement

class KotlinxComplianceTest : JmesPathComplianceTest<JsonElement>() {
    override fun runtime(): Adapter<JsonElement> = KotlinRuntime(RuntimeConfiguration.defaultConfiguration())
}
