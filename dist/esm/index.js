import { registerPlugin } from '@capacitor/core';
const Fetch2Plugin = registerPlugin('DownloadManager', {
    web: () => import('./web').then(m => new m.DownloadManagerWeb()),
});
export * from './definitions';
export { Fetch2Plugin };
//# sourceMappingURL=index.js.map