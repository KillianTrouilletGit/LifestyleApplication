// PlanningScreen.swift
// Simplified planning screen (no Google Calendar API on iOS — uses EventKit placeholder)
import SwiftUI

struct PlanningScreen: View {
    @Environment(\.dismiss) private var dismiss
    
    var body: some View {
        ZStack {
            AmbientBackground()
            VStack(spacing: 0) {
                OperatorHeader(subtitle: "Calendar Link", title: "Weekly Protocol")
                
                Spacer()
                
                VStack(spacing: 16) {
                    Image(systemName: "calendar.badge.clock")
                        .font(.system(size: 64))
                        .foregroundStyle(AppColors.primaryGradient)
                    
                    Text("CALENDAR INTEGRATION")
                        .font(AppTypography.titleMedium)
                        .foregroundStyle(AppColors.primaryAccent)
                    
                    Text("Connect to Apple Calendar or import your schedule to view weekly events here.")
                        .font(AppTypography.bodyMedium)
                        .foregroundStyle(Color.gray)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }
                
                Spacer()
                
                JuicyButton(text: "RETURN TO DASHBOARD", action: { dismiss() })
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
    }
}
