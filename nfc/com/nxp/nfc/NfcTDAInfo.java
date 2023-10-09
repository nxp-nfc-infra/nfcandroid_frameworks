/*
 * Copyright 2023 NXP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nxp.nfc;
import com.nxp.nfc.CardTLVInfo;
import java.util.Arrays;
/**
 * @brief This structure defines the propery of smart card connected over TDA
 */
public class NfcTDAInfo implements android.os.Parcelable {
  public byte id = 0;
  public int status;
  public byte numberOfProtocols = 0;
  public int[] protocols;
  public byte numberOfCardInfo = 0;
  public CardTLVInfo[] cardTLVInfo;

  public NfcTDAInfo() {}

  public NfcTDAInfo(NfcTDAInfo other) {

    this.id = other.id;
    this.status = other.status;
    this.numberOfProtocols = other.numberOfProtocols;
    this.protocols = Arrays.copyOf(other.protocols, other.protocols.length);
    this.numberOfCardInfo = other.numberOfCardInfo;
    this.cardTLVInfo = new CardTLVInfo[other.cardTLVInfo.length];

    for (int i = 0; i < other.cardTLVInfo.length; i++) {
      this.cardTLVInfo[i] = new CardTLVInfo(other.cardTLVInfo[i]);
    }
  }

  public static final android.os.Parcelable.Creator<NfcTDAInfo> CREATOR =
      new android.os.Parcelable.Creator<NfcTDAInfo>() {
        @Override
        public NfcTDAInfo createFromParcel(android.os.Parcel _aidl_source) {
          NfcTDAInfo _aidl_out = new NfcTDAInfo();
          _aidl_out.readFromParcel(_aidl_source);
          return _aidl_out;
        }
        @Override
        public NfcTDAInfo[] newArray(int _aidl_size) {
          return new NfcTDAInfo[_aidl_size];
        }
      };
  @Override
  public final void writeToParcel(android.os.Parcel _aidl_parcel,
                                  int _aidl_flag) {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeByte(id);
    _aidl_parcel.writeInt(status);
    _aidl_parcel.writeByte(numberOfProtocols);
    _aidl_parcel.writeIntArray(protocols);
    _aidl_parcel.writeByte(numberOfCardInfo);
    _aidl_parcel.writeTypedArray(cardTLVInfo, _aidl_flag);
    int _aidl_end_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.setDataPosition(_aidl_start_pos);
    _aidl_parcel.writeInt(_aidl_end_pos - _aidl_start_pos);
    _aidl_parcel.setDataPosition(_aidl_end_pos);
  }
  public final void readFromParcel(android.os.Parcel _aidl_parcel) {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    int _aidl_parcelable_size = _aidl_parcel.readInt();
    try {
      if (_aidl_parcelable_size < 4)
        throw new android.os.BadParcelableException("Parcelable too small");
      ;
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >=
          _aidl_parcelable_size)
        return;
      id = _aidl_parcel.readByte();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >=
          _aidl_parcelable_size)
        return;
      status = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >=
          _aidl_parcelable_size)
        return;
      numberOfProtocols = _aidl_parcel.readByte();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >=
          _aidl_parcelable_size)
        return;
      protocols = _aidl_parcel.createIntArray();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >=
          _aidl_parcelable_size)
        return;
      numberOfCardInfo = _aidl_parcel.readByte();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >=
          _aidl_parcelable_size)
        return;
      cardTLVInfo =
          _aidl_parcel.createTypedArray(com.nxp.nfc.CardTLVInfo.CREATOR);
    } finally {
      if (_aidl_start_pos > (Integer.MAX_VALUE - _aidl_parcelable_size)) {
        throw new android.os.BadParcelableException(
            "Overflow in the size of parcelable");
      }
      _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
    }
  }
  @Override
  public int describeContents() {
    int _mask = 0;
    _mask |= describeContents(cardTLVInfo);
    return _mask;
  }
  private int describeContents(Object _v) {
    if (_v == null)
      return 0;
    if (_v instanceof Object[]) {
      int _mask = 0;
      for (Object o : (Object[])_v) {
        _mask |= describeContents(o);
      }
      return _mask;
    }
    if (_v instanceof android.os.Parcelable) {
      return ((android.os.Parcelable)_v).describeContents();
    }
    return 0;
  }
}
