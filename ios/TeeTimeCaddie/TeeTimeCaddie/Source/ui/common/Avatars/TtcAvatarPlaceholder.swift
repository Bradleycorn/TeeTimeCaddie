import Foundation

/// A non-identity ``TtcAvatar`` affordance: `.guest` (a dashed circle with a person glyph) or
/// `.empty` (a dashed circle with an add glyph). These ignore ``TtcColorRole`` — they render the
/// design's dashed `outlineVariant` placeholder treatment.
///
/// Distinct from a `.neutral` avatar, which still shows initials: use a placeholder when there is no
/// player to name at all.
///
/// The Android twin is `TtcAvatarPlaceholder` in `ui/common/avatars/TtcAvatarPlaceholder.kt`.
enum TtcAvatarPlaceholder {
    case guest
    case empty
}
