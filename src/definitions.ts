import type { PluginListenerHandle } from '@capacitor/core';

export interface DownloadManagerPlugin {
  startDownload(options: { url: string[] }): Promise<{ value: string[] }>;
  removeDownloads(options: { value: string[] }): Promise<{ value: string[] }>;
  resumeDownloads(): Promise<{ value: string }>;
  getDownloadList(): Promise<{ value: string }>;
  addListener(
    eventName: string,
    listenerFunc: (download: { result: string }) => void,
  ): PluginListenerHandle;
}
