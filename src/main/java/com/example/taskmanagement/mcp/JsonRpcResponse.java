package com.example.taskmanagement.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRpcResponse {
    private String jsonrpc = "2.0";
    private String id;
    private Object result;
    private JsonRpcError error;

    public static JsonRpcResponse success(String id, Object result) {
        JsonRpcResponse r = new JsonRpcResponse();
        r.id = id;
        r.result = result;
        return r;
    }
    public static JsonRpcResponse error(String id, int code, String message, Object data) {
        JsonRpcResponse r = new JsonRpcResponse();
        r.id = id;
        r.error = new JsonRpcError(code, message, data);
        return r;
    }
}

