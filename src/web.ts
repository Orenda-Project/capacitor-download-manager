import { WebPlugin } from '@capacitor/core';

import type { DownloadManagerPlugin } from './definitions';

export class DownloadManagerWeb
  extends WebPlugin
  implements DownloadManagerPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
