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

package repository

import androidx.core.os.LocaleListCompat
import com.squareup.moshi.JsonClass
import service.ContextService
import service.FileService
import service.JsonSerializationService
import ui.utils.cause
import utils.Logger
import java.io.FileNotFoundException
import java.util.*

val SUPPORTED_LANGUAGES = listOf("en", "pl", "de", "es", "it", "hi", "ru", "bg", "tr", "ja", "id", "cs", "zh-Hant", "ar", "fi")
val LANGUAGE_NICE_NAMES = mapOf(
    "en" to "English",
    "pl" to "Polski",
    "de" to "Deutsch",
    "es" to "Español",
    "it" to "Italiano",
    "hi" to "हिन्दी",
    "ru" to "Pусский",
    "bg" to "Български",
    "tr" to "Türk",
    "ja" to "日本語",
    "id" to "bahasa Indonesia",
    "cs" to "Český",
    "zh-Hant" to "中文 (繁體)",
    "ar" to "عربى",
    "fi" to "Suomalainen"
)

fun LocaleListCompat.getFirstSupportedLocale(): Locale {
    /**
     * LocaleList list will contain only 1 (current) locale in case user has explicitly selected
     * one in app settings. That's because of how the Localization lib we use works. It sets
     * Locale.defaultLocale() and that affects this method. I don't have any more patience to deal
     * with locales on Android, so I'll just leave this bug, since it's not that important.
     *
     * The effect is that the app will default to your system's primary locale at start, but then
     * it will stick to it, even if you change system settings. Also, changing your locale in app
     * settings to "system default" will have no effect.
     */
    var index = 0
    while(index < size()) {
        val locale = this[index]
        if (locale.toLanguageTag() in SUPPORTED_LANGUAGES) return locale
        if ("%s_%s".format(locale.language, locale.country) in SUPPORTED_LANGUAGES) return locale
        if (locale.language in SUPPORTED_LANGUAGES) return locale
        index++
    }
    return Locale.ENGLISH
}

fun getTranslationRepository(locale: Locale) = factories[locale.toLanguageTag()] ?:
factories[locale.language] ?: run {
    Logger.w("Translation", "Falling back to root translation factory")
    root
}

interface TranslationRepository {
    fun getText(key: String): String?
}

private class FallbackAssetsTranslationRepository(locale: String): TranslationRepository {
    private val local = AssetsTranslationRepository(locale)
    override fun getText(key: String) = local.getText(key) ?: root.getText(key)
}

private class AssetsTranslationRepository(locale: String): TranslationRepository {

    private val log = Logger("Translation")
    private val context = ContextService
    private val file = FileService
    private val serializer = JsonSerializationService

    private val strings by lazy {
        log.v("Loading packs for: $locale")
        val ctx = context.requireAppContext()
        var allStrings = mapOf<String, String>()
        for (f in ASSETS_TRANSLATIONS_FILES) {
            try {
                val stream = ctx.assets.open(ASSETS_TRANSLATIONS_PATH.format(locale, f))
                val content = file.load(stream)
                val parsed = serializer.deserialize(content, TranslationPack::class)
                allStrings += parsed.strings
            } catch (ex: Exception) {
                when {
                    ex is FileNotFoundException -> log.w("No translation pack for: $locale/$f")
                    else -> log.w("Could not load pack".cause(ex))
                }
            }
        }
        allStrings += allStrings.mapKeys {
            it.key.replace(" ", "_")
        }
        allStrings
    }

    override fun getText(key: String) = strings[key]

}

private val root = AssetsTranslationRepository("root")
private val factories = mapOf(
    "root" to root
) + SUPPORTED_LANGUAGES.minus("en").map {
    it to FallbackAssetsTranslationRepository(it)
}

private const val ASSETS_TRANSLATIONS_PATH = "translations/%s/%s"
private val ASSETS_TRANSLATIONS_FILES = listOf("ui.json", "packs.json", "tags.json")

@JsonClass(generateAdapter = true)
class TranslationPack(
    val strings: Map<String, String>
)
