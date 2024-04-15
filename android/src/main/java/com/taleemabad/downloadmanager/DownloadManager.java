package com.taleemabad.downloadmanager;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.tonyodev.fetch2.exception.FetchException;
import com.tonyodev.fetch2core.Downloader;
import com.tonyodev.fetch2okhttp.OkHttpDownloader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager {

    private static final String TAG = "DownloadManager";
    private static final String namespace = "DownloadManager";
    private final int groupId = namespace.hashCode();
    private static Context mContext;
    private static Fetch fetch;
    private static DownloadManager instance;
    private static FetchListener mFetchListener;

    public static DownloadManager getInstance(@NonNull Context context, FetchListener fetchListener) {
        // If the instance is null, create a new instance
        if (instance == null) {
            instance = new DownloadManager(context);
            mContext = context;
            fetch = instance.init();
            mFetchListener = fetchListener;
        }
        // Return the single instance
        return instance;
    }

    public DownloadManager(@NonNull Context context) {
        mContext = context;
    }

    public Fetch getFetch() {
        return fetch;
    }

    public String getNamespace() {
        return namespace;
    }

    public int getGroupID() {
        return groupId;
    }

    private Fetch init() {
        return Fetch.Impl.getInstance(
            new FetchConfiguration.Builder(mContext)
                .setDownloadConcurrentLimit(1)
                .setHttpDownloader(new OkHttpDownloader(Downloader.FileDownloaderType.SEQUENTIAL))
                .setNamespace(namespace)
                .setGlobalNetworkType(NetworkType.ALL)
                .enableAutoStart(true)
                .enableRetryOnNetworkGain(true)
                .enableFileExistChecks(false)
                .enableLogging(true)
                .build()
        );
    }

    private void startDownloading(List<String> urls, FetchListener fetchListener) {
        fetch.addListener(fetchListener);
        fetch.enqueue(
            getFetchRequests(urls),
            updatedRequests -> {
                Log.d(TAG, "enqueue: " + updatedRequests);
            }
        );
    }

    private List<Request> getFetchRequests(List<String> urls) {
        Log.d(TAG, "initFetch: " + urls.toString());
        File file = null;
        ArrayList<Request> requests = new ArrayList<>();
        for (String url : urls) {
            String filePath = Utils.getFilePath(url, mContext);
            file = new File(filePath);
            if (file.exists()) {
                try {
                    file.delete();
                    Log.i(TAG, "File Deleted: " + filePath);
                } catch (Exception e) {
                    Log.e(TAG, "File Delete Error: " + e.getMessage());
                }
            }
            String fileName = Utils.getFilePath(url, mContext);
            Request request = new Request(url, fileName);
            request.setGroupId(groupId);
            request.setPriority(Priority.HIGH);
            request.setTag(fileName);
            request.setNetworkType(NetworkType.ALL);
            requests.add(request);
        }
        return requests;
    }

    public void initDownloading(PluginCall call) {
        JSArray url = call.getArray("url");
        JSObject ret = new JSObject();
        ret.put("value", url);
        try {
            startDownloading(url.toList(), mFetchListener);
        } catch (Exception e) {
            ret.put("error", e.getMessage());
        }
        call.resolve(ret);
    }

    public List<Download> getFetchDownloads() {
        if (fetch == null) {
            fetch = init();
        }
        List<Download> downloadList = new ArrayList<>();
        fetch.getFetchGroup(
            groupId,
            fetchGroup -> {
                try {
                    Log.d(TAG, "FetchGroup: " + fetchGroup.getDownloads());
                    List<Download> downloads = fetchGroup.getDownloads();
                    for (Download download : downloads) {
                        Log.d(TAG, "Download File:: " + download.getId() + " : " + download.getStatus() + " => " + download.getFile());
                        downloadList.add(download);
                    }
                } catch (FetchException e) {
                    Log.d(TAG, "FetchException: " + e.getMessage());
                }
            }
        );
        return downloadList;
    }

    public void resumeDownloads() {
        if (fetch == null) {
            fetch = init();
        }
        fetch.getFetchGroup(
            groupId,
            fetchGroup -> {
                try {
                    Log.d(TAG, "FetchGroup: " + fetchGroup.getId());
                    List<Download> downloads = fetchGroup.getDownloads();
                    for (Download download : downloads) {
                        Log.d(TAG, "Download File:: " + download.getId() + " : " + download.getStatus() + " => " + download.getFile());
                        if (!new File(download.getFile()).exists()) {
                            Log.d(TAG, "File Removed:: " + download.getId() + " => " + download.getFile());
                            fetch.remove(download.getId());
                        } else {
                            switch (download.getStatus()) {
                                case COMPLETED -> Log.d(TAG, "COMPLETED:: " + download.getId() + " => " + download.getStatus());
                                case PAUSED -> {
                                    Log.d(TAG, "PAUSED:: " + download.getId() + " => " + download.getStatus());
                                    fetch.resume(download.getId());
                                }
                                case FAILED -> {
                                    Log.d(TAG, "FAILED:: " + download.getId() + " => " + download.getStatus());
                                    fetch.retry(download.getId());
                                }
                                case CANCELLED -> {
                                    Log.d(TAG, "CANCELLED:: " + download.getId() + " => " + download.getStatus());
                                    fetch.resume(download.getId());
                                }
                                case QUEUED -> {
                                    Log.d(TAG, "QUEUED:: " + download.getId() + " => " + download.getStatus());
                                    fetch.resume(download.getId());
                                }
                                default -> Log.d(TAG, "STATUS:: " + download.getId() + " => " + download.getStatus());
                            }
                        }
                    }
                } catch (FetchException e) {
                    Log.d(TAG, "FetchException: " + e.getMessage());
                }
            }
        );
    }
}
