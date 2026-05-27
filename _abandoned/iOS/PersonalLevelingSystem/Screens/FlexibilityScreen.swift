// FlexibilityScreen.swift
import SwiftUI
import SwiftData

struct FlexibilityScreen: View {
    @State private var viewModel = TrainingViewModel()
    @State private var isRunning = false
    @State private var elapsedMs: TimeInterval = 0
    @State private var startTime: Date? = nil
    @State private var timer: Timer?
    @Environment(\.dismiss) private var dismiss
    let modelContext: ModelContext
    
    private var timeString: String {
        let totalSec = Int(elapsedMs / 1000)
        let h = totalSec / 3600; let m = (totalSec % 3600) / 60; let s = totalSec % 60
        return String(format: "%02d:%02d:%02d", h, m, s)
    }
    
    var body: some View {
        ZStack {
            AmbientBackground()
            VStack(spacing: 0) {
                OperatorHeader(subtitle: "Mobility Protocol", title: "Flexibility")
                Spacer()
                Text(timeString)
                    .font(.system(size: 64, weight: .bold))
                    .foregroundStyle(AppColors.primaryAccent)
                    .monospacedDigit()
                Spacer()
                
                if !isRunning {
                    JuicyButton(text: elapsedMs > 0 ? "RESUME PROTOCOL" : "INITIATE PROTOCOL", action: { start() })
                } else {
                    JuicyButton(text: "PAUSE", action: { pause() })
                }
                Spacer().frame(height: 16)
                JuicyButton(text: "COMPLETE & SAVE", action: {
                    pause(); viewModel.saveFlexibilityTraining(duration: elapsedMs); dismiss()
                }, isEnabled: !isRunning && elapsedMs > 0)
                Spacer().frame(height: 16)
                JuicyButton(text: "ABORT / RETURN", action: { pause(); dismiss() })
            }
            .padding(DesignSystem.padding)
        }
        .navigationBarBackButtonHidden(true)
        .toolbar { ToolbarItem(placement: .navigationBarLeading) { Button(action: { pause(); dismiss() }) { Image(systemName: "chevron.left").foregroundStyle(AppColors.primaryAccent) } } }
        .onAppear { viewModel.setup(modelContext: modelContext) }
        .onDisappear { timer?.invalidate() }
    }
    
    private func start() {
        isRunning = true
        startTime = Date().addingTimeInterval(-elapsedMs / 1000)
        timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { _ in
            if let st = startTime { elapsedMs = Date().timeIntervalSince(st) * 1000 }
        }
    }
    
    private func pause() {
        isRunning = false; timer?.invalidate()
    }
}
