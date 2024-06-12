import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(DownloadManagerPlugin)
public class DownloadManagerPlugin: CAPPlugin , DownloadDelegate {
    
    let encoder = JSONEncoder()
    
    @objc func getDownloadList(_ call: CAPPluginCall) {
        DownloadManager.shared.initDelegate(delegate:self)
        DownloadManager.shared.loadDownloads()
        guard let downloads = DownloadManager.shared.fetchDownloads() else {
            call.reject("No downloads found!")
            return
        }
        let encoder = JSONEncoder()
        encoder.outputFormatting = .prettyPrinted
        do {
            let jsonData = try encoder.encode(downloads)
            if let jsonString = String(data: jsonData, encoding: .utf8) {
                CAPLog.print(jsonString)
                call.resolve(["download": jsonString])
            }
        } catch {
            CAPLog.print("Error encoding Download: \(error)")
            call.reject("No downloads found!")
        }
        
    }
    
    @objc func removeDownloads(_ call: CAPPluginCall) {
        DownloadManager.shared.initDelegate(delegate:self)
        DownloadManager.shared.loadDownloads()
        guard let items = call.getArray("value", String.self) else {
            call.reject("Not a valid data provided!")
            return
        }
        if !items.isEmpty {
            if let removedDownloads = DownloadManager.shared.deleteDownloads(by: items){
                removedDownloads.forEach { download in
                    let encoder = JSONEncoder()
                    encoder.outputFormatting = .prettyPrinted
                    do {
                        let jsonData = try encoder.encode(download)
                        if let jsonString = String(data: jsonData, encoding: .utf8) {
                            self.notifyListeners("onRemoved", data: ["download": jsonString])
                        }
                    } catch {
                        CAPLog.print("Error encoding Download: \(error)")
                        call.reject("Error encoding Download: \(error)")
                    }
                }
            }
        }
    }
    
    @objc func startDownload(_ call: CAPPluginCall) {
        DownloadManager.shared.initDelegate(delegate:self)
        guard let urls = call.getArray("url", String.self) else {
            call.reject("No valid urls provided")
            return
        }
        for url in urls {
            DownloadManager.shared.loadDownloads()
            DownloadManager.shared.startDownload(from: URL(string: url)!)
        }
    }
    
    @objc func resumeDownloads(_ call: CAPPluginCall){
        DownloadManager.shared.initDelegate(delegate:self)
        DownloadManager.shared.loadDownloads()
        DownloadManager.shared.resumeActives()
    }
    
    func onStatusChange(_ download: Download?, _ status: DownloadCallback) {
        if let download = download {
            handleDownload(download: download, event: status)
        }
    }
    
    func handleDownload(download: Download, event: DownloadCallback){
        do {
            encoder.outputFormatting = .prettyPrinted
            let jsonData = try encoder.encode(download)
            if let jsonString = String(data: jsonData, encoding: .utf8) {
                CAPLog.print(["download": jsonString])
                self.notifyListeners(event.rawValue, data: ["download": jsonString])
            }
        } catch {
            CAPLog.print("Error encoding Download: \(error)")
        }
    }
}



