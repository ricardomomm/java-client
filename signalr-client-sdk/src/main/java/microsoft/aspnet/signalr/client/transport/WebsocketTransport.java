/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.transport;

import microsoft.aspnet.signalr.client.ConnectionBase;
import microsoft.aspnet.signalr.client.Logger;
import microsoft.aspnet.signalr.client.SignalRFuture;
import microsoft.aspnet.signalr.client.UpdateableCancellableFuture;
import microsoft.aspnet.signalr.client.http.HttpConnection;

/**
 * Implements the WebsocketTransport for the Java SignalR library
 * Created by stas on 07/07/14.
 */
public class WebsocketTransport extends HttpClientTransport {

    private WebsocketAdapter mWebsocketAdapter;
    private UpdateableCancellableFuture<Void> mConnectionFuture;
    private final Object mConnectionLock = new Object();

    public WebsocketTransport(Logger logger) {
        super(logger);
        mWebsocketAdapter = new TooTallSocketAdapter();
    }

    public WebsocketTransport(Logger logger, WebsocketAdapter websocketAdapter) {
        super(logger);
        mWebsocketAdapter = websocketAdapter;
    }

    public WebsocketTransport(Logger logger, WebsocketAdapter websocketAdapter, HttpConnection httpConnection) {
        super(logger, httpConnection);
        mWebsocketAdapter = websocketAdapter;
    }

    @Override
    public String getName() {
        return "webSockets";
    }

    @Override
    public boolean supportKeepAlive() {
        return true;
    }

    @Override
    public SignalRFuture<Void> start(ConnectionBase connection, ConnectionType connectionType, final DataResultCallback callback) {
        synchronized (mConnectionLock) {

            if (mWebsocketAdapter != null && mWebsocketAdapter.isConnecting())
                return mConnectionFuture;

            mConnectionFuture = new UpdateableCancellableFuture<Void>(null);

            if (mWebsocketAdapter == null) {
                mWebsocketAdapter = new TooTallSocketAdapter();
            }
            mWebsocketAdapter.setCallback(callback);
            mWebsocketAdapter.setConnectionFuture(mConnectionFuture);
            mWebsocketAdapter.setLogger(mLogger);
            mWebsocketAdapter.setName(getName());
            mWebsocketAdapter.setHeaders(connection.getHeaders());

            String url = mWebsocketAdapter.createURL(connection, connectionType, getName());

            mWebsocketAdapter.connect(url);

            connection.closed(new Runnable() {
                @Override
                public void run() {
                    mWebsocketAdapter.close();
                }
            });

            return mConnectionFuture;
        }
    }

    @Override
    public SignalRFuture<Void> stop() {
        synchronized (mConnectionLock) {
            mWebsocketAdapter.close();
            return new UpdateableCancellableFuture<Void>(null);
        }
    }

    @Override
    public SignalRFuture<Void> send(ConnectionBase connection, String data, DataResultCallback callback) {
        synchronized (mConnectionLock) {
            mWebsocketAdapter.send(data);
            return new UpdateableCancellableFuture<Void>(null);
        }
    }
}