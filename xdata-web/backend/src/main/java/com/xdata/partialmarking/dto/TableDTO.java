package com.xdata.partialmarking.dto;

import lombok.Data;
import java.util.List;

@Data
public class TableDTO {
    private String tableName;
    private List<ColumnDTO> columns;
}
