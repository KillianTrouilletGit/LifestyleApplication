// MissionViewModel.swift
// Mission display and completion logic

import Foundation
import SwiftData
import SwiftUI

@Observable
class MissionViewModel {
    var dailyMissions: [Mission] = []
    var weeklyMissions: [Mission] = []
    
    private var missionRepo: MissionRepository
    private var modelContext: ModelContext?
    
    init(missionRepo: MissionRepository) {
        self.missionRepo = missionRepo
        loadMissions()
    }
    
    func setup(modelContext: ModelContext) {
        self.modelContext = modelContext
    }
    
    func loadMissions() {
        dailyMissions = missionRepo.getDailyMissions()
        weeklyMissions = missionRepo.getWeeklyMissions()
    }
    
    func completeMission(_ mission: Mission) {
        guard !mission.isCompleted else { return }
        
        // Optimistic UI update
        if let idx = dailyMissions.firstIndex(where: { $0.id == mission.id }) {
            dailyMissions[idx].isCompleted = true
        }
        if let idx = weeklyMissions.firstIndex(where: { $0.id == mission.id }) {
            weeklyMissions[idx].isCompleted = true
        }
        
        // Persist
        missionRepo.completeMission(mission)
        
        // Add XP to user
        addXpToUser(xp: mission.reward)
    }
    
    private func addXpToUser(xp: Int) {
        guard let context = modelContext else { return }
        
        let descriptor = FetchDescriptor<UserModel>()
        guard let users = try? context.fetch(descriptor),
              let user = users.first else { return }
        
        user.xp += xp
        var xpForNextLevel = 100 * user.level * user.level
        while user.xp >= xpForNextLevel {
            user.xp -= xpForNextLevel
            user.level += 1
            xpForNextLevel = 100 * user.level * user.level
        }
        
        try? context.save()
    }
}
