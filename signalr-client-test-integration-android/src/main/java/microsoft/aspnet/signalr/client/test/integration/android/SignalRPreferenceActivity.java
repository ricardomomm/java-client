/*
Copyright (c) Microsoft Open Technologies, Inc.
All Rights Reserved
See License.txt in the project root for license information.
*/

package microsoft.aspnet.signalr.client.test.integration.android;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * A {@link android.preference.PreferenceActivity} that presents a set of application settings.
 */
public class SignalRPreferenceActivity extends PreferenceActivity {
    @SuppressWarnings("deprecation")
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_general);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return false;
    }
}
