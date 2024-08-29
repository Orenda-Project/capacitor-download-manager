import type {PluginListenerHandle} from '@capacitor/core';

export declare enum OutputFormats {
    JPEG = "JPEG",
    PDF = "PDF",
    BOTH = "BOTH"
}

export declare type ScanResult = {
    images?: string[];
    pdf?: string | null;
};

export interface DownloadManagerPlugin {
    startScan(options?: Partial<{
        pageLimit: number;
        mode: string;
        enableGalleryImport: boolean;
        outputFormats: OutputFormats;
    }>): Promise<ScanResult>;
    startDownload(options: {
        url: string[];
    }): Promise<{
        value: string[];
    }>;
    removeDownloads(options: {
        value: string[];
    }): Promise<{
        value: string[];
    }>;
    resumeDownloads(): Promise<{
        value: string;
    }>;
    getDownloadList(): Promise<{
        value: string;
    }>;
    addListener(eventName: string, listenerFunc: (download: {
        result: string;
    }) => void): PluginListenerHandle;
}
