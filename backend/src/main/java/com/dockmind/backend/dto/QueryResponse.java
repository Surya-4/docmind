package com.dockmind.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class QueryResponse {
    private String answer;
    private List<String> sourceChunks;
}