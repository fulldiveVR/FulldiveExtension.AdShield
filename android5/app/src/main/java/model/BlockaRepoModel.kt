package model

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class BlockaRepo(
    val syntaxVersion: Int,
    val common: BlockaRepoConfig,
    val buildConfigs: List<BlockaRepoConfig>
)

@JsonClass(generateAdapter = true)
data class BlockaRepoConfig(
    val name: String,
    val forBuild: String,
    val supportedLanguages: List<String>? = null,
    val update: BlockaRepoUpdate? = null,
    val payload: BlockaRepoPayload? = null,
    val lastRefresh: Long = 0L
) {

    fun supportedLanguages() = supportedLanguages ?: emptyList()

    fun combine(common: BlockaRepoConfig) = copy(
        supportedLanguages = supportedLanguages ?: common.supportedLanguages,
        update = update ?: common.update
    )

}

@JsonClass(generateAdapter = true)
data class BlockaRepoUpdate(
    val mirrors: List<Uri>,
    val infoUrl: Uri,
    val newest: String
)

@JsonClass(generateAdapter = true)
data class BlockaAfterUpdate(
    val dialogShownForVersion: Int? = null
)

@JsonClass(generateAdapter = true)
data class BlockaRepoPayload(
    val cmd: String,
    val version: Int? = null
)