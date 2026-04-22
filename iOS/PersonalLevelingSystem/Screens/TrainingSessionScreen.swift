// TrainingSessionScreen.swift
import SwiftUI

struct TrainingSessionScreen: View {
    @Bindable var viewModel: TrainingViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var elapsedSeconds: Int = 0
    @State private var timer: Timer?
    
    var body: some View {
        ZStack {
            AmbientBackground()
            
            if viewModel.currentExercises.isEmpty {
                VStack {
                    Text("No Exercises Found or Loading...")
                        .foregroundStyle(AppColors.primaryAccent)
                    JuicyButton(text: "GO BACK", action: { dismiss() })
                        .padding(.top, 32)
                }
            } else {
                let exercise = viewModel.currentExercises[min(viewModel.currentExerciseIndex, viewModel.currentExercises.count - 1)]
                
                VStack(spacing: 0) {
                    // Timer
                    Text(formatTime(elapsedSeconds))
                        .font(.system(size: 57, weight: .bold, design: .default))
                        .foregroundStyle(AppColors.hologramText)
                        .padding(.vertical, 24)
                    
                    Text(exercise.name.uppercased())
                        .font(AppTypography.headlineSmall)
                        .foregroundStyle(AppColors.primaryAccent)
                        .fontWeight(.bold)
                    
                    Spacer().frame(height: 24)
                    
                    ScrollView {
                        VStack(spacing: 8) {
                            ForEach(viewModel.currentSets.indices, id: \.self) { index in
                                TrainingSetRow(
                                    setNumber: index + 1,
                                    state: viewModel.currentSets[index],
                                    onRepsChange: { val in
                                        var sets = viewModel.currentSets
                                        sets[index].reps = val
                                        viewModel.saveCurrentSetState(sets)
                                    },
                                    onWeightChange: { val in
                                        var sets = viewModel.currentSets
                                        sets[index].weight = val
                                        viewModel.saveCurrentSetState(sets)
                                    }
                                )
                            }
                            
                            Spacer().frame(height: 32)
                            
                            let isLast = viewModel.currentExerciseIndex >= viewModel.currentExercises.count - 1
                            JuicyButton(text: isLast ? "FINISH SESSION" : "NEXT EXERCISE", action: { viewModel.nextExercise() })
                            Spacer().frame(height: 16)
                            JuicyButton(text: "END SESSION", action: { dismiss() })
                        }
                    }
                }
                .padding(DesignSystem.padding)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar { ToolbarItem(placement: .navigationBarLeading) { Button(action: { dismiss() }) { Image(systemName: "chevron.left").foregroundStyle(AppColors.primaryAccent) } } }
        .onAppear { startTimer() }
        .onDisappear { timer?.invalidate() }
        .onChange(of: viewModel.sessionFinished) { _, finished in
            if finished { dismiss() }
        }
    }
    
    private func startTimer() {
        timer = Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { _ in
            elapsedSeconds += 1
        }
    }
    
    private func formatTime(_ totalSeconds: Int) -> String {
        let h = totalSeconds / 3600
        let m = (totalSeconds % 3600) / 60
        let s = totalSeconds % 60
        return String(format: "%02d:%02d:%02d", h, m, s)
    }
}

struct TrainingSetRow: View {
    let setNumber: Int
    let state: TrainingSetState
    let onRepsChange: (String) -> Void
    let onWeightChange: (String) -> Void
    
    @State private var reps: String = ""
    @State private var weight: String = ""
    
    var body: some View {
        HStack(spacing: 8) {
            Text("SET \(setNumber)")
                .font(AppTypography.titleMedium)
                .foregroundStyle(AppColors.primaryAccent)
                .frame(width: 60, alignment: .leading)
            
            if state.previousReps > 0 {
                Text("PREV: \(state.previousReps)x\(String(format: "%.0f", state.previousWeight))kg")
                    .font(AppTypography.bodyMedium)
                    .foregroundStyle(Color.gray)
                    .frame(maxWidth: .infinity)
            } else {
                Spacer()
            }
            
            TextField("REPS", text: $reps)
                .font(AppTypography.bodyMedium)
                .foregroundStyle(AppColors.hologramText)
                .keyboardType(.numberPad)
                .frame(width: 60)
                .padding(8)
                .overlay(RoundedRectangle(cornerRadius: 6).stroke(AppColors.primaryAccent.opacity(0.4)))
                .onChange(of: reps) { _, val in onRepsChange(val) }
            
            TextField("KG", text: $weight)
                .font(AppTypography.bodyMedium)
                .foregroundStyle(AppColors.hologramText)
                .keyboardType(.decimalPad)
                .frame(width: 60)
                .padding(8)
                .overlay(RoundedRectangle(cornerRadius: 6).stroke(AppColors.primaryAccent.opacity(0.4)))
                .onChange(of: weight) { _, val in onWeightChange(val) }
        }
        .onAppear { reps = state.reps; weight = state.weight }
    }
}
