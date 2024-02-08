/******************************************************************************
 *
 *  Copyright 2022-2023 NXP
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
 *AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 *FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/

package com.nxp.emvco;

public enum NxpEmvcoEvent {
  EMVCO_OPEN_CHNL_CPLT_EVT(0),
  EMVCO_OPEN_CHNL_ERROR_EVT(1),
  EMVCO_CLOSE_CHNL_CPLT_EVT(2),
  EMVCO_POOLING_START_EVT(3),
  EMVCO_POLLING_STARTED_EVT(4),
  EMVCO_POLLING_STOP_EVT(5),
  EMVCO_UN_SUPPORTED_CARD_EVT(6),
  EMVCO_EVENT_UNKNOWN_EVT(7);

  private final int value;
  NxpEmvcoEvent(int value) { this.value = value; }

  public int getValue() { return value; }

  public static NxpEmvcoEvent valueOf(int rx_event) {
    for (NxpEmvcoEvent event : NxpEmvcoEvent.values()) {
      if (event.ordinal() == rx_event) {
        return event;
      }
    }
    return EMVCO_EVENT_UNKNOWN_EVT;
  };
}
