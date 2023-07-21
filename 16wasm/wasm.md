# wasm是什么

 WASM 的诞生源自前端，是一种为了解决日益复杂的前端 web 应用以及有限的 JavaScript 性能而诞生的技术。它本身并不是一种语言，而是一种字节码标准，一个“编译目标”。WASM 字节码和机器码非常接近，因此可以非常快速的装载运行。任何一种语言，都可以被编译成 WASM 字节码，然后在 WASM 虚拟机中执行（本身是为 web 设计，必然天然跨平台，同时为了沙箱运行保障安全，所以直接编译成机器码并不是最佳选择）。理论上，所有语言，包括 JavaScript、C、C++、Rust、Go、Java 等都可以编译成 WASM 字节码并在 WASM 虚拟机中执行。 

![11](images\11.jpg)



# wasme

 wasme 是 solo.io 提供的一个命令行工具，一个简单的类比就是：docker cli 之于容器镜像，wasme 之于 WASM 扩展。 

The tool for building, pushing, and deploying Envoy WebAssembly Filters

# 安装wasme

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

# wasme命令

```
The tool for building, pushing, and deploying Envoy WebAssembly Filters

Usage:
  wasme [command]

Available Commands:
  build       Build a wasm image from the filter source directory.
  deploy      Deploy an Envoy WASM Filter to the data plane (Envoy proxies).
  help        Help about any command
  init        Initialize a project directory for a new Envoy WASM Filter.
  list        List Envoy WASM Filters stored locally or published to webassemblyhub.io.
  login       Log in so you can push images to the remote server.
  pull        Pull wasm filters from remote registry
  push        Push a wasm filter to remote registry
  tag         Create a tag TARGET_IMAGE that refers to SOURCE_IMAGE
  undeploy    Remove a deployed Envoy WASM Filter from the data plane (Envoy proxies).

Flags:
  -h, --help      help for wasme
  -v, --verbose   verbose output
      --version   version for wasme

Use "wasme [command] --help" for more information about a command
```

## init

```
The provided --language flag will determine the programming language used for the new filter. The default is 
C++.

The provided --platform flag will determine the target platform used for the new filter. This is important to 
ensure compatibility between the filter and the 

If --language, --platform, or --platform-version are not provided, the CLI will present an interactive prompt. Disable the prompt with --disable-prompt

Usage:
  wasme init DEST_DIRECTORY [--language=FILTER_LANGUAGE] [--platform=TARGET_PLATFORM] [--platform-version=TARGET_PLATFORM_VERSION] [flags]

Flags:
      --disable-prompt            Disable the interactive prompt if a required parameter is not passed. If set to true and one of the required flags is not provided, wasme CLI will return an error.
  -h, --help                      help for init
      --language string           The programming language with which to create the filter. Supported languages are: [cpp rust assemblyscript tinygo]
      --platform string           The name of the target platform against which to build. Supported platforms are: [gloo istio]
      --platform-version string   The version of the target platform against which to build. Supported Istio versions are: [1.5.x 1.6.x 1.7.x 1.8.x 1.9.x]. Supported Gloo versions are: [1.3.x 1.5.x 1.6.x]

Global Flags:
  -v, --verbose   verbose output
```

```
[root@master01 wasm]# wasme init auth --language=assemblyscript --platform=istio --platform-version=1.9.x
INFO[0000] extracting 1812 bytes to /root/istio入门到精通/istio-teaching/wasm/auth
```



## build

