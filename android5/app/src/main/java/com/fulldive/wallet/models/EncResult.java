/*
 * Copyright (c) 2022 FullDive
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.fulldive.wallet.models;

import android.util.Base64;

public class EncResult {
    byte[] encData;
    byte[] ivData;

    public EncResult(byte[] encData, byte[] ivData) {
        this.encData = encData;
        this.ivData = ivData;
    }

    public byte[] getEncData() {
        return encData;
    }

    public void setEncData(byte[] encData) {
        this.encData = encData;
    }

    public byte[] getIvData() {
        return ivData;
    }

    public void setIvData(byte[] ivData) {
        this.ivData = ivData;
    }


    public String getEncDataString() {
        String result = null;
        try {
            if (getEncData() != null) {
                result = Base64.encodeToString(getEncData(), 0);
            }
        } catch (Exception e) {
            result = null;
        } finally {
            return result;
        }
    }

    public String getIvDataString() {
        String result = null;
        try {
            if (getIvData() != null) {
                result = Base64.encodeToString(getIvData(), 0);
            }
        } catch (Exception e) {
            result = null;
        } finally {
            return result;
        }
    }
}
