# 什么是slime

 Slime是基于Istio的智能网格管理器。通过Slime，我们可以定义动态的服务治理策略，从而达到自动便捷使用Istio和Envoy高阶功能的目的。 



# 为什么选择Slime

服务网格作为新一代微服务架构，采用 Istio+Envoy ，实现了业务逻辑和微服务治理逻辑的物理解耦，降低微服务框架的开发与运维成本。

Istio 可以实现版本分流、灰度发布、负载均衡等功能，但是在本地限流，黑白名单，降级等微服务治理的高阶特性存在缺陷。起初 Istio 给出的解决方案是 Mixer，将这些原本属于数据面的功能上升到 Mixer Adapter 中。这样做虽然解决了功能扩展的问题，但集中式的架构遭到了不少关注者对其性能的质疑。最终，Istio 在新版本中自断其臂，弃用了 Mixer，这就使得高阶功能的扩展成为目前版本的一块空白。

另一方面 Istio 配置是全量推送的。这就意味着在大规模的网格场景下需推送海量配置。为了减少推送配置量，用户不得不事先搞清楚服务间的依赖关系，配置 SidecarScope做配置隔离，而这无疑增加了运维人员的心智负担，易用性和性能成为不可兼得的鱼和熊掌。

针对 Istio 目前的一些弊端，我们推出了Slime项目。该项目是基于 k8s-operator 实现的，作为 Istio 的 CRD 管理器，**可以无缝对接 Istio，无需任何的定制化改造**

Slime 内部采用了模块化的架构。目前包含了三个非常实用的子模块。

**配置懒加载**：无须配置SidecarScope，自动按需加载配置和服务发现信息 ，解决了全量推送的问题。

**Http插件管理**：使用新的的CRD pluginmanager/envoyplugin包装了可读性及可维护性差的envoyfilter，使得插件扩展更为便捷。

**自适应限流**：实现了本地限流，同时可以结合监控信息自动调整限流策略，填补了 Istio 限流功能的短板。

# 架构

Slime架构主要分为三大块：

1. slime-boot，部署slime-module的operator组件，通过slime-boot可以便捷快速的部署slime-module。
2. slime-controller，slime-module的核心线程，感知SlimeCRD并转换为IstioCRD。
3. slime-metric，slime-module的监控获取线程，用于感知服务状态，slime-controller会根据服务状态动态调整服务治理规则。

![arch](images\arch.png)

Slime架构主要分为三大块：

1. slime-boot，部署Slime（slime-modules和slime-framework）的Operator组件。
2. slime-modules，Slime的核心线程，感知SlimeCRD并转换为IstioCRD，并触发内置的其他逻辑。
3. slime-framework，作为底座，为modules提供通用的基础能力。

![slime-arch-v2](images\slime-arch-v2.png)





 使用者将服务治理策略定义在CRD的spec中，同时，slime-metric从prometheus获取关于服务状态信息，并将其记录在CRD的metricStatus中。slime-module的控制器通过metricStatus感知服务状态后，将服务治理策略中将对应的监控项渲染出，并计算策略中的算式，最终生成治理规则、

![policy_zh](images\policy_zh.png)



# 安装slime-boot

 在使用slime之前，需要安装slime-boot，通过slime-boot，可以方便的安装和卸载slime模块。 执行如下命令： 

```
kubectl create ns mesh-operator
kubectl apply -f crds.yaml -n mesh-operator
kubectl apply -f deployment_slime-boot.yaml -n mesh-operator

```

# 安装Prometheus

# 懒加载

## 特点

