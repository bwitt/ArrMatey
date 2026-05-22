//
//  CustomWebpageViewerScreen.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-23.
//

import Shared
import SwiftUI
import WebKit

struct CustomWebpageViewerScreen: View {
    let webpageId: Int64
    
    @StateObject private var viewModel: CustomWebpageViewerViewModelS
    
    @State private var webView: WKWebView? = nil
    @State private var canGoBack = false
    @State private var canGoForward = false
    @State private var isToolbarVisible = true
    
    @State private var progress: Double = 0
    @State private var currentUrl: String = ""
    @State private var currentTitle: String = ""
    @State private var isRefreshing = false

    init(webpageId: Int64) {
        self.webpageId = webpageId
        self._viewModel = StateObject(wrappedValue: CustomWebpageViewerViewModelS(webpageId: webpageId))
    }

    var body: some View {
        ZStack(alignment: .bottom) {
            if let page = viewModel.webpage {
                VStack(spacing: 0) {
                    if progress > 0 && progress < 1 {
                        ProgressView(value: progress, total: 1.0)
                            .progressViewStyle(.linear)
                            .tint(.accentColor)
                    }
                    
                    WebViewContainer(
                        url: page.url,
                        headers: page.headers.reduce(into: [:]) { $0[$1.key] = $1.value },
                        canGoBack: $canGoBack,
                        canGoForward: $canGoForward,
                        webView: $webView,
                        progress: $progress,
                        currentUrl: $currentUrl,
                        currentTitle: $currentTitle,
                        isRefreshing: $isRefreshing
                    )
                }
                .ignoresSafeArea(edges: .bottom)
            } else {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                VStack {
                    Text(currentTitle.isEmpty ? (viewModel.webpage?.name ?? "") : currentTitle)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .lineLimit(1)
                    if !currentUrl.isEmpty {
                        Text(currentUrl)
                            .font(.caption2)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
            }
            ToolbarItem(placement: .primaryAction) {
                Button(action: { webView?.goBack() }) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 18, weight: .semibold))
                }.disabled(!canGoBack)
            }
            ToolbarItem(placement: .primaryAction) {
                Button(action: { webView?.goForward() }) {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 18, weight: .semibold))
                }.disabled(!canGoForward)
            }
            ToolbarItem(placement: .primaryAction) {
                Menu {
                    Button(action: { webView?.reload() }) {
                        Label(MR.strings().refresh.localized(), systemImage: "arrow.clockwise")
                    }
                    Button(action: {
                        sharePage()
                    }) {
                        Label(MR.strings().share.localized(), systemImage: "square.and.arrow.up")
                    }
                    Button(action: {
                        openInBrowser()
                    }) {
                        Label(MR.strings().open_in_browser.localized(), systemImage: "safari")
                    }
                    Button(action: {
                        copyLink()
                    }) {
                        Label(MR.strings().copy_link.localized(), systemImage: "doc.on.doc")
                    }
                    Button(action: {
                        viewModel.updateUrl(newUrl: currentUrl)
                    }) {
                        Label(MR.strings().replace_url_with_current.localized(), systemImage: "link")
                    }
                } label: {
                    Image(systemName: "ellipsis")
                        .imageScale(.medium)
                }
                .menuIndicator(.hidden)
            }
        }
    }

    private func sharePage() {
        let urlString = currentUrl.isEmpty ? (viewModel.webpage?.url ?? "") : currentUrl
        guard let url = URL(string: urlString) else { return }
        let av = UIActivityViewController(activityItems: [url], applicationActivities: nil)
        
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            rootVC.present(av, animated: true)
        }
    }
    
    private func openInBrowser() {
        let urlString = currentUrl.isEmpty ? (viewModel.webpage?.url ?? "") : currentUrl
        guard let url = URL(string: urlString) else { return }
        UIApplication.shared.open(url)
    }
    
    private func copyLink() {
        let urlString = currentUrl.isEmpty ? (viewModel.webpage?.url ?? "") : currentUrl
        UIPasteboard.general.string = urlString
    }
}
