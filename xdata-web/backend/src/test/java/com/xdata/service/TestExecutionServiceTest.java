package com.xdata.service;

import com.xdata.db.QueryRunner;
import com.xdata.db.ScratchDatabaseFactory;
import com.xdata.model.SchemaInfo;
import com.xdata.repository.SchemaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** End-to-end over the real scratch DB, proving the refactored assembly still grades. */
class TestExecutionServiceTest {

    private final SchemaRepository schemaRepository = mock(SchemaRepository.class);
    private final TestExecutionService service =
            new TestExecutionService(schemaRepository, new ScratchDatabaseFactory(), new QueryRunner());

    private static final List<String> DATA = List.of(
            "INSERT INTO t VALUES (1)", "INSERT INTO t VALUES (2)", "INSERT INTO t VALUES (3)");

    private void schema(Integer id) {
        SchemaInfo info = new SchemaInfo();
        info.setContent("CREATE TABLE t (id INT)");
        when(schemaRepository.findById(id)).thenReturn(Optional.of(info));
    }

    @Test
    void equivalent_queries_compare_equal_on_test_data() {
        schema(1);
        boolean equal = service.compareQueries(
                "SELECT id FROM t WHERE id <= 2", "SELECT id FROM t WHERE id < 3", DATA, 1);
        assertThat(equal).isTrue();
    }

    @Test
    void differing_queries_compare_unequal() {
        schema(1);
        boolean equal = service.compareQueries(
                "SELECT id FROM t WHERE id = 1", "SELECT id FROM t WHERE id >= 1", DATA, 1);
        assertThat(equal).isFalse();
    }
}
