# 1什么是network filter

network filter是envoy的网络过滤器，作用在L3/L4。其中最重要的算是tcp_proxy和http_connection_manager这两个，分别表示tcp连接和http连接。本小节介绍的网络过滤器配置都比较简单，所以放在一章来介绍。

# 2本节介绍的filter列表

 client_ssl_auth 

 connection_limit 

 envoy_mobile_http_connection_manager 

 direct_response 

 echo 

 sni_cluster 

 sni_dynamic_forward_proxy 

 postgres_proxy 

 mysql_proxy 

 mongo_proxy 

 zookeeper_proxy 

# 3配置

## 3.1client_ssl_auth 

```
{
  "auth_api_cluster": "...",认证服务cluster
  "stat_prefix": "...",stat前缀
  "refresh_delay": "{...}",刷新抖动
  "ip_white_list": []ip白名单
}
```

## 3.2connection_limit 

```
{
  "stat_prefix": "...",stat前缀
  "max_connections": "{...}",最大并发连接数
  "delay": "{...}",拒绝连接延迟
  "runtime_enabled": "{...}"是否启用，默认启用
}
```

## 3.3envoy_mobile_http_connection_manager 

```
{
  "config": "{...}"http_connection_manager 配置
}
```

## 3.4direct_response 

```
{
  "response": "{...}"响应内容
}
```

response：

```
{
  "filename": "...",来自文件内容
  "inline_bytes": "...",bytes内容
  "inline_string": "...",string内容
  "environment_variable": "..."环境变量内容
}
```

## 3.5echo 

无

## 3.6sni_cluster 

无

## 3.7sni_dynamic_forward_proxy 

```
{
  "dns_cache_config": "{...}",dns缓存配置
  "port_value": "..."连接upstream的端口
}
```

dns_cache_config：

```
{
  "name": "...",缓存的名字
  "dns_lookup_family": "...",ip类型
  "dns_refresh_rate": "{...}",dns刷新频率，默认60s
  "host_ttl": "{...}",host没有使用的生存时间，默认5m
  "max_hosts": "{...}",缓存存放的最大host数量，默认1024
  "dns_failure_refresh_rate": "{...}",dsn请求失败的刷新频率，默认dns_refresh_rate一样
  "dns_cache_circuit_breaker": "{...}",缓存断路器
  "use_tcp_for_dns_lookups": "...",使用tcp查询
  "dns_resolution_config": "{...}",dns查询服务器配置，废弃
  "typed_dns_resolver_config": "{...}",dns查询服务器配置
  "preresolve_hostnames": [],预先定义的host
  "dns_query_timeout": "{...}"查询超时时间
}
```

dns_lookup_family：

- AUTO

  *(DEFAULT)* ⁣

- V4_ONLY

  ⁣

- V6_ONLY

  ⁣

- V4_PREFERRED

dns_cache_circuit_breaker：

```
{
  "max_pending_requests": "{...}"最大等待请求数量
}
```

 **typed_dns_resolver_config** ：

```
envoy.network.dns_resolver.apple

envoy.network.dns_resolver.cares
```

envoy.network.dns_resolver.cares：

```
{
  "resolvers": [],服务器地址
  "dns_resolver_options": "{...}"选项
}
```

dns_resolver_options：

```
{
  "use_tcp_for_dns_lookups": "...",使用tcp查询
  "no_default_search_domain": "..."是否有默认查询domain
}
```

preresolve_hostnames：

```
{
  "protocol": "...",协议
  "address": "...",地址
  "port_value": "...",端口
  "named_port": "...",命名端口
  "resolver_name": "...",解析器名字
  "ipv4_compat": "..."兼容ipv4
}
```

## 3.8postgres_proxy 

```
{
  "stat_prefix": "...",stat前缀
  "enable_sql_parsing": "{...}",是否启用sql解析，默认true
  "terminate_ssl": "..."是否终止ssl
}
```

## 3.9mysql_proxy 

```
{
  "stat_prefix": "..."stat前缀
}
```

## 3.10mongo_proxy 

