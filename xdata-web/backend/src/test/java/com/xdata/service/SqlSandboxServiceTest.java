package com.xdata.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SqlSandboxServiceTest {

    @InjectMocks
    private SqlSandboxService sqlSandboxService;

    @Test
    public void testValidSelect() {
        assertDoesNotThrow(() -> sqlSandboxService.validateQuery("SELECT * FROM users"));
    }

    @Test
    public void testEmptyQuery() {
        Exception exception = assertThrows(RuntimeException.class, () -> 
            sqlSandboxService.validateQuery("")
        );
        assertTrue(exception.getMessage().contains("leer"));
    }

    @Test
    public void testDangerousQuery() {
        Exception exception = assertThrows(RuntimeException.class, () -> 
            sqlSandboxService.validateQuery("DROP TABLE users")
        );
        assertTrue(exception.getMessage().contains("Nur Lesezugriffe"));
    }

    @Test
    public void testInvalidSyntax() {
        assertThrows(RuntimeException.class, () -> 
            sqlSandboxService.validateQuery("SELECT FROM")
        );
    }
}
