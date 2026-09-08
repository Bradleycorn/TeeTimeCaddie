import Foundation
import SwiftUI
import ThemeUI

// =============================================================================
// Fairway Morning — TeeTimeCaddie color system
//
// Ported 1:1 from the Android app's Color.kt. Names and ARGB hex values match
// Android exactly so the two platforms stay in lockstep. ThemeUI's
// Color(_ hexNumber: Int64) initializer takes a 32-bit ARGB value, identical to
// Compose's Color(0xAARRGGBB) constructor.
//
// Full M3 tonal palettes generated from four brand source colors and two
// neutral palettes derived from the primary hue at low chroma.
//
// Colored palettes include tone 75 for our custom light-mode container
// overrides on Primary and Tertiary; not part of the standard M3 tone set.
// =============================================================================

var backgroundColor: Color {
    return Color(UIColor.systemBackground)
}

// MARK: - Fairway green — primary brand color (source #2E7D5B, HCT tone 47)
let fairwayGreen0 = Color(0xFF000000)
let fairwayGreen10 = Color(0xFF002113)
let fairwayGreen20 = Color(0xFF003824)
let fairwayGreen30 = Color(0xFF005236)
let fairwayGreen40 = Color(0xFF176B4B)
let fairwayGreen50 = Color(0xFF378562)
let fairwayGreen60 = Color(0xFF529F7B)
let fairwayGreen70 = Color(0xFF6DBB94)
let fairwayGreen75 = Color(0xFF7BC8A1)
let fairwayGreen80 = Color(0xFF88D6AF)
let fairwayGreen90 = Color(0xFFA4F3CA)
let fairwayGreen95 = Color(0xFFBEFFDB)
let fairwayGreen99 = Color(0xFFF4FFF6)
let fairwayGreen100 = Color(0xFFFFFFFF)

// MARK: - Sun gold — secondary brand color (source #DBAA3C, HCT tone 72)
let sunGold0 = Color(0xFF000000)
let sunGold10 = Color(0xFF261900)
let sunGold20 = Color(0xFF402D00)
let sunGold30 = Color(0xFF5C4200)
let sunGold40 = Color(0xFF7A5900)
let sunGold50 = Color(0xFF997000)
let sunGold60 = Color(0xFFB7891B)
let sunGold70 = Color(0xFFD4A436)
let sunGold75 = Color(0xFFE3B143)
let sunGold80 = Color(0xFFF2BF4F)
let sunGold90 = Color(0xFFFFDEA1)
let sunGold95 = Color(0xFFFFEFD5)
let sunGold99 = Color(0xFFFFFBFF)
let sunGold100 = Color(0xFFFFFFFF)

// MARK: - Sky blue — tertiary brand color (source #4A7C95, HCT tone 50)
let skyBlue0 = Color(0xFF000000)
let skyBlue10 = Color(0xFF001E2B)
let skyBlue20 = Color(0xFF003548)
let skyBlue30 = Color(0xFF124C63)
let skyBlue40 = Color(0xFF31647C)
let skyBlue50 = Color(0xFF4B7D96)
let skyBlue60 = Color(0xFF6597B1)
let skyBlue70 = Color(0xFF80B2CD)
let skyBlue75 = Color(0xFF8EBFDB)
let skyBlue80 = Color(0xFF9BCDE9)
let skyBlue90 = Color(0xFFC1E8FF)
let skyBlue95 = Color(0xFFE2F3FF)
let skyBlue99 = Color(0xFFFBFCFF)
let skyBlue100 = Color(0xFFFFFFFF)

// MARK: - Error red (source #BA1A1A, HCT tone 40)
let errorRed0 = Color(0xFF000000)
let errorRed10 = Color(0xFF410002)
let errorRed20 = Color(0xFF690004)
let errorRed30 = Color(0xFF930009)
let errorRed40 = Color(0xFFBA1A1A)
let errorRed50 = Color(0xFFDE372F)
let errorRed60 = Color(0xFFFF5449)
let errorRed70 = Color(0xFFFF897D)
let errorRed75 = Color(0xFFFF9F94)
let errorRed80 = Color(0xFFFFB4AB)
let errorRed90 = Color(0xFFFFDAD5)
let errorRed95 = Color(0xFFFFEDEA)
let errorRed99 = Color(0xFFFFFBFF)
let errorRed100 = Color(0xFFFFFFFF)

