/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.tests.util;

import java.util.ArrayList;
import java.util.List;

import microsoft.aspnet.signalr.client.ConnectionState;
import microsoft.aspnet.signalr.client.SignalRFuture;

public class MultiResult {
    public boolean booleanResult = false;
    public int intResult = 0;
    public String stringResult = null;
    public SignalRFuture<?> futureResult = null;
    public final List<Object> listResult = new ArrayList<Object>();
    public final List<Throwable> errorsResult = new ArrayList<Throwable>();
    public final List<ConnectionState> statesResult = new ArrayList<ConnectionState>();
}
