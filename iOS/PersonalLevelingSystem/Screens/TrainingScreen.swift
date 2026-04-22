// TrainingScreen.swift
// Training module hub with navigation to sub-screens

import SwiftUI
import SwiftData

struct TrainingScreen: View {
    @State private var viewModel = TrainingViewModel()
    @Environment(\.dismiss) private var dismiss
    
    let modelContext: ModelContext
    
    var body: some View {
        ZStack {
            AmbientBackground()
            
            ScrollView {
                VStack(spacing: 24) {
                    OperatorHeader(subtitle: "Protocol", title: "Training Module")
                    
                    NavigationLink(value: "create_program") {
                        JuicyButton(text: "CREATE NEW PROGRAM", action: {})
                            .allowsHitTesting(false)
                    }
                    
                    NavigationLink(value: "view_programs") {
                        JuicyButton(text: "VIEW EXISTING PROGRAMS", action: {})
                            .allowsHitTesting(false)
                    }
                    
                    NavigationLink(value: "select_session") {
                        JuicyButton(text: "START PROGRAM TRAINING", action: {})
                            .allowsHitTesting(false)
                    }
                    
                    NavigationLink(value: "flexibility") {
                        JuicyButton(text: "START FLEXIBILITY TRAINING", action: {})
                            .allowsHitTesting(false)
                    }
                    
                    NavigationLink(value: "endurance") {
                        JuicyButton(text: "START ENDURANCE TRAINING", action: {})
                            .allowsHitTesting(false)
                    }
                    
                    Spacer().frame(height: 24)
                    
                    JuicyButton(text: "RETURN TO MAIN", action: { dismiss() })
                }
                .padding(DesignSystem.padding)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .foregroundStyle(AppColors.primaryAccent)
                }
            }
        }
        .navigationDestination(for: String.self) { destination in
            switch destination {
            case "create_program":
                CreateProgramScreen(modelContext: modelContext)
            case "view_programs":
                ViewProgramsScreen(modelContext: modelContext)
            case "select_session":
                SelectSessionScreen(modelContext: modelContext)
            case "flexibility":
                FlexibilityScreen(modelContext: modelContext)
            case "endurance":
                EnduranceScreen(modelContext: modelContext)
            default:
                EmptyView()
            }
        }
        .onAppear {
            viewModel.setup(modelContext: modelContext)
        }
    }
}