// MARK: - Neutral — derived from primary hue, chroma 4
let neutral0 = Color(0xFF000000)
let neutral4 = Color(0xFF0C0F0D)
let neutral6 = Color(0xFF111412)
let neutral10 = Color(0xFF191C1A)
let neutral12 = Color(0xFF1D201E)
let neutral17 = Color(0xFF272B28)
let neutral20 = Color(0xFF2E312F)
let neutral22 = Color(0xFF323633)
let neutral24 = Color(0xFF373A37)
let neutral30 = Color(0xFF444845)
let neutral40 = Color(0xFF5C5F5C)
let neutral50 = Color(0xFF757874)
let neutral60 = Color(0xFF8F918E)
let neutral70 = Color(0xFFA9ACA8)
let neutral80 = Color(0xFFC5C7C3)
let neutral87 = Color(0xFFD8DBD7)
let neutral90 = Color(0xFFE1E3DF)
let neutral92 = Color(0xFFE7E9E5)
let neutral94 = Color(0xFFECEEEA)
let neutral95 = Color(0xFFEFF1ED)
let neutral96 = Color(0xFFF2F4F0)
let neutral98 = Color(0xFFF8FAF6)
let neutral99 = Color(0xFFFBFDF8)
let neutral100 = Color(0xFFFFFFFF)

// MARK: - Neutral variant — derived from primary hue, chroma 8
let neutralVariant0 = Color(0xFF000000)
let neutralVariant4 = Color(0xFF08100C)
let neutralVariant6 = Color(0xFF0D1511)
let neutralVariant10 = Color(0xFF151D19)
let neutralVariant12 = Color(0xFF19211D)
let neutralVariant17 = Color(0xFF232C27)
let neutralVariant20 = Color(0xFF2A322D)
let neutralVariant22 = Color(0xFF2E3731)
let neutralVariant24 = Color(0xFF333B36)
let neutralVariant30 = Color(0xFF404943)
let neutralVariant40 = Color(0xFF58605A)
let neutralVariant50 = Color(0xFF707973)
let neutralVariant60 = Color(0xFF8A938C)
let neutralVariant70 = Color(0xFFA4ADA6)
let neutralVariant80 = Color(0xFFC0C9C1)
let neutralVariant87 = Color(0xFFD3DCD4)
let neutralVariant90 = Color(0xFFDCE5DD)
let neutralVariant92 = Color(0xFFE1EBE2)
let neutralVariant94 = Color(0xFFE7F0E8)
let neutralVariant95 = Color(0xFFEAF3EB)
let neutralVariant96 = Color(0xFFEDF6EE)
let neutralVariant98 = Color(0xFFF3FCF3)
let neutralVariant99 = Color(0xFFF5FFF6)
let neutralVariant100 = Color(0xFFFFFFFF)


#Preview("Fairway Green") {
    HStack(spacing: 50) {
        VStack {
            ColorPreview(fairwayGreen0, name: "0")
            ColorPreview(fairwayGreen10, name: "10")
            ColorPreview(fairwayGreen20, name: "20")
            ColorPreview(fairwayGreen30, name: "30")
            ColorPreview(fairwayGreen40, name: "40")
            ColorPreview(fairwayGreen50, name: "50")
            ColorPreview(fairwayGreen60, name: "60")
        }
        VStack {
            ColorPreview(fairwayGreen70, name: "70")
            ColorPreview(fairwayGreen75, name: "75")
            ColorPreview(fairwayGreen80, name: "80")
            ColorPreview(fairwayGreen90, name: "90")
            ColorPreview(fairwayGreen95, name: "95")
            ColorPreview(fairwayGreen99, name: "99")
            ColorPreview(fairwayGreen100, name: "100")
        }
    }
}

#Preview("Sun Gold") {
    HStack(spacing: 50) {
        VStack {
            ColorPreview(sunGold0, name: "0")
            ColorPreview(sunGold10, name: "10")
            ColorPreview(sunGold20, name: "20")
            ColorPreview(sunGold30, name: "30")
            ColorPreview(sunGold40, name: "40")
            ColorPreview(sunGold50, name: "50")
            ColorPreview(sunGold60, name: "60")
        }
        VStack {
            ColorPreview(sunGold70, name: "70")
            ColorPreview(sunGold75, name: "75")
            ColorPreview(sunGold80, name: "80")
            ColorPreview(sunGold90, name: "90")
            ColorPreview(sunGold95, name: "95")
            ColorPreview(sunGold99, name: "99")
            ColorPreview(sunGold100, name: "100")
        }
    }
}

