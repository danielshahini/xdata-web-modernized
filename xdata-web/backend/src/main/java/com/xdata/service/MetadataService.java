package com.xdata.service;

import com.xdata.db.ScratchDatabase;
import com.xdata.db.ScratchDatabaseFactory;
import com.xdata.model.SchemaInfo;
import com.xdata.repository.SchemaRepository;
import com.xdata.partialmarking.service.SqlSchemaParser;
import com.xdata.partialmarking.service.TableMapBuilder;
import com.xdata.partialmarking.dto.SchemaDTO;
import com.xdata.util.TableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Zentraler Service für Metadaten-Management.
 * Konsolidiert den Zugriff auf Legacy-TableMaps, neue TableMaps und JDBC-Metadaten.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetadataService {

    private final DataSource dataSource;
    private final SchemaRepository schemaRepository;
    private final ScratchDatabaseFactory scratchDatabaseFactory;

    /**
     * Erstellt eine Legacy-TableMap (com.xdata.util.TableMap) für ein gegebenes Schema oder die Standard-DB.
     */
    public TableMap getLegacyTableMap(Integer schemaId) throws SQLException {
        if (schemaId == null) {
            try (Connection conn = dataSource.getConnection()) {
                return TableMap.getInstances(conn, 1);
            }
        }
        
        Optional<SchemaInfo> schemaOpt = schemaRepository.findById(schemaId);
        if (schemaOpt.isEmpty()) {
            return getLegacyTableMap(null);
        }
        
        String ddl = schemaOpt.get().getContent();
        if (ddl == null || ddl.trim().isEmpty()) {
            return getLegacyTableMap(null);
        }

        log.info("Creating Legacy TableMap from DDL for schema ID: {}", schemaId);
        try (ScratchDatabase db = scratchDatabaseFactory.create(ddl)) {
            return TableMap.getInstances(db.connection(), 1);
        }
    }

    /**
     * Erstellt eine neue TableMap (com.xdata.partialmarking.core.TableMap) für ein gegebenes Schema.
     */
    public com.xdata.partialmarking.core.TableMap getNewTableMap(Integer schemaId) throws Exception {
        if (schemaId == null) {
            // Fallback auf JDBC Metadaten für die neue TableMap
            return buildNewTableMapFromJdbc();
        }

        Optional<SchemaInfo> schemaOpt = schemaRepository.findById(schemaId);
        if (schemaOpt.isEmpty()) {
            return buildNewTableMapFromJdbc();
        }

        String ddl = schemaOpt.get().getContent();
        if (ddl == null || ddl.trim().isEmpty()) {
            return buildNewTableMapFromJdbc();
        }

        SchemaDTO schemaDTO = SqlSchemaParser.parse(ddl);
        return TableMapBuilder.build(schemaDTO);
    }

    private com.xdata.partialmarking.core.TableMap buildNewTableMapFromJdbc() throws SQLException {
        log.info("Building new TableMap from JDBC metadata");
        com.xdata.partialmarking.core.TableMap tableMap = new com.xdata.partialmarking.core.TableMap();
        try (Connection conn = dataSource.getConnection()) {
            java.sql.DatabaseMetaData metaData = conn.getMetaData();
            try (java.sql.ResultSet tables = metaData.getTables(null, "public", "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    com.xdata.partialmarking.core.Table table = new com.xdata.partialmarking.core.Table(tableName);
                    try (java.sql.ResultSet columns = metaData.getColumns(null, "public", tableName, "%")) {
                        while (columns.next()) {
                            String columnName = columns.getString("COLUMN_NAME");
                            String typeName = columns.getString("TYPE_NAME");
                            table.addColumn(new com.xdata.partialmarking.core.Column(columnName, typeName));
                        }
                    }
                    tableMap.addTable(table);
                }
            }
        }
        return tableMap;
    }

}
