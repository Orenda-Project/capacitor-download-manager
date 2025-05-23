import { WebPlugin } from '@capacitor/core';
export class DownloadManagerWeb extends WebPlugin {
    startScan() {
        console.error('Document scanning is not supported on the web platform.');
        return Promise.resolve({ images: undefined, pdf: null });
    }
    startDownload(options) {
        return Promise.resolve({ value: options.url });
    }
    startDownloadWithTag(options) {
        return Promise.resolve({ value: options.url });
    }
    getDownloadList() {
        return Promise.resolve({ value: '[]' });
    }
    getDownloadListById() {
        return Promise.resolve({ value: '[]' });
    }
    removeDownloads(options) {
        return Promise.resolve({ value: options.value });
    }
    pauseDownloads(options) {
        return Promise.resolve({ value: options.value });
    }
    cancelDownloads(options) {
        return Promise.resolve({ value: options.value });
    }
    resumeDownloads() {
        return Promise.resolve({ value: '[]' });
    }
}
//# sourceMappingURL=web.js.map