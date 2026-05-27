// MainScreen.swift
// Dashboard with 2-column grid and Performance Carousel

import SwiftUI
import SwiftData

// MARK: - Dashboard Item
struct DashboardItem: Identifiable {
    let id: String
    let title: String
    let systemIcon: String // SF Symbol name
}

// MARK: - MainScreen
struct MainScreen: View {
    @Environment(\.modelContext) private var modelContext
    @State private var performanceVM = PerformanceViewModel()
    @State private var healthVM = HealthViewModel()
    @State private var missionRepo = MissionRepository()
    
    private let items: [DashboardItem] = [
        DashboardItem(id: "missions", title: "Missions", systemIcon: "target"),
        DashboardItem(id: "training", title: "Training", systemIcon: "dumbbell.fill"),
        DashboardItem(id: "nutrition", title: "Nutrition", systemIcon: "fork.knife"),
        DashboardItem(id: "sleep", title: "Sleep", systemIcon: "moon.fill"),
        DashboardItem(id: "water", title: "Hydration", systemIcon: "drop.fill"),
        DashboardItem(id: "planning", title: "Planning", systemIcon: "calendar"),
        DashboardItem(id: "profile", title: "Profile", systemIcon: "person.fill"),
        DashboardItem(id: "settings", title: "Settings", systemIcon: "gearshape.fill"),
    ]
    
    private let columns = [
        GridItem(.flexible(), spacing: 16),
        GridItem(.flexible(), spacing: 16)
    ]
    
    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                OperatorHeader(subtitle: "Operator OS", title: "System Dashboard")
                
                Spacer().frame(height: 16)
                
                // Performance Carousel
                PerformanceCarouselV2(state: performanceVM.uiState)
                
                Spacer().frame(height: 16)
                
                // Dashboard Grid
                LazyVGrid(columns: columns, spacing: 16) {
                    ForEach(items) { item in
                        NavigationLink(value: item.id) {
                            DashboardCard(item: item)
                        }
                    }
                }
            }
            .padding(DesignSystem.padding)
        }
        .background(Color.clear)
        .navigationDestination(for: String.self) { destination in
            destinationView(for: destination)
        }
        .onAppear {
            performanceVM.setup(modelContext: modelContext, missionRepo: missionRepo)
            healthVM.setup(modelContext: modelContext)
        }
    }
    
    @ViewBuilder
    private func destinationView(for id: String) -> some View {
        switch id {
        case "missions":
            MissionsListScreen(missionRepo: missionRepo, modelContext: modelContext)
        case "training":
            TrainingScreen(modelContext: modelContext)
        case "nutrition":
            NutritionScreen(modelContext: modelContext)
        case "sleep":
            SleepScreenView(modelContext: modelContext)
        case "water":
            WaterScreenView(modelContext: modelContext)
        case "planning":
            PlanningScreen()
        case "profile":
            UserProfileScreen(modelContext: modelContext)
        case "settings":
            ModifyUserInfoScreen(modelContext: modelContext)
        default:
            Text("UNKNOWN DESTINATION")
                .foregroundStyle(AppColors.alertOrange)
        }
    }
}

// MARK: - Dashboard Card
struct DashboardCard: View {
    let item: DashboardItem
    
    var body: some View {
        JuicyCard(action: {}) {
            VStack(spacing: 12) {
                Image(systemName: item.systemIcon)
                    .font(.system(size: 32))
                    .foregroundStyle(AppColors.primaryGradient)
                
                Text(item.title.uppercased())
                    .font(AppTypography.labelLarge)
                    .foregroundStyle(.white)
            }
            .frame(maxWidth: .infinity, minHeight: 80)
        }
    }
}

#Preview {
    NavigationStack {
        MainScreen()
    }
    .preferredColorScheme(.dark)
}
