import { registerPlugin } from '@capacitor/core';

import type { DownloadManagerPlugin } from './definitions';

const Fetch2Plugin = registerPlugin<DownloadManagerPlugin>('DownloadManager', {
  web: () => import('./web').then(m => new m.DownloadManagerWeb()),
});

export * from './definitions';
export { Fetch2Plugin };
