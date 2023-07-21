# 1什么是secret

secret是进行tls连接时需要的证书信息。配置可分为静态配置和动态获取，动态获取通过sds获取，静态配置配置在bootstrap中。



# 2配置

```
kubectl patch deploy -n istio-system istio-ingressgateway -p '{"spec":{"template":{"spec":{"volumes":[{"name":"mysecret","secret":{"secretName":"bookinfo-secret"}}]}}}}'

kubectl patch deploy -n istio-system istio-ingressgateway -p '{"spec":{"template":{"spec":{"containers":[{"name":"istio-proxy","volumeMounts":[{"name":"mysecret","mountPath":"/var/lib/istio/secret"}]}]}}}}'
```



## 2.1TLS transport socket

### 2.1.1upstream

```
{
  "common_tls_context": "{...}",公共tls上下文
  "sni": "...",server name indicate
  "allow_renegotiation": "...",是否允许重新商议
  "max_session_keys": "{...}"最大会话key数量
}
```

common_tls_context：

```
{
  "tls_params": "{...}",tls参数
  "tls_certificates": [],tls证书
  "tls_certificate_sds_secret_configs": [],tls证书sds发现配置
  "validation_context": "{...}",校验上下文
  "validation_context_sds_secret_config": "{...}",校验上下文sds发现配置
  "combined_validation_context": "{...}",组合的校验上下文
  "alpn_protocols": [],alpn协议
  "custom_handshaker": "{...}",自定义握手
  "key_log": "{...}"tls key日志配置
}
```

tls_params：

```
{
  "tls_minimum_protocol_version": "...",tls最小协议版本
  "tls_maximum_protocol_version": "...",tls最大协议版本
  "cipher_suites": [],加密方法
  "ecdh_curves": []ecdh算法
}
```

tls_certificates：

```
{
  "certificate_chain": "{...}",证书链
  "private_key": "{...}",私钥
  "pkcs12": "{...}",pkcs12证书
  "watched_directory": "{...}",监控的证书目录
  "private_key_provider": "{...}",私钥提供者
  "password": "{...}",密码
  "ocsp_staple": "{...}"socp，在握手的时候会staple证书
}
```

certificate_chain，private_key，pkcs12：

```
{
  "filename": "...",文件名
  "inline_bytes": "...",内嵌字节
  "inline_string": "...",内嵌字符串
  "environment_variable": "..."环境变量
}
```

watched_directory：

```
{
  "path": "..."目录
}
```

private_key_provider：

```
{
  "provider_name": "...",私钥提供者名称
  "typed_config": "{...}"配置
}
```

tls_certificate_sds_secret_configs，validation_context_sds_secret_config：

```
{
  "name": "...",名称
  "sds_config": "{...}"sds配置
}
```

sds_config：

```
{
  "path": "...",路径
  "path_config_source": "{...}",路径配置源
  "api_config_source": "{...}",api配置源
  "ads": "{...}",ads
  "initial_fetch_timeout": "{...}",初始获取超时时间
  "resource_api_version": "..."资源api版本
}
```

validation_context：

```
{
  "trusted_ca": "{...}",受信任的ca
  "watched_directory": "{...}",监控的目录
  "verify_certificate_spki": [],验证证书的spki
  "verify_certificate_hash": [],验证证书的hash
  "match_typed_subject_alt_names": [],typed匹配的san
  "match_subject_alt_names": [],匹配的san
  "crl": "{...}",证书吊销列表
  "allow_expired_certificate": "...",是否允许过期证书
  "trust_chain_verification": "...",证书信用链验证模式
  "custom_validator_config": "{...}",自定义校验器配置
  "only_verify_leaf_cert_crl": "...",是否值验证叶子证书crl
  "max_verify_depth": "{...}"最大校验深度
}
```

trusted_ca，crl：

```
{
  "filename": "...",文件名
  "inline_bytes": "...",内嵌字节
  "inline_string": "...",内嵌字符串
  "environment_variable": "..."环境变量
}
```

match_typed_subject_alt_names：

```
{
  "san_type": "...",san类型
  "matcher": "{...}"匹配
}
```

san_type：

- SAN_TYPE_UNSPECIFIED

  *(DEFAULT)* ⁣

- EMAIL

  ⁣

- DNS

  ⁣

- URI

  ⁣

- IP_ADDRESS

matcher：

```
{
  "exact": "...",精确匹配
  "prefix": "...",前缀匹配
  "suffix": "...",后缀匹配
  "safe_regex": "{...}",正则匹配
  "contains": "...",包含匹配
  "ignore_case": "..."忽略大小写
}
```

trust_chain_verification：

- VERIFY_TRUST_CHAIN

  *(DEFAULT)* ⁣Perform default certificate verification (e.g., against CA / verification lists)

