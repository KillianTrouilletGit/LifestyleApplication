// DesignSystem.swift
// Design tokens: Spacing, Corner Radius, Spring Physics, Typography

import SwiftUI

// MARK: - Design Tokens
enum DesignSystem {
    static let padding: CGFloat = 16
    static let cornerRadius: CGFloat = 16
}

// MARK: - Spring Physics
extension Animation {
    static var bouncySpring: Animation {
        .spring(response: 0.5, dampingFraction: 0.6, blendDuration: 0)
    }
    
    static var placementSpring: Animation {
        .spring(response: 0.5, dampingFraction: 0.6, blendDuration: 0)
    }
}

// MARK: - Typography
enum AppTypography {
    static let dataFont: Font = .system(.body, design: .monospaced)
    static let headerFont: Font = .system(.body, design: .default)
    
    static let displayLarge: Font = .system(size: 57, weight: .bold, design: .default)
    static let displayMedium: Font = .system(size: 45, weight: .bold, design: .default)
    static let titleLarge: Font = .system(size: 22, weight: .semibold, design: .default)
    static let titleMedium: Font = .system(size: 16, weight: .semibold, design: .default)
    static let headlineSmall: Font = .system(size: 24, weight: .bold, design: .default)
    static let bodyLarge: Font = .system(size: 16, weight: .regular, design: .monospaced)
    static let bodyMedium: Font = .system(size: 14, weight: .regular, design: .monospaced)
    static let bodySmall: Font = .system(size: 12, weight: .regular, design: .monospaced)
    static let labelLarge: Font = .system(size: 14, weight: .medium, design: .monospaced)
    static let labelMedium: Font = .system(size: 12, weight: .medium, design: .monospaced)
    static let labelSmall: Font = .system(size: 11, weight: .medium, design: .monospaced)
}
