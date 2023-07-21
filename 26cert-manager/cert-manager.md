# 什么是cert-manager

 [cert-manager](https://cert-manager.io/)是Kubernetes的附加组件，用于自动管理和颁发各种发行来源的TLS证书。它将确保证书有效并定期更新，并尝试在到期前的适当时间更新证书。 

cert-manager is a Kubernetes add-on to automate the management and issuance of TLS certificates from various issuing sources.

It will ensure certificates are valid and up to date periodically, and attempt to renew certificates at an appropriate time before expiry.

It is loosely based upon the work of [kube-lego](https://github.com/jetstack/kube-lego) and has borrowed some wisdom from other similar projects e.g. [kube-cert-manager](https://github.com/PalmStoneGames/kube-cert-manager).

# 架构

![1](images\1.svg)

# 流程

1. 创建自签名发行者
2. 生成CA证书
3. 创建CA发行者（ClusterIssuer）
4. 生成网站证书
5. 将网站证书配置到Ingress

![1](images\1.png)

# 安装

```
kubectl create ns cert-manager
kubectl apply -f cert-manager.yaml

curl -L -o kubectl-cert-manager.tar.gz https://github.com/jetstack/cert-manager/releases/latest/download/kubectl-cert_manager-linux-amd64.tar.gz
tar xzf kubectl-cert-manager.tar.gz
sudo mv kubectl-cert_manager /usr/local/bin
```

卸载

```
kubectl delete -f cert-manager.yaml
```

以helm方式安装

```
helm repo add jetstack https://charts.jetstack.io
helm search repo cert-manager
helm install \
  cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --set installCRDs=true \
  --set "extraArgs={--feature-gates=ExperimentalGatewayAPISupport=true}"
```

卸载

```
helm uninstall   cert-manager --namespace cert-manager
```



# 免费证书签发原理

Let’s Encrypt 利用 ACME 协议来校验域名是否真的属于你，校验成功后就可以自动颁发免费证书，证书有效期只有 90 天，在到期前需要再校验一次来实现续期，幸运的是 cert-manager 可以自动续期，这样就可以使用永久免费的证书了。如何校验这个域名是否属于你呢？主流的两种校验方式是 HTTP-01 和 DNS-01，详细校验原理可参考 Let’s Encrypt 的运作方式，下面将简单描述下。

## HTTP-01 校验原理

HTTP-01 的校验原理是给你域名指向的 HTTP 服务增加一个临时 location ，Let’s Encrypt 会发送 http 请求到 http:///.well-known/acme-challenge/，YOUR_DOMAIN 就是被校验的域名，TOKEN 是 ACME 协议的客户端负责放置的文件，在这里 ACME 客户端就是 cert-manager，它通过修改或创建 Ingress 规则来增加这个临时校验路径并指向提供 TOKEN 的服务。Let’s Encrypt 会对比 TOKEN 是否符合预期，校验成功后就会颁发证书。此方法仅适用于给使用 Ingress 暴露流量的服务颁发证书，并且不支持泛域名证书。

## DNS-01 校验原理

DNS-01 的校验原理是利用 DNS 提供商的 API Key 拿到你的 DNS 控制权限， 在 Let’s Encrypt 为 ACME 客户端提供令牌后，ACME 客户端 (cert-manager) 将创建从该令牌和您的帐户密钥派生的 TXT 记录，并将该记录放在 _acme-challenge.。 然后 Let’s Encrypt 将向 DNS 系统查询该记录，如果找到匹配项，就可以颁发证书。此方法不需要你的服务使用 Ingress，并且支持泛域名证书。

## 校验方式对比

HTTP-01 的校验方式的优点是: 配置简单通用，不管使用哪个 DNS 提供商都可以使用相同的配置方法；缺点是：需要依赖 Ingress，如果你的服务不是用 Ingress 暴露流量的就不适用，而且不支持泛域名证书。

DNS-01 的校验方式的优点是没有 HTTP-01 校验方式缺点，不依赖 Ingress，也支持泛域名；缺点就是不同 DNS 提供商的配置方式不一样，而且 DNS 提供商有很多，cert-manager 的 Issuer 不可能每个都去支持，不过有一些可以通过部署实现了 cert-manager 的 Webhook 的服务来扩展 Issuer 进行支持，比如 DNSPod 和 阿里 DNS，详细 Webhook 列表请参考: https://cert-manager.io/docs/configuration/acme/dns01/#webhook

选择哪种方式呢？条件允许的话，建议是尽量用 DNS-01 的方式，限制更少，功能更全。

# crd详解

```
[root@node01 cert-manager]# kubectl get crd|grep cert
certificaterequests.cert-manager.io                   2021-09-27T07:42:06Z
certificates.cert-manager.io                          2021-09-27T07:42:06Z
challenges.acme.cert-manager.io                       2021-09-27T07:42:06Z
clusterissuers.cert-manager.io                        2021-09-27T07:42:07Z
issuers.cert-manager.io                               2021-09-27T07:42:07Z
orders.acme.cert-manager.io                           2021-09-27T07:42:08Z
```

Issuer/ClusterIssuer: 用于指示 cert-manager 用什么方式签发证书，本文主要讲解签发免费证书的 ACME 方式。ClusterIssuer 与 Issuer 的唯一区别就是 Issuer 只能用来签发自己所在 namespace 下的证书，ClusterIssuer 可以签发任意 namespace 下的证书。
Certificate: 用于告诉 cert-manager 我们想要什么域名的证书以及签发证书所需要的一些配置，包括对 Issuer/ClusterIssuer 的引用。

## 案例1

生成签名密钥对

```
openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=example.com' -keyout ca.key -out ca.crt
```

将签名密钥对保存为Secret

```
kubectl create secret tls ca-key-pair \
   --cert=ca.crt \
   --key=ca.key \
   --namespace=cert-manager
```

创建引用Secret的Issuer

cert-manager/ca-issuer.yaml

kubectl apply -f ca-issuer.yaml 

```
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: ca-issuer
spec:
  ca:
    secretName: ca-key-pair
```

获得签名证书

cert-manager/certificate-bookinfo.yaml

kubectl apply -f certificate-bookinfo.yaml -n istio-system

```
apiVersion:  cert-manager.io/v1
kind: Certificate
metadata:
  name: bookinfo-certificate
spec:
  secretName: bookinfo-certs
  issuerRef:
    name: ca-issuer
    kind: ClusterIssuer
  commonName: bookinfo.com
  subject:
    organizations:
    - example Inc.
  dnsNames:
  - bookinfo.com
  - bookinfo.demo
```

## issuers

### ca

略

crlDistributionPoints

证书撤销列表分发点

ocspServers

 *在线证书状态协议*(Online Certificate Status Protocol,简称*OCSP*)是维护服务器和其它网络资源安全性的两种普遍模式之一 

### acme

 Automated Certificate Management Environment (ACME) 

必须是外部可访问的k8s集群，需要回调

#### http01

部署gateway-api crd

```
cd gateway-api
kubectl kustomize "config/crd"|kubectl apply -f -
```



cert-manager/acme/letsencrypt-http01-gatewayHTTPRoute.yaml

kubectl apply -f letsencrypt-http01-gatewayHTTPRoute.yaml -n istio

```
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: letsencrypt
spec:
  acme:
    server: https://acme-v02.api.letsencrypt.org/directory
    privateKeySecretRef:
      name: letsencrypt
    solvers:
      - http01:
          gatewayHTTPRoute:
            labels:
              gateway: http01-solver
```

cert-manager/acme/isito-gc.yaml

kubectl apply -f isito-gc.yaml -n istio-system

```
apiVersion: networking.x-k8s.io/v1alpha1
kind: GatewayClass
metadata:
  name: istio
spec:
  controller: istio.io/gateway-controller
```

cert-manager/acme/gateway-istio.yaml

kubectl apply -f gateway-istio.yaml -n istio-system

```
apiVersion: networking.x-k8s.io/v1alpha1
kind: Gateway
metadata:
  name: istio
spec:
  gatewayClassName: istio
  listeners:
  - protocol: HTTP
    port: 80
    routes:
      kind: HTTPRoute
      selector:
        matchLabels:
          gateway: http01-solver
      namespaces:
        from: All
```

certificate-http01-example.yaml

kubectl apply -f certificate-http01-example.yaml -n istio

```
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: example-tls
spec:
  issuerRef:
    name: letsencrypt
  secretName: example-tls
  dnsNames:
  - www.markabledu.com
```

 Waiting for HTTP-01 challenge propagation: failed to perform self check GET request 'http://www.markabledu.com/.well-known/acme-challenge/ZFPte-9amstJ4P-hB2ErXlCE7bYcxQOsSfCc883VVWM': Get "http://www.markabledu.com/.well-known/acme-challenge/ZFPte-9amstJ4P-hB2ErXlCE7bYcxQOsSfCc883VVWM": dial tcp 47.88.28.203:80: i/o timeout (Client.Timeout exceeded while awaiting headers)

#### dns01

cloudflare

cert-manager/acme/letsencrypt-dns01.yaml

kubectl apply -f letsencrypt-dns01.yaml -n istio

```
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: letsencrypt-dns01
spec:
  acme:
    privateKeySecretRef:
      name: letsencrypt-dns01
    server: https://acme-v02.api.letsencrypt.org/directory
    solvers:
    - dns01:
        cloudflare:
          email: my-cloudflare-acc@example.com 
          apiTokenSecretRef:
            key: api-token
            name: cloudflare-api-token-secret

```

certificate-http01-example.yaml

kubectl apply -f certificate-http01-example.yaml -n istio

```
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: example-tls
spec:
  issuerRef:
    name: letsencrypt
  secretName: example-tls
  dnsNames:
  - www.markabledu.com
```

 

### selfSigned

创建自签名发行者

cert-manager/selfsigned-issuer.yaml

kubectl apply -f selfsigned-issuer.yaml -n cert-manager

```
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
  name: selfsigned-issuer
  namespace: cert-manager
spec:
  selfSigned: {}

```

生成CA证书

cert-manager/certificate-example-ca.yaml

kubectl apply -f certificate-example-ca.yaml -n cert-manager

```
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: ca-example-com 
  namespace: cert-manager
spec:
  secretName: ca-example-com-tls
  duration: 2160h # 90d
  renewBefore: 360h # 15d
  subject:
    organizations:
    - Example Inc. 
  commonName: ca.example.com 
  isCA: true 
  privateKey:
    algorithm: RSA
    encoding: PKCS1
    size: 2048
  dnsNames:
  - ca.example.com 
  issuerRef:
    name: selfsigned-issuer 
    kind: Issuer
    group: cert-manager.io

```

创建CA发行者（ClusterIssuer）

cert-manager/clusterIssuer-ca-example.yaml

kubectl apply -f clusterIssuer-ca-example.yaml 

```
apiVersion: cert-manager.io/v1
kind: ClusterIssuer
metadata:
  name: ca-example-issuer
  namespace: cert-manager 
spec:
  ca:
    secretName: ca-example-com-tls 

```

生成网站证书

cert-manager/certificate-site-example-com.yaml

kubectl apply -f certificate-site-example-com.yaml -n istio-system

```
apiVersion: cert-manager.io/v1
kind: Certificate
metadata:
  name: site-example-com
  namespace: istio-system
spec:
  secretName: site-example-com-tls
  duration: 2160h # 90d
  renewBefore: 360h # 15d
  subject:
    organizations:
    - Example Inc. 
  commonName: site.example.com 
  isCA: false
  privateKey:
    algorithm: RSA
    encoding: PKCS1
    size: 2048
  dnsNames:
  - site.example.com 
  issuerRef:
    name: ca-example-issuer
    kind: ClusterIssuer
    group: cert-manager.io

```



#### crlDistributionPoints

证书撤销列表分发点

 http://example.com/crl.pem 



### vault



### venafi





## certificates



# 命令

```
[root@node01 cert-manager]# kubectl cert-manager

kubectl cert-manager is a CLI tool manage and configure cert-manager resources for Kubernetes

Usage:
  kubectl cert-manager [command]

Available Commands:
  approve      Approve a CertificateRequest
  check        Check cert-manager components
  completion   generate the autocompletion script for the specified shell
  convert      Convert cert-manager config files between different API versions
  create       Create cert-manager resources
  deny         Deny a CertificateRequest
  experimental Interact with experimental features
  help         Help about any command
  inspect      Get details on certificate related resources
  renew        Mark a Certificate for manual renewal
  status       Get details on current status of cert-manager resources
  version      Print the cert-manager kubectl plugin version and the deployed cert-manager version
```

