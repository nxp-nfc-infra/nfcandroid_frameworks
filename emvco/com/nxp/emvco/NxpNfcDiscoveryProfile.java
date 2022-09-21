/*
 *
 *  Copyright (C) 2022 NXP
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.nxp.emvco;

import android.annotation.RequiresPermission;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.INfcAdapter;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFormatException;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.nxp.emvco.EmvcoEvent;
import com.nxp.emvco.EmvcoStatus;
import com.nxp.emvco.IEMVCoAppClientCallback;
import com.nxp.emvco.IEMVCoHalClientCallback;
import com.nxp.emvco.INxpNfcDiscoveryProfile;
import java.io.IOException;
import java.util.List;

public final class NxpNfcDiscoveryProfile {
  private static final String TAG = NxpNfcDiscoveryProfile.class.getName();
  private IEMVCoAppClientCallback mEMVCoAppClientCallback = null;
  private INxpNfcDiscoveryProfile sNxpDiscoveryService;
  private Object mNfcProfileSyncObj = new Object();
  private int mNfcState = NfcAdapter.STATE_OFF;
  private INfcAdapter mNfcAdapter;
  private static final int MSG_REGISTER_EMVCO_LISTENER = 1;
  private static final int MSG_SET_EMVCO_MODE = 2;
  private NxpNfcDiscoveryProfileHandler mHandler;
  private static NxpNfcDiscoveryProfile mNxpNfcDiscoveryProfile;

  private IEMVCoHalClientCallback.Stub mEmvcoHalCallback =
      new IEMVCoHalClientCallback.Stub() {
        @Override
        public void sendData(byte[] data) {
          Log.e(TAG, "sendData ");
          if (mEMVCoAppClientCallback != null) {
            Log.e(TAG, "sendData propogated to app");
            mEMVCoAppClientCallback.sendData(data);
          } else {
            Log.d(TAG, "sendData is NULL");
          }
        }
        @Override
        public void sendEvent(EmvcoEvent event, EmvcoStatus status) {
          Log.e(TAG, "sendEvent ");
          if (mEMVCoAppClientCallback != null) {
            Log.e(TAG, "sendEvent propogated to app");
            mEMVCoAppClientCallback.sendEvent(event, status);
          } else {
            Log.d(TAG, "sendEvent is NULL");
          }
        }
      };

  private NxpNfcDiscoveryProfile() {
    Log.e(TAG, "NxpNfcDiscoveryProfile");
    mHandler = new NxpNfcDiscoveryProfileHandler();
    mNfcAdapter = getServiceInterface();
    sNxpDiscoveryService = getNxpNfcDiscoveryProfileAdapterInterface();

    Message emvcoMsg = mHandler.obtainMessage();
    emvcoMsg.what = MSG_REGISTER_EMVCO_LISTENER;
    mHandler.sendMessage(emvcoMsg);
  }

  public static synchronized NxpNfcDiscoveryProfile getInstance() {
    if (mNxpNfcDiscoveryProfile == null) {
      mNxpNfcDiscoveryProfile = new NxpNfcDiscoveryProfile();
    }
    return mNxpNfcDiscoveryProfile;
  }

  @RequiresPermission(android.Manifest.permission.NFC)
  public void doSetEMVCoMode(int tech, boolean isStartEMVCo) {
    Message msg = mHandler.obtainMessage();
    msg.what = MSG_SET_EMVCO_MODE;
    msg.arg1 = tech;
    msg.arg2 = isStartEMVCo ? 1 : 0;
    mHandler.sendMessage(msg);
  }

  @RequiresPermission(android.Manifest.permission.NFC)
  public int doGetCurrentDiscoveryMode() {
    try {
      Log.i(TAG, "doGetCurrentDiscoveryMode ");
      return sNxpDiscoveryService.doGetCurrentDiscoveryMode();
    } catch (RemoteException e) {
      e.printStackTrace();
      return -1;
    }
  }

  @RequiresPermission(android.Manifest.permission.NFC)
  public void
  doRegisterEMVCoEventListener(IEMVCoAppClientCallback mEMVCoCallback) {
    if (mEMVCoCallback != null) {
      mEMVCoAppClientCallback = mEMVCoCallback;
    } else {
      Log.e(TAG, "App has not registered callback");
    }
  }

  /** get handle to NFC service interface */
  private INfcAdapter getServiceInterface() {
    /* get a handle to NFC service */
    IBinder b = ServiceManager.getService("nfc");
    if (b == null) {
      return null;
    }
    return INfcAdapter.Stub.asInterface(b);
  }

  private INxpNfcDiscoveryProfile getNxpNfcDiscoveryProfileAdapterInterface() {
    if (mNfcAdapter == null) {
      throw new UnsupportedOperationException(
          "You need a reference from NfcAdapter to use the "
          + " NXP NFC APIs");
    }
    try {
      IBinder b = mNfcAdapter.getNxpNfcDiscoveryProfileAdapterVendorInterface(
          "nxp_nfc_discovery");
      if (b == null) {
        return null;
      }
      return INxpNfcDiscoveryProfile.Stub.asInterface(b);
    } catch (RemoteException e) {
      return null;
    }
  }

  final class NxpNfcDiscoveryProfileHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case MSG_SET_EMVCO_MODE: {
        int technologyToPool = msg.arg1;
        Log.i(TAG, "doSetEMVCoMode technologyToPool:" + technologyToPool);
        boolean isStartEMVCo = (msg.arg2 == 1) ? true : false;
        try {
          Log.i(
              TAG,
              "doSetEMVCoMode NFC oFF so call doSetEmVCo mode with technologyToPool:" +
                  technologyToPool + " isStartEMVCo:" + isStartEMVCo);
          sNxpDiscoveryService.doSetEMVCoMode(technologyToPool, isStartEMVCo);
        } catch (RemoteException e) {
          e.printStackTrace();
        }
        break;
      }
      case MSG_REGISTER_EMVCO_LISTENER: {
        IEMVCoAppClientCallback mEMVCoCallback =
            (IEMVCoAppClientCallback)msg.obj;
        try {
          Log.i(TAG, "doRegisterEMVCoEventListener ");
          if (mEMVCoCallback != null) {
            mEMVCoAppClientCallback = mEMVCoCallback;
          } else {
            Log.e(TAG, "App has not registered callback");
          }
          sNxpDiscoveryService.doRegisterEMVCoEventListener(mEmvcoHalCallback);
        } catch (RemoteException e) {
          e.printStackTrace();
        }
        break;
      }
      }
    }
  }
}
