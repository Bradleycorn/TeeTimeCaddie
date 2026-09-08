package net.bradball.teetimecaddie.core.analytics

/**
 * Defines a set of types that can be used to categorize analytical events that
 * happen in the application.
 */
internal enum class EventType {
    /** For events that are fired in response to the user clicking on things,
     * or otherwise using the UI. These are UI based events that are not
     * related to actually completing a function or operation.
     * For example, clicking on a "Save" button might fire a "save_click" event.
     */
    CLICK,

    /**
     * For operations that are completed (either successfully or unsuccessfully).
     * These events happen after some operation has taken place.
     * For example, when the user clicks a "Save" button that triggers saving data
     * to the cloud, a "save_data" event will be fire after the data is saved.
     */
    OPERATION,

    /**
     * For navigation events. Screen views are handled on their own, and typically
     * do not need special events. However in cases where custom events are needed
     * to track navigation related actions, this type can be used.
     */
    NAVIGATION,

    /**
     * For application level events. Use this to track events like application
     * startup, etc.
     */
    APPLICATION
}