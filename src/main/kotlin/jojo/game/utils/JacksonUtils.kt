package jojo.game.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.*

object JacksonUtils {
    val objectMapper: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    inline fun <reified T> stringToObject(strJson: String): T {
        return objectMapper.readValue<T>(strJson)
    }

    inline fun <reified T> mapToObject(map: Map<String, String>): T {
        return objectMapper.convertValue<T>(map)
    }
}