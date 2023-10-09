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

import android.os.Parcel;
import android.os.Parcelable;

public class TdaResult implements Parcelable {
  public static final byte RESULT_SUCCESS = 0;
  public static final byte RESULT_FAILURE = 1;
  private byte status;
  private byte error;
  private byte exception;

  public TdaResult() {}

  public TdaResult(byte st, byte err, byte exc) {
    status = st;
    error = err;
    exception = exc;
  }

  protected TdaResult(Parcel in) {
    status = in.readByte();
    error = in.readByte();
    exception = in.readByte();
  }

  public void readFromParcel(Parcel in) {
    status = in.readByte();
    error = in.readByte();
    exception = in.readByte();
  }

  public void setStatus(byte st) { status = st; }

  public void setError(byte err) { error = err; }

  public void setException(byte exc) { exception = exc; }

  public byte getStatus() { return status; }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeByte(status);
    dest.writeByte(error);
    dest.writeByte(exception);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<TdaResult> CREATOR = new Creator<TdaResult>() {
    @Override
    public TdaResult createFromParcel(Parcel in) {
      byte status = in.readByte();
      byte error = in.readByte();
      byte exception = in.readByte();

      return new TdaResult(status, error, exception);
    }

    @Override
    public TdaResult[] newArray(int size) {
      return new TdaResult[size];
    }
  };
}
