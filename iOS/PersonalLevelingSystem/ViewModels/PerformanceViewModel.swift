// PerformanceViewModel.swift
// Aggregates data from all sources for the dashboard carousel

import Foundation
import SwiftData
import SwiftUI

// MARK: - Performance State
struct PerformanceState {
    var level: Int = 1
    var currentXp: Float = 0
    var requiredXp: Float = 1000
    var missionEfficiency: Float = 0
    var dailyMissionsCompleted: Int = 0
    var totalDailyMissions: Int = 0
    var weeklyTrainingFrequency: [Float] = Array(repeating: 0, count: 7)
    var weeklyTrainingLabels: [String] = Array(repeating: "", count: 7)
    var sleepHours: Float = 0
    var waterIntake: Float = 0
    var dailyBalanceIndex: Float = 0
    var dailyCalories: Int = 0
}

// MARK: - PerformanceViewModel
@Observable
class PerformanceViewModel {
    var uiState = PerformanceState()
    
    private var modelContext: ModelContext?
    private var missionRepo: MissionRepository?
    
    func setup(modelContext: ModelContext, missionRepo: MissionRepository) {
        self.modelContext = modelContext
        self.missionRepo = missionRepo
        loadPerformanceData()
    }
    
    func loadPerformanceData() {
        guard let context = modelContext else { return }
        
        Task { @MainActor in
            // 1. User Level & XP
            let userDescriptor = FetchDescriptor<UserModel>(sortBy: [SortDescriptor(\.level, order: .reverse)])
            let users = (try? context.fetch(userDescriptor)) ?? []
            let user = users.first
            let level = user?.level ?? 1
            let currentXp = Float(user?.xp ?? 0)
            let requiredXp = Float(100 * level * level)
            
            // 2. Mission Stats
            let dailyMissions = missionRepo?.getDailyMissions() ?? []
            let completedCount = dailyMissions.filter { $0.isCompleted }.count
            let totalCount = dailyMissions.count
            let efficiency: Float = totalCount > 0 ? Float(completedCount) / Float(totalCount) : 0
            
            // 3. Training Frequency (Last 7 Days sliding window)
            let calendar = Calendar.current
            let now = Date()
            
            // End of today
            var endComponents = calendar.dateComponents([.year, .month, .day], from: now)
            endComponents.hour = 23
            endComponents.minute = 59
            endComponents.second = 59
            let endOfWeek = calendar.date(from: endComponents)!
            
            // Start of 7 days ago
            let sixDaysAgo = calendar.date(byAdding: .day, value: -6, to: now)!
            var startComponents = calendar.dateComponents([.year, .month, .day], from: sixDaysAgo)
            startComponents.hour = 0
            startComponents.minute = 0
            startComponents.second = 0
            let startOfWeek = calendar.date(from: startComponents)!
            
            let sessionDescriptor = FetchDescriptor<TrainingSessionEntry>(
                predicate: #Predicate { session in
                    session.startTime >= startOfWeek && session.startTime <= endOfWeek
                }
            )
            let sessions = (try? context.fetch(sessionDescriptor)) ?? []
            
            var frequency: [Float] = Array(repeating: 0, count: 7)
            var labels: [String] = Array(repeating: "", count: 7)
            let dayFormatter = DateFormatter()
            dayFormatter.dateFormat = "E"
            
            // Pre-fill labels
            for i in 0..<7 {
                let day = calendar.date(byAdding: .day, value: i, to: startOfWeek)!
                let dayStr = dayFormatter.string(from: day)
                labels[i] = String(dayStr.prefix(1)).uppercased()
            }
            
            // Group sessions by day index
            for session in sessions {
                let daysDiff = calendar.dateComponents([.day], from: startOfWeek, to: session.startTime).day ?? 0
                if daysDiff >= 0 && daysDiff < 7 {
                    frequency[daysDiff] += session.durationInHours
                }
            }
            
            // 4. Health Data (Today)
            let todayStart = calendar.startOfDay(for: now)
            let todayEnd = calendar.date(byAdding: .day, value: 1, to: todayStart)!
            
            let sleepDescriptor = FetchDescriptor<SleepEntry>(
                predicate: #Predicate { entry in
                    entry.date >= todayStart && entry.date < todayEnd
                }
            )
            let sleepRecords = (try? context.fetch(sleepDescriptor)) ?? []
            let sleepHours: Float = sleepRecords.reduce(0) { total, entry in
                let cleaned = entry.duration.replacingOccurrences(of: "h", with: "").trimmingCharacters(in: .whitespaces)
                return total + (Float(cleaned) ?? 0)
            }
            
            let waterDescriptor = FetchDescriptor<WaterEntry>(
                predicate: #Predicate { entry in
                    entry.date >= todayStart && entry.date < todayEnd
                }
            )
            let waterRecords = (try? context.fetch(waterDescriptor)) ?? []
            let waterIntake = waterRecords.reduce(Float(0)) { $0 + $1.amount } / 1000.0 // ml -> L
            
            // 5. Nutrition Data (Today)
            let mealDescriptor = FetchDescriptor<MealEntry>(
                predicate: #Predicate { entry in
                    entry.date >= todayStart && entry.date < todayEnd
                }
            )
            let meals = (try? context.fetch(mealDescriptor)) ?? []
            let balanceIndex: Float = meals.isEmpty ? 0 : Float(meals.map(\.balanceIndex).reduce(0, +) / Double(meals.count))
            let totalCalories = meals.reduce(0) { $0 + Int($1.calories) }
            
            self.uiState = PerformanceState(
                level: level,
                currentXp: currentXp,
                requiredXp: requiredXp,
                missionEfficiency: efficiency,
                dailyMissionsCompleted: completedCount,
                totalDailyMissions: totalCount,
                weeklyTrainingFrequency: frequency,
                weeklyTrainingLabels: labels,
                sleepHours: sleepHours,
                waterIntake: waterIntake,
                dailyBalanceIndex: balanceIndex,
                dailyCalories: totalCalories
            )
        }
    }
}
