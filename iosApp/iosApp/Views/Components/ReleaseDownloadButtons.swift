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
                HStack(spacing: 4) {
                    Image(systemName: "person.fill")
                        .font(.system(size: 11))
                    Text(MR.strings().interactive.localized())
                        .font(.system(size: 12, weight: .medium))
                        .lineLimit(1)
                        .minimumScaleFactor(0.5)
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .tint(.themePrimary)
            .controlSize(.small)
            .clipShape(Capsule())
            
            Button(action: onAutomaticClicked) {
                HStack(spacing: 4) {
                    if automaticSearchInProgress {
                        ProgressView()
                            .controlSize(.small)
                            .tint(.white)
                    } else {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 11))
                        Text(MR.strings().automatic.localized())
                            .font(.system(size: 12, weight: .medium))
                            .lineLimit(1)
                            .minimumScaleFactor(0.5)
                    }
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .tint(.themePrimary)
            .controlSize(.small)
            .clipShape(Capsule())
            .disabled(!automaticSearchEnabled || automaticSearchInProgress)
        }
    }
}
