//
//  EpisodeRow.swift
//  iosApp
//
//  Created by Owen LeJeune on 2025-12-15.
//

import SwiftUI
import Shared

struct EpisodeRow: View {
    let episode: Episode
    let onToggleEpisodeMonitor: (Episode) -> Void
    let onAutomaticSearch: () -> Void
    let automaticSearchDisabled: Bool
    let onClicked: () -> Void
    
    @EnvironmentObject private var navigation: NavigationManager
    
    private var statusString: String? {
        episode.episodeFile?.qualityName ??
        (episode.airDate?.isTodayOrAfter() == true ? MR.strings().unaired.localized() : nil)
    }
    
    var body: some View {
        HStack(spacing: 8) {
            // Main info area that navigates to details
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 0) {
                    Text("\(episode.episodeNumber). ")
                        .font(.system(size: 16))
                        .fontWeight(.medium)
                        .foregroundColor(.primary)
                    
                    Text(episode.title ?? "")
                        .font(.system(size: 16))
                        .fontWeight(.medium)
                        .lineLimit(1)
                        .foregroundColor(.primary)
                    
                    if let finaleType = episode.finaleType {
                        Text(" • \(finaleType.resource.localized())")
                            .font(.system(size: 12))
                            .foregroundColor(.secondary)
                    }
                }
                
                HStack(spacing: 4) {
                    if let statusString = statusString {
                        Text(statusString)
                            .font(.system(size: 14))
                            .italic(episode.airDate?.isTodayOrAfter() == true)
                    } else {
                        Text(MR.strings().missing.localized())
                            .font(.system(size: 14))
                            .foregroundColor(.red)
                            .italic()
                    }
                    
                    let airDateText = " • \(episode.formatAirDateUtc() ?? "")"
                    Text(airDateText)
                        .font(.system(size: 14))
                        .fontWeight(episode.airDate?.isToday() == true ? .medium : .regular)
                        .foregroundColor(episode.airDate?.isToday() == true ? .themePrimary : .primary)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .contentShape(Rectangle())
            .onTapGesture {
                onClicked()
            }
            
            // Action buttons
            HStack(spacing: 16) {
                Button(action: {
                    let route: MediaRoute = .seriesReleases(episodeId: episode.id)
                    navigation.go(to: route, of: .sonarr)
                }) {
                    Image(systemName: "person.fill")
                        .font(.system(size: 18))
                        .foregroundColor(.primary)
                }
                .buttonStyle(.plain)
                
                Button(action: onAutomaticSearch) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 18))
                        .foregroundColor(.primary)
                }
                .buttonStyle(.plain)
                .disabled(automaticSearchDisabled)
                
                Button(action: {
                    onToggleEpisodeMonitor(episode)
                }) {
                    Image(systemName: episode.monitored ? "bookmark.fill" : "bookmark")
                        .font(.system(size: 18))
                        .foregroundColor(.primary)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.vertical, 4)
    }
}
