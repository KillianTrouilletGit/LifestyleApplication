// PerformanceCarousel.swift
// Horizontal paging carousel for Level, Missions, Training Volume, Bio-Metrics

import SwiftUI

struct PerformanceCarousel: View {
    let state: PerformanceState
    
    var body: some View {
        VStack(spacing: 8) {
            TabView {
                LevelProgressCard(state: state)
                MissionStatsCard(state: state)
                TrainingFrequencyCard(state: state)
                HealthOverviewCard(state: state)
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .frame(height: 180)
            
            // Custom page indicators
            HStack(spacing: 4) {
                // Note: TabView doesn't expose current index easily,
                // so we use the built-in dots but styled via init
            }
        }
        .frame(height: 200)
    }
}

// MARK: - Use a pager with explicit index for custom indicators
struct PerformanceCarouselV2: View {
    let state: PerformanceState
    @State private var currentPage = 0
    
    var body: some View {
        VStack(spacing: 8) {
            TabView(selection: $currentPage) {
                LevelProgressCard(state: state).tag(0)
                MissionStatsCard(state: state).tag(1)
                TrainingFrequencyCard(state: state).tag(2)
                HealthOverviewCard(state: state).tag(3)
            }
            .tabViewStyle(.page(indexDisplayMode: .never))
            .frame(height: 180)
            
            // Custom dot indicators
            HStack(spacing: 4) {
                ForEach(0..<4, id: \.self) { index in
                    Circle()
                        .fill(currentPage == index ? AppColors.neonCyan : Color(.darkGray))
                        .frame(width: 6, height: 6)
                }
            }
        }
        .frame(height: 200)
    }
}

// MARK: - Level Progress Card
struct LevelProgressCard: View {
    let state: PerformanceState
    
    private var progress: Float {
        state.requiredXp > 0 ? state.currentXp / state.requiredXp : 0
    }
    
    var body: some View {
        JuicyCard(action: {}) {
            VStack(spacing: 4) {
                Text("CURRENT STATUS")
                    .font(AppTypography.labelMedium)
                    .foregroundStyle(AppColors.primaryAccent)
                
                Text("LEVEL \(state.level)")
                    .font(AppTypography.displayMedium)
                    .foregroundStyle(AppColors.neonMagenta)
                
                Spacer().frame(height: 8)
                
                Text("XP: \(Int(state.currentXp)) / \(Int(state.requiredXp))")
                    .font(AppTypography.bodySmall)
                    .foregroundStyle(Color(.lightGray))
                
                ProgressView(value: Double(progress))
                    .tint(AppColors.neonCyan)
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, 32)
                    .padding(.top, 4)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .padding(.horizontal, 16)
    }
}

// MARK: - Mission Stats Card
struct MissionStatsCard: View {
    let state: PerformanceState
    
    private var efficiencyPercent: Int {
        Int(state.missionEfficiency * 100)
    }
    
