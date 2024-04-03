import Foundation
import ThemeUI
import SwiftUI

struct DateField: View {
    
    @EnvironmentObject
    private var theme: AppTheme
    
    @Binding private var selectedDate: Date?
    @State private var pickerDate: Date = Date.today
    @State private var dateString: String = ""
    @State private var oldDateString: String = ""
    @State private var showCalendar = false
    private var label: String
    
    private let formatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "MM/dd/yyyy"
        f.isLenient = false
        return f
    }()
    
    private let selectableDates: DateInterval
    
    init(_ label: String, selectedDate: Binding<Date?>) {
        self._selectedDate = selectedDate
        self.selectableDates = DateInterval(start: Date.today, end: Date.today.add(5, interval: .year))
        self.label = label
        
        guard let initialDate = selectedDate.wrappedValue else { return }
        let initialDateString = formatter.string(from: initialDate)
        self.pickerDate = initialDate
        self.dateString = initialDateString
        self.oldDateString = initialDateString
    }
        
    private func onTextChanged(newValue: String) {
        var newDate = newValue
        
        if newValue.count < oldDateString.count && oldDateString.last == "/" {
            newDate = String(newValue.dropLast(1))
        }
        
        newDate = newDate.filter("0123456789".contains)
        
        if (newDate.count > 8) {
            newDate = String(newDate.dropLast(newDate.count - 8))
        }
                
        if (newDate.count >= 2) {
            newDate = newDate.insert("/", at: 2)
        }
        
        if (newDate.count >= 5) {
            newDate = newDate.insert("/", at: 5)
        }
        
        oldDateString = newDate
        dateString = newDate
        
        guard let date = formatter.date(from: dateString) else {
            selectedDate = nil
            return
        }
        if (selectableDates.contains(date)) {
            selectedDate = date
        } else {
            selectedDate = nil
        }
    }
    
    private func onIconClicked() {
        pickerDate = selectedDate ?? Date.today
        showCalendar = true
    }

    private func onDatePicked(newDate: Date) {
        oldDateString = formatter.string(from: newDate)
        dateString = formatter.string(from: newDate)
        showCalendar = false
    }

    var body: some View {
        FormField(label) {
            HStack {
                TextField(
                    "mm/dd/yyyy",
                    text: $dateString
                )
                .autocorrectionDisabled(true)
                .keyboardType(.numberPad)
                .textContentType(.dateTime)
                .onChange(of: dateString, perform: onTextChanged)
                
                Button(action: onIconClicked) {
                    Image(.Icons.calendar)
                }
                .sheet(isPresented: $showCalendar, content: {
                    PickerView(title: label, selectableDates: selectableDates.start...selectableDates.end, isPresented: $showCalendar, initialDate: pickerDate, onDateSelected: onDatePicked)
                        .presentationDetents([.fraction(0.67)])
                })
            }
        }
    }
}

fileprivate struct PickerView: View {
    
    @EnvironmentObject
    private var theme: AppTheme
    
    private let title: String
    private let selectableDates: ClosedRange<Date>
    @Binding private var isPresented: Bool
    @State private var selectedDate: Date = Date.today
    
    private let onDateSelected: (Date)->Void
    
    init(title: String, selectableDates: ClosedRange<Date>, isPresented: Binding<Bool>, initialDate: Date, onDateSelected: @escaping (Date) -> Void) {
        self.title = title
        self.selectableDates = selectableDates
        self._isPresented = isPresented
        self._selectedDate = State(initialValue: initialDate)
        self.onDateSelected = onDateSelected
    }
    
    var body: some View {
        VStack(alignment: .leading) {
            Text(title)
                .font(theme.typography.headlineMedium)
                .padding(.horizontal, 16)
            DatePicker("", selection: $selectedDate, in: selectableDates, displayedComponents: [.date])
                .datePickerStyle(.graphical)
            HStack {
                Spacer()
                Button(action: { isPresented = false }) {
                    Text("Cancel")
                }.buttonStyle(.Outlined)
                Spacer().frame(width: 16)
                Button(action: {
                    onDateSelected(selectedDate)
                    isPresented = false
                }) {
                    Text("OK")
                }.buttonStyle(.Filled)
            }.padding(.horizontal, 16)
            Spacer()
        }.padding(.vertical, 24)
    }
}


#Preview {
    TeeTimeCaddieTheme {
        VStack(spacing: 20) {
            DateField("Tee Time", selectedDate: .constant(nil))
        }.padding(24)
    }
}
