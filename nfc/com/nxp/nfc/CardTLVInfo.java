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

/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.nxp.nfc;
import java.util.Arrays;
/**
 * @brief This provides the TLV information of the smart card connected over
 * TDA
 */
public class CardTLVInfo implements android.os.Parcelable {
  public byte type = 0;
  public byte length = 0;
  public byte[] value;

  public CardTLVInfo() {}

  public CardTLVInfo(CardTLVInfo other) {
    this.type = other.type;
    this.length = other.length;
    this.value = Arrays.copyOf(other.value, other.value.length);
  }

  public static final android.os.Parcelable.Creator<CardTLVInfo> CREATOR =
      new android.os.Parcelable.Creator<CardTLVInfo>() {
        @Override
        public CardTLVInfo createFromParcel(android.os.Parcel _aidl_source) {
          CardTLVInfo _aidl_out = new CardTLVInfo();
          _aidl_out.readFromParcel(_aidl_source);
          return _aidl_out;
        }
        @Override
        public CardTLVInfo[] newArray(int _aidl_size) {
          return new CardTLVInfo[_aidl_size];
        }
      };
  @Override
  public final void writeToParcel(android.os.Parcel _aidl_parcel,
                                  int _aidl_flag) {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeByte(type);
    _aidl_parcel.writeByte(length);
    _aidl_parcel.writeByteArray(value);
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
      type = _aidl_parcel.readByte();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >=
          _aidl_parcelable_size)
        return;
      length = _aidl_parcel.readByte();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >=
          _aidl_parcelable_size)
        return;
      value = _aidl_parcel.createByteArray();
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
    return _mask;
  }
}
