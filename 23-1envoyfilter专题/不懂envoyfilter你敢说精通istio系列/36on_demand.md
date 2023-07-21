# 1什么是on_demand

on_demand是envoy的一个http过滤器，名称为envoy.filters.http.on_demand，他的作用是按需加载路由。具体就是配置了scoped_routes，取出header里的对应键的值，和配置比较，决定用哪个路由。路由可以来自rds，也可以自己配置。自己配置目前istio还不支持。

# 2配置

没有相关字段

# 3实战

## 3.1route_configuration

route_configuration istio还不支持

X-Header: test=value

ef-on_demand.yaml

kubectl apply -f ef-on_demand.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
              name: envoy.filters.network.http_connection_manager
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                scoped_routes:
                  name: test
                  scope_key_builder:
                    fragments:
                    - header_value_extractor:
                        name: X-Header
                        element_separator: ";"
                        index: 0
                        element:
                          separator: "="
                          key: test
                  scoped_route_configurations_list:
                    scoped_route_configurations:
                    - on_demand: true
                      name: test1
                      route_configuration:
                          name: test1
                          virtual_hosts:
                          - name: test
                            domains:
                            - "*"
                            routes:
                            - name: testroute
                              match: 
                                prefix: /
                              direct_response:
                                status: 200
                                body: 
                                  inline_string: "test1"
                      key:
                        fragments:
                        - string_key: test1
                    - on_demand: true
                      name: test2
                      route_configuration:
                          name: test2
                          virtual_hosts:
                          - name: test
                            domains:
                            - "*"
                            routes:
                            - name: testroute
                              match: 
                                prefix: /
                              direct_response:
                                status: 200
                                body: 
                                  inline_string: "test2"
                      key:
                        fragments:
                        - string_key: test2
  - applyTo: HTTP_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_FIRST
      value: 
        name: envoy.filters.http.on_demand
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.on_demand.v3.OnDemand
```

curl http://192.168.229.128:30563/ -H "X-Header: test=test1"

## 3.2route_configuration_name

ef-on_demand-name.yaml

kubectl apply -f ef-on_demand-name.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: httpconnectionmanager
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
              name: envoy.filters.network.http_connection_manager
              typedConfig:
                '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                scoped_routes:
                  name: test
                  scope_key_builder:
                    fragments:
                    - header_value_extractor:
                        name: X-Header
                        element_separator: ";"
                        index: 0
                        element:
                          separator: "="
                          key: test
                  rds_config_source:
                    ads: {}
                    initialFetchTimeout: 10s
                    resourceApiVersion: V3
                  scoped_route_configurations_list:
                    scoped_route_configurations:
                    - on_demand: true
                      name: test1
                      route_configuration_name: "http.8080"
                      key:
                        fragments:
                        - string_key: test1
                    - on_demand: true
                      name: test2
                      route_configuration_name: "http.8081"
                      key:
                        fragments:
                        - string_key: test2
  - applyTo: HTTP_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_BEFORE
      value: 
        name: envoy.filters.http.on_demand
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.on_demand.v3.OnDemand
```

