# 1什么是cors filter

cors是 Cross-origin resource sharing ，即跨域资源共享，是用来不同域名之间共享资源的一种机制。cors filter是envoy的一个http filter，name为 envoy.filters.http.**cors** ，type url是固定的envoy.extensions.filters.http.**cors**.v3.Cors .这里对cors的实现机制不做介绍，大家可以百度。



# 2配置说明

```
allow_origin_string_match:   允许共享的客户端域名
allow_methods: "GET,OPTIONS"  允许请求的方法
allow_headers: "content-type"  请求允许的头
allow_credentials: true        是否允许cookie
exposeHeaders:   暴露给调用者的response 头
- test
- test2
max_age: "60"    共享生效时间，在这个时间内不用发送option请求，可以直接掉
filter_enabled:  是否生效
  default_value:
    numerator: 0   生效百分比
    denominator: HUNDRED  单位
shadow_enabled:  记录日志是否生效
  default_value:
    numerator: 100  生效百分比
    denominator: HUNDRED   单位
```

```
[root@node01 ~]# cat /var/www/html/index.html
<html>
<head><title></title></head>
<body>
<script type="text/javascript" src="https://code.jquery.com/jquery-3.2.1.min.js"></script>  
<script>
$(function(){
        $("#cors").click(
                function(){
                        $.ajax({
                                type:"get",
                                dataType : "html",
                                url:"http://bookinfo.demo:32542/productpage",
                                success:function(data){
                                        alert(data);
                                }
                        })
                });

        $("#cors2").click(
                function(){
                        $.ajax({
                                type:"get",
                                dataType : "json",
                                url:"http://bookinfo.demo:32542/reviews/1",
                                contentType : 'application/json;charset=UTF-8',
                                success:function(data){
                                        var jsonStr = JSON.stringify(data);
                                        alert(jsonStr);
                                }
                        })
                });
          $("#cors3").click(
                function(){
                        $.ajax({
                                type:"delete",
                                contentType : 'application/json;charset=UTF-8',
                                dataType : "json",
                                url:"http://bookinfo.demo:32542/reviews/1",
                                success:function(data){
                                        var jsonStr = JSON.stringify(data);
                                        alert(jsonStr);
                                }
                        })
                });
           $("#cors4").click(
                function(){
                        $.ajax({
                                type:"get",
                                contentType : 'application/json;charset=UTF-8',
                                dataType : "json",
                                headers:{"X-Custom-Header":"value"},
                                url:"http://bookinfo.demo:32542/reviews/1",
                                success:function(data){
                                        var jsonStr = JSON.stringify(data);
                                        alert(jsonStr);
                                }
                        })
                });
         
});

</script>
<input type="button" id="cors" value="简单请求"/>
<input type="button" id="cors2" value="非简单请求"/>
<input type="button" id="cors3" value="非简单请求delete"/>
<input type="button" id="cors4" value="非简单请求headers"/>
</body>
</html>
```



# 3实战

## 3.1简单请求

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    corsPolicy:
      allowOrigins:
      - exact: "http://192.168.229.134:8081"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-cors-allow_origin_string_match.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cors
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 0.0.0.0_8080  
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
            - name: local_route
              domains:
              - "*"
              cors:
                allow_origin_string_match:
                - exact: "http://192.168.229.134:8081"
                filter_enabled:
                  default_value:
                    numerator: 100
                    denominator: HUNDRED
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  path: "/login"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  path: "/logout"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  prefix: "/api/v1/products"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

                  
kubectl apply -f ef-cors-allow_origin_string_match.yaml -n istio-system --context context-cluster1
```

## 3.2简单请求allowCredentials

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    corsPolicy:
      allowCredentials: true
      allowOrigins:
      - exact: "http://192.168.229.134:8081"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-cors-allowCredentials.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cors
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 0.0.0.0_8080  
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
            - name: local_route
              domains:
              - "*"
              cors:
                allow_origin_string_match:
                - exact: "http://192.168.229.134:8081"
                allow_credentials: true
                filter_enabled:
                  default_value:
                    numerator: 100
                    denominator: HUNDRED
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  path: "/login"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  path: "/logout"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  prefix: "/api/v1/products"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

                  
kubectl apply -f ef-cors-allowCredentials.yaml -n istio-system --context context-cluster1
```