#Preview("Sky Blue") {
    HStack(spacing: 50) {
        VStack {
            ColorPreview(skyBlue0, name: "0")
            ColorPreview(skyBlue10, name: "10")
            ColorPreview(skyBlue20, name: "20")
            ColorPreview(skyBlue30, name: "30")
            ColorPreview(skyBlue40, name: "40")
            ColorPreview(skyBlue50, name: "50")
            ColorPreview(skyBlue60, name: "60")
        }
        VStack {
            ColorPreview(skyBlue70, name: "70")
            ColorPreview(skyBlue75, name: "75")
            ColorPreview(skyBlue80, name: "80")
            ColorPreview(skyBlue90, name: "90")
            ColorPreview(skyBlue95, name: "95")
            ColorPreview(skyBlue99, name: "99")
            ColorPreview(skyBlue100, name: "100")
        }
    }
}

#Preview("Error Red") {
    HStack(spacing: 50) {
        VStack {
            ColorPreview(errorRed0, name: "0")
            ColorPreview(errorRed10, name: "10")
            ColorPreview(errorRed20, name: "20")
            ColorPreview(errorRed30, name: "30")
            ColorPreview(errorRed40, name: "40")
            ColorPreview(errorRed50, name: "50")
            ColorPreview(errorRed60, name: "60")
        }
        VStack {
            ColorPreview(errorRed70, name: "70")
            ColorPreview(errorRed75, name: "75")
            ColorPreview(errorRed80, name: "80")
            ColorPreview(errorRed90, name: "90")
            ColorPreview(errorRed95, name: "95")
            ColorPreview(errorRed99, name: "99")
            ColorPreview(errorRed100, name: "100")
        }
    }
}

#Preview("Neutral") {
    HStack(spacing: 50) {
        VStack {
            ColorPreview(neutral0, name: "0")
            ColorPreview(neutral4, name: "4")
            ColorPreview(neutral6, name: "6")
            ColorPreview(neutral10, name: "10")
            ColorPreview(neutral12, name: "12")
            ColorPreview(neutral17, name: "17")
            ColorPreview(neutral20, name: "20")
            ColorPreview(neutral22, name: "22")
            ColorPreview(neutral24, name: "24")
            ColorPreview(neutral30, name: "30")
            ColorPreview(neutral40, name: "40")
            ColorPreview(neutral50, name: "50")
        }
        VStack {
            ColorPreview(neutral60, name: "60")
            ColorPreview(neutral70, name: "70")
            ColorPreview(neutral80, name: "80")
            ColorPreview(neutral87, name: "87")
            ColorPreview(neutral90, name: "90")
            ColorPreview(neutral92, name: "92")
            ColorPreview(neutral94, name: "94")
            ColorPreview(neutral95, name: "95")
            ColorPreview(neutral96, name: "96")
            ColorPreview(neutral98, name: "98")
            ColorPreview(neutral99, name: "99")
            ColorPreview(neutral100, name: "100")
        }
    }
}

#Preview("Neutral Variant") {
    HStack(spacing: 50) {
        VStack {
            ColorPreview(neutralVariant0, name: "0")
            ColorPreview(neutralVariant4, name: "4")
            ColorPreview(neutralVariant6, name: "6")
            ColorPreview(neutralVariant10, name: "10")
            ColorPreview(neutralVariant12, name: "12")
            ColorPreview(neutralVariant17, name: "17")
            ColorPreview(neutralVariant20, name: "20")
            ColorPreview(neutralVariant22, name: "22")
            ColorPreview(neutralVariant24, name: "24")
            ColorPreview(neutralVariant30, name: "30")
            ColorPreview(neutralVariant40, name: "40")
            ColorPreview(neutralVariant50, name: "50")
        }
        VStack {
            ColorPreview(neutralVariant60, name: "60")
            ColorPreview(neutralVariant70, name: "70")
            ColorPreview(neutralVariant80, name: "80")
            ColorPreview(neutralVariant87, name: "87")
            ColorPreview(neutralVariant90, name: "90")
            ColorPreview(neutralVariant92, name: "92")
            ColorPreview(neutralVariant94, name: "94")
            ColorPreview(neutralVariant95, name: "95")
            ColorPreview(neutralVariant96, name: "96")
            ColorPreview(neutralVariant98, name: "98")
            ColorPreview(neutralVariant99, name: "99")
            ColorPreview(neutralVariant100, name: "100")
        }
    }
}
