// HealthViewModel.swift
// Handles Water, Sleep, and Nutrition data

import Foundation
import SwiftData
import SwiftUI

@Observable
class HealthViewModel {
    var totalWaterToday: Float = 0
    var lastLoggedMeal: MealEntry? = nil
    var dailyBalanceIndex: Double = 0.0
    
    private var modelContext: ModelContext?
    
    func setup(modelContext: ModelContext) {
        self.modelContext = modelContext
        calculateTotalWaterForToday()
        calculateDailyBalanceIndex()
    }
    
    // MARK: - Water
    func saveWater(amount: Float) {
        guard let context = modelContext else { return }
        let entry = WaterEntry(date: .now, amount: amount)
        context.insert(entry)
        try? context.save()
        calculateTotalWaterForToday()
    }
    
    func calculateTotalWaterForToday() {
        guard let context = modelContext else { return }
        
        let calendar = Calendar.current
        let todayStart = calendar.startOfDay(for: .now)
        let todayEnd = calendar.date(byAdding: .day, value: 1, to: todayStart)!
        
        let descriptor = FetchDescriptor<WaterEntry>(
            predicate: #Predicate { entry in
                entry.date >= todayStart && entry.date < todayEnd
            }
        )
        let entries = (try? context.fetch(descriptor)) ?? []
        totalWaterToday = entries.reduce(0) { $0 + $1.amount }
    }
    
    // MARK: - Sleep
    func saveSleep(duration: String) {
        guard let context = modelContext else { return }
        let entry = SleepEntry(date: .now, duration: duration)
        context.insert(entry)
        try? context.save()
    }
    
    // MARK: - Nutrition
    func saveMeal(_ meal: MealEntry) {
        guard let context = modelContext else { return }
        context.insert(meal)
        try? context.save()
        lastLoggedMeal = meal
        calculateDailyBalanceIndex()
    }
    
    func calculateDailyBalanceIndex() {
        guard let context = modelContext else { return }
        
        let calendar = Calendar.current
        let todayStart = calendar.startOfDay(for: .now)
        let todayEnd = calendar.date(byAdding: .day, value: 1, to: todayStart)!
        
        let descriptor = FetchDescriptor<MealEntry>(
            predicate: #Predicate { entry in
                entry.date >= todayStart && entry.date < todayEnd
            }
        )
        let meals = (try? context.fetch(descriptor)) ?? []
        
        if meals.isEmpty {
            dailyBalanceIndex = 0
        } else {
            dailyBalanceIndex = meals.map(\.balanceIndex).reduce(0, +) / Double(meals.count)
        }
    }
    
    // MARK: - Balance Calculation
    static func calculateBalance(protein: Double, carbs: Double, fat: Double) -> Double {
        let totalCal = (protein * 4) + (carbs * 4) + (fat * 9)
        guard totalCal > 0 else { return 0.5 }
        
        let pRatio = (protein * 4) / totalCal
        let cRatio = (carbs * 4) / totalCal
        let fRatio = (fat * 9) / totalCal
        
        // Target: P=0.3, C=0.4, F=0.3
        let diff = abs(pRatio - 0.3) + abs(cRatio - 0.4) + abs(fRatio - 0.3)
        return max(0, min(1, 1.0 - (diff / 1.5)))
    }
}
