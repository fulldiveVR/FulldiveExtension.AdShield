package model

import com.squareup.moshi.JsonClass

typealias AppId = String

class App(
    val id: AppId,
    val name: String,
    val isBypassed: Boolean,
    val isSystem: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as App

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

@JsonClass(generateAdapter = true)
class BypassedAppIds(
    val ids: List<AppId>
)