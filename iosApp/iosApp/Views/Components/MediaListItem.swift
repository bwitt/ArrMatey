//
//  MediaListItem.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-02-10.
//

import SwiftUI
import Shared

struct MediaItemView<T: ArrMedia>: View {
    let item: T
    let isActive: Bool
    let aspectRatio: AspectRatio
    
    init(item: T, aspectRatio: AspectRatio, isActive: Bool = false) {
        self.item = item
        self.aspectRatio = aspectRatio
        self.isActive = isActive
    }
    
    private var itemTitle: String {
        var result = item.title ?? MR.strings().unknown.localized()
        if let year = item.year,
            let title = item.title,
            !title.contains(String(describing: year)) {
                result += " (\(year))"
            }
        return result
    }
    
    var body: some View {
        ZStack {
            BannerView(item: item)
                .frame(height: 100)
            
            Color.black.opacity(0.5)
            
            HStack(spacing: 18) {
                PosterItem(item: item, aspectRatio: aspectRatio)
                    .frame(height: 75)
                
                VStack(alignment: .leading, spacing: 0) {
                    Text(itemTitle)
                        .font(.system(size: 18, weight: .bold))
                        .foregroundColor(.white)
                        .lineLimit(1)
                    
                    MediaDetailsView(item: item, isActive: isActive)
                }
            }
            .frame(maxWidth: .infinity)
            .padding(12)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 100)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.2), radius: 10, x: 0, y: 4)
    }
}
