// SelectSessionScreen.swift
import SwiftUI
import SwiftData

struct SelectSessionScreen: View {
    @State private var viewModel = TrainingViewModel()
    @Environment(\.dismiss) private var dismiss
    let modelContext: ModelContext
    
    var body: some View {
        ZStack {
            AmbientBackground()
            VStack(spacing: 0) {
                OperatorHeader(subtitle: "Select Session", title: "Initiate Protocol")
                Spacer().frame(height: 16)
                
                ScrollView {
                    VStack(spacing: 12) {
                        ForEach(viewModel.sessions, id: \.persistentModelID) { session in
                            NavigationLink(value: session.persistentModelID.hashValue) {
                                JuicyCard(action: {}) {
                                    HStack {
                                        VStack(alignment: .leading, spacing: 4) {
                                            Text(session.name)
                                                .font(AppTypography.titleMedium)
                                                .foregroundStyle(AppColors.primaryAccent)
                                            Text("EXERCISES: \(session.exercises.count)")
                                                .font(AppTypography.bodyMedium)
                                                .foregroundStyle(Color.gray)
                                        }
                                        Spacer()
                                        Text("START >")
                                            .font(AppTypography.labelMedium)
                                            .foregroundStyle(AppColors.secondaryAccent)
                                    }
                                }.allowsHitTesting(false)
                            }
                        }
                    }
                }
                
                JuicyButton(text: "BACK TO MENU", action: { dismiss() })
            }
            .padding(DesignSystem.padding)
        }
        .navigationBarBackButtonHidden(true)
        .toolbar { ToolbarItem(placement: .navigationBarLeading) { Button(action: { dismiss() }) { Image(systemName: "chevron.left").foregroundStyle(AppColors.primaryAccent) } } }
        .navigationDestination(for: Int.self) { _ in
            // For simplicity, start the first matching session
            TrainingSessionScreen(viewModel: viewModel)
        }
        .onAppear { viewModel.setup(modelContext: modelContext); viewModel.loadSessions() }
    }
}
