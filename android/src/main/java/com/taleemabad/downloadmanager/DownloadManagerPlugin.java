package com.taleemabad.downloadmanager;

import android.os.Build;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.getcapacitor.BridgeActivity;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.gson.Gson;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2core.DownloadBlock;


import java.util.ArrayList;
import java.util.List;

@CapacitorPlugin(name = "DownloadManager")
public class DownloadManagerPlugin extends Plugin implements FetchListener {

    private ActivityResultLauncher<IntentSenderRequest> scannerLauncher;

    private DownloadManager downloadManager = null;
    private static final String TAG = "DownloadManager";
    private DocumentScanner documentScanner = null;

    @Override
    public void load() {
        BridgeActivity activity = (BridgeActivity) this.getActivity();
        scannerLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), this::handleScanResult);
        documentScanner = new DocumentScanner(activity, scannerLauncher);
    }

    @PluginMethod
    public void startScan(PluginCall call) {
        String mode = call.getString("mode", "FULL");
        documentScanner.initScanner(call, mode);
    }

    @ActivityCallback
    private void handleScanResult(ActivityResult result) {
        PluginCall savedCall = getSavedCall();
        documentScanner.handleScanResult(result, savedCall);
    }

    private void initDownloadManager() {
        if (downloadManager == null)
            downloadManager = DownloadManager.getInstance(this.getActivity(), this);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @PluginMethod
    public void startDownload(PluginCall call) {
        try {
            saveCall(call);
            initDownloadManager();
            downloadManager.initDownloading(call);
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @PluginMethod
    public void startDownloadWithTag(PluginCall call) {
        try {
            saveCall(call);
            initDownloadManager();
            downloadManager.initDownloadingWithTag(call);
        } catch (Exception e) {
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void getDownloadList(PluginCall call) {
        try {
            initDownloadManager();
            downloadManager.getDownloads(call);
        } catch (Exception e) {
            JSObject ret = new JSObject();
            ret.put("error", e.getMessage());
            notifyListeners("downloadList", ret);
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void getDownloadListById(PluginCall call) {
        try {
            initDownloadManager();
            downloadManager.getDownloadById(call);
        } catch (Exception e) {
            JSObject ret = new JSObject();
            ret.put("error", e.getMessage());
            notifyListeners("downloadList", ret);
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void removeDownloads(PluginCall call) {
        try {
            initDownloadManager();
            List<Integer> ids = new ArrayList<>();
            JSArray downloadIds = call.getArray("value");
            for (int i = 0; i < downloadIds.length(); i++) {
                ids.add(downloadIds.optInt(i));
            }
            downloadManager.deleteDownloads(ids);
            call.resolve();
        } catch (Exception e) {
            JSObject ret = new JSObject();
            ret.put("error", e.getMessage());
            notifyListeners("removeDownload", ret);
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void pauseDownloads(PluginCall call) {
        try {
            initDownloadManager();
            List<Integer> ids = new ArrayList<>();
            JSArray downloadIds = call.getArray("value");
            for (int i = 0; i < downloadIds.length(); i++) {
                ids.add(downloadIds.optInt(i));
            }
            downloadManager.pauseDownloads(ids);
            call.resolve();
        } catch (Exception e) {
            JSObject ret = new JSObject();
            ret.put("error", e.getMessage());
            notifyListeners("pauseDownload", ret);
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void cancelDownloads(PluginCall call) {
        try {
            initDownloadManager();
            List<Integer> ids = new ArrayList<>();
            JSArray downloadIds = call.getArray("value");
            for (int i = 0; i < downloadIds.length(); i++) {
                ids.add(downloadIds.optInt(i));
            }
            downloadManager.cancelDownloads(ids);
            call.resolve();
        } catch (Exception e) {
            JSObject ret = new JSObject();
            ret.put("error", e.getMessage());
            notifyListeners("cancelDownload", ret);
            call.reject(e.getMessage());
        }
    }

    @PluginMethod
    public void resumeDownloads(PluginCall call) {
        try {
            initDownloadManager();
            downloadManager.resumeDownloads();
            call.resolve();
        } catch (Exception e) {
            JSObject ret = new JSObject();
            ret.put("error", e.getMessage());
            notifyListeners("resumeDownload", ret);
            call.reject(e.getMessage());
        }
    }

    @Override
    public void onAdded(@NonNull Download download) {
        Log.i(TAG, DownloadEvent.ON_ADDED + " : " + download);
        notifyListeners(DownloadEvent.ON_ADDED, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onCancelled(@NonNull Download download) {
        Log.i(TAG, DownloadEvent.ON_CANCELLED + download);
        notifyListeners(DownloadEvent.ON_CANCELLED, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onCompleted(@NonNull Download download) {
        Log.i(TAG, DownloadEvent.ON_COMPLETED + download);
        notifyListeners(DownloadEvent.ON_COMPLETED, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onDeleted(@NonNull Download download) {
        Log.i(TAG, DownloadEvent.ON_DELETED + download);
        notifyListeners(DownloadEvent.ON_DELETED, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onDownloadBlockUpdated(@NonNull Download download, @NonNull DownloadBlock downloadBlock, int i) {
    }

    @Override
    public void onError(@NonNull Download download, @NonNull Error error, @Nullable Throwable throwable) {
        Log.i(TAG, DownloadEvent.ON_ERROR + " : " + download);
        notifyListeners(DownloadEvent.ON_ERROR, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onPaused(@NonNull Download download) {
        Log.i(TAG, DownloadEvent.ON_PAUSED + download);
        notifyListeners(DownloadEvent.ON_PAUSED, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onProgress(@NonNull Download download, long l, long l1) {
        Log.i(TAG, DownloadEvent.ON_PROGRESS + " : " + download);
        notifyListeners(DownloadEvent.ON_PROGRESS, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onQueued(@NonNull Download download, boolean b) {
    }

    @Override
    public void onRemoved(@NonNull Download download) {
        Log.i(TAG, DownloadEvent.ON_REMOVED + " : " + download);
        notifyListeners(DownloadEvent.ON_REMOVED, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onResumed(@NonNull Download download) {
        Log.i(TAG, DownloadEvent.ON_RESUMED + " : " + download);
        notifyListeners(DownloadEvent.ON_RESUMED, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onStarted(@NonNull Download download, @NonNull List<? extends DownloadBlock> list, int i) {
    }

    @Override
    public void onWaitingNetwork(@NonNull Download download) {
    }


}
