// ModifyUserInfoScreen.swift
import SwiftUI
import SwiftData

struct ModifyUserInfoScreen: View {
    @State private var viewModel = UserViewModel()
    @State private var name = ""
    @State private var weight = ""
    @State private var height = ""
    @State private var dob = ""
    @State private var showDatePicker = false
    @State private var selectedDate = Date()
    @Environment(\.dismiss) private var dismiss
    let modelContext: ModelContext
    
    var body: some View {
        ZStack {
            AmbientBackground()
            ScrollView {
                VStack(spacing: 16) {
                    OperatorHeader(subtitle: "Credentials", title: "Update Info")
                    JuicyInput(placeholder: "FULL NAME", text: $name)
                    JuicyInput(placeholder: "WEIGHT (KG)", text: $weight, keyboardType: .decimalPad)
                    JuicyInput(placeholder: "HEIGHT (CM)", text: $height, keyboardType: .decimalPad)
                    JuicyInput(placeholder: "DATE OF BIRTH", text: $dob, isReadOnly: true, onTap: { showDatePicker = true })
                    
                    Spacer().frame(height: 16)
                    JuicyButton(text: "SAVE CHANGES", action: {
                        if let user = viewModel.user {
                            user.name = name
                            user.weight = Float(weight) ?? 0
                            user.height = Float(height) ?? 0
                            user.dateOfBirth = dob
                            viewModel.updateUser()
                        } else {
                            let newUser = UserModel(name: name, weight: Float(weight) ?? 0, height: Float(height) ?? 0, dateOfBirth: dob)
                            viewModel.insertUser(newUser)
                        }
                        dismiss()
                    })
                    JuicyButton(text: "CANCEL", action: { dismiss() })
                }
                .padding(DesignSystem.padding)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar { ToolbarItem(placement: .navigationBarLeading) { Button(action: { dismiss() }) { Image(systemName: "chevron.left").foregroundStyle(AppColors.primaryAccent) } } }
        .sheet(isPresented: $showDatePicker) {
            VStack {
                DatePicker("Date of Birth", selection: $selectedDate, displayedComponents: .date)
                    .datePickerStyle(.wheel).labelsHidden().padding()
                JuicyButton(text: "CONFIRM", action: {
                    let fmt = DateFormatter(); fmt.dateFormat = "yyyy-MM-dd"
                    dob = fmt.string(from: selectedDate); showDatePicker = false
                }).padding(.horizontal).padding(.bottom)
            }.presentationDetents([.height(320)]).background(Color.black)
        }
        .onAppear {
            viewModel.setup(modelContext: modelContext)
            if let user = viewModel.user {
                name = user.name; weight = String(user.weight); height = String(user.height); dob = user.dateOfBirth
            }
        }
    }
}
