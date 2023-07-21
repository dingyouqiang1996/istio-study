# istio资源详解原理和环境搭建

## 1-1课程特色

a.1000多个istio实战案例,20多个envoy案例。800多个envoyfilter案例，全程已实战为主，理论相对较少，案例90%可试验

b.涵盖98%以上crd字段配置

c.不仅讲解yaml配置，同时结合envoy配置讲解

d.不回避难点内容，深入讲解envoyfilter配置

e深入讲解envoy

f详细讲解额外内容，比如gateway-api,wasm,升降级，发布，灰度发布，蓝绿发布，istioctl命令，slime，多控制面板，多集群，常见问题

g以一个完整案例串联所有内容

h以markdown文件提供课件，内容详细，方便大家练习

I有学员指出我的istio课程不够突出重点，安装80/20原则，20%内容是常用的，那我是否就讲这20%就可以了呢，其他课程确实是这么干的，他们只讲擅长的20%，我的目的不是这样的,我希望istio课程买我的一个就够了，让你全面学习istio，甚至遇到偏的问题不需要百度，课程里就有讲过，但是难免会出现一个问题，就是不够突出重点，我尽量兼顾全面的时候突出重点，讲到重点，核心功能时我会提示下。



## 课程内容

istio原理，envoy原理，envoy案例，envoy配置，istio crd配置，istio流量管理，istio安全配置，istio可观察性，istio策略控制，istio升级，istio常见问题，istio  wasm，istio多控制面板，gateway-api，slime

## 学习目标







![istio原理1597292280](images\istio原理1597292280.jpg)



## 1-2什么是servicemesh

 提到Service Mesh，就不得不提**微服务**。 根据维基百科的定义：

微服务（Microservices）是一种软件架构风格，它是以专注于单一责任与功能的小型功能区块（Small Building Blocks）为基础，利用模块化的方式组合出复杂的大型应用程序，各功能区块使用与语言无关（Language-Independent/Language agnostic）的API集相互通信。



Service Mesh作为下一代微服务技术的代名词，初出茅庐却深得人心一鸣惊人，大有一统微服务时代的趋势。

那么到底什么是Service Mesh？一言以蔽之：**Service Mesh是微服务时代的TCP协议。**

有了这样一个感性的初步认知，我们再来看到底什么是Service Mesh。



 **服务开发模式和Service Mesh技术的演化过程** 

###                        时代0：开发人员想象中，不同服务间通信的方式 

![1597294008(1)](images\1597294008(1).jpg)

  

###                                                时代1：原始通信时代 

![1597294198(1)](images\1597294198(1).jpg)

 然而现实远比想象的复杂，在实际情况中，通信需要底层能够传输字节码和电子信号的物理层来完成，在TCP协议出现之前，服务需要自己处理网络通信所面临的丢包、乱序、重试等一系列流控问题，因此服务实现中，除了业务逻辑外，还夹杂着对网络传输问题的处理逻辑。

###                                                  时代2：TCP时代 

![1597294830(1)](images\1597294830(1).jpg)

 为了避免每个服务都需要自己实现一套相似的网络传输处理逻辑，TCP协议出现了，它解决了网络传输中通用的流量控制问题，将技术栈下移，从服务的实现中抽离出来，成为操作系统网络层的一部分。 

###                                               时代3：第一代微服务 

![1597294911(1)](images\1597294911(1).jpg)

 在TCP出现之后，机器之间的网络通信不再是一个难题，以GFS/BigTable/MapReduce为代表的分布式系统得以蓬勃发展。这时，分布式系统特有的通信语义又出现了，如熔断策略、负载均衡、服务发现、认证和授权、quota限制、trace和监控等等，于是服务根据业务需求来实现一部分所需的通信语义。 

###                                                时代4：第二代微服务 

![1597295014(1)](images\1597295014(1).jpg)

 为了避免每个服务都需要自己实现一套分布式系统通信的语义功能，随着技术的发展，一些面向微服务架构的开发框架出现了，如Twitter的Finagle、Facebook的Proxygen以及Spring Cloud等等，这些框架实现了分布式系统通信需要的各种通用语义功能：如负载均衡和服务发现等，因此一定程度上屏蔽了这些通信细节，使得开发人员使用较少的框架代码就能开发出健壮的分布式系统。 

###                                   时代5：第一代Service Mesh 

第二代微服务模式看似完美，但开发人员很快又发现，它也存在一些本质问题：

