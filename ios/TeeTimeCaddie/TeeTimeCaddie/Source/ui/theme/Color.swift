import Foundation
import SwiftUI
import ThemeUI

var backgroundColor: Color {
    return Color(UIColor.systemBackground)
}


let Red100 = Color(red: 65, green: 14, blue: 11)
let Red200 = Color(red: 96, green: 20, blue: 16)
let Red300 = Color(red: 140, green: 29, blue: 24)
let Red400 = Color(red: 179, green: 38, blue: 30)
let Red500 = Color(red: 220, green: 54, blue: 46)
let Red600 = Color(red: 228, green: 105, blue: 98)
let Red700 = Color(red: 236, green: 146, blue: 142)
let Red800 = Color(red: 242, green: 184, blue: 181)
let Red900 = Color(red: 249, green: 222, blue: 220)
let Red950 = Color(red: 252, green: 238, blue: 238)
let Red990 = Color(red: 255, green: 251, blue: 249)

let Neutral0 = Color(red: 0, green: 0, blue: 0)
let Neutral4 = Color(red: 14, green: 14, blue: 17)
let Neutral6 = Color(red: 20, green: 19, blue: 23)
let Neutral10 = Color(red: 28, green: 27, blue: 31)
let Neutral12 = Color(red: 32, green: 31, blue: 35)
let Neutral17 = Color(red: 43, green: 41, blue: 45)
let Neutral20 = Color(red: 49, green: 48, blue: 51)
let Neutral22 = Color(red: 49, green: 48, blue: 51)
let Neutral24 = Color(red: 49, green: 48, blue: 51)
let Neutral30 = Color(red: 72, green: 70, blue: 73)
let Neutral40 = Color(red: 96, green: 93, blue: 98)
let Neutral50 = Color(red: 120, green: 117, blue: 121)
let Neutral60 = Color(red: 147, green: 144, blue: 148)
let Neutral70 = Color(red: 174, green: 170, blue: 174)
let Neutral80 = Color(red: 201, green: 197, blue: 202)
let Neutral87 = Color(red: 221, green: 216, blue: 221)
let Neutral90 = Color(red: 230, green: 225, blue: 229)
let Neutral92 = Color(red: 236, green: 231, blue: 236)
let Neutral94 = Color(red: 241, green: 236, blue: 241)
let Neutral95 = Color(red: 244, green: 239, blue: 244)
let Neutral96 = Color(red: 247, green: 242, blue: 247)
let Neutral98 = Color(red: 253, green: 248, blue: 253)
let Neutral99 = Color(red: 255, green: 251, blue: 254)
let Neutral100 = Color(red: 255, green: 255, blue: 255)

let NeutralVariant0 = Color(red: 0, green: 0, blue: 0)
let NeutralVariant10 = Color(red: 29, green: 26, blue: 34)
let NeutralVariant20 = Color(red: 50, green: 47, blue: 55)
let NeutralVariant30 = Color(red: 73, green: 69, blue: 79)
let NeutralVariant40 = Color(red: 96, green: 93, blue: 102)
let NeutralVariant50 = Color(red: 121, green: 116, blue: 126)
let NeutralVariant60 = Color(red: 147, green: 143, blue: 153)
let NeutralVariant70 = Color(red: 174, green: 169, blue: 180)
let NeutralVariant80 = Color(red: 202, green: 196, blue: 208)
let NeutralVariant90 = Color(red: 231, green: 224, blue: 236)
let NeutralVariant95 = Color(red: 245, green: 238, blue: 250)
let NeutralVariant99 = Color(red: 255, green: 251, blue: 254)
let NeutralVariant100 = Color(red: 255, green: 255, blue: 255)

let Purple100 = Color(red: 33, green: 0, blue: 93)
let Purple200 = Color(red: 56, green: 30, blue: 114)
let Purple300 = Color(red: 79, green: 55, blue: 139)
let Purple400 = Color(red: 103, green: 80, blue: 164)
let Purple500 = Color(red: 127, green: 103, blue: 190)
let Purple600 = Color(red: 154, green: 130, blue: 219)
let Purple700 = Color(red: 182, green: 157, blue: 248)
let Purple800 = Color(red: 208, green: 188, blue: 255)
let Purple900 = Color(red: 234, green: 221, blue: 255)
let Purple950 = Color(red: 246, green: 237, blue: 255)
let Purple990 = Color(red: 255, green: 251, blue: 254)

let PurpleGray100 = Color(red: 29, green: 25, blue: 43)
let PurpleGray200 = Color(red: 51, green: 45, blue: 65)
let PurpleGray300 = Color(red: 74, green: 68, blue: 88)
let PurpleGray400 = Color(red: 98, green: 91, blue: 113)
let PurpleGray500 = Color(red: 122, green: 114, blue: 137)
let PurpleGray600 = Color(red: 149, green: 141, blue: 165)
let PurpleGray700 = Color(red: 176, green: 167, blue: 192)
let PurpleGray800 = Color(red: 204, green: 194, blue: 220)
let PurpleGray900 = Color(red: 232, green: 222, blue: 248)
let PurpleGray950 = Color(red: 246, green: 237, blue: 255)
let PurpleGray990 = Color(red: 255, green: 251, blue: 254)

