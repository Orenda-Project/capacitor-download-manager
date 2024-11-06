package com.taleemabad.downloadmanager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.google.gson.Gson;
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

import org.json.JSONException;
import org.json.JSONObject;

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
                        .setHttpDownloader(
                                new OkHttpDownloader(
                                        Downloader.FileDownloaderType.SEQUENTIAL)
                        )
                        .setDownloadConcurrentLimit(1)
                        .setAutoRetryMaxAttempts(3)
                        .setNamespace(namespace)
                        .setGlobalNetworkType(NetworkType.ALL)
                        .enableAutoStart(true)
                        .enableRetryOnNetworkGain(true)
                        .enableFileExistChecks(true)
                        .enableLogging(true)
                        .build()
        );
    }

    private void startDownloading(List<String> urls) {
        fetch.addListener(mFetchListener);
        fetch.enqueue(getFetchRequests(urls), updatedRequests -> {
            Log.i(TAG, "enqueue: " + updatedRequests);
        });
    }

    private void startDownloadingWithTag(List<JSONObject> urls) {
        System.out.println("startDownloadingWithTag: " + urls.toString());
        fetch.addListener(mFetchListener);
        List<Request> requests = getFetchRequestsWithTag(urls);
        fetch.enqueue(requests, updatedRequests -> {
            Log.i(TAG, "enqueue: " + updatedRequests);
        });
    }

    private List<Request> getFetchRequests(List<String> urls) {
        Log.i(TAG, "initFetch: " + urls.toString());
        ArrayList<Request> requests = new ArrayList<>();
        for (String url : urls) {
            String fileName = Utils.getFilePath(url, mContext);
            Request request = new Request(url, fileName);
            request.setGroupId(groupId);
            request.setPriority(Priority.HIGH);
            request.setNetworkType(NetworkType.ALL);
            requests.add(request);
        }
        return requests;
    }

    private List<Request> getFetchRequestsWithTag(List<JSONObject> urls) {
        Log.i(TAG, "initFetch: " + urls.toString());
        ArrayList<Request> requests = new ArrayList<>();
        for (JSONObject url : urls) {
            try {
                String urlStr = url.getString("url");
                String tag = url.getString("tag");
                String fileName = Utils.getFilePath(urlStr, mContext);
                Request request = new Request(urlStr, fileName);
                request.setGroupId(groupId);
                request.setTag(tag);
                request.setPriority(Priority.HIGH);
                request.setNetworkType(NetworkType.ALL);
                requests.add(request);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i(TAG, "getFetchRequestsWithTag: " + e.getMessage());
            }
        }
        return requests;
    }

    public void initDownloading(PluginCall call) {
        JSObject ret = new JSObject();
        try {
            JSArray url = call.getArray("url");
            ret.put("value", url);
            startDownloading(url.toList());
        } catch (Exception e) {
            ret.put("error", e.getMessage());
        }
        call.resolve(ret);
    }

    public void initDownloadingWithTag(PluginCall call) {
        JSObject ret = new JSObject();
        try {
            JSArray url = call.getArray("url");
            ret.put("value", url);
            startDownloadingWithTag(url.toList());
        } catch (Exception e) {
            ret.put("error", e.getMessage());
        }
        call.resolve(ret);
    }

    public void getDownloads(PluginCall call) {
        if (fetch == null) {
            fetch = init();
        }
        List<Download> downloadList = new ArrayList<>();
        fetch.getFetchGroup(groupId, fetchGroup -> {
            try {
                List<Download> downloads = fetchGroup.getDownloads();
                for (Download download : downloads) {
                    Log.i(TAG, "Download File:: " + download);
                    downloadList.add(download);
                }
                JSObject ret = new JSObject();
                ret.put("download", new Gson().toJson(downloadList));
                call.resolve(ret);
            } catch (FetchException e) {
                Log.i(TAG, "FetchException: " + e.getMessage());
            }
        });
    }

    public void resumeDownloads() {
        try {
            if (fetch == null) {
                fetch = init();
            }
            fetch.addListener(mFetchListener);
            fetch.resumeGroup(groupId);
        } catch (Exception e) {
            Log.i(TAG, "resumeDownloads: " + e.getMessage());
        }
    }

    public void deleteDownloads(List<Integer> ids) {
        try {
            if (fetch == null) {
                fetch = init();
            }
            fetch.delete(ids);
        } catch (Exception e) {
            Log.i(TAG, "deleteDownloads: " + e.getMessage());
        }
    }

    public void pauseDownloads(List<Integer> ids) {
        try {
            if (fetch == null) {
                fetch = init();
            }
            fetch.pause(ids);
        } catch (Exception e) {
            Log.i(TAG, "pauseDownloads: " + e.getMessage());
        }
    }
}
