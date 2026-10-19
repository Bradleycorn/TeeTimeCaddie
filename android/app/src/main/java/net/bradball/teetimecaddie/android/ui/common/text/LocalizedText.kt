package net.bradball.teetimecaddie.android.ui.common.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.bradball.teetimecaddie.core.models.LocalizedText

/** This text, with its placeholders filled, resolved for the current locale. */
@Composable
fun stringResource(text: LocalizedText): String = stringResource(text.resource.resourceId, *text.args.toTypedArray())

