import { WebPlugin } from '@capacitor/core';

import type { DownloadManagerPlugin } from './definitions';

export class DownloadManagerWeb
  extends WebPlugin
  implements DownloadManagerPlugin
{
  startScan(): Promise<{ images: string[], pdf: string | null }> {
    return Promise.resolve({ images: [], pdf: null });
  }
  startDownload(options: { url: string[] }): Promise<{ value: string[] }> {
    return Promise.resolve({ value: options.url });
  }
  getDownloadList(): Promise<{ value: string }> {
    return Promise.resolve({ value: '[]' });
  }
  removeDownloads(options: { value: string[] }): Promise<{ value: string[] }> {
    return Promise.resolve({ value: options.value });
  }
  resumeDownloads(): Promise<{ value: string }> {
    return Promise.resolve({ value: '[]' });
  }
}
