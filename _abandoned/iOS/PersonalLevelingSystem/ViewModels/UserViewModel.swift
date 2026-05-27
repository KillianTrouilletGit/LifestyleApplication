// UserViewModel.swift
// User profile management

import Foundation
import SwiftData
import SwiftUI

@Observable
class UserViewModel {
    var user: UserModel? = nil
    
    private var modelContext: ModelContext?
    
    func setup(modelContext: ModelContext) {
        self.modelContext = modelContext
        loadUser()
    }
    
    func loadUser() {
        guard let context = modelContext else { return }
        let descriptor = FetchDescriptor<UserModel>()
        let users = (try? context.fetch(descriptor)) ?? []
        user = users.first
    }
    
    func insertUser(_ newUser: UserModel) {
        guard let context = modelContext else { return }
        context.insert(newUser)
        try? context.save()
        user = newUser
    }
    
    func updateUser() {
        guard let context = modelContext else { return }
        try? context.save()
    }
    
    func calculateXpForNextLevel(_ level: Int) -> Int {
        return 100 * level * level
    }
    
    func calculateDailyRequiredKcal() -> Double? {
        guard let user = user else { return nil }
        let age = calculateAge(dateOfBirth: user.dateOfBirth)
        let bmr = 88.362 + (13.397 * Double(user.weight)) + (4.799 * Double(user.height)) - (5.677 * Double(age))
        let activityFactor = 1.55 // Moderate
        return bmr * activityFactor
    }
    
    private func calculateAge(dateOfBirth: String) -> Int {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        guard let dob = formatter.date(from: dateOfBirth) else { return 25 }
        
        let calendar = Calendar.current
        let components = calendar.dateComponents([.year], from: dob, to: .now)
        return components.year ?? 25
    }
}
