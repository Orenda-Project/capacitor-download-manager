import Foundation
import Capacitor

@objc public class DownloadManager: NSObject {
    public static let shared = DownloadManager()
    var session: URLSession!
    var activeDownloads: [URL: DownloadSession] = [:]
    var downloadList: [Download]?
    var downloadDelegate : DownloadDelegate?
    
    
    override public init() {
        super.init()
        let config = URLSessionConfiguration.background(withIdentifier: "pk.edu.niete.ios")
        session = URLSession(configuration: config, delegate: self, delegateQueue: nil)
        loadDownloads()
        
    }
    func initDelegate(delegate: DownloadDelegate){
        if downloadDelegate == nil {
            self.downloadDelegate = delegate
        }
    }
    
    func addDownload(download: Download) {
        if let (index,_) = getDownloadAt(url: download.url){
            setDownloadAt(index: index , download: download)
        }else{
            downloadList?.append(download)
            downloadDelegate?.onStatusChange(download, DownloadCallback.onAdded)
        }
        saveDownloads()
    }
    
    func getDownloadAt(url: String) -> (Int, Download)? {
        if let index = downloadList?.firstIndex(where: { $0.url == url }){
            return(index,downloadList?[index]) as? (Int, Download)
        }else {
            return nil
        }
    }
    
    func setDownload(download: Download) {
        if let index = downloadList?.firstIndex(where: { $0.url == download.url }){
            downloadList?[index] = download
        }
    }
    
    func setDownloadAt(index: Int, download: Download) {
        downloadList?[index] = download
    }
    
    func saveDownloads() {
        let encoder = JSONEncoder()
        if let encoded = try? encoder.encode(self.downloadList) {
            let defaults = UserDefaults.standard
            defaults.set(encoded, forKey: "Downloads")
        }
    }
    
    func loadDownloads() {
        let defaults = UserDefaults.standard
        if let savedDownloads = defaults.object(forKey: "Downloads") as? Data {
            let decoder = JSONDecoder()
            if let loadedDownloads = try? decoder.decode([Download].self, from: savedDownloads) {
                self.downloadList = loadedDownloads
            }
        } else {
            downloadList = [Download]()
            activeDownloads = [:]
        }
    }
    
    func deleteDownloads(by downloadsToRemove: [String]) -> [Download]? {
        guard var downloads = downloadList else { return nil }
        var removedDownloads: [Download] = []
        for i in downloads.indices {
            if downloadsToRemove.contains(downloads[i].id.uuidString) {
                removedDownloads.append(downloads[i])
                deleteFile(path: downloads[i].file)
                downloads.remove(at: i)
                
                break
            }
        }
        downloadList = downloads
        saveDownloads()
        
        return removedDownloads.isEmpty ? nil : removedDownloads
    }
    
    func getDirectory(directory: String?) -> FileManager.SearchPathDirectory? {
        if let directory = directory {
            switch directory {
            case "CACHE":
                return .cachesDirectory
            case "LIBRARY":
                return .libraryDirectory
            default:
                return .documentDirectory
            }
        }
        return nil
    }
    
    func getFileUrl(at path: String, in directory: String?) -> URL? {
        if let directory = getDirectory(directory: directory) {
            guard let dir = FileManager.default.urls(for: directory, in: .userDomainMask).first else {
                return nil
            }
            if !path.isEmpty {
                return dir.appendingPathComponent(path)
            }
            return dir
        } else {
            return URL(string: path)
        }
    }
    
    func deleteFile(path:String) {
        if let range = path.range(of: "/", options: .backwards) {
            let fileName = String(path[range.upperBound...])
            let directory = "LIBRARY"
            guard let fileUrl = getFileUrl(at: fileName, in: directory) else {
                CAPLog.print("Invalid path")
                return
            }
            
            do {
                if FileManager.default.fileExists(atPath: fileUrl.path) {
                    try FileManager.default.removeItem(atPath: fileUrl.path)
                }
            } catch let error as NSError {
                CAPLog.print(error.localizedDescription, error)
            }
        }
    }
    
    func startDownload(from url: URL) {
        let download = DownloadSession(url: url)
        activeDownloads[url] = download
        addDownload(download: Download(file: url.lastPathComponent, fileUri: url.relativePath, url: url.absoluteString, tag: "", status: DownloadStatus.ADDED, downloaded: 0, total: 0))
        download.task = session.downloadTask(with: url)
        download.task?.resume()
    }
    
    func startDownloadWithTag(from url: URL , tag: String) {
        let download = DownloadSession(url: url)
        activeDownloads[url] = download
        addDownload(download: Download(file: url.lastPathComponent, fileUri: url.relativePath, url: url.absoluteString, tag: tag, status: DownloadStatus.ADDED, downloaded: 0, total: 0))
        download.task = session.downloadTask(with: url)
        download.task?.resume()
    }
    
