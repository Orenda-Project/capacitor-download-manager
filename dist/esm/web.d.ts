import { WebPlugin } from '@capacitor/core';
import type { DownloadManagerPlugin } from './definitions';
export declare class DownloadManagerWeb extends WebPlugin implements DownloadManagerPlugin {
    startDownload(options: {
        url: string[];
    }): Promise<{
        value: string[];
    }>;
    startVideo(options: {
        url: string[];
    }): Promise<{
        value: string[];
    }>;
    getDownloadList(options: string): Promise<{
        value: string;
    }>;
}
