//
//  Shapes.swift
//  TeeTimeCaddie
//
//  Created by Brad Ball on 3/14/24.
//

import Foundation
import SwiftUI

public class Shapes: ObservableObject {
    @Published public private(set) var small: AnyShape
    @Published public private(set) var medium: AnyShape
    @Published public private(set) var large: AnyShape
    
    public init(
        small: some Shape = RoundedRectangle(cornerRadius: 8),
        medium: some Shape = RoundedRectangle(cornerRadius: 10),
        large: some Shape = RoundedRectangle(cornerRadius: 12)) {

            self.small = AnyShape(small)
            self.medium = AnyShape(medium)
            self.large = AnyShape(large)
    }
}
