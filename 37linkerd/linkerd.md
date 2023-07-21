# 什么是linkerd



# 架构

![control-plane](images\control-plane.png)

## CLI

`Linkerd CLI` 通常在集群外部运行（例如在您的本地机器上），用于与 `Linkerd` 交互。

## 控制平面(control plane)

Linkerd `控制平面`是一组在专用 `Kubernetes` 命名空间（默认为 `linkerd`）中运行的服务。 控制平面有几个组件，列举如下。

### 目标服务(destination)

`数据平面代理`使用 `destination` 服务来确定其行为的各个方面。 它用于获取服务发现信息（即发送特定请求的位置和另一端预期的 `TLS` 身份）； 获取有关允许哪些类型的请求的`策略`信息； 获取用于通知`每条路由指标`、`重试`和`超时`的服务配置文件信息；和更多其它有用信息。

### 身份服务(identity)

`identity` 服务充当 [TLS 证书颁发机构](https://en.wikipedia.org/wiki/Certificate_authority)， 接受来自代理的 [CSR](https://en.wikipedia.org/wiki/Certificate_signing_request) 并返回签名证书。 这些证书在代理初始化时颁发，用于代理到代理连接以实现 [mTLS](https://linkerd.hacker-linner.com/2.11/features/automatic-mtls/)。

### 代理注入器(proxy injector)

`proxy injector` 是一个 Kubernetes `admission controller`，它在每次创建 `pod` 时接收一个 `webhook` 请求。 此 `injector` 检查特定于 `Linkerd` 的 `annotation`（`linkerd.io/inject: enabled`）的资源。 当该 `annotation` 存在时，`injector` 会改变 `pod` 的规范， 并将 `proxy-init` 和 `linkerd-proxy` 容器以及相关的启动时间配置添加到 `pod` 中。

## 数据平面(data plane)

`Linkerd 数据平面`包含超轻型`微代理`，这些`微代理`部署为应用程序 `Pod` 内的 `sidecar` 容器。 由于由 [linkerd-init](https://linkerd.hacker-linner.com/2.11/reference/architecture/#linkerd-init-container)（或者，由 `Linkerd` 的 [CNI 插件](https://linkerd.hacker-linner.com/2.11/features/cni/)）制定的 `iptables` 规则， 这些代理透明地拦截`进出`每个 `pod` 的 `TCP` 连接。

### 代理(Linkerd2-proxy)

`Linkerd2-proxy` 是一个用 [Rust](https://www.rust-lang.org/) 编写的超轻、透明的`微代理`。 `Linkerd2-proxy` 专为 `service mesh` 用例而设计，并非设计为通用代理。

代理的功能包括：

- `HTTP`、`HTTP/2` 和任意 `TCP` 协议的透明、零配置代理。
- `HTTP` 和 `TCP` 流量的自动 `Prometheus` 指标导出。
- 透明、零配置的 `WebSocket` 代理。
- 自动、延迟感知、第 `7` 层负载平衡。
- 非 `HTTP` 流量的自动第 `4` 层负载平衡。
- 自动 `TLS`。
- 按需诊断 `Tap API`。
- 还有更多。

代理支持通过 `DNS` 和[目标 gRPC API](https://github.com/linkerd/linkerd2-proxy-api) 进行服务发现。

您可以在此处阅读有关这些微代理的更多信息：

- [为什么 Linkerd 不使用 Envoy](https://linkerd.io/2020/12/03/why-linkerd-doesnt-use-envoy/)
- [Linkerd 最先进的 Rust 代理 Linkerd2-proxy](https://linkerd.io/2020/07/23/under-the-hood-of-linkerds-state-of-the-art-rust-proxy-linkerd2-proxy/)

### Linkerd init 容器

`linkerd-init` 容器作为 [Kubernetes init 容器](https://kubernetes.io/docs/concepts/workloads/pods/init-containers/) 添加到每个网格 `pod` 中，该容器在任何其他容器启动之前运行。 它使用[iptables](https://linkerd.hacker-linner.com/2.11/reference/iptables/) 通过代理将所有 `TCP` 流量，路由到 `Pod` 和从 `Pod` 发出。

# 安装

```
安装命令行工具
curl -fsL https://run.linkerd.io/install | sh

校验k8s集群
  linkerd check --pre                   
安装
  linkerd install  --set proxyInit.runAsRoot=true | kubectl apply -f -  
检查
  linkerd check                          
启动dashboard
  linkerd dashboard 
  
demo app
curl -fsL https://run.linkerd.io/emojivoto.yml | kubectl apply -f -

注入数据面
kubectl get -n emojivoto deploy -o yaml \
  | linkerd inject - \
  | kubectl apply -f -
  
检查数据面
linkerd -n emojivoto check --proxy

viz扩展
linkerd viz install | kubectl apply -f -

buoyant-cloud 扩展
curl -fsL https://buoyant.cloud/install | sh 
linkerd buoyant install | kubectl apply -f - 


linkerd viz dashboard &
linkerd buoyant dashboard &




```

