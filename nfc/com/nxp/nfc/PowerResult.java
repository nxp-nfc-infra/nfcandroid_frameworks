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

public class PowerResult implements Parcelable {
  public enum Result implements Parcelable {
    FAILURE,
    SUCCESS;

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(ordinal()); // Write the ordinal value of the enum
    }

    public static final Creator<Result> CREATOR = new Creator<Result>() {
      @Override
      public Result createFromParcel(Parcel in) {
        return Result.values()[in.readInt()]; // Read the ordinal and convert it
                                              // back to an enum
      }

      @Override
      public Result[] newArray(int size) {
        return new Result[size];
      }
    };
  }

  private Result mResult;

  public PowerResult() {}

  public PowerResult(Result result) { mResult = result; }

  public Result getResult() { return mResult; }

  public void setResult(Result result) { mResult = result; }

  protected PowerResult(Parcel in) { mResult = Result.values()[in.readInt()]; }

  public static final Creator<PowerResult> CREATOR =
      new Creator<PowerResult>() {
        @Override
        public PowerResult createFromParcel(Parcel in) {
          return new PowerResult(in);
        }

        @Override
        public PowerResult[] newArray(int size) {
          return new PowerResult[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(mResult.ordinal());
  }
}