## 3.3简单请求allowOrigins prefix

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    corsPolicy:
      allowOrigins:
      - prefix: "http://192"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-cors-allow_origin_string_match-prefix.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cors
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 0.0.0.0_8080  
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
            - name: local_route
              domains:
              - "*"
              cors:
                allow_origin_string_match:
                - prefix: "http://192"
                allow_credentials: true
                filter_enabled:
                  default_value:
                    numerator: 100
                    denominator: HUNDRED
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  path: "/login"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  path: "/logout"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  prefix: "/api/v1/products"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f ef-cors-allow_origin_string_match-prefix.yaml -n istio-system --context context-cluster1
```

## 3.4简单请求allowOrigins regex

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    corsPolicy:
      allowOrigins:
      - regex: ".*"
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-cors-allow_origin_string_match-regex.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cors
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 0.0.0.0_8080  
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
            - name: local_route
              domains:
              - "*"
              cors:
                allow_origin_string_match:
                - safeRegex:
                    googleRe2: {}
                    regex: .*
                allow_credentials: true
                filter_enabled:
                  default_value:
                    numerator: 100
                    denominator: HUNDRED
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  path: "/login"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  path: "/logout"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  prefix: "/api/v1/products"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f ef-cors-allow_origin_string_match-regex.yaml -n istio-system --context context-cluster1
```

## 3.5简单请求exposeHeaders

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        exact: /productpage
    - uri:
        prefix: /static
    - uri:
        exact: /login
    - uri:
        exact: /logout
    - uri:
        prefix: /api/v1/products
    corsPolicy:
      allowOrigins:
      - exact: "http://192.168.229.134"
      exposeHeaders: 
      - test
      - test2
    route:
    - destination:
        host: productpage
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-cors-exposeHeaders.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cors
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 0.0.0.0_8080  
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
            - name: local_route
              domains:
              - "*"
              cors:
                allow_origin_string_match:
                - safeRegex:
                    googleRe2: {}
                    regex: .*
                exposeHeaders: test,test2
                filter_enabled:
                  default_value:
                    numerator: 100
                    denominator: HUNDRED
              routes:
              - match:
                  path: "/productpage"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  prefix: "/static"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  path: "/login"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  path: "/logout"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
              - match:
                  prefix: "/api/v1/products"
                route:
                  cluster: outbound|9080||productpage.istio.svc.cluster.local
EOF

kubectl apply -f ef-cors-exposeHeaders.yaml -n istio-system --context context-cluster1
```

## 3.6非简单请求

virtualservice实现

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookreviews
spec:
  exportTo:
  - '*'
  gateways:
  - bookinfo-gateway
  hosts:
  - '*'
  http:
  - match:
    - uri:
        prefix: /reviews
    corsPolicy:
      allowOrigins:
      - exact: "http://192.168.229.134:8081"
      allowMethods:
      - GET
      - OPTIONS
      maxAge: "1m"
      allowHeaders:
      - content-type
    route:
    - destination:
        host: reviews
        port:
          number: 9080
```

envoyfilter实现

```
cat << EOF > ef-cors-not-simple.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cors
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 0.0.0.0_8080  
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
            - name: local_route
              domains:
              - "*"
              cors:
                allow_origin_string_match:
                - exact: "http://192.168.229.134:8081"
                allow_methods: "GET,OPTIONS"
                allow_headers: "content-type"
                max_age: "60"
                filter_enabled:
                  default_value:
                    numerator: 100
                    denominator: HUNDRED
              routes:
              - match:
                  prefix: "/reviews"
                route:
                  cluster: outbound|9080||reviews.istio.svc.cluster.local
EOF

kubectl apply -f ef-cors-not-simple.yaml -n istio-system --context context-cluster1
```

## 3.7 shadow_enabled 

vs无法实现



envoyfilter实现

```
cat << EOF > ef-cors-shadow.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: cors
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: NETWORK_FILTER
    match:
      listener:
        name: 0.0.0.0_8080  
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
            - name: local_route
              domains:
              - "*"
              cors:
                allow_origin_string_match:
                - exact: "http://192.168.229.134:8081"
                allow_methods: "GET,OPTIONS"
                allow_headers: "content-type"
                max_age: "60"
                filter_enabled:
                  default_value:
                    numerator: 1
                    denominator: HUNDRED
                shadow_enabled:
                  default_value:
                    numerator: 100
                    denominator: HUNDRED                
              routes:
              - match:
                  prefix: "/reviews"
                route:
                  cluster: outbound|9080||reviews.istio.svc.cluster.local
EOF

kubectl apply -f ef-cors-shadow.yaml -n istio-system --context context-cluster1
```