- 其一，虽然框架本身屏蔽了分布式系统通信的一些通用功能实现细节，但开发者却要花更多精力去掌握和管理复杂的框架本身，在实际应用中，去追踪和解决框架出现的问题也绝非易事；
- 其二，开发框架通常只支持一种或几种特定的语言，回过头来看文章最开始对微服务的定义，一个重要的特性就是语言无关，但那些没有框架支持的语言编写的服务，很难融入面向微服务的架构体系，想因地制宜的用多种语言实现架构体系中的不同模块也很难做到；
- 其三，框架以lib库的形式和服务联编，复杂项目依赖时的库版本兼容问题非常棘手，同时，框架库的升级也无法对服务透明，服务会因为和业务无关的lib库升级而被迫升级。

因此以Linkerd，Envoy，Ngixmesh为代表的代理模式（边车模式）应运而生，这就是第一代Service Mesh，它将分布式服务的通信抽象为单独一层，在这一层中实现负载均衡、服务发现、认证授权、监控追踪、流量控制等分布式系统所需要的功能，作为一个和服务对等的代理服务，和服务部署在一起，接管服务的流量，通过代理之间的通信间接完成服务之间的通信请求，这样上边所说的三个问题也迎刃而解。

![1597295174(1)](images\1597295174(1).jpg)

 如果我们从一个全局视角来看，就会得到如下部署图： 

![1597295210(1)](images\1597295210(1).jpg)

 如果我们暂时略去服务，只看Service Mesh的单机组件组成的网络： 

![1597295291(1)](images\1597295291(1).jpg)

 相信现在，大家已经理解何所谓Service Mesh，也就是服务网格了。它看起来确实就像是一个由若干服务代理所组成的错综复杂的网格。 

###                                         时代6：第二代Service Mesh 

 第一代Service Mesh由一系列独立运行的单机代理服务构成，为了提供统一的上层运维入口，演化出了集中式的控制面板，所有的单机代理组件通过和控制面板交互进行网络拓扑策略的更新和单机数据的汇报。这就是以Istio为代表的第二代Service Mesh。 

![1597295380(1)](images\1597295380(1).jpg)

 只看单机代理组件（数据面板）和控制面板的Service Mesh全局部署视图如下： 

![1597295439(1)](images\1597295439(1).jpg)

至此，见证了6个时代的变迁，大家一定清楚了Service Mesh技术到底是什么，以及是如何一步步演化到今天这样一个形态。

现在，我们再回过头来看Buoyant的CEO William Morgan，也就是Service Mesh这个词的发明人，对*Service Mesh的定义：*

**服务网格是一个基础设施层，用于处理服务间通信。云原生应用有着复杂的服务拓扑，服务网格保证请求在这些拓扑中可靠地穿梭。在实际应用当中，服务网格通常是由一系列轻量级的网络代理组成的，它们与应用程序部署在一起，但对应用程序透明。**

这个定义中，有四个关键词：

- 基础设施层+请求在这些拓扑中可靠穿梭：这两个词加起来描述了Service Mesh的定位和功能，是不是似曾相识？没错，你一定想到了TCP；
- 网络代理：这描述了Service Mesh的实现形态；
- 对应用透明：这描述了Service Mesh的关键特点，正是由于这个特点，Service Mesh能够解决以Spring Cloud为代表的第二代微服务框架所面临的三个本质问题。



总结一下，Service Mesh具有如下优点：

- 屏蔽分布式系统通信的复杂性（负载均衡、服务发现、认证授权、监控追踪、流量控制等等），服务只用关注业务逻辑；
- 真正的语言无关，服务可以用任何语言编写，只需和Service Mesh通信即可；
- 对应用透明，Service Mesh组件可以单独升级。

当然，Service Mesh目前也面临一些挑战：

- Service Mesh组件以代理模式计算并转发请求，一定程度上会降低通信系统性能，并增加系统资源开销；
- Service Mesh组件接管了网络流量，因此服务的整体稳定性依赖于Service Mesh，同时额外引入的大量Service Mesh服务实例的运维和管理也是一个挑战；



## Service Mesh 有哪些开源实现

Service Mesh 的概念从2016年提出至今，已经发展到了第二代。

