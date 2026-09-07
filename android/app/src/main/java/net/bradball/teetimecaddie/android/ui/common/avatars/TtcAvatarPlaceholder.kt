package net.bradball.teetimecaddie.android.ui.common.avatars

import net.bradball.teetimecaddie.android.theme.TtcColorRole

/**
 * A non-identity [TtcAvatar] affordance: [Guest] (a dashed circle with a person glyph) or [Empty]
 * (a dashed circle with an add glyph). These ignore [TtcColorRole] — they render the design's dashed
 * `outlineVariant` placeholder treatment.
 *
 * Distinct from a [TtcColorRole.Neutral] avatar, which still shows initials: use a placeholder when
 * there is no player to name at all.
 *
 * The iOS twin is `TtcAvatarPlaceholder` in `ui/common/Avatars/TtcAvatarPlaceholder.swift`.
 */
enum class TtcAvatarPlaceholder { Guest, Empty }