    var body: some View {
        JuicyCard(action: {}) {
            VStack(spacing: 4) {
                Text("MISSION EFFICIENCY")
                    .font(AppTypography.labelMedium)
                    .foregroundStyle(AppColors.primaryAccent)
                
                Text("\(efficiencyPercent)%")
                    .font(AppTypography.displayMedium)
                    .foregroundStyle(AppColors.neonCyan)
                
                Text("Daily Objectives: \(state.dailyMissionsCompleted)/\(state.totalDailyMissions)")
                    .font(AppTypography.bodySmall)
                    .foregroundStyle(Color(.lightGray))
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .padding(.horizontal, 16)
    }
}

// MARK: - Training Frequency Card (Weekly Volume Bar Chart)
struct TrainingFrequencyCard: View {
    let state: PerformanceState
    
    private var activeDays: Int {
        state.weeklyTrainingFrequency.filter { $0 > 0.1 }.count
    }
    
    private var maxVolume: Float {
        let m = state.weeklyTrainingFrequency.max() ?? 0
        return m > 0 ? m : 1
    }
    
    var body: some View {
        JuicyCard(action: {}) {
            VStack(spacing: 0) {
                HStack {
                    Text("WEEKLY VOLUME")
                        .font(AppTypography.labelMedium)
                        .foregroundStyle(AppColors.primaryAccent)
                    Spacer()
                    Text("\(activeDays)/7 ACT")
                        .font(AppTypography.labelSmall)
                        .foregroundStyle(.white)
                }
                
                Spacer().frame(height: 16)
                
                HStack(alignment: .bottom, spacing: 0) {
                    ForEach(0..<7, id: \.self) { index in
                        let rawValue = index < state.weeklyTrainingFrequency.count ? state.weeklyTrainingFrequency[index] : 0
                        let dayLabel = index < state.weeklyTrainingLabels.count ? state.weeklyTrainingLabels[index] : "-"
                        let heightRatio = CGFloat(min(max(rawValue / maxVolume, 0.05), 1.0))
                        
                        VStack(spacing: 4) {
                            if rawValue > 0.05 {
                                Text(String(format: "%.1f", rawValue))
                                    .font(.system(size: 11, weight: .regular, design: .monospaced))
                                    .foregroundStyle(AppColors.neonCyan)
                            }
                            
                            RoundedRectangle(cornerRadius: 2)
                                .fill(rawValue > 0.05
                                      ? AnyShapeStyle(AppColors.primaryGradient)
                                      : AnyShapeStyle(Color(.darkGray).opacity(0.3)))
                                .frame(width: 16, height: 80 * heightRatio)
                            
                            Spacer().frame(height: 6)
                            
                            Text(dayLabel)
                                .font(.system(size: 12, weight: .bold, design: .monospaced))
                                .foregroundStyle(Color(.lightGray))
                        }
                        .frame(maxWidth: .infinity)
                    }
                }
                .frame(height: 100)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(.horizontal, 8)
        }
        .padding(.horizontal, 16)
    }
}

// MARK: - Health Overview Card (Bio-Metrics)
struct HealthOverviewCard: View {
    let state: PerformanceState
    
    var body: some View {
        JuicyCard(action: {}) {
            VStack(spacing: 12) {
                Text("BIO-METRICS")
                    .font(AppTypography.labelMedium)
                    .foregroundStyle(AppColors.primaryAccent)
                
                HStack {
                    BioMetricItem(label: "SLEEP", value: String(format: "%.1fH", state.sleepHours), color: AppColors.neonMagenta)
                    Spacer()
                    BioMetricItem(label: "H2O", value: String(format: "%.1fL", state.waterIntake), color: AppColors.neonCyan)
                    Spacer()
                    BioMetricItem(label: "KCAL", value: "\(state.dailyCalories)", color: .white)
                    Spacer()
                    BioMetricItem(label: "BAL", value: String(format: "%.2f", state.dailyBalanceIndex), color: AppColors.cyberCyan)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .padding(DesignSystem.padding)
        }
        .padding(.horizontal, 16)
    }
}

// MARK: - BioMetric Item
struct BioMetricItem: View {
    let label: String
    let value: String
    let color: Color
    
    var body: some View {
        VStack(spacing: 2) {
            Text(label)
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .foregroundStyle(Color.gray)
            
            Text(value)
                .font(AppTypography.titleMedium)
                .fontWeight(.bold)
                .foregroundStyle(color)
        }
    }
}

#Preview {
    ZStack {
        Color.black.ignoresSafeArea()
        PerformanceCarouselV2(state: PerformanceState(
            level: 5,
            currentXp: 350,
            requiredXp: 2500,
            missionEfficiency: 0.7,
            dailyMissionsCompleted: 7,
            totalDailyMissions: 10,
            weeklyTrainingFrequency: [1.5, 0, 2.0, 0, 1.8, 0, 0.5],
            weeklyTrainingLabels: ["L", "M", "M", "J", "V", "S", "D"],
            sleepHours: 7.5,
            waterIntake: 2.1,
            dailyBalanceIndex: 0.78,
            dailyCalories: 2150
        ))
    }
}
