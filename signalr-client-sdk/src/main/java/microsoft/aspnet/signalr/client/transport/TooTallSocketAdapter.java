package microsoft.aspnet.signalr.client.transport;

import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.util.Charsetfunctions;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLSocketFactory;

import microsoft.aspnet.signalr.client.LogLevel;

/**
 * Created by Yitz on 3/7/2016.
 */
public class TooTallSocketAdapter extends WebsocketAdapter {

    private static final Gson gson = new Gson();
    private WebSocketClient _tooTallNateSocket;
    private String _prefix;

    public TooTallSocketAdapter() {}

    public void onOpen() {
        mConnectionFuture.setResult(null);
    }

    public void onMessage(String message) {
        mCallback.onData(message);
    }

    public void onError(Throwable e) {
        mConnectionFuture.triggerError(e);
        _tooTallNateSocket.close();
    }

    public boolean isConnecting() {
        return _tooTallNateSocket != null && _tooTallNateSocket.isConnecting();
    }

    public void connect(String url) {

        if (isConnecting())
            return;

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            mConnectionFuture.triggerError(e);
            return;
        }

        if (_tooTallNateSocket == null) {
            _tooTallNateSocket = new WebSocketClient(uri, new Draft_17(), mHeaders, 0) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    TooTallSocketAdapter.this.onOpen();
                }

                @Override
                public void onMessage(String s) {
                    TooTallSocketAdapter.this.onMessage(s);
                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    _tooTallNateSocket.close();
                }

                @Override
                public void onError(Exception e) {
                    TooTallSocketAdapter.this.onError(e);
                }

                @Override
                public void onFragment(Framedata frame) {
                    try {
                        String decodedString = Charsetfunctions.stringUtf8(frame.getPayloadData());

                        if (decodedString.equals("]}")) {
                            return;
                        }

                        if (decodedString.endsWith(":[") || null == _prefix) {
                            _prefix = decodedString;
                            return;
                        }

                        String simpleConcatenate = _prefix + decodedString;

                        if (isJSONValid(simpleConcatenate)) {
                            onMessage(simpleConcatenate);
                        } else {
                            String extendedConcatenate = simpleConcatenate + "]}";
                            if (isJSONValid(extendedConcatenate)) {
                                onMessage(extendedConcatenate);
                            } else {
                                log("invalid json received:" + decodedString, LogLevel.Critical);
                            }
                        }
                    } catch (InvalidDataException e) {
                        e.printStackTrace();
                    }
                }
            };
        }

        if (url.startsWith("wss://")) {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            try {
                _tooTallNateSocket.setSocket(factory.createSocket());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        _tooTallNateSocket.connect();
    }

    public void close() {
        _tooTallNateSocket.close();
    }

    public void send(String data) {
        _tooTallNateSocket.send(data);
    }

    private String getName() {
        return mName;
    }

    private boolean isJSONValid(String test){
        try {
            gson.fromJson(test, Object.class);
            return true;
        } catch(com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }

    private void log(String message, LogLevel level) {
        mLogger.log(getName() + " - " + message, level);
    }

    private void log(Throwable error) {
        mLogger.log(getName() + " - Error: " + error.toString(), LogLevel.Critical);
    }
}