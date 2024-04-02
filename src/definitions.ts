import type { PluginListenerHandle } from '@capacitor/core';

export interface DownloadManagerPlugin {
  startDownload(options: { url: string[] }): Promise<{ value: string[] }>;
  startVideo(options: { url: string[] }): Promise<{ value: string[] }>;
  getDownloadList(options: string): Promise<{ value: string }>;
  addListener(
    eventName: string,
    listenerFunc: (download: { result: string }) => void,
  ): PluginListenerHandle;
}
