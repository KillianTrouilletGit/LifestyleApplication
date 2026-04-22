// NutritionScreen.swift
import SwiftUI
import SwiftData

struct NutritionScreen: View {
    @State private var viewModel = HealthViewModel()
    @State private var foodQuery = ""
    @State private var quantity = ""
    @Environment(\.dismiss) private var dismiss
    let modelContext: ModelContext
    
    var body: some View {
        ZStack {
            AmbientBackground()
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    OperatorHeader(subtitle: "Fuel Management", title: "Nutrition Monitor")
                    
                    JuicyCard(action: {}) {
                        VStack(alignment: .leading, spacing: 12) {
                            Text("MANUAL ENTRY").font(AppTypography.titleMedium).foregroundStyle(AppColors.primaryAccent)
                            JuicyInput(placeholder: "FOOD NAME", text: $foodQuery)
                            JuicyInput(placeholder: "CALORIES (KCAL)", text: $quantity, keyboardType: .decimalPad)
                            JuicyButton(text: "LOG MEAL", action: {
                                let cal = Double(quantity) ?? 0
                                let meal = MealEntry(calories: cal, balanceIndex: 0.5)
                                viewModel.saveMeal(meal)
                                foodQuery = ""; quantity = ""
                            })
                        }
                    }
                    
                    if let meal = viewModel.lastLoggedMeal {
                        JuicyCard(action: {}) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("LAST LOGGED MEAL").font(AppTypography.labelMedium).foregroundStyle(AppColors.secondaryAccent)
                                HStack { Text("Calories").foregroundStyle(AppColors.hologramText); Spacer(); Text(String(format: "%.1f", meal.calories)).foregroundStyle(AppColors.hologramText) }.font(AppTypography.bodySmall)
                                HStack { Text("Protein").foregroundStyle(AppColors.hologramText); Spacer(); Text(String(format: "%.1f g", meal.protein)).foregroundStyle(AppColors.hologramText) }.font(AppTypography.bodySmall)
                            }
                        }
                    }
                    
                    JuicyButton(text: "RETURN", action: { dismiss() })
                }
                .padding(DesignSystem.padding)
            }
        }
        .navigationBarBackButtonHidden(true)
        .toolbar { ToolbarItem(placement: .navigationBarLeading) { Button(action: { dismiss() }) { Image(systemName: "chevron.left").foregroundStyle(AppColors.primaryAccent) } } }
        .onAppear { viewModel.setup(modelContext: modelContext) }
    }
}
