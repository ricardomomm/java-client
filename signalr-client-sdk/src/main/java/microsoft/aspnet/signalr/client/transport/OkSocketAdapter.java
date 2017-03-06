package microsoft.aspnet.signalr.client.transport;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.*;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import microsoft.aspnet.signalr.client.LogLevel;
import okio.Buffer;

/**
 * Implements the WebsocketTransport for the Java SignalR library
 * Created by stas on 07/07/14.
 */
public class OkSocketAdapter extends WebsocketAdapter {

    private WebSocket mWebsocket;
    private WebSocketCall mWebsocketCall;
    private WebSocketListener mCurrentWebSocketListener;
    private ExecutorService mSendExecutor;
    private OkHttpClient mClient;

    public OkSocketAdapter() { }

    private String getName() {
        return mName;
    }

    public void onOpen() {
        mConnectionFuture.setResult(null);
    }

    public void onMessage(String message) {
        mCallback.onData(message);
    }

    public void onError(Throwable e) {
        mConnectionFuture.triggerError(e);
        close();
    }

    public boolean isConnecting() {
        return mCurrentWebSocketListener != null;
    }

    public void connect(String url) {

        if (isConnecting())
            return;

        mCurrentWebSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                mWebsocket = webSocket;
                OkSocketAdapter.this.onOpen();
            }

            @Override
            public void onFailure(IOException e, Response response) {
                OkSocketAdapter.this.onError(e);
            }

            @Override
            public void onMessage(ResponseBody message) throws IOException {
                OkSocketAdapter.this.onMessage(message.string());
                message.close();
            }

            @Override
            public void onPong(Buffer payload) {
                log("Pong " + payload.readUtf8(), LogLevel.Information);
            }

            @Override
            public void onClose(int code, String reason) {
                log("Connection closed [{ code: " + code + ", reason: '" + reason + "' }]", LogLevel.Information);
            }
        };


        mClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .headers(Headers.of(mHeaders))
                .url(url)
                .build();

        mWebsocketCall = WebSocketCall.create(mClient, request);
        mWebsocketCall.enqueue(mCurrentWebSocketListener);
    }

    public void send(final String data) {
        if (mWebsocket == null) {
            return;
        }

        if (mSendExecutor == null) {
            mSendExecutor = Executors.newSingleThreadExecutor();
        }

        mSendExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mConnectionFuture.isCancelled()) {
                    return;
                }

                try {
                    mWebsocket.sendMessage(RequestBody.create(WebSocket.TEXT, data));
                } catch (IOException e) {
                    if (!mConnectionFuture.isCancelled()) {
                        mConnectionFuture.triggerError(e);
                    }
                }
            }
        });
    }

    public void close() {

        if (mSendExecutor != null) {
            mSendExecutor.shutdownNow();
            mSendExecutor = null;
        }
        if (mWebsocket != null) {
            try {
                mWebsocket.close(1000, "");
            } catch (Exception e) {
                log(e);
            } finally {
                mWebsocket = null;
            }
        }
        if (mWebsocketCall != null) {
            mWebsocketCall.cancel();
            mWebsocketCall = null;
        }
        if (mClient != null) {
            mClient.dispatcher().executorService().shutdownNow();
            mClient = null;
        }
        mCurrentWebSocketListener = null;
    }

    private void log(String message, LogLevel level) {
        mLogger.log(getName() + " - " + message, level);
    }

    private void log(Throwable error) {
        mLogger.log(getName() + " - Error: " + error.toString(), LogLevel.Critical);
    }

}
