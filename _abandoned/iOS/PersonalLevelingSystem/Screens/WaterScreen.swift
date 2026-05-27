// WaterScreen.swift
// Water tracking with daily total

import SwiftUI
import SwiftData

struct WaterScreenView: View {
    @State private var viewModel = HealthViewModel()
    @State private var inputAmount = ""
    @Environment(\.dismiss) private var dismiss
    
    let modelContext: ModelContext
    
    var body: some View {
        ZStack {
            AmbientBackground()
            
            VStack(alignment: .leading, spacing: 0) {
                OperatorHeader(subtitle: "Hydration Monitor", title: "H2O Levels")
                
                Spacer().frame(height: 32)
                
                Text("DAILY INTAKE")
                    .font(AppTypography.labelMedium)
                    .foregroundStyle(AppColors.secondaryAccent)
                
                Text(String(format: "%.0f ml", viewModel.totalWaterToday))
                    .font(AppTypography.displayMedium)
                    .foregroundStyle(AppColors.primaryAccent)
                
                Spacer().frame(height: 32)
                
                JuicyInput(placeholder: "AMOUNT (ML)", text: $inputAmount, keyboardType: .decimalPad)
                
                Spacer().frame(height: 16)
                
                JuicyButton(text: "LOG INTAKE", action: {
                    if let amount = Float(inputAmount), amount > 0 {
                        viewModel.saveWater(amount: amount)
                        inputAmount = ""
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
        .onAppear {
            viewModel.setup(modelContext: modelContext)
        }
    }
}
