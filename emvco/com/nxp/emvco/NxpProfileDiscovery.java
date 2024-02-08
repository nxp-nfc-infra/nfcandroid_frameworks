/*
 *
 *  Copyright (C) 2022-2023 NXP
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of NXP nor the names of its contributors may be used
 * to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.nxp.emvco;

import android.annotation.RequiresPermission;
import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.nxp.emvco.INxpEMVCoClientCallback;
import com.nxp.emvco.NxpEmvcoEvent;
import com.nxp.emvco.NxpEmvcoStatus;
import vendor.nxp.emvco.INxpEmvco;
import vendor.nxp.emvco.INxpEmvcoClientCallback;
import vendor.nxp.emvco.INxpEmvcoProfileDiscovery;
import vendor.nxp.emvco.INxpNfcStateChangeRequestCallback;
import vendor.nxp.emvco.NxpDiscoveryMode;

public final class NxpProfileDiscovery {
  private static final String TAG = NxpProfileDiscovery.class.getName();
  private static final int INIT_EMVCO_DELAY_MS = 200;
  private static final int MAX_RETRY_COUNT = 5;
  private static int retryCount = 0;
  private Context mContext;
  private INxpEMVCoClientCallback mEMVCoAppClientCallback = null;
  private static final int TASK_ENABLE_EMVCO_POLL = 1;
  private ProfileDiscoveryHandler mHandler;
  private static NxpProfileDiscovery mProfileDiscovery;
  private INxpEmvco mINxpEmvco;
  private INxpEmvcoProfileDiscovery mINxpEmvcoProfileDiscovery = null;
  private final EMVCoHalServiceDiedRecipient mEMVCoHalServiceDiedRecipient =
      new EMVCoHalServiceDiedRecipient();
  private com.nxp.emvco
      .INxpNfcStateChangeRequestCallback nfcStateChangeRequestCallback;

  private NxpProfileDiscovery(Context context) {
    Log.e(TAG, "NxpProfileDiscovery");
    mContext = context;
    mHandler = new ProfileDiscoveryHandler();
    Log.i(TAG, "registerEmvcoEventListener ");
    mINxpEmvcoProfileDiscovery = getEmvcoHalService();
  }

  public static synchronized NxpProfileDiscovery getInstance(Context context) {
    if (mProfileDiscovery == null) {
      mProfileDiscovery = new NxpProfileDiscovery(context);
    }
    return mProfileDiscovery;
  }

  private INxpNfcStateChangeRequestCallback.Stub mNfcStateChangeCallback =
      new INxpNfcStateChangeRequestCallback.Stub() {
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
      mINxpEmvco.asBinder().unlinkToDeath(mEMVCoHalServiceDiedRecipient, 0);
      mINxpEmvco = null;
      mINxpEmvcoProfileDiscovery = null;
      if (mEMVCoAppClientCallback != null) {
        mEMVCoAppClientCallback.sendEvent(
            NxpEmvcoEvent.EMVCO_CLOSE_CHNL_CPLT_EVT,
            NxpEmvcoStatus.EMVCO_STATUS_OK);
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
        mINxpEmvcoProfileDiscovery.onNfcStateChange(newState);
      } catch (RemoteException e) {
        Log.e(TAG, "Failed to get EMVCo service " + e);
      }
    } else {
      Log.d(TAG, "Please check if HAL service is up"
                     + " and retry after some time");
    }
  }

  private INxpEmvcoClientCallback.Stub mEmvcoHalCallback =
      new INxpEmvcoClientCallback.Stub() {
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
            mEMVCoAppClientCallback.sendEvent(NxpEmvcoEvent.valueOf(event),
                                              NxpEmvcoStatus.valueOf(status));
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
        mINxpEmvcoProfileDiscovery.setEMVCoMode((byte)technologyToPool,
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
  public void setByteConfig(int type, int length, byte value) {
    if (getEmvcoHalService() != null) {
      try {
        Log.i(TAG, "setByteConfig mode with type:" + type + " value:" + value +
                       "length:" + length);
        mINxpEmvcoProfileDiscovery.setByteConfig(type, length, value);
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
    int status = NxpDiscoveryMode.UN_KNOWN;
    if (getEmvcoHalService() != null) {
      try {
        status = mINxpEmvcoProfileDiscovery.getCurrentDiscoveryMode();
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
  public void registerEventListener(INxpEMVCoClientCallback mEMVCoCallback)
      throws SecurityException {
    final int uid = android.os.Process.myUid();
    if (uid == android.os.Process.NFC_UID) {
      throw new SecurityException(
          "For security reasons, NFC process not allowed to listen for EMVCo data");
    }
    if (getEmvcoHalService() != null && mEMVCoCallback != null) {
      mEMVCoAppClientCallback = mEMVCoCallback;
      try {
        mINxpEmvcoProfileDiscovery.registerEMVCoEventListener(
            mEmvcoHalCallback);
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
  registerNFCStateChangeCallback(com.nxp.emvco.INxpNfcStateChangeRequestCallback
                                     INxpNfcStateChangeRequestCallback)
      throws SecurityException {
    Log.i(TAG, "registerNFCStateChangeCallback");
    final int uid = android.os.Process.myUid();
    if (uid != android.os.Process.NFC_UID) {
      throw new SecurityException(
          "For security reasons, only NFC process allowed to listen for this callback");
    }

    if (getEmvcoHalService() != null &&
        INxpNfcStateChangeRequestCallback != null) {
      try {
        nfcStateChangeRequestCallback = INxpNfcStateChangeRequestCallback;
        boolean status =
            mINxpEmvcoProfileDiscovery.registerNFCStateChangeCallback(
                mNfcStateChangeCallback);
        Log.d(TAG, "Register NfcStateChangeCallback status:" + status);
      } catch (RemoteException e) {
        Log.e(TAG, "Failed to get EMVCo service " + e);
      }
    } else {
      Log.d(
          TAG,
          "App has not registered callback. Either EMVCO service not available or INxpNfcStateChangeRequestCallback callback is NULL");
    }
  }

  /** get handle to EMVCo HAL service interface */
  private INxpEmvcoProfileDiscovery getEmvcoHalService() {
    /* get a handle to EMVCo service */
    if (mINxpEmvcoProfileDiscovery != null) {
      return mINxpEmvcoProfileDiscovery;
    }
    IBinder service =
        ServiceManager.getService("vendor.nxp.emvco.INxpEmvco/default");
    if (service != null) {
      try {
        service.linkToDeath(mEMVCoHalServiceDiedRecipient, 0);
        mINxpEmvco = INxpEmvco.Stub.asInterface(service);
        mINxpEmvcoProfileDiscovery =
            mINxpEmvco.getEmvcoProfileDiscoveryInterface();
      } catch (RemoteException e) {
        Log.d(TAG, "Unable to register death recipient");
      }

    } else {
      Log.d(TAG, "Unable to acquire EMVCo HAL service");
    }
    return mINxpEmvcoProfileDiscovery;
  }

  final class ProfileDiscoveryHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case TASK_ENABLE_EMVCO_POLL: {
        Log.i(TAG, "TASK_ENABLE_EMVCO_POLL received");
        mINxpEmvcoProfileDiscovery = getEmvcoHalService();
        if (mINxpEmvcoProfileDiscovery != null) {
          try {
            final int uid = android.os.Process.myUid();
            boolean status = false;
            if (uid == android.os.Process.NFC_UID) {
              status =
                  mINxpEmvcoProfileDiscovery.registerNFCStateChangeCallback(
                      mNfcStateChangeCallback);
              Log.d(TAG, "Register NfcStateChangeCallback status:" + status);
            } else {
              status = mINxpEmvcoProfileDiscovery.registerEMVCoEventListener(
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
