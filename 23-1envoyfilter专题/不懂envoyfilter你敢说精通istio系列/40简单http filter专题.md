# 1什么是http filter

http filter是envoy用来支持http协议的过滤器，他们是配置在HtttpConnectionManager这个网络过滤器里面的。其中route过滤器必须有，而且必须是http过滤器链中最后一个，用来选择路由的。这一小节的过滤器配置都比较简单，所以放在一节中统一介绍。http过滤器可以处理http1.1,http2,https协议。

# 2本节要讲的http filter列表

 alternate_protocols_cache 

 cdn_loop 

 dynamo 

 health_check 

 ip_tagging 

 set_metadata 

 squash 

 sxg 

# 3配置

## 3.1alternate_protocols_cache 

```
{
  "alternate_protocols_cache_options": "{...}"缓存配置选项
}
```

alternate_protocols_cache_options：

```
{
  "name": "...",缓存名称
  "max_entries": "{...}",最大缓存个数，默认1024
  "key_value_store_config": "{...}"key，value存储配置
}
```

key_value_store_config：

```
{
  "name": "...",名称
  "typed_config": "{...}"配置
}
```

## 3.2cdn_loop

```
{
  "cdn_id": "...",cdn的id
  "max_allowed_occurrences": "..."最大允许的cdn id个数
}
```

##  3.3dynamo 

没有配置

## 3.4health_check 

```
{
  "pass_through_mode": "{...}",是否使用pass through模式
  "cache_time": "{...}",如果是pass through模式上游响应缓存时间
  "cluster_min_healthy_percentages": "{...}",如果是非pass through模式，上游集群健康百分比
  "headers": []匹配头
}
```

## 3.5ip_tagging 

```
{
  "request_type": "...",请求类型
  "ip_tags": []tag集合
}
```

request_type：

- BOTH

  *(DEFAULT)* ⁣Both external and internal requests will be tagged. This is the default value.

- INTERNAL

  ⁣Only internal requests will be tagged.

- EXTERNAL

  ⁣Only external requests will be tagged.

ip_tags：

```
{
  "ip_tag_name": "...",ip 标签名称
  "ip_list": []会被标记的ip列表
}
```

## 3.6set_metadata 

```
{
  "metadata_namespace": "...",元数据名称空间
  "value": "{...}"元数据值
}
```

## 3.7squash 

```
{
  "cluster": "...",squash服务器的cluster
  "attachment_template": "{...}",创建DebugAttachment时的资源模板
  "request_timeout": "{...}",请求超时时间，默认1s
  "attachment_timeout": "{...}",attachment超时时间，默认60秒
  "attachment_poll_period": "{...}"检查attach状态周期，默认1秒
}
```

有关介绍参考 https://blog.csdn.net/kunyus/article/details/88616443

## 3.8sxg 

```
{
  "certificate": "{...}",用于签名的证书
  "private_key": "{...}",用于签名的私钥
  "duration": "{...}",sxg包有效期，默认 604800s （7天）
  "mi_record_size": "...",Merkle Integrity记录大小，默认4096
  "cbor_url": "...",CBOR 文件的url，path格式
  "validity_url": "...",抽取验证信息的url
  "client_can_accept_sxg_header": "...",客户端可以接受sxg的头，默认x-client-can-accept-sxg
  "should_encode_sxg_header": "...",响应应该转换为sxg的头，默认x-should-encode-sxg
  "header_prefix_filters": []抽离sxg文档头的前缀
}
```

 sxg全称signed exchange (SXG)  

参考 https://web.dev/signed-exchanges/

# 4实战

## 4.1alternate_protocols_cache 

缓存和解析 Alt-Svc  http 头的，用于http3

httpsimple/ef-alternate_protocols_cache.yaml

kubectl apply -f ef-alternate_protocols_cache.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple 
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
      operation: INSERT_FIRST
      value: 
        name: envoy.filters.http.alternate_protocols_cache
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.alternate_protocols_cache.v3.FilterConfig
          alternate_protocols_cache_options: 
            name: protocols_cache
            max_entries: 1024
            #key_value_store_config:
            
```

key_value_store_config配置文档没有说，有哪些类型

## 4.2cdn_loop 

ef-cdn_loop.yaml

kubectl apply -f ef-cdn_loop.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple 
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
      operation: INSERT_FIRST
      value: 
        name: envoy.filters.http.cdn_loop
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.cdn_loop.v3.CdnLoopConfig
          cdn_id: myid
          max_allowed_occurrences: 0
```

由于我没有cdn，这个无法验证

## 4.3dynamo 

Envoy 支持一个 HTTP 级 DynamoDB 嗅探过滤器，该过滤器有以下特性：

- DynamoDB API 请求/响应解析。
- DynamoDB 按每操作、每数据库表、每分区以及操作的统计信息。
- 故障类型统计覆盖 4xx 响应、由响应 JSON 解析，例如 ProvisionedThroughputExceededException。
- 批处理操作部分失败的统计信息。

部署dynamodb

```
helm repo add keyporttech https://keyporttech.github.io/helm-charts/
helm install dynamo keyporttech/dynamodb
```

ef-dynamo.yaml

