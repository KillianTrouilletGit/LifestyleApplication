// SleepScreen.swift
// Sleep duration logging with time picker

import SwiftUI
import SwiftData

struct SleepScreenView: View {
    @State private var viewModel = HealthViewModel()
    @State private var duration = ""
    @State private var selectedHour = 8
    @State private var selectedMinute = 0
    @State private var showPicker = false
    @Environment(\.dismiss) private var dismiss
    
    let modelContext: ModelContext
    
    var body: some View {
        ZStack {
            AmbientBackground()
            
            VStack(alignment: .leading, spacing: 0) {
                OperatorHeader(subtitle: "Recovery Module", title: "Sleep Monitor")
                
                Spacer().frame(height: 32)
                
                Text("ENTER DURATION (HH:MM)")
                    .font(AppTypography.labelMedium)
                    .foregroundStyle(AppColors.secondaryAccent)
                
                Spacer().frame(height: 8)
                
                JuicyInput(
                    placeholder: "08:00",
                    text: $duration,
                    isReadOnly: true,
                    onTap: { showPicker = true }
                )
                
                Spacer().frame(height: 24)
                
                JuicyButton(text: "LOG REST CYCLE", action: {
                    if !duration.isEmpty {
                        viewModel.saveSleep(duration: duration)
                        dismiss()
                    }
                })
                
                Spacer()
                
                JuicyButton(text: "RETURN", action: { dismiss() })
            }
            .padding(DesignSystem.padding)
        }
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: { dismiss() }) {
                    Image(systemName: "chevron.left")
                        .foregroundStyle(AppColors.primaryAccent)
                }
            }
        }
        .sheet(isPresented: $showPicker) {
            VStack {
                HStack {
                    Picker("Hours", selection: $selectedHour) {
                        ForEach(0..<24) { h in
                            Text("\(h)h").tag(h)
                        }
                    }
                    .pickerStyle(.wheel)
                    .frame(width: 100)
                    
                    Picker("Minutes", selection: $selectedMinute) {
                        ForEach(0..<60) { m in
                            Text(String(format: "%02dm", m)).tag(m)
                        }
                    }
                    .pickerStyle(.wheel)
                    .frame(width: 100)
                }
                .padding()
                
                JuicyButton(text: "CONFIRM", action: {
                    duration = String(format: "%02d:%02d", selectedHour, selectedMinute)
                    showPicker = false
                })
                .padding(.horizontal)
                .padding(.bottom)
            }
            .presentationDetents([.height(300)])
            .background(Color.black)
        }
        .onAppear {
            viewModel.setup(modelContext: modelContext)
        }
    }
}
