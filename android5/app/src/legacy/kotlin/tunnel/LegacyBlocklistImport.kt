package tunnel

import io.paperdb.Paper
import utils.Logger

/**
 * Imports any active, user-defined whitelists and blacklists from Blokada v4.
 * It will not import rules defined as an URL, or file path.
 */
@Deprecated("This is only a temporary legacy import")
object LegacyBlocklistImport {

    private val log = Logger("Legacy")

    fun importLegacyBlocklistUserDenied() = importLegacyBlocklist(false)
    fun importLegacyBlocklistUserAllowed() = importLegacyBlocklist(true)

    private fun importLegacyBlocklist(whitelist: Boolean): List<String>? {
        val key = "filters2"
        val filters = try {
            Paper.book().read<FilterStore>(key).cache
        } catch (ex: Exception) {
            null
        }

         val list = filters?.filter { it.active && it.source.id == "single" && it.whitelist == whitelist }
            ?.mapNotNull {
                try {
                    val ruleset = Paper.book().read("rules:set:${it.id}", Ruleset())
                    Paper.book().delete("rules:set:${it.id}")
                    if (ruleset.size > 0) {
                        log.v("Found a ruleset with ${ruleset.size} rules")
                        ruleset
                    } else null
                } catch (ex: Exception) {
                    null
                }
            }
        return if (list?.isNotEmpty() == true) list.flatten() else null
    }

}

typealias Ruleset = LinkedHashSet<String>
typealias FilterId = String

data class FilterStore(
    val cache: Set<Filter> = emptySet()
)

data class Filter(
    val id: FilterId,
    val source: FilterSourceDescriptor,
    val whitelist: Boolean = false,
    val active: Boolean = false
)

class FilterSourceDescriptor(
    val id: String,
    val source: String
)
