import { useState } from "react";

import { apiClient } from "../api/axios";

/**
 * Streams an authenticated file/zip download to the browser. The API sets the filename via
 * Content-Disposition, but that header is not readable cross-origin, so the caller supplies the
 * filename explicitly. `downloading` holds the key of the in-flight download (for per-row spinners).
 */
export function useDownload() {
  const [downloading, setDownloading] = useState<string | null>(null);

  async function download(url: string, filename: string, key: string = url): Promise<void> {
    setDownloading(key);
    try {
      const { data } = await apiClient.get<Blob>(url, { responseType: "blob" });
      const objectUrl = URL.createObjectURL(data);
      const anchor = document.createElement("a");
      anchor.href = objectUrl;
      anchor.download = filename;
      document.body.appendChild(anchor);
      anchor.click();
      anchor.remove();
      URL.revokeObjectURL(objectUrl);
    } finally {
      setDownloading(null);
    }
  }

  return { download, downloading };
}
