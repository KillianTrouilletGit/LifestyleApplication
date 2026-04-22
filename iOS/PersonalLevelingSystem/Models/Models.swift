// Models.swift
// SwiftData Models — 1:1 mapping from Android Room entities

import Foundation
import SwiftData

// MARK: - User
@Model
final class UserModel {
    var name: String
    var xp: Int
    var level: Int
    var weight: Float
    var height: Float
    var dateOfBirth: String
    
    init(name: String, xp: Int = 0, level: Int = 1, weight: Float = 0, height: Float = 0, dateOfBirth: String = "") {
        self.name = name
        self.xp = xp
        self.level = level
        self.weight = weight
        self.height = height
        self.dateOfBirth = dateOfBirth
    }
}

// MARK: - Water
@Model
final class WaterEntry {
    var date: Date
    var amount: Float // in ml
    
    init(date: Date = .now, amount: Float) {
        self.date = date
        self.amount = amount
    }
}

// MARK: - Sleep
@Model
final class SleepEntry {
    var date: Date
    var duration: String // e.g. "07:30" or "8h"
    
    init(date: Date = .now, duration: String) {
        self.date = date
        self.duration = duration
    }
}

// MARK: - Meal
@Model
final class MealEntry {
    var date: Date
    var time: String
    var calories: Double
    var protein: Double
    var fat: Double
    var carbs: Double
    var fiber: Double
    var balanceIndex: Double
    
    init(date: Date = .now, time: String = "", calories: Double = 0, protein: Double = 0, fat: Double = 0, carbs: Double = 0, fiber: Double = 0, balanceIndex: Double = 0) {
        self.date = date
        self.time = time
        self.calories = calories
        self.protein = protein
        self.fat = fat
        self.carbs = carbs
        self.fiber = fiber
        self.balanceIndex = balanceIndex
    }
}

// MARK: - Program
@Model
final class ProgramModel {
    var name: String
    @Relationship(deleteRule: .cascade) var sessions: [SessionModel] = []
    
    init(name: String) {
        self.name = name
    }
}

// MARK: - Session (blueprint)
@Model
final class SessionModel {
    var name: String
    var program: ProgramModel?
    @Relationship(deleteRule: .cascade) var exercises: [ExerciseModel] = []
    
    init(name: String, program: ProgramModel? = nil) {
        self.name = name
        self.program = program
    }
}

// MARK: - Exercise (blueprint)
@Model
final class ExerciseModel {
    var name: String
    var sets: Int
    var session: SessionModel?
    
    init(name: String, sets: Int, session: SessionModel? = nil) {
        self.name = name
        self.sets = sets
        self.session = session
    }
}

// MARK: - Training Session (actual logged workout)
@Model
final class TrainingSessionEntry {
    var name: String
    var date: Date
    var sessionId: String // reference to blueprint session
    var startTime: Date
    var endTime: Date?
    var weekId: Int
    @Relationship(deleteRule: .cascade) var trainingSets: [TrainingSetEntry] = []
    
    init(name: String, date: Date = .now, sessionId: String, startTime: Date = .now, endTime: Date? = nil, weekId: Int = 0) {
        self.name = name
        self.date = date
        self.sessionId = sessionId
        self.startTime = startTime
        self.endTime = endTime
        self.weekId = weekId
    }
    
    var durationInSeconds: TimeInterval {
        let end = endTime ?? startTime
        return end.timeIntervalSince(startTime)
    }
    
    var durationInHours: Float {
        Float(durationInSeconds / 3600.0)
    }
}

// MARK: - Training Set (actual logged set)
@Model
final class TrainingSetEntry {
    var exerciseId: String
    var reps: Int
    var weight: Float
    var trainingSession: TrainingSessionEntry?
    
    init(exerciseId: String, reps: Int, weight: Float, trainingSession: TrainingSessionEntry? = nil) {
        self.exerciseId = exerciseId
        self.reps = reps
        self.weight = weight
        self.trainingSession = trainingSession
    }
}

// MARK: - Endurance Training
@Model
final class EnduranceEntry {
    var date: Date
    var duration: TimeInterval // in milliseconds (matching Android)
    var distance: Float // km
    
    init(date: Date = .now, duration: TimeInterval, distance: Float) {
        self.date = date
        self.duration = duration
        self.distance = distance
    }
}

// MARK: - Flexibility Training
@Model
final class FlexibilityEntry {
    var date: Date
    var duration: TimeInterval // in milliseconds
    
    init(date: Date = .now, duration: TimeInterval) {
        self.date = date
        self.duration = duration
    }
}

// MARK: - Mission (not persisted in DB, same as Android)
struct Mission: Identifiable {
    let id: String
    let description: String
    let type: MissionType
    var isCompleted: Bool
    let reward: Int
}

enum MissionType {
    case daily
    case weekly
}

// MARK: - Food Analysis (API response)
struct FoodAnalysisResponse: Codable {
    let dish_name: String
    let ingredients: [IngredientResponse]
}

struct IngredientResponse: Codable {
    let name: String
    let estimated_weight_g: Double
    let calories: Double
    let protein_g: Double
    let carbs_g: Double
    let fat_g: Double
    let fiber_g: Double
}
