# 1什么是metadata

metadata就是元数据信息，他提供了基于匹配的  listeners, filter chains, routes and endpoints的 额外的输入参数到过滤器，他是一种map的格式，通常是filter的名字（反向dns格式）。过滤器元数据的键值对在请求处理和连接发生时会别合并，后面的值会覆盖前面的值。元数据有一个名称空间的概念，然后是键值对。比如提供额外数据给httpConnectionManager的元数据名称空间 envoy.http_connection_manager.access_log 。另一个例子是每个service使用的cluster的元数据信息，他可能被多个过滤器使用。对于负载均衡来说，元数据提供了一种方法，来子集端点信息。关联元数据的endpoint，路由一个特定的元数据来选择端点。元数据有四种类型，分别是request类型，route类型，cluster类型，host类型。

# 2metadata有什么作用

tracing customtag值得来源，路由元数据匹配，负载均衡子集决策，ratelimit 动作配置，基于元数据的权限控制，本地响应映射元数据过滤，等。

# 3metadata数据来源

## 3.1 envoy.filters.http.ext_authz 

当使用grpc授权服务器时，当CheckResponse包含dynamic_metadata字段时，会产生动态元数据信息。

当使用http授权服务器时，当来自授权服务器的响应头匹配 [dynamic_metadata_from_headers](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/http/ext_authz/v3/ext_authz.proto#envoy-v3-api-field-extensions-filters-http-ext-authz-v3-authorizationresponse-dynamic-metadata-from-headers) 的配置值会产生元数据信息。动态元数据的key是匹配的头，动态元数据的值是匹配头的值。

## 3.2 *envoy.filters.network.ext_authz* 

当使用grpc授权服务器时，当CheckResponse包含dynamic_metadata字段时，会产生动态元数据信息。

## 3.3 envoy.filters.http.header_to_metadata 

配置一些规则，每条规则有header或cookie，当配置的值存在或不存在时就会触发规则，用来设置动态元数据。

比如：

```
http_filters:
  - name: envoy.filters.http.header_to_metadata
    typed_config:
      "@type": type.googleapis.com/envoy.extensions.filters.http.header_to_metadata.v3.Config
      request_rules:
        - header: x-version
          on_header_present:
            metadata_namespace: envoy.lb
            key: version
            type: STRING
          on_header_missing:
            metadata_namespace: envoy.lb
            key: default
            value: 'true'
            type: STRING
          remove: false
```

上面规则显示，当x-version头存在时设置envoy.lb名称空间的key为version元数据为x-version的值，当x-version不存在时，设置envoy.lb的名称空间的key为default的元数据的值为true。

## 3.4 envoy.filters.http.jwt_authn 

可以配置 **payload_in_****metadata** ，成功验证jwt payload会写到metadata中，名称空间是envoy.filters.http.jwt_authn，例子：

```
envoy.filters.http.jwt_authn:
  my_payload:
    iss: https://example.com
    sub: test@example.com
    aud: https://example.com
    exp: 1501281058
```

可以配置 **header_in_****metadata** ，成功验证的头会写到metadata中，名称空间是envoy.filters.http.jwt_authn，例子：

```
envoy.filters.http.jwt_authn:
  my_header:
    alg: JWT
    kid: EF71iSaosbC5C4tC6Syq1Gm647M
    alg: PS256
```

## 3.5 envoy.filters.network.mongo_proxy 

当 **emit_dynamic_metadata** 为true时会产生metadata，格式如下

| Name  | Type   | Description                                                  |
| ----- | ------ | ------------------------------------------------------------ |
| key   | string | The resource name in *db.collection* format.                 |
| value | array  | A list of strings representing the operations executed on the resource (insert/update/query/delete). |

## 3.6envoy.filters.network.mysql_proxy

当发送到服务端的sql被解析后，会产生动态元数据信息，格式如下：

| Name       | Type   | Description                                                  |
| ---------- | ------ | ------------------------------------------------------------ |
| <table.db> | string | The resource name in *table.db* format. The resource name defaults to the table being accessed if the database cannot be inferred. |
| []         | list   | A list of strings representing the operations executed on the resource. Operations can be one of insert/update/select/drop/delete/create/alter/show. |

## 3.7envoy.filters.network.postgres_proxy

语句被解析后，会产生动态元数据，格式如下：

| Name       | Type   | Description                                                  |
| ---------- | ------ | ------------------------------------------------------------ |
| <table.db> | string | The resource name in *table.db* format.                      |
| []         | list   | A list of strings representing the operations executed on the resource. Operations can be one of insert/update/select/drop/delete/create/alter/show. |

## 3.8 envoy.filters.http.rbac 

会产生如下元数据：

| shadow_effective_policy_id | string  | The effective shadow policy ID matching the action (if any). |
| -------------------------- | ------- | ------------------------------------------------------------ |
| shadow_engine_result       | string  | The engine result for the shadow rules (i.e. either `allowed` or `denied`). |
| access_log_hint            | boolean | Whether the request should be logged. This metadata is shared and set under the key namespace ‘envoy.common’ (See [Shared Dynamic Metadata](https://www.envoyproxy.io/docs/envoy/latest/configuration/advanced/well_known_dynamic_metadata#shared-dynamic-metadata)). |

## 3.9 envoy.filters.network.rbac 

会产生如下元数据：

| Name                       | Type    | Description                                                  |
| -------------------------- | ------- | ------------------------------------------------------------ |
| shadow_effective_policy_id | string  | The effective shadow policy ID matching the action (if any). |
| shadow_engine_result       | string  | The engine result for the shadow rules (i.e. either `allowed` or `denied`). |
| access_log_hint            | boolean | Whether the request should be logged. This metadata is shared and set under the key namespace ‘envoy.common’ (See [Shared Dynamic Metadata](https://www.envoyproxy.io/docs/envoy/latest/configuration/advanced/well_known_dynamic_metadata#shared-dynamic-metadata)). |

## 3.10envoy.filters.network.zookeeper_proxy

当每个消息被解析后，会产生如下元数据：

| Name               | Type   | Description                                                 |
| ------------------ | ------ | ----------------------------------------------------------- |
| <path>             | string | The path associated with the request, response or event     |
| <opname>           | string | The opname for the request, response or event               |
| <create_type>      | string | The string representation of the flags applied to the znode |
| <bytes>            | string | The size of the request message in bytes                    |
| <watch>            | string | True if a watch is being set, false otherwise               |
| <version>          | string | The version parameter, if any, given with the request       |
| <timeout>          | string | The timeout parameter in a connect response                 |
| <protocol_version> | string | The protocol version in a connect response                  |
| <readonly>         | string | The readonly flag in a connect response                     |
| <zxid>             | string | The zxid field in a response header                         |
| <error>            | string | The error field in a response header                        |
| <client_state>     | string | The state field in a watch event                            |
| <event_type>       | string | The event type in a a watch event                           |

## 3.11 envoy.filters.http.ratelimit 

当ratelimit服务返回 [RateLimitResponse](https://www.envoyproxy.io/docs/envoy/latest/api-v3/service/ratelimit/v3/rls.proto#envoy-v3-api-msg-service-ratelimit-v3-ratelimitresponse) 带有dynamic_metadata时，会产生元数据信息。

# 4metadata怎么使用

## 4.1type.tracing.v3.CustomTag

```
{
  "tag": "...",
  "literal": "{...}",
  "environment": "{...}",
  "request_header": "{...}",
  "metadata": "{...}"
}
```



```
cat << EOF > ef-allow-nothing-shadow.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: allow-nothing
spec:
  configPatches:
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
          name: envoy.filters.http.rbac
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.rbac.v3.RBAC
            shadow_rules:
              policies:
                ns[istio]-policy[allow-nothing]-rule[0]:
                  permissions:
                  - notRule:
                      any: true
                  principals:
                  - notId:
                      any: true
            shadowRulesStatPrefix: istio_dry_run_allow_
EOF

kubectl apply -f ef-allow-nothing-shadow.yaml -n istio-system
```

metadata：自定义tag，值从metadata中获取

案例：

ef-metadata-tracing.yaml

kubectl apply -f ef-metadata-tracing.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: metadata
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
                route_config:
                  name: test
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
                          inline_string: "prefix"
                      tracing:
                          customTags:
                          - metadata:
                              kind:
                                request: {}
                              metadataKey:
                                key: envoy.filters.http.rbac
                                path:
                                - key: istio_dry_run_allow_shadow_effective_policy_id
                            tag: istio.authorization.dry_run.allow_policy.name
                          - metadata:
                              kind:
                                request: {}
                              metadataKey:
                                key: envoy.filters.http.rbac
                                path:
                                - key: istio_dry_run_allow_shadow_engine_result
                            tag: istio.authorization.dry_run.allow_policy.result
                          - metadata:
                              kind:
                                request: {}
                              metadataKey:
                                key: envoy.filters.http.rbac
                                path:
                                - key: istio_dry_run_deny_shadow_effective_policy_id
                            tag: istio.authorization.dry_run.deny_policy.name
                          - metadata:
                              kind:
                                request: {}
                              metadataKey:
                                key: envoy.filters.http.rbac
                                path:
                                - key: istio_dry_run_deny_shadow_engine_result
                            tag: istio.authorization.dry_run.deny_policy.result
                          - literal:
                              value: latest
                            tag: istio.canonical_revision
                          - literal:
                              value: istio-ingressgateway
                            tag: istio.canonical_service
                          - literal:
                              value: mesh1
                            tag: istio.mesh_id
                          - literal:
                              value: istio-system
                            tag: istio.namespace
                          overallSampling:
                            numerator: 100
                            denominator: HUNDRED
                          randomSampling:
                            numerator: 100
                            denominator: HUNDRED
                          clientSampling:
                            numerator: 100
                            denominator: HUNDRED
```



## 4.2路由设置metadata

ef-metadata-route.yaml

kubectl apply -f ef-metadata-route.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: metadata
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
                route_config:
                  name: test
                  virtual_hosts:
                  - name: test
                    domains:
                    - "*"
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      metadata:
                        filter_metadata:
                          "envoy.lb": 
                            canary: true
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "prefix"
```





## 4.3local_reply_config

```
{
  "mappers": [],
  "body_format": "{...}"
}
```

mappers:

```
{
  "filter": "{...}",
  "status_code": "{...}",
  "body": "{...}",
  "body_format_override": "{...}",
  "headers_to_add": []
}
```

filter:

```
{
  "status_code_filter": "{...}",
  "duration_filter": "{...}",
  "not_health_check_filter": "{...}",
  "traceable_filter": "{...}",
  "runtime_filter": "{...}",
  "and_filter": "{...}",
  "or_filter": "{...}",
  "header_filter": "{...}",
  "response_flag_filter": "{...}",
  "grpc_status_filter": "{...}",
  "extension_filter": "{...}",
  "metadata_filter": "{...}"
}
```

**metadata_filter：**

```
{
  "matcher": "{...}",匹配条件
  "match_if_key_not_found": "{...}"key不存在时是否匹配
}
```

matcher:

```
{
  "filter": "...",过滤名称
  "path": [],metadata路径
  "value": "{...}",匹配值
  "invert": "..."反向匹配
}
```

value:

```
{
  "null_match": "{...}",null匹配
  "double_match": "{...}",double匹配
  "string_match": "{...}",string匹配
  "bool_match": "...",bool匹配
  "present_match": "...",存在性匹配
  "list_match": "{...}"列表匹配
}
```

string_match:

```
{
  "exact": "...",
  "prefix": "...",
  "suffix": "...",
  "safe_regex": "{...}",
  "contains": "...",
  "ignore_case": "..."
}
```

body_format:

```
{
  "text_format": "...",
  "json_format": "{...}",
  "text_format_source": "{...}",
  "omit_empty_values": "...",
  "content_type": "...",
  "formatters": []
}
```

ef-local_reply_config.yaml 

kubectl apply -f ef-local_reply_config.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: metadata
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
                route_config:
                  name: test
                  virtual_hosts:
                  - name: test
                    domains:
                    - "*"
                    routes:
                    - name: testroute
                      match: 
                        prefix: /product
                      route:
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                local_reply_config:
                  mappers:
                  - status_code: 200
                    filter:
                      metadata_filter:
                        matcher:
                          filter: envoy.lb
                          path: 
                          - key: canary
                          value:
                            string_match:
                              exact: "true"
                          invert: false
                        match_if_key_not_found: true
                    body: 
                      inline_string: "test"
                    body_format_override:
                      text_format: "%LOCAL_REPLY_BODY%:%RESPONSE_CODE%:path=%REQ(:path)%\n" 
                    headers_to_add:
                    - header:
                        key: test
                        value: test
                      append: true
                  body_format:
                    text_format: "%LOCAL_REPLY_BODY%:%RESPONSE_CODE%:path=%REQ(:path)%\n"      
```



## 4.4 envoy.filters.http.set_metadata

ef-metadata-set_metadata.yaml

kubectl apply -f ef-metadata-set_metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: metadata
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 10
  configPatches:
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
          name: envoy.filters.http.set_metadata
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.set_metadata.v3.Config
            metadata_namespace: envoy.lb
            value:
              canary: "true"
            
```

ef-rbac-metadata.yaml

kubectl apply -f ef-rbac-metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rbac
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
  configPatches:
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
          name: envoy.filters.http.rbac
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.rbac.v3.RBAC
            rules:
              action: DENY
              policies:
                "product-viewer":
                  permissions:
                  - metadata:
                      filter: envoy.lb
                      path:
                      - key: canary
                      value: 
                        string_match:
                          exact: "true"
                  principals:
                  - any: true
```



## 4.5route match

ef-metadata-set_metadata.yaml

kubectl apply -f ef-metadata-set_metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: metadata
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 10
  configPatches:
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
          name: envoy.filters.http.set_metadata
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.set_metadata.v3.Config
            metadata_namespace: envoy.lb
            value:
              canary: "true"
            
```



ef-metadata-route-match.yaml

kubectl apply -f ef-metadata-route-match.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: metadata2
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
                route_config:
                  name: test
                  virtual_hosts:
                  - name: test
                    domains:
                    - "*"
                    routes:
                    - name: testroute
                      metadata:
                        filter_metadata:
                          "envoy.lb": 
                            canary: "true"
                      match: 
                        path: /
                        case_sensitive: false
                        dynamic_metadata:
                        - filter: envoy.lb
                          path:
                          - key: canary
                          value:
                            string_match:
                              exact: "true"
                          invert: false
                      route:
                        cluster: outbound|9080||productpage.istio.svc.cluster.local
```



## 4.6基于元数据的权限控制

部署mysql

mysql-deploy.yaml

kubectl apply -f mysql-deploy.yaml -n istio

```
apiVersion: v1
kind: Secret
metadata:
  name: mysql-credentials
type: Opaque
data:
  rootpasswd: cGFzc3dvcmQ=
---
apiVersion: v1
kind: Service
metadata:
  name: mysqldb
  labels:
    app: mysqldb
    service: mysqldb
spec:
  ports:
  - port: 3306
    name: tcp
  selector:
    app: mysqldb
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mysqldb-v1
  labels:
    app: mysqldb
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mysqldb
      version: v1
  template:
    metadata:
      labels:
        app: mysqldb
        version: v1
    spec:
      containers:
      - name: mysqldb
        image: docker.io/istio/examples-bookinfo-mysqldb:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 3306
        env:
          - name: MYSQL_ROOT_PASSWORD
            valueFrom:
              secretKeyRef:
                name: mysql-credentials
                key: rootpasswd
        args: ["--default-authentication-plugin","mysql_native_password"]
        volumeMounts:
        - name: var-lib-mysql
          mountPath: /var/lib/mysql
      volumes:
      - name: var-lib-mysql
        emptyDir: {}
```

vs-gw-mysql.yaml

kubectl apply -f vs-gw-mysql.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: tcp-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 31400
      name: tcp
      protocol: TCP
    hosts:
    - "*"
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: mysqldb
spec:
  hosts:
  - "*"
  gateways:
  - tcp-gateway
  tcp:
  - match:
    - port: 31400
    route:
    - destination:
        host: mysqldb
        port:
          number: 3306
```

ef-metadata-rbac-mysql.yaml

kubectl apply -f ef-metadata-rbac-mysql.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: metadata
spec:
  workloadSelector:
    labels:
      app: mysqldb
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        portNumber: 3306
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.network.mysql_proxy
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.mysql_proxy.v3.MySQLProxy
          stat_prefix: mysql
  - applyTo: NETWORK_FILTER
    match:
      listener:
        portNumber: 3306
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.network.rbac
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.network.rbac.v3.RBAC
          stat_prefix: rbac
          rules:
             action: DENY
             policies:
               "product-viewer":
                 permissions:
                 - metadata:
                     filter: envoy.filters.network.mysql_proxy
                     path:
                     - key: t1.test
                     value:
                       list_match:
                         one_of:
                           string_match:
                             prefix: update
                 principals:
                 - any: true
          enforcement_type: CONTINUOUS 
```



## 4.7负载均衡决策

ef-lb_subset_config-fallback_policy-DEFAULT_SUBSET.yaml

kubectl apply -f ef-lb_subset_config-fallback_policy-DEFAULT_SUBSET.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: match
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        #name: 0.0.0.0_8080  
        portNumber: 8080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
        name: envoy.filters.network.http_connection_manager
        typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
          codec_type: AUTO
          stat_prefix: ingress_http
          route_config:
            name: http.9080
            virtual_hosts:
            - name: “*.9080”
              domains:
              - "*"
              routes:
              - match:
                  prefix: /
                  caseSensitive: true
                route:
                  cluster: cluster123
                  metadata_match:
                    filter_metadata:
                      envoy.lb:
                        env: mark
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value: 
        name: "cluster123"
        type: STRICT_DNS
        connect_timeout: 0.5s
        lb_policy: ROUND_ROBIN
        lb_subset_config:
          fallback_policy: DEFAULT_SUBSET
          default_subset:
            env: "taobao"
          subset_selectors:
          - keys:
            - env
        load_assignment:
          cluster_name: cluster123
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: productpage.istio.svc.cluster.local
                    port_value: 9080
              metadata:
                filter_metadata:
                  envoy.lb:
                    env: hema
            - endpoint:
                address:
                  socket_address:
                    address: productpage.istio-2.svc.cluster.local
                    port_value: 9080
              metadata:
                filter_metadata:
                  envoy.lb:
                    env: taobao
```

deploy-productpage.yaml

kubectl apply -f deploy-productpage.yaml -n istio-2

```
apiVersion: v1
kind: Service
metadata:
  name: productpage
  labels:
    app: productpage
    service: productpage
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: bookinfo-productpage
  labels:
    account: productpage
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: productpage-v1
  labels:
    app: productpage
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: productpage
      version: v1
  template:
    metadata:
      labels:
        app: productpage
        version: v1
    spec:
      serviceAccountName: bookinfo-productpage
      containers:
      - name: productpage
        image: docker.io/istio/examples-bookinfo-productpage-v1:1.16.4
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 9080
        volumeMounts:
        - name: tmp
          mountPath: /tmp
        securityContext:
          runAsUser: 1000
      volumes:
      - name: tmp
        emptyDir: {}
---
```



## 4.8header-To-Metadata Filter

```
{
  "request_rules": [],
  "response_rules": []
}
```

request_rules:

```
{
  "header": "...",
  "cookie": "...",
  "on_header_present": "{...}",
  "on_header_missing": "{...}",
  "remove": "..."
}
```

response_rules:

```
{
  "header": "...",
  "cookie": "...",
  "on_header_present": "{...}",
  "on_header_missing": "{...}",
  "remove": "..."
}
```

on_header_present,on_header_missing:

```
{
  "metadata_namespace": "...",
  "key": "...",
  "value": "...",
  "regex_value_rewrite": "{...}",
  "type": "...",
  "encode": "..."
}
```

ef-metadata-header-to-metadata.yaml

kubectl apply -f ef-metadata-header-to-metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: metadata
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
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
          name: envoy.filters.http.header_to_metadata
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.header_to_metadata.v3.Config
            request_rules:
            - header: x-version
              on_header_present:
                metadata_namespace: envoy.lb
                key: version
                type: STRING
              on_header_missing:
                metadata_namespace: envoy.lb
                key: default
                value: 'true'
                type: STRING
              remove: false
            response_rules:
            - header: x-version
              on_header_present:
                metadata_namespace: envoy.lb
                key: version
                type: STRING
              on_header_missing:
                metadata_namespace: envoy.lb
                key: default
                value: 'true'
                type: STRING
              remove: false            
            
```

ef-rbac-metadata-header-to-metadata.yaml

kubectl apply -f ef-rbac-metadata-header-to-metadata.yaml  -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: rbac
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  priority: 20
  configPatches:
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
          name: envoy.filters.http.rbac
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.rbac.v3.RBAC
            rules:
              action: DENY
              policies:
                "product-viewer":
                  permissions:
                  - metadata:
                      filter: envoy.lb
                      path:
                      - key: version
                      value: 
                        string_match:
                          exact: "v1"
                  principals:
                  - any: true
```

curl -H "x-version: v1" http://192.168.229.128:30563/productpage

## 4.9ratelimit actions

部署ratelimit

1创建cm

```
cat << EOF > ratelimit-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ratelimit-config
data:
  config.yaml: |
    domain: productpage-ratelimit
    descriptors:
      - key: test
        value: "v1"
        rate_limit:
          unit: minute
          requests_per_unit: 4
      - key: test
        rate_limit:
          unit: minute
          requests_per_unit: 10
EOF

kubectl apply -f ratelimit-config.yaml -n istio
```



2创建限速服务deployment

```
cat << EOF > ratelimit-deploy.yaml
apiVersion: v1
kind: Service
metadata:
  name: redis
  labels:
    app: redis
spec:
  ports:
  - name: redis
    port: 6379
  selector:
    app: redis
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - image: redis:alpine
        imagePullPolicy: Always
        name: redis
        ports:
        - name: redis
          containerPort: 6379
      restartPolicy: Always
      serviceAccountName: ""
---
apiVersion: v1
kind: Service
metadata:
  name: ratelimit
  labels:
    app: ratelimit
spec:
  ports:
  - name: http-port
    port: 8080
    targetPort: 8080
    protocol: TCP
  - name: grpc-port
    port: 8081
    targetPort: 8081
    protocol: TCP
  - name: http-debug
    port: 6070
    targetPort: 6070
    protocol: TCP
  selector:
    app: ratelimit
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ratelimit
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ratelimit
  strategy:
    type: Recreate
  template:
    metadata:
      labels:
        app: ratelimit
    spec:
      containers:
      - image: envoyproxy/ratelimit:6f5de117 # 2021/01/08
        imagePullPolicy: Always
        name: ratelimit
        command: ["/bin/ratelimit"]
        env:
        - name: LOG_LEVEL
          value: debug
        - name: REDIS_SOCKET_TYPE
          value: tcp
        - name: REDIS_URL
          value: redis:6379
        - name: USE_STATSD
          value: "false"
        - name: RUNTIME_ROOT
          value: /data
        - name: RUNTIME_SUBDIRECTORY
          value: ratelimit
        ports:
        - containerPort: 8080
        - containerPort: 8081
        - containerPort: 6070
        volumeMounts:
        - name: config-volume
          mountPath: /data/ratelimit/config/config.yaml
          subPath: config.yaml
      volumes:
      - name: config-volume
        configMap:
          name: ratelimit-config
EOF

kubectl apply -f ratelimit-deploy.yaml -n istio
```

3创建envoy-filter

```
cat << EOF > envoyfilter-filter.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      istio: ingressgateway
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        # Adds the Envoy Rate Limit Filter in HTTP filter chain.
        value:
          name: envoy.filters.http.ratelimit
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.ratelimit.v3.RateLimit
            # domain can be anything! Match it to the ratelimter service config
            domain: productpage-ratelimit
            failure_mode_deny: true
            rate_limit_service:
              grpc_service:
                envoy_grpc:
                  cluster_name: rate_limit_cluster
                timeout: 10s
              transport_api_version: V3
    - applyTo: CLUSTER
      match:
        cluster:
          service: ratelimit.istio.svc.cluster.local
      patch:
        operation: ADD
        # Adds the rate limit service cluster for rate limit service defined in step 1.
        value:
          name: rate_limit_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          http2_protocol_options: {}
          load_assignment:
            cluster_name: rate_limit_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: ratelimit.istio.svc.cluster.local
                      port_value: 8081
EOF

kubectl apply -f envoyfilter-filter.yaml -n istio-system
```

ef-metadata-header-to-metadata.yaml

kubectl apply -f ef-metadata-header-to-metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: metadata
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
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
          name: envoy.filters.http.header_to_metadata
          typedConfig:
            '@type': type.googleapis.com/envoy.extensions.filters.http.header_to_metadata.v3.Config
            request_rules:
            - header: x-version
              on_header_present:
                metadata_namespace: envoy.lb
                key: version
                type: STRING
              on_header_missing:
                metadata_namespace: envoy.lb
                key: default
                value: 'true'
                type: STRING
              remove: false
            response_rules:
            - header: x-version
              on_header_present:
                metadata_namespace: envoy.lb
                key: version
                type: STRING
              on_header_missing:
                metadata_namespace: envoy.lb
                key: default
                value: 'true'
                type: STRING
              remove: false            
            
```





ef-metadata-ratelimit.yaml

kubectl apply -f ef-metadata-ratelimit.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: metadata2
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
                route_config:
                  name: test
                  virtual_hosts:
                  - name: test
                    domains:
                    - "*"
                    routes:
                    - name: testroute
                      match: 
                        prefix: /product
                      route:
                        rate_limits:
                        - stage: 0
                          disable_key: test
                          actions:
                          - metadata: 
                              descriptor_key: test
                              default_value: test
                              metadata_key:
                                key: envoy.lb
                                path:
                                - key: version
                              source: DYNAMIC
                        weighted_clusters:
                          clusters:
                          - name: outbound|9080||productpage.istio.svc.cluster.local
                            weight: 100
                          total_weight: 100
                          runtime_key_prefix: test
                       
```

curl -H "x-version: v1" http://192.168.229.128:30563/productpage
