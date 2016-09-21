package microsoft.aspnet.signalr.client.http.android;

import android.util.Log;

import microsoft.aspnet.signalr.client.LogLevel;
import microsoft.aspnet.signalr.client.Logger;

public class LogCatLogger implements Logger {
    private final String tag;

    public LogCatLogger(String tag) {
        this.tag = tag;
    }

    @Override
    public void log(String message, LogLevel level) {
        switch (level) {
            case Verbose:
                Log.v(tag, message);
                break;
            case Critical:
                Log.w(tag, message);
                break;
            case Information:
                Log.i(tag, message);
                break;
        }
    }
}
