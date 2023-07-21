<h2 style="color:red;align:center;" align=“center”>istio业务权限控制，原来可以这么玩</h2>

# 1用jwt获取认证信息

## 1.1jwt介绍

 jwt 是 JSON web token ，为了在网络应用环境中传递声明而执行的一种基于json的开放标准。非常适用于分布式的单点登录场景。主要是用来校验身份提供者和服务提供者之间传递的用户身份信息。 

## 1.2istio如何用jwt

### 1.2.1写死



```
cat << EOF > ra-productpage-jwtrules-audiences.yaml
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "productpage"
spec:
  selector:
    matchLabels:
      app: productpage
  jwtRules:
  - issuer: "testing@secure.istio.io"
    outputPayloadToHeader: auth
    audiences:
    - "app"
    jwks: |
      { "keys":
         [
           {
             "e":"AQAB",
             "kid":"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ",
             "kty":"RSA",
             "n":"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ"
           }
         ]
      }
EOF

kubectl apply -f ra-productpage-jwtrules-audiences.yaml -n istio
```

创建RequestAuthentication，实现jwt

测试

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.134:30986/productpage -H "Authorization: Bearer ${TOKEN}"
```



### 1.2.2url的方式



```
cat << EOF > ra-productpage-jwtrules-jwksUri.yaml
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "productpage"
spec:
  selector:
    matchLabels:
      app: productpage
  jwtRules:
  - issuer: "testing@secure.istio.io"
    jwksUri: http://jwt-server.istio.svc.cluster.local:8000
    outputPayloadToHeader: auth
EOF

kubectl apply -f ra-productpage-jwtrules-jwksUri.yaml -n istio
```

创建RequestAuthentication，实现jwt



```
cat << EOF > jwt-server.yaml
apiVersion: v1
kind: Service
metadata:
  name: jwt-server
  labels:
    app: jwt-server
spec:
  ports:
  - name: http
    port: 8000
    targetPort: 8000
  selector:
    app: jwt-server
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jwt-server
spec:
  replicas: 1
  selector:
    matchLabels:
      app: jwt-server
  template:
    metadata:
      labels:
        app: jwt-server
    spec:
      containers:
      - image: docker.io/istio/jwt-server:0.5
        imagePullPolicy: IfNotPresent
        name: jwt-server
        ports:
        - containerPort: 8000
---
EOF

kubectl apply -f jwt-server.yaml -n istio
```

创建jwt服务器

测试

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.198.154:30986/productpage -H "Authorization: Bearer ${TOKEN}"
```



# 2基于AuthorizationPolicy Custom Action实现

1创建opa策略

opa介绍

http://blog.newbmiao.com/2020/03/13/opa-quick-start.html

验证opa

https://play.openpolicyagent.org/p/ZXkIlAEPCY

```
cat << EOF > policy.rego 
package envoy.authz

import input.attributes.request.http as http_request

default allow = false

token = {"payload": payload} {
    [_, encoded] := split(http_request.headers.authorization, " ")
    [_, payload, _] := io.jwt.decode(encoded)
}

allow {
    action_allowed
}


bar := "bar"

action_allowed {
  bar ==token.payload.foo
}

EOF
```

2创建secret

  kubectl create secret generic opa-policy --from-file policy.rego  -n istio

3创建opa