- ACCEPT_UNTRUSTED

  ⁣Connections where the certificate fails verification will be permitted. For HTTP connections, the result of certificate verification can be used in route matching. ( see [validated](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/route/v3/route_components.proto#envoy-v3-api-field-config-route-v3-routematch-tlscontextmatchoptions-validated) ).

combined_validation_context：

```
{
  "default_validation_context": "{...}",默认证书校验上下文
  "validation_context_sds_secret_config": "{...}"校验上下文sds发现配置
}
```

default_validation_context：

```
{
  "trusted_ca": "{...}",受信任的ca
  "watched_directory": "{...}",监控的目录
  "verify_certificate_spki": [],验证证书的spki
  "verify_certificate_hash": [],验证证书的hash
  "match_typed_subject_alt_names": [],typed匹配的san
  "match_subject_alt_names": [],匹配的san
  "crl": "{...}",证书吊销列表
  "allow_expired_certificate": "...",是否允许过期证书
  "trust_chain_verification": "...",证书信用链验证模式
  "custom_validator_config": "{...}",自定义校验器配置
  "only_verify_leaf_cert_crl": "...",是否值验证叶子证书crl
  "max_verify_depth": "{...}"最大校验深度
}
```



key_log：

```
{
  "path": "...", 路径
  "local_address_range": [],本地地址范围
  "remote_address_range": []远程地址范围
}
```

local_address_range，remote_address_range：

```
{
  "address_prefix": "...",地址前缀
  "prefix_len": "{...}"前缀长度
}
```

### 2.1.2downstream

```
{
  "common_tls_context": "{...}",公共tls上下文
  "require_client_certificate": "{...}",是否需要客户端证书
  "session_ticket_keys": "{...}",会话ticket key
  "session_ticket_keys_sds_secret_config": "{...}",会话ticket key sds发现配置
  "disable_stateless_session_resumption": "...",是否禁用无状态会话恢复
  "session_timeout": "{...}",会话超时时间
  "ocsp_staple_policy": "..."ocsp staple策略
}
```

session_ticket_keys：

```
{
  "keys": []键
}
```

keys：

```
{
  "filename": "...",文件名
  "inline_bytes": "...",内嵌字节
  "inline_string": "...",内嵌字符串
  "environment_variable": "..."环境变量
}
```

session_ticket_keys_sds_secret_config：

```
{
  "name": "...",名称
  "sds_config": "{...}"sds配置
}
```

ocsp_staple_policy：

- LENIENT_STAPLING

  *(DEFAULT)* ⁣OCSP responses are optional. If an OCSP response is absent or expired, the associated certificate will be used for connections without an OCSP staple.

- STRICT_STAPLING

  ⁣OCSP responses are optional. If an OCSP response is absent, the associated certificate will be used without an OCSP staple. If a response is provided but is expired, the associated certificate will not be used for subsequent connections. If no suitable certificate is found, the connection is rejected.

- MUST_STAPLE

  ⁣OCSP responses are required. Configuration will fail if a certificate is provided without an OCSP response. If a response expires, the associated certificate will not be used connections. If no suitable certificate is found, the connection is rejected.

## 2.2bootstrap

```
{
  "listeners": [],监听器
  "clusters": [],集群
  "secrets": []secret
}
```

secrets：

```
{
  "name": "...",名称	
  "tls_certificate": "{...}",tls证书
  "session_ticket_keys": "{...}",会话ticket key
  "validation_context": "{...}",校验上下文
  "generic_secret": "{...}"通用secret
}
```

tls_certificate：

```
{
  "certificate_chain": "{...}",证书链
  "private_key": "{...}",私钥
  "pkcs12": "{...}",pkcs12证书
  "watched_directory": "{...}",监控的证书目录
  "private_key_provider": "{...}",私钥提供者
  "password": "{...}",密码
  "ocsp_staple": "{...}"socp，在握手的时候会staple证书
}
```

validation_context：

```
{
  "trusted_ca": "{...}",受信任的ca
  "watched_directory": "{...}",监控的目录
  "verify_certificate_spki": [],验证证书的spki
  "verify_certificate_hash": [],验证证书的hash
  "match_typed_subject_alt_names": [],typed匹配的san
  "match_subject_alt_names": [],匹配的san
  "crl": "{...}",证书吊销列表
  "allow_expired_certificate": "...",是否允许过期证书
  "trust_chain_verification": "...",证书信用链验证模式
  "custom_validator_config": "{...}",自定义校验器配置
  "only_verify_leaf_cert_crl": "...",是否值验证叶子证书crl
  "max_verify_depth": "{...}"最大校验深度
}
```

generic_secret：

```
{
  "secret": "{...}"证书
}
```

secret：

```
{
  "filename": "...",文件名
  "inline_bytes": "...",内嵌字节
  "inline_string": "...",内嵌字符串
  "environment_variable": "..."环境变量
}
```



## 2.3OAuth 

```
{
  "client_id": "...",客户端id
  "token_secret": "{...}",token secret
  "hmac_secret": "{...}",hmac secret
  "cookie_names": "{...}"cookie名称
}
```

token_secret：

```
{
  "name": "...",名称
  "sds_config": "{...}"sds配置
}
```





# 3用处

1TLS transport socket

2bootstrap

3OAuth 



# 4案例

## 4.1TLS transport socket

### 4.1.1upstream

#### 4.1.1.1general

mkdir productpage 

cd productpage 

openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout example.com.key -out example.com.crt 

 openssl req -out bookinfo.example.com.csr -newkey rsa:2048 -nodes -keyout bookinfo.example.com.key -subj "/CN=bookinfo.example.com/O=some organization"  

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in bookinfo.example.com.csr -out bookinfo.example.com.crt 

创建secret

 kubectl create -n istio-system   secret tls bookinfo-secret --key=bookinfo.example.com.key   --cert=bookinfo.example.com.crt 



 kubectl create -n istio-system   secret generic bookinfo-ca --from-file=ca-key.pem   --from-file=ca-cert.pem 



```
kubectl patch deploy -n istio-system istio-ingressgateway -p '{"spec":{"template":{"spec":{"volumes":[{"name":"bookinfoca","secret":{"secretName":"bookinfo-ca"}}]}}}}'

kubectl patch deploy -n istio-system istio-ingressgateway -p '{"spec":{"template":{"spec":{"containers":[{"name":"istio-proxy","volumeMounts":[{"name":"bookinfoca","mountPath":"/var/lib/istio/validate"}]}]}}}}'
```



ef-secret-transport-socket-upstream.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - prefix: spiffe://cluster.local/ns
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: "kubernetes://bookinfo-secret"
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
                    allow_renegotiation: true
                    max_session_keys: 1
            
```

"kubernetes://productpage"

#### 4.1.1.2common_tls_context

##### 4.1.1.2.1alpn_protocols

ef-secret-transport-socket-upstream-common_tls_context-alpn_protocols.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - prefix: spiffe://cluster.local/ns
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: "kubernetes://bookinfo-secret"
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                      alpn_protocols:
                      - istio-peer-exchange
                      - istio
                      - h2
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
                    allow_renegotiation: true
                    max_session_keys: 1
            
```



##### 4.1.1.2.2tls_params

ef-secret-transport-socket-upstream-common_tls_context-tls_params.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - prefix: spiffe://cluster.local/ns
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: "kubernetes://bookinfo-secret"
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                      tls_params:
                        tls_minimum_protocol_version: TLSv1_2
                        tls_maximum_protocol_version: TLSv1_3
                        cipher_suites:
                          - ECDHE-ECDSA-AES128-GCM-SHA256
                          - ECDHE-RSA-AES128-GCM-SHA256
                          - ECDHE-ECDSA-AES128-SHA
                          - ECDHE-RSA-AES128-SHA
                          - AES128-GCM-SHA256
                          - AES128-SHA
                          - ECDHE-ECDSA-AES256-GCM-SHA384
                          - ECDHE-RSA-AES256-GCM-SHA384
                          - ECDHE-ECDSA-AES256-SHA
                          - ECDHE-RSA-AES256-SHA
                          - AES256-GCM-SHA384
                          - AES256-SHA
                        ecdh_curves:
                        - P-256
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
            
```



##### 4.1.1.2.3tls_certificates

```
{
  "certificate_chain": "{...}",
  "private_key": "{...}",
  "pkcs12": "{...}",
  "watched_directory": "{...}",
  "private_key_provider": "{...}",
  "password": "{...}",
  "ocsp_staple": "{...}"
}
```

###### 4.1.1.2.3.1filename

ef-secret-transport-socket-upstream-common_tls_context-tls_certificates-filename.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - prefix: spiffe://cluster.local/ns
                        validationContextSdsSecretConfig:
                            name: productpage
                            sdsConfig:
                              path_config_source: 
                                path: /var/lib/istio/validate/ca-cert.pem
                                watched_directory:
                                  path: /var/lib/istio/validate
                              initialFetchTimeout: 0s
                              resourceApiVersion: V3
                      tls_certificates:
                      - certificate_chain:
                          filename: /var/lib/istio/secret/tls.crt
                        private_key:
                          filename: /var/lib/istio/secret/tls.key
                        watched_directory:
                          path: /var/lib/istio/secret
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
            
```

routines:OPENSSL_internal:TLSV1_ALERT_UNKNOWN_CA



cm-ca.yaml

kubectl apply -f cm-ca.yaml -n istio-system

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: ca-configmap
data:
  ca-cert.pem: |-
    resources:
      - "@type": "type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.Secret"
        name: productpage
        validation_context:
          trusted_ca:
            inline_bytes: "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUZWakNDQXo2Z0F3SUJBZ0lVSXVCOHE2QXlNd1VoellhamF3YmtSeUw4TndBd0RRWUpLb1pJaHZjTkFRRUwKQlFBd0lqRU9NQXdHQTFVRUNnd0ZTWE4wYVc4eEVEQU9CZ05WQkFNTUIxSnZiM1FnUTBFd0hoY05Nakl3TkRJMwpNRFUxTURBMVdoY05NalF3TkRJMk1EVTFNREExV2pBOU1RNHdEQVlEVlFRS0RBVkpjM1JwYnpFWU1CWUdBMVVFCkF3d1BTVzUwWlhKdFpXUnBZWFJsSUVOQk1SRXdEd1lEVlFRSERBaGpiSFZ6ZEdWeU1UQ0NBaUl3RFFZSktvWkkKaHZjTkFRRUJCUUFEZ2dJUEFEQ0NBZ29DZ2dJQkFNbGgzZk1lTWlFL0JGQkh2QzBzcWM1ZC83bVZmM1RxQTVpRAozaHVOY0VXc3N2eWdNc041Yk9NTlJ5U3UrNVhiNUFvanVxenZXN1pDM21RaDVsNFVyZUYyeHNsdHUzb1lZQnZGCkw3VEp2N3p4Tjl2eG1kTDB4amwxaUtmY2hyd3R0WEttSDlRcFNzaFp2cGNMQ1FBeW1JZHk4NEhLTlJaUjR6UkgKaDBtSjJEMjVZYko5dml3V3BoLzdMeUJpcjBHaW9odFVZbU0rS240TGdsOXpHNnZQQ2g2Z2ZJckJibVR5aS9QMwpubnI1M3FEWlNsTHhQdk0yODBKd2lFS0h5akQ5Ujl0eUNzUzRUd2NaWTU3RG5raW9mQ2lJR0FEUU9tNWVqV3o2CjJVYldKVXBnb1M4SWc2YXFpOXU3dWZmT3pJVUR3bzNQRk1CVGZWNFRYUE82b3BkWXc2TFRFLzNRajN0NXZaa0QKN3hVQjhNUDIybzgzZk0yTmhTYXByRmFnU1dxL211SEcrYVFvL1g5L3Z2QU9IekxUY3VDYVVEZWR5WktvNm9DZQpBcjdCR2ZscFkvb1hOYXIybDhoaXl3UDhia0dIYkhQcGtZM3VHbUFJTWdXMWNGbjI0dnA1c1FXWENMUHU1Qjh2Cm9ZYUxPR3ZwL3grT3VUbGVublNzY3YzSm5FS3lON0ErODc2MFd2SnhBUG82QTgyd2pKaVc0T3pXYzNMTWYrTnMKN2lKQ0Zxa3BwalM1RXRhVU11bjFTK1NoQUZrak1pUnJhUDhGUWpSNjNXWVhOdzlqNm5YbDNPdmxBM2ZsaENBNApwU01DSHdpOFBqbitsdlFCei9ZMUZxaEoveHdJcUtyRTcwU251WEhxQ3ZjTGZmUEZVSzJYQmlqa1l3OWFidlBnCkpiMERwU2FwQWdNQkFBR2phVEJuTUIwR0ExVWREZ1FXQkJUWC9lRjB0bk5sRGJMOWRIazN5VUUvKzJXeVpqQVMKQmdOVkhSTUJBZjhFQ0RBR0FRSC9BZ0VBTUE0R0ExVWREd0VCL3dRRUF3SUM1REFpQmdOVkhSRUVHekFaZ2hkcApjM1JwYjJRdWFYTjBhVzh0YzNsemRHVnRMbk4yWXpBTkJna3Foa2lHOXcwQkFRc0ZBQU9DQWdFQVRMdEZML0d4CkRJVTZMSUJNc203SFFHd1pBVkUwa2pWelFrQ3k0b1FRTjNIQUJnVVF6TFBwWStsQVpHQk1iYWFuNFhKb00vTloKSDFtejdTL1JuQ3VpUVhJUjk1Tk1SWUJ2NDZwN2x1ejh3WCt1RDkvQU5OUmVjNzZpM2FTdVlUZVVOM2NRQVJWbApvd3JzeEZvMVJqbThlWkgwNG9ENDgwTkdwVnBMdFl6ZWJ1dEFiY0ZqR0ZrZVBmSGpLbTdya29hd2djZ1VnSzh2CjJDbGJhWExBVkQrd0E3ejlGWVFGMTRibXIxRkRQSEdtemN1cDRuRTJJckdjb1k5V0F4U2dMQ0pVM3JPTjdtODMKQ1Rpc3VSR1ZueXY2Vk9EWXhWY2x6UURQVVIvR1pCWFlkeGNqZWZicytxTHlienhjTlQxcUI5NmRkdFNUYm1VSQpSYnF1L2E2RUg5aHAxT3pBOHFLSDBvUGk3T3VleS84OFdFRTRJYWdueWFZaTZGSzhuak5UYmZVdTQyODZpWnI5CmFid1cvS2s1Y1RPekd4bFhtTnYrOGFKZmU4NnJ2bW9FNXRncnFhMVM4S2RvdkwyUERCWU5aUWFDT2VKTGJYWlkKTWNuL1hKQlQ3SkpXRndLNnFiUEF1MWNIQXJhQW0wNDlBa3dndk9WajQrTnBJSURKeDJUcmNFSi93Q2VaUm0wMApkeGhKZHVSWFljVWJXN2dpL2FnQWQrb2Q5WVpnd0VGSFB5eU5nYW1tMjZQcGFrc0FDTXhBc29KelhhZi9tOVVhCmFnVXc4MmFjaVp1TWNhYmFaZlJyNC8xdEp4SGx3ZGNyNjQrRDEvWnRCYXNQeW9xUmdiTWMvTWJkSm1oWnF0ZnMKZm1iVEtwWXpFY0x5eTNqTEVsQ3hFNmdkRjZ0MlA1SE54ZGc9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
```



```
kubectl patch deploy -n istio-system istio-ingressgateway -p '{"spec":{"template":{"spec":{"volumes":[{"name":"bookinfoca","configMap":{"name":"ca-configmap"}}]}}}}'

kubectl patch deploy -n istio-system istio-ingressgateway -p '{"spec":{"template":{"spec":{"containers":[{"name":"istio-proxy","volumeMounts":[{"name":"bookinfoca","mountPath":"/var/lib/istio/validate"}]}]}}}}'
```

Secret is not supplied by SDS



###### 4.1.1.2.3.2inline_bytes

ef-secret-transport-socket-upstream-common_tls_context-tls_certificates-inline_bytes.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - prefix: spiffe://cluster.local/ns
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tls_certificates:
                      - certificate_chain:
                          inline_bytes: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURZakNDQWtxZ0F3SUJBZ0lRWTQvalJKRWkrSnd0Ylo3VHNWeEs3ekFOQmdrcWhraUc5dzBCQVFzRkFEQVkKTVJZd0ZBWURWUVFLRXcxamJIVnpkR1Z5TG14dlkyRnNNQjRYRFRJeU1EUXdOekF5TURJME1sb1hEVEl5TURRdwpPREF5TURRME1sb3dBRENDQVNJd0RRWUpLb1pJaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFLZzFaLzI1ClRNU28zYTZtNHN1R0R3T0d5QnVoaU1ERlpNc0d5b1ZVTjl0Y3JkajN4c2IzdnlzN2xUZWlKNDFYMHViSTZvN1MKSkhrUnA4WGtTRTdDVmQ1N0hiS0FOaDZDdlc0WjdNd0JpU0h2NGxQRkdiVVN6QUlZb2FIbHgycVUyQ3BraE9hZQp6dXVlbjJWSFRhOVhkOEZCVlorSlZqeGFLZ01vdDB4bmszV3RKWmNmc2hmbW9YUUU3VUFOMFNoRFNyZWliZFlMCkZrVkU4U0sraW1LOGU5T21RdU9zS3U1VG90ampyTTRGWjF4YzlVbTczQTcrN3IvTTdPbmxNUWpseTl4RnUwV24KZzRiMkZSdVFzQXJsOHNRdWxTMzdWMDMzWVBHU0gvWlltOHFFS1hoN2NwbzkrQ3BJRVF6TkM4Y0JzdTQvT0drbwpJRWxJYW5EWVA0bFZ4SDhDQXdFQUFhT0J2ekNCdkRBT0JnTlZIUThCQWY4RUJBTUNCYUF3SFFZRFZSMGxCQll3CkZBWUlLd1lCQlFVSEF3RUdDQ3NHQVFVRkJ3TUNNQXdHQTFVZEV3RUIvd1FDTUFBd0h3WURWUjBqQkJnd0ZvQVUKTUFCT2dSck8yaTFQdU5RNGxVTC9kRVMxdk44d1hBWURWUjBSQVFIL0JGSXdVSVpPYzNCcFptWmxPaTh2WTJ4MQpjM1JsY2k1c2IyTmhiQzl1Y3k5cGMzUnBieTF6ZVhOMFpXMHZjMkV2YVhOMGFXOHRhVzVuY21WemMyZGhkR1YzCllYa3RjMlZ5ZG1salpTMWhZMk52ZFc1ME1BMEdDU3FHU0liM0RRRUJDd1VBQTRJQkFRQ0hOK05qZVBkTE92VlEKd2VKMmtjT1E2dU0rcllSK0dpZHFNQU9SbnNJdTh2bnBVWjNER0xaRWp5RVplOG1sNUtnNEd3QU5oV1ZLVnVOOQo1bmg2K1I0QkE1MVppaHF0c2NBQjlCSWR5blpSVkZFV3EwQldOWkY0a0xQeEpwRzNsTEhkVHlQc1ZWTjRpMkRqClFaUkhJRGZnZXdSWmxKM2k0SzJlZGg1dVN0TTd1NkdaOFo1c1NONW9ORHZkalAzLzI4Z3BsU3QrRzFkSFZVOXEKakdJY0w0VjRBYnc5aFl1eVoxY1ovUTBiSTE2SzUxWmlkNll4Ynh5MVAyY0NkVGpVM25qYjc1QVJwWHNTMVJtTQpVWEpBaXMydWFjeWhDRmcrc2dZcGVvTGdGSDFIL2VTOUwwWjhPSHNtVDRxZGh0YzhlKzZPZFFZbzVZTW1GVXlyCldmRTM0UndrCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0KLS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUMvVENDQWVXZ0F3SUJBZ0lSQU5peHNTVlBWWlA0YkNaL2pjdHo4NG93RFFZSktvWklodmNOQVFFTEJRQXcKR0RFV01CUUdBMVVFQ2hNTlkyeDFjM1JsY2k1c2IyTmhiREFlRncweU1UQTVNRGt3TURJeE5URmFGdzB6TVRBNQpNRGN3TURJeE5URmFNQmd4RmpBVUJnTlZCQW9URFdOc2RYTjBaWEl1Ykc5allXd3dnZ0VpTUEwR0NTcUdTSWIzCkRRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRQ3ZhamRUTmluNTNHcTgwem8wZ2QxVjU2NTBBVkRWTzhuWkNrQWQKMDQyOTYvY3kxYmJQRHgvSmRUbmlDZkYyYWNSV2NXRGExZkJVWVVSMjRHMWoxcmFzUDNXaDdlQWthS0FtTlpCMwpPVDBjQit2cXhtTGZBTzcxeUk3YVU0Zm5EWTJ5YmpLb0s0anhSQU1DeHBoUlFGWklUOExiMkhxV052TXk3bzhaCms4NGJRSmRxYzQvOGhGQkptNjBEOVNBeGdyZVppcFZqMjk2OTBvbDJPSnhoUkUxUSt0MGFiZVFEZWxseHlJRmUKYkxLNVViaFV4djZiSDdkMGVkL0M4djhhMFlqaWF0NkFVUkU0a3FxNXBybEtwWE5WOUlGVGcvRmNURituaDYxRwpjVCtNcFNEUVNjbTZLMGVPQzNubDFzckQ2aWppT2lKSWM4Qyt1YmxTWTVjRFVNakRBZ01CQUFHalFqQkFNQTRHCkExVWREd0VCL3dRRUF3SUNCREFQQmdOVkhSTUJBZjhFQlRBREFRSC9NQjBHQTFVZERnUVdCQlF3QUU2QkdzN2EKTFUrNDFEaVZRdjkwUkxXODN6QU5CZ2txaGtpRzl3MEJBUXNGQUFPQ0FRRUFVV0VKb0dSbm16UlowRW41Y2JWWQpXTHJma05SQ0l3QXJGUGRZaFkwL21hbk8vbkJ0K2dhTXl4Mm5xQmJTVUt6K0hvbENYM3J2ZThDaTBDNlVGU21TCnhzOGJQMjFRTGFQUlNBdUNWVzI5YklQc1pvODBpd2xwNWd4M0VLNmdLbURmNnpwdlVMTUwwTndYUzcxMzh0U0IKQURUT1ozYmhkZXhzVXVEQkd2c01pamJ6T0xzekl0YXkyMklDZTRlejBFVlBwTlo4aVdNaW54S1QzUU9pdUtmbwpSVFNCNS91aTI5dStyd2pDVnJISCtyU2FaQmt2MkNrS2hGKzBqcnhsWUxud0JENWRHK3R0RlhNQ3Z6d1FGbURMCmFQT3paaGZycmFlZFp0VEd4TkV1R1ROdVBPc0pIcGZCM3NiUXVMVDRKZVAyUjBZbk9sSmFxTWhzcVF2S1g3aWYKR1E9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==
                        private_key:
                          inline_bytes: W3JlZGFjdGVkXQ==
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
            
```



###### 4.1.1.2.3.3pkcs12

ef-secret-transport-socket-upstream-common_tls_context-tls_certificates-pkcs12.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - prefix: spiffe://cluster.local/ns
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tls_certificates:
                      - pkcs12:
                          filename: /etc/pkcs12/productpage.pem
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
            
```

##### 4.1.1.2.4tls_certificate_sds_secret_configs

###### 4.1.1.2.4.1path

ef-secret-transport-socket-upstream-common_tls_context-tls_certificate_sds_secret_configs-path.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - prefix: spiffe://cluster.local/ns
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: productpage
                        sdsConfig:
                          path: /var/lib/istio/secret/tls.crt
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
            
```







###### 4.1.1.2.4.2path_config_source

ef-secret-transport-socket-upstream-common_tls_context-tls_certificate_sds_secret_configs-path_config_source.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - prefix: spiffe://cluster.local/ns
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: productpage
                        sdsConfig:
                          path_config_source: 
                            path: /var/lib/istio/secret/tls.crt
                            watched_directory:
                              path: /var/lib/istio/secret
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
            
```

 unknown field "path_config_source" in envoy.config.core.v3.ConfigSource

###### 4.1.1.2.4.3api_config_source

```
{
  "api_type": "...",
  "transport_api_version": "...",
  "cluster_names": [],
  "grpc_services": [],
  "refresh_delay": "{...}",
  "request_timeout": "{...}",
  "rate_limit_settings": "{...}",
  "set_node_on_first_message_only": "...",
  "config_validators": []
}
```



4.1.1.2.4.3.1envoy_grpc

ef-secret-transport-socket-upstream-common_tls_context-tls_certificate_sds_secret_configs-api_config_source-envoy_grpc.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - prefix: spiffe://cluster.local/ns
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: "kubernetes://bookinfo-secret"
                        sdsConfig:
                          api_config_source:
                              apiType: GRPC
                              transport_api_version: V3
                              rate_limit_settings:
                                max_tokens: 10
                                fill_rate: 5
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                                timeout: 30s
                                initial_metadata:
                                - key: key1
                                  value: value1
                              setNodeOnFirstMessageOnly: true
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
            
```

2022-08-05T04:38:03.185831Z     warn    ads     ADS:CDS: ACK ERROR istio-ingressgateway-6467fd4649-fxj7c.istio-system-31 Internal:Error adding/updating cluster(s) my-productpage: Didn't find a registered implementation for name: 'envoy.config.validators.minimum_clusters'

4.1.1.2.4.3.2google_grpc

```
{
  "target_uri": "...",
  "channel_credentials": "{...}",
  "call_credentials": [],
  "stat_prefix": "...",
  "credentials_factory_name": "...",
  "config": "{...}",
  "per_stream_buffer_limit_bytes": "{...}",
  "channel_args": "{...}"
}
```

ef-secret-transport-socket-upstream-common_tls_context-tls_certificate_sds_secret_configs-api_config_source-google_grpc.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - exact: spiffe://cluster.local/ns/istio/sa/bookinfo-productpage
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: "kubernetes://bookinfo-secret"
                        sdsConfig:
                          api_config_source:
                              apiType: GRPC
                              transport_api_version: V3
                              rate_limit_settings:
                                max_tokens: 10
                                fill_rate: 5
                              grpcServices:
                              - google_grpc:
                                  target_uri: https://test.com
                                  channel_credentials:
                                    ssl_credentials:
                                      root_certs:
                                        filename: /etc/certs/root.pem
                                      private_key:
                                        filename: /etc/certs/key.pem
                                      cert_chain:
                                        filename: /etc/certs/chain.pem
                                  stat_prefix: google
                                  config:
                                    test: value
                                  per_stream_buffer_limit_bytes: 10241024
                                  channel_args:
                                    args:
                                      test:
                                        string_value: test
                                      test2:
                                        int_value: 10
                                timeout: 30s
                                initial_metadata:
                                - key: key1
                                  value: value1
                              setNodeOnFirstMessageOnly: true
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
            
```

###### 4.1.1.2.4.3.3rest

ef-secret-transport-socket-upstream-common_tls_context-tls_certificate_sds_secret_configs-api_config_source-rest.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - prefix: spiffe://cluster.local/ns
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: "kubernetes://bookinfo-secret"
                        sdsConfig:
                            apiConfigSource:
                              apiType: REST
                              cluster_names:
                              - certificate-rest
                              refresh_delay: 20s
                              request_timeout: 1s
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
            
```



###### 4.1.1.2.4.4ads

ef-secret-transport-socket-upstream-common_tls_context-tls_certificate_sds_secret_configs-ads.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                          matchSubjectAltNames:
                          - prefix: spiffe://cluster.local/ns
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: "kubernetes://bookinfo-secret"
                        sdsConfig:
                          ads: {}
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
            
```

TLSV1_ALERT_UNKNOWN_CA

##### 4.1.1.2.5validation_context

```
{
  "trusted_ca": "{...}",
  "watched_directory": "{...}",
  "verify_certificate_spki": [],
  "verify_certificate_hash": [],
  "match_typed_subject_alt_names": [],
  "match_subject_alt_names": [],
  "crl": "{...}",
  "allow_expired_certificate": "...",
  "trust_chain_verification": "...",
  "custom_validator_config": "{...}",
  "only_verify_leaf_cert_crl": "...",
  "max_verify_depth": "{...}"
}
```

ef-secret-transport-socket-upstream-common_tls_context-validation_context.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      validation_context:
                        trusted_ca:
                          filename: /etc/certs/ca.pem
                        verify_certificate_spki:
                        - NvqYIYSbgK2vCJpQhObf77vv+bQWtc5ek5RIOwPiC9A=
                        verify_certificate_hash: 
                        - df6ff72fe9116521268f6f2dd4966f51df479883fe7037b39f75916ac3049d1a
                        match_typed_subject_alt_names:
                        - san_type: DNS
                          matcher:
                            exact: "api.example.com"
                        crl:
                          filename: /etc/certs/crl.pem
                        allow_expired_certificate: true
                        trust_chain_verification: VERIFY_TRUST_CHAIN
                        custom_validator_config:
                          name: envoy.tls.cert_validator.spiffe
                          typed_config:
                            "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.SPIFFECertValidatorConfig
                            trust_domains:
                            - name: foo.com
                              trust_bundle:
                                filename: "foo.pem"
                            - name: envoy.com
                              trust_bundle:
                                filename: "envoy.pem"
                          only_verify_leaf_cert_crl: true
                          max_verify_depth: 100
                      tlsCertificateSdsSecretConfigs:
                      - name: "kubernetes://bookinfo-secret"
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
                    allow_renegotiation: true
                    max_session_keys: 1
            
```

unknown field "max_verify_depth"



verify_certificate_spki生成方法：

```
openssl x509 -in path/to/client.crt -noout -pubkey
  | openssl pkey -pubin -outform DER
  | openssl dgst -sha256 -binary
  | openssl enc -base64
```

verify_certificate_hash生成方法：

```
 openssl x509 -in path/to/client.crt -outform DER | openssl dgst -sha256 | cut -d" " -f2
 或
 openssl x509 -in path/to/client.crt -noout -fingerprint -sha256 | cut -d"=" -f2
```



##### 4.1.1.2.6validation_context_sds_secret_config

ef-secret-transport-socket-upstream-common_tls_context-validation_context_sds_secret_config.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      validationContextSdsSecretConfig:
                        name: ROOTCA
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: "kubernetes://bookinfo-secret"
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
                    allow_renegotiation: true
                    max_session_keys: 1
            
```



##### 4.1.1.2.7combined_validation_context

```
{
  "default_validation_context": "{...}",
  "validation_context_sds_secret_config": "{...}"
}
```

default_validation_context:

```
{
  "trusted_ca": "{...}",
  "watched_directory": "{...}",
  "verify_certificate_spki": [],
  "verify_certificate_hash": [],
  "match_typed_subject_alt_names": [],
  "match_subject_alt_names": [],
  "crl": "{...}",
  "allow_expired_certificate": "...",
  "trust_chain_verification": "...",
  "custom_validator_config": "{...}",
  "only_verify_leaf_cert_crl": "...",
  "max_verify_depth": "{...}"
}
```

ef-secret-transport-socket-upstream-common_tls_context-combined_validation_context.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
            name: http.8080
            virtual_hosts:
            - name: “*.8080”
              domains:
              - "*"
              routes:
              - match:
                  caseSensitive: true
                  headers:
                  - name: :method
                    safeRegexMatch:
                      googleRe2: {}
                      regex: G.*T
                  prefix: /
                route:
                  cluster: my-productpage
  - applyTo: CLUSTER
    patch:
      operation: ADD
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
          transport_socket:
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.UpstreamTlsContext
                    common_tls_context:
                      combinedValidationContext:
                        defaultValidationContext:
                            trusted_ca:
                              filename: /etc/certs/ca.pem
                            verify_certificate_spki:
                            - NvqYIYSbgK2vCJpQhObf77vv+bQWtc5ek5RIOwPiC9A=
                            verify_certificate_hash: 
                            - df6ff72fe9116521268f6f2dd4966f51df479883fe7037b39f75916ac3049d1a
                            match_typed_subject_alt_names:
                            - san_type: DNS
                              matcher:
                                exact: "api.example.com"
                            crl:
                              filename: /etc/certs/crl.pem
                            allow_expired_certificate: true
                            trust_chain_verification: VERIFY_TRUST_CHAIN
                            custom_validator_config:
                              name: envoy.tls.cert_validator.spiffe
                              typed_config:
                                "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.SPIFFECertValidatorConfig
                                trust_domains:
                                - name: foo.com
                                  trust_bundle:
                                    filename: "foo.pem"
                                - name: envoy.com
                                  trust_bundle:
                                    filename: "envoy.pem"
                            only_verify_leaf_cert_crl: true
                            max_verify_depth: 100
                        validationContextSdsSecretConfig:
                          name: ROOTCA
                          sdsConfig:
                            apiConfigSource:
                              apiType: GRPC
                              grpcServices:
                              - envoyGrpc:
                                  clusterName: sds-grpc
                              setNodeOnFirstMessageOnly: true
                              transportApiVersion: V3
                            initialFetchTimeout: 0s
                            resourceApiVersion: V3
                      tlsCertificateSdsSecretConfigs:
                      - name: "kubernetes://bookinfo-secret"
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3
                    sni: outbound_.9080_._.productpage.istio.svc.cluster.local
                    allow_renegotiation: true
                    max_session_keys: 1
            
```



### 4.1.2downstream

```
{
  "common_tls_context": "{...}",
  "require_client_certificate": "{...}",
  "session_ticket_keys": "{...}",
  "session_ticket_keys_sds_secret_config": "{...}",
  "disable_stateless_session_resumption": "...",
  "session_timeout": "{...}",
  "ocsp_staple_policy": "..."
}
```

ef-secret-transport-socket-downstream.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
  namespace: istio-system 
spec:
  workloadSelector:
    labels:
      istio: ingressgateway
  configPatches:
  - applyTo: LISTENER
    match:
      context: GATEWAY
    patch:
      operation: ADD
      value:
        name: proxy
        address:
          socket_address:
            protocol: TCP
            address: 0.0.0.0
            port_value: 8443
        filter_chains:
        - filters:
          - name: "envoy.filters.network.http_connection_manager"
            typed_config:
              "@type": "type.googleapis.com/envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager"
              codec_type: AUTO
              stat_prefix: ingress_https
              http_filters:
              - name: "envoy.filters.http.router"
                typed_config:
                  "@type": "type.googleapis.com/envoy.extensions.filters.http.router.v3.Router"
              route_config:
                name: http.8443
                virtual_hosts:
                - name: “*.8443”
                  domains:
                  - "*"
                  routes:
                  - match:
                      caseSensitive: true
                      headers:
                      - name: :method
                        safeRegexMatch:
                          googleRe2: {}
                          regex: G.*T
                      prefix: /
                    route:
                      cluster: my-productpage
          transport_socket: 
                  name: envoy.transport_sockets.tls
                  typed_config:
                    "@type": type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.DownstreamTlsContext
                    require_client_certificate: false
                    common_tls_context:
                      alpn_protocols:
                      - "h2"
                      - "http/1.1"
                      tlsCertificateSdsSecretConfigs:
                      - name: default
                        sdsConfig:
                          apiConfigSource:
                            apiType: GRPC
                            grpcServices:
                            - envoyGrpc:
                                clusterName: sds-grpc
                            setNodeOnFirstMessageOnly: true
                            transportApiVersion: V3
                          initialFetchTimeout: 0s
                          resourceApiVersion: V3   
        traffic_direction: "OUTBOUND"     
        listener_filters:    
        - name: "envoy.filters.listener.tls_inspector"
          typed_config:
            "@type": "type.googleapis.com/envoy.extensions.filters.listener.tls_inspector.v3.TlsInspector"

  - applyTo: CLUSTER
    patch:
      operation: ADD     
      value:
          name: my-productpage
          type: STRICT_DNS
          connect_timeout: 10s
          lb_policy: ROUND_ROBIN
          load_assignment:
            cluster_name: my-productpage
            endpoints:
            - lb_endpoints:
              - endpoint:
                  address:
                     socket_address:
                      address: productpage.istio.svc.cluster.local
                      port_value: 9080
            
```

keys:

```
openssl rand 80
```

Only one of [session_ticket_keys](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/tls/v3/tls.proto#envoy-v3-api-field-extensions-transport-sockets-tls-v3-downstreamtlscontext-session-ticket-keys), [session_ticket_keys_sds_secret_config](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/tls/v3/tls.proto#envoy-v3-api-field-extensions-transport-sockets-tls-v3-downstreamtlscontext-session-ticket-keys-sds-secret-config), [disable_stateless_session_resumption](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/transport_sockets/tls/v3/tls.proto#envoy-v3-api-field-extensions-transport-sockets-tls-v3-downstreamtlscontext-disable-stateless-session-resumption) may be set

## 4.2bootstrap

ef-secret-bootstrap.yaml

kubectl apply -f ef-secret-bootstrap.yaml -n istio

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: BOOTSTRAP
    patch:
      operation: MERGE
      value:
        static_resources:
          secrets:
          - name: default-test
            tls_certificate: 
              certificate_chain:
                inline_bytes: "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURTekNDQWpPZ0F3SUJBZ0lRREV2TnBYY0FzQ3FDSWI2TjBvWXY5ekFOQmdrcWhraUc5dzBCQVFzRkFEQVkKTVJZd0ZBWURWUVFLRXcxamJIVnpkR1Z5TG14dlkyRnNNQjRYRFRJeU1ETXlNekEwTURFek9Gb1hEVEl5TURNeQpOREEwTURNek9Gb3dBRENDQVNJd0RRWUpLb1pJaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFOTFNZSEl6CklTa3JxY1M5Q3pidFp3OVk0Rm43RngzdzJySDVOZkpGS1VRanh2N2UvQjI0eGJhVFB6ZFNLYjFaajNZeWFRVjEKOXAxZXNoZXRtZVJkRGZMeUxBdktRc3k1VGJLOGxlSWNLeU85SW94dnZTQU0rZDlicEhSUFhlU3o2NmdweDlvQwpVK3BidTcrZkY4dlNBK3dlS1FnTHJhVzAzYUtDUjZ1VVlES1J4UlJpMm5KYUFmWTg2S0tOMHBlS1ZKOVhIcVdFCmxvRUdrWW1LTmlZZDdZS0dmbDc1V0ZoTFBtT0Rjb0VZNVFZL3Q0emtmYVF4QW5XWklaaWVybGhqb2JJZ3lDMnEKNENQTkk1REI4S1JCc1JFaDN5enpiMGxDS3plcHRVanY0ZUQ5L0hwRGVoM2NBRXcvUjhhK0FvQ0ZaajBOelBBTAptS2pIZHZKUFpxQS9nTDBDQXdFQUFhT0JxRENCcFRBT0JnTlZIUThCQWY4RUJBTUNCYUF3SFFZRFZSMGxCQll3CkZBWUlLd1lCQlFVSEF3RUdDQ3NHQVFVRkJ3TUNNQXdHQTFVZEV3RUIvd1FDTUFBd0h3WURWUjBqQkJnd0ZvQVUKOVIzbk5jQ2FHd0N6ajdtNVhuV3BGdkVTcTZJd1JRWURWUjBSQVFIL0JEc3dPWVkzYzNCcFptWmxPaTh2WTJ4MQpjM1JsY2k1c2IyTmhiQzl1Y3k5cGMzUnBieTl6WVM5aWIyOXJhVzVtYnkxd2NtOWtkV04wY0dGblpUQU5CZ2txCmhraUc5dzBCQVFzRkFBT0NBUUVBTGxSS2RWbkxvaW5sdlphQUpyWGQzaGI2NjNxTmxialpXVkg0TXZJK0cvVG4KeVhUWGNPNTFrdkZMQVVUVmNPWmtsNGVyN3k2cXJmRWlETFhxSFRVenN1NGtuVUhzK3hNNnFMcFF1eVJkNkpGVAp3U1p6VHI5cFlaZFVmeWxubnBVQlRHa054WkFSTy9BQU9XZU5jempwWSsvQ041eHJrOWhnK3dxRldKNHhvZ2hjCmpOZE13RWZ0NnF3bkV5VDRNUllVQi9HZFR5WW5RcERSWHlyRWViU2oweElGcWdBaHc0VTVkU0FhR0hLZFB2WVEKamRWNTdRbDBnS2lDM1Zya294VWN0SkNSdW9wNW9hRnl4bFJzWmRvdjdqdmErRDlVVUQ0YXhMa0d6dlpBaW91MwpFVkpRd3F2Z0lKNldsWk40WDRyMHk4Y1pkdTNTaTQ2S1dlZjhoYUp2R3c9PQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCi0tLS0tQkVHSU4gQ0VSVElGSUNBVEUtLS0tLQpNSUlDL0RDQ0FlU2dBd0lCQWdJUWJqSkltdER1UnNhdVQ2dFBWVmQ2dERBTkJna3Foa2lHOXcwQkFRc0ZBREFZCk1SWXdGQVlEVlFRS0V3MWpiSFZ6ZEdWeUxteHZZMkZzTUI0WERUSXlNRE15TWpBek5UY3lNRm9YRFRNeU1ETXgKT1RBek5UY3lNRm93R0RFV01CUUdBMVVFQ2hNTlkyeDFjM1JsY2k1c2IyTmhiRENDQVNJd0RRWUpLb1pJaHZjTgpBUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBS1VhUGU4UldUQTRjRjdSUW5iRlFUK3JoaWlKSk9MUy91bUM2MEorClVIZjI2ekVKL2Z3QXBudGlLTTBLaTR6Zm5xK3pqM1k1azlFTEthR0M2cnNmNjNQVEJRRE5vRXExaTlhWXRraTYKY21CYkNLQlN5NGV3MndMSkxOZzN0VmJIK1orekgwY1dQQmhQUHo1MDZLRWx2Qjl4dVB1czVwWkltM25Ha3E3bQpDYmlzL1hSZGRXbkJZTFpLQWhxcSt5QS9RMkhqSkxsc0xHejJ5Y21KbEZGc3FqeGlQdlg5SmlZYUNKWDdOTTRkCk52YlhVOXROeTZsc0xiUWpkWTJHOXNlRWE4cGpFNW1OVnRWWHN5QXJma1VoK0VuZCtGN2g3bWowUDViR1FvSW4KZ0xkdmd0MU5vbnJQNUUraGx0ZWsxVzlJZEYzdk9zVGZINFI4STArZklCejNqOWNDQXdFQUFhTkNNRUF3RGdZRApWUjBQQVFIL0JBUURBZ0lFTUE4R0ExVWRFd0VCL3dRRk1BTUJBZjh3SFFZRFZSME9CQllFRlBVZDV6WEFtaHNBCnM0KzV1VjUxcVJieEVxdWlNQTBHQ1NxR1NJYjNEUUVCQ3dVQUE0SUJBUUNIUDdEbTFNTy9YRmEwSDV5QTJzYWMKRTlrWHdmeHN3WUEyaWtqR2F0RTFXY0cxaHhkWUJWVlpGRy9CUE8rM1NWZjB6QjVtR3kzemo5WGVta2dKMGsrKwpTOFp3M0RBcE9WcHpWVU0rRE91T3A1TjhNLy9rZ0xtRWdwSUswN29OS3RpUUMrc1hVMkF6alpIdHdUU1I4UzN6Ck9jK1YyUGh4VlBkVTFIZHlOaExDUW8yVTZHNHFDQU9pN3RkQ292UElJVmRvVGpzVUFhTFFLK2hLbU1uVFBFWTgKeGFvNy9VZXlsYmVNZTBNY3AyZnJLckFOdG9MV1hPM29tNXFkb3lVMVdDUlpuc3gxVCs4ZnJldHd5NGlra1dzdgpKL1MzdkxmSktROGJWLzRZWTlsNEtjSEtKN281d29YRllLT3d6YW5tSS9TZmtoK0RLbkYvZk4rSDlUd0JDSDFFCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K"
              private_key: 
                inline_bytes: "W3JlZGFjdGVkXQ=="

```



## 4.3OAuth2 

ef-secret-oauth.yaml

```
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: secret
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
                    path: "/etc/oauth2-ingress/token-secret.yaml"
                hmac_secret:
                  name: hmac
                  sds_config:
                    path: "/etc/oauth2-ingress/hmac-secret.yaml"
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

cm-oauth2.yaml

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: oauth2-ingress
data:
  token-secret.yaml: |-
    resources:
      - "@type": "type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.Secret"
        name: token
        generic_secret:
          secret:
            inline_string: "xxx"
  hmac-secret.yaml: |-
    resources:
      - "@type": "type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.Secret"
        name: hmac
        generic_secret:
          secret:
            inline_bytes: "xxx"
```

通过注解把配置文件挂载到ingressgateway

