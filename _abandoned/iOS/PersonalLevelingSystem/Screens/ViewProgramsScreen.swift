// ViewProgramsScreen.swift
import SwiftUI
import SwiftData

struct ViewProgramsScreen: View {
    @State private var viewModel = TrainingViewModel()
    @Environment(\.dismiss) private var dismiss
    let modelContext: ModelContext
    
    var body: some View {
        ZStack {
            AmbientBackground()
            VStack(spacing: 0) {
                OperatorHeader(subtitle: "Database", title: "Program Archive")
                Spacer().frame(height: 16)
                
                ScrollView {
                    VStack(spacing: 12) {
                        ForEach(viewModel.programs, id: \.persistentModelID) { program in
                            JuicyCard(action: {}) {
                                HStack {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(program.name.uppercased())
                                            .font(AppTypography.titleMedium)
                                            .foregroundStyle(AppColors.primaryAccent)
                                        Text("\(program.sessions.count) SESSIONS LOGGED")
                                            .font(AppTypography.bodyMedium)
                                            .foregroundStyle(Color.gray)
                                    }
                                    Spacer()
                                    Button(action: { viewModel.deleteProgram(program) }) {
                                        Image(systemName: "xmark.circle.fill")
                                            .foregroundStyle(AppColors.secondaryAccent)
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer().frame(height: 16)
                JuicyButton(text: "RETURN TO TRAINING", action: { dismiss() })
            }
            .padding(DesignSystem.padding)
        }
        .navigationBarBackButtonHidden(true)
        .toolbar { ToolbarItem(placement: .navigationBarLeading) { Button(action: { dismiss() }) { Image(systemName: "chevron.left").foregroundStyle(AppColors.primaryAccent) } } }
        .onAppear { viewModel.setup(modelContext: modelContext); viewModel.loadPrograms() }
    }
}
