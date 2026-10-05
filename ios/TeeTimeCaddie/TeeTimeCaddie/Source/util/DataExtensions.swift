//
//  DataExtensions.swift
//  TeeTimeCaddie
//

import Foundation
import TeeTimeCaddieKit

extension Data {
    /// This data as a Kotlin `ByteArray`, for the SDK's photo parameter.
    ///
    /// Kotlin/Native exposes no bulk initialiser, so this fills element by element. Fine for a
    /// downscaled avatar; don't reach for it with anything large.
    ///
    /// Lives here rather than in `ImageExtensions` because importing the SDK into that file makes
    /// `ImageResource` ambiguous — moko exports a type of the same name as the asset catalog's.
    func toKotlinByteArray() -> KotlinByteArray {
        let bytes = [UInt8](self)
        return KotlinByteArray(size: Int32(bytes.count)) { index in
            KotlinByte(value: Int8(bitPattern: bytes[Int(truncating: index)]))
        }
    }
}
