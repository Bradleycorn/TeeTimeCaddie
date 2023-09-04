import SwiftUI

struct PasswordField: View {
    
    private let value: Binding<String>
    private let helperText: String

    @State private var showText: Bool = false
    
    private var icon: String {
        return showText ? "eye.slash.fill" : "eye.fill"
    }
    
    init(_ title: String = "", text: Binding<String>) {
        self.value = text
        self.helperText = title
    }
        
    var body: some View {
        
        HStack {
            ZStack(alignment: .trailing) {
                SecureField(helperText, text: value)
                    .opacity(showText ? 0 : 1)
                    .autocorrectionDisabled(true)
                TextField(helperText, text: value)
                    .opacity(showText ? 1 : 0)
                    .autocorrectionDisabled(true)
            }
            Button(action: { showText.toggle() }) {
                Image(systemName: icon)
            }
        }
    }
}


struct PasswordField_Previews: PreviewProvider {

    struct PreviewContent: View {
        @State var name: String = ""
        @State var password: String = ""

        var body: some View {
            Form {
                
                Section {
                    PasswordField("Passord", text: $password)
                }
                
                Section {
                    TextField("Test", text: $name)
                    PasswordField("Password", text: $password)
                    TextField("Test", text: $name)
                }


            }
        }
    }
    
    static var previews: some View {
        PreviewContent()
    }
    
}

//fileprivate struct TtcInputWrapper<Input: View>: View {
//
//    private let label: String
//    private let labelColor: Color
//    private let textField: ()->Input
//
//
//    init(
//        label: String,
//        labelColor: Color = Color.primary,
//        @ViewBuilder textField: @escaping ()->Input) {
//
//        self.label = label
//        self.labelColor = labelColor
//        self.textField = textField
//    }
//
//
//    var body: some View {
//        VStack(alignment: .leading, spacing: 2) {
//
//            Text(label)
//                .foregroundColor(labelColor)
//                .font(.system(size: 15))
//                .padding(.leading, 12)
//            textField()
//                .padding(.all, 12)
//                .background(backgroundColor)
//                .cornerRadius(8)
//        }
//    }
//}
//
//struct TtcTextField: View {
//
//    private let label: String
//    private let labelColor: Color
//    private let value: Binding<String>
//    private let helperText: String
//
//
//    init(label: String, value: Binding<String>, labelColor: Color = Color.primary, helperText: String = "") {
//        self.label = label
//        self.labelColor = labelColor
//        self.value = value
//        self.helperText = helperText
//    }
//
//    var body: some View {
//        TtcInputWrapper(label: label, labelColor: labelColor) {
//            TextField(helperText, text: value)
//        }
//    }
//}
//
//struct TtcPasswordField: View {
//
//    private let label: String
//    private let labelColor: Color
//    private let value: Binding<String>
//    private let helperText: String
//
//    @State private var showText: Bool = false
//
//    private var icon: String {
//        return showText ? "eye.slash.fill" : "eye.fill"
//    }
//
//    init(label: String, value: Binding<String>, labelColor: Color = Color.primary, helperText: String = "") {
//        self.label = label
//        self.labelColor = labelColor
//        self.value = value
//        self.helperText = helperText
//    }
//
//    var body: some View {
//
//        TtcInputWrapper(label: self.label, labelColor: labelColor) {
//            HStack {
//                ZStack(alignment: .trailing) {
//                    SecureField(helperText, text: value)
//                        .opacity(showText ? 0 : 1)
//                        .autocorrectionDisabled(true)
//                    TextField(helperText, text: value)
//                        .opacity(showText ? 1 : 0)
//                        .autocorrectionDisabled(true)
//                }
//                Button(action: { showText.toggle() }) {
//                    Image(systemName: icon)
//                }
//            }
//        }
//    }
//}

