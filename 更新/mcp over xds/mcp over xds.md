# Istio 1.9 如何对接第三方注册中心

https://zhuanlan.zhihu.com/p/371085990

Istio 控制面组件 pilot-discovery 主要接受两类输入数据，然后按照一定逻辑输出一类数据。

![img](https://pic1.zhimg.com/80/v2-e8154d6a3465b2c5cad28ba4cbc5d59c_720w.webp)

### 输入数据

- **config**：istio 中的一些配置，比如虚拟服务，目标规则等规则。由 configcontroller 处理。
- **service**：相当于注册中心，存放了服务和实例，由 servciecontroller 处理。

### 输出数据

- **xds** : 生成 envoy 配置，下发给 Proxy。由 xds server 处理。

本文我们主要讲述 service 相关。

### Istio 1.9 变化

Istio 1.9 在对接第三方注册中心方面，有两个需要注意的地方：

- **[istio 1.8 不再 in-tree 支持 consul 作为外部注册中心](https://link.zhihu.com/?target=https%3A//github.com/istio/istio/pull/25521)** ：加上之前版本移除了对于euraka的支持。截止1.8，所有通过 in-tree 方式支持外部注册中心的代码彻底被移除。
- **istio1.9 不再支持MCP的方式对接外部注册中心，改为 MCP-over-XDS的方式**：新的方式虽然叫mcp over xds，但本质上没有使用之前的mcp协议。这意味着istio 1.9 中不能使用之前社区开源的一些 mcp 实现。

> 移除 in-tree 方式的支持是每一个大型项目的必经之路，比如k8s通过CSI，CRI两大接口，逐步移除了对于存储和运行时的in-tree支持。
> 大家可以仔细阅读 istio repo 中 诸多关于consul 的 issue。大多是不同版本consul的差异带来的bug，以及功能支持有限。这样的好处是可以解耦所有扩展组件的代码。

### 两种方式对接第三方注册中心

从 1.9 开始， istio 对接第三方注册中心的途径基本上确定了。如果我们要对接类似于consul等第三方注册中心的话，有以下两种：

### 1：MCP-over-XDS

为第三方注册中心编写 MCP-over-XDS server。然后将该server的地址配置到pilot 的configSource 参数中。

pilot 启动的时候，就会向目的 MCP-over-XDS server 发起订阅请求，并建立 grpc 链接。

MCP-over-XDS server 读取 consul 中的数据，并转换为ServiceEntry，然后 push 给 pilot。

pilot接受到数据后，会对比内存中的数据，保证数据一致。

![img](https://pic1.zhimg.com/80/v2-e2155ef61a52708f459fb98939609f8c_720w.webp)

由于istio的流行，nacos 已经原生支持 mcp over xds，具体见 [PR](https://link.zhihu.com/?target=https%3A//github.com/alibaba/nacos/pull/5124)。因为流量规则也是可以通过 mcp over xds 传输，nacos 又可以用作配置中心，所以基于nacos的服务网格也是一个方向。这样可以实现和kubernetes 平台解耦。大致示意图如下：

![img](https://pic2.zhimg.com/80/v2-4a56ce865fb2445eb9463b37f011d225_720w.webp)

> 该方式有一点需要注意：当consul等第三方注册中心，规模比较大，也就是服务和实例比较多，需要关注效率和性能。因为mcp over xds 是一种全量的 push。任何一个变更，都需要一次全量push。

### 2：ServiceEntry / WorkloadEntry

为第三方注册中心编写适配器，读取注册中心的数据，转换为ServiceEntry / WorkloadEntry，然后通过kubernetes apiserver 接口，写入到kubernetes中。

pilot watch了 ServiceEntry 和 WorkloadEntry，那么对应的controller 就会针对两种资源对象的变更，做出对应的处理。

> 该方式，可以做到增量更新。不过该适配器在编写的时候，在写入环节需要注意启用流控，避免对etcd造成过大的压力。

该方式，对于consul 的话，已经有 [consul2istio](https://link.zhihu.com/?target=https%3A//github.com/aeraki-framework/consul2istio) 项目。

![img](https://pic4.zhimg.com/80/v2-1cb3353c594fe101ef01a576cd173dbb_720w.webp)

### 总结

istio 演进比较快，每个版本都有很多变化。我们在落地的时候，需要关注各个版本的差异。

本文主要介绍了istio 1.9 在对接外部注册中心的的变化，并提供了两种对接方式以及每种方式需要注意的地方。