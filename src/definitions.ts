import type {PluginListenerHandle} from '@capacitor/core';

export interface DownloadManagerPlugin {
    startScan(options?: {
        pageLimit?: number;
        mode?: string;
        enableGalleryImport?: boolean;
        outputFormats?: 'JPEG' | 'PDF' | 'BOTH';
    }): Promise<{ images?: string[]; pdf?: string | null }>;

    startDownload(options: { url: string[] }): Promise<{ value: string[] }>;

    removeDownloads(options: { value: string[] }): Promise<{ value: string[] }>;

    resumeDownloads(): Promise<{ value: string }>;

    getDownloadList(): Promise<{ value: string }>;

    addListener(
        eventName: string,
        listenerFunc: (download: { result: string }) => void,
    ): PluginListenerHandle;
}
