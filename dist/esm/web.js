import { WebPlugin } from '@capacitor/core';
export class DownloadManagerWeb extends WebPlugin {
    startDownload(options) {
        return Promise.resolve({ value: options.url });
    }
    startVideo(options) {
        return Promise.resolve({ value: options.url });
    }
    getDownloadList(options) {
        return Promise.resolve({ value: options });
    }
}
//# sourceMappingURL=web.js.map