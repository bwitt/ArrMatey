//
//  StringExtensions.swift
//  iosApp
//
//  Created by Owen LeJeune on 2026-05-20.
//

import Foundation
import UIKit

extension String {
    func decodingHTMLEntities() -> String {
        if !self.contains("&") && !self.contains("<") {
            return self
        }
        
        var result = self
        // Strip HTML tags
        if result.contains("<") {
            result = result.replacingOccurrences(of: "<[^>]+>", with: "", options: .regularExpression)
        }
        
        if !result.contains("&") { return result }
        
        let entities = [
            "&quot;": "\"",
            "&amp;": "&",
            "&apos;": "'",
            "&lt;": "<",
            "&gt;": ">",
            "&nbsp;": " ",
            "&ndash;": "–",
            "&mdash;": "—",
            "&lsquo;": "‘",
            "&rsquo;": "’",
            "&ldquo;": "“",
            "&rdquo;": "”",
            "&#39;": "'"
        ]
        
        for (entity, value) in entities {
            result = result.replacingOccurrences(of: entity, with: value)
        }
        
        return result
    }
}