```
Options for the build are specific to the target language.

Usage:
  wasme build [command]

Available Commands:
  assemblyscript Build a wasm image from an AssemblyScript filter using NPM-in-Docker
  cpp            Build a wasm image from a CPP filter using Bazel-in-Docker
  precompiled    Build a wasm image from a Precompiled filter.
  rust           Build a wasm image from a Rust filter using Bazel-in-Docker
  tinygo         Build a wasm image from a TinyGo filter using TinyGo-in-Docker

Flags:
  -c, --config string    The path to the filter configuration file for the image. If not specified, defaults to <SOURCE_DIRECTOR>/runtime-config.json. This file must be present in order to build the image.
  -h, --help             help for build
  -i, --image string     Name of the docker image containing the Bazel run instructions. Modify to run a custom builder image (default "quay.io/solo-io/ee-builder:0.0.33")
      --store string     Set the path to the local storage directory for wasm images. Defaults to $HOME/.wasme/store
  -t, --tag string       The image ref with which to tag this image. Specified in the format <name:tag>. Required
      --tmp-dir string   Directory for storing temporary files during build. Defaults to /tmp on OSx and Linux. If unset, temporary files will be removed after build

Global Flags:
  -v, --verbose   verbose output

Use "wasme build [command] --help" for more information about a command.
```

```
wasme build assemblyscript . --tag webassemblyhub.io/hxpmark/auth:0.01 --image=quay.mirrors.ustc.edu.cn/solo-io/ee-builder:0.0.33
```



## login

```
Caches credentials for image pushes in the provided credentials-file (defaults to $HOME/.wasme/credentials.json).

Provide -s=SERVER_ADDRESS to provide login credentials for a registry other than webassemblyhub.io.

Usage:
  wasme login [-s SERVER_ADDRESS] -u USERNAME -p PASSWORD  [flags]

Flags:
      --credentials-file string   write to this credentials file. defaults to $HOME/.wasme/credentials.json
  -h, --help                      help for login
  -p, --password string           login password
      --plaintext                 use plaintext to connect to the remote registry (HTTP) rather than HTTPS
  -s, --server string             the address of the remote registry to which to authenticate (default "webassemblyhub.io")
  -u, --username string           login username

Global Flags:
  -v, --verbose   verbose output
```

```
[root@master01 ~]# wasme login --username=195446040@qq.com --password=Hh12345678
INFO[0006] Successfully logged in as 195446040@qq.com (xiaopinghuang) 
INFO[0006] stored credentials in /root/.wasme/credentials.json 
```



## push

```
wasme push webassemblyhub.io/my/filter:v1

Usage:
  wasme push name[:tag|@digest] [flags]

Flags:
  -c, --config stringArray   path to auth config
  -h, --help                 help for push
      --insecure             allow connections to SSL registry without certs
  -p, --password string      registry password
      --plain-http           use plain http and not https
      --store string         Set the path to the local storage directory for wasm images. Defaults to $HOME/.wasme/store
  -u, --username string      registry username

Global Flags:
  -v, --verbose   verbose output
```

```
[root@master01 auth]# wasme push webassemblyhub.io/hxpmark/auth:0.01
INFO[0000] Pushing image webassemblyhub.io/hxpmark/auth:0.01 
INFO[0010] Pushed webassemblyhub.io/hxpmark/auth:0.01   
INFO[0010] Digest: sha256:d696cba6bd95e6f0e45d87fe5698da7c27ff036477a57c7fe3d1f0708042d92c 
```



## pull

```
Pull wasm filters from remote registry

Usage:
  wasme pull <name:tag|name@digest> [flags]

Flags:
  -c, --config stringArray   path to auth config
  -h, --help                 help for pull
      --insecure             allow connections to SSL registry without certs
  -p, --password string      registry password
      --plain-http           use plain http and not https
      --store string         Set the path to the local storage directory for wasm images. Defaults to $HOME/.wasme/store
  -u, --username string      registry username

Global Flags:
  -v, --verbose   verbose output
```

```
[root@master01 auth]# wasme pull webassemblyhub.io/hxpmark/auth:0.01
INFO[0000] Pulling image webassemblyhub.io/hxpmark/auth:0.01 
INFO[0005] Image: webassemblyhub.io/hxpmark/auth:0.01   
INFO[0005] Digest: sha256:a515a5d244b021c753f2e36c744e03a109cff6f5988e34714dbe725c904fa917 
```



## deploy

