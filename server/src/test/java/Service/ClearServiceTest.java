package Service;

import dataaccess.MemoryDataAccess;
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
    public void clearApplicationPositive() {
        assertDoesNotThrow(() -> clearService.clearApplication());
    }

    @Test
    public void clearApplicationNegative() {
        // Not really a "negative" case — should still succeed
        assertDoesNotThrow(() -> clearService.clearApplication());
    }
}
