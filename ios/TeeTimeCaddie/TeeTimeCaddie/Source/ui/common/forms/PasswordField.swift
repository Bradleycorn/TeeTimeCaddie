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
