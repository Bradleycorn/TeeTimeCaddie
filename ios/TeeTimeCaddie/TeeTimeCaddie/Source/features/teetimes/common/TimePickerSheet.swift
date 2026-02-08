//
//  TimePickerSheet.swift
//  TeeTimeCaddie
//

import SwiftUI
import TeeTimeCaddieKit

/// A sheet for selecting a time using a wheel picker.
///
/// This view is shared between the Add and Edit Tee Time screens.
struct TimePickerSheet: View {
    @Binding var selectedTime: Date
    let onCancel: () -> Void
    let onConfirm: () -> Void

    var body: some View {
        NavigationStack {
            DatePicker(
                "",
                selection: $selectedTime,
                displayedComponents: .hourAndMinute
            )
            .datePickerStyle(.wheel)
            .labelsHidden()
            .navigationTitle(TTR.strings().field_Label_Time.desc().localized())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(GR.strings().cancel.desc().localized()) {
                        onCancel()
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(GR.strings().ok.desc().localized()) {
                        onConfirm()
                    }
                }
            }
        }
        .presentationDetents([.medium])
    }
}
