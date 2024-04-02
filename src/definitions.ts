export interface DownloadManagerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