```
Deploys an Envoy WASM Filter to Envoy instances.

You must provide a value for --id which will become the unique ID of the deployed filter. When using --provider=istio, the ID must be a valid Kubernetes resource name.

You must specify --root-id unless a default root id is provided in the image configuration. Use --root-id to select the filter to run if the wasm image contains more than one filter.

Usage:
  wasme deploy [command]

Available Commands:
  envoy       Run Envoy locally in Docker and attach a WASM Filter.
  gloo        Deploy an Envoy WASM Filter to the Gloo Gateway Proxies (Envoy).
  istio       Deploy an Envoy WASM Filter to Istio Sidecar Proxies (Envoy).

Flags:
      --config string    optional config that will be passed to the filter. accepts an inline string.
  -h, --help             help for deploy
      --id string        unique id for naming the deployed filter. this is used for logging as well as removing the filter. when running wasme deploy istio, this name must be a valid Kubernetes resource name.
      --root-id string   optional root ID used to bind the filter at the Envoy level. this value is normally read from the filter image directly.

Global Flags:
  -v, --verbose   verbose output

Use "wasme deploy [command] --help" for more information about a command.
```

### istio

```
Deploy an Envoy WASM Filter to Istio Sidecar Proxies (Envoy).

wasme uses the EnvoyFilter Istio Custom Resource to pull and run wasm filters.
wasme deploys a server-side cache component which runs in cluster and pulls filter images.

If --name is not provided, all deployments in the targeted namespace will attach the filter.

Note: currently only Istio 1.5.x - 1.9.x are supported.

Usage:
  wasme deploy istio <image> --id=<unique name> [--config=<inline string>] [--root-id=<root id>] [--namespaces <comma separated namespaces>] [--name deployment-name] [--patch-context={any|inbound|outbound|gateway}] [flags]

Flags:
      --cache-custom-command strings     custom command to provide to the cache server image
      --cache-image-pull-policy string   image pull policy for the cache server daemonset. see https://kubernetes.io/docs/concepts/containers/images/ (default "IfNotPresent")
      --cache-name string                name of resources for the wasm image cache server (default "wasme-cache")
      --cache-namespace string           namespace of resources for the wasm image cache server (default "wasme")
      --cache-repo string                name of the image repository to use for the cache server daemonset (default "quay.io/solo-io/wasme")
      --cache-tag string                 image tag to use for the cache server daemonset (default "0.0.33")
      --cache-timeout duration           the length of time to wait for the server-side filter cache to pull the filter image before giving up with an error. set to 0 to skip the check entirely (note, this may produce a known race condition). (default 1m0s)
  -h, --help                             help for istio
      --ignore-version-check             set to disable abi version compatability check.
      --istio-namespace string           the namespace where the Istio control plane is installed (default "istio-system")
      --istiod-name string               deployment name of the istiod (default "istiod")
  -l, --labels stringToString            labels of the deployment or daemonset into which to inject the filter. if not set, will apply to all workloads in the target namespace (default [])
  -n, --namespace string                 namespace of the workload(s) to inject the filter. (default "default")
      --patch-context string             patch context of the filter. possible values are any, inbound, outbound, gateway (default "inbound")
  -t, --workload-type string             type of workload into which the filter should be injected. possible values are daemonset, deployment, statefulset (default "deployment")

Global Flags:
      --config string    optional config that will be passed to the filter. accepts an inline string.
      --id string        unique id for naming the deployed filter. this is used for logging as well as removing the filter. when running wasme deploy istio, this name must be a valid Kubernetes resource name.
      --root-id string   optional root ID used to bind the filter at the Envoy level. this value is normally read from the filter image directly.
  -v, --verbose          verbose output
```

