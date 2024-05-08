import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(DownloadManagerPlugin)
public class DownloadManagerPlugin: CAPPlugin {
    private let implementation = DownloadManager()
    
    @objc func getDownloadList(_ call: CAPPluginCall) {
        guard let downloads = self.implementation.fetchDownloads() else {
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
        guard let items = call.getArray("value", String.self) else {
            call.reject("Not a valid data provided!")
            return
        }
        if !items.isEmpty {
            if let removedDownloads = self.implementation.deleteDownloads(by: items){
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
        guard let urls = call.getArray("url", String.self) else {
            call.reject("No valid urls provided")
            return
        }
        guard let downloads = self.implementation.fetchDownloads() else { return }
        for url in urls {
            let contains = downloads.first { download in
                download.url == url
            }
            if contains == nil {
                let progressEmitter: DownloadManager.ProgressEmitter = { bytes, contentLength in
                    let progress = (Int(bytes) / Int(contentLength)) * 100
                    if (progress % 10) == 0 {
                        if let index = self.implementation.downloadList?.firstIndex(where: { $0.url == url }) {
                            if var newDownload = self.implementation.downloadList?[index] {
                                newDownload.downloaded = Int(bytes)
                                newDownload.total = Int(contentLength)
                                newDownload.status = progress == 100 ? "COMPLETED" : "DONWLOADING"
                                self.implementation.downloadList?[index] = newDownload
                                self.implementation.saveDownloads()
                                let encoder = JSONEncoder()
                                encoder.outputFormatting = .prettyPrinted
                                do {
                                    let jsonData = try encoder.encode(newDownload)
                                    if let jsonString = String(data: jsonData, encoding: .utf8) {
                                        CAPLog.print(jsonString)
                                        if Int(bytes) == Int(contentLength) {
                                            CAPLog.print("onCompleted","download \(jsonString)")
                                            self.notifyListeners("onCompleted", data: ["download": jsonString])
                                        } else {
                                            CAPLog.print("onProgress","download \(jsonString)")
                                            self.notifyListeners("onProgress", data: ["download": jsonString])
                                        }
                                    }
                                } catch {
                                    CAPLog.print("Error encoding Download: \(error)")
                                }

                            }
                        }
                    }
                }
                do {
                    if let urlString = URL(string: url) {
                        try implementation.downloadFile(call: call, url: urlString, emitter: progressEmitter, config: bridge?.config)
                    } else {return}
                } catch let error {
                    call.reject(error.localizedDescription)
                }
            } else {
                let encoder = JSONEncoder()
                encoder.outputFormatting = .prettyPrinted
                do {
                    let jsonData = try encoder.encode(contains)
                    if let jsonString = String(data: jsonData, encoding: .utf8) {
                        self.notifyListeners("onCompleted", data: ["download": jsonString])
                    }
                } catch {
                    CAPLog.print("Error encoding Download: \(error)")
                }
            }
        }
    }
}
