// AppColors.swift
// Visual Design System: Future Neon / High-Precision

import SwiftUI

// MARK: - Core Color Palette
struct AppColors {
    // Base Colors
    static let spaceBlack = Color(red: 0, green: 0, blue: 0)
    static let neonMagenta = Color(red: 1, green: 0, blue: 1)         // #FF00FF
    static let neonCyan = Color(red: 0, green: 1, blue: 1)            // #00FFFF
    static let neonViolet = Color(red: 0.616, green: 0, blue: 1)      // #9D00FF
    static let alertOrange = Color(red: 1, green: 0.341, blue: 0.133) // #FF5722
    static let telemetryGreen = Color(red: 0, green: 0.902, blue: 0.463) // #00E676
    
    // Glass Surfaces
    static let glassSurfaceStart = Color.white.opacity(0.2)   // 0x33FFFFFF
    static let glassSurfaceEnd = Color.white.opacity(0.05)     // 0x0DFFFFFF
    static let glassSurface = Color.white.opacity(0.1)         // 0x1AFFFFFF
    
    // Text
    static let hologramText = Color.white.opacity(0.8)         // 0xCCFFFFFF
    
    // Functional Mappings
    static let primaryBackground = spaceBlack
    static let primaryAccent = neonCyan
    static let secondaryAccent = neonMagenta
    static let textPrimary = hologramText
    static let textSecondary = neonCyan.opacity(0.6)
    static let cyberCyan = neonCyan
    
    // Gradients
    static let primaryGradient = LinearGradient(
        colors: [neonMagenta, neonCyan],
        startPoint: .leading,
        endPoint: .trailing
    )
    
    static let glassGradient = LinearGradient(
        colors: [glassSurfaceStart, glassSurfaceEnd],
        startPoint: .top,
        endPoint: .bottom
    )
    
    static let borderGradient = primaryGradient
}

// MARK: - Color Extension for SwiftUI convenience
extension Color {
    static let spaceBlack = AppColors.spaceBlack
    static let neonMagenta = AppColors.neonMagenta
    static let neonCyan = AppColors.neonCyan
    static let neonViolet = AppColors.neonViolet
    static let alertOrange = AppColors.alertOrange
    static let telemetryGreen = AppColors.telemetryGreen
    static let hologramText = AppColors.hologramText
    static let cyberCyan = AppColors.cyberCyan
}

// MARK: - ShapeStyle for gradient borders
extension ShapeStyle where Self == LinearGradient {
    static var neonBorder: LinearGradient {
        AppColors.primaryGradient
    }
}
