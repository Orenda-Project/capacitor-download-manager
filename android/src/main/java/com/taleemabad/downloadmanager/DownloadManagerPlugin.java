package com.taleemabad.downloadmanager;

import android.app.Activity;
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
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.gson.Gson;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;

import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2core.DownloadBlock;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

@CapacitorPlugin(name = "DownloadManager")
public class DownloadManagerPlugin extends Plugin implements FetchListener {

    private ActivityResultLauncher<IntentSenderRequest> scannerLauncher;

    private DownloadManager downloadManager = null;
    private static final String TAG = "DownloadManager";

    @Override
    public void load() {
        BridgeActivity activity = (BridgeActivity) this.getActivity();
        scannerLauncher = activity.registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), this::handleScanResult);
    }

    private void initDownloadManager() {
        if (downloadManager == null)
            downloadManager = DownloadManager.getInstance(this.getActivity(), this);
    }


    private void initScanner(PluginCall call, String mode) {
        boolean enableGalleryImport = Boolean.TRUE.equals(call.getBoolean("enableGalleryImport", true));
        Integer pageLimit = call.getInt("pageLimit", 0);
        String outputFormats = call.getString("outputFormats", "BOTH");

        GmsDocumentScannerOptions.Builder scannerOptions = new GmsDocumentScannerOptions.Builder();

        setOutputFormats(scannerOptions, outputFormats != null ? outputFormats : "BOTH");

        scannerOptions
                .setGalleryImportAllowed(enableGalleryImport)
                .setScannerMode(getScannerMode(mode));

        if (pageLimit != null && pageLimit > 0) {
            scannerOptions.setPageLimit(pageLimit);
        }

        GmsDocumentScanning.getClient(scannerOptions.build())
                .getStartScanIntent(getActivity())
                .addOnSuccessListener(intentSender ->
                        scannerLauncher.launch(new IntentSenderRequest.Builder(intentSender).build()))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Scanning failed: " + e.getMessage());
                    call.reject("Scanning failed: " + e.getMessage());
                });

        saveCall(call);
    }

    @PluginMethod
    public void startScan(PluginCall call) {
        String mode = call.getString("mode", "FULL");
        initScanner(call, mode);
    }

    private void setOutputFormats(GmsDocumentScannerOptions.Builder scannerOptions, String outputFormats) {
        switch (outputFormats) {
            case "JPEG" ->
                    scannerOptions.setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG);
            case "PDF" ->
                    scannerOptions.setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_PDF);
            default ->
                    scannerOptions.setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_PDF);
        }
    }

    private int getScannerMode(String mode) {
        switch (mode) {
            case "FULL" -> {
                return GmsDocumentScannerOptions.SCANNER_MODE_FULL;
            }
            case "BASE" -> {
                return GmsDocumentScannerOptions.SCANNER_MODE_BASE;
            }
            case "BASE_WITH_FILTER" -> {
                return GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER;
            }
            default -> {
                Log.e(TAG, "Unknown scanning mode: " + mode);
                return -1;
            }
        }
    }

    private void handleScanResult(ActivityResult result) {
        PluginCall savedCall = getSavedCall();
        if (savedCall == null) return;

        if (result.getResultCode() == Activity.RESULT_OK) {
            GmsDocumentScanningResult scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.getData());
            List<String> imageUris = new ArrayList<>();
            assert scanResult != null;
            if (scanResult.getPages() != null) {
                for (GmsDocumentScanningResult.Page page : scanResult.getPages()) {
                    imageUris.add(page.getImageUri().toString());
                }
            }

            String pdfUri = scanResult.getPdf() != null ? scanResult.getPdf().getUri().toString() : null;

            JSObject ret = new JSObject();
            ret.put("images", new JSONArray(imageUris));
            ret.put("pdf", pdfUri);
            savedCall.resolve(ret);
        } else {
            savedCall.reject("Scanning was cancelled or failed with unexpected error.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @PluginMethod
    public void startDownload(PluginCall call) {
        saveCall(call);
        this.getActivity().getMainExecutor().execute(() -> {
            initDownloadManager();
            downloadManager.initDownloading(call);
        });
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
        Log.i(TAG, DownloadEvent.ON_DOWNLOAD_BLOCK_UPDATED + " : " + download);
        notifyListeners(DownloadEvent.ON_DOWNLOAD_BLOCK_UPDATED, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onError(@NonNull Download download, @NonNull Error error, @Nullable Throwable throwable) {
        Log.i(TAG, DownloadEvent.ON_ERROR + " : " + download);
        notifyListeners(DownloadEvent.ON_ERROR, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onPaused(@NonNull Download download) {
        Log.i(TAG, DownloadEvent.ON_PAUSED + " : " + download);
        notifyListeners(DownloadEvent.ON_PAUSED, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onProgress(@NonNull Download download, long l, long l1) {
        Log.i(TAG, DownloadEvent.ON_PROGRESS + " : " + download);
        notifyListeners(DownloadEvent.ON_PROGRESS, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onQueued(@NonNull Download download, boolean b) {
        Log.i(TAG, DownloadEvent.ON_QUEUED + " : " + download);
        notifyListeners(DownloadEvent.ON_QUEUED, new JSObject().put("download", new Gson().toJson(download)));
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
        Log.i(TAG, DownloadEvent.ON_STARTED + " : " + download);
        notifyListeners(DownloadEvent.ON_STARTED, new JSObject().put("download", new Gson().toJson(download)));
    }

    @Override
    public void onWaitingNetwork(@NonNull Download download) {
        Log.i(TAG, DownloadEvent.ON_WAITING_NETWORK + " : " + download);
        notifyListeners(DownloadEvent.ON_WAITING_NETWORK, new JSObject().put("download", new Gson().toJson(download)));
    }
}
