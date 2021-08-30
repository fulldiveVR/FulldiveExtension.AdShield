package repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import model.BlockaRepo
import service.HttpService
import service.JsonSerializationService
import utils.Logger

object BlockaRepoRepository {

    private val log = Logger("BlockaRepo")
    private val serializer = JsonSerializationService
    private val http = HttpService
//
//    suspend fun fetch(): BlockaRepo {
//        return coroutineScope {
//            async(Dispatchers.IO) {
//                log.v("Fetching Blocka repo to check for updates and configuration")
//                val content = http.makeRequest(REPO_URL)
//                serializer.deserialize(
//                    content,
//                    BlockaRepo::class
//                )
//            }
//        }.await()
//    }

}

//private const val REPO_URL = "https://blokada.org/api/v5/repo.json"