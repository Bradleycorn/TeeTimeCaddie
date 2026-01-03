package net.bradball.teetimecaddie.android.ui.navigation

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.savedstate.compose.serialization.serializers.SnapshotStateListSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer

// THE CODE IN THIS FILE WAS "BORROWED" FROM THE ANDROIDX SAVEDSTATE LIBRARY.
// https://cs.android.com/androidx/platform/frameworks/support/+/8f72b29820f6ac79a6147a74c727944d3c225e04:navigation3/navigation3-runtime/src/androidMain/kotlin/androidx/navigation3/runtime/NavBackStackSerializer.android.kt
//
// THAT LIBRARY USES THESE CLASSES INTERNALLY TO SERIALIZE A SINGLE BACKSTACK
// VIA THE rememberNavBackStack() COMPOSABLE.
//
// THIS APP HAS MULTIPLE BACKSTACKS, MANAGED BY THE Navigator CLASS (WHICH IS BASED
// ON THE ANDROID NAV3 SAMPLE FOR MULTIPLE BACKSTACKS WITH A BOTTOM NAV BAR).
// https://github.com/android/nav3-recipes/blob/main/app/src/main/java/com/example/nav3recipes/commonui/CommonUiActivity.kt
//
// THE NAVIGATOR'S SERIALIZER CLASS USES THESE CLASSES TO SERIALIZE EACH OF THE
// BACKSTACKS IT MANAGES. WE BORROWED THE CODE FROM THE ANDROIDX LIBRARY SO THAT
// WE CAN BE SURE THAT WE SERIALIZE THE BACKSTACKS IN THE SAME WAY THAT THE OFFICIAL
// LIBRARY DOES IT.


/**
 * A [KSerializer] for [SnapshotStateList] of [T], where T is a serializable type.
 *
 * This factory function creates a serializer for a [SnapshotStateList] containing elements of
 * type [T]. It uses a [ReflectivePolymorphicSerializer] for the elements, allowing for
 * serialization of polymorphic types without requiring pre-registration of subtypes.
 *
 * @return A [KSerializer] for [SnapshotStateList] of [T].
 */
@Suppress("FunctionName") // Factory function.
inline fun <reified T : Any> NavBackStackSerializer(): KSerializer<SnapshotStateList<T>> {
    // uses a reflective polymorphic strategy. This is self-contained and
    // does not require registering subtypes or passing a SerializersModule when
    // encoding/decoding. It uses reflection to resolve `.serializer()` for each type.
    val elementSerializer =  ReflectivePolymorphicSerializer<T>()

    return SnapshotStateListSerializer(elementSerializer)
}

/**
 * A [KSerializer] that enables polymorphic serialization for navigation back stack entries on
 * Android using reflection, without requiring subtypes to be pre-registered.
 *
 * ## The Problem This Solves
 * Standard `kotlinx.serialization` polymorphism requires registering all possible subtypes in a
 * `SerializersModule`. For navigation, where screen destinations are often defined across different
 * modules, this is impractical and creates tight coupling.
 *
 * ## How It Works
 * This serializer circumvents the registration requirement by storing the fully-qualified class
 * name of the object alongside its serialized data. During deserialization, it uses reflection
 * (`Class.forName`) to find the class and its default serializer.
 */
@OptIn(InternalSerializationApi::class)
class ReflectivePolymorphicSerializer<T : Any> : KSerializer<T> {

    override val descriptor =
        buildClassSerialDescriptor("PolymorphicData") {
            element(elementName = "type", serialDescriptor<String>())
            element(elementName = "payload", buildClassSerialDescriptor("Any"))
        }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): T {
        return decoder.decodeStructure(descriptor) {
            val className = decodeStringElement(descriptor, decodeElementIndex(descriptor))
            val classRef = Class.forName(className).kotlin
            val serializer = classRef.serializer()

            decodeSerializableElement(descriptor, decodeElementIndex(descriptor), serializer) as T
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeStructure(descriptor) {
            val className = value::class.java.name
            encodeStringElement(descriptor, index = 0, className)
            val serializer = value::class.serializer() as KSerializer<T>
            encodeSerializableElement(descriptor, index = 1, serializer, value)
        }
    }
}
