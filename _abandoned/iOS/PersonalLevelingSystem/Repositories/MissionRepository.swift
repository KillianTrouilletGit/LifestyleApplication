// MissionRepository.swift
// Mission definitions and persistence via UserDefaults (matching Android SharedPreferences)

import Foundation

class MissionRepository: ObservableObject {
    private let defaults = UserDefaults.standard
    
    // MARK: - Daily Missions
    private(set) var dailyMissions: [Mission] = []
    private(set) var weeklyMissions: [Mission] = []
    
    init() {
        reloadMissions()
    }
    
    func reloadMissions() {
        dailyMissions = [
            Mission(id: "daily_flex", description: "Complete a 15-minutes flexibility training", type: .daily, isCompleted: getStatus("daily_flex"), reward: 50),
            Mission(id: "daily_water", description: "Drink the correct amount of water", type: .daily, isCompleted: getStatus("daily_water"), reward: 30),
            Mission(id: "daily_learn", description: "Practice micro learning for 30 minutes", type: .daily, isCompleted: getStatus("daily_learn"), reward: 40),
            Mission(id: "daily_meditate", description: "Meditate for 10 minutes", type: .daily, isCompleted: getStatus("daily_meditate"), reward: 25),
            Mission(id: "daily_hygiene", description: "Make sure to have proper hygiene", type: .daily, isCompleted: getStatus("daily_hygiene"), reward: 20),
            Mission(id: "daily_sleep", description: "Sleep over 7 hours", type: .daily, isCompleted: getStatus("daily_sleep"), reward: 20),
            Mission(id: "daily_nutrition", description: "Be sure to have a proper nutrition", type: .daily, isCompleted: getStatus("daily_nutrition"), reward: 20),
            Mission(id: "daily_planning", description: "Respect today's planning", type: .daily, isCompleted: getStatus("daily_planning"), reward: 20),
            Mission(id: "daily_planning_2", description: "Fine tune tomorrow's planning", type: .daily, isCompleted: getStatus("daily_planning_2"), reward: 20),
            Mission(id: "daily_appearance", description: "Work on external appearance", type: .daily, isCompleted: getStatus("daily_appearance"), reward: 20),
        ]
        
        weeklyMissions = [
            Mission(id: "weekly_workout", description: "Complete the workout program", type: .weekly, isCompleted: getStatus("weekly_workout"), reward: 100),
            Mission(id: "weekly_planning", description: "Create next week planning", type: .weekly, isCompleted: getStatus("weekly_planning"), reward: 80),
            Mission(id: "weekly_endurance", description: "Run 10 kilometers", type: .weekly, isCompleted: getStatus("weekly_endurance"), reward: 60),
            Mission(id: "weekly_cook", description: "Cook a new recipe", type: .weekly, isCompleted: getStatus("weekly_cook"), reward: 50),
            Mission(id: "weekly_clean", description: "Clean your living space", type: .weekly, isCompleted: getStatus("weekly_clean"), reward: 40),
            Mission(id: "weekly_report", description: "Check the progress of the week", type: .weekly, isCompleted: getStatus("weekly_report"), reward: 40),
            Mission(id: "weekly_weigh", description: "Register new weight", type: .weekly, isCompleted: getStatus("weekly_weigh"), reward: 10),
        ]
    }
    
    func getDailyMissions() -> [Mission] { dailyMissions }
    func getWeeklyMissions() -> [Mission] { weeklyMissions }
    
    func completeMission(_ mission: Mission) {
        setStatus(mission.id, completed: true)
        
        if let idx = dailyMissions.firstIndex(where: { $0.id == mission.id }) {
            dailyMissions[idx].isCompleted = true
        }
        if let idx = weeklyMissions.firstIndex(where: { $0.id == mission.id }) {
            weeklyMissions[idx].isCompleted = true
        }
        
        objectWillChange.send()
    }
    
    func resetDailyMissions() {
        for i in dailyMissions.indices {
            dailyMissions[i].isCompleted = false
            setStatus(dailyMissions[i].id, completed: false)
        }
        objectWillChange.send()
    }
    
    func resetWeeklyMissions() {
        for i in weeklyMissions.indices {
            weeklyMissions[i].isCompleted = false
            setStatus(weeklyMissions[i].id, completed: false)
        }
        objectWillChange.send()
    }
    
    // MARK: - Persistence
    private func getStatus(_ id: String) -> Bool {
        defaults.bool(forKey: "mission_\(id)")
    }
    
    private func setStatus(_ id: String, completed: Bool) {
        defaults.set(completed, forKey: "mission_\(id)")
    }
}
