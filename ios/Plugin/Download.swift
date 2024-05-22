//
//  Download.swift
//  Plugin
//
//  Created by Nabi Ahmad on 02/04/2024.
//  Copyright © 2024 Max Lynch. All rights reserved.
//

import Foundation

protocol DownloadDelegate: AnyObject {
    func didFinish(_ downloads: Download?)
    func didError(_ downloads: Download?)
    func didProgress(_ downloads: Download?)
}

public struct Download: Codable {
    var id: UUID = UUID()
    var file: String = ""
    var fileUri: String = ""
    var url: String = ""
    var status: String = ""
    var error: String = ""
    var downloaded: Int = 0
    var total: Int = 0

}

enum DownloadStatus: Codable {
    case pending
    case downloading
    case paused
    case completed
    case failed
}

enum DownloadType: Codable {
    case file
    case image
    case audio
    case video
}