```
cat << EOF > opa-deployment.yaml
apiVersion: v1
kind: Service
metadata:
  name: opa
  labels:
    app: opa
spec:
  ports:
  - name: grpc
    port: 9191
    targetPort: 9191
  selector:
    app: opa
---
kind: Deployment
apiVersion: apps/v1
metadata:
  name: opa
  labels:
    app: opa
spec:
  replicas: 1
  selector:
    matchLabels:
      app: opa
  template:
    metadata:
      labels:
        app: opa
    spec:
      containers:
        - name: opa
          image: openpolicyagent/opa:latest-envoy
          securityContext:
            runAsUser: 1111
          volumeMounts:
          - readOnly: true
            mountPath: /policy
            name: opa-policy
          args:
          - "run"
          - "--server"
          - "--addr=localhost:8181"
          - "--diagnostic-addr=0.0.0.0:8282"
          - "--set=plugins.envoy_ext_authz_grpc.addr=:9191"
          - "--set=plugins.envoy_ext_authz_grpc.query=data.envoy.authz.allow"
          - "--set=decision_logs.console=true"
          - "--ignore=.*"
          - "/policy/policy.rego"
          ports:
          - containerPort: 9191
          livenessProbe:
            httpGet:
              path: /health?plugins
              scheme: HTTP
              port: 8282
            initialDelaySeconds: 5
            periodSeconds: 5
          readinessProbe:
            httpGet:
              path: /health?plugins
              scheme: HTTP
              port: 8282
            initialDelaySeconds: 5
            periodSeconds: 5
      volumes:
        - name: opa-policy
          secret:
            secretName: opa-policy
 
kubectl apply -f opa-deployment.yaml -n istio
```

4编辑meshconfig

 kubectl edit configmap istio -n istio-system 

```
  mesh: |-
    # Add the following contents:
    extensionProviders:
    - name: "opa.istio"
      envoyExtAuthzGrpc:
        service: "opa.istio.svc.cluster.local"
        port: "9191"
```

5创建ap

```
cat << EOF >ext-authz.yaml
apiVersion: security.istio.io/v1beta1
kind: AuthorizationPolicy
metadata:
 name: ext-authz
 namespace: istio-system
spec:
 selector:
   matchLabels:
     app: istio-ingressgateway
 action: CUSTOM
 provider:
   name: "opa.istio"
 rules:
 - to:
   - operation:
       paths: ["/productpage"]
EOF

kubectl apply -f ext-authz.yaml -n istio-system
```



# 3基于LuaFilter实现

## 3.1先进行jwt认证

```
cat << EOF > ra-productpage-jwtrules-audiences.yaml
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "productpage"
spec:
  selector:
    matchLabels:
      app: productpage
  jwtRules:
  - issuer: "testing@secure.istio.io"
    outputPayloadToHeader: auth
    audiences:
    - "app"
    jwks: |
      { "keys":
         [
           {
             "e":"AQAB",
             "kid":"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ",
             "kty":"RSA",
             "n":"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ"
           }
         ]
      }
EOF

kubectl apply -f ra-productpage-jwtrules-audiences.yaml -n istio
```



## 3.2进行权限认证

```
cat << EOF > ef-http-filter-lua.yaml
apiVersion: networking.istio.io/v1alpha3
kind: EnvoyFilter
metadata:
  name: apply-to
spec:
  workloadSelector:
    labels:
      app: productpage
  configPatches:
  - applyTo: HTTP_FILTER
    match:
      context: SIDECAR_INBOUND
      listener:
        portNumber: 9080
        filterChain:
          filter:
            name: "envoy.filters.network.http_connection_manager"
            subFilter:
              name: "envoy.filters.http.router"
    patch:
      operation: INSERT_BEFORE
      value: 
       name: envoy.filters.http.lua
       typed_config:
          "@type": "type.googleapis.com/envoy.extensions.filters.http.lua.v3.Lua"
          inlineCode: |
                   function envoy_on_request(handle)
                      handle:logWarn(" ============= envoy_on_request ============= ")
                      local headers = handle:headers()
                      local authToken = headers:get("auth")
                      handle:logWarn(authToken)
                      local headers, body = request_handle:httpCall(
                          "outbound|8080||auth-simple.istio.svc.cluster.local",
                          {
                            [":method"] = "GET",
                            [":path"] = "/auth",
                            [":authority"] = "auth-simple:8080"
                          },
                          authToken,
                          5000)
                          if(body=="fail")
                          then
                              request_handle:respond(
                                {[":status"] = "403",
                                 ["upstream_foo"] = headers["foo"]},
                                "nope")
                          end
                         
                      handle:logWarn(" ============================================= ")
                    end
            
EOF

kubectl apply -f ef-http-filter-lua.yaml -n istio
```