```
[root@master01 auth]# wasme deploy istio webassemblyhub.io/hxpmark/auth:0.01 --id=test --labels app=productpage --namespace=istio 
INFO[0000] cache namespace already exists                cache=wasme-cache.wasme image="quay.io/solo-io/wasme:0.0.33"
INFO[0000] cache configmap already exists                cache=wasme-cache.wasme image="quay.io/solo-io/wasme:0.0.33"
INFO[0000] cache service account already exists          cache=wasme-cache.wasme image="quay.io/solo-io/wasme:0.0.33"
INFO[0000] cache role updated                            cache=wasme-cache.wasme image="quay.io/solo-io/wasme:0.0.33"
INFO[0000] cache rolebinding updated                     cache=wasme-cache.wasme image="quay.io/solo-io/wasme:0.0.33"
INFO[0000] cache daemonset updated                       cache=wasme-cache.wasme image="quay.io/solo-io/wasme:0.0.33"
INFO[0005] image is already cached                       cache="{wasme-cache wasme}" image="webassemblyhub.io/hxpmark/auth:0.01"
INFO[0005] updated workload sidecar annotations          filter="id:\"test\" image:\"webassemblyhub.io/hxpmark/auth:0.01\" rootID:\"add_header\" patchContext:\"inbound\" " workload=productpage-v1
INFO[0005] created Istio EnvoyFilter resource            envoy_filter_resource=productpage-v1-test.istio filter="id:\"test\" image:\"webassemblyhub.io/hxpmark/auth:0.01\" rootID:\"add_header\" patchContext:\"inbound\" " workload=productpage-v1
```



### envoy

```
This command runs Envoy locally in docker using a static bootstrap configuration which includes 
the specified WASM filter image. 

The bootstrap can be generated from an internal default or a modified config provided by the user with --bootstrap.

The generated bootstrap config can be output to a file with --out. If using this option, Envoy will not be started locally.

Usage:
  wasme deploy envoy <image> [--config=<filter config>] [--bootstrap=<custom envoy bootstrap file>] [--envoy-image=<custom envoy image>] [flags]

Flags:
  -b, --bootstrap wasme deploy envoy   Path to an Envoy bootstrap config. If set, wasme deploy envoy will run Envoy locally using the provided configuration file. Set -in=- to use stdin. If empty, will use a default configuration template with a single route to `jsonplaceholder.typicode.com`.
      --docker-run-args docker run     Set to provide additional args to the docker run command used to launch Envoy. Ignored if --out is set.
  -e, --envoy-image string             Name of the Docker image containing the Envoy binary (default "docker.io/istio/proxyv2:1.5.1")
      --envoy-run-args envoy           Set to provide additional args to the envoy command used to launch Envoy. Ignored if --out is set.
  -h, --help                           help for envoy
      --out string                     If set, write the modified Envoy configuration to this file instead of launching Envoy. Set -out=- to use stdout.
      --store string                   Set the path to the local storage directory for wasm images. Defaults to $HOME/.wasme/store

Global Flags:
      --config string    optional config that will be passed to the filter. accepts an inline string.
      --id string        unique id for naming the deployed filter. this is used for logging as well as removing the filter. when running wasme deploy istio, this name must be a valid Kubernetes resource name.
      --root-id string   optional root ID used to bind the filter at the Envoy level. this value is normally read from the filter image directly.
  -v, --verbose          verbose output
```



## list

```
List Envoy WASM Filters stored locally or published to webassemblyhub.io.

Usage:
  wasme list [flags]

Flags:
  -h, --help                            help for list
      --published                       Set to true to list images that have been published to a remote registry. If unset, lists images stored in local image cache.
      --search wasme list --published   Search images from the remote registry. If unset, wasme list --published will return all public repositories.
  -s, --server string                   If using --published, read images from this remote registry. (default "webassemblyhub.io")
  -d, --show-dir                        Set to true to show the local directories for images. Does not apply to published images.
      --store string                    Set the path to the local storage directory for wasm images. Defaults to $HOME/.wasme/store. Ignored if using --published
  -w, --wide                            Set to true to list images with their full tag length.

Global Flags:
  -v, --verbose   verbose output
```

