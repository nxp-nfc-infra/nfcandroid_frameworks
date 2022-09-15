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
import android.nfc.INfcAdapter;
import android.nfc.NfcAdapter;
import android.os.Binder;
import android.os.IBinder;
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
  private IEMVCoAppClientCallback mEMVCoAppClientCallback = null;
  private static INfcAdapter mNfcAdapter;
  private static INxpNfcDiscoveryProfile sNxpDiscoveryService;
  private IEMVCoHalClientCallback.Stub mEmvcoHalCallback =
      new IEMVCoHalClientCallback.Stub() {
        @Override
        public void sendData(byte[] data) {
          if (mEMVCoAppClientCallback != null) {
            mEMVCoAppClientCallback.sendData(data);
          } else {
            Log.d(TAG, "sendData is NULL");
          }
        }
        @Override
        public void sendEvent(EmvcoEvent event, EmvcoStatus status) {
          if (mEMVCoAppClientCallback != null) {
            mEMVCoAppClientCallback.sendEvent(event, status);
          } else {
            Log.d(TAG, "sendEvent is NULL");
          }
        }
      };
  private static final String TAG = NxpNfcDiscoveryProfile.class.getName();
  private static NxpNfcDiscoveryProfile mNxpNfcDiscoveryProfile;

  private NxpNfcDiscoveryProfile() {
    mNfcAdapter = getServiceInterface();
    sNxpDiscoveryService = getNxpNfcDiscoveryProfileAdapterInterface();
  }

  public static synchronized NxpNfcDiscoveryProfile getInstance() {
    if (mNxpNfcDiscoveryProfile == null) {
      mNxpNfcDiscoveryProfile = new NxpNfcDiscoveryProfile();
    }
    return mNxpNfcDiscoveryProfile;
  }

  @RequiresPermission(android.Manifest.permission.NFC)
  public void doSetEMVCoMode(int tech, boolean isStartEMVCo) {
    try {
      Log.i(TAG, "doSetEMVCoMode ");
      sNxpDiscoveryService.doSetEMVCoMode(tech, isStartEMVCo);
      return;
    } catch (RemoteException e) {
      e.printStackTrace();
      return;
    }
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
    try {
      Log.i(TAG, "doRegisterEMVCoEventListener ");
      if (mEMVCoCallback != null) {
        mEMVCoAppClientCallback = mEMVCoCallback;
      } else {
        Log.e(TAG, "App has not registered callback");
      }
      sNxpDiscoveryService.doRegisterEMVCoEventListener(mEmvcoHalCallback);
      return;
    } catch (RemoteException e) {
      e.printStackTrace();
      return;
    }
  }

  /** get handle to NFC service interface */
  private static INfcAdapter getServiceInterface() {
    /* get a handle to NFC service */
    IBinder b = ServiceManager.getService("nfc");
    if (b == null) {
      return null;
    }
    return INfcAdapter.Stub.asInterface(b);
  }

  private static INxpNfcDiscoveryProfile
  getNxpNfcDiscoveryProfileAdapterInterface() {
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
}
