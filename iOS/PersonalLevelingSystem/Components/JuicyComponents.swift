// JuicyComponents.swift
// Reusable UI components: JuicyButton, JuicyCard, OperatorHeader, JuicyInput

import SwiftUI

// MARK: - JuicyButton
struct JuicyButton: View {
    let text: String
    let action: () -> Void
    var isEnabled: Bool = true
    
    @State private var isPressed = false
    
    var body: some View {
        Button(action: {
            // Haptic feedback
            let generator = UIImpactFeedbackGenerator(style: .medium)
            generator.impactOccurred()
            action()
        }) {
            Text(text.uppercased())
                .font(AppTypography.labelLarge)
                .fontWeight(.bold)
                .foregroundStyle(isEnabled ? AppColors.primaryAccent : AppColors.primaryAccent.opacity(0.3))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .padding(.horizontal, 16)
                .background(Color.clear)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(AppColors.primaryGradient, lineWidth: 1.5)
                )
        }
        .disabled(!isEnabled)
        .scaleEffect(isPressed ? 0.95 : 1.0)
        .animation(.bouncySpring, value: isPressed)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in isPressed = true }
                .onEnded { _ in isPressed = false }
        )
    }
}

// MARK: - JuicyCard
struct JuicyCard<Content: View>: View {
    let action: () -> Void
    var cornerRadius: CGFloat = DesignSystem.cornerRadius
    @ViewBuilder let content: () -> Content
    
    @State private var isPressed = false
    
    var body: some View {
        Button(action: {
            let generator = UIImpactFeedbackGenerator(style: .light)
            generator.impactOccurred()
            action()
        }) {
            ZStack {
                // Black opaque background
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(Color.black)
                
                // Glass gradient overlay
                RoundedRectangle(cornerRadius: cornerRadius)
                    .fill(AppColors.glassGradient)
                
                // Gradient border
                RoundedRectangle(cornerRadius: cornerRadius)
                    .stroke(AppColors.primaryGradient, lineWidth: 1)
                
                // Content
                content()
                    .padding(DesignSystem.padding)
            }
        }
        .buttonStyle(PlainButtonStyle())
        .scaleEffect(isPressed ? 0.98 : 1.0)
        .animation(.bouncySpring, value: isPressed)
        .simultaneousGesture(
            DragGesture(minimumDistance: 0)
                .onChanged { _ in isPressed = true }
                .onEnded { _ in isPressed = false }
        )
    }
}

// MARK: - OperatorHeader
struct OperatorHeader: View {
    let subtitle: String
    let title: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(subtitle.uppercased())
                .font(AppTypography.labelLarge)
                .foregroundStyle(AppColors.primaryAccent)
                .tracking(2)
            
            Text(title.uppercased())
                .font(AppTypography.headlineSmall)
                .foregroundStyle(AppColors.hologramText)
                .fontWeight(.bold)
            
            Spacer().frame(height: 12)
            
            Rectangle()
                .fill(AppColors.primaryGradient)
                .frame(height: 2)
                .frame(maxWidth: .infinity, alignment: .leading)
                .frame(width: UIScreen.main.bounds.width * 0.35)
        }
        .padding(.vertical, 16)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

// MARK: - JuicyInput
struct JuicyInput: View {
    let placeholder: String
    @Binding var text: String
    var keyboardType: UIKeyboardType = .default
    var isReadOnly: Bool = false
    var onTap: (() -> Void)? = nil
    
    var body: some View {
        ZStack {
            TextField("", text: $text, prompt: Text(placeholder)
                .font(AppTypography.bodyMedium)
                .foregroundStyle(Color.gray.opacity(0.6)))
                .font(AppTypography.bodyMedium)
                .foregroundStyle(AppColors.hologramText)
                .keyboardType(keyboardType)
                .disabled(isReadOnly)
                .padding(.horizontal, 16)
                .padding(.vertical, 14)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .stroke(AppColors.primaryAccent.opacity(0.5), lineWidth: 1)
                )
                .tint(AppColors.primaryAccent)
            
            if isReadOnly, let onTap {
                Color.clear
                    .contentShape(Rectangle())
                    .onTapGesture { onTap() }
            }
        }
    }
}

#Preview("JuicyButton") {
    ZStack {
        Color.black.ignoresSafeArea()
        VStack(spacing: 20) {
            JuicyButton(text: "Start Mission", action: {})
            JuicyButton(text: "Disabled", action: {}, isEnabled: false)
        }
        .padding()
    }
}

#Preview("JuicyCard") {
    ZStack {
        Color.black.ignoresSafeArea()
        JuicyCard(action: {}) {
            VStack {
                Text("TRAINING")
                    .font(AppTypography.labelLarge)
                    .foregroundStyle(.white)
            }
        }
        .frame(width: 180, height: 120)
        .padding()
    }
}

#Preview("OperatorHeader") {
    ZStack {
        Color.black.ignoresSafeArea()
        OperatorHeader(subtitle: "Operator OS", title: "System Dashboard")
            .padding()
    }
}
