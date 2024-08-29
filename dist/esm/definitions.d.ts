import type { PluginListenerHandle } from '@capacitor/core';
declare type URLRequest = {
    tag: string;
    url: string;
};
export interface DownloadManagerPlugin {
    startDownload(options: {
        url: string[];
    }): Promise<{
        value: string[];
    }>;
    startDownloadWithTag(options: {
        url: URLRequest[];
    }): Promise<{
        value: URLRequest[];
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
export {};
