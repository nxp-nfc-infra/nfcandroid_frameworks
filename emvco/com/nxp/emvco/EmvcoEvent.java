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

public enum EmvcoEvent implements Parcelable {
  EMVCO_OPEN_CHNL_CPLT_EVT(0),
  EMVCO_OPEN_CHNL_ERROR_EVT(1),
  EMVCO_CLOSE_CHNL_CPLT_EVT(2),
  EMVCO_POOLING_START_EVT(3),
  EMVCO_POLLING_STARTED_EVT(4),
  EMVCO_POLLING_STOP_EVT(5),
  EMVCO_UN_SUPPORTED_CARD_EVT(6),
  EMVCO_EVENT_UNKNOWN_EVT(7);

  private final int value;
  EmvcoEvent(int value) { this.value = value; }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name());
  }

  public static final Creator<EmvcoEvent> CREATOR = new Creator<EmvcoEvent>() {
    @Override
    public EmvcoEvent createFromParcel(final Parcel source) {
      return EmvcoEvent.valueOf(source.readString());
    }

    @Override
    public EmvcoEvent[] newArray(final int size) {
      return new EmvcoEvent[size];
    }
  };

  public static EmvcoEvent valueOf(int rx_event) {
    for (EmvcoEvent event : EmvcoEvent.values()) {
      if (event.ordinal() == rx_event) {
        return event;
      }
    }
    return EMVCO_EVENT_UNKNOWN_EVT;
  };
}
