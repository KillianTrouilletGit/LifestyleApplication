// CreateProgramScreen.swift
import SwiftUI
import SwiftData

struct CreateProgramScreen: View {
    @State private var viewModel = TrainingViewModel()
    @State private var programName = ""
    @State private var sessions: [UiSession] = []
    @Environment(\.dismiss) private var dismiss
    let modelContext: ModelContext
    
    var body: some View {
        ZStack {
            AmbientBackground()
            VStack(spacing: 0) {
                OperatorHeader(subtitle: "Architect", title: "Create Program")
                
                ScrollView {
                    VStack(spacing: 16) {
                        JuicyInput(placeholder: "PROGRAM NAME", text: $programName)
                        
                        ForEach(sessions.indices, id: \.self) { si in
                            JuicyCard(action: {}) {
                                VStack(spacing: 12) {
                                    HStack {
                                        JuicyInput(placeholder: "SESSION NAME", text: $sessions[si].name)
                                        Button(action: { sessions.remove(at: si) }) {
                                            Image(systemName: "xmark.circle.fill").foregroundStyle(AppColors.alertOrange)
                                        }
                                    }
                                    
                                    ForEach(sessions[si].exercises.indices, id: \.self) { ei in
                                        HStack(spacing: 8) {
                                            JuicyInput(placeholder: "EXERCISE", text: $sessions[si].exercises[ei].name)
                                            JuicyInput(placeholder: "SETS", text: $sessions[si].exercises[ei].sets, keyboardType: .numberPad)
                                                .frame(width: 80)
                                            Button(action: { sessions[si].exercises.remove(at: ei) }) {
                                                Image(systemName: "xmark").foregroundStyle(AppColors.telemetryGreen)
                                            }
                                        }
                                    }
                                    
                                    JuicyButton(text: "+ ADD EXERCISE", action: { sessions[si].exercises.append(UiExercise()) })
                                }
                            }
                        }
                        
                        JuicyButton(text: "+ ADD SESSION", action: { sessions.append(UiSession()) })
                    }
                    .padding(.top, 16)
                }
                
                Spacer().frame(height: 16)
                JuicyButton(text: "SAVE PROGRAM", action: {
                    guard !programName.isEmpty, !sessions.isEmpty else { return }
                    viewModel.saveProgram(name: programName, newSessions: sessions)
                    dismiss()
                })
                Spacer().frame(height: 8)
                JuicyButton(text: "ABORT / RETURN", action: { dismiss() })
            }
            .padding(DesignSystem.padding)
        }
        .navigationBarBackButtonHidden(true)
        .toolbar { ToolbarItem(placement: .navigationBarLeading) { Button(action: { dismiss() }) { Image(systemName: "chevron.left").foregroundStyle(AppColors.primaryAccent) } } }
        .onAppear { viewModel.setup(modelContext: modelContext) }
    }
}
