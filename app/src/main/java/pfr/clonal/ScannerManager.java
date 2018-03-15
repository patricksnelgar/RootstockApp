package pfr.clonal;

import android.content.Context;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.AidcManager.CreatedCallback;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.BarcodeReader.BarcodeListener;
import com.honeywell.aidc.ScannerUnavailableException;

import java.util.HashMap;
import java.util.Map;

class ScannerManager {

    private final String TAG = ScannerManager.class.getSimpleName();

    private AidcManager aidcManager;
    private BarcodeReader barcodeReader;
    private DebugUtil debugUtil;
    private String runEnvironment;
    private boolean hasScanner = false;

    private BarcodeFoundListener barcodeFoundListener;

    ScannerManager(Context context, BarcodeFoundListener l) {
        debugUtil = new DebugUtil();
        runEnvironment = context.getString(R.string.run_environment);
        barcodeFoundListener = l;

        AidcManager.create(context, new CreatedCallback() {
            @Override
            public void onCreated(AidcManager am) {

                aidcManager = am;

                barcodeReader = aidcManager.createBarcodeReader();

                try {

                    barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                            BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);

                    Map<String, Object> properties = new HashMap<>();
                    properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
                    properties.put(BarcodeReader.PROPERTY_MICRO_PDF_417_ENABLED, true);
                    properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, true);
                    properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);

                    barcodeReader.setProperties(properties);

                } catch (Exception e) {
                    debugUtil.logMessage(TAG, "Could not set property: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, runEnvironment);
                }

                hasScanner = claimScanner();
                if (hasScanner) {
                    registerBarcodeListener();
                } else {
                    debugUtil.logMessage(TAG, "Could not claim scanner", DebugUtil.LOG_LEVEL_ERROR, runEnvironment);
                }

            }
        });
    }

    private boolean claimScanner() {
        try {
            if (barcodeReader != null) {
                barcodeReader.claim();
            } else {
                debugUtil.logMessage(TAG, "reader is null", runEnvironment);
                barcodeReader = aidcManager.createBarcodeReader();
                barcodeReader.claim();
            }
        } catch (ScannerUnavailableException se) {
            debugUtil.logMessage(TAG, "Could not claim scanner.", DebugUtil.LOG_LEVEL_ERROR, runEnvironment);
            return false;
        }

        return true;
    }

    private void registerBarcodeListener() {

        barcodeReader.addBarcodeListener(new BarcodeListener() {

            @Override
            public void onBarcodeEvent(BarcodeReadEvent event) {
                debugUtil.logMessage(TAG, "Got barcode read event: " + event.getBarcodeData(), DebugUtil.LOG_LEVEL_INFO, runEnvironment);
                barcodeFoundListener.onBarcodeFound(event.getBarcodeData());
            }

            public void onFailureEvent(BarcodeFailureEvent failureEvent) {
                debugUtil.logMessage(TAG, "Barcode failure event", DebugUtil.LOG_LEVEL_ERROR, runEnvironment);
            }
        });
    }

    void onDestroy() {
        if (barcodeReader != null) {
            barcodeReader.release();
        }

        if (aidcManager != null) {
            aidcManager.close();
        }
    }

    public interface BarcodeFoundListener {
        void onBarcodeFound(String barcode);
    }

}