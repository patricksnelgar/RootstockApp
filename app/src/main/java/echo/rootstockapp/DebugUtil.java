package echo.rootstockapp;

import android.util.Log;
import java.lang.String;

public class DebugUtil {

    public static final String LOG_LEVEL_INFO = "info";
    public static final String LOG_LEVEL_DEBUG = "debug";
    public static final String LOG_LEVEL_WARN = "warn";
    public static final String LOG_LEVEL_ERROR = "error";

    public DebugUtil() {}

    public void logMessage(String tag, String message, String dev){
        if(dev.equals("DEV")){
            Log.d(tag,message);
        }
    }

    public void logMessage(String tag, String message, String level, String dev){
        if(dev.equals("DEV")){
            switch(level){
                case LOG_LEVEL_DEBUG:
                    Log.d(tag, message);
                    break;
                case LOG_LEVEL_WARN:
                    Log.w(tag, message);
                    break;
                case LOG_LEVEL_INFO:
                    Log.i(tag, message);
                    break;
                case LOG_LEVEL_ERROR:
                    Log.e(tag, message);
                    break;
            }
        }
    }
}