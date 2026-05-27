// ContentView.swift
// Root navigation container with Ambient Background & Splash

import SwiftUI

struct ContentView: View {
    @State private var showSplash = true
    
    var body: some View {
        ZStack {
            if showSplash {
                SplashScreen {
                    withAnimation(.easeInOut(duration: 0.3)) {
                        showSplash = false
                    }
                }
                .transition(.opacity)
            } else {
                ZStack {
                    AmbientBackground()
                        .ignoresSafeArea()
                    
                    NavigationStack {
                        MainScreen()
                            .toolbarBackground(.hidden, for: .navigationBar)
                    }
                    .tint(AppColors.primaryAccent)
                }
                .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.5), value: showSplash)
    }
}

#Preview {
    ContentView()
        .preferredColorScheme(.dark)
}
