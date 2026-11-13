package mobilecity.manager;

import mobilecity.manager.RunHistoryStore.RunRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunHistoryStoreTest {
    @Test
    void persistsAndReloadsRunHistory(@TempDir Path directory) throws IOException {
        RunHistoryStore store = new RunHistoryStore(directory);
        Instant startedAt = Instant.parse("2026-10-18T14:00:00Z");
        RunRecord run = new RunRecord("GUI-20261018-210000-000", startedAt, 12500,
                "Hoàn tất Maven", 2, 1, 0, 1, 0, 0,
                "HomeTest#testTC01_LoadHomePage,SearchTest#searchFindsAccentedProductUsingEnter",
                directory.resolve("report"), directory.resolve("test-execution.log"));

        store.save(run);

        List<RunRecord> loaded = store.loadAll();
        assertEquals(List.of(run), loaded);
    }
}