从jwt传过来的auth都里获取用户信息，传到认证服务器进行认证

# 4基于wasm实现

## 4.1什么实wasm

 WASM 的诞生源自前端，是一种为了解决日益复杂的前端 web 应用以及有限的 JavaScript 性能而诞生的技术。它本身并不是一种语言，而是一种字节码标准，一个“编译目标”。WASM 字节码和机器码非常接近，因此可以非常快速的装载运行。任何一种语言，都可以被编译成 WASM 字节码，然后在 WASM 虚拟机中执行（本身是为 web 设计，必然天然跨平台，同时为了沙箱运行保障安全，所以直接编译成机器码并不是最佳选择）。理论上，所有语言，包括 JavaScript、C、C++、Rust、Go、Java 等都可以编译成 WASM 字节码并在 WASM 虚拟机中执行。 

istio中的wasm，是一种扩展机制，主要用来扩展envoy的功能，以wasm filter的方式运行在envoy中。

## 4.2怎么用wasm实现权限控制

### 4.2.1安装wasme

 wasme 是 solo.io 提供的一个命令行工具，一个简单的类比就是：docker cli 之于容器镜像，wasme 之于 WASM 扩展。 

```
下载wasme
https://github.com/solo-io/wasm/releases
mkdir .wasme/bin -p
mv wasme-linux-amd64 ./.wasme/bin/wasme
chmod +x .wasme/bin/wasme
vi /etc/profile
export PATH=$HOME/.wasme/bin:$PATH
. /etc/profile

[root@master01 istio-teaching]# wasme --version
wasme version 0.0.33
```

## 4.2.2先进行jwt认证

```
cat << EOF > ra-productpage-jwtrules-audiences.yaml
apiVersion: "security.istio.io/v1beta1"
kind: "RequestAuthentication"
metadata:
  name: "productpage"
spec:
  selector:
    matchLabels:
      app: productpage
  jwtRules:
  - issuer: "testing@secure.istio.io"
    outputPayloadToHeader: auth
    audiences:
    - "app"
    jwks: |
      { "keys":
         [
           {
             "e":"AQAB",
             "kid":"DHFbpoIUqrY8t2zpA2qXfCmr5VO5ZEr4RzHU_-envvQ",
             "kty":"RSA",
             "n":"xAE7eB6qugXyCAG3yhh7pkDkT65pHymX-P7KfIupjf59vsdo91bSP9C8H07pSAGQO1MV_xFj9VswgsCg4R6otmg5PV2He95lZdHtOcU5DXIg_pbhLdKXbi66GlVeK6ABZOUW3WYtnNHD-91gVuoeJT_DwtGGcp4ignkgXfkiEm4sw-4sfb4qdt5oLbyVpmW6x9cfa7vs2WTfURiCrBoUqgBo_-4WTiULmmHSGZHOjzwa8WtrtOQGsAFjIbno85jp6MnGGGZPYZbDAa_b3y5u-YpW7ypZrvD8BgtKVjgtQgZhLAGezMt0ua3DRrWnKqTZ0BJ_EyxOGuHJrLsn00fnMQ"
           }
         ]
      }
EOF

kubectl apply -f ra-productpage-jwtrules-audiences.yaml -n istio
```



### 4.2.3进行权限认证

创建项目

```
 wasme init . --language=rust --platform=istio --platform-version=1.9.x
```

修改程序