kubectl apply -f ef-dynamo.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple 
spec:
  workloadSelector:
    labels:
      app.kubernetes.io/instance: dynamo
      app.kubernetes.io/name: dynamodb
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        filterChain:
          destinationPort: 8000
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_BEFORE
      value:
        name: envoy.filters.http.dynamo
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.dynamo.v3.Dynamo
```

## 4.4health_check 

ef-health_check.yaml

kubectl apply -f ef-health_check.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple 
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
        name: envoy.filters.http.health_check
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.health_check.v3.HealthCheck
          pass_through_mode: true
          cache_time: 10ms
          cluster_min_healthy_percentages:
            outbound|9080||productpage.istio.svc.cluster.local:
              value: 100
          headers:
          - name: test
            present_match: false
```

## 4.5ip_tagging 

 The implementation for IP Tagging provides a scalable way to compare an IP address to a large list of CIDR ranges efficiently. 

ef-ip_tagging.yaml

kubectl apply -f ef-ip_tagging.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple 
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
        name: envoy.filters.http.ip_tagging
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.ip_tagging.v3.IPTagging
          request_type: BOTH
          ip_tags:
          - ip_tag_name: test
            ip_list:
            - address_prefix: 0.0.0.0
              prefix_len: 0
```

具体使用案例，目前还不知道

## 4.6set_metadata 

用来设置元数据

ef-set_metadata.yaml

kubectl apply -f ef-set_metadata.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: simple 
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
        name: envoy.filters.http.set_metadata
        typed_config:
          "@type": type.googleapis.com/envoy.extensions.filters.http.set_metadata.v3.Config
          metadata_namespace: envoy.lb
          value:
            version: v1
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



## 4.7squash

部署：

squash-server.yaml

kubectl apply -f squash-server.yaml -n istio

```
apiVersion: v1
kind: ServiceAccount
metadata:
  name: squash-server
---
kind: Role
apiVersion: rbac.authorization.k8s.io/v1beta1
metadata:
  name: squash-pod-reader
rules:
- apiGroups: [""] # "" indicates the core API group
  resources: ["pods"]
  verbs: ["get", "watch", "list"]
---
kind: RoleBinding
apiVersion: rbac.authorization.k8s.io/v1beta1
metadata:
  name: squash-read-pod-info
subjects:
- kind: ServiceAccount
  name: squash-server
#  apiGroup: rbac.authorization.k8s.io # some reason this fails on kube 1.7.4
roleRef:
  kind: Role
  name: squash-pod-reader
  apiGroup: rbac.authorization.k8s.io
---
apiVersion: apps/v1beta1
kind: Deployment
metadata:
  name: squash-server
  labels:
    app: squash-server
spec:
  replicas: 1
  selector:
    matchLabels:
      app: squash-server
  template:
    metadata:
      labels:
        app: squash-server
    spec:
      serviceAccountName: squash-server
      containers:
      - name: squash-server
        image: soloio/squash-server:v0.2.0-46-gf2c3713
        ports:
        - containerPort: 8080
          protocol: TCP
          name: http-squash-api
---
kind: Service
apiVersion: v1
metadata:
  name: squash-server
spec:
  selector:
    app: squash-server
  ports:
    - name: http-squash-api
      protocol: TCP
      port: 80
      targetPort: 8080
```



squash-client.yaml

kubectl apply -f squash-client.yaml -n istio

```
apiVersion: extensions/v1beta1
kind: DaemonSet
metadata:
  labels:
    app: squash-client
  name: squash-client
spec:
  template:
    metadata:
      labels:
        app: squash-client
    spec:
      hostPID: true
      containers:
      - name: squash-client
        image: soloio/squash-client:v0.2.0-46-gf2c3713
        volumeMounts:
        - mountPath: /var/run/cri.sock
          name: crisock
        securityContext:
          privileged: true
        ports:
        - containerPort: 1234
          protocol: TCP
        env:
        - name: SERVERURL
          value: "http://$(SQUASH_SERVER_SERVICE_HOST):$(SQUASH_SERVER_SERVICE_PORT)"
        - name: POD_NAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: POD_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: HOST_ADDR
          value: "$(POD_NAME).$(POD_NAMESPACE)"
        - name: NODE_NAME
          valueFrom:
            fieldRef:
              fieldPath: spec.nodeName
      volumes:
      - name: crisock
        hostPath:
          path: /var/run/dockershim.sock
```

2019年开始就不更新了，这个不讲了

## 4.8sxg

https://blog.csdn.net/google_SEO_yang/article/details/122398223

 The SXG filter is experimental and is currently under active development. 

还在开发中，这个不讲了，也不知道干嘛用的。示例配置如下

```
cbor_url: "/.sxg/cert.cbor"
validity_url: "/.sxg/validity.msg"
certificate:
  name: certificate
  sds_config:
    path: "/etc/envoy/sxg-certificate.yaml"
private_key:
  name: private_key
  sds_config:
    path: "/etc/envoy/sxg-private-key.yaml"
duration: 432000s
mi_record_size: 1024
client_can_accept_sxg_header: "x-custom-accept-sxg"
should_encode_sxg_header: "x-custom-should-encode"
header_prefix_filters:
  - "x-foo-"
  - "x-bar-"
```

