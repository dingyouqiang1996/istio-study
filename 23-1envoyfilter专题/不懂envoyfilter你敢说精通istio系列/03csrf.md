# 什么实csrf filter

 CSRF（Cross-site request forgery），中文名称：跨站请求伪造，也被称为：one click attack/session riding，缩写为：CSRF/XSRF。 

csrf tilter是一个http类型的filter，envoy中专门用来防止csrf攻击的过滤器。过滤器名称是 envoy.filters.http.**csrf** 。type 路径是envoy.extensions.filters.http.**csrf**.v3.CsrfPolicy.它的配置是设置在虚拟主机或路由上的。csrf没有相应crd配置，必须通过envoyfilter配置。

csrf不做过多介绍，大家可以百度。



# 实战

ef-cors.yaml

kubectl apply ef-cors.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: apply-to
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
            name: local_route
            virtual_hosts:
            - name: www
              domains:
              - "*"
              cors:
                allow_origin_string_match:
                - safe_regex:
                    google_re2: {}
                    regex: .*
                filter_enabled:
                  default_value:
                    numerator: 100
                    denominator: HUNDRED
              typed_per_filter_config:
                envoy.filters.http.csrf:
                  "@type": type.googleapis.com/envoy.extensions.filters.http.csrf.v3.CsrfPolicy
                  filter_enabled:
                    default_value:
                      numerator: 100
                      denominator: HUNDRED
                    runtime_key: csrf.www.enabled
                  shadow_enabled:
                    default_value:
                      numerator: 0
                      denominator: HUNDRED
                    runtime_key: csrf.www.shadow_enabled
              routes:
              - match:
                  prefix: "/csrf/disabled"
                route:
                  cluster: generic_service
                typed_per_filter_config:
                  envoy.filters.http.csrf:
                    "@type": type.googleapis.com/envoy.extensions.filters.http.csrf.v3.CsrfPolicy
                    filter_enabled:
                      default_value:
                        numerator: 0
                        denominator: HUNDRED
              - match:
                  prefix: "/csrf/shadow"
                route:
                  cluster: generic_service
                typed_per_filter_config:
                  envoy.filters.http.csrf:
                    "@type": type.googleapis.com/envoy.extensions.filters.http.csrf.v3.CsrfPolicy
                    filter_enabled:
                      default_value:
                        numerator: 0
                        denominator: HUNDRED
                    shadow_enabled:
                      default_value:
                        numerator: 100
                        denominator: HUNDRED
              - match:
                  prefix: "/csrf/additional_origin"
                route:
                  cluster: generic_service
                typed_per_filter_config:
                  envoy.filters.http.csrf:
                    "@type": type.googleapis.com/envoy.extensions.filters.http.csrf.v3.CsrfPolicy
                    filter_enabled:
                      default_value:
                        numerator: 100
                        denominator: HUNDRED
                    additional_origins:
                    - safe_regex:
                        google_re2: {}
                        regex: .*
              - match:
                  prefix: "/"
                route:
                  cluster: generic_service
  - applyTo: HTTP_FILTER
    match:
        listener:
          #name: 0.0.0.0_8080  
          portNumber: 8080
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
    patch:
        operation: INSERT_BEFORE
        value:
          name: envoy.filters.http.cors
          typed_config:                  
            "@type": type.googleapis.com/envoy.extensions.filters.http.cors.v3.Cors
  - applyTo: HTTP_FILTER
    match:
        listener:
          #name: 0.0.0.0_8080  
          portNumber: 8080
          filterChain:
            filter:
              name: envoy.filters.network.http_connection_manager
              subFilter:
                name: envoy.filters.http.router
    patch:
        operation: INSERT_BEFORE
        value:           
          name: envoy.filters.http.csrf
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.csrf.v3.CsrfPolicy
            filter_enabled:
              default_value:
                numerator: 100
                denominator: HUNDRED 
  - applyTo: CLUSTER
    match:
      context: ANY
      cluster: {} 
    patch:
      operation: ADD
      value:
        name: generic_service
        connect_timeout: 0.25s
        type: STRICT_DNS
        lb_policy: ROUND_ROBIN
        load_assignment:
          cluster_name: generic_service
          endpoints:
          - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: csrf.istio.svc.cluster.local
                    port_value: 8080
```

说明配置csrf时要配置cors，指示哪些源是安全的。

我的这个配置根据 [envoy/examples/csrf at main · envoyproxy/envoy (github.com)](https://github.com/envoyproxy/envoy/tree/main/examples/csrf) 这个案例修改而来。

cors配置不再介绍，前一篇文章已经有介绍。

virtual_hosts的typed_per_filter_config是对整个virtual_hosts配置过滤器，这里为啥用typed_per_filter_config，是因为本来filter是配置在httpConnectionManager里面的，这里有了这个就表示过滤器对virtual host生效。filter_enabled表示规则生效，shadow_enabled规则不会生效，但会记录。default_value下面配置分子分母，是个百分比。additional_origins是额外的安全的源。根据上面的配置，默认生效，/csrf/disabled不生效，/csrf/shadow只记录不生效，/csrf/additional_origin生效，并且信任额外的源。同时我们在route前插入了一个csrf过滤器，默认生效。cluster配置指向了一个我们部署的demo应用。

csrf.istio.svc.cluster.local是我修改后的部署。yaml如下：

deploy-csrf.yaml

kubectl apply -f deploy-csrf.yaml -n istio

```
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: csrf
  labels:
    app: csrf
spec:
  selector:
    matchLabels:
      app: csrf
  replicas: 1
  template:
    metadata:
      labels:
        app: csrf
        version: v1
    spec:
      containers:
        - name: csrf
          image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/csrf-satesite:latest
          imagePullPolicy: Always
          ports:
            - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: csrf
spec:
  selector:
    app: csrf
  ports:
    - name: tcp-csrf
      protocol: TCP
      port: 8080
      targetPort: 8080
---
```

