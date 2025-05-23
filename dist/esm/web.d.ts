import { WebPlugin } from '@capacitor/core';
import type { DownloadManagerPlugin } from './definitions';
export declare class DownloadManagerWeb extends WebPlugin implements DownloadManagerPlugin {
    startScan(): Promise<{
        images?: string[];
        pdf?: string | null;
    }>;
    startDownload(options: {
        url: string[];
    }): Promise<{
        value: string[];
    }>;
    startDownloadWithTag(options: {
        url: {
            tag: string;
            url: string;
        }[];
    }): Promise<{
        value: {
            tag: string;
            url: string;
        }[];
    }>;
    getDownloadList(): Promise<{
        value: string;
    }>;
    getDownloadListById(): Promise<{
        value: string;
    }>;
    removeDownloads(options: {
        value: string[];
    }): Promise<{
        value: string[];
    }>;
    pauseDownloads(options: {
        value: string[];
    }): Promise<{
        value: string[];
    }>;
    cancelDownloads(options: {
        value: string[];
    }): Promise<{
        value: string[];
    }>;
    resumeDownloads(): Promise<{
        value: string;
    }>;
}
