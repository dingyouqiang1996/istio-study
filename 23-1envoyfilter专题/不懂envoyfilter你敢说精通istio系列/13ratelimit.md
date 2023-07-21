# 1什么是ratelimit

ratelimit是限速的意思，主要作用是限值流量，防止因系统过载而崩溃。istio限速有两大类，一个是本地限速，另一个是全局限速。本地限速是在envoy内部提供一种令牌桶限速得功能，全局限速需要访问外部限速服务。按生效位置分可分为virtualhost级别的限速和route级别的限速。按协议分可分为http的限速和tcp的限速。按服务位置分可分为mesh内部限速和对外部请求的限速。按集群分可分为单集群限速和多集群限速。

# 2配置

extensions.filters.http.ratelimit.v3.RateLimit：

```
{
  "domain": "...",对应ratelimit配置文件中的域名
  "stage": "...",stage配置和action中的stage必须匹配，默认0
  "request_type": "...", internal, external or both默认both
  "timeout": "{...}",ratelimit服务请求的超时时间，默认20ms
  "failure_mode_deny": "...",如果ratelimit服务请求失败，则返回错误
  "rate_limited_as_resource_exhausted": "...",RESOURCE_EXHAUSTED code代替UNAVAILABLE code
  "rate_limit_service": "{...}",指定外部限速服务
  "enable_x_ratelimit_headers": "...",定义ratelimit头版本
  "disable_x_envoy_ratelimited_header": "..."禁用x-envoy-ratelimited头
}
```

rate_limit_service：

```
{
  "grpc_service": "{...}",配置ratelimit服务
  "transport_api_version": "..."版本
}
```

grpc_service：

```
{
  "envoy_grpc": "{...}",envoy grpc客户端
  "google_grpc": "{...}",google grpc客户端
  "timeout": "{...}",grpc请求超时时间
  "initial_metadata": []额外的元数据信息
}
```

envoy_grpc：

```
{
  "cluster_name": "...",集群名称
  "authority": "..." :authority头的值，默认cluster_name
}
```

google_grpc：

```
{
  "target_uri": "...",目标uri
  "channel_credentials": "{...}",秘钥
  "call_credentials": [],调用秘钥
  "stat_prefix": "...",stat前缀
  "credentials_factory_name": "...",秘钥工厂名称
  "config": "{...}",额外配置
  "per_stream_buffer_limit_bytes": "{...}",每个流的缓存限值，默认1MiB
  "channel_args": "{...}"通道参数
}
```

transport_api_version：

xDS API and non-xDS services version. This is used to describe both resource and transport protocol versions (in distinct configuration fields).

- AUTO

  *(DEFAULT)* ⁣When not specified, we assume v2, to ease migration to Envoy’s stable API versioning. If a client does not support v2 (e.g. due to deprecation), this is an invalid value.

- V2

  ⁣Use xDS v2 API.

- V3

  ⁣Use xDS v3 API.

enable_x_ratelimit_headers：

Defines the version of the standard to use for X-RateLimit headers.

- OFF

  *(DEFAULT)* ⁣X-RateLimit headers disabled.

