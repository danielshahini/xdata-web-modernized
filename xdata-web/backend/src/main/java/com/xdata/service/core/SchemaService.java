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

                    List<SchemaMetadataDTO.ColumnMetadataDTO> columns = ct.getColumnDefinitions().stream()
                            .map(cd -> SchemaMetadataDTO.ColumnMetadataDTO.builder()
                                    .columnName(cd.getColumnName())
                                    .dataType(cd.getColDataType().getDataType())
                                    .build())
                            .collect(Collectors.toList());

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
}
