/*
 * This file is part of Blokada.
 *
 * Blokada is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Blokada is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Blokada.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright © 2020 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package service

import model.BlokadaException
import java.io.InputStream
import android.util.Base64


object Base64Service {

    private val fileService = FileService

    fun decodeStream(stream: InputStream, key: String): InputStream {
        try {
            val encoded = fileService.load(stream)
            val decoded = Base64.decode(encoded, Base64.DEFAULT)
            return decoded.inputStream()
        } catch (ex: Exception) {
            throw BlokadaException("Could not decode base64 file", ex)
        }
    }

}