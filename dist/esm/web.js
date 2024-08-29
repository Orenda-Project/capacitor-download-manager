import { WebPlugin } from '@capacitor/core';
export class DownloadManagerWeb extends WebPlugin {
    startDownload(options) {
        return Promise.resolve({ value: options.url });
    }
    startDownloadWithTag(options) {
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