package io.burt.jmespath.kotlinx

import io.burt.jmespath.BaseRuntime
import io.burt.jmespath.JmesPathType
import io.burt.jmespath.RuntimeConfiguration
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


@OptIn(ExperimentalSerializationApi::class)
class KotlinRuntime(configuration: RuntimeConfiguration, private val json: Json) :
    BaseRuntime<JsonElement>(configuration) {


    constructor() : this(RuntimeConfiguration.defaultConfiguration(), Json {
        encodeDefaults = true
        explicitNulls = true
    })

    constructor(configuration: RuntimeConfiguration) : this(configuration, Json {
        encodeDefaults = true
        explicitNulls = true
    })

    override fun toString(value: JsonElement) = value.jsonPrimitive.content

    override fun parseString(str: String): JsonElement {
        return json.parseToJsonElement(str)
    }

    override fun createNull(): JsonElement {
        return JsonNull
    }

    override fun createString(str: String): JsonElement {
        return json.parseToJsonElement(str)
    }

    override fun createBoolean(b: Boolean): JsonElement {
        return JsonPrimitive(b)
    }

    override fun createNumber(n: Double): JsonElement {
        return JsonPrimitive(n)
    }

    override fun createNumber(n: Long): JsonElement {
        return JsonPrimitive(n)
    }

    override fun createObject(obj: MutableMap<JsonElement, JsonElement>?): JsonElement {
        return json.encodeToJsonElement(obj)
    }

    override fun createArray(elements: MutableCollection<JsonElement>): JsonElement {
        return JsonArray(elements.toList())
    }

    override fun getPropertyNames(value: JsonElement): Collection<JsonElement> {
        return if (value is JsonObject) {
            value.jsonObject.keys.map { JsonPrimitive(it) }
        } else
            emptyList()
    }

    override fun getProperty(value: JsonElement, name: JsonElement): JsonElement {
        return value.jsonObject[name.jsonPrimitive.content] ?: JsonNull
    }

    override fun typeOf(value: JsonElement): JmesPathType {
        return if (value == JsonNull)
            JmesPathType.NULL
        else if (value is JsonArray)
            JmesPathType.ARRAY
        else if (value is JsonObject)
            JmesPathType.OBJECT
        else if (value.jsonPrimitive.booleanOrNull != null)
            JmesPathType.BOOLEAN
        else if (value.jsonPrimitive.doubleOrNull != null)
            JmesPathType.NUMBER
        else
            JmesPathType.STRING
    }

    override fun isTruthy(value: JsonElement): Boolean {
        return value.jsonPrimitive.jsonPrimitive.boolean
    }

    override fun toNumber(value: JsonElement): Number {
        return value.jsonPrimitive.content.toDouble()
    }

    override fun toList(value: JsonElement?): List<JsonElement> {
        return when (value) {
            is JsonArray -> value.jsonArray
            is JsonObject -> value.values.toList()
            else -> emptyList()
        }
    }
}