```
{
  "stat_prefix": "...",stat前缀
  "access_log": "...",访问日志路径
  "delay": "{...}",注入延迟
  "emit_dynamic_metadata": "...",是否产生动态元数据，默认false
  "commands": []产生metrics的命令
}
```

delay：

```
{
  "fixed_delay": "{...}",固定延迟
  "header_delay": "{...}",由头控制的延迟
  "percentage": "{...}"延迟比列
}
```

## 3.11zookeeper_proxy 

```
{
  "stat_prefix": "...",stat前缀
  "max_packet_bytes": "{...}"消息最大大小，默认1Mb,istio未实现
}
```

# 4实战

## 4.1client_ssl_auth 

先部署tcp-echo

simple/tcp-echo-services.yaml

kubectl apply -f tcp-echo-services.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: tcp-echo
  labels:
    app: tcp-echo
    service: tcp-echo
spec:
  ports:
  - name: tcp
    port: 9000
  - name: tcp-other
    port: 9001
  # Port 9002 is omitted intentionally for testing the pass through filter chain.
  selector:
    app: tcp-echo
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tcp-echo-v1
  labels:
    app: tcp-echo
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: tcp-echo
      version: v1
  template:
    metadata:
      labels:
        app: tcp-echo
        version: v1
    spec:
      containers:
      - name: tcp-echo
        image: docker.io/istio/tcp-echo-server:1.2
        imagePullPolicy: IfNotPresent
        args: [ "9000,9001,9002", "one" ]
        ports:
        - containerPort: 9000
        - containerPort: 9001
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tcp-echo-v2
  labels:
    app: tcp-echo
    version: v2
spec:
  replicas: 1
  selector:
    matchLabels:
      app: tcp-echo
      version: v2
  template:
    metadata:
      labels:
        app: tcp-echo
        version: v2
    spec:
      containers:
      - name: tcp-echo
        image: docker.io/istio/tcp-echo-server:1.2
        imagePullPolicy: IfNotPresent
        args: [ "9000,9001,9002", "two" ]
        ports:
        - containerPort: 9000
        - containerPort: 9001
```

tcp-echo-all-v1.yaml

kubectl apply -f tcp-echo-all-v1.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: Gateway
metadata:
  name: tcp-echo-gateway
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
kind: DestinationRule
metadata:
  name: tcp-echo-destination
spec:
  host: tcp-echo
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
---
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: tcp-echo
spec:
  hosts:
  - "*"
  gateways:
  - tcp-echo-gateway
  tcp:
  - match:
    - port: 31400
    route:
    - destination:
        host: tcp-echo
        port:
          number: 9000
```

 

This functionality can be used to build edge proxy VPN support for web infrastructure. 

ef-client_ssl_auth.yaml

kubectl apply -f ef-client_ssl_auth.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400 
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.network.client_ssl_auth
        typed_config:
          '@type': type.googleapis.com/envoy.extensions.filters.network.client_ssl_auth.v3.ClientSSLAuth
          auth_api_cluster: auth_api_cluster
          stat_prefix: test
          refresh_delay: 60000ms
          ip_white_list:
          - address_prefix: 0.0.0.0
            prefix_len: 0
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
        name: auth_api_cluster
        connect_timeout: 0.25s
        type: STRICT_DNS
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: auth_api_cluster
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: auth
                    port_value: 80
```

由于我们没有auth_api_cluster实现，所以白名单以外的情况暂时不能验证。

## 4.2connection_limit 

ef-connection_limit.yaml

kubectl apply -f ef-connection_limit.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400 
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.network.connection_limit
        typed_config:
          '@type': type.googleapis.com/envoy.extensions.filters.network.connection_limit.v3.ConnectionLimit
          stat_prefix: connectionLimit
          max_connections: 1
          delay: 1s
          runtime_enabled:
            default_value: true
            runtime_key: connectionlimit.enabled
```

开启两个telnet，第二个1秒后就关闭了·

## 4.3envoy_mobile_http_connection_manager 

配置方式和http_connection_manager 差不多，请参考http_connection_manager 

