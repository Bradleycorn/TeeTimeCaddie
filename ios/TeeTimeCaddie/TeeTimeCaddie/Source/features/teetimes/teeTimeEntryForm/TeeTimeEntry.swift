import SwiftUI
import ThemeUI
import TeeTimeCaddieKit

struct TeeTimeEntryScreen: View {
    @EnvironmentObject private var theme: AppTheme
    
    @State private var courseName: String = ""
    @State private var date: Date? = nil
    
    private let onTeeTimeCreated: ()->Void
    
    init(onTeeTimeCreated: @escaping ()->Void) {
        self.onTeeTimeCreated = onTeeTimeCreated
    }
   
    @StateObject
    private var vm = TeetimeEntryViewModel()

    var body: some View {
        VStack(alignment: .leading) {
            Text(TTR.strings().add_tee_time.desc().localized())
                .font(theme.typography.headlineMedium)
            Spacer().frame(height: 12)

            TtcTextField(TTR.strings().field_label_course_name.desc().localized(), value: $courseName)
            Spacer().frame(height: 16)

            DateField(TTR.strings().field_label_Date.desc().localized(), selectedDate: $date)
            Spacer().frame(height: 16)

            HStack {
                Spacer()
                Button(TTR.strings().button_create.desc().localized(), action: {
                    vm.createTeeTime(course: courseName, date: date!, onTeeTimeCreated: onTeeTimeCreated)
                })
                .buttonStyle(.Filled)
            }
            Spacer()
        }
        .padding(16)
        .errorAlert(error: $vm.displayError)
    }
}

#Preview {
    TeeTimeCaddieTheme {
        Surface {
            Text("Screen")
        }
        .sheet(isPresented: .constant(true)){
            TeeTimeEntryScreen(onTeeTimeCreated: {})
                .presentationDetents([.fraction(0.4)])
        }
    }
}
