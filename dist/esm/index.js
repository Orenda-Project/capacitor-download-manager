import { registerPlugin } from '@capacitor/core';
const DownloadManager = registerPlugin('DownloadManager', {
    web: () => import('./web').then(m => new m.DownloadManagerWeb()),
});
export * from './definitions';
export { DownloadManager };
//# sourceMappingURL=index.js.map