```
// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

use log::debug;
use proxy_wasm::traits::*;
use proxy_wasm::types::*;
use std::time::Duration;

#[no_mangle]
pub fn _start() {
    proxy_wasm::set_http_context(|_, _| -> Box<dyn HttpContext> { Box::new(HttpAuth) });
}

struct HttpAuth;

impl HttpAuth {
    fn fail(&mut self,message: &str) {
      debug!("auth: allowed");
      //self.send_http_response(403, vec![], Some(b"not authorized"));
      self.send_http_response(403, vec![], Some(message.as_bytes()));
    }
}

// Implement http functions related to this request.
// This is the core of the filter code.
impl HttpContext for HttpAuth {

    // This callback will be invoked when request headers arrive
    fn on_http_request_headers(&mut self, _: usize) -> Action {
        // get all the request headers
        let userId = self.get_http_request_header("auth").unwrap_or(String::from(""));
        // transform them from Vec<(String,String)> to Vec<(&str,&str)>; as dispatch_http_call needs
        // Vec<(&str,&str)>.
       //let ref_headers : Vec<(&str,&str)> = headers.iter().map(|(ref k,ref v)|(k.as_str(),v.as_str())).collect();

        // Dispatch a call to the auth-cluster. Here we assume that envoy's config has a cluster
        // named auth-cluster. We send the auth cluster all our headers, so it has context to
        // perform auth decisions.
        let res = self.dispatch_http_call(
            "outbound|8080||auth-simple.istio.svc.cluster.local", // cluster name
            vec![
                (":method", "GET"),
                (":path", "/auth"),
                (":authority", "auth-simple:8080"),
                ("userId", userId.as_str()),
                ], // headers
            None, // no body
            vec![], // no trailers
            Duration::from_secs(5), // one second timeout
        );

        // If dispatch reutrn an error, fail the request.
        match res {
            Err(_) =>{
                self.fail("");
            }
            Ok(_)  => {}
        }

        // the dispatch call is asynchronous. This means it returns immediatly, while the request
        // happens in the background. When the response arrives `on_http_call_response` will be 
        // called. In the mean time, we need to pause the request, so it doesn't continue upstream.
        Action::Pause
    }

    fn on_http_response_headers(&mut self, _: usize) -> Action {
        // Add a header on the response.
        //self.set_http_response_header("Hello", Some("world"));
        Action::Continue
    }
}

impl Context for HttpAuth {
    fn on_http_call_response(&mut self, _ : u32, header_size: usize, _body_size: usize, _num_trailers: usize) {
        // We have a response to the http call!

        // if we have no headers, it means the http call failed. Fail the incoming request as well.
        //if header_size == 0 {
        //    self.fail();
        //    return;
        //}

        // Check if the auth server returned "200", if so call `resume_http_request` so request is
        // sent upstream.
        // Otherwise, fail the incoming request.
        //match self.get_http_request_header(":status") {
        //    Some(ref status) if status == "200"  => {
        //        self.resume_http_request();
        //        return;
        //    }
        //    _ => {
        //        debug!("auth: not authorized");
        //        self.fail();
        //    }
        //}

        if let Some(body) = self.get_http_call_response_body(0, _body_size) {
            if let Ok(body) = std::str::from_utf8(&body) {
               //self.set_http_response_header("Hello", Some(body)); 
               if body == "ok" {
                    self.resume_http_request();
                    return;
                }
                debug!("auth: not authorized");
                //self.send_http_response(403, vec![], Some(body.as_bytes()));
                self.fail(body);
            }
        }
    }
}
```

代码说明：

```
let res = self.dispatch_http_call(
            "outbound|8080||auth-simple.istio.svc.cluster.local", // cluster name
            vec![
                (":method", "GET"),
                (":path", "/auth"),
                (":authority", "auth-simple:8080"),
                ("userId", userId.as_str()),
                ], // headers
            None, // no body
            vec![], // no trailers
            Duration::from_secs(5), // one second timeout
        );
这一段向远程服务器发送验证权限请求，第一个参数必须事envoy的cluster名称，这个要注意
方法get,请求路径是/auth,userId是我们传过去的认证参数

if let Some(body) = self.get_http_call_response_body(0, _body_size) {
            if let Ok(body) = std::str::from_utf8(&body) {
               //self.set_http_response_header("Hello", Some(body)); 
               if body == "ok" {
                    self.resume_http_request();
                    return;
                }
                debug!("auth: not authorized");
                //self.send_http_response(403, vec![], Some(body.as_bytes()));
                self.fail(body);
            }
        }
这一段从远程认证服务器获取返回，如果返回内容是ok，就通过权限，否则就返回错误给前端。
```

