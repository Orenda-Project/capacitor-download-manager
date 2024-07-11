//
//  Download.swift
//  Plugin
//
//  Created by Nabi Ahmad on 02/04/2024.
//  Copyright © 2024 Max Lynch. All rights reserved.
//

import Foundation
protocol DownloadDelegate: AnyObject {
    func onStatusChange(_ download: Download?, _ status: DownloadCallback)
}

class DownloadSession {
    let url: URL
    var task: URLSessionDownloadTask?
    var resumeData: Data?
    
    init(url: URL) {
        self.url = url
    }
}

public struct Download: Codable {
    let id: UUID
    var file: String
    var fileUri: String
    var url: String
    var tag: String?
    var status: DownloadStatus
    var error: String?
    var downloaded: Int
    var total: Int
    
    init(file: String, fileUri: String, url: String, tag: String?, status: DownloadStatus, downloaded: Int, total: Int) {
        self.id = UUID()
        self.file = file
        self.fileUri = fileUri
        self.url = url
        self.tag = tag
        self.status = status
        self.downloaded = downloaded
        self.total = total
    }
}

enum DownloadStatus: String, Codable {
    case ADDED, DOWNLOADING, COMPLETED, FAILED
}

enum DownloadType: String, Codable {
    case file, image, audio, video
}

enum DownloadCallback: String {
    case onProgress, onCompleted, onError, onAdded
}
