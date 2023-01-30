/*
 *
 *  Copyright (C) 2022,2023 NXP
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
import android.content.Context;
import android.hardware.emvco.DiscoveryMode;
import android.hardware.emvco.IEmvco;
import android.hardware.emvco.IEmvcoClientCallback;
import android.hardware.emvco.IEmvcoProfileDiscovery;
import android.hardware.emvco.INfcStateChangeRequestCallback;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.nxp.emvco.EmvcoEvent;
import com.nxp.emvco.EmvcoStatus;
import com.nxp.emvco.IEMVCoClientCallback;

public final class ProfileDiscovery {
  private static final String TAG = ProfileDiscovery.class.getName();
  private static final int INIT_EMVCO_DELAY_MS = 200;
  private static final int MAX_RETRY_COUNT = 5;
  private static int retryCount = 0;
  private Context mContext;
  private IEMVCoClientCallback mEMVCoAppClientCallback = null;
  private static final int TASK_ENABLE_EMVCO_POLL = 1;
  private ProfileDiscoveryHandler mHandler;
  private static ProfileDiscovery mProfileDiscovery;
  private IEmvco mIEmvco;
  private IEmvcoProfileDiscovery mIEmvcoProfileDiscovery = null;
  private final EMVCoHalServiceDiedRecipient mEMVCoHalServiceDiedRecipient =
      new EMVCoHalServiceDiedRecipient();
  private com.nxp.emvco
      .INfcStateChangeRequestCallback nfcStateChangeRequestCallback;

  private ProfileDiscovery(Context context) {
    Log.e(TAG, "ProfileDiscovery");
    mContext = context;
    mHandler = new ProfileDiscoveryHandler();
    Log.i(TAG, "RegisterEMVCoEventListener ");
    mIEmvcoProfileDiscovery = getEmvcoHalService();
  }

  public static synchronized ProfileDiscovery getInstance(Context context) {
    if (mProfileDiscovery == null) {
      mProfileDiscovery = new ProfileDiscovery(context);
    }
    return mProfileDiscovery;
  }

  private INfcStateChangeRequestCallback.Stub mNfcStateChangeCallback =
      new INfcStateChangeRequestCallback.Stub() {
        @Override
        public void enableNfc(boolean turnOn) {
          Log.i(TAG, "setNfcState turnOn:" + turnOn);
          if (nfcStateChangeRequestCallback != null) {
            nfcStateChangeRequestCallback.enableNfc(turnOn);
          } else {
            Log.i(TAG, "setNfcState nfcStateChangeRequestCallback is NULL");
          }
        }

        @Override
        public int getInterfaceVersion() {
          return this.VERSION;
        }

        @Override
        public String getInterfaceHash() {
          return this.HASH;
        }
      };

  final class EMVCoHalServiceDiedRecipient implements IBinder.DeathRecipient {
    @Override
    public void binderDied() {
      Log.e(TAG, "EMVCoHalServiceDiedRecipient binderDied");
      mIEmvco.asBinder().unlinkToDeath(mEMVCoHalServiceDiedRecipient, 0);
      mIEmvco = null;
      mIEmvcoProfileDiscovery = null;
      if (mEMVCoAppClientCallback != null) {
        mEMVCoAppClientCallback.sendEvent(EmvcoEvent.EMVCO_CLOSE_CHNL_CPLT_EVT,
                                          EmvcoStatus.EMVCO_STATUS_OK);
      } else {
        Log.i(TAG, "sendEvent is NULL");
      }
      Message message = Message.obtain();
      message.what = TASK_ENABLE_EMVCO_POLL;
      mHandler.sendMessageDelayed(message, INIT_EMVCO_DELAY_MS);
    }
  }

  public void onNfcStateChange(int newState) {
    Log.i(TAG, "onNfcStateChange newState:" + newState);
    if (getEmvcoHalService() != null) {
      try {
        mIEmvcoProfileDiscovery.onNfcStateChange(newState);
      } catch (RemoteException e) {
        Log.e(TAG, "Failed to get EMVCo service " + e);
      }
    } else {
      Log.d(TAG, "Please check if HAL service is up"
                     + " and retry after some time");
    }
  }

  private IEmvcoClientCallback.Stub mEmvcoHalCallback =
      new IEmvcoClientCallback.Stub() {
        @Override
        public void sendData(byte[] data) {
          if (mEMVCoAppClientCallback != null) {
            mEMVCoAppClientCallback.sendData(data);
          } else {
            Log.i(TAG, "sendData is NULL");
          }
        }
        @Override
        public void sendEvent(int event, int status) {
          if (mEMVCoAppClientCallback != null) {
            mEMVCoAppClientCallback.sendEvent(EmvcoEvent.valueOf(event),
                                              EmvcoStatus.valueOf(status));
          } else {
            Log.d(TAG, "sendEvent is NULL");
          }
        }
        @Override
        public int getInterfaceVersion() {
          return this.VERSION;
        }

        @Override
        public String getInterfaceHash() {
          return this.HASH;
        }
      };

  @RequiresPermission(android.Manifest.permission.NFC)
  public void setEMVCoMode(int technologyToPool, boolean isStartEMVCo) {
    if (getEmvcoHalService() != null) {
      try {
        Log.i(TAG, "setEMVCoMode mode with technologyToPool:" +
                       technologyToPool + " isStartEMVCo:" + isStartEMVCo);
        mIEmvcoProfileDiscovery.setEMVCoMode((byte)technologyToPool,
                                             isStartEMVCo);
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    } else {
      Log.d(TAG, "Please check if HAL service is up"
                     + " and retry after some time");
    }
  }

  @RequiresPermission(android.Manifest.permission.NFC)
  public int getCurrentDiscoveryMode() {
    int status = DiscoveryMode.UN_KNOWN;
    if (getEmvcoHalService() != null) {
      try {
        status = mIEmvcoProfileDiscovery.getCurrentDiscoveryMode();
        Log.i(TAG, "getCurrentDiscoveryMode:" + status);
      } catch (RemoteException e) {
        e.printStackTrace();
        return status;
      }
    } else {
      Log.d(TAG, "Please check if HAL service is up"
                     + " and retry after some time");
    }
    return status;
  }

  @RequiresPermission(android.Manifest.permission.NFC)
  public void registerEventListener(IEMVCoClientCallback mEMVCoCallback)
      throws SecurityException {
    final int uid = android.os.Process.myUid();
    if (uid == android.os.Process.NFC_UID) {
      throw new SecurityException(
          "For security reasons, NFC process not allowed to listen for EMVCo data");
    }
    if (getEmvcoHalService() != null && mEMVCoCallback != null) {
      mEMVCoAppClientCallback = mEMVCoCallback;
      try {
        mIEmvcoProfileDiscovery.registerEMVCoEventListener(mEmvcoHalCallback);
      } catch (RemoteException e) {
        Log.e(TAG, "Failed to get EMVCo service " + e);
      }
    } else {
      Log.e(
          TAG,
          "App has not registered callback. Either EMVCO service not available or App callback is NULL");
    }
  }

  @RequiresPermission(android.Manifest.permission.NFC)
  public void
  registerNFCStateChangeCallback(com.nxp.emvco.INfcStateChangeRequestCallback
                                     iNfcStateChangeRequestCallback)
      throws SecurityException {
    Log.i(TAG, "registerNFCStateChangeCallback");
    final int uid = android.os.Process.myUid();
    if (uid != android.os.Process.NFC_UID) {
      throw new SecurityException(
          "For security reasons, only NFC process allowed to listen for this callback");
    }

    if (getEmvcoHalService() != null &&
        iNfcStateChangeRequestCallback != null) {
      try {
        nfcStateChangeRequestCallback = iNfcStateChangeRequestCallback;
        boolean status = mIEmvcoProfileDiscovery.registerNFCStateChangeCallback(
            mNfcStateChangeCallback);
        Log.d(TAG, "Register NfcStateChangeCallback status:" + status);
      } catch (RemoteException e) {
        Log.e(TAG, "Failed to get EMVCo service " + e);
      }
    } else {
      Log.d(
          TAG,
          "App has not registered callback. Either EMVCO service not available or INfcStateChangeRequestCallback callback is NULL");
    }
  }

  /** get handle to EMVCo HAL service interface */
  private IEmvcoProfileDiscovery getEmvcoHalService() {
    /* get a handle to EMVCo service */
    if (mIEmvcoProfileDiscovery != null) {
      return mIEmvcoProfileDiscovery;
    }
    IBinder service =
        ServiceManager.getService("android.hardware.emvco.IEmvco/default");
    if (service != null) {
      try {
        service.linkToDeath(mEMVCoHalServiceDiedRecipient, 0);
        mIEmvco = IEmvco.Stub.asInterface(service);
        mIEmvcoProfileDiscovery = mIEmvco.getEmvcoProfileDiscoveryInterface();
      } catch (RemoteException e) {
        Log.d(TAG, "Unable to register death recipient");
      }

    } else {
      Log.d(TAG, "Unable to acquire EMVCo HAL service");
    }
    return mIEmvcoProfileDiscovery;
  }

  final class ProfileDiscoveryHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case TASK_ENABLE_EMVCO_POLL: {
        Log.i(TAG, "TASK_ENABLE_EMVCO_POLL received");
        mIEmvcoProfileDiscovery = getEmvcoHalService();
        if (mIEmvcoProfileDiscovery != null) {
          try {
            final int uid = android.os.Process.myUid();
            boolean status = false;
            if (uid == android.os.Process.NFC_UID) {
              status = mIEmvcoProfileDiscovery.registerNFCStateChangeCallback(
                  mNfcStateChangeCallback);
              Log.d(TAG, "Register NfcStateChangeCallback status:" + status);
            } else {
              status = mIEmvcoProfileDiscovery.registerEMVCoEventListener(
                  mEmvcoHalCallback);
              Log.d(TAG,
                    "Register registerEMVCoEventListener status:" + status);
            }
            if (nfcStateChangeRequestCallback != null) {
              nfcStateChangeRequestCallback.enableNfc(true);
            }
          } catch (RemoteException e) {
            Log.e(TAG, "Failed to send onNfcStateChange");
          }
        } else {
          if (retryCount++ <= MAX_RETRY_COUNT) {
            Log.e(
                TAG,
                "EMVCO service not up after crash. Retrying to register listener with EMVCo HAL");
            Message message = Message.obtain();
            message.what = TASK_ENABLE_EMVCO_POLL;
            mHandler.sendMessageDelayed(message, INIT_EMVCO_DELAY_MS);
          }
        }
        break;
      }
      }
    }
  }
}
