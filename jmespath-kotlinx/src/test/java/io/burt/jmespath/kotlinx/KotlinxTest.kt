package io.burt.jmespath.kotlinx


import io.burt.jmespath.JmesPathRuntimeTest
import io.burt.jmespath.RuntimeConfiguration
import kotlinx.serialization.json.JsonElement

class KotlinxTest : JmesPathRuntimeTest<JsonElement>() {
    override fun createRuntime(configuration: RuntimeConfiguration) = KotlinRuntime(configuration)
}
