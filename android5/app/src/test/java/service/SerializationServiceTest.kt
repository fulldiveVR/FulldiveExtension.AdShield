package service

import model.HistoryEntry
import model.HistoryEntryType
import model.Stats
import org.junit.Test
import org.junit.Assert
import java.util.*

class SerializationServiceTest {
    @Test fun basic() {
        val stats = Stats(
            allowed = 1,
            denied = 2,
            entries = listOf(
                HistoryEntry(
                    name = "example.com",
                    type = HistoryEntryType.passed,
                    time = Date(),
                    requests = 1
                )
            )
        )

        val json = SerializationService.serialize(stats)
        val deserialized = SerializationService.deserialize(json, Stats::class)

        Assert.assertEquals(1, deserialized.allowed)
        Assert.assertEquals(2, deserialized.denied)
        Assert.assertEquals("example.com", deserialized.entries.first().name)
    }
}