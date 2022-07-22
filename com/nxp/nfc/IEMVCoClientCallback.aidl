 /*
  * Copyright 2022 NXP
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
import com.nxp.nfc.EmvcoEvent;
import com.nxp.nfc.EmvcoStatus;

/**
 * @hide
 */

interface IEMVCoClientCallback {
    /**
     * The callback passed in from the NFC stack that the HAL
     * can use to pass incomming response data to the stack.
     *
     * @param data
     * Data packet to transmit NCI Responses, Notifications, and Data
     * Messages over sendData.
     * Detailed format is defined in NFC Controller Interface (NCI) Technical Specification.
     * https://nfc-forum.org/our-work/specification-releases/
     */
   oneway void sendData(in byte[] data, in int dataSize);

   /**
     * The callback passed in from the NFC stack that the HAL
     * can use to pass events back to the stack.
     */
   oneway void sendEvent(in EmvcoEvent event, in EmvcoStatus status);
}
