package com.xdata.partialmarking.dto;

import lombok.Data;
import java.util.List;

@Data
public class SchemaDTO {
    private List<TableDTO> tables;
}
