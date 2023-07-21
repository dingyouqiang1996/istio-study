# 1什么实route filter

route filter是http connection manager的最后一个http filter，它必须出现在最后不然会报错。起到路由的作用。在所有的http连接场景中都会用到。它的工作原理是利用路由表进行路由，路由表包括静态路由和动态路由。它同时处理重试和stats逻辑。

# 2配置

```
{
  "dynamic_stats": "{...}",是否产生动态统计信息，默认true，高性能场景可关闭
  "start_child_span": "...",是否启用子span 出口路由
  "upstream_log": [],配置上游日志
  "suppress_envoy_headers": "...",不添加额外的x-envoy- headers 到request和response
  "strict_check_headers": [],设置一个列表，严格检查头信息
  "respect_expected_rq_timeout": "...",是否忽略x-envoy-expected-rq-timeout-ms头
  "suppress_grpc_request_failure_code_stats": "..."是否避免产生grpc请求失败状态码stats
}
```

upstream_log：

```
{
  "name": "...",名称
  "filter": "{...}",过滤
  "typed_config": "{...}" 根据日志扩展设置自定义配置
}
```

name：

```
envoy.access_loggers.file  文件日志

envoy.access_loggers.http_grpc http_grpc日志

envoy.access_loggers.open_telemetry telemetry日志

envoy.access_loggers.stream 流日志

envoy.access_loggers.tcp_grpc tcp grpc日志

envoy.access_loggers.wasm  wasm日志
```

filter:

```
{
  "status_code_filter": "{...}", 状态码过滤
  "duration_filter": "{...}",  时间过滤
  "not_health_check_filter": "{...}", 没有健康检查过滤
  "traceable_filter": "{...}", 可跟踪过滤
  "runtime_filter": "{...}", 运行时过滤
  "and_filter": "{...}", 与过滤
  "or_filter": "{...}", 或过滤
  "header_filter": "{...}", 头过滤
  "response_flag_filter": "{...}",响应标识过滤
  "grpc_status_filter": "{...}",grpc状态过滤
  "extension_filter": "{...}",扩展过滤
  "metadata_filter": "{...}"元数据过滤
}
```

# 3实战

没有crd配置方法

## 3.1关闭动态统计信息

```
cat << EOF > ef-route-dynamic_stats.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  namespace: istio-system
  name: route
spec:
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: ANY
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: REPLACE
        value:
          name: envoy.filters.http.router
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
            dynamic_stats: false
EOF
kubectl apply -f ef-route-dynamic_stats.yaml -n istio-system --context context-cluster1
```

![1](09image\1.jpg)

## 3.2启用子span 出口路由

```
cat << EOF > ef-route-start_child_span.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  namespace: istio-system
  name: route
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: REPLACE
        value:
          name: envoy.filters.http.router
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
            start_child_span: true
EOF

kubectl apply -f ef-route-start_child_span.yaml -n istio-system --context context-cluster1
```

![2](09image\2.jpg)

## 3.3配置日志

### 3.3.1文件日志

```
cat << EOF > ef-route-file-accesslog.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  namespace: istio
  name: route
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: SIDECAR_INBOUND
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: REPLACE
        value:
          name: envoy.filters.http.router
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
            upstream_log:
            - name: envoy.access_loggers.file
              filter:
                status_code_filter:
                  comparison: 
                    op: GE
                    value: 
                      default_value: 200
                      runtime_key: log.enforce
              typed_config:
                "@type": type.googleapis.com/envoy.extensions.access_loggers.file.v3.FileAccessLog
                path: /var/log/route/route.log
                log_format:
                  text_format: "%LOCAL_REPLY_BODY%:%RESPONSE_CODE%:path=%REQ(:path)%\n"
                  content_type: text/plain
                  omit_empty_values: true            
EOF

kubectl apply -f ef-route-file-accesslog.yaml -n istio --context context-cluster1
```

productpage deployment添加annotations

```
cat << EOF >productpage-deploy.yaml 
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
      annotations:
        sidecar.istio.io/userVolumeMount: '[{"name":"log", "mountPath":"/var/log/route"}]'
        sidecar.istio.io/userVolume: '[{"name": "log", "emptyDir":{}}]'
      labels:
        app: productpage
        version: v1
    spec:
      serviceAccountName: bookinfo-productpage
      containers:
      - name: productpage
        image: docker.io/istio/examples-bookinfo-productpage-v1:1.16.2
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
EOF

kubectl apply -f productpage-deploy.yaml  -n istio
```

![3](09image\3.jpg)

## 3.4其他配置

```
cat << EOF > ef-route-other.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  namespace: istio-system
  name: route
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
    - applyTo: HTTP_FILTER
      match:
        context: GATEWAY
        listener:
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
      patch:
        operation: REPLACE
        value:
          name: envoy.filters.http.router
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.router.v3.Router
            suppress_envoy_headers: true
            strict_check_headers:
            - x-envoy-max-retries
            - x-envoy-upstream-rq-timeout-ms
            - x-envoy-upstream-rq-per-try-timeout-ms
            - x-envoy-retry-grpc-on
            - x-envoy-retry-on
            respect_expected_rq_timeout: true
            suppress_grpc_request_failure_code_stats: true
EOF

kubectl apply -f ef-route-other.yaml -n istio-system --context context-cluster1
```

![4](09image\4.jpg)

