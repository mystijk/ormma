/*  Copyright (c) 2011 The ORMMA.org project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package org.ormma.controller;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.ormma.controller.util.OrmmaNetworkBroadcastReceiver;
import org.ormma.view.OrmmaView;

/**
 * The Class OrmmaNetworkController.  OrmmaController for interacting with network states
 */
public class OrmmaNetworkController extends OrmmaController {
	
	private static final String LOG_TAG = "OrmmaNetworkController";
	
	private ConnectivityManager mConnectivityManager;
	private int mNetworkListenerCount;
	private OrmmaNetworkBroadcastReceiver mBroadCastReceiver;
	private IntentFilter mFilter;

	/**
	 * Instantiates a new ormma network controller.
	 *
	 * @param adView the ad view
	 * @param context the context
	 */
	public OrmmaNetworkController(OrmmaView adView, Context context) {
		super(adView, context);
		mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * Gets the network.
	 *
	 * @return the network
	 */
	public String getNetwork() {
		NetworkInfo ni = mConnectivityManager.getActiveNetworkInfo();
        String networkType = "unknown";
		if (ni == null){
			networkType = "offline";
		}
		else{
			switch (ni.getState()) {
			case UNKNOWN:
				networkType = "unknown";
				break;
			case DISCONNECTED:
				networkType = "offline";
				break;
			default:
				int type = ni.getType();
				if (type == ConnectivityManager.TYPE_MOBILE)
					networkType = "cell";
				else if (type == ConnectivityManager.TYPE_WIFI)
					networkType = "wifi";
			}
		}
		Log.d(LOG_TAG, "getNetwork: " + networkType);
		return networkType;
	}

	/**
	 * Start network listener.
	 */
	public void startNetworkListener() {
		if (mNetworkListenerCount == 0) {
			mBroadCastReceiver = new OrmmaNetworkBroadcastReceiver(this);
			mFilter = new IntentFilter();
			mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		}
		mNetworkListenerCount++;
		mContext.registerReceiver(mBroadCastReceiver, mFilter);
	}

	/**
	 * Stop network listener.
	 */
	public void stopNetworkListener() {
		mNetworkListenerCount--;
		if (mNetworkListenerCount == 0) {
			mContext.unregisterReceiver(mBroadCastReceiver);
			mBroadCastReceiver = null;
			mFilter = null;

		}
	}

	/**
	 * On connection changed.
	 */
	public void onConnectionChanged() {
		String script = "window.ormmaview.fireChangeEvent({ network: \'" + getNetwork() + "\'});";
		Log.d(LOG_TAG, script );
		mOrmmaView.injectJavaScript(script);
	}

	/* (non-Javadoc)
	 * @see com.ormma.controller.OrmmaController#stopAllListeners()
	 */
	@Override
	public void stopAllListeners() {
		mNetworkListenerCount = 0;
		try {
			mContext.unregisterReceiver(mBroadCastReceiver);
		} catch (Exception e) {
		}
	}

}
