// SplashScreen.swift
// Boot sequence animation matching Android's terminal-style splash

import SwiftUI

struct SplashScreen: View {
    let onAnimationFinished: () -> Void
    
    @State private var step = 0
    
    private let logs = [
        "> INITIALIZING BOOT SEQUENCE...",
        "> [OK] MOUNTING CORE MODULES",
        "> [OK] NEURAL INTERFACE SYNCED",
        "> [OK] CALIBRATING BIOMETRICS",
        "> SYSTEM ONLINE. WELCOME OPERATOR."
    ]
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            VStack(spacing: 0) {
                Text("OPERATOR OS")
                    .font(AppTypography.displayMedium)
                    .foregroundStyle(AppColors.neonCyan)
                    .fontWeight(.bold)
                    .tracking(4)
                
                Spacer().frame(height: 8)
                
                Rectangle()
                    .fill(AppColors.neonMagenta.opacity(0.5))
                    .frame(height: 2)
                    .frame(maxWidth: UIScreen.main.bounds.width * 0.6)
                
                Spacer().frame(height: 24)
                
                // Log sequence
                VStack(alignment: .leading, spacing: 4) {
                    ForEach(Array(logs.prefix(step).enumerated()), id: \.offset) { index, log in
                        Text(log)
                            .font(.system(size: 11, weight: .medium, design: .monospaced))
                            .foregroundStyle(index == step - 1 ? AppColors.primaryAccent : Color.gray)
                    }
                }
                .frame(maxWidth: UIScreen.main.bounds.width * 0.8, alignment: .leading)
            }
            .padding(32)
        }
        .task {
            for i in logs.indices {
                try? await Task.sleep(nanoseconds: 500_000_000) // 500ms
                withAnimation(.easeIn(duration: 0.2)) {
                    step = i + 1
                }
            }
            try? await Task.sleep(nanoseconds: 800_000_000) // 800ms
            onAnimationFinished()
        }
    }
}

#Preview {
    SplashScreen(onAnimationFinished: {})
}
