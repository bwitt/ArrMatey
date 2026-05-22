//
//  WebViewContainer.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-03-23.
//

import SwiftUI
import WebKit
import Shared

struct WebViewContainer: UIViewRepresentable {
    let url: String
    let headers: [String: String]
    @Binding var canGoBack: Bool
    @Binding var canGoForward: Bool
    @Binding var webView: WKWebView?
    @Binding var progress: Double
    @Binding var currentUrl: String
    @Binding var currentTitle: String
    @Binding var isRefreshing: Bool

    func makeUIView(context: Context) -> WKWebView {
        let config = WKWebViewConfiguration()
        let view = WKWebView(frame: .zero, configuration: config)
        view.navigationDelegate = context.coordinator
        view.scrollView.delegate = context.coordinator
        
        view.allowsBackForwardNavigationGestures = true
        
        let refreshControl = UIRefreshControl()
        refreshControl.addTarget(context.coordinator, action: #selector(Coordinator.refresh(_:)), for: .valueChanged)
        view.scrollView.refreshControl = refreshControl
        
        view.addObserver(context.coordinator, forKeyPath: "estimatedProgress", options: .new, context: nil)
        view.addObserver(context.coordinator, forKeyPath: "title", options: .new, context: nil)
        view.addObserver(context.coordinator, forKeyPath: "URL", options: .new, context: nil)

        return view
    }

    func updateUIView(_ uiView: WKWebView, context: Context) {
        if isRefreshing {
            uiView.scrollView.refreshControl?.beginRefreshing()
        } else {
            uiView.scrollView.refreshControl?.endRefreshing()
        }
        
        let fixedUrlString = url.trimmingCharacters(in: .whitespacesAndNewlines)
        
        guard let urlObj = URL(string: fixedUrlString) else {
            return
        }
        
        if uiView.url == nil && !uiView.isLoading {
            var request = URLRequest(url: urlObj)
            headers.forEach { request.addValue($1, forHTTPHeaderField: $0) }
            
            uiView.load(request)
            
            DispatchQueue.main.async {
                self.webView = uiView
            }
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }

    class Coordinator: NSObject, WKNavigationDelegate, UIScrollViewDelegate {
        var parent: WebViewContainer

        init(_ parent: WebViewContainer) {
            self.parent = parent
        }
        
        @objc func refresh(_ sender: UIRefreshControl) {
            parent.webView?.reload()
        }

        func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
            DispatchQueue.main.async {
                self.parent.isRefreshing = true
            }
        }

        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            DispatchQueue.main.async {
                self.parent.canGoBack = webView.canGoBack
                self.parent.canGoForward = webView.canGoForward
                self.parent.isRefreshing = false
            }
        }
        
        func webView(_ webView: WKWebView, didFailProvisionalNavigation navigation: WKNavigation!, withError error: Error) {
            print("WebView failed provisional load: \(error.localizedDescription)")
            DispatchQueue.main.async {
                self.parent.isRefreshing = false
            }
        }

        func webView(_ webView: WKWebView, didFail navigation: WKNavigation!, withError error: Error) {
            print("WebView navigation failed: \(error.localizedDescription)")
            DispatchQueue.main.async {
                self.parent.isRefreshing = false
            }
        }
        
        override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
            guard let webView = object as? WKWebView else { return }
            
            DispatchQueue.main.async {
                if keyPath == "estimatedProgress" {
                    self.parent.progress = webView.estimatedProgress
                } else if keyPath == "title" {
                    self.parent.currentTitle = webView.title ?? ""
                } else if keyPath == "URL" {
                    self.parent.currentUrl = webView.url?.absoluteString ?? ""
                    self.parent.canGoBack = webView.canGoBack
                    self.parent.canGoForward = webView.canGoForward
                }
            }
        }
    }
}
