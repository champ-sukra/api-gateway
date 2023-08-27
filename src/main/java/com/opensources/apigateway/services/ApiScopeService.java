package com.opensources.apigateway.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.opensources.apigateway.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApiScopeService {

    private final FileUtil fileUtil;

    @Autowired
    public ApiScopeService(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public JsonNode loadApiScopes() throws IOException {
        return fileUtil.loadFileAsJson("api_scope.json");
    }

    public List<JsonNode> loadDynamicApiScopes() throws IOException {
        List<JsonNode> jsonNodes = new ArrayList<>();
        List<String> files = fileUtil.loadFiles("api_scopes");
        for (String file : files) {
            jsonNodes.add(fileUtil.loadFileAsJson("api_scopes/" + file));
        }

        return jsonNodes;
    }
}
