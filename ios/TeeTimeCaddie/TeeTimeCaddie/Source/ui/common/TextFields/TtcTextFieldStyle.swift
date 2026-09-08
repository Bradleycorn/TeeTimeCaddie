import SwiftUI
import ThemeUI

/// Shared shape values for the Fairway Morning text fields, used by ``TtcTextFieldContainer`` for
/// ``TtcTextField`` and ``TtcPasswordField``. (Parallels `TtcCardStyle` / `TtcChipStyle`; the Android
/// twin is `TtcTextFieldDefaults`.)
///
/// Colors are not resolved here — the field's states are computed inline in the container from
/// `theme.colorScheme`, since they depend on focus and error rather than on a ``TtcColorRole``.
enum TtcTextFieldStyle {

    /// The field container's clip/border/hit shape — the theme's `medium` (12pt), rounded on all four
    /// corners.
    ///
    /// This deliberately diverges from Android, which rounds the **top** corners only at
    /// `extraSmall` (4dp): that platform draws a Material 3 filled field whose squared bottom makes
    /// room for the focus/error indicator. iOS has no bottom indicator — the whole container is a
    /// rounded `surfaceContainer` block that borders on focus — so it takes `medium` all round.
    /// Each platform renders its own idiomatic field; see `TtcTextFieldDefaults.Shape`.
    static func shape(_ shapes: Shapes) -> AnyShape { shapes.medium }
}
