package com.xdata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchemaMetadataDTO {
    private Integer schemaId;
    private String schemaName;
    private List<TableMetadataDTO> tables;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableMetadataDTO {
        private String tableName;
        private List<ColumnMetadataDTO> columns;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnMetadataDTO {
        private String columnName;
        private String dataType;
    }
}
