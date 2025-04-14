package com.taleemabad.downloadmanager;

import android.app.Activity;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.ActivityCallback;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;

import java.util.ArrayList;
import java.util.List;

public class DocumentScanner {

    private static final String TAG = "DocumentScanner";
    private final Activity activity;
    private final ActivityResultLauncher<IntentSenderRequest> scannerLauncher;

    public DocumentScanner(Activity activity, ActivityResultLauncher<IntentSenderRequest> scannerLauncher) {
        this.activity = activity;
        this.scannerLauncher = scannerLauncher;
    }

    public void initScanner(PluginCall call, String mode) {
        GmsDocumentScannerOptions.Builder scannerOptions = new GmsDocumentScannerOptions.Builder();
        configureScannerOptions(call, scannerOptions, mode);

        GmsDocumentScanning.getClient(scannerOptions.build())
                .getStartScanIntent(activity)
                .addOnSuccessListener(intentSender ->
                        scannerLauncher.launch(new IntentSenderRequest.Builder(intentSender).build()))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Scanning failed: " + e.getMessage());
                    call.reject("Scanning failed: " + e.getMessage());
                });
    }

    private void configureScannerOptions(PluginCall call, GmsDocumentScannerOptions.Builder scannerOptions, String mode) {
        boolean enableGalleryImport = Boolean.TRUE.equals(call.getBoolean("enableGalleryImport", true));
        Integer pageLimit = call.getInt("pageLimit", 0);
        String outputFormats = call.getString("outputFormats", "JPEG");

        setOutputFormats(scannerOptions, outputFormats != null ? outputFormats : "JPEG");
        scannerOptions
                .setGalleryImportAllowed(enableGalleryImport)
                .setScannerMode(getScannerMode(mode));

        if (pageLimit != null && pageLimit > 0) {
            scannerOptions.setPageLimit(pageLimit);
        }
    }

    @ActivityCallback
    public void handleScanResult(ActivityResult result, PluginCall savedCall) {
        if (savedCall == null) return;

        JSObject response = new JSObject();

        if (result.getResultCode() == Activity.RESULT_OK) {
            GmsDocumentScanningResult scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.getData());
            List<String> imageUris = new ArrayList<>();

            if (scanResult != null && scanResult.getPages() != null) {
                for (GmsDocumentScanningResult.Page page : scanResult.getPages()) {
                    imageUris.add(page.getImageUri().toString());
                }
            }

            String pdfUri = scanResult != null && scanResult.getPdf() != null ? scanResult.getPdf().getUri().toString() : null;

            response.put("images", new JSArray(imageUris));
            response.put("pdf", pdfUri);
            savedCall.resolve(response);
        } else {
            response.put("status", "cancel");
            savedCall.reject("Scanning was cancelled or failed with unexpected error.");
        }
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
        return switch (mode) {
            case "FULL" -> GmsDocumentScannerOptions.SCANNER_MODE_FULL;
            case "BASE" -> GmsDocumentScannerOptions.SCANNER_MODE_BASE;
            case "BASE_WITH_FILTER" -> GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER;
            default -> {
                Log.e(TAG, "Unknown scanning mode: " + mode);
                yield -1;
            }
        };
    }
}