1. 支持1.8+的Istio版本，无侵入性，[版本适配详情](https://github.com/slime-io/slime/issues/145)
2. 可自动对接整个服务网格
3. 兜底转发过程支持Istio所有流量治理能力
4. 兜底逻辑简单，与服务数量无关，无性能问题
5. 支持为服务手动或自动启用懒加载
6. 支持Accesslog和Prometheus等多种动态服务依赖获取方式
7. 支持添加静态服务依赖关系，动静依赖关系结合，功能全面

## 架构

![lazyload-architecture-20211222_zh](images\lazyload-architecture-20211222_zh.png)



## 安装和使用

1. 根据global-sidecar的部署模式不同，该模块目前分为两种模式：
   - Cluster模式：使用cluster级别的global-sidecar：集群唯一global-sidecar应用
   - Namespace模式：使用namespace级别的global-sidecar：每个使用懒加载的namespace下一个global-sidecar应用
2. 根据服务依赖关系指标来源不同，该模块分为两种模式：
   - Accesslog模式：global-sidecar通过入流量拦截，生成包含服务依赖关系的accesslog
   - Prometheus模式：业务应用在完成访问后，生成metric，上报prometheus。此种模式需要集群对接Prometheus

总的来说，Lazyload模块有4种使用模式，较为推荐Cluster+Accesslog模式。

## 特性介绍

- 可基于Accesslog开启懒加载
- 支持为服务手动或自动启用懒加载
- 支持自定义兜底流量分派
- 支持添加静态服务依赖关系
- 支持自定义服务依赖别名
- 日志输出到本地并轮转

## ServiceFence说明

ServiceFence可以看作是针对某一服务的Sidecar资源，区别是ServiceFence不仅会根据依赖关系生成Sidecar资源，同时会根据VirtualService规则判断服务的真实后端，并自动扩大Fence的范围。

例如，c.default.svc.cluster.local在fence中。此时有一条路由规则的host为c.default.svc.cluster.local，其destinatoin为d.default.svc.cluster.local，那么d服务也会被自动扩充到Fence中。

![ll](images\ll.png)

## 安装和使用

### Cluster模式

在该模式下，服务网格中的所有namespace都可以使用懒加载，无需像Namespace模式一样，显式指定使用懒加载的命令空间列表。

该模式会部署一个global-sidecar应用，位于lazyload controller的namespace下，默认为mesh-operator。所有兜底请求都会发到这个global-sidecar应用。

#### Accesslog

指标来源为global-sidecar的accesslog。

slimeboot_cluster_accesslog.yaml

```
---
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  image:
    pullPolicy: IfNotPresent
    repository: docker.io/slimeio/slime-lazyload
    tag: v0.3.0_linux_amd64
  module:
    - name: lazyload 
      kind: lazyload 
      enable: true
      general:
        wormholePort: 
          - "9080"
      global:
        misc:
          globalSidecarMode: cluster 
          metricSourceType: accesslog
  component:
    globalSidecar:
      enable: true
      sidecarInject:
        enable: true
        mode: pod
        labels:
          sidecar.istio.io/inject: "true"
      resources:
        requests:
          cpu: 200m
          memory: 200Mi
        limits:
          cpu: 400m
          memory: 400Mi
      image:
        repository: docker.io/slimeio/slime-global-sidecar
        tag: v0.2.0-1b93bf7
      probePort: 8080
```



#### Prometheus

指标来源为Prometheus。

slimeboot_cluster_prometheus.yaml

```
---
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-lazyload
    tag: v0.3.0_linux_amd64
  module:
    - name: lazyload 
      kind: lazyload 
      enable: true
      general: 
        wormholePort: 
          - "9080"
      global:
        misc:
          globalSidecarMode: cluster 
      metric:
        prometheus:
          address: http://prometheus.istio-system:9090
          handlers:
            destination:
              query:
              sum(istio_requests_total{source_app="$source_app",reporter="destination"})by(destination_service)
              type: Group
  component:
    globalSidecar:
      enable: true
      sidecarInject:
        enable: true 
        mode: pod
        labels: 
          sidecar.istio.io/inject: "true"
      resources:
        requests:
          cpu: 200m
          memory: 200Mi
        limits:
          cpu: 400m
          memory: 400Mi
      image:
        repository: docker.io/slimeio/slime-global-sidecar
        tag: v0.2.0-1b93bf7
```



### Namespace模式

该模式需要显式指定使用懒加载的命名空间列表，位置是SlimeBoot的`spec.module.general.namespace`。该模式会在每个打算启用懒加载的namespace下部署一个global-sidecar应用。每个namespace的兜底请求都会发到同namespace下的global-sidecar应用。

#### accesslog

##### mode: namespace

slimeboot_namespace_accesslog-mode-namespace.yaml

```
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-lazyload
    tag: v0.3.0_linux_amd64
  module:
    - name: lazyload
      kind: lazyload 
      enable: true
      general: 
        wormholePort:
          - "9080"
        namespace: 
          - istio
      global:
        misc:
          globalSidecarMode: namespace 
          metricSourceType: accesslog 
  component:
    globalSidecar:
      enable: true
      sidecarInject:
        enable: true 
        mode: namespace
      resources:
        requests:
          cpu: 200m
          memory: 200Mi
        limits:
          cpu: 400m
          memory: 400Mi
      image:
        repository: docker.io/slimeio/slime-global-sidecar
        tag: v0.2.0-1b93bf7
```

##### mode: pod

slimeboot_namespace_accesslog-mode-pod.yaml

```
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-lazyload
    tag: v0.3.0_linux_amd64
  module:
    - name: lazyload
      kind: lazyload 
      enable: true
      general: 
        wormholePort:
          - "9080"
        namespace: 
          - istio
      global:
        misc:
          globalSidecarMode: namespace 
          metricSourceType: accesslog 
  component:
    globalSidecar:
      enable: true
      sidecarInject:
        enable: true 
        mode: pod
        labels:
          sidecar.istio.io/inject: "true"
      resources:
        requests:
          cpu: 200m
          memory: 200Mi
        limits:
          cpu: 400m
          memory: 400Mi
      image:
        repository: docker.io/slimeio/slime-global-sidecar
        tag: v0.2.0-1b93bf7
```



#### prometheus metric

指标来源为Prometheus。

##### mode: namespace

slimeboot_namespace_prometheus-mode-namespace.yaml

```
---
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-lazyload
    tag: v0.3.0_linux_amd64
  module:
    - name: lazyload 
      kind: lazyload 
      enable: true
      general: 
        wormholePort: 
          - "9080"
      global:
        misc:
          globalSidecarMode: namespace 
      metric:
        prometheus:
          address: http://prometheus.istio-system:9090
          handlers:
            destination:
              query:
              sum(istio_requests_total{source_app="$source_app",reporter="destination"})by(destination_service)
              type: Group
  component:
    globalSidecar:
      enable: true
      sidecarInject:
        enable: true 
        mode: namespace
      resources:
        requests:
          cpu: 200m
          memory: 200Mi
        limits:
          cpu: 400m
          memory: 400Mi
      image:
        repository: docker.io/slimeio/slime-global-sidecar
        tag: v0.2.0-1b93bf7
```

##### mode: pod

slimeboot_namespace_prometheus-mode-pod.yaml

```
---
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-lazyload
    tag: v0.3.0_linux_amd64
  module:
    - name: lazyload 
      kind: lazyload 
      enable: true
      general: 
        wormholePort: 
          - "9080"
      global:
        misc:
          globalSidecarMode: namespace 
      metric:
        prometheus:
          address: http://prometheus.istio-system:9090
          handlers:
            destination:
              query:
              sum(istio_requests_total{source_app="$source_app",reporter="destination"})by(destination_service)
              type: Group
  component:
    globalSidecar:
      enable: true
      sidecarInject:
        enable: true 
        mode: pod
        labels: 
          sidecar.istio.io/inject: "true"
      resources:
        requests:
          cpu: 200m
          memory: 200Mi
        limits:
          cpu: 400m
          memory: 400Mi
      image:
        repository: docker.io/slimeio/slime-global-sidecar
        tag: v0.2.0-1b93bf7
```

##### 

最新镜像

slimeio/slime-lazyload:v0.2.5-6e0f9e6



 使用Slime的配置懒加载功能需打开Fence模块，同时安装global-sidecar, pilot等附加组件 

slime/lazy-load-deployment.yaml

kubectl apply -f lazy-load-deployment.yaml -n mesh-operator

```
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-lazyload
    tag: v0.2.0-alpha
  resources:
    limits:
      cpu: 200m
      memory: 200Mi
    requests:
      cpu: 20m
      memory: 20Mi 
  module:
    - name: lazyload
      fence:
        enable: true
        wormholePort: 
          - "9080" 
      metric:
        prometheus:
          address: http://prometheus.istio-system:9090 
          handlers:
            destination:
              query: |
                sum(istio_requests_total{source_app="$source_app",reporter="destination"})by(destination_service)
              type: Group
  component:
    globalSidecar:
      enable: true
      type: namespaced
      namespace:
        - slime
      resources:
        requests:
          cpu: 20m
          memory: 20Mi
        limits:
          cpu: 200m
          memory: 200Mi
    pilot:
      enable: true
      resources:
        requests:
          cpu: 20m
          memory: 20Mi
        limits:
          cpu: 200m
          memory: 200Mi
      image:
        repository: docker.io/slimeio/pilot
        tag: global-pilot-v0.0.2-a85b00
```

### 特性介绍

#### 基于Accesslog开启懒加载

指定SlimeBoot CR资源中`spec.module.global.misc.metricSourceType`等于`accesslog`会使用Accesslog获取服务调用关系，等于`prometheus`则使用Prometheus。

使用Accesslog获取服务调用关系的大概过程：

- slime-boot在创建global-sidecar时，发现`metricSourceType: accesslog`，额外生成一个configmap，内容是包含lazyload controller处理accesslog的地址信息的static_resources。再通过一个envoyfilter，将static_resources加入global-sidecar配置中，使得global-sidecar的accesslog会发送到lazyload controller
- global-sidecar完成兜底转发时会生成accesslog，包含了调用方和被调用方服务信息。global-sidecar将信息发送给lazyload controller
- lazyload controller分析accesslog，获取到新的服务调用关系

随后的过程，就是修改servicefence和sidecar，和处理prometheus metric的过程一致。

### 支持为服务手动或自动启用懒加载

支持通过`autoFence`参数，指定启用懒加载是手动模式、自动模式。这里的启用懒加载，指的是创建serviceFence资源，从而生成Sidecar CR。

支持通过`defaultFence`参数，指定自动模式下，是否全局启用懒加载。

配置方式如下

```
---
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  module:
    - name: lazyload
      kind: lazyload
      enable: true
      general:
        autoFence: true # true为自动模式，false为手动模式，默认为手动模式
        defaultFence: true # 自动模式下默认行为，true为创建servicefence，false为不创建，默认不创建
  # ...
```

#### 自动模式

当`autoFence`参数为`true`时，进入自动模式。自动模式下，启用懒加载的服务范围，通过三个维度调整。

Service级别 - label `slime.io/serviceFenced`

- `false`： 不自动启用
- `true`： 自动启用
- 其他值或缺省： 使用Namespace级别配置

Namespace级别 - label `slime.io/serviceFenced`

- `false`： 该namespace下的所有服务都不自动启用
- `true`：该namespace下的所有服务都自动启用
- 其他值或缺省： 使用全局级别配置

全局级别 - lazyload的`defaultFence`参数

- `false`： 全局不自动启用
- `true`：全局自动启用

优先级：Service级别 > Namespace级别 > 全局级别

注：对于自动生成的ServiceFence资源 ，会通过标准Label `app.kubernetes.io/created-by=fence-controller`来记录，实现了状态关联变更。 而不匹配该Label的ServiceFence资源，视为手动配置，不受以上Label影响。

**举例**

> Namespace `testns`下有三个服务： `svc1`, `svc2`, `svc3`

- 当`autoFence`为`true`且`defaultFence`为`true`时，自动生成以上三个服务的ServiceFence
- 给Namespace testns加上Label `slime.io/serviceFenced: "false"`， 所有ServiceFence消失
- 给`svc1`打上 `slime.io/serviceFenced: "true"` label： 服务`svc1`的ServiceFence创建
- 删掉Namespace和Service上的label：恢复三个ServiceFence

### 自定义兜底流量分派

lazyload/fence默认会将envoy无法匹配路由（缺省）的流量兜底发送到global sidecar，应对短暂服务数据缺失的问题，这是“懒加载”所必然面对的。 该方案因为技术细节上的局限性，对于目标（如域名）是集群外的流量，无法正常处理，详见 [[Configuration Lazy Loading\]: Failed to access external service #3](https://github.com/slime-io/slime/issues/3)。

基于这个背景，设计了本特性，同时也能用于更灵活的业务场景。 大致思路是通过域名匹配的方式将不同的缺省流量分派到不同的目标做正确处理。

配置样例：

```
module:
  - name: lazyload
    kind: lazyload
    enable: true
    general:
      wormholePort:
      - "80"
      - "8080"
      dispatches:  # new field
      - name: 163
        domains:
        - "www.163.com"
        cluster: "outbound|80||egress1.testns.svc.cluster.local"  # standard istio cluster format: <direction>|<svcPort>|<subset>|<svcFullName>, normally direction is outbound and subset is empty      
      - name: baidu
        domains:
        - "*.baidu.com"
        - "baidu.*"
        cluster: "{{ (print .Values.foo \".\" .Values.namespace ) }}"  # you can use template to construct cluster dynamically
      - name: sohu
        domains:
        - "*.sohu.com"
        - "sodu.*"
        cluster: "_GLOBAL_SIDECAR"  # a special name which will be replaced with actual global sidecar cluster
      - name: default
        domains:
        - "*"
        cluster: "PassthroughCluster"  # a special istio cluster which will passthrough the traffic according to orgDest info. It's the default behavior of native istio.
```

> 在本例中，我们把一部分流量分派给了指定的cluster； 另一部分让它走global sidecar； 然后对其余的流量，让它保持原生istio的行为： passthrough

**注意**：

- 自定义分派场景，如果希望保持原有逻辑 “其他所有未定义流量走global sidecar” 的话，需要显式配置如上的倒数第二条

### 静态服务依赖关系添加

懒加载除了从slime metric处根据动态指标更新服务依赖关系，还支持通过`serviceFence.spec`添加静态服务依赖关系。支持三种细分场景：依赖某个服务、依赖某个namespace所有服务、依赖具有某个label的所有服务。

值得注意的是，静态服务依赖关系与动态服务依赖关系一样，支持根据VirtualService规则判断服务的真实后端，并自动扩大Fence的范围。详见[ServiceFence说明](https://github.com/slime-io/slime/blob/master/staging/src/slime.io/slime/modules/lazyload/README_zh.md#ServiceFence说明)

#### 依赖某个服务

适用于启用懒加载的服务静态依赖另外一个或多个服务的场景，可以在初始化时直接将配置加入sidecar crd。

下面的样例中，为启用懒加载的服务添加对`reviews.default`服务的静态依赖关系。

```
# servicefence
spec:
  enable: true
  host:
    reviews.default.svc.cluster.local: # static dependenct of reviews.default service
      stable:

# related sidecar
spec:
  egress:
  - hosts:
    - '*/reviews.default.svc.cluster.local'
    - istio-system/*
    - mesh-operator/*
```

#### 依赖某个namespace所有服务

适用于启用懒加载的服务静态依赖另外一个或多个namespace中所有服务的场景，可以在初始化时直接将配置加入sidecar crd。

下面的样例中，为启用懒加载的服务添加对`test`命名空间中所有服务的静态依赖关系。

```
# servicefence
spec:
  enable: true
  host:
    test/*: {} # static dependency of all services in test namespace

# related sidecar
spec:
  egress:
  - hosts:
    - test/*
    - istio-system/*
    - mesh-operator/*
```

#### 依赖具有某个label的所有服务

适用于启用懒加载的服务静态依赖具有某个label或多个label的所有服务的场景，可以在初始化时直接将配置加入sidecar crd。

下面的样例中，为启用懒加载的服务添加拥有`app=details`的所有服务，以及拥有`app=reviews, group=default`的所有服务的静态依赖关系。

```
# servicefence
spec:
  enable: true
  labelSelector: # Match service label, multiple selectors are 'or' relationship
    - selector:
        app: details
    - selector: # labels in one selector are 'and' relationship
        app: reviews
        group: default

# related sidecar
spec:
  egress:
  - hosts:
    - '*/details.default.svc.cluster.local' # with label "app=details"
    - '*/details.test.svc.cluster.local' # with label "app=details"
    - '*/reviews.default.svc.cluster.local' # with label "app=details" and "group=default"
    - istio-system/*
    - mesh-operator/*
```

### 支持自定义服务依赖别名

在某些场景，我们希望懒加载根据已知的服务依赖，添加一些额外的服务依赖进去。

用户可以通过配置`general.domainAliases`，提供自定义的转换关系，实现需求。`general.domainAliases`包含多个`domainAlias`，每个`domainAlias`由匹配规则`pattern`和转换规则`templates`组成。`pattern`只包含一个匹配规则，`templates`则可以包含多个转换规则。

举个例子，我们希望添加`<svc>.<ns>.svc.cluster.local`时，额外添加`<ns>.<svc>.mailsaas`的服务依赖，则可以这么配置

```
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  module:
    - name: lazyload-test
      kind: lazyload
      enable: true
      general:
        wormholePort: # replace to your application service ports, and extend the list in case of multi ports
          - "9080"
        domainAliases: 
          - pattern: '(?P<service>[^\.]+)\.(?P<namespace>[^\.]+)\.svc\.cluster\.local$'
            templates:
              - "$namespace.$service.mailsaas"
  #...
```

对应的servicefence会这样

```
apiVersion: microservice.slime.io/v1alpha1
kind: ServiceFence
metadata:
  name: ratings
  namespace: default
spec:
  enable: true
  host:
    details.default.svc.cluster.local: # static dependent service
      stable: {}
status:
  domains:
    default.details.mailsaas: # static dependent service converted result
      hosts:
      - default.details.mailsaas
    default.productpage.mailsaas: # dynamic dependent service converted result
      hosts:
      - default.productpage.mailsaas
    details.default.svc.cluster.local:
      hosts:
      - details.default.svc.cluster.local
    productpage.default.svc.cluster.local:
      hosts:
      - productpage.default.svc.cluster.local
  metricStatus:
    '{destination_service="productpage.default.svc.cluster.local"}': "1" # dynamic dependent service
```

sidecar则是这样

```
apiVersion: networking.istio.io/v1beta1
kind: Sidecar
metadata:
  name: ratings
  namespace: default
spec:
  egress:
  - hosts:
    - '*/default.details.mailsaas' # static dependent service converted result
    - '*/default.productpage.mailsaas' # dynamic dependent service converted result
    - '*/details.default.svc.cluster.local'
    - '*/productpage.default.svc.cluster.local'
    - istio-system/*
    - mesh-operator/*
  workloadSelector:
    labels:
      app: ratings
```

### 日志输出到本地并轮转

slime的日志默认输出到标准输出，指定SlimeBoot CR资源中`spec.module.global.log.logRotate`等于`true`会将日志输出到本地并启动日志轮转，不再输出到标准输出。

轮转配置也是可调整的，默认的配置如下，可以通过显示指定logRotateConfig中的各个值进行覆盖。

```
spec:
  module:
    - name: lazyload # custom value
      kind: lazyload # should be "lazyload"
      enable: true
      general: # replace previous "fence" field
        wormholePort: # replace to your application svc ports
          - "9080"
      global:
        log:
          logRotate: true
          logRotateConfig:
            filePath: "/tmp/log/slime.log"
            maxSizeMB: 100
            maxBackups: 10
            maxAgeDay: 10
            compress: true
```

通常需要配合存储卷使用，在存储卷准备完毕后，指定SlimeBoot CR资源中的`spec.volumes`和`spec.containers.slime.volumeMounts`来显示将存储卷挂载到日志本地文件所在的路径。

以下是基于minikube kubernetes场景下的完整demo

#### 创建存储卷

基于/mnt/data路径创建hostpath类型的存储卷

```
# hostPath pv for minikube demo
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: lazyload-claim
  namespace: mesh-operator
spec:
  storageClassName: manual
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 3Gi
---
apiVersion: v1
kind: PersistentVolume
metadata:
  name: lazyload-volumn
  labels:
    type: local
spec:
  storageClassName: manual
  capacity:
    storage: 5Gi
  accessModes:
    - ReadWriteOnce
  hostPath:
    path: "/mnt/data"
```

#### 在SlimeBoot中声明挂载信息

在SlimeBoot CR资源中指定了存储卷会挂载到pod的"/tmp/log"路径，这样slime的日志会持久化到/mnt/data路径下，并且会自动轮转。

```
---
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-lazyload
    tag: master-e5f2d83-dirty_1b68486
  module:
    - name: lazyload # custom value
      kind: lazyload # should be "lazyload"
      enable: true
      general: # replace previous "fence" field
        wormholePort:
          - "9080"
      global:
        log:
          logRotate: true
          logRotateConfig:
            filePath: "/tmp/log/slime.log"
            maxSizeMB: 100
            maxBackups: 10
            maxAgeDay: 10
            compress: true
#...
  volumes:
    - name: lazyload-storage
      persistentVolumeClaim:
        claimName: lazyload-claim
  containers:
    slime:
      volumeMounts:
        - mountPath: "/tmp/log"
          name: lazyload-storage
```

slimeboot_logrotate.yaml

```
---
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-lazyload
    tag: v0.3.0_linux_amd64
  module:
    - name: lazyload # custom value
      kind: lazyload # should be "lazyload"
      enable: true
      general: # replace previous "fence" field
        wormholePort:
          - "80"
          - "9080"
      global:
        misc:
          globalSidecarMode: cluster
          metricSourceType: accesslog
        log:
          logRotate: true
          logRotateConfig:
            filePath: "/tmp/log/slime.log"
            maxSizeMB: 100
            maxBackups: 10
            maxAgeDay: 10
            compress: true
  component:
    globalSidecar:
      enable: true
      type: cluster
      sidecarInject:
        enable: true # should be true
        mode: pod # if type = cluster, can only be "pod"; if type = namespace, can be "pod" or "namespace"
        labels: # optional, used for sidecarInject.mode = pod
          sidecar.istio.io/inject: "true"
      resources:
        requests:
          cpu: 200m
          memory: 200Mi
        limits:
          cpu: 400m
          memory: 400Mi
      image:
        repository: docker.io/slimeio/slime-global-sidecar
        tag: v0.2.0-1b93bf7
  volumes:
    - name: lazyload-storage
      persistentVolumeClaim:
        claimName: lazyload-claim
  containers:
    slime:
      volumeMounts:
        - mountPath: "/tmp/log"
          name: lazyload-storage
```



## 应用fence

slime/sf-productpage.yaml

kubectl apply -f sf-productpage.yaml -n slime

```
apiVersion: microservice.slime.io/v1alpha1
kind: ServiceFence
metadata:
  name: productpage
  namespace: slime
spec:
  enable: true
```

## Disable global-sidecar

slime/lazyload-disable-global-sidecar.yaml

kubectl apply -f lazyload-disable-global-sidecar.yaml -n mesh-operator

```
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-lazyload
    tag: v0.2.1
  resources:
    limits:
      cpu: 200m
      memory: 200Mi
    requests:
      cpu: 20m
      memory: 20Mi  
  module:
    - fence:
        enable: true
        wormholePort:
          - "9080"
      name: slime-fence
      metric:
        prometheus:
          address: http://prometheus.istio-system:9090
          handlers:
            destination:
              query: |
                sum(istio_requests_total{source_app="$source_app",reporter="destination"})by(destination_service)
              type: Group
```

## Use cluster unique global-sidecar

slime/lazyload-cluster-unique-global-sidecar.yaml

kubectl apply -f lazyload-cluster-unique-global-sidecar.yaml -n mesh-operator

```
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-lazyload
    tag: v0.2.1
  resources:
    limits:
      cpu: 200m
      memory: 200Mi
    requests:
      cpu: 20m
      memory: 20Mi 
  module:
    - fence:
        enable: true
        wormholePort:
          - "9080"
      name: slime-fence
      metric:
        prometheus:
          address: http://prometheus.istio-system:9090
          handlers:
            destination:
              query: |
                sum(istio_requests_total{source_app="$source_app",reporter="destination"})by(destination_service)
              type: Group
  component:
    globalSidecar:
      enable: true
      type: cluster
    pilot:
      enable: true
      image:
        repository: docker.io/slimeio/pilot
        tag: global-pilot-v0.0.2-a85b00
```

名称空间下所有service启用懒加载

```
kubectl label ns slime slime.io/serviceFenced=true
```

> namespace `testns` has three services under it: `svc1`, `svc2`, `svc3`

- Label `testns` with `slime.io/serviceFenced=true`: Generate cr for the above three services
- Label `svc2` with `slime.io/serviceFenced=false`: only the cr for `svc1`, `svc3` remain
- Remove this label from `svc2`: restores three cr
- Remove `app.kubernetes.io/created-by=fence-controller` from the cr of `svc3`; remove the label on `testns`: only the cr of `svc3` remains

## 自定义兜底流量分派

slime/lazyload-traffic-dispatch.yaml

kubectl apply -f lazyload-traffic-dispatch.yaml -n mesh-operator

```
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: lazyload
  namespace: mesh-operator
spec:
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-lazyload
    tag: v0.2.1
  resources:
    limits:
      cpu: 200m
      memory: 200Mi
    requests:
      cpu: 20m
      memory: 20Mi 
  module:
    - fence:
        enable: true
        wormholePort:
          - "9080"
        dispatches:
        - name: 163
          domains:
          - "www.163.com"
          cluster: "outbound|80||egress1.testns.svc.cluster.local"
      name: slime-fence
      metric:
        prometheus:
          address: http://prometheus.istio-system:9090
          handlers:
            destination:
              query: |
                sum(istio_requests_total{source_app="$source_app",reporter="destination"})by(destination_service)
              type: Group
  component:
    globalSidecar:
      enable: true
      type: namespaced
      namespace:
        - slime
    pilot:
      enable: true
      image:
        repository: docker.io/slimeio/pilot
        tag: global-pilot-v0.0.2-a85b00
```

## 卸载

```
#!/bin/bash
if [[ "$#" -eq 0 ]]; then
  echo "No specified tag or commit. Use the latest tag."
  tag_or_commit=$(curl -s https://api.github.com/repos/slime-io/slime/tags | grep 'name' | cut -d\" -f4 | head -1)
  if [[ -z $tag_or_commit ]]; then
    echo "Failed to get the latest tag. Exited."
    exit 1
  fi
  echo "The Latest tag: $tag_or_commit."
else
  tag_or_commit=$1
  echo "Use specified tag or commit: $tag_or_commit"
fi

crds_url="https://raw.githubusercontent.com/slime-io/slime/$tag_or_commit/install/init/crds.yaml"
deployment_slimeboot_url="https://raw.githubusercontent.com/slime-io/slime/$tag_or_commit/install/init/deployment_slime-boot.yaml"
slimeboot_lazyload_url="https://raw.githubusercontent.com/slime-io/slime/$tag_or_commit/install/samples/lazyload/slimeboot_lazyload.yaml"

for i in $(kubectl get ns);do kubectl delete servicefence -n $i --all;done
kubectl delete -f "${slimeboot_lazyload_url}"
kubectl delete -f "${deployment_slimeboot_url}"
kubectl delete -f "${crds_url}"
kubectl delete ns mesh-operator
```



# 插件管理

## 安装

slime/plugin-deploy.yaml

kubectl apply -f plugin-deploy.yaml -n mesh-operator

```
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: plugin
  namespace: mesh-operator
spec:
  module:
    - name: plugin
      kind: plugin
      enable: true
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-plugin
    tag: v0.2.2
```

## 例子1

slime/pm-productpage.yaml

kubectl apply -f pm-productpage.yaml -n slime

```
apiVersion: microservice.slime.io/v1alpha1
kind: PluginManager
metadata:
  name: productpage
spec:
  workload_labels:
    app: productpage
  plugin:
  - enable: true
    name: envoy.filters.http.lua
    inline:
      settings:
        rate_limits:
        - actions:
          - header_value_match:
              descriptor_value: dv-productpage
              headers:
              - invert_match: false
                name: test
                safe_regex_match:
                  google_re2: {}
                  regex: test
          stage: 0
```



# 自适应限流

## 特点

1. 方便使用，只需提交`SmartLimiter`资源即可达到服务限流的目的。
2. 自适应限流，根据`pod`的资源使用量动态的触发限流规则。
3. 覆盖场景多，支持全局共享限流，全局均分限流，单机限流。

## 功能

1. 单机限流，每个pod单独的计数器
2. 全局共享限流，所有pod共享一个全局计数器
3. 全局均分限流，所有pod均分计数。

## 思路

为了让用户从复杂的`EnvoyFilter`配置中脱离出来，我们利用`kubernetes`的`CRD`机制定义了一套简便的`API`，即`kubernetes`内的`SmartLimiter`资源。用户只需要按照`SmartLimiter`的规范提交一个`CR`,就会在集群中自动生成一个`EnvoyFilter`

## 架构

自适应限流的主体架构分为两个部分，一部分包括`SmartLimiter`到`EnvoyFilter`的逻辑转化，另一部分包括集群内监控数据的获取，包括服务的`CPU`, `Memory`,`POD`数量等数据。

![SmartLimiter](images\SmartLimiter.png)

## 安装

slime/sl-deploy.yaml

kubectl apply -f sl-deploy.yaml -n mesh-operator

```
apiVersion: config.netease.com/v1alpha1
kind: SlimeBoot
metadata:
  name: smartlimiter
  namespace: mesh-operator
spec:
  image:
    pullPolicy: Always
    repository: docker.io/slimeio/slime-limiter
    tag: v0.2.1
  resources:
    limits:
      cpu: 200m
      memory: 200Mi
    requests:
      cpu: 20m
      memory: 20Mi
  module:
    - limiter:
        backend: 1
      enable: true
      metric:
        prometheus:
          address: http://prometheus.istio-system:9090
          handlers:
            cpu.sum:
              query: |
                sum(container_cpu_usage_seconds_total{namespace="$namespace",pod=~"$pod_name",image=""})
            cpu.max:
              query: |
                max(container_cpu_usage_seconds_total{namespace="$namespace",pod=~"$pod_name",image=""})
            rt99:
              query: |
                histogram_quantile(0.99, sum(rate(istio_request_duration_milliseconds_bucket{kubernetes_pod_name=~"$pod_name"}[2m]))by(le))
        k8s:
          handlers:
            - pod # inline
      name: limiter
```



指标

```
cpu:
总和：
sum(container_cpu_usage_seconds_total{namespace="$namespace",pod=~"$pod_name",image=""})
最大值：
max(container_cpu_usage_seconds_total{namespace="$namespace",pod=~"$pod_name",image=""})
limit:
container_spec_cpu_quota{pod=~"$pod_name"}

内存：
总和：
sum(container_memory_usage_bytes{namespace="$namespace",pod=~"$pod_name",image=""})
最大值：
max(container_memory_usage_bytes{namespace="$namespace",pod=~"$pod_name",image=""})
limit:
sum(container_spec_memory_limit_bytes{pod=~"$pod_name"})

请求时延：
90值：
histogram_quantile(0.90, sum(rate(istio_request_duration_milliseconds_bucket{kubernetes_pod_name=~"$pod_name"}[2m]))by(le))
95值：
histogram_quantile(0.95, sum(rate(istio_request_duration_milliseconds_bucket{kubernetes_pod_name=~"$pod_name"}[2m]))by(le))
99值：
histogram_quantile(0.99, sum(rate(istio_request_duration_milliseconds_bucket{kubernetes_pod_name=~"$pod_name"}[2m]))by(le))
```

## 服务限流

### 单机限流

单机限流功能替服务的每个pod设置固定的限流数值，其底层是依赖envoy插件envoy.filters.http.local_ratelimit 提供的限流能力，[Local Ratelimit Plugin](https://www.envoyproxy.io/docs/envoy/latest/configuration/http/http_filters/local_rate_limit_filter)。

简单样例如下，我们对reviews服务进行限流，根据condition字段的值判断是否执行限流，这里我们直接设置了true，让其永久执行限流，同样用户可以设置一个动态的值，limiter 会计算其结果，动态的进行限流。fill_interval 指定限流间隔为60s，quota指定限流数量100，strategy标识该限流是单机限流single，target 字段标识需要限流的端口9080。

slime/sl-productpage-01.yaml

kubectl apply -f sl-productpage-01.yaml -n slime

```
apiVersion: microservice.slime.io/v1alpha1
kind: SmartLimiter
metadata:
  name: productpage
spec:
  sets:
    _base:
      descriptor:
      - action:
          fill_interval:
            seconds: 60
          quota: "1"
        condition: '{{._base.cpu.sum}}>10'
```

### 全局均分限流

全局均分限功能根据用户设置的总的限流数，然后平均分配到各个pod，底层同样是依赖envoy插件envoy.filters.http.local_ratelimit 提供的限流能力[Local Ratelimit Plugin](https://www.envoyproxy.io/docs/envoy/latest/configuration/http/http_filters/local_rate_limit_filter)。

简单样例如下，我们对reviews服务进行限流，

根据condition字段的值判断是否执行限流，这里我们直接设置了true，让其永久执行限流，同样用户可以设置一个动态的值，limiter 会计算其结果，动态的进行限流。fill_interval 指定限流间隔为60s，quota指定限流数量100/{{._base.pod}}, {{._base.pod}}的值是由limiter模块根据metric计算得到，假如该服务有2个副本，那么quota的值为50，strategy标识该限流是均分限流，target 字段标识需要限流的端口9080。

```
apiVersion: microservice.slime.io/v1alpha2
kind: SmartLimiter
metadata:
  name: reviews
  namespace: default
spec:
  sets:
    _base:
      descriptor:
      - action:
          fill_interval:
            seconds: 60
          quota: '100/{{._base.pod}}'
          strategy: 'average'  
        condition: 'true'
        target:
          port: 9080
```

### 全局共享限流

全局共享限流功能替服务的所有pod维护了一个全局计数器，底层依赖的是envoy插件nvoy.filters.http.ratelimit 提供的限流能力 [Ratelimit Plugin](https://www.envoyproxy.io/docs/envoy/latest/configuration/http/http_filters/rate_limit_filter) 和RLS服务提供给的全局计数能力[RLS](https://github.com/envoyproxy/ratelimit) 。

当提交一个全局共享限流SmartLimiter后，limiter模块会根据其内容生成EnvoyFilter和名为slime-rate-limit-config的ConfigMap。EnvoyFilter会被Istio监听到，下发限流配置至envoy，而ConfigMap则会被挂载到RLS服务，RLS根据ConfigMap内容生成全局共享计数器。

简单样例如下，我们对reviews服务进行限流，字段含义可参考上面文档。主要区别在于 strategy为global，并且有rls 地址，如果不指定的话为默认为outbound|18081||rate-limit.istio-system.svc.cluster.local，这对应着默认安装的RLS。注意：由于RLS功能的要求，seconds 只支持 1、60、3600、86400，即1秒、1分钟、1小时、1天

```
apiVersion: microservice.slime.io/v1alpha2
kind: SmartLimiter
metadata:
  name: reviews
  namespace: default
spec:
  sets:
    _base:
      #rls: 'outbound|18081||rate-limit.istio-system.svc.cluster.local' 如果不指定默认是该地址
      descriptor:
      - action:
          fill_interval:
            seconds: 60
          quota: '100'
          strategy: 'global'
        condition: 'true'
        target:
          port: 9080  
```



### 分组限流

slime/sl-reviews.yaml

kubectl apply -f sl-reviews.yaml -n slime

```
apiVersion: microservice.slime.io/v1alpha1
kind: SmartLimiter
metadata:
  name: reviews
spec:
  sets:
    v1: 
      descriptor:
      - action:
          fill_interval:
            seconds: 60
          quota: "1"
        condition: "true"
```

slime/dr-reviews.yaml

kubectl apply -f dr-reviews.yaml -n slime

```
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: reviews
spec:
  host: reviews
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
  - name: v3
    labels:
      version: v3
```

