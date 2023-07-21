# 1什么是oauth2

OAuth（开放授权）是一个开放标准，允许用户授权第三方移动应用访问他们存储在另外的服务提供者上的信息，而不需要将用户名和密码提供给第三方移动应用或分享他们数据的所有内容，OAuth2.0是OAuth协议的延续版本，但不向后兼容OAuth 1.0即完全废止了OAuth1.0。

# 2qq流程案例

![qq](48images\qq.webp)

# 3envoy流程

The OAuth filter’s flow involves:

- An unauthenticated user arrives at myapp.com, and the oauth filter redirects them to the [authorization_endpoint](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/http/oauth2/v3/oauth.proto#envoy-v3-api-field-extensions-filters-http-oauth2-v3-oauth2config-authorization-endpoint) for login. The [client_id](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/http/oauth2/v3/oauth.proto#envoy-v3-api-field-extensions-filters-http-oauth2-v3-oauth2credentials-client-id) and the [redirect_uri](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/http/oauth2/v3/oauth.proto#envoy-v3-api-field-extensions-filters-http-oauth2-v3-oauth2config-redirect-uri) are sent as query string parameters in this first redirect.
- After a successful login, the authn server should be configured to redirect the user back to the [redirect_uri](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/http/oauth2/v3/oauth.proto#envoy-v3-api-field-extensions-filters-http-oauth2-v3-oauth2config-redirect-uri) provided in the query string in the first step. In the below code example, we choose /callback as the configured match path. An “authorization grant” is included in the query string for this second redirect.
- Using this new grant and the [token_secret](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/http/oauth2/v3/oauth.proto#envoy-v3-api-field-extensions-filters-http-oauth2-v3-oauth2credentials-token-secret), the filter then attempts to retrieve an access token from the [token_endpoint](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/http/oauth2/v3/oauth.proto#envoy-v3-api-field-extensions-filters-http-oauth2-v3-oauth2config-token-endpoint). The filter knows it has to do this instead of reinitiating another login because the incoming request has a path that matches the [redirect_path_matcher](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/http/oauth2/v3/oauth.proto#envoy-v3-api-field-extensions-filters-http-oauth2-v3-oauth2config-redirect-path-matcher) criteria.
- Upon receiving an access token, the filter sets cookies so that subseqeuent requests can skip the full flow. These cookies are calculated using the [hmac_secret](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/http/oauth2/v3/oauth.proto#envoy-v3-api-field-extensions-filters-http-oauth2-v3-oauth2credentials-hmac-secret) to assist in encoding.
- The filter calls continueDecoding() to unblock the filter chain.
- The filter sets IdToken and RefreshToken cookies if they are provided by Identity provider along with AccessToken.

# 4配置

```
{
  "config": "{...}"配置
}
```

config:

```
{
  "token_endpoint": "{...}",获取token的服务器配置
  "authorization_endpoint": "...",授权端点
  "credentials": "{...}",相关密钥配置
  "redirect_uri": "...",跳转url
  "redirect_path_matcher": "{...}",跳转路径匹配
  "signout_path": "{...}",删除cookie路径
  "forward_bearer_token": "...",forward 令牌
  "pass_through_matcher": [],不需要认证路径
  "auth_scopes": [],认证范围
  "resources": []资源参数
}
```

token_endpoint：

```
{
  "uri": "...",获取token的uri
  "cluster": "...",上游集群名称
  "timeout": "{...}"超时时间
}
```

credentials：

```
{
  "client_id": "...",客户id
  "token_secret": "{...}",传给token_endpoint的secret
  "hmac_secret": "{...}",hmac的secret
  "cookie_names": "{...}"cookie名称
}
```

token_secret，hmac_secret：

```
{
  "name": "...",名称
  "sds_config": "{...}"secret发现
}
```

cookie_names：

```
{
  "bearer_token": "...",cookie名称
  "oauth_hmac": "...",cookie名称
  "oauth_expires": "..."cookie名称
}
```

redirect_path_matcher，signout_path：

```
{
  "path": "{...}"路径
}
```

path：

```
{
  "exact": "...",精确匹配
  "prefix": "...",前缀匹配
  "suffix": "...",后缀匹配
  "safe_regex": "{...}",regex匹配
  "contains": "...",包含
  "ignore_case": "..."忽略大小写
}
```

pass_through_matcher：

```
{
  "name": "...",名称
  "exact_match": "...",精确匹配
  "safe_regex_match": "{...}",正则匹配
  "range_match": "{...}",范围匹配
  "present_match": "...",是否存在
  "prefix_match": "...",前缀匹配
  "suffix_match": "...",后缀匹配
  "contains_match": "...",包含
  "string_match": "{...}",字符串匹配
  "invert_match": "..."反向匹配
}
```



# 5案例

## 5.1general

ef-oauth2-general.yaml

kubectl apply -f ef-oauth2-general.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: oauth2
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
          name: envoy.filters.http.oauth2
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.oauth2.v3.OAuth2
            config:
              token_endpoint:
                cluster: oauth
                uri: api.weixin.qq.com/sns/oauth2/access_token
                timeout: 3s
              authorization_endpoint: https://open.weixin.qq.com/connect/oauth2/authorize
              redirect_uri: "%REQ(:x-forwarded-proto)%://%REQ(:authority)%/callback"
              redirect_path_matcher:
                path:
                  exact: /callback
              signout_path:
                path:
                  exact: /signout
              credentials:
                client_id: wx2b0891384028ece7
                token_secret:
                  name: token
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
                hmac_secret:
                  name: hmac
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: oauth
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: oauth
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: api.weixin.qq.com
                      port_value: 443
```

## 5.2signout_path

ef-oauth2-signout_path.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: oauth2
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
          name: envoy.filters.http.oauth2
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.oauth2.v3.OAuth2
            config:
              token_endpoint:
                cluster: oauth
                uri: oauth.com/token
                timeout: 3s
              authorization_endpoint: https://oauth.com/oauth/authorize/
              redirect_uri: "%REQ(:x-forwarded-proto)%://%REQ(:authority)%/callback"
              redirect_path_matcher:
                path:
                  exact: /callback
              signout_path:
                path:
                  exact: /signout
              credentials:
                client_id: foo
                token_secret:
                  name: token
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
                hmac_secret:
                  name: hmac
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: oauth
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: oauth
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```

## 5.3auth_scopes

ef-oauth2-auth_scopes.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: oauth2
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
          name: envoy.filters.http.oauth2
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.oauth2.v3.OAuth2
            config:
              token_endpoint:
                cluster: oauth
                uri: oauth.com/token
                timeout: 3s
              authorization_endpoint: https://oauth.com/oauth/authorize/
              redirect_uri: "%REQ(:x-forwarded-proto)%://%REQ(:authority)%/callback"
              redirect_path_matcher:
                path:
                  exact: /callback
              signout_path:
                path:
                  exact: /signout
              credentials:
                client_id: foo
                token_secret:
                  name: token
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
                hmac_secret:
                  name: hmac
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
              # (Optional): defaults to 'user' scope if not provided
              auth_scopes:
              - user
              - openid
              - email
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: oauth
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: oauth
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```

## 5.4resources

## ef-oauth2-resources.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: oauth2
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
          name: envoy.filters.http.oauth2
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.oauth2.v3.OAuth2
            config:
              token_endpoint:
                cluster: oauth
                uri: oauth.com/token
                timeout: 3s
              authorization_endpoint: https://oauth.com/oauth/authorize/
              redirect_uri: "%REQ(:x-forwarded-proto)%://%REQ(:authority)%/callback"
              redirect_path_matcher:
                path:
                  exact: /callback
              signout_path:
                path:
                  exact: /signout
              credentials:
                client_id: foo
                token_secret:
                  name: token
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
                hmac_secret:
                  name: hmac
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
              # (Optional): set resource parameter for Authorization request
              resources:
              - oauth2-resource
              - http://example.com
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: oauth
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: oauth
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```

## 5.5forward_bearer_token

ef-oauth2-forward_bearer_token.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: oauth2
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
          name: envoy.filters.http.oauth2
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.oauth2.v3.OAuth2
            config:
              token_endpoint:
                cluster: oauth
                uri: oauth.com/token
                timeout: 3s
              authorization_endpoint: https://oauth.com/oauth/authorize/
              redirect_uri: "%REQ(:x-forwarded-proto)%://%REQ(:authority)%/callback"
              redirect_path_matcher:
                path:
                  exact: /callback
              signout_path:
                path:
                  exact: /signout
              credentials:
                client_id: foo
                token_secret:
                  name: token
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
                hmac_secret:
                  name: hmac
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
              forward_bearer_token: true
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: oauth
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: oauth
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080  
   
```

## 5.6pass_through_matcher

ef-oauth2-pass_through_matcher.yaml

kubectl apply -f ef-oauth2-pass_through_matcher.yaml -n istio-system

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: oauth2
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
          name: envoy.filters.http.oauth2
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.oauth2.v3.OAuth2
            config:
              token_endpoint:
                cluster: oauth
                uri: api.weixin.qq.com/sns/oauth2/access_token
                timeout: 3s
              authorization_endpoint: https://open.weixin.qq.com/connect/oauth2/authorize
              redirect_uri: "%REQ(:x-forwarded-proto)%://%REQ(:authority)%/callback"
              redirect_path_matcher:
                path:
                  exact: /callback
              signout_path:
                path:
                  exact: /signout
              credentials:
                client_id: wx2b0891384028ece7
                token_secret:
                  name: token
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
                hmac_secret:
                  name: hmac
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
              pass_through_matcher:
              - name: ":path"
                contains_match: productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: oauth
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: oauth
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: api.weixin.qq.com
                      port_value: 443
```

## 5.7cookie_names

ef-oauth2-cookie_names.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: oauth2
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
          name: envoy.filters.http.oauth2
          typed_config:
            "@type": type.googleapis.com/envoy.extensions.filters.http.oauth2.v3.OAuth2
            config:
              token_endpoint:
                cluster: oauth
                uri: oauth.com/token
                timeout: 3s
              authorization_endpoint: https://oauth.com/oauth/authorize/
              redirect_uri: "%REQ(:x-forwarded-proto)%://%REQ(:authority)%/callback"
              redirect_path_matcher:
                path:
                  exact: /callback
              signout_path:
                path:
                  exact: /signout
              credentials:
                client_id: foo
                token_secret:
                  name: token
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
                hmac_secret:
                  name: hmac
                  sds_config:
                    api_config_source: 
                      api_type: "GRPC"
                      grpc_services:
                      - envoy_grpc: 
                          cluster_name: "sds-grpc"
                      set_node_on_first_message_only: true
                      transport_api_version: "V3"
                    initial_fetch_timeout: "0s"
                    resource_api_version: "V3"
                cookie_names:
                  bearer_token: BearerToken
                  oauth_hmac: OauthHMAC
                  oauth_expires: OauthExpires
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: oauth
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: oauth
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
```

