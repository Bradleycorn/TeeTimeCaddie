package net.bradball.teetimecaddie.android.ui.common.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.bradball.teetimecaddie.core.models.LocalizedText

/** This text, with its placeholders filled, resolved for the current locale. The iOS twin is `LocalizedText.localized()`. */
@Composable
fun LocalizedText.localized(): String = stringResource(resource.resourceId, *args.toTypedArray())
