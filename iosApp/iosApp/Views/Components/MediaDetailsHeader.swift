//
//  MediaDetailsHeader.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-29.
//

import Shared
import SwiftUI

struct MediaDetailsHeader: View {
    let item: ArrMedia
    let type: InstanceType
    
    @Environment(\.colorScheme) var colorScheme
    
    private var infoString: String {
        var result = ""
        if let year = item.year {
            result += "\(year)"
        }
        if !item.runtimeString.isEmpty {
            result += (result.isEmpty ? "" : " • ") + item.runtimeString
        }
        if let certification = item.certification {
            result += (result.isEmpty ? "" : " • ") + certification
        }
        return result
    }
    
    var body: some View {
        ZStack(alignment: .topLeading) {
            MediaHeaderBanner(bannerUrl: URL(string: item.getBanner()?.remoteUrl ?? ""))
            
            // Gradient Overlay
            VStack(spacing: 0) {
                Spacer()
                LinearGradient(
                    gradient: Gradient(colors: [
                        .clear,
                        Color(.systemBackground).opacity(0.8),
                        Color(.systemBackground)
                    ]),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .frame(height: 150)
            }
            .frame(height: 400)
            
            HStack(alignment: .bottom, spacing: 24) {
                PosterItem(item: item, aspectRatio: type.aspectRatio)
                    .frame(width: 125)
                
                VStack(alignment: .leading, spacing: 8) {
                    ClearLogoView(item: item)
                    
                    VStack(alignment: .leading, spacing: 4) {
                        let ratings = item.ratings?.toRatingItems() ?? []
                        if !ratings.isEmpty {
                            FlowLayout(spacing: 12) {
                                ForEach(ratings, id: \.self) { rating in
                                    HStack(spacing: 4) {
                                        if let icon = rating.icon {
                                            Image(resource: icon)
                                                .resizable()
                                                .frame(width: 14, height: 14)
                                        } else {
                                            Image(systemName: "star.fill")
                                                .foregroundColor(.arrOrange)
                                                .font(.system(size: 14))
                                        }
                                        Text(rating.score)
                                            .font(.system(size: 12, weight: .semibold))
                                            .fixedSize(horizontal: true, vertical: false)
                                    }
                                }
                            }
                        }

                        if !(item is Arrtist) && !(item is Author) {
                            if !infoString.isEmpty {
                                Text(infoString)
                                    .font(.body)
                            }
                            
                            if let releasedBy = item.releasedBy {
                                Text(releasedBy)
                                    .font(.subheadline)
                            }
                        }
                        
                        Text(item.genres.joined(separator: " • "))
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                            .lineLimit(2)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.horizontal, 12)
            .padding(.bottom, 12)
            .padding(.top, 170)
        }
        .frame(height: 400)
    }
}

struct RequestMediaDetailsHeader: View {
    let item: RequestMediaDetails
    
    var body: some View {
        ZStack(alignment: .topLeading) {
            MediaHeaderBanner(bannerUrl: URL(string: item.fullBackdropPath ?? ""))
            
            // Gradient Overlay
            VStack(spacing: 0) {
                Spacer()
                LinearGradient(
                    gradient: Gradient(colors: [
                        .clear,
                        Color(.systemBackground).opacity(0.8),
                        Color(.systemBackground)
                    ]),
                    startPoint: .top,
                    endPoint: .bottom
                )
                .frame(height: 150)
            }
            .frame(height: 400)
            
            HStack(alignment: .bottom, spacing: 24) {
                GenericPosterItem(posterUrl: item.fullPosterPath)
                    .frame(width: 150)
                
                VStack(alignment: .leading, spacing: 4) {
                    Text(infoString)
                        .font(.system(size: 16))
                        .padding(.top, 6)
                    
                    Text(item.genres.map { $0.name }.joined(separator: " • "))
                        .font(.system(size: 14))
                        .foregroundColor(.secondary)
                        .lineSpacing(2)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.top, 170)
            .padding(.horizontal, 12)
            .padding(.bottom, 12)
        }
        .frame(height: 400)
    }
    
    private var infoString: String {
        var items: [String] = []
        if let displayDate = item.displayDate {
            items.append(displayDate.format(pattern: "MMM d, yyyy"))
        }
        if let movie = item as? MovieDetails, let runtime = movie.runtime {
            items.append(Int(truncating: runtime).formatAsRuntime())
        }
        if let tv = item as? TvDetails {
            items.append(MR.plurals().seasons.localized(Int(tv.seasons.count)))
        }
        if let certification = item.getCertification(localeCode: Locale.current.region?.identifier ?? "") {
            items.append(certification)
        }
        return items.joined(separator: " • ")
    }
}
