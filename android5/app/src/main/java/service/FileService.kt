/*
 * This file is part of Blokada.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * Copyright © 2022 Blocka AB. All rights reserved.
 *
 * @author Karol Gusak (karol@blocka.net)
 */

package service

import android.os.Build
import androidx.annotation.RequiresApi
import appextension.getPrivateSharedPreferences
import appextension.getProperty
import appextension.setProperty
import com.fulldive.wallet.extensions.letOr
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import model.Uri
import utils.Logger
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

// TODO: make an implementation of this that doesn't use so much memory
object FileService {

    private const val KEY_SAVED_URLS = "KEY_SAVED_URLS"
    private val gson = Gson()
    private val log = Logger("File")
    val sharedPreferences = ContextService.requireContext().getPrivateSharedPreferences()

    val loadedUrls: MutableMap<String, Long> = getLoadedUrlsMap().toMutableMap()

    fun exists(uri: Uri): Boolean {
        return File(uri).exists()
    }

    fun needToDownload(uri: Uri): Boolean {
        return loadedUrls[uri].letOr(
            { createDate ->
                createDate <= Calendar.getInstance().timeInMillis - 24 * 60 * 60 * 3 // keep alive for 3 days
            }, {
                true
            }
        )
    }

    fun getLastModifiedTimeInMillis(file: File): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getLastModifiedTimeFromBasicFileAttrs(file)
            } else {
                file.lastModified()
            }
        } catch (x: Exception) {
            x.printStackTrace()
            0L
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getLastModifiedTimeFromBasicFileAttrs(file: File): Long {
        val basicFileAttributes = Files.readAttributes(
            file.toPath(),
            BasicFileAttributes::class.java
        )
        return basicFileAttributes.creationTime().toMillis()
    }

    fun remove(uri: Uri) {
        try {
            File(uri).delete()
        } catch (ex: Exception) {
        }
    }

    fun commonDir(): Uri {
        return ContextService.requireAppContext().filesDir.canonicalPath
    }

    suspend fun merge(uris: List<Uri>, destination: Uri) {
        val merged = mutableListOf<String>()
        for (uri in uris) {
            val content = load(uri)
            merged.addAll(content)
        }
        save(destination, merged)
    }

    fun load(source: Uri): List<String> {
        return File(source).useLines { it.toList() }
    }

    fun load(source: InputStream): String {
        return source.use { input ->
            val reader = BufferedReader(input.reader())
            reader.readText()
        }
    }

    fun save(destination: Uri, content: List<String>) {
        log.v("Saving ${content.size} lines to file: $destination")
        File(destination).bufferedWriter().use { out ->
            for (line in content) {
                out.write(line + "\n")
            }
        }
    }

    fun save(destination: Uri, content: String, url: String) {
        log.v("Saving file: $destination")
        File(destination).writeText(content)
        if (url.isNotEmpty()) {
            loadedUrls[url] = Calendar.getInstance().timeInMillis
            saveLoadedUrls()
        }
    }

    fun save(destination: Uri, source: InputStream) {
        log.v("Saving file from input stream to: $destination")
        source.use { input ->
            File(destination).outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    fun append(destination: Uri, line: String, maxSizeKb: Int = 0) {
        val file = File(destination)
        if (maxSizeKb > 0) {
            val sizeKb = file.length()
            if (sizeKb / 1024 >= maxSizeKb || sizeKb == 0L) {
                file.writeText(line)
                return
            }
        }
        file.appendText("\n$line")
    }

    private fun getLoadedUrlsMap(): Map<String, Long> {
        val jsonString = sharedPreferences.getProperty(KEY_SAVED_URLS, "[]")
        val type = object : TypeToken<Map<String, Long>>() {}.type
        return gson.fromJson(jsonString, type)
    }

    private fun saveLoadedUrls() {
        sharedPreferences.setProperty(KEY_SAVED_URLS, gson.toJson(loadedUrls))
    }
}

fun Uri.file(filename: String): Uri {
    return "$this/$filename"
}