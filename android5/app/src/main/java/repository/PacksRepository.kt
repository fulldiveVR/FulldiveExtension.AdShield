package repository

import model.*
import model.Defaults
import model.Defaults.PACKS_VERSION
import service.PersistenceService

class PacksRepository {

    private val persistence = PersistenceService

//    private var packs = persistence.load(Packs::class)
    private var packs = Defaults.packs()
        set(value) {
            field = value
            persistence.save(value)
        }

    fun getPacks() = packs.packs

    fun getPack(packId: PackId): Pack {
        return packs.packs.first { it.id == packId }
    }

}