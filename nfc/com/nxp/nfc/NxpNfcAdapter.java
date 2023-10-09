/*
 *
 *  The original Work has been changed by NXP.
 *
 *  Copyright 2013-2021,2023 NXP
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

package com.nxp.nfc;
import java.util.HashMap;
import java.util.Map;
import android.annotation.RequiresPermission;
import android.nfc.INfcAdapter;
import android.nfc.NfcAdapter;
import android.os.IBinder;
import android.os.ServiceManager;

import android.os.RemoteException;
import android.annotation.RequiresPermission;
import android.os.ServiceManager;
import android.util.Log;
import com.nxp.nfc.NfcTDAInfo;
import com.nxp.nfc.TdaResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public final class NxpNfcAdapter {
  private static final String TAG = "NXPNFC";
  // Guarded by NfcAdapter.class
  static boolean sIsInitialized = false;

  /**
   * The NfcAdapter object for each application context.
   * There is a 1-1 relationship between application context and
   * NfcAdapter object.
   */
  static HashMap<NfcAdapter, NxpNfcAdapter> sNfcAdapters =
      new HashMap(); // guard by NfcAdapter.class
    // Final after first constructor, except for
    // attemptDeadServiceRecovery() when NFC crashes - we accept a best effort
    // recovery
  private static INxpNfcAdapter sNxpService;
  private static INfcAdapter sService;
  private static INxpNfcTDA sNxpTdaService;

  private NxpNfcAdapter() {}
  /**
   * Returns the NxpNfcAdapter for application context,
   * or throws if NFC is not available.
   * @hide
   */
  public static synchronized NxpNfcAdapter
  getNxpNfcAdapter(NfcAdapter adapter) {
    if (!sIsInitialized) {
      if (adapter == null) {
        Log.v(TAG, "could not find NFC support");
        throw new UnsupportedOperationException();
      }
      sService = getServiceInterface();
      if (sService == null) {
        Log.e(TAG, "could not retrieve NFC service");
        throw new UnsupportedOperationException();
      }
      sNxpTdaService = getNxpNfcTdaAdapterInterface();
      if (sNxpTdaService == null) {
        Log.e(TAG, "could not retrieve NXP NFC TDA service");
        throw new UnsupportedOperationException();
      }
      sNxpService = getNxpNfcAdapterInterface();
      if (sNxpService == null) {
        Log.e(TAG, "could not retrieve NXP NFC service");
        throw new UnsupportedOperationException();
      }
      sIsInitialized = true;
    }
    NxpNfcAdapter nxpAdapter = sNfcAdapters.get(adapter);
    if (nxpAdapter == null) {
      nxpAdapter = new NxpNfcAdapter();
      sNfcAdapters.put(adapter, nxpAdapter);
    }
    return nxpAdapter;
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

  /**
   * NFC service dead - attempt best effort recovery
   * @hide
   */
  private static void attemptDeadServiceRecovery(Exception e) {
    Log.e(TAG, "Service dead - attempting to recover", e);
    INfcAdapter service = getServiceInterface();
    if (service == null) {
      Log.e(TAG, "could not retrieve NFC service during service recovery");
      return;
    }

    sService = service;
    sNxpTdaService = getNxpNfcTdaAdapterInterface();
    sNxpService = getNxpNfcAdapterInterface();
    return;
  }
   /**
     * @hide
     */
    public static INxpNfcAdapter getNxpNfcAdapterInterface() {
      if (sService == null) {
        throw new UnsupportedOperationException(
            "You need a reference from NfcAdapter to use the "
            + " NXP NFC APIs");
      }
      try {
        IBinder b = sService.getNfcAdapterVendorInterface("nxp");
        if (b == null) {
          return null;
        }
        return INxpNfcAdapter.Stub.asInterface(b);
      } catch (RemoteException e) {
        return null;
      }
    }
  /**
   * @hide
   */
  public static INxpNfcTDA getNxpNfcTdaAdapterInterface() {
    if (sService == null) {
      throw new UnsupportedOperationException(
          "You need a reference from NfcAdapter to use the "
          + " NXP NFC APIs");
    }
    try {
      IBinder b = sService.getNxpNfcTdaAdapterVendorInterface("nxp_nfc_tda");
      if (b == null) {
        return null;
      }
      return INxpNfcTDA.Stub.asInterface(b);
    } catch (RemoteException e) {
      return null;
    }
  }
   /**
     * This API performs writes of T4T data to Nfcee.
     * @param fileId File Id to which to write
     * @param data data bytes to be written
     * @param length current data length
     * @return number of bytes written if success else negative number of
                error code listed as here .
                -1  STATUS_FAILED
                -2  ERROR_RF_ACTIVATED
                -3  ERROR_MPOS_ON
                -4  ERROR_NFC_NOT_ON
                -5  ERROR_INVALID_FILE_ID
                -6  ERROR_INVALID_LENGTH
                -7  ERROR_CONNECTION_FAILED
                -8  ERROR_EMPTY_PAYLOAD
                -9  ERROR_NDEF_VALIDATION_FAILED
     * <p>Requires {@link   android.Manifest.permission#NFC} permission.
     */
    @RequiresPermission(android.Manifest.permission.NFC)
    public int doWriteT4tData(byte[] fileId, byte[] data, int length) {
      try {
        return sNxpService.doWriteT4tData(fileId, data, length);
      } catch (RemoteException e) {
        e.printStackTrace();
        attemptDeadServiceRecovery(e);
        return -1;
      }
    }

    /**
     * This API performs reading of T4T content of Nfcee.
     * @param fileId : File Id from which to read
     * @return read bytes :-Returns read message if success
     *                      Returns null if failed to read
     *                      Returns 0xFF if file is empty.
     * <p>Requires {@link   android.Manifest.permission#NFC} permission.
     */
    @RequiresPermission(android.Manifest.permission.NFC)
    public byte[] doReadT4tData(byte[] fileId) {
      try {
        return sNxpService.doReadT4tData(fileId);
      } catch (RemoteException e) {
        e.printStackTrace();
        attemptDeadServiceRecovery(e);
        return null;
      }
    }

  @RequiresPermission(android.Manifest.permission.NFC)
  public NfcTDAInfo[] discoverTDA(TdaResult tdaResult) {
    try {
      return sNxpTdaService.discoverTDA(tdaResult);
    } catch (RemoteException e) {
      e.printStackTrace();
      attemptDeadServiceRecovery(e);
      return null;
    }
  }

  @RequiresPermission(android.Manifest.permission.NFC)
  public byte openTDA(byte tdaID, boolean standBy, TdaResult tdaResult) {
    byte inValidCid = 0x00;
    try {
      return sNxpTdaService.openTDA(tdaID, standBy, tdaResult);
    } catch (RemoteException e) {
      e.printStackTrace();
      attemptDeadServiceRecovery(e);
      return inValidCid;
    }
  }

  @RequiresPermission(android.Manifest.permission.NFC)
  public byte[] transceive(byte[] in_cmd_data, TdaResult tdaResult) {
    try {
      return sNxpTdaService.transceive(in_cmd_data, tdaResult);
    } catch (RemoteException e) {
      e.printStackTrace();
      attemptDeadServiceRecovery(e);
      return null;
    }
  }

  @RequiresPermission(android.Manifest.permission.NFC)
  public void closeTDA(byte tdaID, boolean standBy, TdaResult tdaResult) {
    try {
      sNxpTdaService.closeTDA(tdaID, standBy, tdaResult);
    } catch (RemoteException e) {
      e.printStackTrace();
      attemptDeadServiceRecovery(e);
    }
    return;
  }
}
