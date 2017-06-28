package echo.rootstockapp;

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

public class ScannerManager {

    private final String TAG = ScannerManager.class.getSimpleName();

    private AidcManager aidcManager;
    private BarcodeReader barcodeReader;
    private DebugUtil debugUtil;
    private String run_environment;
    private boolean hasScanner = false;

    public ScannerManager(Context context, DebugUtil db, String env) {
        debugUtil = db;
        run_environment = env;

        AidcManager.create(context, new CreatedCallback() {
            @Override
            public void onCreated(AidcManager am){
                             
                aidcManager = am;

                barcodeReader = aidcManager.createBarcodeReader();  
               
                if(barcodeReader!=null){
                    try{
                    
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                                                    BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
                        //barcodeReader.setProperty(BarcodeReader.PROPERTY_MICRO_PDF_417_ENABLED, true);

                        Map<String,Object> properties = new HashMap<String,Object>();
                        properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_MICRO_PDF_417_ENABLED, true);
                        properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, true);
                       // properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
                        properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);

                        barcodeReader.setProperties(properties);

                    } catch (Exception e){
                        debugUtil.logMessage(TAG, "Could not set property: " + e.getLocalizedMessage(), DebugUtil.LOG_LEVEL_ERROR, run_environment);
                    }

                    hasScanner = claimScanner();
                    if(hasScanner){
                        regsiterBarcodeListener();
                    } else {
                        debugUtil.logMessage(TAG,"Could not claim scanner", DebugUtil.LOG_LEVEL_ERROR, run_environment);
                    }
                } else {
                    debugUtil.logMessage(TAG,"Could not create Barcode Reader object", DebugUtil.LOG_LEVEL_ERROR, run_environment);
                }
                      
            }
        });
    }



    private boolean claimScanner(){
        try{
            if(barcodeReader != null){
                barcodeReader.claim();
            } else if (barcodeReader == null){
                debugUtil.logMessage(TAG, "reader is null", run_environment);
                barcodeReader = aidcManager.createBarcodeReader();
                barcodeReader.claim();
            }
        } catch (ScannerUnavailableException se){
            debugUtil.logMessage(TAG, "Could not claim scanner.", DebugUtil.LOG_LEVEL_ERROR, run_environment);
            return false;
        }

        return true;
    }

    private void regsiterBarcodeListener(){

        barcodeReader.addBarcodeListener(new BarcodeListener(){

            @Override
            public void onBarcodeEvent(BarcodeReadEvent event){
                debugUtil.logMessage(TAG,"Got barcode read event: " + event.getBarcodeData(), DebugUtil.LOG_LEVEL_INFO, run_environment);
                //databaseLookup(event.getBarcodeData());
            }

            public void onFailureEvent(BarcodeFailureEvent fevent){
                debugUtil.logMessage(TAG, "Barcode failure event", DebugUtil.LOG_LEVEL_ERROR, run_environment);
            }
        });
    }
    
    public void onDestroy(){
        if(barcodeReader != null){
            barcodeReader.release();
        }

        if(aidcManager != null){
            aidcManager.close();
        }
    }
    
}