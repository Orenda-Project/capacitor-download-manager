import { WebPlugin } from '@capacitor/core';
import type { DownloadManagerPlugin } from './definitions';
export declare class DownloadManagerWeb extends WebPlugin implements DownloadManagerPlugin {
    startDownload(options: {
        url: string[];
    }): Promise<{
        value: string[];
    }>;
    getDownloadList(): Promise<{
        value: string;
    }>;
    removeDownloads(options: {
        value: string[];
    }): Promise<{
        value: string[];
    }>;
    resumeDownloads(): Promise<{
        value: string;
    }>;
}