let Plum100 = Color(red: 49, green: 17, blue: 29)
let Plum200 = Color(red: 73, green: 37, blue: 50)
let Plum300 = Color(red: 99, green: 59, blue: 72)
let Plum400 = Color(red: 125, green: 82, blue: 96)
let Plum500 = Color(red: 152, green: 105, blue: 119)
let Plum600 = Color(red: 181, green: 131, blue: 146)
let Plum700 = Color(red: 210, green: 157, blue: 172)
let Plum800 = Color(red: 239, green: 184, blue: 200)
let Plum900 = Color(red: 255, green: 216, blue: 228)
let Plum950 = Color(red: 255, green: 236, blue: 241)
let Plum990 = Color(red: 255, green: 251, blue: 250)


#Preview("Purple") {
    VStack {
        ColorPreview(Purple100, name: "100")
        ColorPreview(Purple200, name: "200")
        ColorPreview(Purple300, name: "300")
        ColorPreview(Purple400, name: "400")
        ColorPreview(Purple500, name: "500")
        ColorPreview(Purple600, name: "600")
        ColorPreview(Purple700, name: "700")
        ColorPreview(Purple800, name: "800")
        ColorPreview(Purple900, name: "900")
        ColorPreview(Purple950, name: "950")
        ColorPreview(Purple990, name: "990")
    }

}

#Preview("PurpleGray") {
    VStack {
        ColorPreview(PurpleGray100, name: "100")
        ColorPreview(PurpleGray200, name: "200")
        ColorPreview(PurpleGray300, name: "300")
        ColorPreview(PurpleGray400, name: "400")
        ColorPreview(PurpleGray500, name: "500")
        ColorPreview(PurpleGray600, name: "600")
        ColorPreview(PurpleGray700, name: "700")
        ColorPreview(PurpleGray800, name: "800")
        ColorPreview(PurpleGray900, name: "900")
        ColorPreview(PurpleGray950, name: "950")
        ColorPreview(PurpleGray990, name: "990")
    }
}


#Preview("Plum") {
    VStack {
        ColorPreview(Plum100, name: "100")
        ColorPreview(Plum200, name: "200")
        ColorPreview(Plum300, name: "300")
        ColorPreview(Plum400, name: "400")
        ColorPreview(Plum500, name: "500")
        ColorPreview(Plum600, name: "600")
        ColorPreview(Plum700, name: "700")
        ColorPreview(Plum800, name: "800")
        ColorPreview(Plum900, name: "900")
        ColorPreview(Plum950, name: "950")
        ColorPreview(Plum990, name: "990")
    }
}

#Preview("Red") {
    VStack {
        ColorPreview(Red100, name: "100")
        ColorPreview(Red200, name: "200")
        ColorPreview(Red300, name: "300")
        ColorPreview(Red400, name: "400")
        ColorPreview(Red500, name: "500")
        ColorPreview(Red600, name: "600")
        ColorPreview(Red700, name: "700")
        ColorPreview(Red800, name: "800")
        ColorPreview(Red900, name: "900")
        ColorPreview(Red950, name: "950")
        ColorPreview(Red990, name: "990")
    }
}

#Preview("Neutral") {
    HStack(spacing: 50) {
        VStack {
            ColorPreview(Neutral0, name: "0")
            ColorPreview(Neutral4, name: "4")
            ColorPreview(Neutral6, name: "6")
            ColorPreview(Neutral10, name: "10")
            ColorPreview(Neutral12, name: "12")
            ColorPreview(Neutral17, name: "17")
            ColorPreview(Neutral20, name: "20")
            ColorPreview(Neutral22, name: "22")
            ColorPreview(Neutral24, name: "24")
            ColorPreview(Neutral30, name: "30")
            ColorPreview(Neutral40, name: "40")
            ColorPreview(Neutral50, name: "50")
        }
        VStack {
            ColorPreview(Neutral60, name: "60")
            ColorPreview(Neutral70, name: "70")
            ColorPreview(Neutral80, name: "80")
            ColorPreview(Neutral87, name: "87")
            ColorPreview(Neutral90, name: "90")
            ColorPreview(Neutral92, name: "92")
            ColorPreview(Neutral94, name: "94")
            ColorPreview(Neutral95, name: "95")
            ColorPreview(Neutral96, name: "96")
            ColorPreview(Neutral98, name: "98")
            ColorPreview(Neutral99, name: "99")
            ColorPreview(Neutral100, name: "100")
        }
    }
}

#Preview("Neutral Variant") {
    VStack {
        ColorPreview(NeutralVariant0, name: "0")
        ColorPreview(NeutralVariant10, name: "10")
        ColorPreview(NeutralVariant20, name: "20")
        ColorPreview(NeutralVariant30, name: "30")
        ColorPreview(NeutralVariant40, name: "40")
        ColorPreview(NeutralVariant50, name: "50")
        ColorPreview(NeutralVariant60, name: "60")
        ColorPreview(NeutralVariant70, name: "70")
        ColorPreview(NeutralVariant80, name: "80")
        ColorPreview(NeutralVariant90, name: "90")
        ColorPreview(NeutralVariant95, name: "95")
        ColorPreview(NeutralVariant99, name: "99")
        ColorPreview(NeutralVariant100, name: "100")
    }
}
