// TrainingViewModel.swift
// Training program CRUD & active session logic

import Foundation
import SwiftData
import SwiftUI

// MARK: - UI State Types
struct TrainingSetState: Identifiable {
    let id = UUID()
    var reps: String = ""
    var weight: String = ""
    var previousReps: Int = 0
    var previousWeight: Float = 0
    var exerciseId: String = ""
    var setNumber: Int = 0
}

struct UiSession: Identifiable {
    let id = UUID()
    var name: String = ""
    var exercises: [UiExercise] = []
}

struct UiExercise: Identifiable {
    let id = UUID()
    var name: String = ""
    var sets: String = ""
}

// MARK: - TrainingViewModel
@Observable
class TrainingViewModel {
    var programs: [ProgramModel] = []
    var sessions: [SessionModel] = []
    var currentExercises: [ExerciseModel] = []
    var currentExerciseIndex: Int = 0
    var currentSets: [TrainingSetState] = []
    var sessionFinished: Bool = false
    
    private var modelContext: ModelContext?
    private var currentTrainingSession: TrainingSessionEntry?
    
    func setup(modelContext: ModelContext) {
        self.modelContext = modelContext
    }
    
    // MARK: - Program CRUD
    func loadPrograms() {
        guard let context = modelContext else { return }
        let descriptor = FetchDescriptor<ProgramModel>()
        programs = (try? context.fetch(descriptor)) ?? []
    }
    
    func loadSessions() {
        guard let context = modelContext else { return }
        let descriptor = FetchDescriptor<SessionModel>()
        sessions = (try? context.fetch(descriptor)) ?? []
    }
    
    func saveProgram(name: String, newSessions: [UiSession]) {
        guard let context = modelContext else { return }
        
        let program = ProgramModel(name: name)
        context.insert(program)
        
        for uiSession in newSessions {
            let session = SessionModel(name: uiSession.name, program: program)
            context.insert(session)
            program.sessions.append(session)
            
            for uiExercise in uiSession.exercises {
                let sets = Int(uiExercise.sets) ?? 1
                let exercise = ExerciseModel(name: uiExercise.name, sets: sets, session: session)
                context.insert(exercise)
                session.exercises.append(exercise)
            }
        }
        
        try? context.save()
        loadPrograms()
    }
    
    func deleteProgram(_ program: ProgramModel) {
        guard let context = modelContext else { return }
        context.delete(program)
        try? context.save()
        loadPrograms()
    }
    
    // MARK: - Flexibility & Endurance
    func saveFlexibilityTraining(duration: TimeInterval) {
        guard let context = modelContext else { return }
        let entry = FlexibilityEntry(date: .now, duration: duration)
        context.insert(entry)
        try? context.save()
    }
    
    func saveEnduranceTraining(duration: TimeInterval, distance: Float) {
        guard let context = modelContext else { return }
        let entry = EnduranceEntry(date: .now, duration: duration, distance: distance)
        context.insert(entry)
        try? context.save()
    }
    
    // MARK: - Active Session
    func startSession(_ session: SessionModel) {
        guard let context = modelContext else { return }
        
        sessionFinished = false
        
        // Create training session record
        let trainingSession = TrainingSessionEntry(
            name: session.name,
            sessionId: session.persistentModelID.hashValue.description,
            startTime: .now
        )
        context.insert(trainingSession)
        try? context.save()
        currentTrainingSession = trainingSession
        
        // Load exercises
        currentExercises = session.exercises
        currentExerciseIndex = 0
        
        if let first = currentExercises.first {
            loadSetsForExercise(first)
        }
    }
    
    private func loadSetsForExercise(_ exercise: ExerciseModel) {
        var newSets: [TrainingSetState] = []
        for i in 0..<exercise.sets {
            newSets.append(TrainingSetState(
                reps: "",
                weight: "",
                previousReps: 0,
                previousWeight: 0,
                exerciseId: exercise.persistentModelID.hashValue.description,
                setNumber: i
            ))
        }
        currentSets = newSets
    }
    
    func saveCurrentSetState(_ sets: [TrainingSetState]) {
        currentSets = sets
    }
    
    func nextExercise() {
        guard let context = modelContext, let trainingSession = currentTrainingSession else { return }
        
        // Save current sets
        for setState in currentSets {
            let trainingSet = TrainingSetEntry(
                exerciseId: setState.exerciseId,
                reps: Int(setState.reps) ?? 0,
                weight: Float(setState.weight) ?? 0,
                trainingSession: trainingSession
            )
            context.insert(trainingSet)
            trainingSession.trainingSets.append(trainingSet)
        }
        
        if currentExerciseIndex < currentExercises.count - 1 {
            currentExerciseIndex += 1
            let nextExercise = currentExercises[currentExerciseIndex]
            loadSetsForExercise(nextExercise)
        } else {
            // Session complete
            trainingSession.endTime = .now
            try? context.save()
            sessionFinished = true
        }
    }
}
