# 什么是IstioOperator crd

istio operator 主要用于部署istio



# istioOperator详解

部署operator

```
istioctl operator init

istioctl operator init --watchedNamespaces=istio-namespace1,istio-namespace2
```

| Field                | Type                     | Description                                                  | Required |
| -------------------- | ------------------------ | ------------------------------------------------------------ | -------- |
| `profile`            | `string`                 | Path or name for the profile e.g.minimal (looks in profiles dir for a file called minimal.yaml)/tmp/istio/install/values/custom/custom-install.yaml (local file path)default profile is used if this field is unset. | No       |
| `installPackagePath` | `string`                 | Path for the install package. e.g./tmp/istio-installer/nightly (local file path) | No       |
| `hub`                | `string`                 | Root for docker image paths e.g. `docker.io/istio`           | No       |
| `tag`                | `TypeInterface`          | Version tag for docker images e.g. `1.7.2`                   | No       |
| `namespace`          | `string`                 | Namespace to install control plane resources into. If unset, Istio will be installed into the same namespace as the `IstioOperator` CR. You must also set `values.global.istioNamespace` if you wish to install Istio in a custom namespace. If you have enabled CNI, you must exclude this namespace by adding it to the list `values.cni.excludeNamespaces`. | No       |
| `revision`           | `string`                 | Identify the revision this installation is associated with. This option is currently experimental. | No       |
| `defaultRevision`    | `bool`                   | Identify whether this revision is the default revision for the cluster This option is currently experimental. | No       |
| `meshConfig`         | `TypeMapStringInterface` | Config used by control plane components internally.          | No       |
| `components`         | `IstioComponentSetSpec`  | Kubernetes resource settings, enablement and component-specific settings that are not internal to the component. | No       |
| `values`             | `TypeMapStringInterface` | Overrides for default `values.yaml`. This is a validated pass-through to Helm templates. See the [Helm installation options](https://istio.io/v1.5/docs/reference/config/installation-options/) for schema details. Anything that is available in `IstioOperatorSpec` should be set above rather than using the passthrough. This includes Kubernetes resource settings for components in `KubernetesResourcesSpec`. | No       |
| `unvalidatedValues`  | `TypeMapStringInterface` | Unvalidated overrides for default `values.yaml`. Used for custom templates where new parameters are added. | No       |
| `addonComponents`    | `map`                    | Deprecated. Users should manage the installation of addon components on their own. Refer to samples/addons for demo installation of addon components. | No       |

## profile

operator/iop-profile-demo.yaml

istioctl install  -f  iop-profile-demo.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
```



## installPackagePath

operator/iop-profile-installPackagePath.yaml

istioctl install  -f  iop-profile-installPackagePath.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  installPackagePath: /root/istio-1.11.2/manifests
```



## hub,tag

operator/iop-hub-tag.yaml

istioctl install  -f  iop-hub-tag.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  hub: docker.io/istio
  tag: 1.11.2
```



## namespace

operator/iop-namespace.yaml

istioctl install  -f  iop-namespace.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  namespace: istio-system-2
```



## revision,defaultRevision

operator/iop-revision-defaultRevision.yaml

istioctl install  -f  iop-revision-defaultRevision.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  revision: canary
  defaultRevision: true
```



## meshConfig

MeshConfig defines mesh-wide settings for the Istio service mesh.

| Field                            | Type                          | Description                                                  | Required |
| -------------------------------- | ----------------------------- | ------------------------------------------------------------ | -------- |
| `proxyListenPort`                | `int32`                       | Port on which Envoy should listen for incoming connections from other services. Default port is 15001. | No       |
| `proxyHttpPort`                  | `int32`                       | Port on which Envoy should listen for HTTP PROXY requests if set. | No       |
| `connectTimeout`                 | `Duration`                    | Connection timeout used by Envoy. (MUST BE >=1ms) Default timeout is 10s. | No       |
| `protocolDetectionTimeout`       | `Duration`                    | Automatic protocol detection uses a set of heuristics to determine whether the connection is using TLS or not (on the server side), as well as the application protocol being used (e.g., http vs tcp). These heuristics rely on the client sending the first bits of data. For server first protocols like MySQL, MongoDB, etc. Envoy will timeout on the protocol detection after the specified period, defaulting to non mTLS plain TCP traffic. Set this field to tweak the period that Envoy will wait for the client to send the first bits of data. (MUST BE >=1ms or 0s to disable). Default detection timeout is 5s. | No       |
| `tcpKeepalive`                   | `TcpKeepalive`                | If set then set `SO_KEEPALIVE` on the socket to enable TCP Keepalives. | No       |
| `ingressClass`                   | `string`                      | Class of ingress resources to be processed by Istio ingress controller. This corresponds to the value of `kubernetes.io/ingress.class` annotation. | No       |
| `ingressService`                 | `string`                      | Name of the Kubernetes service used for the istio ingress controller. If no ingress controller is specified, the default value `istio-ingressgateway` is used. | No       |
| `ingressControllerMode`          | `IngressControllerMode`       | Defines whether to use Istio ingress controller for annotated or all ingress resources. Default mode is `STRICT`. | No       |
| `ingressSelector`                | `string`                      | Defines which gateway deployment to use as the Ingress controller. This field corresponds to the Gateway.selector field, and will be set as `istio: INGRESS_SELECTOR`. By default, `ingressgateway` is used, which will select the default IngressGateway as it has the `istio: ingressgateway` labels. It is recommended that this is the same value as ingress_service. | No       |
| `enableTracing`                  | `bool`                        | Flag to control generation of trace spans and request IDs. Requires a trace span collector defined in the proxy configuration. | No       |
| `accessLogFile`                  | `string`                      | File address for the proxy access log (e.g. /dev/stdout). Empty value disables access logging. | No       |
| `accessLogFormat`                | `string`                      | Format for the proxy access log Empty value results in proxy’s default access log format | No       |
| `accessLogEncoding`              | `AccessLogEncoding`           | Encoding for the proxy access log (`TEXT` or `JSON`). Default value is `TEXT`. | No       |
| `enableEnvoyAccessLogService`    | `bool`                        | This flag enables Envoy’s gRPC Access Log Service. See [Access Log Service](https://www.envoyproxy.io/docs/envoy/latest/api-v2/config/accesslog/v2/als.proto) for details about Envoy’s gRPC Access Log Service API. Default value is `false`. | No       |
| `disableEnvoyListenerLog`        | `bool`                        | This flag disables Envoy Listener logs. See [Listener Access Log](https://www.envoyproxy.io/docs/envoy/latest/api-v3/config/listener/v3/listener.proto#envoy-v3-api-field-config-listener-v3-listener-access-log) Istio Enables Envoy’s listener access logs on “NoRoute” response flag. Default value is `false`. | No       |
| `defaultConfig`                  | `ProxyConfig`                 | Default proxy config used by gateway and sidecars. In case of Kubernetes, the proxy config is applied once during the injection process, and remain constant for the duration of the pod. The rest of the mesh config can be changed at runtime and config gets distributed dynamically. On Kubernetes, this can be overridden on individual pods with the `proxy.istio.io/config` annotation. | No       |
| `outboundTrafficPolicy`          | `OutboundTrafficPolicy`       | Set the default behavior of the sidecar for handling outbound traffic from the application. If your application uses one or more external services that are not known apriori, setting the policy to `ALLOW_ANY` will cause the sidecars to route any unknown traffic originating from the application to its requested destination. Users are strongly encouraged to use ServiceEntries to explicitly declare any external dependencies, instead of using `ALLOW_ANY`, so that traffic to these services can be monitored. Can be overridden at a Sidecar level by setting the `OutboundTrafficPolicy` in the [Sidecar API](https://istio.io/latest/docs/reference/config/networking/sidecar/#OutboundTrafficPolicy). Default mode is `ALLOW_ANY` which means outbound traffic to unknown destinations will be allowed. | No       |
| `configSources`                  | `ConfigSource[]`              | ConfigSource describes a source of configuration data for networking rules, and other Istio configuration artifacts. Multiple data sources can be configured for a single control plane. | No       |
| `enableAutoMtls`                 | `BoolValue`                   | This flag is used to enable mutual `TLS` automatically for service to service communication within the mesh, default true. If set to true, and a given service does not have a corresponding `DestinationRule` configured, or its `DestinationRule` does not have ClientTLSSettings specified, Istio configures client side TLS configuration appropriately. More specifically, If the upstream authentication policy is in `STRICT` mode, use Istio provisioned certificate for mutual `TLS` to connect to upstream. If upstream service is in plain text mode, use plain text. If the upstream authentication policy is in PERMISSIVE mode, Istio configures clients to use mutual `TLS` when server sides are capable of accepting mutual `TLS` traffic. If service `DestinationRule` exists and has `ClientTLSSettings` specified, that is always used instead. | No       |
| `trustDomain`                    | `string`                      | The trust domain corresponds to the trust root of a system. Refer to [SPIFFE-ID](https://github.com/spiffe/spiffe/blob/master/standards/SPIFFE-ID.md#21-trust-domain) | No       |
| `trustDomainAliases`             | `string[]`                    | The trust domain aliases represent the aliases of `trust_domain`. For example, if we have`trustDomain: td1 trustDomainAliases: ["td2", "td3"] `Any service with the identity `td1/ns/foo/sa/a-service-account`, `td2/ns/foo/sa/a-service-account`, or `td3/ns/foo/sa/a-service-account` will be treated the same in the Istio mesh. | No       |
| `caCertificates`                 | `CertificateData[]`           | The extra root certificates for workload-to-workload communication. The plugin certificates (the ‘cacerts’ secret) or self-signed certificates (the ‘istio-ca-secret’ secret) are automatically added by Istiod. The CA certificate that signs the workload certificates is automatically added by Istio Agent. | No       |
| `defaultServiceExportTo`         | `string[]`                    | The default value for the ServiceEntry.export_to field and services imported through container registry integrations, e.g. this applies to Kubernetes Service resources. The value is a list of namespace names and reserved namespace aliases. The allowed namespace aliases are:`* - All Namespaces . - Current Namespace ~ - No Namespace `If not set the system will use “*” as the default value which implies that services are exported to all namespaces.`All namespaces` is a reasonable default for implementations that don’t need to restrict access or visibility of services across namespace boundaries. If that requirement is present it is generally good practice to make the default `Current namespace` so that services are only visible within their own namespaces by default. Operators can then expand the visibility of services to other namespaces as needed. Use of `No Namespace` is expected to be rare but can have utility for deployments where dependency management needs to be precise even within the scope of a single namespace.For further discussion see the reference documentation for `ServiceEntry`, `Sidecar`, and `Gateway`. | No       |
| `defaultVirtualServiceExportTo`  | `string[]`                    | The default value for the VirtualService.export_to field. Has the same syntax as `default_service_export_to`.If not set the system will use “*” as the default value which implies that virtual services are exported to all namespaces | No       |
| `defaultDestinationRuleExportTo` | `string[]`                    | The default value for the `DestinationRule.export_to` field. Has the same syntax as `default_service_export_to`.If not set the system will use “*” as the default value which implies that destination rules are exported to all namespaces | No       |
| `rootNamespace`                  | `string`                      | The namespace to treat as the administrative root namespace for Istio configuration. When processing a leaf namespace Istio will search for declarations in that namespace first and if none are found it will search in the root namespace. Any matching declaration found in the root namespace is processed as if it were declared in the leaf namespace.The precise semantics of this processing are documented on each resource type. | No       |
| `localityLbSetting`              | `LocalityLoadBalancerSetting` | Locality based load balancing distribution or failover settings. | No       |
| `dnsRefreshRate`                 | `Duration`                    | Configures DNS refresh rate for Envoy clusters of type `STRICT_DNS` Default refresh rate is `5s`. | No       |
| `h2UpgradePolicy`                | `H2UpgradePolicy`             | Specify if http1.1 connections should be upgraded to http2 by default. if sidecar is installed on all pods in the mesh, then this should be set to `UPGRADE`. If one or more services or namespaces do not have sidecar(s), then this should be set to `DO_NOT_UPGRADE`. It can be enabled by destination using the `destinationRule.trafficPolicy.connectionPool.http.h2UpgradePolicy` override. | No       |
| `inboundClusterStatName`         | `string`                      | Name to be used while emitting statistics for inbound clusters. The same pattern is used while computing stat prefix for network filters like TCP and Redis. By default, Istio emits statistics with the pattern `inbound|||`. For example `inbound|7443|grpc-reviews|reviews.prod.svc.cluster.local`. This can be used to override that pattern.A Pattern can be composed of various pre-defined variables. The following variables are supported.`%SERVICE%` - Will be substituted with name of the service.`%SERVICE_FQDN%` - Will be substituted with FQDN of the service.`%SERVICE_PORT%` - Will be substituted with port of the service.`%SERVICE_PORT_NAME%` - Will be substituted with port name of the service.Following are some examples of supported patterns for reviews:`%SERVICE_FQDN%_%SERVICE_PORT%` will use reviews.prod.svc.cluster.local_7443 as the stats name.`%SERVICE%` will use reviews.prod as the stats name. | No       |
| `outboundClusterStatName`        | `string`                      | Name to be used while emitting statistics for outbound clusters. The same pattern is used while computing stat prefix for network filters like TCP and Redis. By default, Istio emits statistics with the pattern `outbound|||`. For example `outbound|8080|v2|reviews.prod.svc.cluster.local`. This can be used to override that pattern.A Pattern can be composed of various pre-defined variables. The following variables are supported.`%SERVICE%` - Will be substituted with name of the service.`%SERVICE_FQDN%` - Will be substituted with FQDN of the service.`%SERVICE_PORT%` - Will be substituted with port of the service.`%SERVICE_PORT_NAME%` - Will be substituted with port name of the service.`%SUBSET_NAME%` - Will be substituted with subset.Following are some examples of supported patterns for reviews:`%SERVICE_FQDN%_%SERVICE_PORT%` will use `reviews.prod.svc.cluster.local_7443` as the stats name.`%SERVICE%` will use reviews.prod as the stats name. | No       |
| `certificates`                   | `Certificate[]`               | Configure the provision of certificates.                     | No       |
| `thriftConfig`                   | `ThriftConfig`                | Set configuration for Thrift protocol                        | No       |
| `enablePrometheusMerge`          | `BoolValue`                   | If enabled, Istio agent will merge metrics exposed by the application with metrics from Envoy and Istio agent. The sidecar injection will replace `prometheus.io` annotations present on the pod and redirect them towards Istio agent, which will then merge metrics of from the application with Istio metrics. This relies on the annotations `prometheus.io/scrape`, `prometheus.io/port`, and `prometheus.io/path` annotations. If you are running a separately managed Envoy with an Istio sidecar, this may cause issues, as the metrics will collide. In this case, it is recommended to disable aggregation on that deployment with the `prometheus.istio.io/merge-metrics: "false"` annotation. If not specified, this will be enabled by default. | No       |
| `verifyCertificateAtClient`      | `BoolValue`                   | `VerifyCertificateAtClient` sets the mesh global default for peer certificate validation at the client-side proxy when `SIMPLE` TLS or `MUTUAL` TLS (non `ISTIO_MUTUAL`) origination modes are used. This setting can be overridden at the host level via DestinationRule API. By default, `VerifyCertificateAtClient` is `true`.`CaCertificates`: If set, proxy verifies CA signature based on given CaCertificates. If unset, and VerifyCertificateAtClient is true, proxy uses default System CA bundle. If unset and `VerifyCertificateAtClient` is false, proxy will not verify the CA.`SubjectAltNames`: If set, proxy verifies subject alt names are present in the SAN. If unset, and `VerifyCertificateAtClient` is true, proxy uses host in destination rule to verify the SANs. If unset, and `VerifyCertificateAtClient` is false, proxy does not verify SANs.For SAN, client-side proxy will exact match host in `DestinationRule` as well as one level wildcard if the specified host in DestinationRule doesn’t contain a wildcard. For example, if the host in `DestinationRule` is `x.y.com`, client-side proxy will match either `x.y.com` or `*.y.com` for the SAN in the presented server certificate. For wildcard host name in DestinationRule, client-side proxy will do a suffix match. For example, if host is `*.x.y.com`, client-side proxy will verify the presented server certificate SAN matches ``.x.y.com` suffix. | No       |
| `extensionProviders`             | `ExtensionProvider[]`         | Defines a list of extension providers that extend Istio’s functionality. For example, the AuthorizationPolicy can be used with an extension provider to delegate the authorization decision to a custom authorization system. | No       |
| `defaultProviders`               | `DefaultProviders`            | Specifies extension providers to use by default in Istio configuration resources. | No       |
| `discoverySelectors`             | `LabelSelector[]`             | A list of Kubernetes selectors that specify the set of namespaces that Istio considers when computing configuration updates for sidecars. This can be used to reduce Istio’s computational load by limiting the number of entities (including services, pods, and endpoints) that are watched and processed. If omitted, Istio will use the default behavior of processing all namespaces in the cluster. Elements in the list are disjunctive (OR semantics), i.e. a namespace will be included if it matches any selector. The following example selects any namespace that matches either below: 1. The namespace has both of these labels: `env: prod` and `region: us-east1` 2. The namespace has label `app` equal to `cassandra` or `spark`.`discoverySelectors:  - matchLabels:      env: prod      region: us-east1  - matchExpressions:    - key: app      operator: In      values:        - cassandra        - spark `Refer to the [kubernetes selector docs](https://kubernetes.io/docs/concepts/overview/working-with-objects/labels/#label-selectors) for additional detail on selector semantics. | No       |
| `pathNormalization`              | `ProxyPathNormalization`      | ProxyPathNormalization configures how URL paths in incoming and outgoing HTTP requests are normalized by the sidecars and gateways. The normalized paths will be used in all aspects through the requests’ lifetime on the sidecars and gateways, which includes routing decisions in outbound direction (client proxy), authorization policy match and enforcement in inbound direction (server proxy), and the URL path proxied to the upstream service. If not set, the NormalizationType.DEFAULT configuration will be used. | No       |

### connectTimeout

operator/iop-meshConfig-connectTimeout.yaml

istioctl install  -f  iop-meshConfig-connectTimeout.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    connectTimeout: 20s
```



```
[root@node01 meshconfig]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    connectTimeout: 20s
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      proxyMetadata: {}
      tracing:
        zipkin:
          address: zipkin.istio-system:9411
    enablePrometheusMerge: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```

![1631591557(1)](images\1631591557(1).jpg)



### protocolDetectionTimeout

operator/iop-meshConfig-protocolDetectionTimeout.yaml

istioctl install  -f  iop-meshConfig-protocolDetectionTimeout.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    protocolDetectionTimeout: 25s
```



```
[root@node01 meshconfig]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      proxyMetadata: {}
      tracing:
        zipkin:
          address: zipkin.istio-system:9411
    enablePrometheusMerge: true
    protocolDetectionTimeout: 30s
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```

![1631592151(1)](images\1631592151(1).jpg)

### tcpKeepalive

 probes , time , interval 

operator/iop-meshConfig-tcpKeepalive.yaml

istioctl install  -f  iop-meshConfig-tcpKeepalive.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    tcpKeepalive: 
      probes: 11
      time: 1h
      interval: 11s
```



```
[root@node01 meshconfig]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      proxyMetadata: {}
      tracing:
        zipkin:
          address: zipkin.istio-system:9411
    enablePrometheusMerge: true
    rootNamespace: istio-system
    tcpKeepalive:
      interval: 11s
      probes: 11
      time: 1h
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```

operator/meshconfig/dr-productpage-tcpkeepalive.yaml

kubectl apply -f dr-productpage-tcpkeepalive.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: DestinationRule
metadata:
  name: productpage
spec:
  host: productpage
  subsets:
  - name: v1
    labels:
      version: v1
  trafficPolicy:
    connectionPool:
      tcp:
        maxConnections: 100
        connectTimeout: 30ms
        tcpKeepalive:
          time: 7200s
          interval: 75s
          probes: 10
```

![1631592551(1)](images\1631592551(1).jpg)

###  ingressClass 

operator/iop-meshConfig-ingressClass.yaml

istioctl install  -f  iop-meshConfig-ingressClass.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    ingressClass: istio
```





ingress-productpage.yaml

kubectl apply -f ingress-productpage.yaml -n istio

```
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: productpage
  annotations:
    kubernetes.io/ingress.class: istio
spec:
  rules:
  - http:
      paths:
      - backend:
          serviceName: productpage
          servicePort: 9080
        path: /productpage
      - path: /static
        backend:
          serviceName: productpage
          servicePort: 9080
```

###  ingressService 

operator/iop-meshConfig-ingressService.yaml

istioctl install  -f  iop-meshConfig-ingressService.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    ingressClass: istio
    ingressService: istio-ingressgateway
```

ingress-productpage.yaml

kubectl apply -f ingress-productpage.yaml -n istio

```
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  name: productpage
  annotations:
    kubernetes.io/ingress.class: istio
spec:
  rules:
  - http:
      paths:
      - backend:
          serviceName: productpage
          servicePort: 9080
        path: /productpage
      - path: /static
        backend:
          serviceName: productpage
          servicePort: 9080
```

###  

 ingressControllerMode 

| Name          | Description                                                  |
| ------------- | ------------------------------------------------------------ |
| `UNSPECIFIED` | Unspecified Istio ingress controller.                        |
| `OFF`         | Disables Istio ingress controller.                           |
| `DEFAULT`     | Istio ingress controller will act on ingress resources that do not contain any annotation or whose annotations match the value specified in the ingress_class parameter described earlier. Use this mode if Istio ingress controller will be the default ingress controller for the entire Kubernetes cluster. |
| `STRICT`      | Istio ingress controller will only act on ingress resources whose annotations match the value specified in the ingress_class parameter described earlier. Use this mode if Istio ingress controller will be a secondary ingress controller (e.g., in addition to a cloud-provided ingress controller). |

operator/iop-meshConfig-ingressControllerMode.yaml

istioctl install  -f  iop-meshConfig-ingressControllerMode.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    ingressClass: istio
    ingressService: istio-ingressgateway
    ingressControllerMode: STRICT
```





###  ingressSelector 

operator/meshconfig/iop-meshConfig-ingressSelector.yaml

istioctl install  -f  iop-meshConfig-ingressSelector.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    ingressClass: istio
    ingressService: istio-ingressgateway
    ingressSelector: ingressgateway
```

operator/meshconfig/gateway-01.yaml

kubectl apply -f gateway-01.yaml -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: bookinfo-gateway
spec:
  selector:
    istio: ingressgateway
  servers:
  - port:
      number: 80
      name: http
      protocol: HTTP
    hosts:
    - "*"
```

operator/meshconfig/vs-bookinfo-hosts-star.yaml 

kubectl apply -f vs-bookinfo-hosts-star.yaml  -n istio

```
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: bookinfo
spec:
  hosts:
  - "*"
  gateways:
  - bookinfo-gateway
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
    route:
    - destination:
        host: productpage.istio.svc.cluster.local
        port:
          number: 9080
```

###  enableTracing 

operator/meshconfig/iop-meshConfig-enableTracing.yaml

istioctl install  -f  iop-meshConfig-enableTracing.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    enableTracing: true
    defaultConfig:
      tracing:
        sampling: 100
        zipkin:
          address: zipkin.istio-system:9411
```



```
[root@node01 meshconfig]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      proxyMetadata: {}
      tracing:
        sampling: 100
        zipkin:
          address: zipkin.istio-system:9411
    enablePrometheusMerge: true
    enableTracing: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```



### accessLogFile

operator/meshconfig/iop-meshConfig-accessLogFile.yaml

istioctl install  -f  iop-meshConfig-accessLogFile.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
```

```
[root@node01 meshconfig]# kubectl get cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      proxyMetadata: {}
      tracing:
        zipkin:
          address: zipkin.istio-system:9411
    enablePrometheusMerge: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}'
```



###  accessLogFormat 



Default access log format

Istio will use the following default access log format if `accessLogFormat` is not specified:

```plain
[%START_TIME%] \"%REQ(:METHOD)% %REQ(X-ENVOY-ORIGINAL-PATH?:PATH)% %PROTOCOL%\" %RESPONSE_CODE% %RESPONSE_FLAGS% %RESPONSE_CODE_DETAILS% %CONNECTION_TERMINATION_DETAILS%
\"%UPSTREAM_TRANSPORT_FAILURE_REASON%\" %BYTES_RECEIVED% %BYTES_SENT% %DURATION% %RESP(X-ENVOY-UPSTREAM-SERVICE-TIME)% \"%REQ(X-FORWARDED-FOR)%\" \"%REQ(USER-AGENT)%\" \"%REQ(X-REQUEST-ID)%\"
\"%REQ(:AUTHORITY)%\" \"%UPSTREAM_HOST%\" %UPSTREAM_CLUSTER% %UPSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_LOCAL_ADDRESS% %DOWNSTREAM_REMOTE_ADDRESS% %REQUESTED_SERVER_NAME% %ROUTE_NAME%\n
```



| Log operator                                                 | access log in sleep                            | access log in httpbin                             |
| ------------------------------------------------------------ | ---------------------------------------------- | ------------------------------------------------- |
| `[%START_TIME%]`                                             | `[2020-11-25T21:26:18.409Z]`                   | `[2020-11-25T21:26:18.409Z]`                      |
| `\"%REQ(:METHOD)% %REQ(X-ENVOY-ORIGINAL-PATH?:PATH)% %PROTOCOL%\"` | `"GET /status/418 HTTP/1.1"`                   | `"GET /status/418 HTTP/1.1"`                      |
| `%RESPONSE_CODE%`                                            | `418`                                          | `418`                                             |
| `%RESPONSE_FLAGS%`                                           | `-`                                            | `-`                                               |
| `%RESPONSE_CODE_DETAILS%`                                    | `via_upstream`                                 | `via_upstream`                                    |
| `%CONNECTION_TERMINATION_DETAILS%`                           | `-`                                            | `-`                                               |
| `\"%UPSTREAM_TRANSPORT_FAILURE_REASON%\"`                    | `"-"`                                          | `"-"`                                             |
| `%BYTES_RECEIVED%`                                           | `0`                                            | `0`                                               |
| `%BYTES_SENT%`                                               | `135`                                          | `135`                                             |
| `%DURATION%`                                                 | `4`                                            | `3`                                               |
| `%RESP(X-ENVOY-UPSTREAM-SERVICE-TIME)%`                      | `4`                                            | `1`                                               |
| `\"%REQ(X-FORWARDED-FOR)%\"`                                 | `"-"`                                          | `"-"`                                             |
| `\"%REQ(USER-AGENT)%\"`                                      | `"curl/7.73.0-DEV"`                            | `"curl/7.73.0-DEV"`                               |
| `\"%REQ(X-REQUEST-ID)%\"`                                    | `"84961386-6d84-929d-98bd-c5aee93b5c88"`       | `"84961386-6d84-929d-98bd-c5aee93b5c88"`          |
| `\"%REQ(:AUTHORITY)%\"`                                      | `"httpbin:8000"`                               | `"httpbin:8000"`                                  |
| `\"%UPSTREAM_HOST%\"`                                        | `"10.44.1.27:80"`                              | `"127.0.0.1:80"`                                  |
| `%UPSTREAM_CLUSTER%`                                         | `outbound|8000||httpbin.foo.svc.cluster.local` | `inbound|8000||`                                  |
| `%UPSTREAM_LOCAL_ADDRESS%`                                   | `10.44.1.23:37652`                             | `127.0.0.1:41854`                                 |
| `%DOWNSTREAM_LOCAL_ADDRESS%`                                 | `10.0.45.184:8000`                             | `10.44.1.27:80`                                   |
| `%DOWNSTREAM_REMOTE_ADDRESS%`                                | `10.44.1.23:46520`                             | `10.44.1.23:37652`                                |
| `%REQUESTED_SERVER_NAME%`                                    | `-`                                            | `outbound_.8000_._.httpbin.foo.svc.cluster.local` |
| `%ROUTE_NAME%`                                               | `default`                                      | `default`                                         |



operator/meshconfig/iop-meshConfig-accessLogFormat.yaml

istioctl install  -f  iop-meshConfig-accessLogFormat.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    accessLogFormat : "[%START_TIME%] \"%REQ(:METHOD)% %REQ(X-ENVOY-ORIGINAL-PATH?:PATH)% %PROTOCOL%\""
```

![1631599173(1)](images\1631599173(1).jpg)



### accessLogEncoding 

MeshConfig.AccessLogEncoding

| Name   | Description                            |
| ------ | -------------------------------------- |
| `TEXT` | text encoding for the proxy access log |
| `JSON` | json encoding for the proxy access log |

operator/meshconfig/iop-meshConfig-accessLogEncoding.yaml

istioctl install  -f  iop-meshConfig-accessLogEncoding.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    accessLogEncoding: JSON
```



![1631599639(1)](images\1631599639(1).jpg)



 kubectl logs -f -n istio ratings-v1-688d667b55-zzjtq -c istio-proxy --tail 10

![1631599565(1)](images\1631599565(1).jpg)

### enableEnvoyAccessLogService 

operator/meshconfig/iop-meshConfig-enableEnvoyAccessLogService.yaml

istioctl install  -f  iop-meshConfig-enableEnvoyAccessLogService.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    enableEnvoyAccessLogService: true
    defaultConfig:
      envoyAccessLogService:
        address: skywalking-oap.istio-system:11800
```

###  disableEnvoyListenerLog 

operator/meshconfig/iop-meshConfig-disableEnvoyListenerLog.yaml

istioctl install  -f  iop-meshConfig-disableEnvoyListenerLog.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    disableEnvoyListenerLog : true
```

###  

###  outboundTrafficPolicy 

####  REGISTRY_ONLY 

operator/meshconfig/iop-meshConfig-outboundTrafficPolicy-REGISTRY_ONLY.yaml

istioctl install  -f  iop-meshConfig-outboundTrafficPolicy-REGISTRY_ONLY.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    outboundTrafficPolicy:
      mode: REGISTRY_ONLY 
```



```
[root@node01 meshconfig]# kubectl exec -it -n istio ratings-v1-58fc5b895c-m7bbb /bin/bash
kubectl exec [POD] [COMMAND] is DEPRECATED and will be removed in a future version. Use kubectl exec [POD] -- [COMMAND] instead.
node@ratings-v1-58fc5b895c-m7bbb:/opt/microservices$ curl www.baidu.com
node@ratings-v1-58fc5b895c-m7bbb:/opt/microservices$ curl www.baidu.com -I
HTTP/1.1 502 Bad Gateway
date: Fri, 17 Sep 2021 04:42:00 GMT
server: envoy
transfer-encoding: chunked
```



####  ALLOW_ANY 

operator/meshconfig/iop-meshConfig-outboundTrafficPolicy-ALLOW_ANY.yaml

istioctl install  -f  iop-meshConfig-outboundTrafficPolicy-ALLOW_ANY.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    outboundTrafficPolicy:
      mode: ALLOW_ANY 
```



```
root@node01 meshconfig]# kubectl exec -it -n istio ratings-v1-58fc5b895c-m7bbb /bin/bash
kubectl exec [POD] [COMMAND] is DEPRECATED and will be removed in a future version. Use kubectl exec [POD] -- [COMMAND] instead.
node@ratings-v1-58fc5b895c-m7bbb:/opt/microservices$ curl www.baidu.com -I
HTTP/1.1 200 OK
accept-ranges: bytes
cache-control: private, no-cache, no-store, proxy-revalidate, no-transform
content-length: 277
content-type: text/html
date: Fri, 17 Sep 2021 04:43:38 GMT
etag: "575e1f7c-115"
last-modified: Mon, 13 Jun 2016 02:50:36 GMT
pragma: no-cache
server: envoy
x-envoy-upstream-service-time: 42
```



### ConfigSource

ConfigSource describes information about a configuration store inside a mesh. A single control plane instance can interact with one or more data sources.

| Field                 | Type                | Description                                                  | Required |
| --------------------- | ------------------- | ------------------------------------------------------------ | -------- |
| `address`             | `string`            | Address of the server implementing the Istio Mesh Configuration protocol (MCP). Can be IP address or a fully qualified DNS name. Use fs:/// to specify a file-based backend with absolute path to the directory. | No       |
| `tlsSettings`         | `ClientTLSSettings` | Use the tls_settings to specify the tls mode to use. If the MCP server uses Istio mutual TLS and shares the root CA with Pilot, specify the TLS mode as `ISTIO_MUTUAL`. | No       |
| `subscribedResources` | `Resource[]`        | Describes the source of configuration, if nothing is specified default is MCP | No       |

operator/meshconfig/iop-meshConfig-ConfigSource.yaml

istioctl install  -f  iop-meshConfig-ConfigSource.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    configSources: 
    - address: https://192.168.229.131:6443
      tlsSettings:
        mode: ISTIO_MUTUAL
      subscribedResources:
      - SERVICE_REGISTRY
```



###  enableAutoMtls 

operator/meshconfig/iop-meshConfig-enableAutoMtls.yaml

istioctl install  -f  iop-meshConfig-enableAutoMtls.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    enableAutoMtls: true 
```



###  trustDomain 

operator/meshconfig/iop-meshConfig-trustDomain.yaml

istioctl install  -f  iop-meshConfig-trustDomain.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    trustDomain: cluster.local
```



```
[root@node01 meshconfig]# kubectl get  cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      proxyMetadata: {}
      tracing:
        zipkin:
          address: zipkin.istio-system:9411
    enablePrometheusMerge: true
    rootNamespace: istio-system
    trustDomain: cluster.local
  meshNetworks: 'networks: {}
```



###  trustDomainAliases 

operator/meshconfig/iop-meshConfig-trustDomainAliases.yaml

istioctl install  -f  iop-meshConfig-trustDomainAliases.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    trustDomain: cluster.local
    trustDomainAliases:
    - cluster.local2
    - cluster.local3
```



```
[root@node01 meshconfig]# kubectl get  cm istio -n istio-system -o yaml
apiVersion: v1
data:
  mesh: |-
    accessLogFile: /dev/stdout
    defaultConfig:
      discoveryAddress: istiod.istio-system.svc:15012
      proxyMetadata: {}
      tracing:
        zipkin:
          address: zipkin.istio-system:9411
    enablePrometheusMerge: true
    rootNamespace: istio-system
    trustDomain: cluster.local
    trustDomainAliases:
    - cluster.local2
    - cluster.local3
  meshNetworks: 'networks: {}'
```



###  caCertificates 

operator/meshconfig/iop-meshConfig-caCertificates .yaml

istioctl install  -f  iop-meshConfig-caCertificates.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    caCertificates:
    - pem: |
        data
```



###  defaultServiceExportTo 

operator/meshconfig/iop-meshConfig-defaultServiceExportTo.yaml

istioctl install  -f  iop-meshConfig-defaultServiceExportTo.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    defaultServiceExportTo:
    - “*”
```



###  defaultVirtualServiceExportTo 

operator/meshconfig/iop-meshConfig-defaultVirtualServiceExportTo.yaml

istioctl install  -f  iop-meshConfig-defaultVirtualServiceExportTo.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    defaultVirtualServiceExportTo:
    - “*”
```



###  defaultDestinationRuleExportTo 

operator/meshconfig/iop-meshConfig-defaultDestinationRuleExportTo.yaml

istioctl install  -f  iop-meshConfig-defaultDestinationRuleExportTo.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    defaultDestinationRuleExportTo:
    - “*”
```



###  rootNamespace 

operator/meshconfig/iop-meshConfig-rootNamespace.yaml

istioctl install  -f  iop-meshConfig-rootNamespace.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    rootNamespace: istio-system
```



###  localityLbSetting 

operator/meshconfig/iop-meshConfig-localityLbSetting.yaml

istioctl install  -f  iop-meshConfig-localityLbSetting.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    localityLbSetting:
      enabled: true
      distribute:
      - from: us-west/zone1/*
        to:
          "us-west/zone1/*": 80
          "us-west/zone2/*": 20
      - from: us-west/zone2/*
        to:
          "us-west/zone1/*": 20
          "us-west/zone2/*": 80
```



###  dnsRefreshRate 

operator/meshconfig/iop-meshConfig-dnsRefreshRate.yaml

istioctl install  -f  iop-meshConfig-dnsRefreshRate.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    dnsRefreshRate: 11s
```



###  h2UpgradePolicy 

operator/meshconfig/iop-meshConfig-h2UpgradePolicy-DO_NOT_UPGRADE.yaml

istioctl install  -f  iop-meshConfig-h2UpgradePolicy-DO_NOT_UPGRADE.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    h2UpgradePolicy: DO_NOT_UPGRADE
```



operator/meshconfig/iop-meshConfig-h2UpgradePolicy-UPGRADE.yaml

istioctl install  -f  iop-meshConfig-h2UpgradePolicy-UPGRADE.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    h2UpgradePolicy: UPGRADE
```



###  inboundClusterStatName 

有问题

operator/meshconfig/iop-meshConfig-inboundClusterStatName.yaml

istioctl install  -f  iop-meshConfig-inboundClusterStatName.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    inboundClusterStatName: "%SERVICE_FQDN%"
```



###  outboundClusterStatName 

operator/meshconfig/iop-meshConfig-outboundClusterStatName.yaml

istioctl install  -f  iop-meshConfig-outboundClusterStatName.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    outboundClusterStatName: "%SERVICE_FQDN%"
```



###  certificates 

operator/meshconfig/iop-meshConfig-certificates.yaml

istioctl install  -f  iop-meshConfig-certificates.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    certificates:
      secretName:
      dnsNames:
      -
```



###  thriftConfig 

operator/meshconfig/iop-meshConfig-thriftConfig.yaml

istioctl install  -f  iop-meshConfig-thriftConfig.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    thriftConfig:
      rateLimitUrl:
      rateLimitTimeout:
```



###  enablePrometheusMerge 

operator/meshconfig/iop-meshConfig-enablePrometheusMerge.yaml

istioctl install  -f  iop-meshConfig-enablePrometheusMerge.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    enablePrometheusMerge: true
```



###  verifyCertificateAtClient 

operator/meshconfig/iop-meshConfig-verifyCertificateAtClient.yaml

istioctl install  -f  iop-meshConfig-verifyCertificateAtClient.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    verifyCertificateAtClient: true
    
```



###  discoverySelectors 

####  matchLabels 

operator/meshconfig/iop-meshConfig-discoverySelectors-matchLabels.yaml

istioctl install  -f  iop-meshConfig-discoverySelectors-matchLabels.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    discoverySelectors:
    - matchLabels:
        istio-injection: enabled
    
```



####  matchExpressions 

operator/meshconfig/iop-meshConfig-discoverySelectors-matchExpressions.yaml

istioctl install  -f  iop-meshConfig-discoverySelectors-matchExpressions.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    discoverySelectors:
    - matchExpressions:
      - {key: "istio-injection", operator: In, values: [“enabled”]} 
    
```





###  pathNormalization 

| Name                       | Description                                                  |
| -------------------------- | ------------------------------------------------------------ |
| `DEFAULT`                  | Apply default normalizations. Currently, this is BASE.       |
| `NONE`                     | No normalization, paths are used as is.                      |
| `BASE`                     | Normalize according to [RFC 3986](https://tools.ietf.org/html/rfc3986). For Envoy proxies, this is the [`normalize_path`](https://www.envoyproxy.io/docs/envoy/latest/api-v3/extensions/filters/network/http_connection_manager/v3/http_connection_manager.proto.html) option. For example, `/a/../b` normalizes to `/b`. |
| `MERGE_SLASHES`            | In addition to the `BASE` normalization, consecutive slashes are also merged. For example, `/a//b` normalizes to `a/b`. |
| `DECODE_AND_MERGE_SLASHES` | In addition to normalization in `MERGE_SLASHES`, slash characters are UTF-8 decoded (case insensitive) prior to merging. This means `%2F`, `%2f`, `%5C`, and `%5c` sequences in the request path will be rewritten to `/` or `\`. For example, `/a%2f/b` normalizes to `a/b`. |

operator/meshconfig/iop-meshConfig-pathNormalization.yaml

istioctl install  -f  iop-meshConfig-pathNormalization.yaml

```
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: demo
  meshConfig:
    accessLogFile: /dev/stdout
    pathNormalization: 
      normalization: DECODE_AND_MERGE_SLASHES 
    
```





###  extensionProviders 

| Field                | Type                                             | Description                                                  | Required |
| -------------------- | ------------------------------------------------ | ------------------------------------------------------------ | -------- |
| `name`               | `string`                                         | REQUIRED. A unique name identifying the extension provider.  | No       |
| `envoyExtAuthzHttp`  | `EnvoyExternalAuthorizationHttpProvider (oneof)` | Configures an external authorizer that implements the Envoy ext_authz filter authorization check service using the HTTP API. | No       |
| `envoyExtAuthzGrpc`  | `EnvoyExternalAuthorizationGrpcProvider (oneof)` | Configures an external authorizer that implements the Envoy ext_authz filter authorization check service using the gRPC API. | No       |
| `zipkin`             | `ZipkinTracingProvider (oneof)`                  | Configures a tracing provider that uses the Zipkin API.      | No       |
| `lightstep`          | `LightstepTracingProvider (oneof)`               | Configures a Lightstep tracing provider.                     | No       |
| `datadog`            | `DatadogTracingProvider (oneof)`                 | Configures a Datadog tracing provider.                       | No       |
| `stackdriver`        | `StackdriverProvider (oneof)`                    | Configures a Stackdriver provider.                           | No       |
| `opencensus`         | `OpenCensusAgentTracingProvider (oneof)`         | Configures an OpenCensusAgent tracing provider.              | No       |
| `skywalking`         | `SkyWalkingTracingProvider (oneof)`              | Configures a Apache SkyWalking provider.                     | No       |
| `prometheus`         | `PrometheusMetricsProvider (oneof)`              | Configures a Prometheus metrics provider.                    | No       |
| `envoyFileAccessLog` | `EnvoyFileAccessLogProvider (oneof)`             | Configures an Envoy File Access Log provider.                | No       |





###  defaultProviders 

| Field           | Type       | Description                                         | Required |
| --------------- | ---------- | --------------------------------------------------- | -------- |
| `tracing`       | `string[]` | Name of the default provider(s) for tracing.        | No       |
| `metrics`       | `string[]` | Name of the default provider(s) for metrics.        | No       |
| `accessLogging` | `string[]` | Name of the default provider(s) for access logging. | No       |



###  defaultConfig 

 ProxyConfig defines variables for individual Envoy instances. This can be configured on a per-workload basis as well as by the mesh-wide defaults. To set the mesh wide defaults, configure the `defaultConfig` section of `meshConfig`.  

| Field                             | Type                      | Description                                                  | Required |
| --------------------------------- | ------------------------- | ------------------------------------------------------------ | -------- |
| `configPath`                      | `string`                  | Path to the generated configuration file directory. Proxy agent generates the actual configuration and stores it in this directory. | No       |
| `binaryPath`                      | `string`                  | Path to the proxy binary                                     | No       |
| `serviceCluster`                  | `string`                  | Service cluster defines the name for the `service_cluster` that is shared by all Envoy instances. This setting corresponds to `--service-cluster` flag in Envoy. In a typical Envoy deployment, the `service-cluster` flag is used to identify the caller, for source-based routing scenarios.Since Istio does not assign a local `service/service` version to each Envoy instance, the name is same for all of them. However, the source/caller’s identity (e.g., IP address) is encoded in the `--service-node` flag when launching Envoy. When the RDS service receives API calls from Envoy, it uses the value of the `service-node` flag to compute routes that are relative to the service instances located at that IP address. | No       |
| `drainDuration`                   | `Duration`                | The time in seconds that Envoy will drain connections during a hot restart. MUST be >=1s (e.g., *1s/1m/1h*) Default drain duration is `45s`. | No       |
| `parentShutdownDuration`          | `Duration`                | The time in seconds that Envoy will wait before shutting down the parent process during a hot restart. MUST be >=1s (e.g., `1s/1m/1h`). MUST BE greater than `drain_duration` parameter. Default shutdown duration is `60s`. | No       |
| `discoveryAddress`                | `string`                  | Address of the discovery service exposing xDS with mTLS connection. The inject configuration may override this value. | No       |
| `statsdUdpAddress`                | `string`                  | IP Address and Port of a statsd UDP listener (e.g. `10.75.241.127:9125`). | No       |
| `proxyAdminPort`                  | `int32`                   | Port on which Envoy should listen for administrative commands. Default port is `15000`. | No       |
| `controlPlaneAuthPolicy`          | `AuthenticationPolicy`    | AuthenticationPolicy defines how the proxy is authenticated when it connects to the control plane. Default is set to `MUTUAL_TLS`. | No       |
| `customConfigFile`                | `string`                  | File path of custom proxy configuration, currently used by proxies in front of Mixer and Pilot. | No       |
| `statNameLength`                  | `int32`                   | Maximum length of name field in Envoy’s metrics. The length of the name field is determined by the length of a name field in a service and the set of labels that comprise a particular version of the service. The default value is set to 189 characters. Envoy’s internal metrics take up 67 characters, for a total of 256 character name per metric. Increase the value of this field if you find that the metrics from Envoys are truncated. | No       |
| `concurrency`                     | `Int32Value`              | The number of worker threads to run. If unset, this will be automatically determined based on CPU requests/limits. If set to 0, all cores on the machine will be used. Default is 2 worker threads. | No       |
| `proxyBootstrapTemplatePath`      | `string`                  | Path to the proxy bootstrap template file                    | No       |
| `interceptionMode`                | `InboundInterceptionMode` | The mode used to redirect inbound traffic to Envoy.          | No       |
| `tracing`                         | `Tracing`                 | Tracing configuration to be used by the proxy.               | No       |
| `sds`                             | `SDS`                     | Secret Discovery Service(SDS) configuration to be used by the proxy. | No       |
| `envoyAccessLogService`           | `RemoteService`           | Address of the service to which access logs from Envoys should be sent. (e.g. `accesslog-service:15000`). See [Access Log Service](https://www.envoyproxy.io/docs/envoy/latest/api-v2/config/accesslog/v2/als.proto) for details about Envoy’s gRPC Access Log Service API. | No       |
| `envoyMetricsService`             | `RemoteService`           | Address of the Envoy Metrics Service implementation (e.g. `metrics-service:15000`). See [Metric Service](https://www.envoyproxy.io/docs/envoy/latest/api-v2/config/metrics/v2/metrics_service.proto) for details about Envoy’s Metrics Service API. | No       |
| `proxyMetadata`                   | `map`                     | Additional environment variables for the proxy. Names starting with `ISTIO_META_` will be included in the generated bootstrap and sent to the XDS server. | No       |
| `statusPort`                      | `int32`                   | Port on which the agent should listen for administrative commands such as readiness probe. Default is set to port `15020`. | No       |
| `extraStatTags`                   | `string[]`                | An additional list of tags to extract from the in-proxy Istio telemetry. These extra tags can be added by configuring the telemetry extension. Each additional tag needs to be present in this list. Extra tags emitted by the telemetry extensions must be listed here so that they can be processed and exposed as Prometheus metrics. | No       |
| `terminationDrainDuration`        | `Duration`                | The amount of time allowed for connections to complete on proxy shutdown. On receiving `SIGTERM` or `SIGINT`, `istio-agent` tells the active Envoy to start draining, preventing any new connections and allowing existing connections to complete. It then sleeps for the `termination_drain_duration` and then kills any remaining active Envoy processes. If not set, a default of `5s` will be applied. | No       |
| `meshId`                          | `string`                  | The unique identifier for the [service mesh](https://istio.io/latest/docs/reference/glossary/#service-mesh) All control planes running in the same service mesh should specify the same mesh ID. Mesh ID is used to label telemetry reports for cases where telemetry from multiple meshes is mixed together. | No       |
| `readinessProbe`                  | `ReadinessProbe`          | VM Health Checking readiness probe. This health check config exactly mirrors the kubernetes readiness probe configuration both in schema and logic. Only one health check method of 3 can be set at a time. | No       |
| `proxyStatsMatcher`               | `ProxyStatsMatcher`       | Proxy stats matcher defines configuration for reporting custom Envoy stats. To reduce memory and CPU overhead from Envoy stats system, Istio proxies by default create and expose only a subset of Envoy stats. This option is to control creation of additional Envoy stats with prefix, suffix, and regex expressions match on the name of the stats. This replaces the stats inclusion annotations (`sidecar.istio.io/statsInclusionPrefixes`, `sidecar.istio.io/statsInclusionRegexps`, and `sidecar.istio.io/statsInclusionSuffixes`). For example, to enable stats for circuit breaker, retry, and upstream connections, you can specify stats matcher as follow:`proxyStatsMatcher:  inclusionRegexps:    - .*circuit_breakers.*  inclusionPrefixes:    - upstream_rq_retry    - upstream_cx `Note including more Envoy stats might increase number of time series collected by prometheus significantly. Care needs to be taken on Prometheus resource provision and configuration to reduce cardinality. | No       |
| `holdApplicationUntilProxyStarts` | `BoolValue`               | Boolean flag for enabling/disabling the holdApplicationUntilProxyStarts behavior. This feature adds hooks to delay application startup until the pod proxy is ready to accept traffic, mitigating some startup race conditions. Default value is ‘false’. | No       |
| `caCertificatesPem`               | `string[]`                | The PEM data of the extra root certificates for workload-to-workload communication. This includes the certificates defined in MeshConfig and any other certificates that Istiod uses as CA. The plugin certificates (the ‘cacerts’ secret), self-signed certificates (the ‘istio-ca-secret’ secret) are added automatically by Istiod. | No       |
| `zipkinAddress`                   | `string`                  | Address of the Zipkin service (e.g. *zipkin:9411*). DEPRECATED: Use [tracing](https://istio.io/latest/docs/reference/config/istio.mesh.v1alpha1/#ProxyConfig-tracing) instead. | No       |

####  configPath 



####  binaryPath 



####  serviceCluster 



####  drainDuration 



####  parentShutdownDuration 



####  discoveryAddress 



####  statsdUdpAddress 



####  proxyAdminPort 



####  controlPlaneAuthPolicy 



####  customConfigFile 



####  statNameLength 



####  concurrency 



####  proxyBootstrapTemplatePath 



####  interceptionMode 



####  tracing 



####  sds 



####  envoyAccessLogService 



####  envoyMetricsService 



####  proxyMetadata 



####  statusPort 



####  extraStatTags 



####  terminationDrainDuration 



####  meshId 



####  readinessProbe 



####  proxyStatsMatcher 



####  holdApplicationUntilProxyStarts 



####  caCertificatesPem 







## components

| Field             | Type                | Description                                     | Required |
| ----------------- | ------------------- | ----------------------------------------------- | -------- |
| `base`            | `BaseComponentSpec` |                                                 | No       |
| `pilot`           | `ComponentSpec`     |                                                 | No       |
| `cni`             | `ComponentSpec`     |                                                 | No       |
| `istiodRemote`    | `ComponentSpec`     | Remote cluster using an external control plane. | No       |
| `ingressGateways` | `GatewaySpec[]`     |                                                 | No       |
| `egressGateways`  | `GatewaySpec[]`     |                                                 | No       |

###  base 

####  enabled 



####  k8s 

| Field                 | Type                          | Description                                                  | Required |
| --------------------- | ----------------------------- | ------------------------------------------------------------ | -------- |
| `affinity`            | `Affinity`                    | k8s affinity. https://kubernetes.io/docs/concepts/configuration/assign-pod-node/#affinity-and-anti-affinity | No       |
| `env`                 | `EnvVar[]`                    | Deployment environment variables. https://kubernetes.io/docs/tasks/inject-data-application/define-environment-variable-container/ | No       |
| `hpaSpec`             | `HorizontalPodAutoscalerSpec` | k8s HorizontalPodAutoscaler settings. https://kubernetes.io/docs/tasks/run-application/horizontal-pod-autoscale/ | No       |
| `imagePullPolicy`     | `string`                      | k8s imagePullPolicy. https://kubernetes.io/docs/concepts/containers/images/ | No       |
| `nodeSelector`        | `map`                         | k8s nodeSelector. https://kubernetes.io/docs/concepts/configuration/assign-pod-node/#nodeselector | No       |
| `podDisruptionBudget` | `PodDisruptionBudgetSpec`     | k8s PodDisruptionBudget settings. https://kubernetes.io/docs/concepts/workloads/pods/disruptions/#how-disruption-budgets-work | No       |
| `podAnnotations`      | `map`                         | k8s pod annotations. https://kubernetes.io/docs/concepts/overview/working-with-objects/annotations/ | No       |
| `priorityClassName`   | `string`                      | k8s priority*class*name. Default for all resources unless overridden. https://kubernetes.io/docs/concepts/configuration/pod-priority-preemption/#priorityclass | No       |
| `readinessProbe`      | `ReadinessProbe`              | k8s readinessProbe settings. https://kubernetes.io/docs/tasks/configure-pod-container/configure-liveness-readiness-probes/ k8s.io.api.core.v1.Probe readiness_probe = 9; | No       |
| `replicaCount`        | `uint32`                      | k8s Deployment replicas setting. https://kubernetes.io/docs/concepts/workloads/controllers/deployment/ | No       |
| `resources`           | `Resources`                   | k8s resources settings. https://kubernetes.io/docs/concepts/configuration/manage-compute-resources-container/#resource-requests-and-limits-of-pod-and-container | No       |
| `service`             | `ServiceSpec`                 | k8s Service settings. https://kubernetes.io/docs/concepts/services-networking/service/ | No       |
| `strategy`            | `DeploymentStrategy`          | k8s deployment strategy. https://kubernetes.io/docs/concepts/workloads/controllers/deployment/ | No       |
| `tolerations`         | `Toleration[]`                | k8s toleration https://kubernetes.io/docs/concepts/configuration/taint-and-toleration/ | No       |
| `serviceAnnotations`  | `map`                         | k8s service annotations. https://kubernetes.io/docs/concepts/overview/working-with-objects/annotations/ | No       |
| `securityContext`     | `PodSecurityContext`          | k8s pod security context https://kubernetes.io/docs/tasks/configure-pod-container/security-context/#set-the-security-context-for-a-pod | No       |
| `volumes`             | `Volume[]`                    | k8s volume https://kubernetes.io/docs/concepts/storage/volumes/ Volumes defines the collection of Volume to inject into the pod. | No       |
| `volumeMounts`        | `VolumeMount[]`               | k8s volumeMounts VolumeMounts defines the collection of VolumeMount to inject into containers. | No       |
| `overlays`            | `K8sObjectOverlay[]`          | Overlays for k8s resources in rendered manifests.            | No       |



###  pilot 

| Field       | Type                      | Description                                              | Required |
| ----------- | ------------------------- | -------------------------------------------------------- | -------- |
| `enabled`   | `TypeBoolValueForPB`      | Selects whether this component is installed.             | No       |
| `namespace` | `string`                  | Namespace for the component.                             | No       |
| `hub`       | `string`                  | Hub for the component (overrides top level hub setting). | No       |
| `tag`       | `TypeInterface`           | Tag for the component (overrides top level tag setting). | No       |
| `spec`      | `TypeInterface`           | Arbitrary install time configuration for the component.  | No       |
| `k8s`       | `KubernetesResourcesSpec` | Kubernetes resource spec.                                | No       |





###  cni 

### 

| Field       | Type                      | Description                                              | Required |
| ----------- | ------------------------- | -------------------------------------------------------- | -------- |
| `enabled`   | `TypeBoolValueForPB`      | Selects whether this component is installed.             | No       |
| `namespace` | `string`                  | Namespace for the component.                             | No       |
| `hub`       | `string`                  | Hub for the component (overrides top level hub setting). | No       |
| `tag`       | `TypeInterface`           | Tag for the component (overrides top level tag setting). | No       |
| `spec`      | `TypeInterface`           | Arbitrary install time configuration for the component.  | No       |
| `k8s`       | `KubernetesResourcesSpec` | Kubernetes resource spec.                                | No       |



###  istiodRemote 

### 

| Field       | Type                      | Description                                              | Required |
| ----------- | ------------------------- | -------------------------------------------------------- | -------- |
| `enabled`   | `TypeBoolValueForPB`      | Selects whether this component is installed.             | No       |
| `namespace` | `string`                  | Namespace for the component.                             | No       |
| `hub`       | `string`                  | Hub for the component (overrides top level hub setting). | No       |
| `tag`       | `TypeInterface`           | Tag for the component (overrides top level tag setting). | No       |
| `spec`      | `TypeInterface`           | Arbitrary install time configuration for the component.  | No       |
| `k8s`       | `KubernetesResourcesSpec` | Kubernetes resource spec.                                | No       |



###  ingressGateways 

| Field       | Type                      | Description                                              | Required |
| ----------- | ------------------------- | -------------------------------------------------------- | -------- |
| `enabled`   | `TypeBoolValueForPB`      | Selects whether this gateway is installed.               | No       |
| `namespace` | `string`                  | Namespace for the gateway.                               | No       |
| `name`      | `string`                  | Name for the gateway.                                    | No       |
| `label`     | `map`                     | Labels for the gateway.                                  | No       |
| `hub`       | `string`                  | Hub for the component (overrides top level hub setting). | No       |
| `tag`       | `TypeInterface`           | Tag for the component (overrides top level tag setting). | No       |
| `k8s`       | `KubernetesResourcesSpec` | Kubernetes resource spec.                                | No       |

###  egressGateways 



| Field       | Type                      | Description                                              | Required |
| ----------- | ------------------------- | -------------------------------------------------------- | -------- |
| `enabled`   | `TypeBoolValueForPB`      | Selects whether this gateway is installed.               | No       |
| `namespace` | `string`                  | Namespace for the gateway.                               | No       |
| `name`      | `string`                  | Name for the gateway.                                    | No       |
| `label`     | `map`                     | Labels for the gateway.                                  | No       |
| `hub`       | `string`                  | Hub for the component (overrides top level hub setting). | No       |
| `tag`       | `TypeInterface`           | Tag for the component (overrides top level tag setting). | No       |
| `k8s`       | `KubernetesResourcesSpec` | Kubernetes resource spec.                                | No       |

## unvalidatedValues



##  values 

### global



### base



### pilot



### telemetry



### istiodRemote



### gateways





# profile

```
查看profile
istioctl profile list
```



## default

```
[root@node01 profiles]# cat default.yaml 
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
metadata:
  namespace: istio-system
spec:
  hub: docker.io/istio
  tag: 1.11.2

  # You may override parts of meshconfig by uncommenting the following lines.
  meshConfig:
    defaultConfig:
      proxyMetadata: {}
    enablePrometheusMerge: true
    # Opt-out of global http2 upgrades.
    # Destination rule is used to opt-in.
    # h2_upgrade_policy: DO_NOT_UPGRADE

  # Traffic management feature
  components:
    base:
      enabled: true
    pilot:
      enabled: true

    # Istio Gateway feature
    ingressGateways:
    - name: istio-ingressgateway
      enabled: true
    egressGateways:
    - name: istio-egressgateway
      enabled: false

    # Istio CNI feature
    cni:
      enabled: false
    
    # Remote and config cluster configuration for an external istiod
    istiodRemote:
      enabled: false

  # Global values passed through to helm global.yaml.
  # Please keep this in sync with manifests/charts/global.yaml
  values:
    global:
      istioNamespace: istio-system
      istiod:
        enableAnalysis: false
      logging:
        level: "default:info"
      logAsJson: false
      pilotCertProvider: istiod
      jwtPolicy: third-party-jwt
      proxy:
        image: proxyv2
        clusterDomain: "cluster.local"
        resources:
          requests:
            cpu: 100m
            memory: 128Mi
          limits:
            cpu: 2000m
            memory: 1024Mi
        logLevel: warning
        componentLogLevel: "misc:error"
        privileged: false
        enableCoreDump: false
        statusPort: 15020
        readinessInitialDelaySeconds: 1
        readinessPeriodSeconds: 2
        readinessFailureThreshold: 30
        includeIPRanges: "*"
        excludeIPRanges: ""
        excludeOutboundPorts: ""
        excludeInboundPorts: ""
        autoInject: enabled
        tracer: "zipkin"
      proxy_init:
        image: proxyv2
        resources:
          limits:
            cpu: 2000m
            memory: 1024Mi
          requests:
            cpu: 10m
            memory: 10Mi
      # Specify image pull policy if default behavior isn't desired.
      # Default behavior: latest images will be Always else IfNotPresent.
      imagePullPolicy: ""
      operatorManageWebhooks: false
      tracer:
        lightstep: {}
        zipkin: {}
        datadog: {}
        stackdriver: {}
      imagePullSecrets: []
      oneNamespace: false
      defaultNodeSelector: {}
      configValidation: true
      multiCluster:
        enabled: false
        clusterName: ""
      omitSidecarInjectorConfigMap: false
      network: ""
      defaultResources:
        requests:
          cpu: 10m
      defaultPodDisruptionBudget:
        enabled: true
      priorityClassName: ""
      useMCP: false
      sds:
        token:
          aud: istio-ca
      sts:
        servicePort: 0
      meshNetworks: {}
      mountMtlsCerts: false
    base:
      enableCRDTemplates: false
      validationURL: ""
    pilot:
      autoscaleEnabled: true
      autoscaleMin: 1
      autoscaleMax: 5
      replicaCount: 1
      image: pilot
      traceSampling: 1.0
      env: {}
      cpu:
        targetAverageUtilization: 80
      nodeSelector: {}
      keepaliveMaxServerConnectionAge: 30m
      enableProtocolSniffingForOutbound: true
      enableProtocolSniffingForInbound: true
      deploymentLabels:
      configMap: true

    telemetry:
      enabled: true
      v2:
        enabled: true
        metadataExchange:
          wasmEnabled: false
        prometheus:
          wasmEnabled: false
          enabled: true
        stackdriver:
          enabled: false
          logging: false
          monitoring: false
          topology: false
          configOverride: {}

    istiodRemote:
      injectionURL: ""
      
    gateways:
      istio-egressgateway:
        zvpn: {}
        env: {}
        autoscaleEnabled: true
        type: ClusterIP
        name: istio-egressgateway
        secretVolumes:
          - name: egressgateway-certs
            secretName: istio-egressgateway-certs
            mountPath: /etc/istio/egressgateway-certs
          - name: egressgateway-ca-certs
            secretName: istio-egressgateway-ca-certs
            mountPath: /etc/istio/egressgateway-ca-certs

      istio-ingressgateway:
        autoscaleEnabled: true
        type: LoadBalancer
        name: istio-ingressgateway
        zvpn: {}
        env: {}
        secretVolumes:
          - name: ingressgateway-certs
            secretName: istio-ingressgateway-certs
            mountPath: /etc/istio/ingressgateway-certs
          - name: ingressgateway-ca-certs
            secretName: istio-ingressgateway-ca-certs
            mountPath: /etc/istio/ingressgateway-ca-certs
```



## demo

```
[root@node01 profiles]# cat demo.yaml 
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  meshConfig:
    accessLogFile: /dev/stdout
  components:
    egressGateways:
    - name: istio-egressgateway
      enabled: true
      k8s:
        resources:
          requests:
            cpu: 10m
            memory: 40Mi

    ingressGateways:
    - name: istio-ingressgateway
      enabled: true
      k8s:
        resources:
          requests:
            cpu: 10m
            memory: 40Mi
        service:
          ports:
            ## You can add custom gateway ports in user values overrides, but it must include those ports since helm replaces.
            # Note that AWS ELB will by default perform health checks on the first port
            # on this list. Setting this to the health check port will ensure that health
            # checks always work. https://github.com/istio/istio/issues/12503
            - port: 15021
              targetPort: 15021
              name: status-port
            - port: 80
              targetPort: 8080
              name: http2
            - port: 443
              targetPort: 8443
              name: https
            - port: 31400
              targetPort: 31400
              name: tcp
              # This is the port where sni routing happens
            - port: 15443
              targetPort: 15443
              name: tls

    pilot:
      k8s:
        env:
          - name: PILOT_TRACE_SAMPLING
            value: "100"
        resources:
          requests:
            cpu: 10m
            memory: 100Mi

  values:
    global:
      proxy:
        resources:
          requests:
            cpu: 10m
            memory: 40Mi

    pilot:
      autoscaleEnabled: false

    gateways:
      istio-egressgateway:
        autoscaleEnabled: false
      istio-ingressgateway:
        autoscaleEnabled: false
```



## empty

```
[root@node01 profiles]# cat empty.yaml 
# The empty profile has everything disabled
# This is useful as a base for custom user configuration
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  components:
    base:
      enabled: false
    pilot:
      enabled: false
    ingressGateways:
    - name: istio-ingressgateway
      enabled: false
```



## external

```
[root@node01 profiles]# cat external.yaml 
# The external profile is used to configure a mesh using an external control plane.
# Only the injector mutating webhook configuration is installed.
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  components:
    base:
      enabled: false
    pilot:
      enabled: false
    ingressGateways:
    - name: istio-ingressgateway
      enabled: false
    istiodRemote:
      enabled: true
  values:
    global:
      externalIstiod: true
      omitSidecarInjectorConfigMap: true
      configCluster: false
    pilot:
      configMap: false
```



## minimal

```
[root@node01 profiles]# cat minimal.yaml 
# The minimal profile will install just the core control plane
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  components:
    ingressGateways:
    - name: istio-ingressgateway
      enabled: false
```



## openshift

```
[root@node01 profiles]# cat openshift.yaml 
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  components:
    cni:
      enabled: true
      namespace: kube-system
      k8s:
        overlays:
          - kind: DaemonSet
            name: istio-cni-node
            patches:
              - path: spec.template.spec.containers[0].securityContext.privileged
                value: true
  values:
    cni:
      cniBinDir: /var/lib/cni/bin
      cniConfDir: /etc/cni/multus/net.d
      chained: false
      cniConfFileName: "istio-cni.conf"
      excludeNamespaces:
       - istio-system
       - kube-system
      logLevel: info
    sidecarInjectorWebhook:
      injectedAnnotations:
        k8s.v1.cni.cncf.io/networks: istio-cni
```



## preview

```
[root@node01 profiles]# cat preview.yaml 
# The preview profile contains features that are experimental.
# This is intended to explore new features coming to Istio.
# Stability, security, and performance are not guaranteed - use at your own risk.
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  meshConfig:
    defaultConfig:
      proxyMetadata:
        # Enable Istio agent to handle DNS requests for known hosts
        # Unknown hosts will automatically be resolved using upstream dns servers in resolv.conf
        ISTIO_META_DNS_CAPTURE: "true"
        # Enable dynamic bootstrap generation.
        BOOTSTRAP_XDS_AGENT: "true"
  values:
    telemetry:
      v2:
        metadataExchange:
          wasmEnabled: true
        prometheus:
          wasmEnabled: true
```





# 综合案例































```
cat <<EOF > cluster2.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  profile: external
  values:
    pilot:
      configMap: false
    istiodRemote:
      injectionURL: https://192.168.229.100:15017/inject
    base:
      validationURL: https://192.168.229.100:15017/validate
    global:
      meshID: mesh1
      multiCluster:
        enabled: true
        clusterName: cluster2
      network: network2
      remotePilotAddress: 192.168.229.100
      externalIstiod: true
      omitSidecarInjectorConfigMap: true
      #configCluster: false
      caAddress: https://192.168.229.100:15012
    pilot:
      autoscaleEnabled: false
    gateways:
      istio-egressgateway:
        autoscaleEnabled: false
      istio-ingressgateway:
        autoscaleEnabled: false
  meshConfig:
    accessLogFile: /dev/stdout
    enableTracing: true
  components:
    base:
      enabled: false
    pilot:
      enabled: false
    egressGateways:
    - name: istio-egressgateway
      enabled: true
      k8s:
        env:
          - name: ISTIO_META_ROUTER_MODE
            value: "sni-dnat"
          - name: ISTIO_META_REQUESTED_NETWORK_VIEW
            value: network2
        resources:
          requests:
            cpu: 10m
            memory: 40Mi
    istiodRemote:
      enabled: true
    ingressGateways:
    - name: istio-ingressgateway
      enabled: true
      k8s:
        env:
          - name: ISTIO_META_ROUTER_MODE
            value: "sni-dnat"
          - name: ISTIO_META_REQUESTED_NETWORK_VIEW
            value: network2
        resources:
          requests:
            cpu: 10m
            memory: 40Mi
        service:
          ports:
            - port: 15021
              targetPort: 15021
              name: status-port
            - port: 80
              targetPort: 8080
              name: http2
            - port: 443
              targetPort: 8443
              name: https
            - port: 31400
              targetPort: 31400
              name: tcp
            - port: 15443
              targetPort: 15443
              name: tls
    - name: istio-eastwestgateway
      label:
        istio: eastwestgateway
        app: istio-eastwestgateway
        topology.istio.io/network: network2
      enabled: true
      k8s:
        resources:
          requests:
            cpu: 10m
            memory: 40Mi
        env:
          - name: ISTIO_META_ROUTER_MODE
            value: "sni-dnat"
          - name: ISTIO_META_REQUESTED_NETWORK_VIEW
            value: network2
        service:
          ports:
            - name: status-port
              port: 15021
              targetPort: 15021
            - name: tls
              port: 15443
              targetPort: 15443
            - name: tls-istiod
              port: 15012
              targetPort: 15012
            - name: tls-webhook
              port: 15017
              targetPort: 15017
EOF
```

