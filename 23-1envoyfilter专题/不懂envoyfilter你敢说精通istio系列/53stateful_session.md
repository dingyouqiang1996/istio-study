# 1什么是stateful_session

stateful_sesstion是一个http类型的filter，用来配置会话粘性，会影响负载均衡选取的上游host。没有用hash based负载均衡时，使用这个会话粘性任然后效。name是nvoy.filters.http.stateful_session。

# 2配置

extensions.filters.http.stateful_session.v3.StatefulSession

```
{
  "session_state": "{...}"会话状态配置
}
```

session_state：

```
{
  "name": "...",名称
  "typed_config": "{...}"配置
}
```

envoy.http.stateful_session.cookie：

```
{
  "cookie": "{...}"cookie
}
```

cookie：

```
{
  "name": "...",名称
  "ttl": "{...}",生存时间
  "path": "..."路径
}
```

extensions.filters.http.stateful_session.v3.StatefulSessionPerRoute

```
{
  "disabled": "...",是否禁用
  "stateful_session": "{...}"会话状态配置
}
```



# 3案例

## 3.1StatefulSession

ef-StatefulSession.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: session 
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
            name: envoy.filters.http.stateful_session
            typed_config:
              "@type": type.googleapis.com/envoy.extensions.filters.http.stateful_session.v3.StatefulSession
              session_state:
                name: envoy.http.stateful_session.cookie
                typed_config:
                  "@type": type.googleapis.com/envoy.extensions.http.stateful_session.cookie.v3.CookieBasedSessionState
                  cookie:
                    name: global-session-cookie
                    path: /path
                    ttl: 120s
```



## 3.2StatefulSessionPerRoute

### 3.2.1disabled

ef-StatefulSessionPerRoute-disabled.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: session
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
                    typed_per_filter_config:
                      envoy.filters.http.stateful_session:
                        "@type": type.googleapis.com/envoy.extensions.filters.http.stateful_session.v3.StatefulSessionPerRoute
                        disabled: true
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "prefix"
```

### 3.2.2stateful_session

ef-StatefulSessionPerRoute-stateful_session.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: session
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
                    typed_per_filter_config:
                      envoy.filters.http.stateful_session:
                        "@type": type.googleapis.com/envoy.extensions.filters.http.stateful_session.v3.StatefulSessionPerRoute
                        session_state:
                          name: envoy.http.stateful_session.cookie
                          typed_config:
                            "@type": type.googleapis.com/envoy.extensions.http.stateful_session.cookie.v3.CookieBasedSessionState
                            cookie:
                              name: global-session-cookie
                              path: /path
                              ttl: 120s
                    routes:
                    - name: testroute
                      match: 
                        prefix: /
                      direct_response:
                        status: 200
                        body: 
                          inline_string: "prefix"
```

