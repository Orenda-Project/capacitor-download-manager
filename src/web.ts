import { WebPlugin } from '@capacitor/core';

import type { DownloadManagerPlugin } from './definitions';

export class DownloadManagerWeb
  extends WebPlugin
  implements DownloadManagerPlugin
{
  startDownload(options: { url: string[] }): Promise<{ value: string[] }> {
    return Promise.resolve({ value: options.url });
  }
  startVideo(options: { url: string[] }): Promise<{ value: string[] }> {
    return Promise.resolve({ value: options.url });
  }
  getDownloadList(options: string): Promise<{ value: string }> {
    return Promise.resolve({ value: options });
  }
}
