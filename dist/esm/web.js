import { WebPlugin } from '@capacitor/core';
export class DownloadManagerWeb extends WebPlugin {
    startScan() {
        return Promise.resolve({ images: [], pdf: null });
    }
    startDownload(options) {
        return Promise.resolve({ value: options.url });
    }
    getDownloadList() {
        return Promise.resolve({ value: '[]' });
    }
    removeDownloads(options) {
        return Promise.resolve({ value: options.value });
    }
    resumeDownloads() {
        return Promise.resolve({ value: '[]' });
    }
}
//# sourceMappingURL=web.js.map