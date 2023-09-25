package net.bradball.teetimecaddie.core.analytics

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.properties.Properties
import kotlinx.serialization.properties.encodeToMap

@Serializable
sealed class AnalyticsEvent(val name: String, internal val type: EventType) {

    @Serializable
    object Login: AnalyticsEvent("login", EventType.OPERATION)
    @Serializable
    data class FailedLogin(val reason: String? = null): AnalyticsEvent("failed_login", EventType.OPERATION)
    @Serializable
    object CreateAccount: AnalyticsEvent("create_account", EventType.OPERATION)
    @Serializable
    data class FailedRegistration(val reason: String?): AnalyticsEvent("failed_registration", EventType.OPERATION)


    @OptIn(ExperimentalSerializationApi::class)
    val asMap: Map<String, Any> by lazy {
        Properties.encodeToMap(this)
    }
}