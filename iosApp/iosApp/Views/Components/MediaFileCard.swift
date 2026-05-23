//
//  MediaFileCard.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-01-23.
//

import Shared
import SwiftUI

struct MediaFileCard: View {
    let file: MediaFile
    let onDelete: () -> Void
    
    init(file: MediaFile, onDelete: @escaping () -> Void = {}) {
        self.file = file
        self.onDelete = onDelete
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(file.relativePath)
                .font(.system(size: 18, weight: .medium))
            
            Text(fileInfoLine(file: file))
                .font(.system(size: 14))
            
            if let dateAdded = file.dateAdded?.format(pattern: "MMM d, yyyy") {
                Text(MR.strings().added_on.formatted(args: [dateAdded]))
                    .font(.system(size: 14))
            }
        }
        .padding(.vertical, 12)
        .padding(.horizontal, 18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 10, style: .continuous)
                .fill(Color(.systemGroupedBackground))
        )
        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
            Button(role: .destructive) {
                onDelete()
            } label: {
                Label("Delete", systemImage: "trash.fill")
            }
        }
    }
    
    private func fileInfoLine(file: MediaFile) -> String {
        let languageName = file.languages.first?.name ?? ""
        let sizeString = file.size.bytesAsFileSizeString()
        let qualityName = file.quality?.quality.name ?? ""
        return [languageName, sizeString, qualityName]
            .filter { !$0.isEmpty }
            .joined(separator: " • ")
    }
}