```
[root@master01 auth]# wasme list 
NAME                                 TAG    SIZE    SHA      UPDATED
webassemblyhub.io/hxpmark/add-header v0.0.1 12.6 kB a515a5d2 07 May 21 12:29 CST
webassemblyhub.io/hxpmark/auth       0.01   12.6 kB a515a5d2 28 Aug 21 13:58 CST
```

```
[root@master01 auth]# wasme list  --published --search auth
NAME                                            TAG    SIZE     SHA      UPDATED
webassemblyhub.io/akubala/authz-headerz         0.0.1  239.9 kB 4ead4a11 14 Jan 21 10:54 UTC
webassemblyhub.io/akubala/authz-headerz         0.0.2  237.9 kB fd6cc663 14 Jan 21 12:29 UTC
webassemblyhub.io/akubala/authz-headerz         0.0.3  237.9 kB 8ce72539 14 Jan 21 12:48 UTC
webassemblyhub.io/akubala/authz-headerz         0.0.4  237.9 kB 817236fb 14 Jan 21 12:53 UTC
webassemblyhub.io/akubala/authz-headerz         0.0.5  242.2 kB b92bc8c2 14 Jan 21 13:20 UTC
webassemblyhub.io/akubala/authz-headerz         0.0.6  243.6 kB 6b8e18e8 14 Jan 21 13:27 UTC
webassemblyhub.io/akubala/authz-headerz         0.0.7  242.2 kB 48d1f8c7 14 Jan 21 13:38 UTC
webassemblyhub.io/akubala/authz-headerz         0.0.8  243.6 kB 91d7fc2f 14 Jan 21 13:51 UTC
webassemblyhub.io/akubala/authz-headerz         0.0.9  243.0 kB 348f34ee 14 Jan 21 13:59 UTC
webassemblyhub.io/akubala/authz-headerz         0.0.10 236.2 kB e8f747ce 14 Jan 21 14:14 UTC
webassemblyhub.io/akubala/authz-headerz         0.0.11 244.8 kB 4c829545 14 Jan 21 14:24 UTC
webassemblyhub.io/akubala/authz-headerz         0.0.12 244.9 kB c41e44c3 14 Jan 21 14:37 UTC
webassemblyhub.io/akubala/authz-headerz         0.0.14 240.6 kB 93891059 14 Jan 21 14:44 UTC
webassemblyhub.io/ashiskum/http_auth_random     v0.1   70.9 kB  bee8844c 29 Apr 20 03:26 UTC
webassemblyhub.io/bochuxt/auth                  v0.1   26.7 kB  fd64e837 24 Apr 20 04:19 UTC
webassemblyhub.io/canthefason/authz-filter      v0.1   1.0 MB   d7a78022 02 Sep 20 21:53 UTC
webassemblyhub.io/canthefason/authz-filter      v0.2   1.0 MB   43577ee1 02 Sep 20 22:34 UTC
webassemblyhub.io/hxpmark/auth                  0.01   13.9 kB  d696cba6 28 Aug 21 05:59 UTC
webassemblyhub.io/jianshao/auth-filter          v0.1.0 25.2 kB  9a4d425e 13 Nov 20 03:26 UTC
webassemblyhub.io/jianshao/authz-filter         v0.0.1 17.7 kB  30512543 20 May 20 03:39 UTC
webassemblyhub.io/jianshao/authz-filter         v0.0.2 31.9 kB  37cece54 09 Nov 20 09:09 UTC
webassemblyhub.io/jianshao/authz-filter         v0.0.3 28.1 kB  2de24d7a 10 Nov 20 09:49 UTC
webassemblyhub.io/luufery/eciton-auth           v0.0.1 290.6 kB fa26768b 28 Dec 20 06:19 UTC
webassemblyhub.io/mayster/auth                  0.1.0  259.8 kB 5f5aa13e 20 Apr 21 16:10 UTC
webassemblyhub.io/mayster/tinygo-auth           v0.1   259.8 kB 4cf26752 20 Apr 21 14:51 UTC
webassemblyhub.io/mayster/tinygo-auth           v0.2   248.7 kB 6f991cba 20 Apr 21 15:33 UTC
webassemblyhub.io/mwrzesinski/envoy-auth-filter v0.1   2.1 MB   9a159858 25 May 20 02:48 UTC
webassemblyhub.io/sanjo/go-auth-random          v1.0   84.3 kB  da0dde81 13 Apr 20 06:37 UTC
webassemblyhub.io/sanjo/http_auth_random        v1.0   3.5 MB   570d895c 11 Apr 20 03:27 UTC
webassemblyhub.io/taegyunk/http-auth            v0.1   54.6 kB  512ad6ed 20 Jul 20 13:42 UTC
webassemblyhub.io/tanglonghao/auth-filter       v0.1   13.8 kB  d3e98936 20 Jul 20 05:27 UTC
webassemblyhub.io/tanglonghao/auth-filter       v0.4   13.5 kB  ae2df7d2 20 Jul 20 06:13 UTC
webassemblyhub.io/tanglonghao/auth-filter       v0.5   13.5 kB  ae2df7d2 20 Jul 20 06:18 UTC
webassemblyhub.io/tanglonghao/auth-filter       v0.6   13.5 kB  a295352d 20 Jul 20 06:45 UTC
webassemblyhub.io/tanglonghao/auth-filter       v0.7   13.5 kB  17613b6d 20 Jul 20 06:56 UTC
webassemblyhub.io/tanglonghao/auth-filter       v0.8   13.2 kB  fee7d378 20 Jul 20 06:59 UTC
webassemblyhub.io/tanglonghao/auth-filter       v0.9   13.2 kB  bfb3df15 22 Jul 20 03:04 UTC
webassemblyhub.io/thesisworker/authn_filter     v0.1   1.1 MB   1b64dfe0 08 Jun 21 22:13 UTC
```



