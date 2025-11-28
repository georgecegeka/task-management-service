package com.example.taskmanagement.mcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonRpcRequest {
    private String jsonrpc;
    private String id;
    private String method;
    private Map<String, Object> params;
}

