//
//  ReleaseDownloadButtons.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-22.
//

import SwiftUI
import Shared

struct ReleaseDownloadButtons: View {
    var onInteractiveClicked: () -> Void
    var automaticSearchEnabled: Bool
    var onAutomaticClicked: () -> Void
    var automaticSearchInProgress: Bool = false
    
    var body: some View {
        HStack(spacing: 6) {
            Button(action: onInteractiveClicked) {
                Label(
                    title: { Text(MR.strings().interactive.localized()) },
                    icon: { Image(systemName: "person.fill") }
                )
                .font(.system(size: 16, weight: .medium))
                .foregroundStyle(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
            }
            .buttonStyle(.borderedProminent)
            .tint(.themePrimary)
            .clipShape(Capsule())
            
            Button(action: onAutomaticClicked) {
                Group {
                    if automaticSearchInProgress {
                        ProgressView()
                            .controlSize(.small)
                            .tint(.white)
                    } else {
                        Label(
                            title: { Text(MR.strings().automatic.localized()) },
                            icon: { Image(systemName: "magnifyingglass") }
                        )
                        .font(.system(size: 16, weight: .medium))
                        .foregroundStyle(.white)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 8)
            }
            .buttonStyle(.borderedProminent)
            .tint(.themePrimary)
            .clipShape(Capsule())
            .disabled(!automaticSearchEnabled || automaticSearchInProgress)
        }
    }
}
