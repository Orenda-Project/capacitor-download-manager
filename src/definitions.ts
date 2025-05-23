import type { PluginListenerHandle } from '@capacitor/core';

export enum OutputFormats {
  JPEG = 'JPEG',
  PDF = 'PDF',
  BOTH = 'BOTH',
}

export type ScanResult = {
  images?: string[];
  pdf?: string | null;
};

type URLRequest = {
  tag: string;
  url: string;
};
export interface DownloadManagerPlugin {
  startScan(
    options?: Partial<{
      pageLimit: number;
      mode: string;
      enableGalleryImport: boolean;
      outputFormats: OutputFormats;
    }>,
  ): Promise<ScanResult>;

  startDownload(options: { url: string[] }): Promise<{ value: string[] }>;

  startDownloadWithTag(options: {
    url: URLRequest[];
  }): Promise<{ value: URLRequest[] }>;

  removeDownloads(options: { value: string[] }): Promise<{ value: string[] }>;

  pauseDownloads(options: { value: string[] }): Promise<{ value: string[] }>;

  cancelDownloads(options: { value: string[] }): Promise<{ value: string[] }>;

  resumeDownloads(): Promise<{ value: string }>;

  getDownloadList(): Promise<{ value: string }>;

  getDownloadListById(): Promise<{ value: string }>;

  addListener(
    eventName: string,
    listenerFunc: (download: { result: string }) => void,
  ): PluginListenerHandle;
}
