package net.bradball.teetimecaddie.core.analytics

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.encodeToMap

@Serializable
sealed class AnalyticsEvent(val name: String, internal val type: EventType) {

    @Serializable
    data class MyEvent(val test: String): AnalyticsEvent("my_event", EventType.CLICK)


    @OptIn(ExperimentalSerializationApi::class)
    val asMap: Map<String, Any> by lazy {
        Properties.encodeToMap(this)
    }
}