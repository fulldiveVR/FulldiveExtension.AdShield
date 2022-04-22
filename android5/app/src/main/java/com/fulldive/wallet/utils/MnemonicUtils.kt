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

package com.fulldive.wallet.utils

import com.fulldive.wallet.extensions.safe
import com.fulldive.wallet.models.Chain
import org.bitcoinj.core.ECKey
import org.bitcoinj.crypto.*
import org.bouncycastle.crypto.digests.RIPEMD160Digest
import java.io.ByteArrayOutputStream
import java.math.BigInteger

object MnemonicUtils {
    const val MNEMONIC_WORDS_COUNT = 24
    private val GENERATORS = intArrayOf(0x3b6a57b2, 0x26508e6d, 0x1ea119fa, 0x3d4233dd, 0x2a1462b3)
    private val CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l".toByteArray()
    private val HEX_CHARSET = "0123456789abcdef".toCharArray()

    @Throws(Exception::class)
    fun createAddress(
        entropy: String,
        path: Int
    ): String {
        val childKey = createKeyWithPathFromEntropy(entropy, path)
        return getDpAddress(Chain.chainAddressPrefix, childKey.publicKeyAsHex)
    }

    @Throws(Exception::class)
    fun createAddress(
        privateKey: String
    ): String {
        return getDpAddress(Chain.chainAddressPrefix, hexPublicKeyFromPrivateKey(privateKey))
    }

    fun byteArrayToHexString(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = HEX_CHARSET[v ushr 4]
            hexChars[j * 2 + 1] = HEX_CHARSET[v and 0x0F]
        }
        return String(hexChars)
    }

    @Throws(IllegalArgumentException::class)
    fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        require(len % 2 != 1) { "Hex string must have even number of characters" }
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4)
                    + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    fun getStringHdSeedFromWords(words: List<String>): String? {
        return getByteHdSeedFromWords(words)?.let(MnemonicUtils::byteArrayToHexString)
    }

    fun isValidStringHdSeedFromWords(words: List<String>): Boolean {
        return getByteHdSeedFromWords(words) != null
    }

    private fun getByteHdSeedFromWords(words: List<String>): ByteArray? {
        return safe {
            getHDSeed(MnemonicCode.INSTANCE.toEntropy(words))
        }
    }

    @Throws(Exception::class)
    fun createKeyWithPathFromEntropy(
        entropy: String,
        path: Int,
    ): DeterministicKey {
        val result: DeterministicKey
        val masterKey = HDKeyDerivation.createMasterPrivateKey(
            getHDSeed(hexStringToByteArray(entropy))
        )
        result = DeterministicHierarchy(masterKey)
            .deriveChild(
                getPath(),
                true,
                true,
                ChildNumber(path)
            )
        return result
    }

    @Throws(Exception::class)
    private fun getHDSeed(entropy: ByteArray): ByteArray {
        return MnemonicCode.toSeed(MnemonicCode.INSTANCE.toMnemonic(entropy), "")
    }

    private fun hexPublicKeyFromPrivateKey(privateKey: String): String {
        return ECKey.fromPrivate(BigInteger(privateKey, 16)).publicKeyAsHex
    }

    private fun getDpAddress(prefix: String, publicHexKey: String): String {
        val result: String
        val digest = Sha256.getSha256Digest()
        val hash = digest.digest(hexStringToByteArray(publicHexKey))
        val digest2 = RIPEMD160Digest()
        digest2.update(hash, 0, hash.size)
        val hash3 = ByteArray(digest2.digestSize)
        digest2.doFinal(hash3, 0)
        val converted = convertBits(hash3, 8, 5, true)
        result = bech32Encode(prefix.toByteArray(), converted)
        return result
    }

    private fun bech32Encode(hrp: ByteArray, data: ByteArray): String {
        val chk = createChecksum(hrp, data)
        val combined = ByteArray(chk.size + data.size)
        System.arraycopy(data, 0, combined, 0, data.size)
        System.arraycopy(chk, 0, combined, data.size, chk.size)
        val xlat = ByteArray(combined.size)
        for (i in combined.indices) {
            xlat[i] = CHARSET[combined[i].toInt()]
        }
        val ret = ByteArray(hrp.size + xlat.size + 1)
        System.arraycopy(hrp, 0, ret, 0, hrp.size)
        System.arraycopy(byteArrayOf(0x31), 0, ret, hrp.size, 1)
        System.arraycopy(xlat, 0, ret, hrp.size + 1, xlat.size)
        return String(ret)
    }

    @Throws(java.lang.Exception::class)
    fun convertBits(data: ByteArray, frombits: Int, tobits: Int, pad: Boolean): ByteArray {
        var acc = 0
        var bits = 0
        val baos = ByteArrayOutputStream()
        val maxv = (1 shl tobits) - 1
        for (i in data.indices) {
            val value: Int = data[i].toInt() and 0xff
            if (value ushr frombits != 0) {
                throw IllegalStateException("invalid data range: data[$i]=$value (frombits=$frombits)")
            }
            acc = acc shl frombits or value
            bits += frombits
            while (bits >= tobits) {
                bits -= tobits
                baos.write(acc ushr bits and maxv)
            }
        }
        when {
            pad -> {
                if (bits > 0) {
                    baos.write(acc shl tobits - bits and maxv)
                }
            }
            bits >= frombits -> throw IllegalStateException("illegal zero padding")
            acc shl tobits - bits and maxv != 0 -> throw IllegalStateException("non-zero padding")
        }
        return baos.toByteArray()
    }

    private fun createChecksum(hrp: ByteArray, data: ByteArray): ByteArray {
        val zeroes = byteArrayOf(0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
        val expanded = hrpExpand(hrp)
        val values = ByteArray(zeroes.size + expanded.size + data.size)
        System.arraycopy(expanded, 0, values, 0, expanded.size)
        System.arraycopy(data, 0, values, expanded.size, data.size)
        System.arraycopy(zeroes, 0, values, expanded.size + data.size, zeroes.size)
        val polymod = polymod(values) xor 1
        val ret = ByteArray(6)
        for (i in ret.indices) {
            ret[i] = (polymod shr 5 * (5 - i) and 0x1f).toByte()
        }
        return ret
    }

    private fun polymod(values: ByteArray): Int {
        var chk = 1
        for (b in values) {
            val top = (chk shr 0x19).toByte()
            chk = b.toInt() xor (chk and 0x1ffffff shl 5)
            for (i in 0..4) {
                chk = chk xor if (top.toInt() shr i and 1 == 1) GENERATORS[i] else 0
            }
        }
        return chk
    }

    private fun hrpExpand(hrp: ByteArray): ByteArray {
        val buf1 = ByteArray(hrp.size)
        val buf2 = ByteArray(hrp.size)
        val mid = ByteArray(1)
        for (i in hrp.indices) {
            buf1[i] = (hrp[i].toInt() shr 5).toByte()
        }
        mid[0] = 0x00
        for (i in hrp.indices) {
            buf2[i] = (hrp[i].toInt() and 0x1f).toByte()
        }
        val ret = ByteArray(hrp.size * 2 + 1)
        System.arraycopy(buf1, 0, ret, 0, buf1.size)
        System.arraycopy(mid, 0, ret, buf1.size, mid.size)
        System.arraycopy(buf2, 0, ret, buf1.size + mid.size, buf2.size)
        return ret
    }

    private fun getPath(): List<ChildNumber> {
        return mutableListOf(
            ChildNumber(44, true),
            ChildNumber(118, true),
            ChildNumber.ZERO_HARDENED,
            ChildNumber.ZERO
        )
    }
}