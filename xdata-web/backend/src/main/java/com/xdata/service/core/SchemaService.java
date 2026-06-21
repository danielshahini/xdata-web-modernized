package com.xdata.service.core;

import com.xdata.dto.SchemaMetadataDTO;
import com.xdata.model.SchemaInfo;
import com.xdata.repository.SchemaRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.xdata.service.SqlValidationService;

@Service
@RequiredArgsConstructor
public class SchemaService {

    private final SchemaRepository schemaRepository;
    private final SqlValidationService sqlValidationService;

    public List<SchemaInfo> getAllSchemas() {
        return schemaRepository.findAll();
    }

    public List<SchemaInfo> getSchemasByCourse(String courseId) {
        return schemaRepository.findByCourse_InstructorCourseId(courseId);
    }

    public List<SchemaInfo> getSchemasByCourses(List<String> courseIds) {
        return schemaRepository.findByCourse_InstructorCourseIdIn(courseIds);
    }

    public Optional<SchemaInfo> getSchemaById(Integer id) {
        return schemaRepository.findById(id);
    }

    @Transactional
    public SchemaInfo saveSchema(SchemaInfo schemaInfo) {
        sqlValidationService.validateSchemaScript(schemaInfo.getContent());
        return schemaRepository.save(schemaInfo);
    }

    @Transactional
    public void deleteSchema(Integer id) {
        schemaRepository.deleteById(id);
    }

    public SchemaMetadataDTO getSchemaMetadata(Integer id) {
        SchemaInfo schemaInfo = schemaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schema not found"));

        List<SchemaMetadataDTO.TableMetadataDTO> tables = new ArrayList<>();

        try {
            Statements statements = CCJSqlParserUtil.parseStatements(schemaInfo.getContent());
            for (Statement statement : statements.getStatements()) {
                if (statement instanceof CreateTable) {
                    CreateTable ct = (CreateTable) statement;
                    String tableName = ct.getTable().getName();

                    // Collect primary-key columns and foreign-key targets, from both
                    // inline column specs (e.g. "id INTEGER PRIMARY KEY",
                    // "x INTEGER REFERENCES t(c)") and table-level constraints.
                    java.util.Set<String> pkColumns = new java.util.HashSet<>();
                    java.util.Map<String, String[]> fkByColumn = new java.util.HashMap<>();

                    if (ct.getIndexes() != null) {
                        for (net.sf.jsqlparser.statement.create.table.Index idx : ct.getIndexes()) {
                            String type = idx.getType() == null ? "" : idx.getType().toUpperCase();
                            if (type.contains("PRIMARY") && idx.getColumnsNames() != null) {
                                idx.getColumnsNames().forEach(c -> pkColumns.add(unquote(c)));
                            }
                            if (idx instanceof net.sf.jsqlparser.statement.create.table.ForeignKeyIndex) {
                                net.sf.jsqlparser.statement.create.table.ForeignKeyIndex fk =
                                        (net.sf.jsqlparser.statement.create.table.ForeignKeyIndex) idx;
                                String refTable = fk.getTable() != null ? fk.getTable().getName() : null;
                                List<String> local = idx.getColumnsNames();
                                List<String> ref = fk.getReferencedColumnNames();
                                if (local != null) {
                                    for (int i = 0; i < local.size(); i++) {
                                        String rc = (ref != null && i < ref.size()) ? ref.get(i)
                                                : (ref != null && !ref.isEmpty() ? ref.get(0) : null);
                                        fkByColumn.put(unquote(local.get(i)),
                                                new String[]{refTable, rc == null ? null : unquote(rc)});
                                    }
                                }
                            }
                        }
                    }

                    java.util.regex.Pattern fkInline = java.util.regex.Pattern.compile(
                            "REFERENCES\\s+\"?(\\w+)\"?\\s*\\(\\s*\"?(\\w+)\"?\\s*\\)",
                            java.util.regex.Pattern.CASE_INSENSITIVE);

                    List<SchemaMetadataDTO.ColumnMetadataDTO> columns = ct.getColumnDefinitions().stream()
                            .map(cd -> {
                                String colName = unquote(cd.getColumnName());
                                String specs = cd.getColumnSpecs() == null ? ""
                                        : String.join(" ", cd.getColumnSpecs());
                                String specsUpper = specs.toUpperCase();
                                if (specsUpper.contains("PRIMARY KEY")) pkColumns.add(colName);
                                String refTable = null, refColumn = null;
                                java.util.regex.Matcher m = fkInline.matcher(specs);
                                if (m.find()) {
                                    refTable = m.group(1);
                                    refColumn = m.group(2);
                                }
                                String[] fk = fkByColumn.get(colName);
                                if (fk != null) {
                                    if (refTable == null) refTable = fk[0];
                                    if (refColumn == null) refColumn = fk[1];
                                }
                                return SchemaMetadataDTO.ColumnMetadataDTO.builder()
                                        .columnName(cd.getColumnName())
                                        .dataType(cd.getColDataType().getDataType())
                                        .primaryKey(pkColumns.contains(colName))
                                        .referencesTable(refTable)
                                        .referencesColumn(refColumn)
                                        .build();
                            })
                            .collect(Collectors.toList());

                    // Second pass: table-level PK constraints may be parsed after the
                    // columns, so flag any columns now known to be primary keys.
                    columns.forEach(c -> c.setPrimaryKey(pkColumns.contains(unquote(c.getColumnName()))));

                    tables.add(SchemaMetadataDTO.TableMetadataDTO.builder()
                            .tableName(tableName)
                            .columns(columns)
                            .build());
                }
            }
        } catch (Exception e) {
            // Log error and return what we have
        }

        return SchemaMetadataDTO.builder()
                .schemaId(id)
                .schemaName(schemaInfo.getSchemaName())
                .tables(tables)
                .build();
    }

    /** Strip surrounding double quotes/backticks from an identifier. */
    private static String unquote(String s) {
        return s == null ? null : s.replaceAll("^[\"`]|[\"`]$", "");
    }
}
