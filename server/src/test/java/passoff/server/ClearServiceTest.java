package passoff.server;

import dataaccess.MemoryDataAccess;
import dataaccess.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.ClearService;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {
    private ClearService clearService;

    @BeforeEach
    public void setup() {
        clearService = new ClearService(new MemoryDataAccess());
    }

    @Test
    public void clear_positive() {
        assertDoesNotThrow(() -> clearService.clearApplication());
    }

    @Test
    public void clear_negative() {
        // Not really a "negative" case — should still succeed
        assertDoesNotThrow(() -> clearService.clearApplication());
    }
}
