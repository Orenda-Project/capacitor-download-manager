import { registerPlugin } from '@capacitor/core';

import type { DownloadManagerPlugin } from './definitions';

const DownloadManager = registerPlugin<DownloadManagerPlugin>(
  'DownloadManager',
  {
    web: () => import('./web').then(m => new m.DownloadManagerWeb()),
  },
);

export * from './definitions';
export { DownloadManager };