## 4.4direct_response

ef-direct_response.yaml

kubectl apply -f ef-direct_response.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400 
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.direct_response
        typed_config:
          '@type': type.googleapis.com/envoy.extensions.filters.network.direct_response.v3.Config
          response:
            inline_string: "this is a test"
            
```

 telnet 192.168.229.134 32691

## 4.5echo

ef-echo.yaml

kubectl apply -f ef-echo.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400 
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: REPLACE
      value:
        name: envoy.filters.network.echo
        typed_config:
          '@type': type.googleapis.com/envoy.extensions.filters.network.echo.v3.Echo
```

## 4.6sni_cluster 

 Set the upstream cluster name from the SNI field in the TLS connection 

The `sni_cluster` is a network filter that uses the **SNI** value in a TLS connection as the upstream cluster name. The filter will not modify the upstream cluster for non-TLS connections. This filter should be configured with the name `envoy.filters.network.sni_cluster`.

This filter has no configuration. It must be installed before the [tcp_proxy](https://www.envoyproxy.io/docs/envoy/latest/configuration/listeners/network_filters/tcp_proxy_filter#config-network-filters-tcp-proxy) filter.



ef-sni_cluster.yaml

kubectl apply -f ef-sni_cluster.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400 
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.network.sni_cluster
        typed_config:
          '@type': type.googleapis.com/envoy.extensions.filters.network.sni_cluster.v3.SniCluster
  - applyTo: CLUSTER
    match:
      cluster:
        name: outbound|9000||tcp-echo.istio.svc.cluster.local
    patch:
      operation: MERGE
      value:
                transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    sni: outbound|9000|v2|tcp-echo.istio.svc.cluster.local
      

```

## 4.7sni_dynamic_forward_proxy 

ef-sni_dynamic_forward_proxy.yaml

kubectl apply -f ef-sni_dynamic_forward_proxy.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 8443
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: MERGE
      value:
              name: envoy.filters.network.http_connection_manager
              typedConfig:
                  '@type': type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager
                  stat_prefix: ingress_http
                  route_config:
                    name: local_route
                    virtual_hosts:
                    - name: local_service
                      domains: ["*"]
                      routes:
                      - match:
                          prefix: "/"
                        route:
                          cluster: dynamic_forward_proxy_cluster
                          host_rewrite_literal: www.baidu.com
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 8443
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
    patch:
      operation: INSERT_BEFORE
      value:
            name: envoy.filters.network.sni_dynamic_forward_proxy
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.network.sni_dynamic_forward_proxy.v3.FilterConfig
              dns_cache_config:
                name: dynamic_forward_proxy_cache_config
                dns_lookup_family: V4_ONLY
                typed_dns_resolver_config:
                  name: envoy.network.dns_resolver.cares
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.network.dns_resolver.cares.v3.CaresDnsResolverConfig
                    resolvers:
                    - socket_address:
                        address: "8.8.8.8"
                        port_value: 53
                    dns_resolver_options:
                      use_tcp_for_dns_lookups: true
                      no_default_search_domain: true
                            
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: dynamic_forward_proxy_cluster
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: dynamic_forward_proxy_cluster
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: www.baidu.com
                      port_value: 80
```



## 4.8postgres_proxy 

部署postgres

postgres-deploy.yaml

kubectl apply -f postgres-deploy.yaml -n istio

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: postgres-config
  labels:
    app: postgres
data:
  POSTGRES_DB: master
  POSTGRES_USER: postgres
  POSTGRES_PASSWORD: postgres
---
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: postgres
spec:
  serviceName: "postgres"
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
        - name: postgres
          image: postgres:9.5
          envFrom:
            - configMapRef:
                name: postgres-config
          ports:
            - containerPort: 5432
              name: postgredb
          volumeMounts:
#            - name: postgres-data
#              mountPath: /var/lib/postgresql/data
#              subPath: postgres
#  volumeClaimTemplates:
#    - metadata:
#        name: postgres-data
#      spec:
#        accessModes: ["ReadWriteOnce"]
#        resources:
#          requests:
#            storage: 2Gi
        
---
apiVersion: v1
kind: Service
metadata:
  name: postgres
  labels:
    app: postgres
spec:
  ports:
    - port: 5432
      protocol: TCP
      targetPort: 5432
      name: postgres
  selector:
    app: postgres
```

ef-postgres_proxy.yaml

kubectl apply -f ef-postgres_proxy.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400 
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.network.postgres_proxy
        typed_config:
          '@type': type.googleapis.com/envoy.extensions.filters.network.postgres_proxy.v3.PostgresProxy
          stat_prefix: postgre
          enable_sql_parsing: true
          terminate_ssl: false
```

vs-gw-postgre.yaml

kubectl apply -f vs-gw-postgre.yaml -n istio

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
  name: postgre
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
        host: postgres
        port:
          number: 5432
```

## 4.9mysql_proxy 

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

ef-mysql_proxy.yaml

kubectl apply -f ef-mysql_proxy.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400 
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.network.mysql_proxy
        typed_config:
          '@type': type.googleapis.com/envoy.extensions.filters.network.mysql_proxy.v3.MySQLProxy
          stat_prefix: mysql
```

## 4.10mongo_proxy 

部署mongodb

mongodb-deploy.yaml

kubectl apply -f mongodb-deploy.yaml -n istio

```

apiVersion: v1
kind: Service
metadata:
  name: mongodb
  labels:
    app: mongodb
    service: mongodb
spec:
  ports:
  - port: 27017
    name: mongo
  selector:
    app: mongodb
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mongodb-v1
  labels:
    app: mongodb
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mongodb
      version: v1
  template:
    metadata:
      labels:
        app: mongodb
        version: v1
    spec:
      containers:
      - name: mongodb 
        image: docker.io/istio/examples-bookinfo-mongodb:1.16.2
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 27017
        volumeMounts:
        - name: data-db
          mountPath: /data/db
      volumes:
      - name: data-db
        emptyDir: {}
```

vs-gw-mongodb.yaml

kubectl apply -f vs-gw-mongodb.yaml -n istio

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
  name: mongodb
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
        host: mongodb
        port:
          number: 27017
```

ef-mongo_proxy.yaml

kubectl apply -f ef-mongo_proxy.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400 
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.network.mongo_proxy
        typed_config:
          '@type': type.googleapis.com/envoy.extensions.filters.network.mongo_proxy.v3.MongoProxy
          stat_prefix: mongodb
          emit_dynamic_metadata: true
          delay:
            fixed_delay: 1s
            percentage:
              numerator: 20
              denominator: 	HUNDRED
          commands:
          - delete
          - insert
          - update
          access_log: /dev/stdout
          
```

## 4.11zookeeper_proxy 

部署zookeeper

zookeeper-deploy.yaml

kubectl apply -f zookeeper-deploy.yaml -n istio

```
apiVersion: v1
kind: Service
metadata:
  name: zookeeper
  labels:
    app: zookeeper
    service: zookeeper
spec:
  ports:
  - port: 2181
    name: zookeeper
  selector:
    app: zookeeper
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: zookeeper
  labels:
    app: zookeeper
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: zookeeper
      version: v1
  template:
    metadata:
      labels:
        app: zookeeper
        version: v1
    spec:
      containers:
      - name: zookeeper 
        image: docker.io/zookeeper:3.7.0
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: 2181
```

vs-gw-zookeeper.yaml

kubectl apply -f vs-gw-zookeeper.yaml -n istio

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
  name: zookeeper
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
        host: zookeeper
        port:
          number: 2181
```



ef-zookeeper.yaml

kubectl apply -f ef-zookeeper.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      context: GATEWAY
      listener:
        portNumber: 31400 
        filterChain:
          filter:
            name: "envoy.filters.network.tcp_proxy"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.network.zookeeper_proxy
        typed_config:
          '@type': type.googleapis.com/envoy.extensions.filters.network.zookeeper_proxy.v3.ZooKeeperProxy
          stat_prefix: zookeeper
          #max_packet_bytes: 10241024
          
```

