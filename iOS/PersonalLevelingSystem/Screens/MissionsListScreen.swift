// MissionsListScreen.swift
// Daily and Weekly mission list with gradient checkboxes

import SwiftUI
import SwiftData

struct MissionsListScreen: View {
    @State private var viewModel: MissionViewModel
    @Environment(\.dismiss) private var dismiss
    
    init(missionRepo: MissionRepository, modelContext: ModelContext) {
        let vm = MissionViewModel(missionRepo: missionRepo)
        vm.setup(modelContext: modelContext)
        _viewModel = State(initialValue: vm)
    }
    
    var body: some View {
        ZStack {
            AmbientBackground()
            
            VStack(spacing: 0) {
                ScrollView {
                    VStack(alignment: .leading, spacing: 0) {
                        OperatorHeader(subtitle: "Objectives", title: "Active Missions")
                        
                        Spacer().frame(height: 16)
                        
                        if !viewModel.dailyMissions.isEmpty {
                            MissionSectionHeader(title: "DAILY MISSIONS")
                            
                            ForEach(viewModel.dailyMissions) { mission in
                                MissionItem(mission: mission) {
                                    viewModel.completeMission(mission)
                                }
                            }
                        }
                        
                        if !viewModel.weeklyMissions.isEmpty {
                            Spacer().frame(height: 16)
                            MissionSectionHeader(title: "WEEKLY MISSIONS")
                            
                            ForEach(viewModel.weeklyMissions) { mission in
                                MissionItem(mission: mission) {
                                    viewModel.completeMission(mission)
                                }
                            }
                        }
                    }
                }
                
                Spacer().frame(height: 16)
                
                JuicyButton(text: "CLOSE", action: { dismiss() })
            }
            .padding(DesignSystem.padding)
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
    }
}

// MARK: - Section Header
struct MissionSectionHeader: View {
    let title: String
    
    var body: some View {
        Text(title)
            .font(AppTypography.labelLarge)
            .foregroundStyle(AppColors.primaryAccent)
            .padding(.vertical, 8)
    }
}

// MARK: - Mission Item
struct MissionItem: View {
    let mission: Mission
    let onCheck: () -> Void
    
    var body: some View {
        HStack(spacing: 12) {
            Text(mission.description)
                .font(AppTypography.bodyLarge)
                .foregroundStyle(AppColors.hologramText)
                .frame(maxWidth: .infinity, alignment: .leading)
            
            // Custom gradient checkbox
            Button(action: {
                if !mission.isCompleted {
                    let generator = UIImpactFeedbackGenerator(style: .light)
                    generator.impactOccurred()
                    onCheck()
                }
            }) {
                ZStack {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(mission.isCompleted ? AnyShapeStyle(AppColors.primaryGradient) : AnyShapeStyle(Color.clear))
                        .frame(width: 24, height: 24)
                    
                    if !mission.isCompleted {
                        RoundedRectangle(cornerRadius: 4)
                            .stroke(AppColors.primaryGradient, lineWidth: 2)
                            .frame(width: 24, height: 24)
                    }
                    
                    if mission.isCompleted {
                        Image(systemName: "checkmark")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundStyle(.black)
                    }
                }
            }
            .disabled(mission.isCompleted)
        }
        .padding(.vertical, 8)
    }
}
