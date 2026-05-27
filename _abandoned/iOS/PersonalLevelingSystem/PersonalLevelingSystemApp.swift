// PersonalLevelingSystemApp.swift
// Main App Entry Point

import SwiftUI
import SwiftData

@main
struct PersonalLevelingSystemApp: App {
    let modelContainer: ModelContainer
    
    init() {
        do {
            let schema = Schema([
                UserModel.self,
                WaterEntry.self,
                SleepEntry.self,
                MealEntry.self,
                TrainingSessionEntry.self,
                TrainingSetEntry.self,
                ExerciseModel.self,
                SessionModel.self,
                ProgramModel.self,
                EnduranceEntry.self,
                FlexibilityEntry.self
            ])
            let modelConfiguration = ModelConfiguration(schema: schema, isStoredInMemoryOnly: false)
            modelContainer = try ModelContainer(for: schema, configurations: [modelConfiguration])
            
            // Create default user if needed
            Task { @MainActor in
                let context = modelContainer.mainContext
                let descriptor = FetchDescriptor<UserModel>()
                let users = try? context.fetch(descriptor)
                if users?.isEmpty ?? true {
                    let defaultUser = UserModel(name: "User", xp: 0, level: 1, weight: 0, height: 0, dateOfBirth: "NONE")
                    context.insert(defaultUser)
                    try? context.save()
                }
            }
        } catch {
            fatalError("Could not initialize ModelContainer: \(error)")
        }
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .preferredColorScheme(.dark)
        }
        .modelContainer(modelContainer)
    }
}