构建

```
wasme build rust  .  -t webassemblyhub.io/hxpmark/auth-rs:0.07 --image=quay.mirrors.ustc.edu.cn/solo-io/ee-builder:0.0.33
```

push

```
wasme push webassemblyhub.io/hxpmark/auth-rs:0.07
```

部署

```
 wasme deploy istio webassemblyhub.io/hxpmark/auth-rs:0.07 --id=auth-rs --labels app=productpage --namespace=istio 
```



auth服务器代码

```
package com.mark;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestHeader;


@SpringBootApplication
@RestController
public class SpringBootDemoHelloworldApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootDemoHelloworldApplication.class, args);
	}


	@GetMapping("/auth")
	public String auth(@RequestHeader("userId") String userId) {
		//String userId=request.getHeader("UserId");
		if ("admin".equals(userId)){
			return "ok";
		}
		return "fail";
	}
}

```

构建auth镜像

```
docker  build  . --tag registry.cn-hangzhou.aliyuncs.com/hxpdocker/auth-simple:1.1

docker push registry.cn-hangzhou.aliyuncs.com/hxpdocker/auth-simple:1.1
```

```
[root@master01 auth-simple]# cat Dockerfile 
FROM java:8

ADD  ./auth-simple.jar /app.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
```



部署auth-simple

```
cat << EOF > k8s.yaml 
apiVersion: v1
kind: Service
metadata:
  name: auth-simple
  labels:
    app: auth-simple
spec:
  ports:
    - port: 8080
      name: http
      protocol: TCP
  selector:
    app: auth-simple
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-simple
  labels:
    app: auth-simple
    version: v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: auth-simple
      version: v1
  template:
    metadata:
      labels:
        app: auth-simple
        version: v1
    spec:
      containers:
        - name: auth-simple
          image: registry.cn-hangzhou.aliyuncs.com/hxpdocker/auth-simple:1.5
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8080
---
EOF

kubectl apply -f k8s.yaml -n istio
```

测试：

```
TOKEN=eyJhbGciOiJSUzI1NiIsImtpZCI6IkRIRmJwb0lVcXJZOHQyenBBMnFYZkNtcjVWTzVaRXI0UnpIVV8tZW52dlEiLCJ0eXAiOiJKV1QifQ.eyJleHAiOjM1MzczOTExMDQsImdyb3VwcyI6WyJncm91cDEiLCJncm91cDIiXSwiaWF0IjoxNTM3MzkxMTA0LCJpc3MiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyIsInNjb3BlIjpbInNjb3BlMSIsInNjb3BlMiJdLCJzdWIiOiJ0ZXN0aW5nQHNlY3VyZS5pc3Rpby5pbyJ9.EdJnEZSH6X8hcyEii7c8H5lnhgjB5dwo07M5oheC8Xz8mOllyg--AHCFWHybM48reunF--oGaG6IXVngCEpVF0_P5DwsUoBgpPmK1JOaKN6_pe9sh0ZwTtdgK_RP01PuI7kUdbOTlkuUi2AO-qUyOm7Art2POzo36DLQlUXv8Ad7NBOqfQaKjE9ndaPWT7aexUsBHxmgiGbz1SyLH879f7uHYPbPKlpHU6P9S-DaKnGLaEchnoKnov7ajhrEhGXAQRukhDPKUHO9L30oPIr5IJllEQfHYtt6IZvlNUGeLUcif3wpry1R5tBXRicx2sXMQ7LyuDremDbcNy_iE76Upg

curl 192.168.229.134:30986/productpage -H "Authorization: Bearer ${TOKEN}"
```

