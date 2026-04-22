// AmbientBackground.swift
// Animated neon orb background matching Android's AmbientBackground

import SwiftUI

struct AmbientBackground: View {
    var body: some View {
        TimelineView(.animation(minimumInterval: 1.0/30.0)) { timeline in
            Canvas { context, size in
                let time = timeline.date.timeIntervalSinceReferenceDate
                drawOrbs(context: context, size: size, time: time)
            }
        }
        .background(AppColors.spaceBlack)
        .ignoresSafeArea()
    }
    
    private func drawOrbs(context: GraphicsContext, size: CGSize, time: Double) {
        let orbs: [(color: Color, xPhase: Double, yPhase: Double, sPhase: Double, speed: Double)] = [
            (.neonMagenta, 0.3, 0.7, 0.0, 0.08),
            (.neonCyan,    0.7, 0.3, 1.5, 0.06),
            (.neonViolet,  0.5, 0.5, 3.0, 0.07),
            (.neonCyan.opacity(0.5), 0.4, 0.6, 4.5, 0.05),
        ]
        
        for orb in orbs {
            let cx = size.width * (0.5 + 0.3 * sin(time * orb.speed + orb.xPhase))
            let cy = size.height * (0.5 + 0.3 * cos(time * orb.speed * 0.8 + orb.yPhase))
            let scale = 0.8 + 0.4 * sin(time * orb.speed * 1.5 + orb.sPhase)
            let radius = min(size.width, size.height) * 0.5 * scale
            let alpha = 0.1 + 0.1 * sin(time * orb.speed * 1.2 + orb.sPhase)
            
            let center = CGPoint(x: cx, y: cy)
            
            let gradient = Gradient(stops: [
                .init(color: orb.color.opacity(alpha), location: 0),
                .init(color: orb.color.opacity(0), location: 1)
            ])
            
            let shading = GraphicsContext.Shading.radialGradient(
                gradient,
                center: center,
                startRadius: 0,
                endRadius: radius
            )
            
            let rect = CGRect(
                x: cx - radius,
                y: cy - radius,
                width: radius * 2,
                height: radius * 2
            )
            
            context.fill(Ellipse().path(in: rect), with: shading)
        }
    }
}

#Preview {
    AmbientBackground()
}
