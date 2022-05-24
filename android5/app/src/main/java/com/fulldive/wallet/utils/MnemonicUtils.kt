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
import org.bitcoinj.core.Bech32
import org.bitcoinj.core.ECKey
import org.bitcoinj.crypto.*
import org.bouncycastle.util.encoders.Hex
import org.web3j.crypto.Keys
import java.io.ByteArrayOutputStream
import java.math.BigInteger

object MnemonicUtils {
    const val MNEMONIC_WORDS_COUNT = 24
    private val HEX_CHARSET = "0123456789abcdef".toCharArray()

    @Throws(Exception::class)
    fun createAddress(
        entropy: String,
        path: Int
    ): String {
        val childKey = createKeyWithPathFromEntropy(entropy, path)
        return generateAddressFromPrivateKey(Chain.chainAddressPrefix, childKey.privateKeyAsHex)
    }

    @Throws(Exception::class)
    fun createAddress(
        privateKey: String
    ): String {
        return generateAddressFromPrivateKey(Chain.chainAddressPrefix, privateKey)
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

    // Ethermint Style Key gen (OKex)
    @Throws(java.lang.Exception::class)
    fun createNewAddressSecp256k1(mainPrefix: String, publickKey: ByteArray): String {
        val uncompressedPubKey = ECKey.CURVE.curve.decodePoint(publickKey).getEncoded(false)
        val pub = ByteArray(64)
        System.arraycopy(uncompressedPubKey, 1, pub, 0, 64)
        val address = Keys.getAddress(pub)
        val bytes = convertBits(address, 8, 5, true)
        return Bech32.encode(mainPrefix, bytes)
    }

    @Throws(Exception::class)
    private fun getHDSeed(entropy: ByteArray): ByteArray {
        return MnemonicCode.toSeed(MnemonicCode.INSTANCE.toMnemonic(entropy), "")
    }

    private fun hexPublicKeyFromPrivateKey(privateKey: String): String {
        return ECKey.fromPrivate(BigInteger(privateKey, 16)).publicKeyAsHex
    }

    private fun generateAddressFromPublicKey(prefix: String, publicKey: String): String {
        return createNewAddressSecp256k1(prefix, Hex.decode(publicKey))
    }

    private fun generateAddressFromPrivateKey(prefix: String, privateKey: String): String {
        val publicKey = hexPublicKeyFromPrivateKey(privateKey)
        return generateAddressFromPublicKey(prefix, publicKey)
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

    private fun getPath(): List<ChildNumber> {
        return mutableListOf(
            ChildNumber(44, true),
            ChildNumber(60, true),
            ChildNumber.ZERO_HARDENED,
            ChildNumber.ZERO
        )
    }
}