## tag

```
Create a tag TARGET_IMAGE that refers to SOURCE_IMAGE

Usage:
  wasme tag SOURCE_IMAGE[:TAG] TARGET_IMAGE[:TAG] [flags]

Flags:
  -h, --help           help for tag
      --store string   Set the path to the local storage directory for wasm images. Defaults to $HOME/.wasme/store

Global Flags:
  -v, --verbose   verbose output
```

```
[root@master01 auth]# wasme tag webassemblyhub.io/hxpmark/auth:0.01 webassemblyhub.io/hxpmark/auth:0.02
INFO[0000] tagged image                                  digest="sha256:a515a5d244b021c753f2e36c744e03a109cff6f5988e34714dbe725c904fa917" image="webassemblyhub.io/hxpmark/auth:0.01"
[root@master01 auth]# wasme list
NAME                                 TAG    SIZE    SHA      UPDATED
webassemblyhub.io/hxpmark/add-header v0.0.1 12.6 kB a515a5d2 07 May 21 12:29 CST
webassemblyhub.io/hxpmark/auth       0.01   12.6 kB a515a5d2 28 Aug 21 13:58 CST
webassemblyhub.io/hxpmark/auth       0.02   12.6 kB a515a5d2 28 Aug 21 14:06 CST
```



## undeploy

```
Removes a deployed Envoy WASM Filter from Envoy instances.

Usage:
  wasme undeploy [command]

Available Commands:
  gloo        Remove an Envoy WASM Filter from the Gloo Gateway Proxies (Envoy).
  istio       Remove an Envoy WASM Filter from the Istio Sidecar Proxies (Envoy).

Flags:
      --dry-run     print output any configuration changes to stdout rather than applying them to the target file / kubernetes cluster
  -h, --help        help for undeploy
      --id string   unique id for naming the deployed filter. this is used for logging as well as removing the filter. when running wasme deploy istio, this name must be a valid Kubernetes resource name.

Global Flags:
  -v, --verbose   verbose output

Use "wasme undeploy [command] --help" for more information about a command.
```

### istio

