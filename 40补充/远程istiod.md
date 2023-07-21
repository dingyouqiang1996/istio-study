# 什么是remote istiod



# 架构

![external-controlplane](images\external-controlplane.svg)

# 部署

1部署gateway（cluster1）

```
cat <<EOF > controlplane-gateway.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
metadata:
  namespace: istio-system
spec:
  components:
    ingressGateways:
      - name: istio-ingressgateway
        enabled: true
        k8s:
          service:
            ports:
              - port: 15021
                targetPort: 15021
                name: status-port
              - port: 15012
                targetPort: 15012
                name: tls-xds
              - port: 15017
                targetPort: 15017
                name: tls-webhook
EOF
```



```
istioctl install -f controlplane-gateway.yaml

[root@localhost remote]# kubectl get pod -n istio-system
NAME                                    READY   STATUS    RESTARTS   AGE
istio-ingressgateway-798f7c57c5-6vl6s   1/1     Running   0          16s
istiod-8488b9bdc7-gd9js                 1/1     Running   0          20s


kubectl patch svc  -n istio-system istio-ingressgateway -p '{"spec":{"externalIPs":["192.168.229.100"]}}'
```



2cluster2

```
cat <<EOF > remote-config-cluster.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
metadata:
  namespace: external-istiod
spec:
  profile: external
  values:
    global:
      istioNamespace: external-istiod
      configCluster: true
    pilot:
      configMap: true
    istiodRemote:
      injectionURL: https://192.168.229.100:15017/inject/:ENV:cluster=cluster1:ENV:net=network1
    base:
      validationURL: https://192.168.229.100:15017/validate
EOF
```



```
kubectl create namespace external-istiod 
istioctl manifest generate -f remote-config-cluster.yaml | kubectl apply  -f -

[root@localhost remote]# kubectl get mutatingwebhookconfiguration
NAME                                     WEBHOOKS   AGE
istio-sidecar-injector-external-istiod   4          41s
```



```
istioctl x create-remote-secret  --type=config --namespace=external-istiod --service-account=istiod --create-service-account=false > cluster2-secret.yaml

scp cluster2-secret.yaml root@192.168.229.153:/root
```



3cluster1

```
kubectl create namespace external-istiod
kubectl create sa istiod-service-account -n external-istiod 

kubectl apply -f cluster2-secret.yaml
```



```
cat <<EOF > external-istiod.yaml
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
metadata:
  namespace: external-istiod
spec:
  profile: empty
  meshConfig:
    rootNamespace: external-istiod
    defaultConfig:
      discoveryAddress: 192.168.229.100:15012
      proxyMetadata:
        XDS_ROOT_CA: /etc/ssl/certs/ca-certificates.crt
        CA_ROOT_CA: /etc/ssl/certs/ca-certificates.crt
  components:
    pilot:
      enabled: true
      k8s:
        overlays:
        - kind: Deployment
          name: istiod
          patches:
          - path: spec.template.spec.volumes[100]
            value: |-
              name: config-volume
              configMap:
                name: istio
          - path: spec.template.spec.volumes[100]
            value: |-
              name: inject-volume
              configMap:
                name: istio-sidecar-injector
          - path: spec.template.spec.containers[0].volumeMounts[100]
            value: |-
              name: config-volume
              mountPath: /etc/istio/config
          - path: spec.template.spec.containers[0].volumeMounts[100]
            value: |-
              name: inject-volume
              mountPath: /var/lib/istio/inject
        env:
        - name: INJECTION_WEBHOOK_CONFIG_NAME
          value: "istio-sidecar-injector-external-istiod"
        - name: VALIDATION_WEBHOOK_CONFIG_NAME
          value: "istio-validator-external-istiod"
        - name: EXTERNAL_ISTIOD
          value: "true"
        - name: CLUSTER_ID
          value: cluster1
        - name: SHARED_MESH_CONFIG
          value: istio
  values:
    global:
      caAddress: 192.168.229.100:15012
      istioNamespace: external-istiod
      operatorManageWebhooks: true
      configValidation: false
      meshID: mesh1
EOF


istioctl manifest generate -f external-istiod.yaml | kubectl apply  -f -


[root@localhost remote]# kubectl get po -n external-istiod 
NAME                      READY   STATUS    RESTARTS   AGE
istiod-65fb7bb5d5-4rpqd   1/1     Running   0          25s
```





```
1创建证书

openssl req -x509 -sha256 -nodes -days 365 -newkey rsa:2048 -subj '/O=example Inc./CN=192.168.229.100' -keyout example.com.key -out example.com.crt 

 openssl req -out bookinfo.example.com.csr -newkey rsa:2048 -nodes -keyout bookinfo.example.com.key -subj "/CN=192.168.229.100/O=some organization"  

 openssl x509 -req -days 365 -CA example.com.crt -CAkey example.com.key -set_serial 0 -in bookinfo.example.com.csr -out bookinfo.example.com.crt 

2创建secret

 kubectl create -n i   secret generic  bookinfo-credential --from-file=tls.key=bookinfo.example.com.key   --from-file=tls.crt=bookinfo.example.com.crt --from-file=ca.crt=example.com.crt 
```



```
cat <<EOF > external-istiod-gw.yaml
apiVersion: networking.istio.io/v1beta1
kind: Gateway
metadata:
  name: external-istiod-gw
  namespace: external-istiod
spec:
  selector:
    istio: ingressgateway
  servers:
    - port:
        number: 15012
        protocol: https
        name: https-XDS
      tls:
        mode: SIMPLE
        credentialName: bookinfo-credential
      hosts:
      - 192.168.229.100
    - port:
        number: 15017
        protocol: https
        name: https-WEBHOOK
      tls:
        mode: SIMPLE
        credentialName: bookinfo-credential
      hosts:
      - 192.168.229.100
---
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
   name: external-istiod-vs
   namespace: external-istiod
spec:
    hosts:
    - 192.168.229.100
    gateways:
    - external-istiod-gw
    http:
    - match:
      - port: 15012
      route:
      - destination:
          host: istiod.external-istiod.svc.cluster.local
          port:
            number: 15012
    - match:
      - port: 15017
      route:
      - destination:
          host: istiod.external-istiod.svc.cluster.local
          port:
            number: 443
---
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: external-istiod-dr
  namespace: external-istiod
spec:
  host: istiod.external-istiod.svc.cluster.local
  trafficPolicy:
    portLevelSettings:
    - port:
        number: 15012
      tls:
        mode: SIMPLE
      connectionPool:
        http:
          h2UpgradePolicy: UPGRADE
    - port:
        number: 443
      tls:
        mode: SIMPLE
EOF


kubectl apply -f external-istiod-gw.yaml 
```

