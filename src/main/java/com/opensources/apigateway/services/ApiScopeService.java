package com.opensources.apigateway.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.opensources.apigateway.configurations.ApplicationConfig;
import com.opensources.apigateway.models.CacheControl;
import com.opensources.apigateway.models.DynamicRoute;
import com.opensources.apigateway.models.Route;
import com.opensources.apigateway.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ApiScopeService {

    private final FileUtil fileUtil;
    private List<DynamicRoute> dynamicRoutes;
    private final ApplicationConfig applicationConfig;

    @Autowired
    public ApiScopeService(FileUtil fileUtil, List<DynamicRoute> dynamicRoutes, ApplicationConfig applicationConfig) throws IOException {
        this.fileUtil = fileUtil;
        this.dynamicRoutes = dynamicRoutes;
        this.applicationConfig = applicationConfig;

        loadApiScopes();
    }

    private void loadApiScopes() throws IOException {
        JsonNode jsonNode = fileUtil.loadFileAsJson("api_scopes.json");

        dynamicRoutes = new ArrayList<>();

        //loop all apis for creating the scope
        for (JsonNode scope : jsonNode) {
            boolean authRequired = scope.get("auth_required").asBoolean();
            String path = scope.get("path").asText();
            String method = scope.get("method").asText();

            JsonNode cacheControlNode = scope.get("cache_control");
            String pattern = cacheControlNode.get("pattern").asText();
//            int ttl = cacheControlNode.get("ttl").asInt();

            CacheControl cacheControl = new CacheControl(pattern);

            DynamicRoute dynamicRoute;
            JsonNode outerRoutes = scope.get("routes");
            List<List<Route>> finalRoutes = null;
            if (outerRoutes != null && outerRoutes.isArray()) {
                //handle orchestration flow
                finalRoutes = new ArrayList<>();
                for (JsonNode outerRoute : outerRoutes) {
                    finalRoutes.add(getInnerRoutes(outerRoute));
                }
                dynamicRoute = new DynamicRoute(null, null, -1,
                        path,
                        method,
                        authRequired, cacheControl, finalRoutes);
            } else {
                String service = scope.get("service").asText();
                ApplicationConfig.ServiceProperties properties = applicationConfig.getServices().get(service);
                dynamicRoute = new DynamicRoute(properties.getScheme(), properties.getHost(), properties.getPort(), path, method,
                        authRequired,
                        cacheControl,
                        null);
            }

            dynamicRoutes.add(dynamicRoute);
        }
    }

    private List<Route> getInnerRoutes(JsonNode outerRoute) {
        List<Route> routes = new ArrayList<>();
        for (JsonNode innerRoute : outerRoute) {
            String innerService = innerRoute.get("service").asText();
            String innerPath = innerRoute.get("path").asText();
            String innerMethod = innerRoute.get("method").asText();
            String innerKey = innerRoute.get("key").asText();

            ApplicationConfig.ServiceProperties properties = applicationConfig.getServices().get(innerService);
            Route routeObj = new Route(properties.getScheme(),
                    properties.getHost(),
                    properties.getPort(),
                    innerPath,
                    innerMethod,
                    innerKey
            );
            routes.add(routeObj);
        }
        return routes;
    }

    public List<DynamicRoute> getDynamicRoutes() {
        return dynamicRoutes;
    }

    public DynamicRoute getMatchedAPI(String servletPath, String method) {
        AntPathMatcher matcher = new AntPathMatcher();
        for (DynamicRoute dynamicRoute : dynamicRoutes) {
            if (matcher.match(dynamicRoute.getPath(), servletPath) && method.equals(dynamicRoute.getMethod())) {
                return dynamicRoute;
            }
        }
        return null;
    }
}
