// UserProfileScreen.swift
import SwiftUI
import SwiftData

struct UserProfileScreen: View {
    @State private var viewModel = UserViewModel()
    @Environment(\.dismiss) private var dismiss
    let modelContext: ModelContext
    
    var body: some View {
        ZStack {
            AmbientBackground()
            VStack(spacing: 0) {
                OperatorHeader(subtitle: "Identity", title: "Operator Profile")
                Spacer().frame(height: 24)
                
                // Profile image placeholder
                Circle()
                    .stroke(AppColors.primaryAccent, lineWidth: 2)
                    .frame(width: 120, height: 120)
                    .overlay(
                        Image(systemName: "person.fill")
                            .font(.system(size: 48))
                            .foregroundStyle(AppColors.primaryAccent.opacity(0.5))
                    )
                
                Spacer().frame(height: 16)
                Text(viewModel.user?.name ?? "UNKNOWN OPERATOR")
                    .font(AppTypography.headlineSmall)
                    .foregroundStyle(AppColors.hologramText)
                
                Spacer().frame(height: 8)
                Text("LEVEL: \(viewModel.user?.level ?? 1)")
                    .font(AppTypography.bodyLarge)
                    .foregroundStyle(AppColors.telemetryGreen)
                
                Spacer().frame(height: 24)
                
                NavigationLink(value: "modify_user") {
                    JuicyButton(text: "MODIFY INFORMATION", action: {}).allowsHitTesting(false)
                }
                
                Spacer().frame(height: 32)
                
                let maxXp = viewModel.calculateXpForNextLevel(viewModel.user?.level ?? 1)
                let currentXp = viewModel.user?.xp ?? 0
                let progress = maxXp > 0 ? Float(currentXp) / Float(maxXp) : 0
                
                HStack {
                    Text("XP PROGRESS: \(currentXp) / \(maxXp)")
                        .font(AppTypography.labelSmall)
                        .foregroundStyle(Color.gray)
                    Spacer()
                }
                Spacer().frame(height: 8)
                ProgressView(value: Double(progress))
                    .tint(AppColors.primaryAccent)
                
                Spacer()
                JuicyButton(text: "BACK", action: { dismiss() })
            }
            .padding(DesignSystem.padding)
        }
        .navigationBarBackButtonHidden(true)
        .toolbar { ToolbarItem(placement: .navigationBarLeading) { Button(action: { dismiss() }) { Image(systemName: "chevron.left").foregroundStyle(AppColors.primaryAccent) } } }
        .navigationDestination(for: String.self) { dest in
            if dest == "modify_user" { ModifyUserInfoScreen(modelContext: modelContext) }
        }
        .onAppear { viewModel.setup(modelContext: modelContext) }
    }
}
