/******************************************************************************
 *
 *  Copyright 2023 NXP
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

public enum ConfigType {
  POLL_PROFILE_SEL(0),
  UNKNOWN_KEY(1);

  private final int value;
  ConfigType(int value) { this.value = value; }

  public int getValue() { return value; }

  public static ConfigType valueOf(int rx_event) {
    for (ConfigType event : ConfigType.values()) {
      if (event.ordinal() == rx_event) {
        return event;
      }
    }
    return UNKNOWN_KEY;
  };
}