```
wasme uses the Istio EnvoyFilter CR to pull and run wasm filters.

Use --namespace to target workload(s) in a the namespaces of Gateway CRs to update.
Use --name to target a specific workload (deployment or daemonset) in the target namespace. If unspecified, all deployments 
in the namespace will be targeted.

Usage:
  wasme undeploy istio --id=<unique name> --namespace=<deployment namespace> [--name=<deployment name>] [flags]

Flags:
      --cache-timeout duration   the length of time to wait for the server-side filter cache to pull the filter image before giving up with an error. set to 0 to skip the check entirely (note, this may produce a known race condition). (default 1m0s)
      --config string            optional config that will be passed to the filter. accepts an inline string.
  -h, --help                     help for istio
      --ignore-version-check     set to disable abi version compatability check.
      --istio-namespace string   the namespace where the Istio control plane is installed (default "istio-system")
      --istiod-name string       deployment name of the istiod (default "istiod")
  -l, --labels stringToString    labels of the deployment or daemonset into which to inject the filter. if not set, will apply to all workloads in the target namespace (default [])
  -n, --namespace string         namespace of the workload(s) to inject the filter. (default "default")
      --patch-context string     patch context of the filter. possible values are any, inbound, outbound, gateway (default "inbound")
      --root-id string           optional root ID used to bind the filter at the Envoy level. this value is normally read from the filter image directly.
  -t, --workload-type string     type of workload into which the filter should be injected. possible values are daemonset, deployment, statefulset (default "deployment")

Global Flags:
      --dry-run     print output any configuration changes to stdout rather than applying them to the target file / kubernetes cluster
      --id string   unique id for naming the deployed filter. this is used for logging as well as removing the filter. when running wasme deploy istio, this name must be a valid Kubernetes resource name.
  -v, --verbose     verbose output
```

```
[root@master01 auth]# wasme undeploy istio --id=test --labels app=productpage --namespace=istio
INFO[0000] removing filter from one or more workloads...  filter=test params="{map[app:productpage] istio deployment}"
INFO[0000] removing sidecar annotations from workload    filter=test workload=productpage-v1
INFO[0000] deleted Istio EnvoyFilter resource            filter=productpage-v1-test
```



# FilterDeployment

## 部署

```
istio-teaching/wasm/wasme.io_v1_crds.yaml
kubectl apply -f wasme.io_v1_crds.yaml

istio-teaching/wasm/wasme-default.yaml
kubectl apply -f wasme-default.yaml

[root@master01 wasm]# kubectl get pod -n wasme
NAME                              READY   STATUS    RESTARTS   AGE
wasme-cache-kb669                 1/1     Running   0          21s
wasme-cache-qz7wn                 1/1     Running   0          27s
wasme-cache-xxf8t                 1/1     Running   0          8s
wasme-operator-6fcb4c5d8b-zz8fp   1/1     Running   0          32s
```



## 资源详解

wasm/add-header.yaml

kubectl apply -f add-header.yaml -n istio

```

apiVersion: wasme.io/v1
kind: FilterDeployment
metadata:
  labels:
    app: wasme
  name: add-header
  namespace: istio
spec:
  deployment:
    istio:
      kind: Deployment
      labels:
        app: productpage
  filter:
    config:
      '@type': type.googleapis.com/google.protobuf.StringValue
      value: world
    image: webassemblyhub.io/hxpmark/add-header:v0.0.1
 
```



## 原理

![how-it-works](images\how-it-works.png)

# wasm编写

## cpp



## assemblyScript



## rust

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
        let userId = self.get_http_request_header("userId").unwrap_or(String::from(""));
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
wasme undeploy istio --id=auth-rs --labels app=productpage --namespace=istio
```



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



部署auth-simple 1.4

auth-simple/k8s.yaml

kubectl apply -f k8s.yaml -n istio

```
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
```

curl http://bookinfo.demo:31002/productpage --header "userId: admin" -I

curl http://bookinfo.demo:31002/productpage --header "userId: mark" -I

