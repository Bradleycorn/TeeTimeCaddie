package net.bradball.teetimecaddie.android.ui.common.forms.filters


/**
 * A text filter for [CdiTextField]s that
 * limits inputs to numbers only, and rejects other characters.
 */
val numericTextFilter: (String, String) -> String = { _, new ->
    new.replace(Regex("\\D"), "")
}