- DRAFT_VERSION_03

  ⁣Use [draft RFC Version 03](https://tools.ietf.org/id/draft-polli-ratelimit-headers-03.html).




extensions.filters.network.local_ratelimit.v3.LocalRateLimit

```
{
  "stat_prefix": "...",stat前缀
  "token_bucket": "{...}",令牌桶规则
  "runtime_enabled": "{...}"启用百分比
}
```

token_bucket：

```
{
  "max_tokens": "...",最大令牌数
  "tokens_per_fill": "{...}",每次填充的令牌数
  "fill_interval": "{...}"填充间隔
}
```

runtime_enabled：

```
{
  "default_value": "{...}",百分比
  "runtime_key": "..."运行时key
}
```

config.route.v3.RateLimit

```
{
  "stage": "{...}",阶段号，必须和ratelimit过滤器匹配
  "disable_key": "...",禁用的key
  "actions": [],动作
  "limit": "{...}"动态元数据
}
```

actions：

```
{
  "source_cluster": "{...}",源集群动作
  "destination_cluster": "{...}",目标集群动作
  "request_headers": "{...}",请求头动作
  "remote_address": "{...}",远程地址动作
  "generic_key": "{...}",通用key动作
  "header_value_match": "{...}",头匹配动作
  "dynamic_metadata": "{...}",动态元数据动作
  "metadata": "{...}",元数据动作
  "extension": "{...}"扩展动作
}
```



limit：

```
{
  "dynamic_metadata": "{...}"动态元数据
}
```



extensions.filters.network.ratelimit.v3.RateLimit

```
{
  "stat_prefix": "...",
  "domain": "...",
  "descriptors": [],
  "timeout": "{...}",
  "failure_mode_deny": "...",
  "rate_limit_service": "{...}"
}
```

# 3实战

## 3.1.1http

## 3.1.1.1单集群

### 3.1.1.1.1集群内服务限流

#### 3.1.1.1.1.1本地限流

```
cat <<EOF > envoyfilter-local-rate-limit.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-local-ratelimit-svc
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.local_ratelimit
          typed_config:
            "@type": type.googleapis.com/udpa.type.v1.TypedStruct
            type_url: type.googleapis.com/envoy.extensions.filters.http.local_ratelimit.v3.LocalRateLimit
            value:
              stat_prefix: http_local_rate_limiter
              token_bucket:
                max_tokens: 10
                tokens_per_fill: 10
                fill_interval: 60s
              filter_enabled:
                runtime_key: local_rate_limit_enabled
                default_value:
                  numerator: 100
                  denominator: HUNDRED
              filter_enforced:
                runtime_key: local_rate_limit_enforced
                default_value:
                  numerator: 100
                  denominator: HUNDRED
              response_headers_to_add:
                - append: false
                  header:
                    key: x-local-rate-limit
                    value: 'true'
EOF

kubectl apply -f envoyfilter-local-rate-limit.yaml -n istio
```

说明：本地限流需要通过EnvoyFilter来实现，他不会请求外部服务，在envoy内部实现支持，是一个令牌桶的算法。http filter的名称必须是envoy.filters.http.local_ratelimit，type和typeurl是固定的，stat_prefix可以随便改，表示生成stat的指标前缀。token_bucket配置令牌桶，max_tokens表示最大令牌数量，tokens_per_fill表示每次填充的令牌数量，fill_interval表示填充令牌的间隔。filter_enabled表示启用但不是强制，filter_enforced表示强制，可以配置百分比。response_headers_to_add修改响应头信息，append为false表示修改，true表示添加。runtime_key 运行时的key，具体有啥用不清楚。



执行压测：

```
[root@node01 45]# go-stress-testing -c 10 -n 10000 -u http://192.168.229.134:30945/productpage

 开始启动  并发数:10 请求数:10000 请求参数: 
request:
 form:http 
 url:http://192.168.229.134:30945/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│      7│      2│    761│    2.94│  124.68│    1.98│ 3406.97│  21,476│  21,470│200:2;429:761
   2s│     10│      5│   1636│    2.55│ 1788.46│    1.98│ 3928.11│  52,771│  26,383│200:5;429:1636
   3s│     10│      5│   2962│    1.70│ 1788.46│    1.04│ 5871.68│  76,639│  25,545│200:5;429:2962
   4s│     10│      5│   4459│    1.28│ 1788.46│    1.04│ 7810.78│ 103,585│  25,896│200:5;429:4459
```

429 Too Many Requests (太多请求)

当你需要限制客户端请求某个服务的数量，也就是限制请求速度时，该状态码就会非常有用



清理：

```
kubectl delete envoyfilter filter-local-ratelimit-svc -n istio
```



#### 3.1.1.1.1.2全局限流

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
      - key: PATH
        value: "/productpage"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: PATH
        rate_limit:
          unit: minute
          requests_per_unit: 100
EOF

kubectl apply -f ratelimit-config.yaml -n istio
```

说明: 这个configmap是限速服务用到的配置文件，他是envoy v3版本的限速格式。domain是域名，他会在envoyfilter中被引用，descriptors的PATH,表示请求的路径可以有多个值，rate_limit配置限速配额，这里productpage配了1分钟1个请求，其他url是1分钟100个请求



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

创建了redis，和官方的一个ratelimit服务。



3创建envoy-filter

```
cat << EOF > envoyfilter-filter.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit
  namespace: istio-system
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

这个envoyfilter作用在网关上面，配置了一个http过滤器envoy.filters.http.ratelimit，和一个cluster。http 过滤器的cluster地址指向cluster配置的地址，这里就是我们的ratelimit service所在的地址。domain是上面configmap的值一样，failure_mode_deny表示超过请求限值就拒绝，rate_limit_service配置ratelimit服务的地址（cluster），这里可以配置grpc类型的也可以配置http类型的。



4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio-system
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: GATEWAY
        routeConfiguration:
          vhost:
            name: "*:80"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: # any actions in here
              - request_headers:
                  header_name: ":path"
                  descriptor_key: "PATH"
EOF

kubectl apply -f envoyfilter-action.yaml  -n istio-system
```

这个envoyfilter作用在入口网关处，给80端口的虚拟主机配置了一个rate_limits 动作，descriptor_key用于选择在configmap里配置的key。

压测：

```
[root@node01 ~]# go-stress-testing -c 10 -n 10000 -u http://192.168.229.134:30945/productpage

 开始启动  并发数:10 请求数:10000 请求参数: 
request:
 form:http 
 url:http://192.168.229.134:30945/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│      1│   1051│    1.01│   55.51│    3.70│ 9914.38│   4,183│   4,176│200:1;429:1051
   2s│     10│      1│   1629│    0.50│   55.51│    3.70│19807.86│   4,183│   2,090│200:1;429:1629
   3s│     10│      1│   2154│    0.34│   55.51│    3.70│29829.63│   4,183│   1,393│200:1;429:2154
   4s│     10│      1│   2662│    0.25│   55.51│    3.70│39823.69│   4,183│   1,045│200:1;429:2662
   5s│     10│      1│   3166│    0.20│   58.63│    3.70│49865.16│   4,183│     836│200:1;429:3166
```



清理：

```
kubectl delete cm ratelimit-config -n istio
kubectl delete -f ratelimit-deploy.yaml -n istio
kubectl delete envoyfilter filter-ratelimit -n istio-system
kubectl delete envoyfilter filter-ratelimit-svc -n istio-system
```



### 3.1.1.1.2集群外服务限流

#### 3.1.1.1.2.1本地限流

```
cat << EOF > se-baidu.yaml
apiVersion: networking.istio.io/v1alpha3
kind: ServiceEntry
metadata:
  name: baidu
spec:
  hosts:
    - www.baidu.com
  ports:
    - number: 80
      name: http-port
      protocol: HTTP
  resolution: DNS
EOF

kubectl apply -f se-baidu.yaml -n istio
```

说明，创建了访问百度的service entry

```
cat <<EOF > envoyfilter-local-rate-limit-http-outside.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-local-ratelimit-svc
spec:
  workloadSelector:
    labels:
      app: ratings
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_OUTBOUND
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
              subFilter:
                name: "envoy.filters.http.router"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.local_ratelimit
          typed_config:
            "@type": type.googleapis.com/udpa.type.v1.TypedStruct
            type_url: type.googleapis.com/envoy.extensions.filters.http.local_ratelimit.v3.LocalRateLimit
            value:
              stat_prefix: http_local_rate_limiter
              token_bucket:
                max_tokens: 1
                tokens_per_fill: 1
                fill_interval: 60s
              filter_enabled:
                runtime_key: local_rate_limit_enabled
                default_value:
                  numerator: 100
                  denominator: HUNDRED
              filter_enforced:
                runtime_key: local_rate_limit_enforced
                default_value:
                  numerator: 100
                  denominator: HUNDRED
              response_headers_to_add:
                - append: false
                  header:
                    key: x-local-rate-limit
                    value: 'true'
EOF

kubectl apply -f envoyfilter-local-rate-limit-http-outside.yaml -n istio
```

说明：SIDECAR_OUTBOUND表示对外发出请求起作用。本地限流需要通过EnvoyFilter来实现，他不会请求外部服务，在envoy内部实现支持，是一个令牌桶的算法。http filter的名称必须是envoy.filters.http.local_ratelimit，type和typeurl是固定的，stat_prefix可以随便改，表示生成stat的指标前缀。token_bucket配置令牌桶，max_tokens表示最大令牌数量，tokens_per_fill表示每次填充的令牌数量，fill_interval表示填充令牌的间隔。filter_enabled表示启用但不是强制，filter_enforced表示强制，可以配置百分比。response_headers_to_add修改响应头信息，append为false表示修改，true表示添加。runtime_key 运行时的key，具体有啥用不清楚。



```
kubectl exec -it -n istio ratings-v2-mysql-vm-66dc56449d-lk6gv /bin/bash

local_rate_limitednode@ratings-v2-mysql-vm-66dc56449d-lk6gv:/opt/microservices$ curl www.baidu.com -I
HTTP/1.1 429 Too Many Requests
x-local-rate-limit: true
content-length: 18
content-type: text/plain
date: Fri, 17 Sep 2021 23:20:13 GMT
server: envoy
```

进入ratings容器，对百度发请求，409错误，说明限流生效

清理：

```
kubectl delete se baidu -n istio
kubectl delete envoyfilter filter-local-ratelimit-svc -n istio
```



#### 3.1.1.1.2.2全局限流

部署ratelimit

1创建cm

```
cat << EOF > ratelimit-config-outside-http.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ratelimit-config
data:
  config.yaml: |
    domain: productpage-ratelimit
    descriptors:
      - key: PATH
        value: "/"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: PATH
        value: "/aa"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: PATH
        rate_limit:
          unit: minute
          requests_per_unit: 100
EOF

kubectl apply -f ratelimit-config-outside-http.yaml -n istio
```

说明: 这个configmap是限速服务用到的配置文件，他是envoy v3版本的限速格式。domain是域名，他会在envoyfilter中被引用，descriptors的PATH,表示请求的路径可以有多个值，rate_limit配置限速配额，这里productpage配了1分钟1个请求，/aa一分钟1个请求，其他url是1分钟100个请求



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

创建了redis，和官方的一个ratelimit服务。



```
cat << EOF > se-baidu.yaml
apiVersion: networking.istio.io/v1alpha3
kind: ServiceEntry
metadata:
  name: baidu
spec:
  hosts:
    - www.baidu.com
  ports:
    - number: 80
      name: http-port
      protocol: HTTP
  resolution: DNS
EOF

kubectl apply -f se-baidu.yaml -n istio
```

创建访问百度的serviceentry



创建envoy-filter

```
cat << EOF > envoyfilter-filter-outside-http.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: ratings
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_OUTBOUND
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

kubectl apply -f envoyfilter-filter-outside-http.yaml -n istio
```

这个envoyfilter作用在ratings上面，SIDECAR_OUTBOUND作用的对外流量上面，配置了一个http过滤器envoy.filters.http.ratelimit，和一个cluster。http 过滤器的cluster地址指向cluster配置的地址，这里就是我们的ratelimit service所在的地址。domain是上面configmap的值一样，failure_mode_deny表示超过请求限值就拒绝，rate_limit_service配置ratelimit服务的地址（cluster），这里可以配置grpc类型的也可以配置http类型的。



4创建action envoyfilter

```
cat << EOF > envoyfilter-action-outside-http.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: ratings
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_OUTBOUND
        routeConfiguration:
          vhost:
            name: "www.baidu.com:80"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: # any actions in here
              - request_headers:
                  header_name: ":path"
                  descriptor_key: "PATH"
EOF

kubectl apply -f envoyfilter-action-outside-http.yaml   -n istio
```

host是我们配置的百度的地址（www.baidu.com:80），这个envoyfilter作用在ratings处，给80端口的虚拟主机配置了一个rate_limits 动作，descriptor_key用于选择在configmap里配置的key。

```
kubectl exec -it -n istio ratings-v2-mysql-vm-66dc56449d-lk6gv /bin/bash

node@ratings-v2-mysql-vm-66dc56449d-lk6gv:/opt/microservices$ curl www.baidu.com/ -I
HTTP/1.1 429 Too Many Requests
x-envoy-ratelimited: true
date: Fri, 17 Sep 2021 23:51:33 GMT
server: envoy
transfer-encoding: chunked
```

进入rating容器，向百度发请求，409错误，说明限流成功

清理：

```
kubectl delete cm ratelimit-config -n istio
kubectl delete -f ratelimit-deploy.yaml -n istio
kubectl delete envoyfilter filter-ratelimit -n istio
kubectl delete envoyfilter filter-ratelimit-svc -n istio
```



## 3.1.1.2多集群

###  3.1.1.2.1准备集群

![arch](D:/公众号/图文/45image/arch.jpg)

这里多集群安装我们不在展开，不懂得可以看我之前的文章。

```
集群1
128,129,130
集群2
131,132,133

两个网络联通
128。129.130
route add -net 172.21.1.0 netmask 255.255.255.0 gw 192.168.229.131
route add -net 172.21.2.0 netmask 255.255.255.0 gw 192.168.229.133
route add -net 172.21.0.0 netmask 255.255.255.0 gw 192.168.229.132
route add -net 10.69.0.0 netmask 255.255.0.0 gw 192.168.229.131

131,132，133
route add -net 172.20.0.0 netmask 255.255.255.0 gw 192.168.229.128
route add -net 172.20.1.0 netmask 255.255.255.0 gw 192.168.229.129
route add -net 172.20.2.0 netmask 255.255.255.0 gw 192.168.229.130
route add -net 10.68.0.0 netmask 255.255.0.0 gw 192.168.229.128


cluster1:
生成istio安装operator文件
cat <<EOF > cluster1.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster1
      network: network1
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF


生成istio安装operator文件
cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  values:
    global:
      meshID: mesh1
      multiCluster:
        clusterName: cluster2
      network: network1
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
EOF

把部署文件传到cluster2
scp cluster2.yaml root@192.168.229.131:/root


cluster1:
生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster1  --server=https://192.168.229.128:6443 > remote-secret-cluster1.yaml
 传输secret到cluster2
scp remote-secret-cluster1.yaml root@192.168.229.131:/root

cluster2
生成访问apiserver secret
 istioctl x create-remote-secret --name=cluster2  --server=https://192.168.229.131:6443 > remote-secret-cluster2.yaml
  传输secret到cluster2
 scp remote-secret-cluster2.yaml root@192.168.229.128:/root
 
 cluster1
 应用secret
 kubectl apply -f remote-secret-cluster2.yaml
 
 部署集群
 istioctl install  -f cluster1.yaml
  
  
  cluster2:
  应用secret
  kubectl apply -f remote-secret-cluster1.yaml
  部署集群
istioctl install  -f cluster2.yaml

 cluster1: 
  重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
 
 cluster2:
   重启pod
 kubectl rollout restart deploy -n istio
 kubectl rollout restart deploy -n istio-system
```

清理：

```
cluster1:

kubectl delete secret istio-remote-secret-cluster2 -n istio-system
istioctl x uninstall -f cluster1.yaml

reboot

cluster2:

kubectl delete secret istio-remote-secret-cluster1 -n istio-system
istioctl x uninstall -f cluster2.yaml

reboot
```

### 3.1.1.2.2集群内本地限流

多集群只演示集群内本地限流其他是一样的

```
cat <<EOF > envoyfilter-local-rate-limit-multi-http.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-local-ratelimit-svc
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        listener:
          filterChain:
            filter:
              name: "envoy.filters.network.http_connection_manager"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.local_ratelimit
          typed_config:
            "@type": type.googleapis.com/udpa.type.v1.TypedStruct
            type_url: type.googleapis.com/envoy.extensions.filters.http.local_ratelimit.v3.LocalRateLimit
            value:
              stat_prefix: http_local_rate_limiter
              token_bucket:
                max_tokens: 10
                tokens_per_fill: 10
                fill_interval: 60s
              filter_enabled:
                runtime_key: local_rate_limit_enabled
                default_value:
                  numerator: 100
                  denominator: HUNDRED
              filter_enforced:
                runtime_key: local_rate_limit_enforced
                default_value:
                  numerator: 100
                  denominator: HUNDRED
              response_headers_to_add:
                - append: false
                  header:
                    key: x-local-rate-limit
                    value: 'true'
EOF

kubectl apply -f envoyfilter-local-rate-limit-multi-http.yaml -n istio
```

说明：本地限流需要通过EnvoyFilter来实现，他不会请求外部服务，在envoy内部实现支持，是一个令牌桶的算法。http filter的名称必须是envoy.filters.http.local_ratelimit，type和typeurl是固定的，stat_prefix可以随便改，表示生成stat的指标前缀。token_bucket配置令牌桶，max_tokens表示最大令牌数量，tokens_per_fill表示每次填充的令牌数量，fill_interval表示填充令牌的间隔。filter_enabled表示启用但不是强制，filter_enforced表示强制，可以配置百分比。response_headers_to_add修改响应头信息，append为false表示修改，true表示添加。runtime_key 运行时的key，具体有啥用不清楚。

开启压测：

```
[root@node01 45]# go-stress-testing -c 10 -n 10000 -u http://192.168.229.128:30363/productpage

 开始启动  并发数:10 请求数:10000 请求参数: 
request:
 form:http 
 url:http://192.168.229.128:30363/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│      0│      0│      0│    0.00│    0.00│    0.00│    0.00│        │        │
   2s│      7│     16│      6│   15.25│ 1453.38│    4.56│  655.73│  71,950│  35,974│200:16;429:6
   3s│      7│     17│      6│   14.30│ 1453.38│    4.56│  699.44│  76,133│  25,376│200:17;429:6
   4s│     10│     34│     24│   14.30│ 3207.96│    2.71│  699.46│ 154,262│  38,559│200:34;429:24
   5s│     10│     78│     68│   16.03│ 3207.96│    2.71│  623.67│ 370,054│  74,009│200:78;429:68
   6s│     10│    160│    150│   26.98│ 3207.96│    2.71│  370.66│ 770,420│ 128,402│200:160;429:150
   7s│     10│    238│    228│   34.53│ 3207.96│    2.71│  289.60│1,148,994│ 164,131│200:238;429:228
```

<span style="color:red;">发现多集群和单集群的配置是一样，同样生效</span>

## 3.2.2tcp

### 3.2.2.1单集群

#### 3.2.2.1.1集群内服务限流

##### 3.2.2.1.1.1本地限流

部署mysql

```
cat << EOF > mysql.yaml
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
EOF

kubectl apply -f mysql.yaml -n istio
```

说明：部署了mysql服务，ratings获取数据时会请求这个服务

部署mysql版ratings

```
cat << EOF > ratings-mysql.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ratings-v2-mysql
  labels:
    app: ratings
    version: v2-mysql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ratings
      version: v2-mysql
  template:
    metadata:
      labels:
        app: ratings
        version: v2-mysql
    spec:
      containers:
      - name: ratings
        image: docker.io/istio/examples-bookinfo-ratings-v2:1.16.2
        imagePullPolicy: IfNotPresent
        env:
          # ratings-v2 will use mongodb as the default db backend.
          # if you would like to use mysqldb then you can use this file
          # which sets DB_TYPE = 'mysql' and the rest of the parameters shown
          # here and also create the # mysqldb service using bookinfo-mysql.yaml
          # NOTE: This file is mutually exclusive to bookinfo-ratings-v2.yaml
          - name: DB_TYPE
            value: "mysql"
          - name: MYSQL_DB_HOST
            value: mysqldb
          - name: MYSQL_DB_PORT
            value: "3306"
          - name: MYSQL_DB_USER
            value: root
          - name: MYSQL_DB_PASSWORD
            value: password
        ports:
        - containerPort: 9080
        securityContext:
          runAsUser: 1000
EOF

kubectl apply -f ratings-mysql.yaml -n istio
```

部署了mysql版的ratings，是指了env变量。



```
cat <<EOF > envoyfilter-local-rate-limit-mysql-inside.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-local-ratelimit-svc
spec:
  workloadSelector:
    labels:
      app: mysqldb
      version: v1
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
          name: envoy.filters.network.local_ratelimit
          typed_config:
            "@type": type.googleapis.com/udpa.type.v1.TypedStruct
            type_url: type.googleapis.com/envoy.extensions.filters.network.local_ratelimit.v3.LocalRateLimit
            value:
              stat_prefix: tcp_local_rate_limiter
              token_bucket:
                max_tokens: 1
                tokens_per_fill: 1
                fill_interval: 60s
              runtime_enabled:
                runtime_key: tcp_rate_limit_enabled
                default_value: true
EOF

kubectl apply -f envoyfilter-local-rate-limit-mysql-inside.yaml -n istio
```

注意这里applyTo的是NETWORK_FILTER，因为mysql是tcp服务，不是http服务，filter的名字是envoy.filters.network.local_ratelimit，type_url也是固定的不要写错。token_bucket配置了访问限速的令牌数量及其填充速度。我们设置的filter在envoy.filters.network.tcp_proxy前面，所以是INSERT_BEFORE。

![3](D:/公众号/图文/45image/3.jpg)

清理：

```
kubectl delete -f  mysql.yaml -n istio
kubectl delete -f ratings-mysql.yaml -n istio
kubectl delete envoyfilter filter-local-ratelimit-svc -n istio
```



##### 3.2.2.1.1.2全局限流

部署mysql

wechat/envoyfilter/ratelimit/tcp/inner/global/mysql.yaml

```
cat << EOF > mysql.yaml
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
EOF

kubectl apply -f mysql.yaml -n istio
```

部署mysql版ratings

```
cat << EOF > ratings-mysql.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ratings-v2-mysql
  labels:
    app: ratings
    version: v2-mysql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ratings
      version: v2-mysql
  template:
    metadata:
      labels:
        app: ratings
        version: v2-mysql
    spec:
      containers:
      - name: ratings
        image: docker.io/istio/examples-bookinfo-ratings-v2:1.16.2
        imagePullPolicy: IfNotPresent
        env:
          # ratings-v2 will use mongodb as the default db backend.
          # if you would like to use mysqldb then you can use this file
          # which sets DB_TYPE = 'mysql' and the rest of the parameters shown
          # here and also create the # mysqldb service using bookinfo-mysql.yaml
          # NOTE: This file is mutually exclusive to bookinfo-ratings-v2.yaml
          - name: DB_TYPE
            value: "mysql"
          - name: MYSQL_DB_HOST
            value: mysqldb
          - name: MYSQL_DB_PORT
            value: "3306"
          - name: MYSQL_DB_USER
            value: root
          - name: MYSQL_DB_PASSWORD
            value: password
        ports:
        - containerPort: 9080
        securityContext:
          runAsUser: 1000
EOF

kubectl apply -f ratings-mysql.yaml -n istio
```



部署ratelimit

1创建cm

```
cat << EOF > ratelimit-config-mysql-inside.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ratelimit-config
data:
  config.yaml: |
    domain: mysql-ratelimit
    descriptors:
      - key: test
        value: "test"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: test
        rate_limit:
          unit: minute
          requests_per_unit: 10
EOF

kubectl apply -f ratelimit-config-mysql-inside.yaml -n istio
```

说明: 这个configmap是限速服务用到的配置文件，他是envoy v3版本的限速格式。domain是域名，他会在envoyfilter中被引用，descriptors的PATH,表示请求的路径可以有多个值，rate_limit配置限速配额，这里productpage配了1分钟1个请求，其他url是1分钟100个请求



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

创建了redis，和官方的一个ratelimit服务。



3创建envoy-filter

```
cat << EOF > envoyfilter-filter-mysql-inside.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: mysqldb
      version: v1
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
          name: envoy.filters.network.ratelimit
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.network.ratelimit.v3.RateLimit
            domain: mysql-ratelimit
            failure_mode_deny: true
            stat_prefix: mysql_ratelimit
            descriptors:
            - entries:
              - key: test
                value: test
              limit: 
                requests_per_unit: 2
                unit: MINUTE
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

kubectl apply -f envoyfilter-filter-mysql-inside.yaml -n istio
```



```
node@ratings-v2-mysql-565f8fd887-8hp9s:/opt/microservices$ curl ratings.istio.svc:9080/ratings/0
{"id":0,"ratings":{"Reviewer1":5,"Reviewer2":4}}node@ratings-v2-mysql-565f8fd887-8hp9s:/opt/microservices$ curl ratings.istio.svc:9080/ratings/0
{"error":"could not connect to ratings database"}node@ratings-v2-mysql-565f8fd887-8hp9s:/opt/microservices$ curl ratings.istio.svc:9080/ratings/0
{"error":"could not connect to ratings database"}node@ratings-v2-mysql-565f8fd887-8hp9s:/opt/microservices$
```

清理：

```
kubectl delete -f mysql.yaml -n istio
kubectl delete -f ratings-mysql.yaml -n istio
kubectl delete -f envoyfilter-filter-mysql-inside.yaml -n istio
kubectl delete -f ratelimit-deploy.yaml -n istio
kubectl delete -f ratelimit-config-mysql-inside.yaml -n istio
```



#### 3.2.2.1.2集群外服务限流

##### 3.2.2.1.2.1本地限流

部署rating-v2

```
cat << EOF > bookinfo-ratings-v2-mysql-vm.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ratings-v2-mysql-vm
  labels:
    app: ratings
    version: v2-mysql-vm
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ratings
      version: v2-mysql-vm
  template:
    metadata:
      labels:
        app: ratings
        version: v2-mysql-vm
    spec:
      containers:
      - name: ratings
        image: docker.io/istio/examples-bookinfo-ratings-v2:1.16.2
        imagePullPolicy: IfNotPresent
        env:
          # This assumes you registered your mysql vm as
          # istioctl register -n vm mysqldb 1.2.3.4 3306
          - name: DB_TYPE
            value: "mysql"
          - name: MYSQL_DB_HOST
            value: mysql.vm.demo
          - name: MYSQL_DB_PORT
            value: "3306"
          - name: MYSQL_DB_USER
            value: root
          - name: MYSQL_DB_PASSWORD
            value: root
        ports:
        - containerPort: 9080
        securityContext:
          runAsUser: 1000
EOF


kubectl apply -f bookinfo-ratings-v2-mysql-vm.yaml -n istio
```

<span style="color:red;">在vm上部署mysql，这个略过，有需要文档的同学，可以加我微信，因为有点复杂</span>

创建serviceentry

```
cat << EOF > se-mysql.yaml
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mysql-se
spec:
  hosts:
  - mysql.vm.demo
  addresses:
  - 192.168.229.12
  location: MESH_INTERNAL
  ports:
  - number: 3306
    name: mysql
    protocol: TCP
    targetPort: 3306
  resolution: STATIC
  workloadSelector:
    labels:
      app: mysql
      type: vm
EOF

kubectl apply -f se-mysql.yaml -n vm
```

这里创建了一个访问我们部署的虚拟机服务mysql的serviceentry



coredns配置加上解析记录

```
apiVersion: v1
data:
  Corefile: |
    .:53 {
        errors
        health {
            lameduck 5s
        }
        hosts {
            192.168.229.11  httpd.vm.demo
            192.168.229.12  mysql.vm.demo
            fallthrough
        }
        ready
        kubernetes cluster.local in-addr.arpa ip6.arpa {
            pods insecure
            fallthrough in-addr.arpa ip6.arpa
            ttl 30
        }
        prometheus :9153
        forward . /etc/resolv.conf {
            max_concurrent 1000
        }
        cache 30
        reload
        loadbalance
    }
kind: ConfigMap
```

添加192.168.229.12  mysql.vm.demo这一段

重启coredns

kubectl rollout restart -n kube-system deployment coredns 



执行压测

```
go-stress-testing -c 1 -n 10000 -u  http://192.168.229.134:32688/productpage
```

```
[root@node01 ~]# go-stress-testing -c 1 -n 10000 -u  http://192.168.229.134:32688/productpage

 开始启动  并发数:1 请求数:10000 请求参数: 
request:
 form:http 
 url:http://192.168.229.134:32688/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│      1│     18│      0│   18.20│   91.36│   17.66│   54.94│  87,270│  87,264│200:18
   2s│      1│     37│      0│   19.11│   91.40│   13.13│   52.34│ 178,723│  89,351│200:37
   3s│      1│     54│      0│   18.42│   97.80│   13.13│   54.30│ 260,814│  86,928│200:54
   4s│      1│     72│      0│   18.22│   97.80│   13.13│   54.88│ 349,080│  87,258│200:72
   5s│      1│     90│      0│   18.17│  103.04│   12.98│   55.04│ 436,350│  87,264│200:90
   6s│      1│    111│      0│   18.59│  103.04│   12.98│   53.80│ 538,165│  89,686│200:111
   7s│      1│    132│      0│   18.91│  103.04│   12.98│   52.89│ 638,984│  91,279│200:132
   8s│      1│    150│      0│   18.88│  103.04│   12.30│   52.95│ 727,250│  90,905│200:150
```

没有限流之前压测都是200的返回结果



```
cat <<EOF > envoyfilter-local-rate-limit-mysql-vm-outside.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-local-ratelimit-svc
spec:
  workloadSelector:
    labels:
      app: ratings
  configPatches:
    - applyTo: NETWORK_FILTER
      match:
        context: SIDECAR_OUTBOUND
        listener:
          portNumber: 3306
          filterChain:
            filter:
              name: "envoy.filters.network.tcp_proxy"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.network.local_ratelimit
          typed_config:
            "@type": type.googleapis.com/udpa.type.v1.TypedStruct
            type_url: type.googleapis.com/envoy.extensions.filters.network.local_ratelimit.v3.LocalRateLimit
            value:
              stat_prefix: tcp_local_rate_limiter
              token_bucket:
                max_tokens: 10
                tokens_per_fill: 10
                fill_interval: 60s
              runtime_enabled:
                runtime_key: tcp_rate_limit_enabled
                default_value: true
EOF

kubectl apply -f envoyfilter-local-rate-limit-mysql-vm-outside.yaml -n istio
```

部署envoyfilter使限流生效，作用在ratings服务上面，而且是出口流量SIDECAR_OUTBOUND，network filter名称必须事envoy.filters.network.local_ratelimit，type _url也是固定的。token_bucket设置令牌桶。



![4](D:/公众号/图文/45image/4.jpg)

这里无法连接数据库，说明数据库被限流了，ratings无法连接vm mysql服务。

清理：

```
kubectl delete envoyfilter filter-local-ratelimit-svc -n istio
kubectl delete se mysql-se -n vm
```



##### 3.2.2.1.2.2全局限流

部署rating-v2

```
cat << EOF > bookinfo-ratings-v2-mysql-vm.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ratings-v2-mysql-vm
  labels:
    app: ratings
    version: v2-mysql-vm
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ratings
      version: v2-mysql-vm
  template:
    metadata:
      labels:
        app: ratings
        version: v2-mysql-vm
    spec:
      containers:
      - name: ratings
        image: docker.io/istio/examples-bookinfo-ratings-v2:1.16.2
        imagePullPolicy: IfNotPresent
        env:
          # This assumes you registered your mysql vm as
          # istioctl register -n vm mysqldb 1.2.3.4 3306
          - name: DB_TYPE
            value: "mysql"
          - name: MYSQL_DB_HOST
            value: mysql.vm.demo
          - name: MYSQL_DB_PORT
            value: "3306"
          - name: MYSQL_DB_USER
            value: root
          - name: MYSQL_DB_PASSWORD
            value: root
        ports:
        - containerPort: 9080
        securityContext:
          runAsUser: 1000
EOF


kubectl apply -f bookinfo-ratings-v2-mysql-vm.yaml -n istio
```

<span style="color:red;">在vm上部署mysql，这个略过，有需要文档的同学，可以加我微信，因为有点复杂</span>

创建serviceentry

```
cat << EOF > se-mysql.yaml
apiVersion: networking.istio.io/v1beta1
kind: ServiceEntry
metadata:
  name: mysql-se
spec:
  hosts:
  - mysql.vm.demo
  addresses:
  - 192.168.229.12
  location: MESH_INTERNAL
  ports:
  - number: 3306
    name: mysql
    protocol: TCP
    targetPort: 3306
  resolution: STATIC
  workloadSelector:
    labels:
      app: mysql
      type: vm
EOF

kubectl apply -f se-mysql.yaml -n vm
```

这里创建了一个访问我们部署的虚拟机服务mysql的serviceentry



coredns配置加上解析记录

```
apiVersion: v1
data:
  Corefile: |
    .:53 {
        errors
        health {
            lameduck 5s
        }
        hosts {
            192.168.229.11  httpd.vm.demo
            192.168.229.12  mysql.vm.demo
            fallthrough
        }
        ready
        kubernetes cluster.local in-addr.arpa ip6.arpa {
            pods insecure
            fallthrough in-addr.arpa ip6.arpa
            ttl 30
        }
        prometheus :9153
        forward . /etc/resolv.conf {
            max_concurrent 1000
        }
        cache 30
        reload
        loadbalance
    }
kind: ConfigMap
```

添加192.168.229.12  mysql.vm.demo这一段

重启coredns

kubectl rollout restart -n kube-system deployment coredns 



执行压测

```
go-stress-testing -c 1 -n 10000 -u  http://192.168.229.134:32688/productpage
```

```
[root@node01 ~]# go-stress-testing -c 1 -n 10000 -u  http://192.168.229.134:32688/productpage

 开始启动  并发数:1 请求数:10000 请求参数: 
request:
 form:http 
 url:http://192.168.229.134:32688/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│      1│     18│      0│   18.20│   91.36│   17.66│   54.94│  87,270│  87,264│200:18
   2s│      1│     37│      0│   19.11│   91.40│   13.13│   52.34│ 178,723│  89,351│200:37
   3s│      1│     54│      0│   18.42│   97.80│   13.13│   54.30│ 260,814│  86,928│200:54
   4s│      1│     72│      0│   18.22│   97.80│   13.13│   54.88│ 349,080│  87,258│200:72
   5s│      1│     90│      0│   18.17│  103.04│   12.98│   55.04│ 436,350│  87,264│200:90
   6s│      1│    111│      0│   18.59│  103.04│   12.98│   53.80│ 538,165│  89,686│200:111
   7s│      1│    132│      0│   18.91│  103.04│   12.98│   52.89│ 638,984│  91,279│200:132
   8s│      1│    150│      0│   18.88│  103.04│   12.30│   52.95│ 727,250│  90,905│200:150
```

没有限流之前压测都是200的返回结果



```
cat << EOF > envoyfilter-filter-mysql-outside.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: ratings
  configPatches:
    - applyTo: NETWORK_FILTER
      match:
        context: SIDECAR_OUTBOUND
        listener:
          portNumber: 3306
          filterChain:
            filter:
              name: "envoy.filters.network.tcp_proxy"
      patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.network.ratelimit
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.network.ratelimit.v3.RateLimit
            domain: mysql-ratelimit
            failure_mode_deny: true
            stat_prefix: mysql_ratelimit
            descriptors:
            - entries:
              - key: test
                value: test
              limit: 
                requests_per_unit: 2
                unit: MINUTE
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

kubectl apply -f envoyfilter-filter-mysql-outside.yaml -n istio
```



清理

```
kubectl delete envoyfilter filter-local-ratelimit-svc -n istio
kubectl delete se mysql-se -n vm
```



#### 3.2.2.2多集群

##### 3.2.2.2.1集群准备

同上

##### 3.2.2.2.2集群内本地限流

cluster1,cluster2部署mysql

```
cat << EOF > mysql.yaml
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
EOF

kubectl apply -f mysql.yaml -n istio

```

说明：部署了mysql服务，ratings获取数据时会请求这个服务

cluster1,cluster2部署mysql版ratings

```
cat << EOF > ratings-mysql.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ratings-v2-mysql
  labels:
    app: ratings
    version: v2-mysql
spec:
  replicas: 1
  selector:
    matchLabels:
      app: ratings
      version: v2-mysql
  template:
    metadata:
      labels:
        app: ratings
        version: v2-mysql
    spec:
      containers:
      - name: ratings
        image: docker.io/istio/examples-bookinfo-ratings-v2:1.16.2
        imagePullPolicy: IfNotPresent
        env:
          # ratings-v2 will use mongodb as the default db backend.
          # if you would like to use mysqldb then you can use this file
          # which sets DB_TYPE = 'mysql' and the rest of the parameters shown
          # here and also create the # mysqldb service using bookinfo-mysql.yaml
          # NOTE: This file is mutually exclusive to bookinfo-ratings-v2.yaml
          - name: DB_TYPE
            value: "mysql"
          - name: MYSQL_DB_HOST
            value: mysqldb
          - name: MYSQL_DB_PORT
            value: "3306"
          - name: MYSQL_DB_USER
            value: root
          - name: MYSQL_DB_PASSWORD
            value: password
        ports:
        - containerPort: 9080
        securityContext:
          runAsUser: 1000
EOF

kubectl apply -f ratings-mysql.yaml -n istio
```

部署了mysql版的ratings，是指了env变量。



```
cat <<EOF > envoyfilter-local-rate-limit-mysql-inside.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-local-ratelimit-svc
spec:
  workloadSelector:
    labels:
      app: mysqldb
      version: v1
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
          name: envoy.filters.network.local_ratelimit
          typed_config:
            "@type": type.googleapis.com/udpa.type.v1.TypedStruct
            type_url: type.googleapis.com/envoy.extensions.filters.network.local_ratelimit.v3.LocalRateLimit
            value:
              stat_prefix: tcp_local_rate_limiter
              token_bucket:
                max_tokens: 1
                tokens_per_fill: 1
                fill_interval: 60s
              runtime_enabled:
                runtime_key: tcp_rate_limit_enabled
                default_value: true
EOF

cluster1，cluster2：
kubectl apply -f envoyfilter-local-rate-limit-mysql-inside.yaml -n istio
```

注意这里applyTo的是NETWORK_FILTER，因为mysql是tcp服务，不是http服务，filter的名字是envoy.filters.network.local_ratelimit，type_url也是固定的不要写错。token_bucket配置了访问限速的令牌数量及其填充速度。我们设置的filter在envoy.filters.network.tcp_proxy前面，所以是INSERT_BEFORE。



![9](D:/公众号/图文/45image/9.jpg)

<span style="color:red">多集群集群内本地限流，需要在每个istiod里面增加ratelimit配置</span>

```
清理：
kubectl delete envoyfilter filter-local-ratelimit-svc -n istio
```

## 3.3不同限流动作

### 3.3.1source_cluster

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
      - key: source_cluster
        value: "outbound|80||istio-ingressgateway.istio-system.svc.cluster.local"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: source_cluster
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
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: productpage
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
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

kubectl apply -f envoyfilter-filter.yaml -n istio
```



4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_INBOUND
        routeConfiguration:
          vhost:
            name: "inbound|http|9080"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: 
              - source_cluster: {}
EOF

kubectl apply -f envoyfilter-action.yaml  -n istio
```



```
[root@node01 ~]# go-stress-testing -n 1000000 -c 10 -u http://192.168.229.135:32688/productpage

 开始启动  并发数:10 请求数:1000000 请求参数: 
request:
 form:http 
 url:http://192.168.229.135:32688/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│     10│    741│   10.06│  164.41│    5.07│  994.21│  48,814│  48,793│200:10;429:739;500:2
   2s│     10│     10│   1644│    5.02│  164.41│    5.07│ 1992.68│  48,814│  24,403│200:10;429:1642;500:2
   3s│     10│     10│   2486│    3.34│  164.41│    4.99│ 2989.70│  48,814│  16,268│200:10;429:2484;500:2
   4s│     10│     10│   3332│    2.51│  164.41│    4.94│ 3989.95│  48,814│  12,203│200:10;429:3330;500:2
   5s│     10│     10│   4213│    2.00│  164.41│    4.61│ 4988.90│  48,814│   9,762│200:10;429:4207;500:6
```



### 3.3.2destination_cluster

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
      - key: destination_cluster
        value: "inbound|9080||"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: destination_cluster
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
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: productpage
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
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

kubectl apply -f envoyfilter-filter.yaml -n istio
```



4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_INBOUND
        routeConfiguration:
          vhost:
            name: "inbound|http|9080"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: 
              - destination_cluster: {}
EOF

kubectl apply -f envoyfilter-action.yaml  -n istio
```



```
[root@node01 ~]# go-stress-testing -n 1000000 -c 10 -u http://192.168.229.135:32688/productpage

 开始启动  并发数:10 请求数:1000000 请求参数: 
request:
 form:http 
 url:http://192.168.229.135:32688/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│      1│    799│    1.01│   38.49│    5.84│ 9904.46│   4,183│   4,182│200:1;429:799
   2s│     10│      1│   1703│    0.50│   38.49│    5.31│19918.49│   4,183│   2,090│200:1;429:1703
   3s│     10│      1│   2553│    0.33│   38.49│    4.88│29894.78│   4,183│   1,394│200:1;429:2553
   4s│     10│      1│   3452│    0.25│   38.49│    4.88│39907.66│   4,183│   1,045│200:1;429:3452
   5s│     10│      1│   4339│    0.20│   38.49│    4.26│49863.27│   4,183│     836│200:1;429:4339
   6s│     10│      1│   5130│    0.17│   38.49│    4.26│59819.79│   4,183│     697│200:1;429:5130
   7s│     10│      1│   5945│    0.14│   38.49│    4.26│69888.47│   4,183│     597│200:1;429:5945
   8s│     10│      1│   6773│    0.13│   38.49│    4.26│79848.79│   4,183│     522│200:1;429:6772;500:1
```



### 3.3.3request_headers

略

### 3.3.4remote_address

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
      - key: remote_address
        value: "172.20.0.0"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: remote_address
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
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: productpage
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
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

kubectl apply -f envoyfilter-filter.yaml -n istio
```



4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_INBOUND
        routeConfiguration:
          vhost:
            name: "inbound|http|9080"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: 
              - remote_address: {}
EOF

kubectl apply -f envoyfilter-action.yaml  -n istio
```



```
[root@node01 ~]# go-stress-testing -n 1000000 -c 10 -u http://192.168.229.135:32688/productpage

 开始启动  并发数:10 请求数:1000000 请求参数: 
request:
 form:http 
 url:http://192.168.229.135:32688/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│      1│    825│    1.01│   58.24│    4.89│ 9914.57│   4,183│   4,181│200:1;429:825
   2s│     10│      1│   1652│    0.50│   58.24│    4.89│19930.21│   4,183│   2,091│200:1;429:1652
   3s│     10│      1│   2565│    0.33│   58.24│    4.89│29894.37│   4,183│   1,394│200:1;429:2565
   4s│     10│      1│   3433│    0.25│   58.24│    4.76│39857.25│   4,183│   1,045│200:1;429:3433
   5s│     10│      1│   4176│    0.20│   58.24│    4.76│49888.21│   4,183│     836│200:1;429:4176
```





### 3.3.5generic_key

```
{
  "descriptor_value": "...",
  "descriptor_key": "..."
}
```

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
        value: "test"
        rate_limit:
          unit: minute
          requests_per_unit: 2
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
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: productpage
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
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

kubectl apply -f envoyfilter-filter.yaml -n istio
```



4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_INBOUND
        routeConfiguration:
          vhost:
            name: "inbound|http|9080"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: 
              - generic_key:
                  descriptor_key: test
                  descriptor_value: test
EOF

kubectl apply -f envoyfilter-action.yaml  -n istio
```



```
[root@node01 ~]# go-stress-testing -n 1000000 -c 10 -u http://192.168.229.135:32688/productpage

 开始启动  并发数:10 请求数:1000000 请求参数: 
request:
 form:http 
 url:http://192.168.229.135:32688/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│      0│    789│    0.00│   27.99│    5.37│    0.00│        │        │429:789
   2s│     10│      0│   1624│    0.00│   27.99│    4.61│    0.00│        │        │429:1624
   3s│     10│      0│   2492│    0.00│   27.99│    4.61│    0.00│        │        │429:2492
   4s│     10│      0│   3335│    0.00│   27.99│    4.21│    0.00│        │        │429:3335
   5s│     10│      0│   4181│    0.00│   27.99│    4.21│    0.00│        │        │429:4181
   6s│     10│      0│   5026│    0.00│   27.99│    4.21│    0.00│        │        │429:5026
   7s│     10│      0│   5842│    0.00│   27.99│    4.21│    0.00│        │        │429:5842
   8s│     10│      0│   6721│    0.00│   27.99│    4.21│    0.00│        │        │429:6721
   9s│     10│      2│   7586│    0.22│   95.85│    4.21│44910.75│  10,362│   1,151│200:2;429:7586
  10s│     10│      2│   8444│    0.20│   95.85│    4.21│49900.13│  10,362│   1,036│200:2;429:8441;500:3
  11s│     10│      2│   9295│    0.18│   95.85│    4.21│54893.06│  10,362│     941│200:2;429:9292;500:3
  12s│     10│      2│  10083│    0.17│   95.85│    4.21│59903.03│  10,362│     863│200:2;429:10080;500:3
  13s│     10│      2│  10889│    0.15│   95.85│    4.21│64892.42│  10,362│     797│200:2;429:10886;500:3
  14s│     10│      2│  11383│    0.14│   95.85│    4.21│69829.26│  10,362│     740│200:2;429:11380;500:3
```



### 3.3.6header_value_match

```
{
  "descriptor_value": "...",
  "expect_match": "{...}",
  "headers": []
}
```

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
      - key: header_match
        value: "test"
        rate_limit:
          unit: minute
          requests_per_unit: 3
      - key: header_match
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
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: productpage
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
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

kubectl apply -f envoyfilter-filter.yaml -n istio
```



4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_INBOUND
        routeConfiguration:
          vhost:
            name: "inbound|http|9080"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: 
              - header_value_match:
                  descriptor_value: test
                  expect_match: true
                  headers:
                  - name: test
                    exact_match: test
EOF

kubectl apply -f envoyfilter-action.yaml  -n istio
```



```
[root@node01 ~]#  go-stress-testing -n 1000000 -c 10 -u http://192.168.229.128:30563/productpage  -H "test:test"

 开始启动  并发数:10 请求数:1000000 请求参数: 
request:
 form:http 
 url:http://192.168.229.135:32688/productpage 
 method:GET 
 headers:map[test:test] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│      0│    551│    0.00│   46.14│    4.79│    0.00│        │        │429:551
   2s│     10│      0│   1058│    0.00│   46.14│    4.79│    0.00│        │        │429:1058
   3s│     10│      3│   1505│    1.01│   93.00│    4.79│ 9918.12│  14,545│   4,847│200:3;429:1505
   4s│     10│      3│   1996│    0.75│   93.00│    4.79│13286.69│  14,545│   3,635│200:3;429:1996
   5s│     10│      3│   2491│    0.60│   93.00│    4.79│16616.88│  14,545│   2,908│200:3;429:2491
   6s│     10│      3│   2962│    0.50│   93.00│    4.79│19942.64│  14,545│   2,423│200:3;429:2962
   7s│     10│      3│   3481│    0.43│   93.00│    4.79│23293.64│  14,545│   2,077│200:3;429:3481
```



### 3.3.7dynamic_metadata

已废弃

### 3.3.8metadata

```
{
  "descriptor_key": "...",
  "metadata_key": "{...}",
  "default_value": "...",
  "source": "..."
}
```

source：

- DYNAMIC

  *(DEFAULT)* ⁣Query [dynamic metadata](https://www.envoyproxy.io/docs/envoy/latest/configuration/advanced/well_known_dynamic_metadata#well-known-dynamic-metadata)

- ROUTE_ENTRY

  ⁣Query [route entry metadata](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/route/v3/route_components.proto.html?highlight=rate_limits#envoy-v3-api-field-config-route-v3-route-metadata)

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
        value: "test"
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
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: productpage
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
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

kubectl apply -f envoyfilter-filter.yaml -n istio
```



4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_INBOUND
        routeConfiguration:
          vhost:
            name: "inbound|http|9080"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: 
              - metadata:
                  descriptor_key: test
                  metadata_key:
                    key: envoy.xxx
                    path:
                    - key: prop
                    - key: foo
                  default_value: test
EOF

kubectl apply -f envoyfilter-action.yaml  -n istio
```



```
[root@node01 ~]# go-stress-testing -n 1000000 -c 10 -u http://192.168.229.135:32688/productpage

 开始启动  并发数:10 请求数:1000000 请求参数: 
request:
 form:http 
 url:http://192.168.229.135:32688/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│      0│    852│    0.00│   26.01│    4.44│    0.00│        │        │429:852
   2s│     10│      0│   1766│    0.00│   26.04│    4.44│    0.00│        │        │429:1766
   3s│     10│      4│   2634│    1.34│   88.00│    4.44│ 7470.19│  18,732│   6,243│200:4;429:2634
   4s│     10│      4│   3507│    1.00│   88.00│    4.44│ 9973.41│  18,732│   4,682│200:4;429:3507
   5s│     10│      4│   4005│    0.80│   88.00│    4.44│12454.99│  18,732│   3,745│200:4;429:4005
   6s│     10│      4│   4496│    0.67│   88.00│    4.44│14932.15│  18,732│   3,121│200:4;429:4496
```



### 3.3.9extension

extensions.rate_limit_descriptors.expr.v3.Descriptor

```
{
  "descriptor_key": "...",
  "skip_if_error": "...",
  "text": "...",
  "parsed": "{...}"
}
```

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
        value: "outbound_.9080_._.productpage.istio.svc.cluster.local"
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
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: productpage
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
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

kubectl apply -f envoyfilter-filter.yaml -n istio
```



4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_INBOUND
        routeConfiguration:
          vhost:
            name: "inbound|http|9080"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: 
              - extension:
                  name: envoy.rate_limit_descriptors.expr
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.rate_limit_descriptors.expr.v3.Descriptor
                    descriptor_key: test
                    skip_if_error: true
                    text: "connection.requested_server_name"
EOF

kubectl apply -f envoyfilter-action.yaml  -n istio
```



```
[root@node01 ~]# go-stress-testing -n 1000000 -c 10 -u http://192.168.229.135:32688/productpage

 开始启动  并发数:10 请求数:1000000 请求参数: 
request:
 form:http 
 url:http://192.168.229.135:32688/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│      0│    850│    0.00│   28.69│    3.87│    0.00│        │        │429:850
   2s│     10│      0│   1688│    0.00│   32.26│    3.87│    0.00│        │        │429:1688
   3s│     10│      0│   2593│    0.00│   32.26│    3.87│    0.00│        │        │429:2593
   4s│     10│      4│   3443│    1.00│   97.18│    3.87│ 9975.97│  19,724│   4,930│200:4;429:3440;500:3
   5s│     10│      4│   4005│    0.80│   97.18│    3.87│12431.83│  19,724│   3,944│200:4;429:4002;500:3
   6s│     10│      4│   4464│    0.67│   97.18│    3.87│14930.66│  19,724│   3,287│200:4;429:4461;500:3
   7s│     10│      4│   4962│    0.57│   97.18│    3.87│17428.69│  19,724│   2,817│200:4;429:4959;500:3
   8s│     10│      4│   5477│    0.50│   97.18│    3.87│19941.18│  19,724│   2,464│200:4;429:5474;500:3
```



## 3.4按请求类型限流

### 3.4.1internal

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
      - key: header_match
        value: "test"
        rate_limit:
          unit: minute
          requests_per_unit: 3
      - key: header_match
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
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: productpage
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
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
            request_type: internal
            timeout: 20ms
            rate_limited_as_resource_exhausted: true
            enable_x_ratelimit_headers: DRAFT_VERSION_03
            disable_x_envoy_ratelimited_header: false
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

kubectl apply -f envoyfilter-filter.yaml -n istio
```



4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_INBOUND
        routeConfiguration:
          vhost:
            name: "inbound|http|9080"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: 
              - header_value_match:
                  descriptor_value: test
                  expect_match: true
                  headers:
                  - name: test
                    exact_match: test
EOF

kubectl apply -f envoyfilter-action.yaml  -n istio
```



```
[root@node01 ~]# go-stress-testing -n 1000000 -c 10 -u http://192.168.229.135:32688/productpage -H "test:test"

 开始启动  并发数:10 请求数:1000000 请求参数: 
request:
 form:http 
 url:http://192.168.229.135:32688/productpage 
 method:GET 
 headers:map[test:test] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│      0│    402│    0.00│   58.08│    8.42│    0.00│        │        │429:402
   2s│     10│      0│    838│    0.00│   58.21│    7.16│    0.00│        │        │429:838
   3s│     10│      0│   1345│    0.00│   58.21│    7.16│    0.00│        │        │429:1345
   4s│     10│      0│   1813│    0.00│   58.21│    6.42│    0.00│        │        │429:1813
   5s│     10│      3│   2284│    0.60│   94.15│    6.42│16583.06│  14,545│   2,908│200:3;429:2284
   6s│     10│      3│   2750│    0.50│   94.15│    6.42│19914.90│  14,545│   2,424│200:3;429:2750
   7s│     10│      3│   3189│    0.43│   94.15│    6.42│23246.60│  14,545│   2,077│200:3;429:3189
   8s│     10│      3│   3616│    0.38│   94.15│    6.42│26548.82│  14,545│   1,817│200:3;429:3616
```



### 3.4.2external 

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
      - key: header_match
        value: "test"
        rate_limit:
          unit: minute
          requests_per_unit: 3
      - key: header_match
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
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: productpage
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
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
            request_type: external
            timeout: 20ms
            rate_limited_as_resource_exhausted: true
            enable_x_ratelimit_headers: DRAFT_VERSION_03
            disable_x_envoy_ratelimited_header: false
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

kubectl apply -f envoyfilter-filter.yaml -n istio
```



4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_INBOUND
        routeConfiguration:
          vhost:
            name: "inbound|http|9080"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: 
              - header_value_match:
                  descriptor_value: test
                  expect_match: true
                  headers:
                  - name: test
                    exact_match: test
EOF

kubectl apply -f envoyfilter-action.yaml  -n istio
```



```
[root@node01 ~]# go-stress-testing -n 1000000 -c 10 -u http://192.168.229.135:32688/productpage -H "test:test"

 开始启动  并发数:10 请求数:1000000 请求参数: 
request:
 form:http 
 url:http://192.168.229.135:32688/productpage 
 method:GET 
 headers:map[test:test] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│     94│      0│   98.74│  150.75│   32.92│  101.27│ 455,078│ 454,974│200:94
   2s│     10│    191│      0│   97.52│  189.63│   32.92│  102.55│ 925,701│ 462,829│200:191
   3s│     10│    288│      0│   98.58│  189.63│   32.92│  101.44│1,396,320│ 465,432│200:288
   4s│     10│    390│      0│   99.04│  189.63│   32.92│  100.97│1,890,850│ 472,669│200:390
```

因为请求是内部的，所以这里没有ratelimit

### 3.4.3both

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
      - key: header_match
        value: "test"
        rate_limit:
          unit: minute
          requests_per_unit: 3
      - key: header_match
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
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: productpage
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
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
            request_type: both
            timeout: 20ms
            rate_limited_as_resource_exhausted: true
            enable_x_ratelimit_headers: DRAFT_VERSION_03
            disable_x_envoy_ratelimited_header: false
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

kubectl apply -f envoyfilter-filter.yaml -n istio
```



4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_INBOUND
        routeConfiguration:
          vhost:
            name: "inbound|http|9080"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: 
              - header_value_match:
                  descriptor_value: test
                  expect_match: true
                  headers:
                  - name: test
                    exact_match: test
EOF

kubectl apply -f envoyfilter-action.yaml  -n istio
```



```
[root@node01 ~]# go-stress-testing -n 1000000 -c 10 -u http://192.168.229.135:32688/productpage -H "test:test"

 开始启动  并发数:10 请求数:1000000 请求参数: 
request:
 form:http 
 url:http://192.168.229.135:32688/productpage 
 method:GET 
 headers:map[test:test] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│      0│    832│    0.00│   30.45│    4.18│    0.00│        │        │429:832
   2s│     10│      0│   1621│    0.00│   30.45│    4.18│    0.00│        │        │429:1621
   3s│     10│      0│   2398│    0.00│   30.45│    4.18│    0.00│        │        │429:2398
   4s│     10│      0│   3188│    0.00│   30.45│    3.86│    0.00│        │        │429:3179;500:9
   5s│     10│      0│   4087│    0.00│   30.45│    3.86│    0.00│        │        │429:4078;500:9
   6s│     10│      3│   4961│    0.50│   87.15│    3.86│19948.44│  14,545│   2,424│200:3;429:4952;500:9
   7s│     10│      3│   5843│    0.43│   87.15│    3.86│23253.71│  14,545│   2,077│200:3;429:5834;500:9
   8s│     10│      3│   6673│    0.38│   87.15│    3.86│26606.37│  14,545│   1,818│200:3;429:6664;500:9
```



## 3.5stage

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
      - key: remote_address
        value: "172.20.0.0"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: remote_address
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
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: productpage
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
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
            stage: 1
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

kubectl apply -f envoyfilter-filter.yaml -n istio
```



4创建action envoyfilter

```
cat << EOF > envoyfilter-action.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_INBOUND
        routeConfiguration:
          vhost:
            name: "inbound|http|9080"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: 
              - remote_address: {}
              stage: 1

EOF

kubectl apply -f envoyfilter-action.yaml  -n istio
```



```
[root@node01 ~]# go-stress-testing -n 1000000 -c 10 -u http://192.168.229.135:32688/productpage

 开始启动  并发数:10 请求数:1000000 请求参数: 
request:
 form:http 
 url:http://192.168.229.135:32688/productpage 
 method:GET 
 headers:map[] 
 data: 
 verify:statusCode 
 timeout:30s 
 debug:false 



─────┬───────┬───────┬───────┬────────┬────────┬────────┬────────┬────────┬────────┬────────
 耗时│ 并发数│ 成功数│ 失败数│   qps  │最长耗时│最短耗时│平均耗时│下载字节│字节每秒│ 错误码
─────┼───────┼───────┼───────┼────────┼────────┼────────┼────────┼────────┼────────┼────────
   1s│     10│      1│    835│    1.01│   44.86│    5.45│ 9941.10│   4,183│   4,182│200:1;429:815;500:20
   2s│     10│      1│   1711│    0.50│   44.86│    4.96│19839.11│   4,183│   2,091│200:1;429:1688;500:23
   3s│     10│      1│   2465│    0.33│   44.86│    4.96│29876.69│   4,183│   1,393│200:1;429:2442;500:23
   4s│     10│      1│   3007│    0.25│   66.38│    4.96│39852.79│   4,183│   1,045│200:1;429:2984;500:23
   5s│     10│      1│   3852│    0.20│   66.38│    4.55│49888.50│   4,183│     836│200:1;429:3829;500:23
   6s│     10│      1│   4733│    0.17│   66.38│    4.55│59809.06│   4,183│     696│200:1;429:4710;500:23
   7s│     10│      1│   5581│    0.14│   66.38│    4.55│69770.80│   4,183│     597│200:1;429:5558;500:23
   8s│     10│      1│   6497│    0.13│   66.38│    4.55│79826.00│   4,183│     522│200:1;429:6474;500:23
   9s│     10│      1│   7407│    0.11│   66.38│    4.55│89840.05│   4,183│     464│200:1;429:7384;500:23
  10s│     10│      1│   8271│    0.10│   66.38│    4.55│99801.86│   4,183│     418│200:1;429:8248;500:23
```



## 3.6sentinel

deploy-sentinel.yaml

kubectl apply -f deploy-sentinel.yaml -n istio

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: sentinel-rule-cm
data:
  rule-yaml: |-
    domain: productpage-ratelimit
    descriptors:
      - key: destination_cluster
        value: "inbound|9080||"
        rate_limit:
          unit: minute
          requests_per_unit: 1
      - key: destination_cluster
        rate_limit:
          unit: minute
          requests_per_unit: 10
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: sentinel-rls-server
  labels:
    app: sentinel
spec:
  replicas: 1
  selector:
    matchLabels:
      app: sentinel
  template:
    metadata:
      labels:
        app: sentinel
    spec:
      containers:
        - name: sentinelserver
          # You could replace the image with your own image here
          image: "registry.cn-hangzhou.aliyuncs.com/sentinel-docker-repo/sentinel-envoy-rls-server:latest"
          imagePullPolicy: Always
          ports:
            - containerPort: 10245
            - containerPort: 8719
          volumeMounts:
            - name: sentinel-rule-config
              mountPath: /tmp/sentinel
          env:
            - name: SENTINEL_RLS_RULE_FILE_PATH
              value: "/tmp/sentinel/rule.yaml"
      volumes:
        - name: sentinel-rule-config
          configMap:
            name: sentinel-rule-cm
            items:
              - key: rule-yaml
                path: rule.yaml
---
apiVersion: v1
kind: Service
metadata:
  name: sentinel-rls-service
  labels:
    name: sentinel-rls-service
spec:
  type: ClusterIP
  ports:
    - port: 8719
      targetPort: 8719
      name: sentinel-command
    - port: 10245
      targetPort: 10245
      name: sentinel-grpc
  selector:
    app: sentinel
```



```
cat << EOF > envoyfilter-filter-sentinel.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit
  namespace: istio
spec:
  workloadSelector:
    # select by label in the same namespace
    labels:
      app: productpage
  configPatches:
    # The Envoy config you want to modify
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
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
            stage: 0
            request_type: both
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
                       address: sentinel-rls-service.istio.svc.cluster.local
                       port_value: 10245
EOF

kubectl apply -f envoyfilter-filter-sentinel.yaml -n istio
```



```
cat << EOF > envoyfilter-action-sentinel.yaml 
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: filter-ratelimit-svc
  namespace: istio
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: VIRTUAL_HOST
      match:
        context: SIDECAR_INBOUND
        routeConfiguration:
          vhost:
            name: "inbound|http|9080"
            route:
              action: ANY
      patch:
        operation: MERGE
        # Applies the rate limit rules.
        value:
          rate_limits:
            - actions: 
              - destination_cluster: {}
              stage: 0
EOF

kubectl apply -f envoyfilter-action-sentinel.yaml  -n istio
```



# 4清理

```
kubectl delete cm ratelimit-config -n istio
kubectl delete -f ratelimit-deploy.yaml -n istio
kubectl delete envoyfilter filter-ratelimit -n istio
kubectl delete envoyfilter filter-ratelimit-svc -n istio
```

