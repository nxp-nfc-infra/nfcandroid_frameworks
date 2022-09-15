/******************************************************************************
 *
 *  Copyright 2022 NXP
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
 ******************************************************************************/

package com.nxp.emvco;
import android.os.Parcel;
import android.os.Parcelable;

public enum EmvcoStatus implements Parcelable {
  OK,
  FAILED,
  ERR_TRANSPORT,
  ERR_CMD_TIMEOUT,
  REFUSED,
  UNKNOWN;

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name());
  }

  public static final Creator<EmvcoStatus> CREATOR =
      new Creator<EmvcoStatus>() {
        @Override
        public EmvcoStatus createFromParcel(final Parcel source) {
          return EmvcoStatus.valueOf(source.readString());
        }

        @Override
        public EmvcoStatus[] newArray(final int size) {
          return new EmvcoStatus[size];
        }
      };

  public static EmvcoStatus valueOf(int rx_status) {
    for (EmvcoStatus status : EmvcoStatus.values()) {
      if (status.ordinal() == rx_status) {
        return status;
      }
    }
    return UNKNOWN;
  };
}