第一代 service mesh 以 [Linkerd](https://linkerd.io/) 和 [Envoy](https://www.envoyproxy.io/) 为代表。

第二代service mesh主要改进集中在更加强大的控制面功能（与之对应的 sidecar proxy 被称之为数据面），典型代表有 [Istio](https://istio.io/) 和 [Conduit](https://conduit.io/)。 

 nginx也推出了其 service mesh 的开源实现：[nginMesh](https://github.com/nginmesh/nginmesh). 

![1597296708(1)](images\1597296708(1).jpg)

## 1-3什么是istio

 官方对 Istio 的介绍浓缩成了一句话： 

 An open platform to connect, secure, control and observe services. 

翻译过来，就是”连接、安全加固、控制和观察服务的开放平台“。开放平台就是指它本身是开源的，服务对应的是微服务，也可以粗略地理解为单个应用。

![1597297081(1)](images\1597297081(1).jpg)

中间的四个动词就是 Istio 的主要功能，官方也各有一句话的说明。这里再阐释一下：

连接（connect）：Istio通过集中配置的流量规则控制服务间的流量和调用，实现负载均衡、熔断、故障注入、重试、重定向等服务治理功能。

安全（secure）：Istio提供透明的认证机制、通道加密、服务访问授权等安全能力，可增强服务访问的安全性。

控制（control）：Istio通过可动态插拔、可扩展的策略实现访问控制、速率限制、配额管理、服务计费等能力。

观察（observe）：动态获取服务运行数据和输出，提供强大的调用链、监控和调用日志收集输出的能力。配合可视化工具，可方便运维人员了解服务的运行状态，发现并解决问题。

## istio历史

1.  Google, IBM, and Lyft 创建，于2017年5月24发布0.1版本
2.  2018年7月31日晚上 24 点，Istio [宣布](https://istio.io/blog/2018/announcing-1.0/)推出 1.0 正式版本，并表示已可用于生产环境。这距离最初的 0.1 版本发布已过去一年多的时间。
3. 2019年3月19日发布1.1版本
4. 2019年6月18日发布1.2版本
5. 2019年9月12日发布1.3版本
6. 2019年12月14日发布1.4版本
7. 2020年3月5日发布1.5版本，架构发生重大改变
8. 2020年5月21日发布1.6版本 
9.  2020年8月21日发布1.7版本
10.  2020年11月19日发布1.8版本
11.  2021年2月9日发布1.9版本
12.  2021年5月18日发布1.10版本
13.  2021年8月12日发布1.11版本
14.  2021年12月18日发布1.12版本
15.  2022年2月22日发布1.13版本

## 1-4（1.5前架构）

![1597473333(1)](images\1597473333(1).jpg)



![1597473398(1)](images\1597473398(1).jpg)



1）**Istio-pilot**

![1597474160(1)](images\1597474160(1).jpg)

 如果把数据面的envoy 也看作一种agent， 则Pilot 类似传统C /S 架构中的服务端Master，下发指令控制客户端完成业务功能。和传统的微服务架构对比， Pilot 至少涵盖服务注册中心和Config Server等管理组件的功能。
    如上图所示：pilot直接从运行平台(kubernetes,consul)提取数据并将其构造和转换成istio的服务发现模型， 因此pilot只有服务发现功能，无须进行服务注册。这种抽象模型解耦Pilot 和底层平台的不同实现，可支持kubernetes，consul等平台 。

 除了服务发现， Pilot 更重要的一个功能是向数据面下发规则，包括VirtualService 、DestinationRule 、Gateway 、ServiceEntry 等流量治理规则，也包括认证授权等安全规则。Pilot 负责将各种规则转换成Envoy 可识别的格式，通过标准的XDS 协议发送给Envoy,指导Envoy 完成功作。在通信上， Envoy 通过gRPC 流式订阅Pilot 的配置资源。如下图所示， Pilot 将VirtualService 表达的路由规则分发到Evnoy 上， Envoy 根据该路由规则进行流量转发。 

![1597474422(1)](images\1597474422(1).jpg)

2）**mixer**

 Istio 控制面部署了两个Mixer 组件： istio-telemetry 和istio-policy ，分别处理遥测数据的收集和策略的执行。查看两个组件的Pod 镜像会发现，容器的镜像是相同的，都是"/istio/mixer"
    Mixer 是Istio 独有的一种设计,不同于Pilot ，在其他平台上总能找到类似功能的服务组件。从调用时机上来说,Pilot 管理的是配置数据，在配置改变时和数据面交互即可；然而，对于Mixer 来说，在服务间交互时Envoy 都会对Mixer 进行一次调用，因此这是一种实时管理。当然，在实现上通过在Mixer 和Proxy 上使用缓存机制，可保证不用每次进行数据面请求时都和Mixer 交互。 

 **istio-telemetry** 

 istio-telemetry是专门用于收集遥测数据的Mixer服务组件;如下图所示 所示，当网格中的两个服务间有调用发生时，服务的代理Envoy 就会上报遥测数据给istio-telemetry服务组件，istio-telemetry 服务组件则根据配置将生成访问Metric等数据分发给后端的遥测服务。数据面代理通过Report 接口上报数据时访问数据会被批量上报。 

![1597474684(1)](images\1597474684(1).jpg)

 **istio-policy** 

  istio-policy 是另外一个Mixer 服务，和istio-telemetry 基本上是完全相同的机制和流程。
    如图下图所示，数据面在转发服务的请求前调用istio-policy 的Check接口检查是否允许访问， Mixer 根据配置将请求转发到对应的Adapter 做对应检查，给代理返回允许访问还是拒绝。可以对接如配额、授权、黑白名单等不同的控制后端，对服务间的访问进行可扩展的控制。 

![1597474770(1)](images\1597474770(1).jpg)

3）**istio-citadel**

 服务列表中的istio-citadel 是Istio 的核心安全组件，提供了自动生成、分发、轮换与撤销密钥和证书功能。Citadel 一直监听Kube-apiserver ，以Secret 的形式为每个服务都生成证书密钥，并在Pod 创建时挂载到Pod 上，代理容器使用这些文件来做服务身份认证，进而代理两端服务实现双向TLS认证、通道加密、访问授权等安全功能，这样用户就不用在代码里面维护证书密钥了。如下图 所示，frontend 服务对forecast 服务的访问用到了HTTP 方式，通过配置即可对服务增加认证功能， 双方的Envoy 会建立双向认证的TLS 通道，从而在服务间启用双向认证的HTTPS 。 

![1597474866](images\1597474866.jpg)

4）**istio-galley**

 istio-galley 并不直接向数据面提供业务能力，而是在控制面上向其他组件提供支持。Galley 作为负责配置管理的组件，验证配置信息的格式和内容的正确性，并将这些配置信息提供给管理面的Pilot和Mixer服务使用，这样其他管理面组件只用和Galley 打交道，从而与底层平台解耦。在新的版本中Galley的作用越来越核心。 

5）**istio-sidecar-injector**

 istio-sidecar-inj ector 是负责向动注入的组件，只要开启了自动注入，在Pod 创建时就会自动调用istio-sidecar-injector 向Pod 中注入Sidecar 容器。在Kubernetes环境下，根据自动注入配置， Kube-apiserver 在拦截到Pod 创建的请求时，会调用自动注入服务istio-sidecar-injector生成Sidecar 容器的描述并将其插入原Pod的定义中，这样，在创建的Pod 内除了包括业务容器，还包括Sidecar 容器。这个注入过程对用户透明，用户使用原方式创建工作负载。 

6）**istio-ingressgateway**

 istio-ingressgateway 就是入口处的Gateway ，从网格外访问网格内的服务就是通过这个Gateway 进行的。istio-ingressgateway 比较特别， 是一个Loadbalancer 类型的Service,不同于其他服务组件只有一两个端口,istio-ingressgateway 开放了一组端口，这些就是网格内服务的外部访问端口.如下图 所示，网格入口网关istio-ingressgateway 的负载和网格内的Sidecar 是同样的执行体，也和网格内的其他Sidecar 一样从Pilot处接收流量规则并执行。 。Istio 通过一个特有的资源对象Gateway 来配置对外的协议、端口等。 

![1597475074(1)](images\1597475074(1).jpg)

7）**istio-egressgateway**

 istio-egressgateway就是出口处的Gateway ，从网格内访问网格外的服务就是通过这个Gateway 进行的。

8）**envoy proxy**

 istio 的sidecar（istio-proxy）是开源项目envoy的扩展版，Envoy是用C＋＋开发的非常有影响力的轻量级高性能开源服务代理。作为服务网格的数据面，是istio架构中唯一的数据面组件， Envoy 提供了动态服务发现、负载均衡、TLS , HTTP/2 及gRPC 代理、熔断器、健康检查、流量拆分、灰度发布、故障注入等功能。在istio-proxy容器中除了有Envoy ，还有一个pilot-agent的守护进程。 

![1597475478(1)](images\1597475478(1).jpg)

## 1.5后架构

## 架构调整

这部分主要分析 Istio 1.5 在架构上的调整，这也是该版本最核心的变化。主要包括重建了控制平面，将原有的多个组件整合为一个单体结构 `istiod`；同时废弃了被诟病已久的 Mixer 组件。还对是否向后兼容的部分也做了说明，如果你要从 1.4.x 版本升级到 1.5 必须知道这些变化。

![1597475811(1)](images\1597475811(1).jpg)

## 1-5部署istio

### 下载istio

下载地址： https://github.com/istio/istio/releases 

目前最新的是1.13.2，以1.13.2位例进行部署

解压istio：tar zvxf istio-1.13.2-linux-amd64.tar.gz 

介绍下载的内容：

![1597476262(1)](images\1597476262(1).jpg)

bin: istioctl命令文件

manifests：资源文件

samples： 例子

tools： 一些工具脚本

### 配置环境变量

export PATH=/root/istio-1.10.0/bin:$PATH

### 执行命令部署

istioctl install --set profile=demo

### 删除istio

istioctl x uninstall --purge

## 1.10istio pod

## ![1621567506(1)](images\1621567506(1).jpg)

## 1-6bookinfo示例介绍

![1597477078(1)](images\1597477078(1).jpg)

Bookinfo是istio官网示例，应用程序分为四个单独的微服务：

- `productpage`。该`productpage`微服务调用`details`和`reviews`微服务来填充页面。
- `details`。该`details`微服务包含图书信息。
- `reviews`。该`reviews`微服务包含了书评。它们调用`ratings`微服务。
- `ratings`。该`ratings`微服务包含预定伴随书评排名信息。

`reviews`微服务有3个版本：

- 版本v1不会调用该`ratings`服务。
- 版本v2调用该`ratings`服务，并将每个等级显示为1到5个**黑星**★。
- 版本v3调用该`ratings`服务，并将每个等级显示为1到5个**红色星号**★。

details有两个版本：

- 版本v1可用服务
- 版本v2不可用服务

ratings服务有4个版本：

- 版本v1不会没有数据库
- 版本v2调用mongodb数据库
- 版本v2-mysql调用mysql数据库
- 版本v2-mysql-vm调用外部mysql数据库

## 部署bookinfo

1创建名称空间

kubectl create ns istio

2设置名称空间标签实现自动注入

kubectl label ns istio istio-injection=enabled

3部署bookinfo

kubectl apply -f bookinfo.yaml -n istio

kubectl apply -f bookinfo-details-v2.yaml -n istio

kubectl apply -f bookinfo-ratings-v2.yaml -n istio

 kubectl apply -f bookinfo-ratings-v2-mysql.yaml -n istio

kubectl apply -f bookinfo-ratings-v2-mysql-vm.yaml -n istio

kubectl apply -f bookinfo-db.yaml -n istio

kubectl apply -f bookinfo-mysql.yaml -n istio

4部署gateway和virtualservice，destinationrule

kubectl apply -f bookinfo-gateway.yaml -n istio

kubectl apply -f destination-rule-all.yaml -n istio

 kubectl apply -f virtual-service-all-v1.yaml -n istio

5修改istio-ingressgateway svc为nodeport类型

6访问url 

http://192.168.198.154:nodeport/productpage

![1597478404(1)](images\1597478404(1).jpg)

## 1-7istio crd

```
[root@node01 bootstrap]# kubectl get crd|grep istio
authorizationpolicies.security.istio.io    2022-03-22T03:56:02Z
destinationrules.networking.istio.io       2022-03-22T03:56:02Z
envoyfilters.networking.istio.io           2022-03-22T03:56:02Z
gateways.networking.istio.io               2022-03-22T03:56:03Z
istiooperators.install.istio.io            2022-03-22T03:56:03Z
peerauthentications.security.istio.io      2022-03-22T03:56:03Z
proxyconfigs.networking.istio.io           2022-03-22T03:56:03Z
requestauthentications.security.istio.io   2022-03-22T03:56:03Z
serviceentries.networking.istio.io         2022-03-22T03:56:04Z
sidecars.networking.istio.io               2022-03-22T03:56:04Z
telemetries.telemetry.istio.io             2022-03-22T03:56:04Z
virtualservices.networking.istio.io        2022-03-22T03:56:04Z
wasmplugins.extensions.istio.io            2022-03-22T03:56:05Z
workloadentries.networking.istio.io        2022-03-22T03:56:05Z
workloadgroups.networking.istio.io         2022-03-22T03:56:05Z

```

authorizationpolicies:权限控制相关

destinationrules：目标规则

envoyfilters：envoy过滤器

gateways：网关

istiooperators：安装相关，不介绍

peerauthentications：用于配置tls

proxyconfigs 代理的配置，包括并发，环境变量，镜像

requestauthentications：用于配置jwt

serviceentries：服务入口，用于连接外部服务

sidecars：控制sidecar

telemetries：遥测配置

virtualservices：虚拟服务

wasmplugins  wasm插件配置

workloadentries：工作负载条目

workloadgroups：工作负载组