    func startDownloadSession(from url: URL, task: URLSessionDownloadTask ) {
        let download = DownloadSession(url: url)
        activeDownloads[url] = download
        download.task = task
        download.task?.resume()
    }
    
    func resumeDownload(from url: URL, resumeData: Data) {
        let download = DownloadSession(url: url)
        activeDownloads[url] = download
        download.task = session.downloadTask(withResumeData: resumeData)
        download.task?.resume()
    }
    
    func saveResumeData(for url: URL) {
        if let download = activeDownloads[url] {
            download.task?.cancel(byProducingResumeData: { resumeData in
                download.resumeData = resumeData
                UserDefaults.standard.set(resumeData, forKey: url.absoluteString)
            })
        }
    }
    
    public func fetchDownloads() -> [Download]? {
        return self.downloadList
    }
    
    public func saveActives(){
        for download in activeDownloads.values {
            saveResumeData(for: download.url)
        }
    }
    
    public func resumeActives(){
        let defaults = UserDefaults.standard
        downloadList?.forEach({ download in
            if(download.status == DownloadStatus.COMPLETED){
                return
            }else if let resumeData = defaults.data(forKey: download.url) {
                resumeDownload(from: URL(string: download.url)!, resumeData: resumeData)
                defaults.removeObject(forKey: download.url)
            } else {
                startDownload(from: URL(string: download.url)!)
            }
        })
    }
    
}

extension DownloadManager: URLSessionDownloadDelegate {
    
    public func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didFinishDownloadingTo location: URL) {
        // Handle file move or process after download completion
        // Define the FileManager
        let fileManager = FileManager.default
        
        // Get the .library directory URL
        guard let libraryDirectory = fileManager.urls(for: .libraryDirectory, in: .userDomainMask).first else {
            print("Could not find the library directory")
            return
        }
        
        // Create a destination URL in the .library directory
        let destinationURL = libraryDirectory.appendingPathComponent(downloadTask.originalRequest?.url?.lastPathComponent ?? "file")
        
        // Remove any existing file at the destination URL
        if fileManager.fileExists(atPath: destinationURL.path) {
            do {
                try fileManager.removeItem(at: destinationURL)
            } catch {
                print("Error removing existing file: \(error.localizedDescription)")
                return
            }
        }
        
        // Move the downloaded file to the destination URL
        do {
            try fileManager.moveItem(at: location, to: destinationURL)
            print("File moved to: \(destinationURL.path)")
            
            guard let url = downloadTask.originalRequest?.url?.absoluteString else {return}
            if var (index, download) = getDownloadAt(url: url) {
                download.file = destinationURL.path
                download.status = DownloadStatus.COMPLETED
                downloadList?[index] = download
                downloadDelegate?.onStatusChange(download,DownloadCallback.onCompleted)
                saveDownloads()
            }
            
        } catch {
            print("Error moving file: \(error.localizedDescription)")
        }
    }
    
    public func urlSession(_ session: URLSession, downloadTask: URLSessionDownloadTask, didWriteData bytesWritten: Int64, totalBytesWritten: Int64, totalBytesExpectedToWrite: Int64) {
        guard let url = downloadTask.originalRequest?.url?.absoluteString else {return}
        if var (index, download) = getDownloadAt(url: url) {
            let progress = (totalBytesWritten/totalBytesExpectedToWrite)*100
            download.downloaded = Int(totalBytesWritten)
            download.total = Int(totalBytesExpectedToWrite)
            download.status = progress == 100 ? DownloadStatus.COMPLETED : DownloadStatus.DOWNLOADING
            downloadList?[index] = download
            downloadDelegate?.onStatusChange(download, DownloadCallback.onProgress)
            saveDownloads()
        }
        
    }
    
    public func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        guard let url = task.originalRequest?.url else { return }
        activeDownloads[url] = nil
        if let error = error as NSError?, let resumeData = error.userInfo[NSURLSessionDownloadTaskResumeData] as? Data {
            UserDefaults.standard.set(resumeData, forKey: url.absoluteString)
        }
        else if let error = error as NSError?, error.domain == NSURLErrorDomain {
            if let resumeData = error.userInfo[NSURLSessionDownloadTaskResumeData] as? Data {
                UserDefaults.standard.set(resumeData, forKey: url.absoluteString)
                if var (index, download) = getDownloadAt(url: url.absoluteString) {
                    download.status = DownloadStatus.FAILED
                    download.error = error.localizedDescription
                    downloadList?[index] = download
                    downloadDelegate?.onStatusChange(download, DownloadCallback.onError)
                    saveDownloads()
                }
            }
        }
    }
}
