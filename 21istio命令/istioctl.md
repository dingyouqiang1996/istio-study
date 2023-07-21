# 命令帮助

```
[root@node01 ~]# istioctl
Istio configuration command line utility for service operators to
debug and diagnose their Istio mesh.

Usage:
  istioctl [command]

Available Commands:
  admin                Manage control plane (istiod) configuration
  analyze              Analyze Istio configuration and print validation messages
  authz                (authz is experimental. Use `istioctl experimental authz`)
  bug-report           Cluster information and log capture support tool.
  completion           Generate the autocompletion script for the specified shell
  create-remote-secret Create a secret with credentials to allow Istio to access remote Kubernetes apiservers
  dashboard            Access to Istio web UIs
  experimental         Experimental commands that may be modified or deprecated
  help                 Help about any command
  install              Applies an Istio manifest, installing or reconfiguring Istio on a cluster.
  kube-inject          Inject Istio sidecar into Kubernetes pod resources
  manifest             Commands related to Istio manifests
  operator             Commands related to Istio operator controller.
  profile              Commands related to Istio configuration profiles
  proxy-config         Retrieve information about proxy configuration from Envoy [kube only]
  proxy-status         Retrieves the synchronization status of each Envoy in the mesh [kube only]
  remote-clusters      Lists the remote clusters each istiod instance is connected to.
  tag                  Command group used to interact with revision tags
  upgrade              Upgrade Istio control plane in-place
  validate             Validate Istio policy and rules files
  verify-install       Verifies Istio Installation Status
  version              Prints out build version information

Flags:
      --context string          The name of the kubeconfig context to use
  -h, --help                    help for istioctl
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Additional help topics:
  istioctl options                           Displays istioctl global options
```

# admin

用于管理istiod配置

```
[root@node01 ~]# istioctl admin
A group of commands used to manage istiod configuration

Usage:
  istioctl admin [flags]
  istioctl admin [command]

Aliases:
  admin, istiod

Examples:
  # Retrieve information about istiod configuration.
  istioctl admin log

Available Commands:
  log         Manage istiod logging.

Flags:
  -h, --help              help for admin
  -l, --selector string   label selector (default "app=istiod")

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl admin [command] --help" for more information about a command.
```

## log

```
[root@node01 ~]# istioctl admin log --help
Retrieve or update logging levels of istiod components.

Usage:
  istioctl admin log [<pod-name>] [--level <scope>:<level>][--stack-trace-level <scope>:<level>]|[-r|--reset]|[--output|-o short|yaml] [flags]

Aliases:
  log, l

Examples:
  # Retrieve information about istiod logging levels.
  istioctl admin log

  # Retrieve information about istiod logging levels on a specific control plane pod.
  istioctl admin l istiod-5c868d8bdd-pmvgg

  # Update levels of the specified loggers.
  istioctl admin log --level ads:debug,authorization:debug

  # Reset levels of all the loggers to default value (info).
  istioctl admin log -r


Flags:
      --ctrlz_port int             ControlZ port (default 9876)
  -h, --help                       help for log
      --level string               Comma-separated list of output logging level for scopes in format <scope>:<level>[,<scope>:<level>,...]Possible values for <level>: none, error, warn, info, debug
  -o, --output string              Output format: one of json|short (default "short")
  -r, --reset                      Reset levels to default value. (info)
      --stack-trace-level string   Comma-separated list of stack trace level  for scopes in format <scope>:<stack-trace-level>[,<scope>:<stack-trace-level>,...] Possible values for <stack-trace-level>: none, error, warn, info, debug

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
  -l, --selector string         label selector (default "app=istiod")
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```



```
[root@node01 ~]# istioctl admin log
Active scopes:
  ads:info
  adsc:info
  analysis:info
  authn:info
  authorization:info
  ca:info
  controllers:info
  default:info
  delta:info
  file:info
  gateway:info
  grpcgen:info
  installer:info
  klog:info
  kube:info
  model:info
  monitor:info
  pkica:info
  pkira:info
  processing:info
  proxyconfig:info
  retry:info
  rootcertrotator:info
  secretcontroller:info
  serverca:info
  serviceentry:info
  spiffe:info
  status:info
  telemetry:info
  tpath:info
  trustBundle:info
  util:info
  validation:info
  validationController:info
  validationServer:info
  wasm:info
  wle:info
```



```
[root@node01 ~]# istioctl admin log istiod-8495d444bb-vvnpn 
Active scopes:
  ads:info
  adsc:info
  analysis:info
  authn:info
  authorization:info
  ca:info
  controllers:info
  default:info
  delta:info
  file:info
  gateway:info
  grpcgen:info
  installer:info
  klog:info
  kube:info
  model:info
  monitor:info
  pkica:info
  pkira:info
  processing:info
  proxyconfig:info
  retry:info
  rootcertrotator:info
  secretcontroller:info
  serverca:info
  serviceentry:info
  spiffe:info
  status:info
  telemetry:info
  tpath:info
  trustBundle:info
  util:info
  validation:info
  validationController:info
  validationServer:info
  wasm:info
  wle:info
```



```
 istioctl admin log --level ads:debug,authorization:debug
```



```
istioctl admin log -r
```



```
[root@node01 ~]# istioctl admin log --ctrlz_port 9876
Active scopes:
  ads:info
  adsc:info
  analysis:info
  authn:info
  authorization:info
  ca:info
  controllers:info
  default:info
  delta:info
  file:info
  gateway:info
  grpcgen:info
  installer:info
  klog:info
  kube:info
  model:info
  monitor:info
  pkica:info
  pkira:info
  processing:info
  proxyconfig:info
  retry:info
  rootcertrotator:info
  secretcontroller:info
  serverca:info
  serviceentry:info
  spiffe:info
  status:info
  telemetry:info
  tpath:info
  trustBundle:info
  util:info
  validation:info
  validationController:info
  validationServer:info
  wasm:info
  wle:info
```



```
[root@node01 ~]#  istioctl admin log -o json
[
  {
    "scope_name": "ads",
    "log_level": "info"
  },
  {
    "scope_name": "adsc",
    "log_level": "info"
  },
  {
    "scope_name": "analysis",
    "log_level": "info"
  },
  {
    "scope_name": "authn",
    "log_level": "info"
  },
  {
    "scope_name": "authorization",
    "log_level": "info"
  },
  {
    "scope_name": "ca",
    "log_level": "info"
  },
  {
    "scope_name": "controllers",
    "log_level": "info"
  },
  {
    "scope_name": "default",
    "log_level": "info"
  },
  {
    "scope_name": "delta",
    "log_level": "info"
  },
  {
    "scope_name": "file",
    "log_level": "info"
  },
  {
    "scope_name": "gateway",
    "log_level": "info"
  },
  {
    "scope_name": "grpcgen",
    "log_level": "info"
  },
  {
    "scope_name": "installer",
    "log_level": "info"
  },
  {
    "scope_name": "klog",
    "log_level": "info"
  },
  {
    "scope_name": "kube",
    "log_level": "info"
  },
  {
    "scope_name": "model",
    "log_level": "info"
  },
  {
    "scope_name": "monitor",
    "log_level": "info"
  },
  {
    "scope_name": "pkica",
    "log_level": "info"
  },
  {
    "scope_name": "pkira",
    "log_level": "info"
  },
  {
    "scope_name": "processing",
    "log_level": "info"
  },
  {
    "scope_name": "proxyconfig",
    "log_level": "info"
  },
  {
    "scope_name": "retry",
    "log_level": "info"
  },
  {
    "scope_name": "rootcertrotator",
    "log_level": "info"
  },
  {
    "scope_name": "secretcontroller",
    "log_level": "info"
  },
  {
    "scope_name": "serverca",
    "log_level": "info"
  },
  {
    "scope_name": "serviceentry",
    "log_level": "info"
  },
  {
    "scope_name": "spiffe",
    "log_level": "info"
  },
  {
    "scope_name": "status",
    "log_level": "info"
  },
  {
    "scope_name": "telemetry",
    "log_level": "info"
  },
  {
    "scope_name": "tpath",
    "log_level": "info"
  },
  {
    "scope_name": "trustBundle",
    "log_level": "info"
  },
  {
    "scope_name": "util",
    "log_level": "info"
  },
  {
    "scope_name": "validation",
    "log_level": "info"
  },
  {
    "scope_name": "validationController",
    "log_level": "info"
  },
  {
    "scope_name": "validationServer",
    "log_level": "info"
  },
  {
    "scope_name": "wasm",
    "log_level": "info"
  },
  {
    "scope_name": "wle",
    "log_level": "info"
  }
]
```



# analyze

分析配置文件，打印校验信息

```
[root@node01 ~]# istioctl analyze --help
Analyze Istio configuration and print validation messages

Usage:
  istioctl analyze <file>... [flags]

Examples:
  # Analyze the current live cluster
  istioctl analyze

  # Analyze the current live cluster, simulating the effect of applying additional yaml files
  istioctl analyze a.yaml b.yaml my-app-config/

  # Analyze the current live cluster, simulating the effect of applying a directory of config recursively
  istioctl analyze --recursive my-istio-config/

  # Analyze yaml files without connecting to a live cluster
  istioctl analyze --use-kube=false a.yaml b.yaml my-app-config/

  # Analyze the current live cluster and suppress PodMissingProxy for pod mypod in namespace 'testing'.
  istioctl analyze -S "IST0103=Pod mypod.testing"

  # Analyze the current live cluster and suppress PodMissingProxy for all pods in namespace 'testing',
  # and suppress MisplacedAnnotation on deployment foobar in namespace default.
  istioctl analyze -S "IST0103=Pod *.testing" -S "IST0107=Deployment foobar.default"

  # List available analyzers
  istioctl analyze -L

Flags:
  -A, --all-namespaces            Analyze all namespaces
      --color                     Default true.  Disable with '=false' or set $TERM to dumb (default true)
      --failure-threshold Level   The severity level of analysis at which to set a non-zero exit code. Valid values: [Info Warning Error] (default Error)
  -h, --help                      help for analyze
      --ignore-unknown            Don't complain about un-parseable input documents, for cases where analyze should run only on k8s compliant inputs.
  -L, --list-analyzers            List the analyzers available to run. Suppresses normal execution.
      --meshConfigFile string     Overrides the mesh config values to use for analysis.
  -o, --output string             Output format: one of [log json yaml] (default "log")
      --output-threshold Level    The severity level of analysis at which to display messages. Valid values: [Info Warning Error] (default Info)
  -R, --recursive                 Process directory arguments recursively. Useful when you want to analyze related manifests organized within the same directory.
  -S, --suppress stringArray      Suppress reporting a message code on a specific resource. Values are supplied in the form <code>=<resource> (e.g. '--suppress "IST0102=DestinationRule primary-dr.default"'). Can be repeated. You can include the wildcard character '*' to support a partial match (e.g. '--suppress "IST0102=DestinationRule *.default" ).
      --timeout duration          The duration to wait before failing (default 30s)
  -k, --use-kube                  Use live Kubernetes cluster for analysis. Set --use-kube=false to analyze files only. (default true)
  -v, --verbose                   Enable verbose output

Global Flags:
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
  -n, --namespace string    Config namespace
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

示例

```
[root@node01 ~]# istioctl analyze
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
```

```
[root@node01 gateway]# istioctl analyze gateway-01.yaml 
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
```

```
[root@node01 istioctl]# istioctl analyze --recursive myconfig/
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
```

```
[root@node01 gateway]# istioctl analyze --use-kube=false gateway-01.yaml 
2022-06-19T02:47:07.732697Z     warn     core/v1/Namespace resource [/istio-system] could not be found

✔ No validation issues found when analyzing gateway-01.yaml.
```

```
[root@node01 gateway]# istioctl analyze --suppress "IST0102=Pod *.istio"
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
```

```
[root@node01 gateway]# istioctl analyze --suppress "IST0103=Pod *.istio" -S "IST0107=Deployment productpage-v1.istio"
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
```

```
[root@node01 gateway]# istioctl analyze --list-analyzers
* annotations.K8sAnalyzer:
    Checks for misplaced and invalid Istio annotations in Kubernetes resources
* applicationUID.Analyzer:
    Checks invalid application UID
* auth.AuthorizationPoliciesAnalyzer:
    Checks the validity of authorization policies
* deployment.MultiServiceAnalyzer:
    Checks association between services and pods
* deprecation.DeprecationAnalyzer:
    Checks for deprecated Istio types and fields
* destinationrule.CaCertificateAnalyzer:
    Checks if caCertificates is set when TLS mode is SIMPLE/MUTUAL
* envoyfilter.EnvoyPatchAnalyzer:
    Checks an envoyFilters 
* gateway.CertificateAnalyzer:
    Checks a gateway certificate
* gateway.ConflictingGatewayAnalyzer:
    Checks a gateway's selector, port number and hosts
* gateway.IngressGatewayPortAnalyzer:
    Checks a gateway's ports against the gateway's Kubernetes service ports
* gateway.SecretAnalyzer:
    Checks a gateway's referenced secrets for correctness
* injection.Analyzer:
    Checks conditions related to Istio sidecar injection
* injection.ImageAnalyzer:
    Checks the image of auto-injection configured with the running proxies on pods
* injection.ImageAutoAnalyzer:
    Makes sure that Pods and Deployments with `image: auto` are going to be injected
* meshnetworks.MeshNetworksAnalyzer:
    Check the validity of MeshNetworks in the cluster
* schema.ValidationAnalyzer.AuthorizationPolicy:
    Runs schema validation as an analyzer on 'AuthorizationPolicy' resources
* schema.ValidationAnalyzer.DestinationRule:
    Runs schema validation as an analyzer on 'DestinationRule' resources
* schema.ValidationAnalyzer.EnvoyFilter:
    Runs schema validation as an analyzer on 'EnvoyFilter' resources
* schema.ValidationAnalyzer.Gateway:
    Runs schema validation as an analyzer on 'Gateway' resources
* schema.ValidationAnalyzer.MeshConfig:
    Runs schema validation as an analyzer on 'MeshConfig' resources
* schema.ValidationAnalyzer.MeshNetworks:
    Runs schema validation as an analyzer on 'MeshNetworks' resources
* schema.ValidationAnalyzer.PeerAuthentication:
    Runs schema validation as an analyzer on 'PeerAuthentication' resources
* schema.ValidationAnalyzer.ProxyConfig:
    Runs schema validation as an analyzer on 'ProxyConfig' resources
* schema.ValidationAnalyzer.RequestAuthentication:
    Runs schema validation as an analyzer on 'RequestAuthentication' resources
* schema.ValidationAnalyzer.ServiceEntry:
    Runs schema validation as an analyzer on 'ServiceEntry' resources
* schema.ValidationAnalyzer.Sidecar:
    Runs schema validation as an analyzer on 'Sidecar' resources
* schema.ValidationAnalyzer.Telemetry:
    Runs schema validation as an analyzer on 'Telemetry' resources
* schema.ValidationAnalyzer.VirtualService:
    Runs schema validation as an analyzer on 'VirtualService' resources
* schema.ValidationAnalyzer.WasmPlugin:
    Runs schema validation as an analyzer on 'WasmPlugin' resources
* schema.ValidationAnalyzer.WorkloadEntry:
    Runs schema validation as an analyzer on 'WorkloadEntry' resources
* schema.ValidationAnalyzer.WorkloadGroup:
    Runs schema validation as an analyzer on 'WorkloadGroup' resources
* service.PortNameAnalyzer:
    Checks the port names associated with each service
* serviceentry.Analyzer:
    Checks the validity of ServiceEntry
* sidecar.DefaultSelectorAnalyzer:
    Validates that there aren't multiple sidecar resources that have no selector
* sidecar.SelectorAnalyzer:
    Validates that sidecars that define a workload selector match at least one pod, and that there aren't multiple sidecar resources that select overlapping pods
* virtualservice.ConflictingMeshGatewayHostsAnalyzer:
    Checks if multiple virtual services associated with the mesh gateway have conflicting hosts
* virtualservice.DestinationHostAnalyzer:
    Checks the destination hosts associated with each virtual service
* virtualservice.DestinationRuleAnalyzer:
    Checks the destination rules associated with each virtual service
* virtualservice.GatewayAnalyzer:
    Checks the gateways associated with each virtual service
* virtualservice.JWTClaimRouteAnalyzer:
    Checks the VirtualService using JWT claim based routing has corresponding RequestAuthentication
* virtualservice.RegexAnalyzer:
    Checks regex syntax
* webhook.Analyzer:
    Checks the validity of Istio webhooks
```

```
[root@node01 gateway]#  istioctl analyze --all-namespaces
Info [IST0102] (Namespace argo-rollouts) The namespace is not enabled for Istio injection. Run 'kubectl label namespace argo-rollouts istio-injection=enabled' to enable it, or 'kubectl label namespace argo-rollouts istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace efk) The namespace is not enabled for Istio injection. Run 'kubectl label namespace efk istio-injection=enabled' to enable it, or 'kubectl label namespace efk istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace gateway-system) The namespace is not enabled for Istio injection. Run 'kubectl label namespace gateway-system istio-injection=enabled' to enable it, or 'kubectl label namespace gateway-system istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace vm-httpd) The namespace is not enabled for Istio injection. Run 'kubectl label namespace vm-httpd istio-injection=enabled' to enable it, or 'kubectl label namespace vm-httpd istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0118] (Service argo-rollouts/argo-rollouts-metrics) Port name metrics (port: 8090, targetPort: 8090) doesn't follow the naming convention of Istio port.
```

```
[root@node01 gateway]#  istioctl analyze -n istio

✔ No validation issues found when analyzing namespace: istio.
```

```
[root@node01 gateway]#  istioctl analyze -n istio --timeout=1s

✔ No validation issues found when analyzing namespace: istio.
```

```
[root@node01 gateway]# istioctl analyze --all-namespaces --output json
[
        {
                "code": "IST0102",
                "documentationUrl": "https://istio.io/v1.14/docs/reference/config/analysis/ist0102/?ref=istioctl-analyze",
                "level": "Info",
                "message": "The namespace is not enabled for Istio injection. Run 'kubectl label namespace argo-rollouts istio-injection=enabled' to enable it, or 'kubectl label namespace argo-rollouts istio-injection=disabled' to explicitly mark it as not needing injection.",
                "origin": "Namespace argo-rollouts"
        },
        {
                "code": "IST0102",
                "documentationUrl": "https://istio.io/v1.14/docs/reference/config/analysis/ist0102/?ref=istioctl-analyze",
                "level": "Info",
                "message": "The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.",
                "origin": "Namespace default"
        },
        {
                "code": "IST0102",
                "documentationUrl": "https://istio.io/v1.14/docs/reference/config/analysis/ist0102/?ref=istioctl-analyze",
                "level": "Info",
                "message": "The namespace is not enabled for Istio injection. Run 'kubectl label namespace efk istio-injection=enabled' to enable it, or 'kubectl label namespace efk istio-injection=disabled' to explicitly mark it as not needing injection.",
                "origin": "Namespace efk"
        },
        {
                "code": "IST0102",
                "documentationUrl": "https://istio.io/v1.14/docs/reference/config/analysis/ist0102/?ref=istioctl-analyze",
                "level": "Info",
                "message": "The namespace is not enabled for Istio injection. Run 'kubectl label namespace gateway-system istio-injection=enabled' to enable it, or 'kubectl label namespace gateway-system istio-injection=disabled' to explicitly mark it as not needing injection.",
                "origin": "Namespace gateway-system"
        },
        {
                "code": "IST0102",
                "documentationUrl": "https://istio.io/v1.14/docs/reference/config/analysis/ist0102/?ref=istioctl-analyze",
                "level": "Info",
                "message": "The namespace is not enabled for Istio injection. Run 'kubectl label namespace vm-httpd istio-injection=enabled' to enable it, or 'kubectl label namespace vm-httpd istio-injection=disabled' to explicitly mark it as not needing injection.",
                "origin": "Namespace vm-httpd"
        },
        {
                "code": "IST0118",
                "documentationUrl": "https://istio.io/v1.14/docs/reference/config/analysis/ist0118/?ref=istioctl-analyze",
                "level": "Info",
                "message": "Port name metrics (port: 8090, targetPort: 8090) doesn't follow the naming convention of Istio port.",
                "origin": "Service argo-rollouts/argo-rollouts-metrics"
        }
]
```

```
[root@node01 gateway]# istioctl analyze --all-namespaces --output yaml
- code: IST0102
  documentationUrl: https://istio.io/v1.14/docs/reference/config/analysis/ist0102/?ref=istioctl-analyze
  level: Info
  message: The namespace is not enabled for Istio injection. Run 'kubectl label namespace
    argo-rollouts istio-injection=enabled' to enable it, or 'kubectl label namespace
    argo-rollouts istio-injection=disabled' to explicitly mark it as not needing injection.
  origin: Namespace argo-rollouts
- code: IST0102
  documentationUrl: https://istio.io/v1.14/docs/reference/config/analysis/ist0102/?ref=istioctl-analyze
  level: Info
  message: The namespace is not enabled for Istio injection. Run 'kubectl label namespace
    default istio-injection=enabled' to enable it, or 'kubectl label namespace default
    istio-injection=disabled' to explicitly mark it as not needing injection.
  origin: Namespace default
- code: IST0102
  documentationUrl: https://istio.io/v1.14/docs/reference/config/analysis/ist0102/?ref=istioctl-analyze
  level: Info
  message: The namespace is not enabled for Istio injection. Run 'kubectl label namespace
    efk istio-injection=enabled' to enable it, or 'kubectl label namespace efk istio-injection=disabled'
    to explicitly mark it as not needing injection.
  origin: Namespace efk
- code: IST0102
  documentationUrl: https://istio.io/v1.14/docs/reference/config/analysis/ist0102/?ref=istioctl-analyze
  level: Info
  message: The namespace is not enabled for Istio injection. Run 'kubectl label namespace
    gateway-system istio-injection=enabled' to enable it, or 'kubectl label namespace
    gateway-system istio-injection=disabled' to explicitly mark it as not needing
    injection.
  origin: Namespace gateway-system
- code: IST0102
  documentationUrl: https://istio.io/v1.14/docs/reference/config/analysis/ist0102/?ref=istioctl-analyze
  level: Info
  message: The namespace is not enabled for Istio injection. Run 'kubectl label namespace
    vm-httpd istio-injection=enabled' to enable it, or 'kubectl label namespace vm-httpd
    istio-injection=disabled' to explicitly mark it as not needing injection.
  origin: Namespace vm-httpd
- code: IST0118
  documentationUrl: https://istio.io/v1.14/docs/reference/config/analysis/ist0118/?ref=istioctl-analyze
  level: Info
  message: 'Port name metrics (port: 8090, targetPort: 8090) doesn''t follow the naming
    convention of Istio port.'
  origin: Service argo-rollouts/argo-rollouts-metrics
```

```
[root@node01 gateway]# istioctl analyze --all-namespaces --output log
Info [IST0102] (Namespace argo-rollouts) The namespace is not enabled for Istio injection. Run 'kubectl label namespace argo-rollouts istio-injection=enabled' to enable it, or 'kubectl label namespace argo-rollouts istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace efk) The namespace is not enabled for Istio injection. Run 'kubectl label namespace efk istio-injection=enabled' to enable it, or 'kubectl label namespace efk istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace gateway-system) The namespace is not enabled for Istio injection. Run 'kubectl label namespace gateway-system istio-injection=enabled' to enable it, or 'kubectl label namespace gateway-system istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace vm-httpd) The namespace is not enabled for Istio injection. Run 'kubectl label namespace vm-httpd istio-injection=enabled' to enable it, or 'kubectl label namespace vm-httpd istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0118] (Service argo-rollouts/argo-rollouts-metrics) Port name metrics (port: 8090, targetPort: 8090) doesn't follow the naming convention of Istio port.
```

```
[root@node01 gateway]#  istioctl analyze --all-namespaces --output-threshold=info
Info [IST0102] (Namespace argo-rollouts) The namespace is not enabled for Istio injection. Run 'kubectl label namespace argo-rollouts istio-injection=enabled' to enable it, or 'kubectl label namespace argo-rollouts istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace efk) The namespace is not enabled for Istio injection. Run 'kubectl label namespace efk istio-injection=enabled' to enable it, or 'kubectl label namespace efk istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace gateway-system) The namespace is not enabled for Istio injection. Run 'kubectl label namespace gateway-system istio-injection=enabled' to enable it, or 'kubectl label namespace gateway-system istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace vm-httpd) The namespace is not enabled for Istio injection. Run 'kubectl label namespace vm-httpd istio-injection=enabled' to enable it, or 'kubectl label namespace vm-httpd istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0118] (Service argo-rollouts/argo-rollouts-metrics) Port name metrics (port: 8090, targetPort: 8090) doesn't follow the naming convention of Istio port.
```

```
[root@node01 gateway]# istioctl analyze --all-namespaces --output-threshold=warning

✔ No validation issues found when analyzing all namespaces.
```

```
[root@node01 gateway]# istioctl analyze --all-namespaces --output-threshold=error

✔ No validation issues found when analyzing all namespaces.
```

```
[root@node01 gateway]# istioctl analyze --all-namespaces --failure-threshold=error
Info [IST0102] (Namespace argo-rollouts) The namespace is not enabled for Istio injection. Run 'kubectl label namespace argo-rollouts istio-injection=enabled' to enable it, or 'kubectl label namespace argo-rollouts istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace efk) The namespace is not enabled for Istio injection. Run 'kubectl label namespace efk istio-injection=enabled' to enable it, or 'kubectl label namespace efk istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace gateway-system) The namespace is not enabled for Istio injection. Run 'kubectl label namespace gateway-system istio-injection=enabled' to enable it, or 'kubectl label namespace gateway-system istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace vm-httpd) The namespace is not enabled for Istio injection. Run 'kubectl label namespace vm-httpd istio-injection=enabled' to enable it, or 'kubectl label namespace vm-httpd istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0118] (Service argo-rollouts/argo-rollouts-metrics) Port name metrics (port: 8090, targetPort: 8090) doesn't follow the naming convention of Istio port.
```

```
[root@node01 gateway]#  istioctl analyze --all-namespaces --failure-threshold=warning
Info [IST0102] (Namespace argo-rollouts) The namespace is not enabled for Istio injection. Run 'kubectl label namespace argo-rollouts istio-injection=enabled' to enable it, or 'kubectl label namespace argo-rollouts istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace efk) The namespace is not enabled for Istio injection. Run 'kubectl label namespace efk istio-injection=enabled' to enable it, or 'kubectl label namespace efk istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace gateway-system) The namespace is not enabled for Istio injection. Run 'kubectl label namespace gateway-system istio-injection=enabled' to enable it, or 'kubectl label namespace gateway-system istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace vm-httpd) The namespace is not enabled for Istio injection. Run 'kubectl label namespace vm-httpd istio-injection=enabled' to enable it, or 'kubectl label namespace vm-httpd istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0118] (Service argo-rollouts/argo-rollouts-metrics) Port name metrics (port: 8090, targetPort: 8090) doesn't follow the naming convention of Istio port.
```

```
[root@node01 gateway]#  istioctl analyze --all-namespaces --failure-threshold=info
Info [IST0102] (Namespace argo-rollouts) The namespace is not enabled for Istio injection. Run 'kubectl label namespace argo-rollouts istio-injection=enabled' to enable it, or 'kubectl label namespace argo-rollouts istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace efk) The namespace is not enabled for Istio injection. Run 'kubectl label namespace efk istio-injection=enabled' to enable it, or 'kubectl label namespace efk istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace gateway-system) The namespace is not enabled for Istio injection. Run 'kubectl label namespace gateway-system istio-injection=enabled' to enable it, or 'kubectl label namespace gateway-system istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace vm-httpd) The namespace is not enabled for Istio injection. Run 'kubectl label namespace vm-httpd istio-injection=enabled' to enable it, or 'kubectl label namespace vm-httpd istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0118] (Service argo-rollouts/argo-rollouts-metrics) Port name metrics (port: 8090, targetPort: 8090) doesn't follow the naming convention of Istio port.
Error: Analyzers found issues when analyzing all namespaces.
See https://istio.io/v1.14/docs/reference/config/analysis for more information about causes and resolutions.
```

```
[root@node01 istioctl]# istioctl analyze --all-namespaces --meshConfigFile meshconfig.yaml 
Info [IST0102] (Namespace argo-rollouts) The namespace is not enabled for Istio injection. Run 'kubectl label namespace argo-rollouts istio-injection=enabled' to enable it, or 'kubectl label namespace argo-rollouts istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace efk) The namespace is not enabled for Istio injection. Run 'kubectl label namespace efk istio-injection=enabled' to enable it, or 'kubectl label namespace efk istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace gateway-system) The namespace is not enabled for Istio injection. Run 'kubectl label namespace gateway-system istio-injection=enabled' to enable it, or 'kubectl label namespace gateway-system istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace vm-httpd) The namespace is not enabled for Istio injection. Run 'kubectl label namespace vm-httpd istio-injection=enabled' to enable it, or 'kubectl label namespace vm-httpd istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0118] (Service argo-rollouts/argo-rollouts-metrics) Port name metrics (port: 8090, targetPort: 8090) doesn't follow the naming convention of Istio port.
```

```
[root@node01 istioctl]# istioctl analyze --all-namespaces --color=false
Info [IST0102] (Namespace argo-rollouts) The namespace is not enabled for Istio injection. Run 'kubectl label namespace argo-rollouts istio-injection=enabled' to enable it, or 'kubectl label namespace argo-rollouts istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace efk) The namespace is not enabled for Istio injection. Run 'kubectl label namespace efk istio-injection=enabled' to enable it, or 'kubectl label namespace efk istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace gateway-system) The namespace is not enabled for Istio injection. Run 'kubectl label namespace gateway-system istio-injection=enabled' to enable it, or 'kubectl label namespace gateway-system istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace vm-httpd) The namespace is not enabled for Istio injection. Run 'kubectl label namespace vm-httpd istio-injection=enabled' to enable it, or 'kubectl label namespace vm-httpd istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0118] (Service argo-rollouts/argo-rollouts-metrics) Port name metrics (port: 8090, targetPort: 8090) doesn't follow the naming convention of Istio port.
```

```
[root@node01 istioctl]# istioctl analyze --all-namespaces --verbose
Analyzed resources in all namespaces
Executed analyzers:
         annotations.K8sAnalyzer
         auth.AuthorizationPoliciesAnalyzer
         deployment.MultiServiceAnalyzer
         applicationUID.Analyzer
         deprecation.DeprecationAnalyzer
         gateway.IngressGatewayPortAnalyzer
         gateway.CertificateAnalyzer
         gateway.SecretAnalyzer
         gateway.ConflictingGatewayAnalyzer
         injection.Analyzer
         injection.ImageAnalyzer
         injection.ImageAutoAnalyzer
         meshnetworks.MeshNetworksAnalyzer
         service.PortNameAnalyzer
         sidecar.DefaultSelectorAnalyzer
         sidecar.SelectorAnalyzer
         virtualservice.ConflictingMeshGatewayHostsAnalyzer
         virtualservice.DestinationHostAnalyzer
         virtualservice.DestinationRuleAnalyzer
         virtualservice.GatewayAnalyzer
         virtualservice.JWTClaimRouteAnalyzer
         virtualservice.RegexAnalyzer
         destinationrule.CaCertificateAnalyzer
         serviceentry.Analyzer
         webhook.Analyzer
         envoyfilter.EnvoyPatchAnalyzer
         schema.ValidationAnalyzer.WasmPlugin
         schema.ValidationAnalyzer.MeshConfig
         schema.ValidationAnalyzer.MeshNetworks
         schema.ValidationAnalyzer.DestinationRule
         schema.ValidationAnalyzer.EnvoyFilter
         schema.ValidationAnalyzer.Gateway
         schema.ValidationAnalyzer.ServiceEntry
         schema.ValidationAnalyzer.Sidecar
         schema.ValidationAnalyzer.VirtualService
         schema.ValidationAnalyzer.WorkloadEntry
         schema.ValidationAnalyzer.WorkloadGroup
         schema.ValidationAnalyzer.ProxyConfig
         schema.ValidationAnalyzer.AuthorizationPolicy
         schema.ValidationAnalyzer.PeerAuthentication
         schema.ValidationAnalyzer.RequestAuthentication
         schema.ValidationAnalyzer.Telemetry

Info [IST0102] (Namespace argo-rollouts) The namespace is not enabled for Istio injection. Run 'kubectl label namespace argo-rollouts istio-injection=enabled' to enable it, or 'kubectl label namespace argo-rollouts istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace default) The namespace is not enabled for Istio injection. Run 'kubectl label namespace default istio-injection=enabled' to enable it, or 'kubectl label namespace default istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace efk) The namespace is not enabled for Istio injection. Run 'kubectl label namespace efk istio-injection=enabled' to enable it, or 'kubectl label namespace efk istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace gateway-system) The namespace is not enabled for Istio injection. Run 'kubectl label namespace gateway-system istio-injection=enabled' to enable it, or 'kubectl label namespace gateway-system istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0102] (Namespace vm-httpd) The namespace is not enabled for Istio injection. Run 'kubectl label namespace vm-httpd istio-injection=enabled' to enable it, or 'kubectl label namespace vm-httpd istio-injection=disabled' to explicitly mark it as not needing injection.
Info [IST0118] (Service argo-rollouts/argo-rollouts-metrics) Port name metrics (port: 8090, targetPort: 8090) doesn't follow the naming convention of Istio port.
```





# authz

```
[root@node01 istioctl]# istioctl authz --help
(authz is experimental. Use `istioctl experimental authz`)

Usage:
  istioctl authz [flags]

Flags:
  -h, --help   help for authz

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 istioctl]# istioctl x authz


THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl experimental authz [command]

Available Commands:
  check       Check AuthorizationPolicy applied in the pod.

Flags:
  -h, --help   help for authz

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl experimental authz [command] --help" for more information about a command.
```

## check

```
[root@node01 istioctl]# istioctl x authz check --help
Check prints the AuthorizationPolicy applied to a pod by directly checking
the Envoy configuration of the pod. The command is especially useful for inspecting
the policy propagation from Istiod to Envoy and the final AuthorizationPolicy list merged
from multiple sources (mesh-level, namespace-level and workload-level).

The command also supports reading from a standalone config dump file with flag -f.

Usage:
  istioctl experimental authz check [<type>/]<name>[.<namespace>] [flags]

Examples:
  # Check AuthorizationPolicy applied to pod httpbin-88ddbcfdd-nt5jb:
  istioctl x authz check httpbin-88ddbcfdd-nt5jb

  # Check AuthorizationPolicy applied to one pod under a deployment
  istioctl x authz check deployment/productpage-v1

  # Check AuthorizationPolicy from Envoy config dump file:
  istioctl x authz check -f httpbin_config_dump.json

Flags:
  -f, --file string   The json file with Envoy config dump to be checked
  -h, --help          help for check

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

示例

```
[root@node01 istioctl]# istioctl x authz check productpage-v1-85c4dcbb4c-gsjgw -n istio
ACTION   AuthorizationPolicy   RULES
```

```
[root@master01 istioctl]# istioctl x authz check -f rating_config_dump.json 
ACTION   AuthorizationPolicy   RULES
```

```
[root@node01 istioctl]# istioctl x authz check deploy/productpage-v1.istio
ACTION   AuthorizationPolicy   RULES
```

```
[root@node01 istioctl]# istioctl x authz check svc/productpage.istio
ACTION   AuthorizationPolicy   RULES
```



# bug-report

```
[root@node01 istioctl]# istioctl bug-report --help
bug-report selectively captures cluster information and logs into an archive to help diagnose problems.
Proxy logs can be filtered using:
  --include|--exclude ns1,ns2.../dep1,dep2.../pod1,pod2.../lbl1=val1,lbl2=val2.../ann1=val1,ann2=val2.../cntr1,cntr...
where ns=namespace, dep=deployment, lbl=label, ann=annotation, cntr=container

The filter spec is interpreted as 'must be in (ns1 OR ns2) AND (dep1 OR dep2) AND (cntr1 OR cntr2)...'
The log will be included only if the container matches at least one include filter and does not match any exclude filters.
All parts of the filter are optional and can be omitted e.g. ns1//pod1 filters only for namespace ns1 and pod1.
All names except label and annotation keys support '*' glob matching pattern.

e.g.
--include ns1,ns2 (only namespaces ns1 and ns2)
--include n*//p*/l=v* (pods with name beginning with 'p' in namespaces beginning with 'n' and having label 'l' with value beginning with 'v'.)

Usage:
  istioctl bug-report [flags]
  istioctl bug-report [command]

Available Commands:
  version     Prints out build version information

Flags:
      --critical-errs strings    List of comma separated glob patterns to match against log error strings. If any pattern matches an error in the log, the logs is given the highest priority for archive inclusion.
      --dir string               Set a specific directory for temporary artifact storage.
      --dry-run                  Only log commands that would be run, don't fetch or write.
      --duration duration        How far to go back in time from end-time for log entries to include in the archive. Default is infinity. If set, --start-time must be unset.
      --end-time string          End time for the range of log entries to include in the archive. Default is now.
      --exclude strings          Spec for which pod's proxy logs to exclude from the archive, after the include spec is processed. See above for format and examples. (default ["kube-node-lease,kube-public,kube-system,local-path-storage"])
  -f, --filename string          Path to a file containing configuration in YAML format. The file contents are applied over the default values and flag settings, with lists being replaced per JSON merge semantics.
      --full-secrets             If set, secret contents are included in output.
  -h, --help                     help for bug-report
      --ignore-errs strings      List of comma separated glob patterns to match against log error strings. Any error matching these patterns is ignored when calculating the log importance heuristic.
      --include strings          Spec for which pod's proxy logs to include in the archive. See above for format and examples.
      --istio-namespace string   Namespace where Istio control plane is installed. (default "istio-system")
      --start-time string        Start time for the range of log entries to include in the archive. Default is the infinite past. If set, --duration must be unset.
      --timeout duration         Maximum amount of time to spend fetching logs. When timeout is reached only the logs captured so far are saved to the archive. (default 30m0s)

Global Flags:
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl bug-report [command] --help" for more information about a command.
```

```
[root@node01 istioctl]# istioctl bug-report  version
version.BuildInfo{Version:"1.14.1", GitRevision:"f59ce19ec6b63bbb70a65c43ac423845f1129464", GolangVersion:"go1.18.2", BuildStatus:"Clean", GitTag:"1.14.1"}
```

```
istioctl bug-report  --dir=/root/report
```

```
 istioctl bug-report   --dry-run
```

```
istioctl bug-report  --duration=1s

```

```
istioctl bug-report  --exclude="istio,istio-system"
```

```
istioctl bug-report --full-secrets
```

```
istioctl bug-report --include=istio

istioctl bug-report --include "i*//p*/app=p*"
```

```
istioctl bug-report --include=istio --timeout=1s

istioctl bug-report --critical-errs  ".*productpage.*"

istioctl bug-report -f meshconfig.yaml 

istioctl bug-report --end-time "2022-06-20T13:06:42Z"

istioctl bug-report --ignore-errs ".*exception.*"

istioctl bug-report  --istio-namespace "istio-system"

istioctl bug-report  --start-time "2022-06-19T13:06:42Z"

```

```
[root@node01 ~]# istioctl bug-report  version --help
Prints out build version information

Usage:
  istioctl bug-report version [flags]

Flags:
  -h, --help            help for version
  -o, --output string   One of 'yaml' or 'json'.
  -s, --short           Use --short=false to generate full version information

Global Flags:
      --context string           Name of the kubeconfig Context to use.
      --critical-errs strings    List of comma separated glob patterns to match against log error strings. If any pattern matches an error in the log, the logs is given the highest priority for archive inclusion.
      --dir string               Set a specific directory for temporary artifact storage.
      --dry-run                  Only log commands that would be run, don't fetch or write.
      --duration duration        How far to go back in time from end-time for log entries to include in the archive. Default is infinity. If set, --start-time must be unset.
      --end-time string          End time for the range of log entries to include in the archive. Default is now.
      --exclude strings          Spec for which pod's proxy logs to exclude from the archive, after the include spec is processed. See above for format and examples. (default ["kube-node-lease,kube-public,kube-system,local-path-storage"])
  -f, --filename string          Path to a file containing configuration in YAML format. The file contents are applied over the default values and flag settings, with lists being replaced per JSON merge semantics.
      --full-secrets             If set, secret contents are included in output.
      --ignore-errs strings      List of comma separated glob patterns to match against log error strings. Any error matching these patterns is ignored when calculating the log importance heuristic.
      --include strings          Spec for which pod's proxy logs to include in the archive. See above for format and examples.
      --istio-namespace string   Namespace where Istio control plane is installed. (default "istio-system")
  -c, --kubeconfig string        Path to kube config.
      --start-time string        Start time for the range of log entries to include in the archive. Default is the infinite past. If set, --duration must be unset.
      --timeout duration         Maximum amount of time to spend fetching logs. When timeout is reached only the logs captured so far are saved to the archive. (default 30m0s)
      --vklog Level              number for the log level verbosity. Like -v flag. ex: --vklog=9
[root@node01 ~]# 
```



# dashboard

```
[root@node01 ~]#  istioctl dashboard --help
Access to Istio web UIs

Usage:
  istioctl dashboard [flags]
  istioctl dashboard [command]

Aliases:
  dashboard, dash, d

Available Commands:
  controlz    Open ControlZ web UI
  envoy       Open Envoy admin web UI
  grafana     Open Grafana web UI
  jaeger      Open Jaeger web UI
  kiali       Open Kiali web UI
  prometheus  Open Prometheus web UI
  skywalking  Open SkyWalking UI
  zipkin      Open Zipkin web UI

Flags:
      --address string   Address to listen on. Only accepts IP address or localhost as a value. When localhost is supplied, istioctl will try to bind on both 127.0.0.1 and ::1 and will fail if neither of these address are available to bind. (default "localhost")
      --browser          When --browser is supplied as false, istioctl dashboard will not open the browser. Default is true which means istioctl dashboard will always open a browser to view the dashboard. (default true)
  -h, --help             help for dashboard
  -p, --port int         Local port to listen to

Global Flags:
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
  -n, --namespace string    Config namespace
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl dashboard [command] --help" for more information about a command.
```

## controlz

```
[root@node01 ~]#  istioctl dashboard controlz --help
Open the ControlZ web UI for a pod in the Istio control plane

Usage:
  istioctl dashboard controlz [<type>/]<name>[.<namespace>] [flags]

Examples:
  # Open ControlZ web UI for the istiod-123-456.istio-system pod
  istioctl dashboard controlz istiod-123-456.istio-system

  # Open ControlZ web UI for the istiod-56dd66799-jfdvs pod in a custom namespace
  istioctl dashboard controlz istiod-123-456 -n custom-ns

  # Open ControlZ web UI for any Istiod pod
  istioctl dashboard controlz deployment/istiod.istio-system

  # with short syntax
  istioctl dash controlz pilot-123-456.istio-system
  istioctl d controlz pilot-123-456.istio-system


Flags:
      --ctrlz_port int    ControlZ port (default 9876)
  -h, --help              help for controlz
  -l, --selector string   Label selector

Global Flags:
      --address string      Address to listen on. Only accepts IP address or localhost as a value. When localhost is supplied, istioctl will try to bind on both 127.0.0.1 and ::1 and will fail if neither of these address are available to bind. (default "localhost")
      --browser             When --browser is supplied as false, istioctl dashboard will not open the browser. Default is true which means istioctl dashboard will always open a browser to view the dashboard. (default true)
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
  -n, --namespace string    Namespace where the addon is running, if not specified, istio-system would be used (default "istio-system")
  -p, --port int            Local port to listen to
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 istioctl]# istioctl dashboard controlz istiod-8495d444bb-vvnpn.istio-system  --address 0.0.0.0
http://0.0.0.0:9876
```

```
[root@node01 istioctl]# istioctl dashboard controlz istiod-8495d444bb-vvnpn.istio-system  --address 0.0.0.0
http://0.0.0.0:9876 --port 1234
```

## envoy

```
[root@node01 ~]#  istioctl dashboard envoy --help
Open the Envoy admin dashboard for a sidecar

Usage:
  istioctl dashboard envoy [<type>/]<name>[.<namespace>] [flags]

Examples:
  # Open Envoy dashboard for the productpage-123-456.default pod
  istioctl dashboard envoy productpage-123-456.default

  # Open Envoy dashboard for one pod under a deployment
  istioctl dashboard envoy deployment/productpage-v1

  # with short syntax
  istioctl dash envoy productpage-123-456.default
  istioctl d envoy productpage-123-456.default


Flags:
  -h, --help              help for envoy
  -l, --selector string   Label selector

Global Flags:
      --address string      Address to listen on. Only accepts IP address or localhost as a value. When localhost is supplied, istioctl will try to bind on both 127.0.0.1 and ::1 and will fail if neither of these address are available to bind. (default "localhost")
      --browser             When --browser is supplied as false, istioctl dashboard will not open the browser. Default is true which means istioctl dashboard will always open a browser to view the dashboard. (default true)
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
  -n, --namespace string    Namespace where the addon is running, if not specified, istio-system would be used (default "istio-system")
  -p, --port int            Local port to listen to
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl dashboard envoy productpage-v1-6b746f74dc-vlvmt.istio
http://localhost:15000
Failed to open browser; open http://localhost:15000 in your browser.
```

```
[root@master01 istioctl]# istioctl dashboard envoy productpage-v1-6b746f74dc-vlvmt.istio --address 0.0.0.0 --port=15005
http://0.0.0.0:15005
Failed to open browser; open http://0.0.0.0:15005 in your browser.
```

## grafana  

```
[root@node01 ~]#  istioctl dashboard grafana   --help
Open Istio's Grafana dashboard

Usage:
  istioctl dashboard grafana [flags]

Examples:
  istioctl dashboard grafana

  # with short syntax
  istioctl dash grafana
  istioctl d grafana

Flags:
  -h, --help   help for grafana

Global Flags:
      --address string      Address to listen on. Only accepts IP address or localhost as a value. When localhost is supplied, istioctl will try to bind on both 127.0.0.1 and ::1 and will fail if neither of these address are available to bind. (default "localhost")
      --browser             When --browser is supplied as false, istioctl dashboard will not open the browser. Default is true which means istioctl dashboard will always open a browser to view the dashboard. (default true)
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
  -p, --port int            Local port to listen to
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```



## jaeger 

```
[root@node01 ~]#  istioctl dashboard jaeger    --help
Open Istio's Jaeger dashboard

Usage:
  istioctl dashboard jaeger [flags]

Examples:
  istioctl dashboard jaeger

  # with short syntax
  istioctl dash jaeger
  istioctl d jaeger

Flags:
  -h, --help   help for jaeger

Global Flags:
      --address string      Address to listen on. Only accepts IP address or localhost as a value. When localhost is supplied, istioctl will try to bind on both 127.0.0.1 and ::1 and will fail if neither of these address are available to bind. (default "localhost")
      --browser             When --browser is supplied as false, istioctl dashboard will not open the browser. Default is true which means istioctl dashboard will always open a browser to view the dashboard. (default true)
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
  -p, --port int            Local port to listen to
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```



## kiali 

```
[root@node01 ~]#  istioctl dashboard kiali     --help
Open Istio's Kiali dashboard

Usage:
  istioctl dashboard kiali [flags]

Examples:
  istioctl dashboard kiali

  # with short syntax
  istioctl dash kiali
  istioctl d kiali

Flags:
  -h, --help   help for kiali

Global Flags:
      --address string      Address to listen on. Only accepts IP address or localhost as a value. When localhost is supplied, istioctl will try to bind on both 127.0.0.1 and ::1 and will fail if neither of these address are available to bind. (default "localhost")
      --browser             When --browser is supplied as false, istioctl dashboard will not open the browser. Default is true which means istioctl dashboard will always open a browser to view the dashboard. (default true)
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
  -p, --port int            Local port to listen to
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

## prometheus 

```
[root@node01 ~]#  istioctl dashboard prometheus      --help
Open Istio's Prometheus dashboard

Usage:
  istioctl dashboard prometheus [flags]

Examples:
  istioctl dashboard prometheus

  # with short syntax
  istioctl dash prometheus
  istioctl d prometheus

Flags:
  -h, --help   help for prometheus

Global Flags:
      --address string      Address to listen on. Only accepts IP address or localhost as a value. When localhost is supplied, istioctl will try to bind on both 127.0.0.1 and ::1 and will fail if neither of these address are available to bind. (default "localhost")
      --browser             When --browser is supplied as false, istioctl dashboard will not open the browser. Default is true which means istioctl dashboard will always open a browser to view the dashboard. (default true)
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
  -p, --port int            Local port to listen to
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

## skywalking

```
[root@node01 ~]#  istioctl dashboard skywalking      --help
Open the Istio dashboard in the SkyWalking UI

Usage:
  istioctl dashboard skywalking [flags]

Examples:
  istioctl dashboard skywalking

  # with short syntax
  istioctl dash skywalking
  istioctl d skywalking

Flags:
  -h, --help   help for skywalking

Global Flags:
      --address string      Address to listen on. Only accepts IP address or localhost as a value. When localhost is supplied, istioctl will try to bind on both 127.0.0.1 and ::1 and will fail if neither of these address are available to bind. (default "localhost")
      --browser             When --browser is supplied as false, istioctl dashboard will not open the browser. Default is true which means istioctl dashboard will always open a browser to view the dashboard. (default true)
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
  -p, --port int            Local port to listen to
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
helm repo add skywalking https://apache.jfrog.io/artifactory/skywalking-helm 

cd skywalking
  
helm install  skywalking -n istio-system \
  --set oap.env.SW_ENVOY_METRIC_ALS_HTTP_ANALYSIS=k8s-mesh \
  --set fullnameOverride=skywalking \
  --set oap.envoy.als.enabled=true \
  --set ui.image.tag=8.7.0 \
  --set oap.image.tag=8.7.0-es6 \
  --set oap.storageType=elasticsearch \
  --set ui.image.repository=apache/skywalking-ui \
  --set oap.image.repository=apache/skywalking-oap-server \
  .
```



## zipkin   

```
[root@node01 ~]#  istioctl dashboard zipkin        --help
Open Istio's Zipkin dashboard

Usage:
  istioctl dashboard zipkin [flags]

Examples:
  istioctl dashboard zipkin

  # with short syntax
  istioctl dash zipkin
  istioctl d zipkin

Flags:
  -h, --help   help for zipkin

Global Flags:
      --address string      Address to listen on. Only accepts IP address or localhost as a value. When localhost is supplied, istioctl will try to bind on both 127.0.0.1 and ::1 and will fail if neither of these address are available to bind. (default "localhost")
      --browser             When --browser is supplied as false, istioctl dashboard will not open the browser. Default is true which means istioctl dashboard will always open a browser to view the dashboard. (default true)
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
  -p, --port int            Local port to listen to
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```



# experimental

```
[root@node01 ~]# istioctl x --help
Experimental commands that may be modified or deprecated

Usage:
  istioctl experimental [command]

Aliases:
  experimental, x, exp

Available Commands:
  add-to-mesh          Add workloads into Istio service mesh
  authz                Inspect Istio AuthorizationPolicy
  config               Configure istioctl defaults
  create-remote-secret Create a secret with credentials to allow Istio to access remote Kubernetes apiservers
  describe             Describe resource and related Istio configuration
  envoy-stats          Retrieves Envoy metrics in the specified pod
  injector             List sidecar injector and sidecar versions
  internal-debug       Retrieves the debug information of istio
  kube-uninject        Uninject Envoy sidecar from Kubernetes pod resources
  metrics              Prints the metrics for the specified workload(s) when running in Kubernetes.
  precheck             check whether Istio can safely be installed or upgrade
  proxy-status         Retrieves the synchronization status of each Envoy in the mesh
  remote-clusters      Lists the remote clusters each istiod instance is connected to.
  remove-from-mesh     Remove workloads from Istio service mesh
  revision             Provide insight into various revisions (istiod, gateways) installed in the cluster
  uninstall            Uninstall Istio from a cluster
  version              Prints out build version information
  wait                 Wait for an Istio resource
  workload             Commands to assist in configuring and deploying workloads running on VMs and other non-Kubernetes environments

Flags:
  -h, --help   help for experimental

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl experimental [command] --help" for more information about a command.
```

## add-to-mesh

```
[root@node01 ~]# istioctl x add-to-mesh --help
'istioctl experimental add-to-mesh' restarts pods with an Istio sidecar or configures meshed pod access to external services.
Use 'add-to-mesh' as an alternate to namespace-wide auto injection for troubleshooting compatibility.

The 'remove-from-mesh' command can be used to restart with the sidecar removed.

THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl experimental add-to-mesh [flags]
  istioctl experimental add-to-mesh [command]

Aliases:
  add-to-mesh, add

Examples:
  # Restart all productpage pods with an Istio sidecar
  istioctl experimental add-to-mesh service productpage

  # Restart just pods from the productpage-v1 deployment
  istioctl experimental add-to-mesh deployment productpage-v1

  # Restart just pods from the details-v1 deployment
  istioctl x add deployment details-v1

  # Control how meshed pods see an external service
  istioctl experimental add-to-mesh external-service vmhttp 172.12.23.125,172.12.23.126 \
   http:9080 tcp:8888 --labels app=test,version=v1 --annotations env=stage --serviceaccount stageAdmin

Available Commands:
  deployment       Add deployment to Istio service mesh
  external-service Add external service (e.g. services running on a VM) to Istio service mesh
  service          Add Service to Istio service mesh

Flags:
  -h, --help                         help for add-to-mesh
      --injectConfigFile string      Injection configuration filename. Cannot be used with --injectConfigMapName
      --injectConfigMapName string   ConfigMap name for Istio sidecar injection, key should be "config". (default "istio-sidecar-injector")
      --meshConfigFile string        Mesh configuration filename. Takes precedence over --meshConfigMapName if set
      --meshConfigMapName string     ConfigMap name for Istio mesh configuration, key should be "mesh" (default "istio")
      --valuesFile string            Injection values configuration filename.

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl experimental add-to-mesh [command] --help" for more information about a command.
```

### deployment

```
[root@node01 ~]# istioctl x add-to-mesh deployment --help
'istioctl experimental add-to-mesh deployment' restarts pods with the Istio sidecar.  Use 'add-to-mesh'
to test deployments for compatibility with Istio.  It can be used instead of namespace-wide auto-injection of sidecars and is especially helpful for compatibility testing.

If your deployment does not function after using 'add-to-mesh' you must re-deploy it and troubleshoot it for Istio compatibility.
See https://istio.io/v1.14/docs/ops/deployment/requirements/

See also 'istioctl experimental remove-from-mesh deployment' which does the reverse.

THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl experimental add-to-mesh deployment <deployment> [flags]

Aliases:
  deployment, deploy, dep

Examples:
  # Restart pods from the productpage-v1 deployment with Istio sidecar
  istioctl experimental add-to-mesh deployment productpage-v1

  # Restart pods from the details-v1 deployment with Istio sidecar
  istioctl x add-to-mesh deploy details-v1

  # Restart pods from the ratings-v1 deployment with Istio sidecar
  istioctl x add dep ratings-v1

Flags:
  -h, --help              help for deployment
  -r, --revision string   Control plane revision

Global Flags:
      --context string               The name of the kubeconfig context to use
      --injectConfigFile string      Injection configuration filename. Cannot be used with --injectConfigMapName
      --injectConfigMapName string   ConfigMap name for Istio sidecar injection, key should be "config". (default "istio-sidecar-injector")
  -i, --istioNamespace string        Istio system namespace (default "istio-system")
  -c, --kubeconfig string            Kubernetes configuration file
      --meshConfigFile string        Mesh configuration filename. Takes precedence over --meshConfigMapName if set
      --meshConfigMapName string     ConfigMap name for Istio mesh configuration, key should be "mesh" (default "istio")
  -n, --namespace string             Config namespace
      --valuesFile string            Injection values configuration filename.
      --vklog Level                  number for the log level verbosity. Like -v flag. ex: --vklog=9
```



```
kubectl apply -f productpage-deploy.yaml

[root@master01 istioctl]# istioctl x add-to-mesh deploy productpage-v1
deployment productpage-v1.default updated successfully with Istio sidecar injected.
Next Step: Add related labels to the deployment to align with Istio's requirement: https://istio.io/v1.11/docs/ops/deployment/requirements/
[root@master01 istioctl]# kubectl get pod
NAME                              READY   STATUS        RESTARTS   AGE
productpage-v1-6b746f74dc-t5hjm   0/1     Terminating   0          73s
productpage-v1-758dbcc7f5-k5lhf   2/2     Running       0          12s


istioctl x add-to-mesh deploy productpage-v1 --injectConfigMapName istio-sidecar-injector  --meshConfigMapName istio

```



### external-service

```
[root@node01 ~]# istioctl x add-to-mesh external-service --help
istioctl experimental add-to-mesh external-service create a ServiceEntry and
a Service without selector for the specified external service in Istio service mesh.
The typical usage scenario is Mesh Expansion on VMs.

See also 'istioctl experimental remove-from-mesh external-service' which does the reverse.

THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl experimental add-to-mesh external-service <svcname> <ip> [name1:]port1 [[name2:]port2] ... [flags]

Aliases:
  external-service, es

Examples:
 # Control how meshed pods contact 172.12.23.125 and .126
  istioctl experimental add-to-mesh external-service vmhttp 172.12.23.125,172.12.23.126 \
   http:9080 tcp:8888 --labels app=test,version=v1 --annotations env=stage --serviceaccount stageAdmin

Flags:
  -a, --annotations strings     List of string annotations to apply if creating a service/endpoint; e.g. -a foo=bar,x=y
  -h, --help                    help for external-service
  -l, --labels strings          List of labels to apply if creating a service/endpoint; e.g. -l env=prod,vers=2
  -s, --serviceaccount string   Service account to link to the service (default "default")

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl x add-to-mesh external-service test 192.168.198.188 http:80 https:443 --labels app=test,version=v1 --annotations env=stage -n istio
ServiceEntry "mesh-expansion-test.istio" has been created in the Istio service mesh for the external service "test"
Kubernetes Service "test.istio" has been created in the Istio service mesh for the external service "test"
[root@master01 istioctl]# kubectl get svc -n istio
NAME             TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)             AGE
details          ClusterIP   10.68.203.136   <none>        9080/TCP            6d3h
my-nginx         ClusterIP   10.68.174.221   <none>        443/TCP             2d2h
my-nginx-v2      ClusterIP   10.68.71.43     <none>        443/TCP             2d
productpage      ClusterIP   10.68.191.45    <none>        9080/TCP            6d3h
productpage-v2   ClusterIP   10.68.181.29    <none>        9080/TCP            3d3h
ratings          ClusterIP   10.68.83.113    <none>        9080/TCP            6d3h
reviews          ClusterIP   10.68.59.45     <none>        9080/TCP            6d3h
tcp-echo         ClusterIP   10.68.175.57    <none>        9000/TCP,9001/TCP   6d2h
test             ClusterIP   10.68.95.131    <none>        80/TCP,443/TCP      31s
[root@master01 istioctl]# kubectl get se -n istio
NAME                  HOSTS                              LOCATION        RESOLUTION   AGE
mesh-expansion-test   ["test.istio.svc.cluster.local"]   MESH_INTERNAL   STATIC       51s
```



### service

```
[root@node01 ~]# istioctl x add-to-mesh service --help
istioctl experimental add-to-mesh service restarts pods with the Istio sidecar.  Use 'add-to-mesh'
to test deployments for compatibility with Istio.  It can be used instead of namespace-wide auto-injection of sidecars and is especially helpful for compatibility testing.

If your service does not function after using 'add-to-mesh' you must re-deploy it and troubleshoot it for Istio compatibility.
See https://istio.io/v1.14/docs/ops/deployment/requirements/

See also 'istioctl experimental remove-from-mesh service' which does the reverse.

THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl experimental add-to-mesh service <service> [flags]

Aliases:
  service, svc

Examples:
  # Restart all productpage pods with an Istio sidecar
  istioctl experimental add-to-mesh service productpage

  # Restart all details-v1 pods with an Istio sidecar
  istioctl x add-to-mesh svc details-v1

  # Restart all ratings-v1 pods with an Istio sidecar
  istioctl x add svc ratings-v1

Flags:
  -h, --help              help for service
  -r, --revision string   Control plane revision

Global Flags:
      --context string               The name of the kubeconfig context to use
      --injectConfigFile string      Injection configuration filename. Cannot be used with --injectConfigMapName
      --injectConfigMapName string   ConfigMap name for Istio sidecar injection, key should be "config". (default "istio-sidecar-injector")
  -i, --istioNamespace string        Istio system namespace (default "istio-system")
  -c, --kubeconfig string            Kubernetes configuration file
      --meshConfigFile string        Mesh configuration filename. Takes precedence over --meshConfigMapName if set
      --meshConfigMapName string     ConfigMap name for Istio mesh configuration, key should be "mesh" (default "istio")
  -n, --namespace string             Config namespace
      --valuesFile string            Injection values configuration filename.
      --vklog Level                  number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl x add-to-mesh svc productpage
deployment productpage-v1.default updated successfully with Istio sidecar injected.
Next Step: Add related labels to the deployment to align with Istio's requirement: https://istio.io/v1.11/docs/ops/deployment/requirements/
[root@master01 istioctl]# kubectl get pod 
NAME                              READY   STATUS        RESTARTS   AGE
productpage-v1-758dbcc7f5-k7rxz   2/2     Running       0          13s
productpage-v1-7ccd5885dc-rwwfz   0/1     Terminating   0          18s
```



## remove-from-mesh

```
[root@node01 ~]# istioctl x remove-from-mesh --help
'istioctl experimental remove-from-mesh' restarts pods without an Istio sidecar or removes external service access configuration.
Use 'remove-from-mesh' to quickly test uninjected behavior as part of compatibility troubleshooting.
The 'add-to-mesh' command can be used to add or restore the sidecar.

THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl experimental remove-from-mesh [flags]
  istioctl experimental remove-from-mesh [command]

Aliases:
  remove-from-mesh, rm

Examples:
  # Restart all productpage pods without an Istio sidecar
  istioctl experimental remove-from-mesh service productpage

  # Restart all details-v1 pods without an Istio sidecar
  istioctl x rm service details-v1

  # Restart all ratings-v1 pods without an Istio sidecar
  istioctl x rm deploy ratings-v1

Available Commands:
  deployment       Remove deployment from Istio service mesh
  external-service Remove Service Entry and Kubernetes Service for the external service from Istio service mesh
  service          Remove Service from Istio service mesh

Flags:
  -h, --help   help for remove-from-mesh

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl experimental remove-from-mesh [command] --help" for more information about a command.
```

### deployment

```
[root@node01 ~]# istioctl x remove-from-mesh deployment --help
'istioctl experimental remove-from-mesh deployment' restarts pods with the Istio sidecar un-injected.
'remove-from-mesh' is a compatibility troubleshooting tool.

THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl experimental remove-from-mesh deployment <deployment> [flags]

Aliases:
  deployment, deploy, dep

Examples:
  # Restart all productpage-v1 pods without an Istio sidecar
  istioctl experimental remove-from-mesh deployment productpage-v1

  # Restart all details-v1 pods without an Istio sidecar
  istioctl x remove-from-mesh deploy details-v1

  # Restart all ratings-v1 pods without an Istio sidecar
  istioctl x rm dep ratings-v1

Flags:
  -h, --help   help for deployment

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```



```
[root@master01 istioctl]# istioctl x remove-from-mesh deploy productpage-v1
deployment "productpage-v1.default" updated successfully with Istio sidecar un-injected.
[root@master01 istioctl]# kubectl get pod
NAME                              READY   STATUS        RESTARTS   AGE
productpage-v1-55bcc66c65-9qkzr   1/1     Running       0          6s
productpage-v1-758dbcc7f5-k7rxz   2/2     Terminating   0          11m
```



### external-service

```
[root@node01 ~]# istioctl x remove-from-mesh external-service --help
'istioctl experimental remove-from-mesh external-service' removes the ServiceEntry and
the Kubernetes Service for the specified external service (e.g. services running on a VM) from Istio service mesh.
The typical usage scenario is Mesh Expansion on VMs.

THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl experimental remove-from-mesh external-service <svcname> [flags]

Aliases:
  external-service, es

Examples:
  # Remove "vmhttp" service entry rules
  istioctl experimental remove-from-mesh external-service vmhttp

  # Remove "vmhttp" service entry rules
  istioctl x remove-from-mesh es vmhttp

  # Remove "vmhttp" service entry rules
  istioctl x rm es vmhttp

Flags:
  -h, --help   help for external-service

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl x remove-from-mesh es test
Error: service "test" does not exist, skip
[root@master01 istioctl]# istioctl x remove-from-mesh es test -n istio
Kubernetes Service "test.istio" has been deleted for external service "test"
Service Entry "mesh-expansion-test" has been deleted for external service "test"
[root@master01 istioctl]# kubectl get svc -n isto
No resources found in isto namespace.
[root@master01 istioctl]# kubectl get svc -n istio
NAME             TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)             AGE
details          ClusterIP   10.68.203.136   <none>        9080/TCP            6d3h
my-nginx         ClusterIP   10.68.174.221   <none>        443/TCP             2d2h
my-nginx-v2      ClusterIP   10.68.71.43     <none>        443/TCP             2d
productpage      ClusterIP   10.68.191.45    <none>        9080/TCP            6d3h
productpage-v2   ClusterIP   10.68.181.29    <none>        9080/TCP            3d3h
ratings          ClusterIP   10.68.83.113    <none>        9080/TCP            6d3h
reviews          ClusterIP   10.68.59.45     <none>        9080/TCP            6d3h
tcp-echo         ClusterIP   10.68.175.57    <none>        9000/TCP,9001/TCP   6d2h
[root@master01 istioctl]# kubectl get se -n istio
No resources found in istio namespace.
```



### service

```
[root@node01 ~]# istioctl x remove-from-mesh service --help
'istioctl experimental remove-from-mesh service' restarts pods with the Istio sidecar un-injected.
'remove-from-mesh' is a compatibility troubleshooting tool.

THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl experimental remove-from-mesh service <service> [flags]

Aliases:
  service, svc

Examples:
  # Restart all productpage pods without an Istio sidecar
  istioctl experimental remove-from-mesh service productpage

  # Restart all details-v1 pods without an Istio sidecar
  istioctl x remove-from-mesh svc details-v1

  # Restart all ratings-v1 pods without an Istio sidecar
  istioctl x rm svc ratings-v1

Flags:
  -h, --help   help for service

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# kubectl get pod
NAME                              READY   STATUS            RESTARTS   AGE
productpage-v1-758dbcc7f5-zqdv6   0/2     PodInitializing   0          4s
productpage-v1-7ccd5885dc-67w2g   1/1     Running           0          26s
[root@master01 istioctl]# kubectl get pod
NAME                              READY   STATUS    RESTARTS   AGE
productpage-v1-758dbcc7f5-zqdv6   2/2     Running   0          11s
[root@master01 istioctl]# istioctl x remove-from-mesh svc productpage
deployment "productpage-v1.default" updated successfully with Istio sidecar un-injected.
[root@master01 istioctl]# kubectl get pod
NAME                              READY   STATUS        RESTARTS   AGE
productpage-v1-55bcc66c65-xqdhp   1/1     Running       0          6s
productpage-v1-758dbcc7f5-zqdv6   2/2     Terminating   0          37s
```

## config

```
[root@node01 ~]# istioctl x config --help
Configure istioctl defaults

Usage:
  istioctl experimental config [command]

Examples:
  # list configuration parameters
  istioctl config list

Available Commands:
  list        List istio configurable defaults

Flags:
  -h, --help   help for config

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl experimental config [command] --help" for more information about a command.
```

### list

```
[root@node01 ~]# istioctl x config list --help
List istio configurable defaults

Usage:
  istioctl experimental config list [flags]

Flags:
  -h, --help   help for list

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl x config list  
FLAG                    VALUE            FROM
authority                                default
cert-dir                                 default
insecure                                 default
istioNamespace          istio-system     default
plaintext                                default
prefer-experimental                      default
xds-address                              default
xds-port                15012            default
```



## create-remote-secret

```
[root@node01 ~]# istioctl x create-remote-secret --help
Create a secret with credentials to allow Istio to access remote Kubernetes apiservers

Usage:
  istioctl experimental create-remote-secret [flags]

Examples:
  # Create a secret to access cluster c0's apiserver and install it in cluster c1.
  istioctl --kubeconfig=c0.yaml x create-remote-secret --name c0 \
    | kubectl --kubeconfig=c1.yaml apply -f -

  # Delete a secret that was previously installed in c1
  istioctl --kubeconfig=c0.yaml x create-remote-secret --name c0 \
    | kubectl --kubeconfig=c1.yaml delete -f -

  # Create a secret access a remote cluster with an auth plugin
  istioctl --kubeconfig=c0.yaml x create-remote-secret --name c0 --auth-type=plugin --auth-plugin-name=gcp \
    | kubectl --kubeconfig=c1.yaml apply -f -

Flags:
      --auth-plugin-config stringToString   Authenticator plug-in configuration. --auth-type=plugin must be set with this option (default [])
      --auth-plugin-name string             Authenticator plug-in name. --auth-type=plugin must be set with this option
      --auth-type RemoteSecretAuthType      Type of authentication to use. supported values = [bearer-token plugin] (default bearer-token)
      --create-service-account              If true, the service account needed for creating the remote secret will be created if it doesn't exist. (default true)
  -h, --help                                help for create-remote-secret
  -d, --manifests string                    Specify a path to a directory of charts and profiles
                                            (e.g. ~/Downloads/istio-1.14.1/manifests)
                                            or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                            
      --name string                         Name of the local cluster whose credentials are stored in the secret. If a name is not specified the kube-system namespace's UUID of the local cluster will be used.
      --secret-name string                  The name of the specific secret to use from the service-account. Needed when there are multiple secrets in the service account.
      --server string                       The address and port of the Kubernetes API server.
      --service-account string              Create a secret with this service account's credentials. Default value is "istio-reader-service-account" if --type is "remote", "istiod" if --type is "config".
      --type SecretType                     Type of the generated secret. supported values = [remote config] (default remote)

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl  x create-remote-secret --name c0
# This file is autogenerated, do not edit.
apiVersion: v1
kind: Secret
metadata:
  annotations:
    networking.istio.io/cluster: c0
  creationTimestamp: null
  labels:
    istio/multiCluster: "true"
  name: istio-remote-secret-c0
  namespace: istio-system
stringData:
  c0: |
    apiVersion: v1
    clusters:
    - cluster:
        certificate-authority-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUR1RENDQXFDZ0F3SUJBZ0lVQ1NZRVlnZW92QWlvbEhqUzl1V1lKRmdKSW1nd0RRWUpLb1pJaHZjTkFRRUwKQlFBd1lURUxNQWtHQTFVRUJoTUNRMDR4RVRBUEJnTlZCQWdUQ0VoaGJtZGFhRzkxTVFzd0NRWURWUVFIRXdKWQpVekVNTUFvR0ExVUVDaE1EYXpoek1ROHdEUVlEVlFRTEV3WlRlWE4wWlcweEV6QVJCZ05WQkFNVENtdDFZbVZ5CmJtVjBaWE13SUJjTk1qRXdOREF4TURrMU1qQXdXaGdQTWpFeU1UQXpNRGd3T1RVeU1EQmFNR0V4Q3pBSkJnTlYKQkFZVEFrTk9NUkV3RHdZRFZRUUlFd2hJWVc1bldtaHZkVEVMTUFrR0ExVUVCeE1DV0ZNeEREQUtCZ05WQkFvVApBMnM0Y3pFUE1BMEdBMVVFQ3hNR1UzbHpkR1Z0TVJNd0VRWURWUVFERXdwcmRXSmxjbTVsZEdWek1JSUJJakFOCkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXJqVmFmSDR6V0pDaksyRjY5R3VNTytVRmNVS3cKZWQ3enlSaHBhZlR6ZzkzTkZQNUFaSHkwMlJvdWUxWWRtVXpxVjJZczRiaVVLUlRLbFc2NWJQRGxBeklSalFiNApiYS9aYTgxUllpVWZ5ZndaSGpTMjdmQ0xsVGp3WlFIaHJCZS9xYUE4dzdHb0Q0Mm8rT1dXYk9JeVB3c3p4TmJ3CjVxOVFObHJVY3dLV3ZTZnozTGV1ZEVBQmNZWEd4NmppbndOZU5aYkxvM0NCMi9STEw1M0prdk5hTlU5bUltOGUKUlZnTkNOZ3BPWHREeWJJdXQyTTh6SWFERGU0d3dWVzVLLzlIZHMvOTkrUlNrTnJZcnBRYkpVekk3UnZsaGl5Rwppckc4bW5pRTBXQnVSZCtseXJFQ1hZaDBOeFlJSnFjcFViUnBjRXRUaXk3MWdsWitKVVlsVWErb1FRSURBUUFCCm8yWXdaREFPQmdOVkhROEJBZjhFQkFNQ0FRWXdFZ1lEVlIwVEFRSC9CQWd3QmdFQi93SUJBakFkQmdOVkhRNEUKRmdRVWJUS0c1UmR3UE9VMDJFK2sxcW1FSWtJSHZNY3dId1lEVlIwakJCZ3dGb0FVYlRLRzVSZHdQT1UwMkUrawoxcW1FSWtJSHZNY3dEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBSmZMUEo4SVhoV1hzSkR2UnlsQiszQTA3eWVZCnRvZDVGeVpieElPWFA4OUFCb3VvellwclNRYXFSTFIxY0lZeEVDTkx4ZEhQcjQ5VjZZOW1BM016NzAyUjk5bmIKQTY4eXVqMHNtMml5Qmlud0JaSUh3SlY2RllZTWRsdk8zRUE1aXpRSWduSkxCNW1vc0JPYnhMZmV0YUlHTnVEaApGR1ZGZzNhRDk1Y094KzlyenV6amthMFh3T29WRUVNS0I4WHRpMHhLME4xK1RCano2V3MzL0psN2orME5JazVOCjZmUnNLN0Q1NlB4d3VxR1Y2dk41WFJ0N3NhZHNod2tpTnFoL0haekRhTjlxcWRRQnRXVE1LRERsc1VWTTg1WmEKNDVwaElpNHV4dVhuYUhyMkJCeXdjaGEwalYramMyNUFhRFU2bm1HQ0dZZ1NkNWhaYlVJQ2hHR2l5Wnc9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K
        server: https://192.168.198.154:6443
      name: c0
    contexts:
    - context:
        cluster: c0
        user: c0
      name: c0
    current-context: c0
    kind: Config
    preferences: {}
    users:
    - name: c0
      user:
        token: eyJhbGciOiJSUzI1NiIsImtpZCI6IjV2Ny1kcmNpMXpITzhKRU5aaV9KOGx5OGotLTFPc1hpUDVLQlhhUFJESEkifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJpc3Rpby1zeXN0ZW0iLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoiaXN0aW8tcmVhZGVyLXNlcnZpY2UtYWNjb3VudC10b2tlbi0yYmpwcyIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJpc3Rpby1yZWFkZXItc2VydmljZS1hY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiMmFjZDllYzEtMjZkNi00MmEzLWI2NmMtZWQzZTUwODIwZmI1Iiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OmlzdGlvLXN5c3RlbTppc3Rpby1yZWFkZXItc2VydmljZS1hY2NvdW50In0.Qp86hiCVcl0VZBxCfXlTmx4JvzfUXE6wueLFwKOsIVaSvj5Npy5ew07FYI9RWGVr2XnSpNBcd9ck4JiAJjXHq571kg7lJaDMPaUpKS4n-Sj6w843o1zC6sX0vsZohrW4ddq6a360PNABwZGkVGgOFEoFD6tJKC4FgXP5e1C2c0iIpXSG94oAnNaxpDD6SrLZhW8k6rTRhmXJpqQZ666GOpP65Fh983ZF7SykKaWqyoDv7O-PpfvPJxxjlvZWqx4AYJlITnE_6DL5ppQUMnInRzOJgr3RlziccbN7ov3DiQH0nf9qOMYbbEosemOFoVhzurRawJgNgbdDe-0pB5EcwQ
---
```

```
[root@node01 ~]#  istioctl  x create-remote-secret --name c0 --server https://192.168.229.128:6443
# This file is autogenerated, do not edit.
apiVersion: v1
kind: Secret
metadata:
  annotations:
    networking.istio.io/cluster: c0
  creationTimestamp: null
  labels:
    istio/multiCluster: "true"
  name: istio-remote-secret-c0
  namespace: istio-system
stringData:
  c0: |
    apiVersion: v1
    clusters:
    - cluster:
        certificate-authority-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUR1RENDQXFDZ0F3SUJBZ0lVQno0SjJKZ2JRczkzSUZJUS9yOXBXejFMSHZNd0RRWUpLb1pJaHZjTkFRRUwKQlFBd1lURUxNQWtHQTFVRUJoTUNRMDR4RVRBUEJnTlZCQWdUQ0VoaGJtZGFhRzkxTVFzd0NRWURWUVFIRXdKWQpVekVNTUFvR0ExVUVDaE1EYXpoek1ROHdEUVlEVlFRTEV3WlRlWE4wWlcweEV6QVJCZ05WQkFNVENtdDFZbVZ5CmJtVjBaWE13SUJjTk1qRXdPVEExTURjME1qQXdXaGdQTWpFeU1UQTRNVEl3TnpReU1EQmFNR0V4Q3pBSkJnTlYKQkFZVEFrTk9NUkV3RHdZRFZRUUlFd2hJWVc1bldtaHZkVEVMTUFrR0ExVUVCeE1DV0ZNeEREQUtCZ05WQkFvVApBMnM0Y3pFUE1BMEdBMVVFQ3hNR1UzbHpkR1Z0TVJNd0VRWURWUVFERXdwcmRXSmxjbTVsZEdWek1JSUJJakFOCkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXZWZWJGRWFiQjlXSHErbUNwc0twYTR1QUlBd1QKb2cyL21JYmExS2lpVzNwM0Q0eTQxdFdUY0RWS29DSjRDNmtJaDZiTG5BL1dTemhjMGd1Q1Y1ZGFxR0FxZUdzYgpLdDU2aWhmanAxcUNZaGV4cGxGK2hveVkwMWRrR0tUNkF1VE5HTTREVHlrWEpvY3E1NC85RGdtZkcvUGhZYWc4ClFNS0JwQXFnMnhXb2NscVh5LzErelJ6SEZwdklNU2txZEJKYjl3dU45Uk5HNlpVR1NkWXEyVDhOOStRL2NFakIKeU1CU084SHk4R1ByS29ROUFXczhqRVVUczQvb2tBbi9mM0Y0bDR4N0VadzBhTm9vTVY4YTFJL1pFSklBVzZmRwovdzc3aHZZWXp4ZjdEL2RSTnBmcVQyUThJUzJCYVh1TlFjOUZiRjdjS2VjdE9lSTROWXZhQ00zT0Z3SURBUUFCCm8yWXdaREFPQmdOVkhROEJBZjhFQkFNQ0FRWXdFZ1lEVlIwVEFRSC9CQWd3QmdFQi93SUJBakFkQmdOVkhRNEUKRmdRVUNsN21IbitQRTB4VXBDeFlZZlZaVFJoSEJaMHdId1lEVlIwakJCZ3dGb0FVQ2w3bUhuK1BFMHhVcEN4WQpZZlZaVFJoSEJaMHdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBQUVMVjhLQlZjSlN3dk5ZUEpJdnhYbWJ4UmtmCncvQkdFbmExRGluQldpL1hvYnRXZzVBQkh2ajBKSTVzaEUyZXk1U1NvU1hlemwvSVRNQ2ZRMU9vc1FmaXY1cUMKQzBYUkFVQ25qeWRRdmV0RWI5SUEvcU9xNDZpek15RWtaR1IvaHBBKzBqNDhsSWFiZzVxc2NoUG1YR25kVk5zVgpPUWRQWVRKVVJxeVBjRDBWZ0M3cnc4aWdUZEtWUWo4RzR5aituMUNDVFcrL3RyYjc4NkZKeERWTHlvN0twYUdvCjNlVnBYejdIRE0vSmVVbEhOUk1nbUpTV3pCankxVk1WWEIxV2hjN2ZzcnMwTXV2dkRmM0pIMTNXY0NuRXFYK2sKNEZxbkdmdGI4Z0hOVkx6S0RqRGgvcmZIeS9zV1pDRFhScSt6NGFrUm1YSFdSTzBvRXFkODBRSzVLYzg9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K
        server: https://192.168.229.128:6443
      name: c0
    contexts:
    - context:
        cluster: c0
        user: c0
      name: c0
    current-context: c0
    kind: Config
    preferences: {}
    users:
    - name: c0
      user:
        token: eyJhbGciOiJSUzI1NiIsImtpZCI6IkhXZktmUVpaUGdNNTliSWZDVVVjbHlEeDJuYlJPYmRqUzJFb2VOb09XTUEifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJpc3Rpby1zeXN0ZW0iLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoiaXN0aW8tcmVhZGVyLXNlcnZpY2UtYWNjb3VudC10b2tlbi01NHF3eCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJpc3Rpby1yZWFkZXItc2VydmljZS1hY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiZWM1NDU4NTYtNzE3OS00NjgzLTg4ZTYtNzBjY2U1NmI1MzZkIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OmlzdGlvLXN5c3RlbTppc3Rpby1yZWFkZXItc2VydmljZS1hY2NvdW50In0.Oel0Qukrvqnf3VJQz7XLcUBaQ6oGQzctFr3OglxWDo7CdxncHz2rFnOlGBtSkbdjJiotc2-Hy5nl8gh1jwP_er9H8T6_f1AIU_SuTv38J_RBIwfD6DOKUHz4epieSDrZ9huZsd1lPGjUyJCSoF3ILYicK9reiGHMkUaF1jpvBtZ-t4CLTE_ti_4wtegEEOYvyXm1AKGYR4Ox4tTt46M0fmLgQx3t2ho-GxFTk4YUpNhGUtrdvjb6W_vekjndjbzFr8_9tsrRFVELPmPGSWWSKn7C2PrUq1tn50_CqgqiydH2qYjDPdwnD14r-qwA0fqyGHODPA_zUT4LqUGF6lhMoA
---
```

```
[root@node01 ~]# istioctl  x create-remote-secret --name c0 --context context-cluster1
# This file is autogenerated, do not edit.
apiVersion: v1
kind: Secret
metadata:
  annotations:
    networking.istio.io/cluster: c0
  creationTimestamp: null
  labels:
    istio/multiCluster: "true"
  name: istio-remote-secret-c0
  namespace: istio-system
stringData:
  c0: |
    apiVersion: v1
    clusters:
    - cluster:
        certificate-authority-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUR1RENDQXFDZ0F3SUJBZ0lVQno0SjJKZ2JRczkzSUZJUS9yOXBXejFMSHZNd0RRWUpLb1pJaHZjTkFRRUwKQlFBd1lURUxNQWtHQTFVRUJoTUNRMDR4RVRBUEJnTlZCQWdUQ0VoaGJtZGFhRzkxTVFzd0NRWURWUVFIRXdKWQpVekVNTUFvR0ExVUVDaE1EYXpoek1ROHdEUVlEVlFRTEV3WlRlWE4wWlcweEV6QVJCZ05WQkFNVENtdDFZbVZ5CmJtVjBaWE13SUJjTk1qRXdPVEExTURjME1qQXdXaGdQTWpFeU1UQTRNVEl3TnpReU1EQmFNR0V4Q3pBSkJnTlYKQkFZVEFrTk9NUkV3RHdZRFZRUUlFd2hJWVc1bldtaHZkVEVMTUFrR0ExVUVCeE1DV0ZNeEREQUtCZ05WQkFvVApBMnM0Y3pFUE1BMEdBMVVFQ3hNR1UzbHpkR1Z0TVJNd0VRWURWUVFERXdwcmRXSmxjbTVsZEdWek1JSUJJakFOCkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXZWZWJGRWFiQjlXSHErbUNwc0twYTR1QUlBd1QKb2cyL21JYmExS2lpVzNwM0Q0eTQxdFdUY0RWS29DSjRDNmtJaDZiTG5BL1dTemhjMGd1Q1Y1ZGFxR0FxZUdzYgpLdDU2aWhmanAxcUNZaGV4cGxGK2hveVkwMWRrR0tUNkF1VE5HTTREVHlrWEpvY3E1NC85RGdtZkcvUGhZYWc4ClFNS0JwQXFnMnhXb2NscVh5LzErelJ6SEZwdklNU2txZEJKYjl3dU45Uk5HNlpVR1NkWXEyVDhOOStRL2NFakIKeU1CU084SHk4R1ByS29ROUFXczhqRVVUczQvb2tBbi9mM0Y0bDR4N0VadzBhTm9vTVY4YTFJL1pFSklBVzZmRwovdzc3aHZZWXp4ZjdEL2RSTnBmcVQyUThJUzJCYVh1TlFjOUZiRjdjS2VjdE9lSTROWXZhQ00zT0Z3SURBUUFCCm8yWXdaREFPQmdOVkhROEJBZjhFQkFNQ0FRWXdFZ1lEVlIwVEFRSC9CQWd3QmdFQi93SUJBakFkQmdOVkhRNEUKRmdRVUNsN21IbitQRTB4VXBDeFlZZlZaVFJoSEJaMHdId1lEVlIwakJCZ3dGb0FVQ2w3bUhuK1BFMHhVcEN4WQpZZlZaVFJoSEJaMHdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBQUVMVjhLQlZjSlN3dk5ZUEpJdnhYbWJ4UmtmCncvQkdFbmExRGluQldpL1hvYnRXZzVBQkh2ajBKSTVzaEUyZXk1U1NvU1hlemwvSVRNQ2ZRMU9vc1FmaXY1cUMKQzBYUkFVQ25qeWRRdmV0RWI5SUEvcU9xNDZpek15RWtaR1IvaHBBKzBqNDhsSWFiZzVxc2NoUG1YR25kVk5zVgpPUWRQWVRKVVJxeVBjRDBWZ0M3cnc4aWdUZEtWUWo4RzR5aituMUNDVFcrL3RyYjc4NkZKeERWTHlvN0twYUdvCjNlVnBYejdIRE0vSmVVbEhOUk1nbUpTV3pCankxVk1WWEIxV2hjN2ZzcnMwTXV2dkRmM0pIMTNXY0NuRXFYK2sKNEZxbkdmdGI4Z0hOVkx6S0RqRGgvcmZIeS9zV1pDRFhScSt6NGFrUm1YSFdSTzBvRXFkODBRSzVLYzg9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K
        server: https://192.168.229.128:6443
      name: c0
    contexts:
    - context:
        cluster: c0
        user: c0
      name: c0
    current-context: c0
    kind: Config
    preferences: {}
    users:
    - name: c0
      user:
        token: eyJhbGciOiJSUzI1NiIsImtpZCI6IkhXZktmUVpaUGdNNTliSWZDVVVjbHlEeDJuYlJPYmRqUzJFb2VOb09XTUEifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJpc3Rpby1zeXN0ZW0iLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoiaXN0aW8tcmVhZGVyLXNlcnZpY2UtYWNjb3VudC10b2tlbi01NHF3eCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJpc3Rpby1yZWFkZXItc2VydmljZS1hY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiZWM1NDU4NTYtNzE3OS00NjgzLTg4ZTYtNzBjY2U1NmI1MzZkIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OmlzdGlvLXN5c3RlbTppc3Rpby1yZWFkZXItc2VydmljZS1hY2NvdW50In0.Oel0Qukrvqnf3VJQz7XLcUBaQ6oGQzctFr3OglxWDo7CdxncHz2rFnOlGBtSkbdjJiotc2-Hy5nl8gh1jwP_er9H8T6_f1AIU_SuTv38J_RBIwfD6DOKUHz4epieSDrZ9huZsd1lPGjUyJCSoF3ILYicK9reiGHMkUaF1jpvBtZ-t4CLTE_ti_4wtegEEOYvyXm1AKGYR4Ox4tTt46M0fmLgQx3t2ho-GxFTk4YUpNhGUtrdvjb6W_vekjndjbzFr8_9tsrRFVELPmPGSWWSKn7C2PrUq1tn50_CqgqiydH2qYjDPdwnD14r-qwA0fqyGHODPA_zUT4LqUGF6lhMoA
---
```

```
[root@node01 ~]# istioctl  x create-remote-secret --name c0 --context context-cluster1 --manifests  /root/istio-1.14.1/manifests/
# This file is autogenerated, do not edit.
apiVersion: v1
kind: Secret
metadata:
  annotations:
    networking.istio.io/cluster: c0
  creationTimestamp: null
  labels:
    istio/multiCluster: "true"
  name: istio-remote-secret-c0
  namespace: istio-system
stringData:
  c0: |
    apiVersion: v1
    clusters:
    - cluster:
        certificate-authority-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUR1RENDQXFDZ0F3SUJBZ0lVQno0SjJKZ2JRczkzSUZJUS9yOXBXejFMSHZNd0RRWUpLb1pJaHZjTkFRRUwKQlFBd1lURUxNQWtHQTFVRUJoTUNRMDR4RVRBUEJnTlZCQWdUQ0VoaGJtZGFhRzkxTVFzd0NRWURWUVFIRXdKWQpVekVNTUFvR0ExVUVDaE1EYXpoek1ROHdEUVlEVlFRTEV3WlRlWE4wWlcweEV6QVJCZ05WQkFNVENtdDFZbVZ5CmJtVjBaWE13SUJjTk1qRXdPVEExTURjME1qQXdXaGdQTWpFeU1UQTRNVEl3TnpReU1EQmFNR0V4Q3pBSkJnTlYKQkFZVEFrTk9NUkV3RHdZRFZRUUlFd2hJWVc1bldtaHZkVEVMTUFrR0ExVUVCeE1DV0ZNeEREQUtCZ05WQkFvVApBMnM0Y3pFUE1BMEdBMVVFQ3hNR1UzbHpkR1Z0TVJNd0VRWURWUVFERXdwcmRXSmxjbTVsZEdWek1JSUJJakFOCkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXZWZWJGRWFiQjlXSHErbUNwc0twYTR1QUlBd1QKb2cyL21JYmExS2lpVzNwM0Q0eTQxdFdUY0RWS29DSjRDNmtJaDZiTG5BL1dTemhjMGd1Q1Y1ZGFxR0FxZUdzYgpLdDU2aWhmanAxcUNZaGV4cGxGK2hveVkwMWRrR0tUNkF1VE5HTTREVHlrWEpvY3E1NC85RGdtZkcvUGhZYWc4ClFNS0JwQXFnMnhXb2NscVh5LzErelJ6SEZwdklNU2txZEJKYjl3dU45Uk5HNlpVR1NkWXEyVDhOOStRL2NFakIKeU1CU084SHk4R1ByS29ROUFXczhqRVVUczQvb2tBbi9mM0Y0bDR4N0VadzBhTm9vTVY4YTFJL1pFSklBVzZmRwovdzc3aHZZWXp4ZjdEL2RSTnBmcVQyUThJUzJCYVh1TlFjOUZiRjdjS2VjdE9lSTROWXZhQ00zT0Z3SURBUUFCCm8yWXdaREFPQmdOVkhROEJBZjhFQkFNQ0FRWXdFZ1lEVlIwVEFRSC9CQWd3QmdFQi93SUJBakFkQmdOVkhRNEUKRmdRVUNsN21IbitQRTB4VXBDeFlZZlZaVFJoSEJaMHdId1lEVlIwakJCZ3dGb0FVQ2w3bUhuK1BFMHhVcEN4WQpZZlZaVFJoSEJaMHdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBQUVMVjhLQlZjSlN3dk5ZUEpJdnhYbWJ4UmtmCncvQkdFbmExRGluQldpL1hvYnRXZzVBQkh2ajBKSTVzaEUyZXk1U1NvU1hlemwvSVRNQ2ZRMU9vc1FmaXY1cUMKQzBYUkFVQ25qeWRRdmV0RWI5SUEvcU9xNDZpek15RWtaR1IvaHBBKzBqNDhsSWFiZzVxc2NoUG1YR25kVk5zVgpPUWRQWVRKVVJxeVBjRDBWZ0M3cnc4aWdUZEtWUWo4RzR5aituMUNDVFcrL3RyYjc4NkZKeERWTHlvN0twYUdvCjNlVnBYejdIRE0vSmVVbEhOUk1nbUpTV3pCankxVk1WWEIxV2hjN2ZzcnMwTXV2dkRmM0pIMTNXY0NuRXFYK2sKNEZxbkdmdGI4Z0hOVkx6S0RqRGgvcmZIeS9zV1pDRFhScSt6NGFrUm1YSFdSTzBvRXFkODBRSzVLYzg9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K
        server: https://192.168.229.128:6443
      name: c0
    contexts:
    - context:
        cluster: c0
        user: c0
      name: c0
    current-context: c0
    kind: Config
    preferences: {}
    users:
    - name: c0
      user:
        token: eyJhbGciOiJSUzI1NiIsImtpZCI6IkhXZktmUVpaUGdNNTliSWZDVVVjbHlEeDJuYlJPYmRqUzJFb2VOb09XTUEifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJpc3Rpby1zeXN0ZW0iLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoiaXN0aW8tcmVhZGVyLXNlcnZpY2UtYWNjb3VudC10b2tlbi01NHF3eCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJpc3Rpby1yZWFkZXItc2VydmljZS1hY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiZWM1NDU4NTYtNzE3OS00NjgzLTg4ZTYtNzBjY2U1NmI1MzZkIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OmlzdGlvLXN5c3RlbTppc3Rpby1yZWFkZXItc2VydmljZS1hY2NvdW50In0.Oel0Qukrvqnf3VJQz7XLcUBaQ6oGQzctFr3OglxWDo7CdxncHz2rFnOlGBtSkbdjJiotc2-Hy5nl8gh1jwP_er9H8T6_f1AIU_SuTv38J_RBIwfD6DOKUHz4epieSDrZ9huZsd1lPGjUyJCSoF3ILYicK9reiGHMkUaF1jpvBtZ-t4CLTE_ti_4wtegEEOYvyXm1AKGYR4Ox4tTt46M0fmLgQx3t2ho-GxFTk4YUpNhGUtrdvjb6W_vekjndjbzFr8_9tsrRFVELPmPGSWWSKn7C2PrUq1tn50_CqgqiydH2qYjDPdwnD14r-qwA0fqyGHODPA_zUT4LqUGF6lhMoA
---
```

```
[root@node01 ~]# istioctl --kubeconfig=c0.yaml x create-remote-secret --name c0 --auth-type=plugin --auth-plugin-name=gcp
# This file is autogenerated, do not edit.
apiVersion: v1
kind: Secret
metadata:
  annotations:
    networking.istio.io/cluster: c0
  creationTimestamp: null
  labels:
    istio/multiCluster: "true"
  name: istio-remote-secret-c0
  namespace: istio-system
stringData:
  c0: |
    apiVersion: v1
    clusters:
    - cluster:
        certificate-authority-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUR1RENDQXFDZ0F3SUJBZ0lVQno0SjJKZ2JRczkzSUZJUS9yOXBXejFMSHZNd0RRWUpLb1pJaHZjTkFRRUwKQlFBd1lURUxNQWtHQTFVRUJoTUNRMDR4RVRBUEJnTlZCQWdUQ0VoaGJtZGFhRzkxTVFzd0NRWURWUVFIRXdKWQpVekVNTUFvR0ExVUVDaE1EYXpoek1ROHdEUVlEVlFRTEV3WlRlWE4wWlcweEV6QVJCZ05WQkFNVENtdDFZbVZ5CmJtVjBaWE13SUJjTk1qRXdPVEExTURjME1qQXdXaGdQTWpFeU1UQTRNVEl3TnpReU1EQmFNR0V4Q3pBSkJnTlYKQkFZVEFrTk9NUkV3RHdZRFZRUUlFd2hJWVc1bldtaHZkVEVMTUFrR0ExVUVCeE1DV0ZNeEREQUtCZ05WQkFvVApBMnM0Y3pFUE1BMEdBMVVFQ3hNR1UzbHpkR1Z0TVJNd0VRWURWUVFERXdwcmRXSmxjbTVsZEdWek1JSUJJakFOCkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXZWZWJGRWFiQjlXSHErbUNwc0twYTR1QUlBd1QKb2cyL21JYmExS2lpVzNwM0Q0eTQxdFdUY0RWS29DSjRDNmtJaDZiTG5BL1dTemhjMGd1Q1Y1ZGFxR0FxZUdzYgpLdDU2aWhmanAxcUNZaGV4cGxGK2hveVkwMWRrR0tUNkF1VE5HTTREVHlrWEpvY3E1NC85RGdtZkcvUGhZYWc4ClFNS0JwQXFnMnhXb2NscVh5LzErelJ6SEZwdklNU2txZEJKYjl3dU45Uk5HNlpVR1NkWXEyVDhOOStRL2NFakIKeU1CU084SHk4R1ByS29ROUFXczhqRVVUczQvb2tBbi9mM0Y0bDR4N0VadzBhTm9vTVY4YTFJL1pFSklBVzZmRwovdzc3aHZZWXp4ZjdEL2RSTnBmcVQyUThJUzJCYVh1TlFjOUZiRjdjS2VjdE9lSTROWXZhQ00zT0Z3SURBUUFCCm8yWXdaREFPQmdOVkhROEJBZjhFQkFNQ0FRWXdFZ1lEVlIwVEFRSC9CQWd3QmdFQi93SUJBakFkQmdOVkhRNEUKRmdRVUNsN21IbitQRTB4VXBDeFlZZlZaVFJoSEJaMHdId1lEVlIwakJCZ3dGb0FVQ2w3bUhuK1BFMHhVcEN4WQpZZlZaVFJoSEJaMHdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBQUVMVjhLQlZjSlN3dk5ZUEpJdnhYbWJ4UmtmCncvQkdFbmExRGluQldpL1hvYnRXZzVBQkh2ajBKSTVzaEUyZXk1U1NvU1hlemwvSVRNQ2ZRMU9vc1FmaXY1cUMKQzBYUkFVQ25qeWRRdmV0RWI5SUEvcU9xNDZpek15RWtaR1IvaHBBKzBqNDhsSWFiZzVxc2NoUG1YR25kVk5zVgpPUWRQWVRKVVJxeVBjRDBWZ0M3cnc4aWdUZEtWUWo4RzR5aituMUNDVFcrL3RyYjc4NkZKeERWTHlvN0twYUdvCjNlVnBYejdIRE0vSmVVbEhOUk1nbUpTV3pCankxVk1WWEIxV2hjN2ZzcnMwTXV2dkRmM0pIMTNXY0NuRXFYK2sKNEZxbkdmdGI4Z0hOVkx6S0RqRGgvcmZIeS9zV1pDRFhScSt6NGFrUm1YSFdSTzBvRXFkODBRSzVLYzg9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K
        server: https://192.168.229.128:6443
      name: c0
    contexts:
    - context:
        cluster: c0
        user: c0
      name: c0
    current-context: c0
    kind: Config
    preferences: {}
    users:
    - name: c0
      user:
        auth-provider:
          config: {}
          name: gcp
---
```

```
[root@node01 ~]# istioctl --kubeconfig=c0.yaml x create-remote-secret --name c0 --auth-type=bearer-token
# This file is autogenerated, do not edit.
apiVersion: v1
kind: Secret
metadata:
  annotations:
    networking.istio.io/cluster: c0
  creationTimestamp: null
  labels:
    istio/multiCluster: "true"
  name: istio-remote-secret-c0
  namespace: istio-system
stringData:
  c0: |
    apiVersion: v1
    clusters:
    - cluster:
        certificate-authority-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUR1RENDQXFDZ0F3SUJBZ0lVQno0SjJKZ2JRczkzSUZJUS9yOXBXejFMSHZNd0RRWUpLb1pJaHZjTkFRRUwKQlFBd1lURUxNQWtHQTFVRUJoTUNRMDR4RVRBUEJnTlZCQWdUQ0VoaGJtZGFhRzkxTVFzd0NRWURWUVFIRXdKWQpVekVNTUFvR0ExVUVDaE1EYXpoek1ROHdEUVlEVlFRTEV3WlRlWE4wWlcweEV6QVJCZ05WQkFNVENtdDFZbVZ5CmJtVjBaWE13SUJjTk1qRXdPVEExTURjME1qQXdXaGdQTWpFeU1UQTRNVEl3TnpReU1EQmFNR0V4Q3pBSkJnTlYKQkFZVEFrTk9NUkV3RHdZRFZRUUlFd2hJWVc1bldtaHZkVEVMTUFrR0ExVUVCeE1DV0ZNeEREQUtCZ05WQkFvVApBMnM0Y3pFUE1BMEdBMVVFQ3hNR1UzbHpkR1Z0TVJNd0VRWURWUVFERXdwcmRXSmxjbTVsZEdWek1JSUJJakFOCkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXZWZWJGRWFiQjlXSHErbUNwc0twYTR1QUlBd1QKb2cyL21JYmExS2lpVzNwM0Q0eTQxdFdUY0RWS29DSjRDNmtJaDZiTG5BL1dTemhjMGd1Q1Y1ZGFxR0FxZUdzYgpLdDU2aWhmanAxcUNZaGV4cGxGK2hveVkwMWRrR0tUNkF1VE5HTTREVHlrWEpvY3E1NC85RGdtZkcvUGhZYWc4ClFNS0JwQXFnMnhXb2NscVh5LzErelJ6SEZwdklNU2txZEJKYjl3dU45Uk5HNlpVR1NkWXEyVDhOOStRL2NFakIKeU1CU084SHk4R1ByS29ROUFXczhqRVVUczQvb2tBbi9mM0Y0bDR4N0VadzBhTm9vTVY4YTFJL1pFSklBVzZmRwovdzc3aHZZWXp4ZjdEL2RSTnBmcVQyUThJUzJCYVh1TlFjOUZiRjdjS2VjdE9lSTROWXZhQ00zT0Z3SURBUUFCCm8yWXdaREFPQmdOVkhROEJBZjhFQkFNQ0FRWXdFZ1lEVlIwVEFRSC9CQWd3QmdFQi93SUJBakFkQmdOVkhRNEUKRmdRVUNsN21IbitQRTB4VXBDeFlZZlZaVFJoSEJaMHdId1lEVlIwakJCZ3dGb0FVQ2w3bUhuK1BFMHhVcEN4WQpZZlZaVFJoSEJaMHdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBQUVMVjhLQlZjSlN3dk5ZUEpJdnhYbWJ4UmtmCncvQkdFbmExRGluQldpL1hvYnRXZzVBQkh2ajBKSTVzaEUyZXk1U1NvU1hlemwvSVRNQ2ZRMU9vc1FmaXY1cUMKQzBYUkFVQ25qeWRRdmV0RWI5SUEvcU9xNDZpek15RWtaR1IvaHBBKzBqNDhsSWFiZzVxc2NoUG1YR25kVk5zVgpPUWRQWVRKVVJxeVBjRDBWZ0M3cnc4aWdUZEtWUWo4RzR5aituMUNDVFcrL3RyYjc4NkZKeERWTHlvN0twYUdvCjNlVnBYejdIRE0vSmVVbEhOUk1nbUpTV3pCankxVk1WWEIxV2hjN2ZzcnMwTXV2dkRmM0pIMTNXY0NuRXFYK2sKNEZxbkdmdGI4Z0hOVkx6S0RqRGgvcmZIeS9zV1pDRFhScSt6NGFrUm1YSFdSTzBvRXFkODBRSzVLYzg9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K
        server: https://192.168.229.128:6443
      name: c0
    contexts:
    - context:
        cluster: c0
        user: c0
      name: c0
    current-context: c0
    kind: Config
    preferences: {}
    users:
    - name: c0
      user:
        token: eyJhbGciOiJSUzI1NiIsImtpZCI6IkhXZktmUVpaUGdNNTliSWZDVVVjbHlEeDJuYlJPYmRqUzJFb2VOb09XTUEifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJpc3Rpby1zeXN0ZW0iLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoiaXN0aW8tcmVhZGVyLXNlcnZpY2UtYWNjb3VudC10b2tlbi01NHF3eCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50Lm5hbWUiOiJpc3Rpby1yZWFkZXItc2VydmljZS1hY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQudWlkIjoiZWM1NDU4NTYtNzE3OS00NjgzLTg4ZTYtNzBjY2U1NmI1MzZkIiwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50OmlzdGlvLXN5c3RlbTppc3Rpby1yZWFkZXItc2VydmljZS1hY2NvdW50In0.Oel0Qukrvqnf3VJQz7XLcUBaQ6oGQzctFr3OglxWDo7CdxncHz2rFnOlGBtSkbdjJiotc2-Hy5nl8gh1jwP_er9H8T6_f1AIU_SuTv38J_RBIwfD6DOKUHz4epieSDrZ9huZsd1lPGjUyJCSoF3ILYicK9reiGHMkUaF1jpvBtZ-t4CLTE_ti_4wtegEEOYvyXm1AKGYR4Ox4tTt46M0fmLgQx3t2ho-GxFTk4YUpNhGUtrdvjb6W_vekjndjbzFr8_9tsrRFVELPmPGSWWSKn7C2PrUq1tn50_CqgqiydH2qYjDPdwnD14r-qwA0fqyGHODPA_zUT4LqUGF6lhMoA
---
```

```
[root@node01 ~]# istioctl --kubeconfig=c0.yaml x create-remote-secret --name c0 --auth-type=bearer-token --type=config --create-service-account=true
# This file is autogenerated, do not edit.
apiVersion: v1
kind: Secret
metadata:
  annotations:
    networking.istio.io/cluster: c0
  creationTimestamp: null
  labels:
    istio/multiCluster: "true"
  name: istio-kubeconfig
  namespace: istio-system
stringData:
  config: |
    apiVersion: v1
    clusters:
    - cluster:
        certificate-authority-data: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUR1RENDQXFDZ0F3SUJBZ0lVQno0SjJKZ2JRczkzSUZJUS9yOXBXejFMSHZNd0RRWUpLb1pJaHZjTkFRRUwKQlFBd1lURUxNQWtHQTFVRUJoTUNRMDR4RVRBUEJnTlZCQWdUQ0VoaGJtZGFhRzkxTVFzd0NRWURWUVFIRXdKWQpVekVNTUFvR0ExVUVDaE1EYXpoek1ROHdEUVlEVlFRTEV3WlRlWE4wWlcweEV6QVJCZ05WQkFNVENtdDFZbVZ5CmJtVjBaWE13SUJjTk1qRXdPVEExTURjME1qQXdXaGdQTWpFeU1UQTRNVEl3TnpReU1EQmFNR0V4Q3pBSkJnTlYKQkFZVEFrTk9NUkV3RHdZRFZRUUlFd2hJWVc1bldtaHZkVEVMTUFrR0ExVUVCeE1DV0ZNeEREQUtCZ05WQkFvVApBMnM0Y3pFUE1BMEdBMVVFQ3hNR1UzbHpkR1Z0TVJNd0VRWURWUVFERXdwcmRXSmxjbTVsZEdWek1JSUJJakFOCkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXZWZWJGRWFiQjlXSHErbUNwc0twYTR1QUlBd1QKb2cyL21JYmExS2lpVzNwM0Q0eTQxdFdUY0RWS29DSjRDNmtJaDZiTG5BL1dTemhjMGd1Q1Y1ZGFxR0FxZUdzYgpLdDU2aWhmanAxcUNZaGV4cGxGK2hveVkwMWRrR0tUNkF1VE5HTTREVHlrWEpvY3E1NC85RGdtZkcvUGhZYWc4ClFNS0JwQXFnMnhXb2NscVh5LzErelJ6SEZwdklNU2txZEJKYjl3dU45Uk5HNlpVR1NkWXEyVDhOOStRL2NFakIKeU1CU084SHk4R1ByS29ROUFXczhqRVVUczQvb2tBbi9mM0Y0bDR4N0VadzBhTm9vTVY4YTFJL1pFSklBVzZmRwovdzc3aHZZWXp4ZjdEL2RSTnBmcVQyUThJUzJCYVh1TlFjOUZiRjdjS2VjdE9lSTROWXZhQ00zT0Z3SURBUUFCCm8yWXdaREFPQmdOVkhROEJBZjhFQkFNQ0FRWXdFZ1lEVlIwVEFRSC9CQWd3QmdFQi93SUJBakFkQmdOVkhRNEUKRmdRVUNsN21IbitQRTB4VXBDeFlZZlZaVFJoSEJaMHdId1lEVlIwakJCZ3dGb0FVQ2w3bUhuK1BFMHhVcEN4WQpZZlZaVFJoSEJaMHdEUVlKS29aSWh2Y05BUUVMQlFBRGdnRUJBQUVMVjhLQlZjSlN3dk5ZUEpJdnhYbWJ4UmtmCncvQkdFbmExRGluQldpL1hvYnRXZzVBQkh2ajBKSTVzaEUyZXk1U1NvU1hlemwvSVRNQ2ZRMU9vc1FmaXY1cUMKQzBYUkFVQ25qeWRRdmV0RWI5SUEvcU9xNDZpek15RWtaR1IvaHBBKzBqNDhsSWFiZzVxc2NoUG1YR25kVk5zVgpPUWRQWVRKVVJxeVBjRDBWZ0M3cnc4aWdUZEtWUWo4RzR5aituMUNDVFcrL3RyYjc4NkZKeERWTHlvN0twYUdvCjNlVnBYejdIRE0vSmVVbEhOUk1nbUpTV3pCankxVk1WWEIxV2hjN2ZzcnMwTXV2dkRmM0pIMTNXY0NuRXFYK2sKNEZxbkdmdGI4Z0hOVkx6S0RqRGgvcmZIeS9zV1pDRFhScSt6NGFrUm1YSFdSTzBvRXFkODBRSzVLYzg9Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K
        server: https://192.168.229.128:6443
      name: c0
    contexts:
    - context:
        cluster: c0
        user: c0
      name: c0
    current-context: c0
    kind: Config
    preferences: {}
    users:
    - name: c0
      user:
        token: eyJhbGciOiJSUzI1NiIsImtpZCI6IkhXZktmUVpaUGdNNTliSWZDVVVjbHlEeDJuYlJPYmRqUzJFb2VOb09XTUEifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJpc3Rpby1zeXN0ZW0iLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoiaXN0aW9kLXRva2VuLXZ4NXg4Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImlzdGlvZCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6ImQ1NTczZWYzLTg0ZDAtNGU0YS05NDJlLTgzNzk5NDI2NzI2MSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDppc3Rpby1zeXN0ZW06aXN0aW9kIn0.JEzdk0l7fo0SSJ8Wb88OQJW61_dvYErAHL0_Aw4Qyrk4wUA7VnPYF1RX0gVDmepS_z8lUgPISBpaPQJmFVwSAD5CSNCqHDAFII8-0cLUaT2oIyO7OQBjMecLt1E3WMGGW6RRR3ZgQ9WtqxMRmS0lXkjap-w-C-BHVkXHNuTJJbMyvcwQ6T1wtu4YPlYhJwjRBqgNI81b56lum56BwEOAQedzAXAes4zHdOrNuyWsv_LuJLtEuLY_99arYzbIH9-wZcea_9FW8DjSxbqZFSSJqszUBvxyMj27L2GMSYIR448AvOBo2wU9HowGWVfvXmks44iZrMtgc7tKO4lWDU-aAg
---
```



## describe

```
[root@node01 ~]# istioctl x describe --help
Describe resource and related Istio configuration

Usage:
  istioctl experimental describe [flags]
  istioctl experimental describe [command]

Aliases:
  describe, des

Available Commands:
  pod         Describe pods and their Istio configuration [kube-only]
  service     Describe services and their Istio configuration [kube-only]

Flags:
  -h, --help   help for describe

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl experimental describe [command] --help" for more information about a command.
```

### pod

```
[root@node01 ~]# istioctl x describe pod --help
Analyzes pod, its Services, DestinationRules, and VirtualServices and reports
the configuration objects that affect that pod.

THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl experimental describe pod <pod> [flags]

Aliases:
  pod, po

Examples:
  istioctl experimental describe pod productpage-v1-c7765c886-7zzd4

Flags:
  -h, --help             help for pod
      --ignoreUnmeshed   Suppress warnings for unmeshed pods

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl x describe pod productpage-v1-6b746f74dc-vlvmt -n istio
Pod: productpage-v1-6b746f74dc-vlvmt
   Pod Ports: 9080 (productpage), 15090 (istio-proxy)
--------------------
Service: productpage
   Port: http 9080/HTTP targets pod port 9080
RBAC policies: ns[istio]-policy[productpage-allow-all]-rule[0]


Exposed on Ingress Gateway http://192.168.198.155
VirtualService: .
```



### service

```
[root@node01 ~]# istioctl x describe service --help
Analyzes service, pods, DestinationRules, and VirtualServices and reports
the configuration objects that affect that service.

THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl experimental describe service <svc> [flags]

Aliases:
  service, svc

Examples:
  istioctl experimental describe service productpage

Flags:
  -h, --help             help for service
      --ignoreUnmeshed   Suppress warnings for unmeshed pods

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl x describe svc productpage -n istio
Service: productpage
   Port: http 9080/HTTP targets pod port 9080
RBAC policies: ns[istio]-policy[productpage-allow-all]-rule[0]


Exposed on Ingress Gateway http://192.168.198.155
VirtualService: .
```



## injector

```
[root@node01 ~]# istioctl x injector --help
List sidecar injector and sidecar versions

Usage:
  istioctl experimental injector [flags]
  istioctl experimental injector [command]

Examples:
  istioctl experimental injector list

Available Commands:
  list        List sidecar injector and sidecar versions

Flags:
  -h, --help   help for injector

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl experimental injector [command] --help" for more information about a command.
```

### list

```
[root@node01 ~]# istioctl x injector list --help
List sidecar injector and sidecar versions

Usage:
  istioctl experimental injector list [flags]

Examples:
  istioctl experimental injector list

Flags:
  -h, --help   help for list

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 ~]#  istioctl x injector list 
NAMESPACE      ISTIO-REVISION POD-REVISIONS
argo-rollouts  default        <non-Istio>: 1 NEEDS RESTART: 1
efk            default        <no pods>
gateway-system default        <non-Istio>: 3 NEEDS RESTART: 3
istio          default        <non-Istio>: 6 NEEDS RESTART: 6
vm-httpd       default        <no pods>

NAMESPACES          INJECTOR-HOOK              ISTIO-REVISION SIDECAR-IMAGE
argo-rollouts       istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
efk                 istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
gateway-system      istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
istio-system        istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
kube-node-lease     istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
kube-public         istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
kube-system         istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
vm-httpd            istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
istio               istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
argo-rollouts       istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
efk                 istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
gateway-system      istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
istio-system        istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
kube-node-lease     istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
kube-public         istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
kube-system         istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
vm-httpd            istio-revision-tag-default default        docker.io/istio/proxyv2:1.14.1
DOES NOT AUTOINJECT istio-sidecar-injector     default        docker.io/istio/proxyv2:1.14.1
```



## internal-debug

```
[root@node01 ~]# istioctl x internal-debug --help

Retrieves the debug information from Istiod or Pods in the mesh using the service account from the pod if --cert-dir is empty.
By default it will use the default serviceAccount from (istio-system) namespace if the pod is not specified.


THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl experimental internal-debug [<type>/]<name>[.<namespace>] [flags]

Examples:
  # Retrieve sync status for all Envoys in a mesh
  istioctl x internal-debug syncz

  # Retrieve sync diff for a single Envoy and Istiod
  istioctl x internal-debug syncz istio-egressgateway-59585c5b9c-ndc59.istio-system

  # SECURITY OPTIONS

  # Retrieve syncz debug information directly from the control plane, using token security
  # (This is the usual way to get the debug information with an out-of-cluster control plane.)
  istioctl x internal-debug syncz --xds-address istio.cloudprovider.example.com:15012

  # Retrieve syncz debug information via Kubernetes config, using token security
  # (This is the usual way to get the debug information with an in-cluster control plane.)
  istioctl x internal-debug syncz

  # Retrieve syncz debug information directly from the control plane, using RSA certificate security
  # (Certificates must be obtained before this step.  The --cert-dir flag lets istioctl bypass the Kubernetes API server.)
  istioctl x internal-debug syncz --xds-address istio.example.com:15012 --cert-dir ~/.istio-certs

  # Retrieve syncz information via XDS from specific control plane in multi-control plane in-cluster configuration
  # (Select a specific control plane in an in-cluster canary Istio configuration.)
  istioctl x internal-debug syncz --xds-label istio.io/rev=default


Flags:
      --all                  Send the same request to all instances of Istiod. Only applicable for in-cluster deployment.
      --authority string     XDS Subject Alternative Name (for example istiod.istio-system.svc)
      --cert-dir string      XDS Endpoint certificate directory
  -h, --help                 help for internal-debug
      --insecure             Skip server certificate and domain verification. (NOT SECURE!)
      --plaintext            Use plain-text HTTP/2 when connecting to server (no TLS).
  -r, --revision string      Control plane revision
      --timeout duration     The duration to wait before failing (default 30s)
      --xds-address string   XDS Endpoint
      --xds-label string     Istiod pod label selector
      --xds-port int         Istiod pod port (default 15012)

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

示例

```
[root@master01 istioctl]# istioctl x internal-debug syncz
[
  {
    "proxy": "my-nginx-v2-96db89644-ggcn6.istio",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=d9e07f5f-7ff4-4f3e-81d3-65a1c2252022",
    "cluster_acked": "uWs19d+uFvg=d9e07f5f-7ff4-4f3e-81d3-65a1c2252022",
    "listener_sent": "uWs19d+uFvg=d336ed91-9001-4e4e-ba25-5c62649463c7",
    "listener_acked": "uWs19d+uFvg=d336ed91-9001-4e4e-ba25-5c62649463c7",
    "route_sent": "uWs19d+uFvg=0964384f-4811-405e-9f0a-e64efd73f178",
    "route_acked": "uWs19d+uFvg=0964384f-4811-405e-9f0a-e64efd73f178",
    "endpoint_sent": "uWs19d+uFvg=f840a68b-4cf8-49de-915d-b2b69a172010",
    "endpoint_acked": "uWs19d+uFvg=f840a68b-4cf8-49de-915d-b2b69a172010"
  },
  {
    "proxy": "my-nginx-5974d8fddc-fksh4.istio",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=1bab020b-0906-42bf-a3ac-46f8f8663a55",
    "cluster_acked": "uWs19d+uFvg=1bab020b-0906-42bf-a3ac-46f8f8663a55",
    "listener_sent": "uWs19d+uFvg=a59e91fc-af5e-4265-afe3-d60429417e52",
    "listener_acked": "uWs19d+uFvg=a59e91fc-af5e-4265-afe3-d60429417e52",
    "route_sent": "uWs19d+uFvg=dffcaa37-703f-4935-9b49-6280b97c3306",
    "route_acked": "uWs19d+uFvg=dffcaa37-703f-4935-9b49-6280b97c3306",
    "endpoint_sent": "uWs19d+uFvg=f8b693a8-6b52-48db-969e-7e991770b253",
    "endpoint_acked": "uWs19d+uFvg=f8b693a8-6b52-48db-969e-7e991770b253"
  },
  {
    "proxy": "reviews-v2-7bf8c9648f-br6d9.istio",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=65658724-c601-4771-8abb-557745d6ec37",
    "cluster_acked": "uWs19d+uFvg=65658724-c601-4771-8abb-557745d6ec37",
    "listener_sent": "uWs19d+uFvg=95b23103-a8c0-45c0-a1fa-81ee4ab01ff6",
    "listener_acked": "uWs19d+uFvg=95b23103-a8c0-45c0-a1fa-81ee4ab01ff6",
    "route_sent": "uWs19d+uFvg=b42a3948-acaa-4f53-9280-c0de3fb2acc3",
    "route_acked": "uWs19d+uFvg=b42a3948-acaa-4f53-9280-c0de3fb2acc3",
    "endpoint_sent": "uWs19d+uFvg=183101e4-b890-4002-a2c4-fe2cfd1286db",
    "endpoint_acked": "uWs19d+uFvg=183101e4-b890-4002-a2c4-fe2cfd1286db"
  },
  {
    "proxy": "reviews-v3-84779c7bbc-5bkqm.istio",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=6a82e9e5-b8f6-4d3c-9813-2970faa2387c",
    "cluster_acked": "uWs19d+uFvg=6a82e9e5-b8f6-4d3c-9813-2970faa2387c",
    "listener_sent": "uWs19d+uFvg=66b82283-ea93-4da3-afff-a113093c21b1",
    "listener_acked": "uWs19d+uFvg=66b82283-ea93-4da3-afff-a113093c21b1",
    "route_sent": "uWs19d+uFvg=fbfe8026-509a-4b02-98c1-a126975de6e7",
    "route_acked": "uWs19d+uFvg=fbfe8026-509a-4b02-98c1-a126975de6e7",
    "endpoint_sent": "uWs19d+uFvg=c54c71a2-b390-44a2-a17c-166df52d0ff6",
    "endpoint_acked": "uWs19d+uFvg=c54c71a2-b390-44a2-a17c-166df52d0ff6"
  },
  {
    "proxy": "test-1.default",
    "istio_version": "65536.65536.65536"
  },
  {
    "proxy": "istio-egressgateway-6dc5588794-762v9.istio-system",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=ce4fddaf-aa3c-4152-837c-8e83d8e25fef",
    "cluster_acked": "uWs19d+uFvg=ce4fddaf-aa3c-4152-837c-8e83d8e25fef",
    "listener_sent": "uWs19d+uFvg=f383905f-e881-4dc0-a69d-06fa927c34c8",
    "listener_acked": "uWs19d+uFvg=f383905f-e881-4dc0-a69d-06fa927c34c8",
    "endpoint_sent": "uWs19d+uFvg=6c23e6b9-b763-490f-9d04-268c835f48df",
    "endpoint_acked": "uWs19d+uFvg=6c23e6b9-b763-490f-9d04-268c835f48df"
  },
  {
    "proxy": "productpage-v2-7b59b6d467-4t2tw.istio",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=3f925c2a-74c3-44ab-b478-d6fecc5d22d8",
    "cluster_acked": "uWs19d+uFvg=3f925c2a-74c3-44ab-b478-d6fecc5d22d8",
    "listener_sent": "uWs19d+uFvg=2de72949-c21e-4a42-852f-df9b9a6f5093",
    "listener_acked": "uWs19d+uFvg=2de72949-c21e-4a42-852f-df9b9a6f5093",
    "route_sent": "uWs19d+uFvg=24957ea9-5680-4329-8ee3-84c6028fe1c0",
    "route_acked": "uWs19d+uFvg=24957ea9-5680-4329-8ee3-84c6028fe1c0",
    "endpoint_sent": "uWs19d+uFvg=9beadb5d-64b8-428d-94ba-d9415e1c327c",
    "endpoint_acked": "uWs19d+uFvg=9beadb5d-64b8-428d-94ba-d9415e1c327c"
  },
  {
    "proxy": "details-v1-79f774bdb9-zd9fs.istio",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=f41c1048-1f44-4888-bb15-0cde7aaad4a3",
    "cluster_acked": "uWs19d+uFvg=f41c1048-1f44-4888-bb15-0cde7aaad4a3",
    "listener_sent": "uWs19d+uFvg=b27099d2-e2ef-473a-bda7-b5b5bf610b3d",
    "listener_acked": "uWs19d+uFvg=b27099d2-e2ef-473a-bda7-b5b5bf610b3d",
    "route_sent": "uWs19d+uFvg=2865af6c-a18f-4bc1-89dc-f2472e76401e",
    "route_acked": "uWs19d+uFvg=2865af6c-a18f-4bc1-89dc-f2472e76401e",
    "endpoint_sent": "uWs19d+uFvg=0484de6d-df3f-4d2d-b676-1e539ea8192a",
    "endpoint_acked": "uWs19d+uFvg=0484de6d-df3f-4d2d-b676-1e539ea8192a"
  },
  {
    "proxy": "tcp-echo-v2-56cd9b5c4f-hwsw4.istio",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=578d2b78-07c2-407b-8045-4f49903d9373",
    "cluster_acked": "uWs19d+uFvg=578d2b78-07c2-407b-8045-4f49903d9373",
    "listener_sent": "uWs19d+uFvg=61e926d8-32ad-4cfa-b319-a929f52d6119",
    "listener_acked": "uWs19d+uFvg=61e926d8-32ad-4cfa-b319-a929f52d6119",
    "route_sent": "uWs19d+uFvg=3db5e5ca-b7bb-4d8c-92d9-d7d7ee33bb65",
    "route_acked": "uWs19d+uFvg=3db5e5ca-b7bb-4d8c-92d9-d7d7ee33bb65",
    "endpoint_sent": "uWs19d+uFvg=da478eb5-6129-454b-a337-1cba4517ef57",
    "endpoint_acked": "uWs19d+uFvg=da478eb5-6129-454b-a337-1cba4517ef57"
  },
  {
    "proxy": "istio-eastwestgateway-7dbddff9bb-5b84g.istio-system",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=ec7bbd84-fae4-43cb-88d7-b4adc68784a2",
    "cluster_acked": "uWs19d+uFvg=ec7bbd84-fae4-43cb-88d7-b4adc68784a2",
    "listener_sent": "uWs19d+uFvg=aa16baad-8aa4-4fff-8b3d-285164ee7fa6",
    "listener_acked": "uWs19d+uFvg=aa16baad-8aa4-4fff-8b3d-285164ee7fa6",
    "endpoint_sent": "uWs19d+uFvg=0cfee915-7b6e-42cb-83ce-73da01fd94b8",
    "endpoint_acked": "uWs19d+uFvg=0cfee915-7b6e-42cb-83ce-73da01fd94b8"
  },
  {
    "proxy": "tcp-echo-866d7f8dcb-jhpfn.istio",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=f5884de9-3ea3-42bf-a562-64750683de7c",
    "cluster_acked": "uWs19d+uFvg=f5884de9-3ea3-42bf-a562-64750683de7c",
    "listener_sent": "uWs19d+uFvg=a5349b87-c153-40a8-a6dc-1d5dad00df55",
    "listener_acked": "uWs19d+uFvg=a5349b87-c153-40a8-a6dc-1d5dad00df55",
    "route_sent": "uWs19d+uFvg=37c24a98-24e3-4a4f-9cbc-755a12533f4c",
    "route_acked": "uWs19d+uFvg=37c24a98-24e3-4a4f-9cbc-755a12533f4c",
    "endpoint_sent": "uWs19d+uFvg=3d039160-e55b-499f-aba2-c947d6bd7746",
    "endpoint_acked": "uWs19d+uFvg=3d039160-e55b-499f-aba2-c947d6bd7746"
  },
  {
    "proxy": "ratings-v1-b6994bb9-gxxmq.istio",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=20bcb5a4-f72b-4e1f-a34c-aa243dd798cf",
    "cluster_acked": "uWs19d+uFvg=20bcb5a4-f72b-4e1f-a34c-aa243dd798cf",
    "listener_sent": "uWs19d+uFvg=d6f65f6e-c77a-4231-a5e4-fe456dc918cc",
    "listener_acked": "uWs19d+uFvg=d6f65f6e-c77a-4231-a5e4-fe456dc918cc",
    "route_sent": "uWs19d+uFvg=5c8ddef3-0df0-4832-b9f8-97fd30aff824",
    "route_acked": "uWs19d+uFvg=5c8ddef3-0df0-4832-b9f8-97fd30aff824",
    "endpoint_sent": "uWs19d+uFvg=4f539856-a149-411a-83f1-b1d7a98ada2e",
    "endpoint_acked": "uWs19d+uFvg=4f539856-a149-411a-83f1-b1d7a98ada2e"
  },
  {
    "proxy": "tcp-echo-v1-7dd5c5dcfb-dzkhq.istio",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=1bf347d9-2548-4474-93ef-f8ff8038faa3",
    "cluster_acked": "uWs19d+uFvg=1bf347d9-2548-4474-93ef-f8ff8038faa3",
    "listener_sent": "uWs19d+uFvg=23e2df07-8753-489f-a609-acf0384ead2f",
    "listener_acked": "uWs19d+uFvg=23e2df07-8753-489f-a609-acf0384ead2f",
    "route_sent": "uWs19d+uFvg=3e2ddee5-cc92-49ba-bb5d-c796395401d0",
    "route_acked": "uWs19d+uFvg=3e2ddee5-cc92-49ba-bb5d-c796395401d0",
    "endpoint_sent": "uWs19d+uFvg=103e7b15-18ad-4323-a4c0-21953b1539d2",
    "endpoint_acked": "uWs19d+uFvg=103e7b15-18ad-4323-a4c0-21953b1539d2"
  },
  {
    "proxy": "istio-ingressgateway-746c595f8b-bszqn.istio-system",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=ffddc1fb-c056-4935-82d1-fffd2ea2be99",
    "cluster_acked": "uWs19d+uFvg=ffddc1fb-c056-4935-82d1-fffd2ea2be99",
    "listener_sent": "uWs19d+uFvg=4acf84ae-a840-409e-8dc6-feff4a121a06",
    "listener_acked": "uWs19d+uFvg=4acf84ae-a840-409e-8dc6-feff4a121a06",
    "route_sent": "uWs19d+uFvg=10e23c9e-c797-4652-9235-3bf55a3f47d9",
    "route_acked": "uWs19d+uFvg=10e23c9e-c797-4652-9235-3bf55a3f47d9",
    "endpoint_sent": "uWs19d+uFvg=6c67e653-7a54-4c7b-84c5-d38e0eb5d5f5",
    "endpoint_acked": "uWs19d+uFvg=6c67e653-7a54-4c7b-84c5-d38e0eb5d5f5"
  },
  {
    "proxy": "reviews-v1-545db77b95-8gzq9.istio",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=7a283c93-eab9-4c49-8d26-85cb751e2040",
    "cluster_acked": "uWs19d+uFvg=7a283c93-eab9-4c49-8d26-85cb751e2040",
    "listener_sent": "uWs19d+uFvg=5edd92c3-2798-415e-a337-17fc2f645424",
    "listener_acked": "uWs19d+uFvg=5edd92c3-2798-415e-a337-17fc2f645424",
    "route_sent": "uWs19d+uFvg=6e83b5e0-7325-44aa-8adc-d2704ea401f0",
    "route_acked": "uWs19d+uFvg=6e83b5e0-7325-44aa-8adc-d2704ea401f0",
    "endpoint_sent": "uWs19d+uFvg=9921d1aa-1454-4092-be30-7d12393ead26",
    "endpoint_acked": "uWs19d+uFvg=9921d1aa-1454-4092-be30-7d12393ead26"
  },
  {
    "proxy": "productpage-v1-6b746f74dc-vlvmt.istio",
    "istio_version": "1.11.0",
    "cluster_sent": "uWs19d+uFvg=2e48a106-61ce-4942-8e48-611f2aa2c432",
    "cluster_acked": "uWs19d+uFvg=2e48a106-61ce-4942-8e48-611f2aa2c432",
    "listener_sent": "uWs19d+uFvg=e96cc44f-2cd9-45cc-a17f-ee76ac04d0ea",
    "listener_acked": "uWs19d+uFvg=e96cc44f-2cd9-45cc-a17f-ee76ac04d0ea",
    "route_sent": "uWs19d+uFvg=2a787962-0ad1-46ea-ab45-8f749438f319",
    "route_acked": "uWs19d+uFvg=2a787962-0ad1-46ea-ab45-8f749438f319",
    "endpoint_sent": "uWs19d+uFvg=45a8efd8-771f-48fc-9fd6-09ffa98db617",
    "endpoint_acked": "uWs19d+uFvg=45a8efd8-771f-48fc-9fd6-09ffa98db617"
  }
]
```

```
istioctl x internal-debug configz

istioctl x internal-debug syncz  --all

istioctl x internal-debug syncz  --authority istiod.istio-system.svc
istioctl x internal-debug syncz --xds-address 10.68.55.160:15012
istioctl x internal-debug syncz --xds-address 10.68.55.160:15012  --insecure

istioctl x internal-debug syncz    --plaintext
 istioctl x internal-debug syncz   --revision default
 
 istioctl x internal-debug syncz   --revision default --timeout 1s
 
  istioctl x internal-debug syncz   --xds-label app=istiod  --xds-port 15012 --xds-address 10.68.55.160:15012
  
  istioctl x internal-debug syncz productpage-v1-85c4dcbb4c-gsjgw.istio
```



## kube-uninject

```

[root@node01 ~]# istioctl x kube-uninject --help

kube-uninject is used to prevent Istio from adding a sidecar and
also provides the inverse of "istioctl kube-inject -f".

Usage:
  istioctl experimental kube-uninject [flags]

Examples:
  # Update resources before applying.
  kubectl apply -f <(istioctl experimental kube-uninject -f <resource.yaml>)

  # Create a persistent version of the deployment by removing Envoy sidecar.
  istioctl experimental kube-uninject -f deployment.yaml -o deployment-uninjected.yaml

  # Update an existing deployment.
  kubectl get deployment -o yaml | istioctl experimental kube-uninject -f - | kubectl apply -f -

Flags:
  -f, --filename string   Input Kubernetes resource filename
  -h, --help              help for kube-uninject
  -o, --output string     Modified output Kubernetes resource filename

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

示例

```
istioctl kube-inject -f productpage-deploy.yaml > productpage-deploy-inject.yaml

[root@master01 istioctl]# cat productpage-deploy-inject.yaml
apiVersion: v1
kind: Service
metadata:
  name: productpage
  labels:
    app: productpage
    service: productpage
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: bookinfo-productpage
  labels:
    account: productpage
---
apiVersion: apps/v1
kind: Deployment
metadata:
  creationTimestamp: null
  labels:
    app: productpage
    version: v1
  name: productpage-v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: productpage
      version: v1
  strategy: {}
  template:
    metadata:
      annotations:
        kubectl.kubernetes.io/default-container: productpage
        kubectl.kubernetes.io/default-logs-container: productpage
        prometheus.io/path: /stats/prometheus
        prometheus.io/port: "15020"
        prometheus.io/scrape: "true"
        sidecar.istio.io/status: '{"initContainers":["istio-init"],"containers":["istio-proxy"],"volumes":["istio-envoy","istio-data","istio-podinfo","istio-token","istiod-ca-cert"],"imagePullSecrets":null,"revision":"default"}'
      creationTimestamp: null
      labels:
        app: productpage
        security.istio.io/tlsMode: istio
        service.istio.io/canonical-name: productpage
        service.istio.io/canonical-revision: v1
        version: v1
    spec:
      containers:
      - image: docker.io/istio/examples-bookinfo-productpage-v1:1.16.2
        imagePullPolicy: IfNotPresent
        name: productpage
        ports:
        - containerPort: 9080
        resources: {}
        securityContext:
          runAsUser: 1000
        volumeMounts:
        - mountPath: /tmp
          name: tmp
      - args:
        - proxy
        - sidecar
        - --domain
        - $(POD_NAMESPACE).svc.cluster.local
        - --proxyLogLevel=warning
        - --proxyComponentLogLevel=misc:error
        - --log_output_level=default:info
        - --concurrency
        - "2"
        env:
        - name: JWT_POLICY
          value: third-party-jwt
        - name: PILOT_CERT_PROVIDER
          value: istiod
        - name: CA_ADDR
          value: istiod.istio-system.svc:15012
        - name: POD_NAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: POD_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: INSTANCE_IP
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
        - name: SERVICE_ACCOUNT
          valueFrom:
            fieldRef:
              fieldPath: spec.serviceAccountName
        - name: HOST_IP
          valueFrom:
            fieldRef:
              fieldPath: status.hostIP
        - name: PROXY_CONFIG
          value: |
            {"meshId":"mesh1"}
        - name: ISTIO_META_POD_PORTS
          value: |-
            [
                {"containerPort":9080}
            ]
        - name: ISTIO_META_APP_CONTAINERS
          value: productpage
        - name: ISTIO_META_INTERCEPTION_MODE
          value: REDIRECT
        - name: ISTIO_META_MESH_ID
          value: mesh1
        - name: TRUST_DOMAIN
          value: cluster.local
        - name: ISTIO_META_CLUSTER_ID
          value: Kubernetes
        image: docker.io/istio/proxyv2:1.11.0
        name: istio-proxy
        ports:
        - containerPort: 15090
          name: http-envoy-prom
          protocol: TCP
        readinessProbe:
          failureThreshold: 30
          httpGet:
            path: /healthz/ready
            port: 15021
          initialDelaySeconds: 1
          periodSeconds: 2
          timeoutSeconds: 3
        resources:
          limits:
            cpu: "2"
            memory: 1Gi
          requests:
            cpu: 10m
            memory: 40Mi
        securityContext:
          allowPrivilegeEscalation: false
          capabilities:
            drop:
            - ALL
          privileged: false
          readOnlyRootFilesystem: true
          runAsGroup: 1337
          runAsNonRoot: true
          runAsUser: 1337
        volumeMounts:
        - mountPath: /var/run/secrets/istio
          name: istiod-ca-cert
        - mountPath: /var/lib/istio/data
          name: istio-data
        - mountPath: /etc/istio/proxy
          name: istio-envoy
        - mountPath: /var/run/secrets/tokens
          name: istio-token
        - mountPath: /etc/istio/pod
          name: istio-podinfo
      initContainers:
      - args:
        - istio-iptables
        - -p
        - "15001"
        - -z
        - "15006"
        - -u
        - "1337"
        - -m
        - REDIRECT
        - -i
        - '*'
        - -x
        - ""
        - -b
        - '*'
        - -d
        - 15090,15021,15020
        image: docker.io/istio/proxyv2:1.11.0
        name: istio-init
        resources:
          limits:
            cpu: "2"
            memory: 1Gi
          requests:
            cpu: 10m
            memory: 40Mi
        securityContext:
          allowPrivilegeEscalation: false
          capabilities:
            add:
            - NET_ADMIN
            - NET_RAW
            drop:
            - ALL
          privileged: false
          readOnlyRootFilesystem: false
          runAsGroup: 0
          runAsNonRoot: false
          runAsUser: 0
      serviceAccountName: bookinfo-productpage
      volumes:
      - emptyDir:
          medium: Memory
        name: istio-envoy
      - emptyDir: {}
        name: istio-data
      - downwardAPI:
          items:
          - fieldRef:
              fieldPath: metadata.labels
            path: labels
          - fieldRef:
              fieldPath: metadata.annotations
            path: annotations
        name: istio-podinfo
      - name: istio-token
        projected:
          sources:
          - serviceAccountToken:
              audience: istio-ca
              expirationSeconds: 43200
              path: istio-token
      - configMap:
          name: istio-ca-root-cert
        name: istiod-ca-cert
      - emptyDir: {}
        name: tmp
status: {}
---
```

```
istioctl experimental kube-uninject -f productpage-deploy-inject.yaml -o productpage-deploy-uninject.yaml

[root@master01 istioctl]# cat productpage-deploy-uninject.yaml
apiVersion: v1
kind: Service
metadata:
  name: productpage
  labels:
    app: productpage
    service: productpage
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: bookinfo-productpage
  labels:
    account: productpage
---
apiVersion: apps/v1
kind: Deployment
metadata:
  creationTimestamp: null
  labels:
    app: productpage
    version: v1
  name: productpage-v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: productpage
      version: v1
  strategy: {}
  template:
    metadata:
      annotations:
        kubectl.kubernetes.io/default-container: productpage
        kubectl.kubernetes.io/default-logs-container: productpage
        prometheus.io/path: /stats/prometheus
        prometheus.io/port: "15020"
        prometheus.io/scrape: "true"
        sidecar.istio.io/inject: "false"
      creationTimestamp: null
      labels:
        app: productpage
        security.istio.io/tlsMode: istio
        service.istio.io/canonical-name: productpage
        service.istio.io/canonical-revision: v1
        version: v1
    spec:
      containers:
      - image: docker.io/istio/examples-bookinfo-productpage-v1:1.16.2
        imagePullPolicy: IfNotPresent
        name: productpage
        ports:
        - containerPort: 9080
        resources: {}
        securityContext:
          runAsUser: 1000
        volumeMounts:
        - mountPath: /tmp
          name: tmp
      serviceAccountName: bookinfo-productpage
      volumes:
      - emptyDir: {}
        name: tmp
status: {}
---
```



## metrics

```
[root@node01 ~]# istioctl x metrics --help

Prints the metrics for the specified service(s) when running in Kubernetes.

This command finds a Prometheus pod running in the specified istio system
namespace. It then executes a series of queries per requested workload to
find the following top-level workload metrics: total requests per second,
error rate, and request latency at p50, p90, and p99 percentiles. The
query results are printed to the console, organized by workload name.

All metrics returned are from server-side reports. This means that latencies
and error rates are from the perspective of the service itself and not of an
individual client (or aggregate set of clients). Rates and latencies are
calculated over a time interval of 1 minute.

Usage:
  istioctl experimental metrics <workload name>...

Aliases:
  metrics, m

Examples:
  # Retrieve workload metrics for productpage-v1 workload
  istioctl experimental metrics productpage-v1

  # Retrieve workload metrics for various services with custom duration
  istioctl experimental metrics productpage-v1 -d 2m

  # Retrieve workload metrics for various services in the different namespaces
  istioctl experimental metrics productpage-v1.foo reviews-v1.bar ratings-v1.baz

Flags:
  -d, --duration duration   Duration of query metrics, default value is 1m. (default 1m0s)
  -h, --help                help for metrics

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 ~]#  istioctl experimental metrics productpage-v1 -n istio
                                  WORKLOAD    TOTAL RPS    ERROR RPS  P50 LATENCY  P90 LATENCY  P99 LATENCY
                            productpage-v1        0.000        0.000           0s           0s           0s
```



## precheck

```
[root@node01 ~]# istioctl x precheck --help
precheck inspects a Kubernetes cluster for Istio install and upgrade requirements.

Usage:
  istioctl experimental precheck [flags]

Examples:
  # Verify that Istio can be installed or upgraded
  istioctl x precheck

  # Check only a single namespace
  istioctl x precheck --namespace default

Flags:
  -h, --help                help for precheck
  -r, --revision string     Control plane revision
      --skip-controlplane   skip checking the control plane

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 ~]# istioctl x precheck
✔ No issues found when checking the cluster. Istio is safe to install or upgrade!
  To get started, check out https://istio.io/latest/docs/setup/getting-started/
```

```
[root@node01 ~]#  istioctl x precheck --namespace default
✔ No issues found when checking the cluster. Istio is safe to install or upgrade!
  To get started, check out https://istio.io/latest/docs/setup/getting-started/
```



## proxy-status

```
[root@node01 ~]# istioctl x proxy-status --help

Retrieves last sent and last acknowledged xDS sync from Istiod to each Envoy in the mesh

Usage:
  istioctl experimental proxy-status [<type>/]<name>[.<namespace>] [flags]

Aliases:
  proxy-status, ps

Examples:
  # Retrieve sync status for all Envoys in a mesh
  istioctl x proxy-status

  # Retrieve sync diff for a single Envoy and Istiod
  istioctl x proxy-status istio-egressgateway-59585c5b9c-ndc59.istio-system

  # SECURITY OPTIONS

  # Retrieve proxy status information directly from the control plane, using token security
  # (This is the usual way to get the proxy-status with an out-of-cluster control plane.)
  istioctl x ps --xds-address istio.cloudprovider.example.com:15012

  # Retrieve proxy status information via Kubernetes config, using token security
  # (This is the usual way to get the proxy-status with an in-cluster control plane.)
  istioctl x proxy-status

  # Retrieve proxy status information directly from the control plane, using RSA certificate security
  # (Certificates must be obtained before this step.  The --cert-dir flag lets istioctl bypass the Kubernetes API server.)
  istioctl x ps --xds-address istio.example.com:15012 --cert-dir ~/.istio-certs

  # Retrieve proxy status information via XDS from specific control plane in multi-control plane in-cluster configuration
  # (Select a specific control plane in an in-cluster canary Istio configuration.)
  istioctl x ps --xds-label istio.io/rev=default


Flags:
      --authority string     XDS Subject Alternative Name (for example istiod.istio-system.svc)
      --cert-dir string      XDS Endpoint certificate directory
  -f, --file string          Envoy config dump JSON file
  -h, --help                 help for proxy-status
      --insecure             Skip server certificate and domain verification. (NOT SECURE!)
      --plaintext            Use plain-text HTTP/2 when connecting to server (no TLS).
  -r, --revision string      Control plane revision
      --timeout duration     The duration to wait before failing (default 30s)
      --xds-address string   XDS Endpoint
      --xds-label string     Istiod pod label selector
      --xds-port int         Istiod pod port (default 15012)

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 ~]# istioctl x ps
NAME                                                   CLUSTER        CDS        LDS        EDS        RDS          ECDS         ISTIOD                      VERSION
details-v1-584d9c9654-ldcch.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-egressgateway-575d8bd99b-7nv76.istio-system      Kubernetes     SYNCED     SYNCED     SYNCED     NOT_SENT     NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-ingressgateway-6668f9548d-8z8lq.istio-system     Kubernetes     SYNCED     SYNCED     SYNCED     NOT_SENT     NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
productpage-v1-85c4dcbb4c-gsjgw.istio                  Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
ratings-v1-8557bdf74b-9mw8d.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v1-5548d44b7d-86xc9.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v2-75d4b48554-7gjrx.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v3-5698f5dc86-mdww7.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
```

```
[root@node01 ~]#  istioctl x ps  --authority istiod.istio-system.svc
NAME                                                   CLUSTER        CDS        LDS        EDS        RDS          ECDS         ISTIOD                      VERSION
details-v1-584d9c9654-ldcch.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-egressgateway-575d8bd99b-7nv76.istio-system      Kubernetes     SYNCED     SYNCED     SYNCED     NOT_SENT     NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-ingressgateway-6668f9548d-8z8lq.istio-system     Kubernetes     SYNCED     SYNCED     SYNCED     NOT_SENT     NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
productpage-v1-85c4dcbb4c-gsjgw.istio                  Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
ratings-v1-8557bdf74b-9mw8d.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v1-5548d44b7d-86xc9.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v2-75d4b48554-7gjrx.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v3-5698f5dc86-mdww7.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
```

```
[root@node01 istioctl]# istioctl x ps -f rating_config_dump.json 
NAME                                                   CLUSTER        CDS        LDS        EDS        RDS          ECDS         ISTIOD                      VERSION
details-v1-584d9c9654-ldcch.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-egressgateway-575d8bd99b-7nv76.istio-system      Kubernetes     SYNCED     SYNCED     SYNCED     NOT_SENT     NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-ingressgateway-6668f9548d-8z8lq.istio-system     Kubernetes     SYNCED     SYNCED     SYNCED     NOT_SENT     NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
productpage-v1-85c4dcbb4c-gsjgw.istio                  Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
ratings-v1-8557bdf74b-9mw8d.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v1-5548d44b7d-86xc9.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v2-75d4b48554-7gjrx.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v3-5698f5dc86-mdww7.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
```

```
[root@node01 istioctl]# istioctl x ps --insecure 
NAME                                                   CLUSTER        CDS        LDS        EDS        RDS          ECDS         ISTIOD                      VERSION
details-v1-584d9c9654-ldcch.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-egressgateway-575d8bd99b-7nv76.istio-system      Kubernetes     SYNCED     SYNCED     SYNCED     NOT_SENT     NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-ingressgateway-6668f9548d-8z8lq.istio-system     Kubernetes     SYNCED     SYNCED     SYNCED     NOT_SENT     NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
productpage-v1-85c4dcbb4c-gsjgw.istio                  Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
ratings-v1-8557bdf74b-9mw8d.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v1-5548d44b7d-86xc9.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v2-75d4b48554-7gjrx.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v3-5698f5dc86-mdww7.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
```

```
[root@node01 istioctl]# istioctl x ps --timeout 1s
NAME                                                   CLUSTER        CDS        LDS        EDS        RDS          ECDS         ISTIOD                      VERSION
details-v1-584d9c9654-ldcch.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-egressgateway-575d8bd99b-7nv76.istio-system      Kubernetes     SYNCED     SYNCED     SYNCED     NOT_SENT     NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-ingressgateway-6668f9548d-8z8lq.istio-system     Kubernetes     SYNCED     SYNCED     SYNCED     NOT_SENT     NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
productpage-v1-85c4dcbb4c-gsjgw.istio                  Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
ratings-v1-8557bdf74b-9mw8d.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v1-5548d44b7d-86xc9.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v2-75d4b48554-7gjrx.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v3-5698f5dc86-mdww7.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
```

```
[root@node01 istioctl]#   istioctl x ps   --xds-label app=istiod  --xds-port 15012 --xds-address 10.68.55.160:15012
NAME                                                   CLUSTER        CDS        LDS        EDS        RDS          ECDS         ISTIOD                      VERSION
details-v1-584d9c9654-ldcch.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-egressgateway-575d8bd99b-7nv76.istio-system      Kubernetes     SYNCED     SYNCED     SYNCED     NOT_SENT     NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-ingressgateway-6668f9548d-8z8lq.istio-system     Kubernetes     SYNCED     SYNCED     SYNCED     NOT_SENT     NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
productpage-v1-85c4dcbb4c-gsjgw.istio                  Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
ratings-v1-8557bdf74b-9mw8d.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v1-5548d44b7d-86xc9.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v2-75d4b48554-7gjrx.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v3-5698f5dc86-mdww7.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT_SENT     istiod-8495d444bb-vvnpn     1.14.1
```



## revision

```
[root@node01 ~]# istioctl x revision --help
The revision command provides a revision centric view of istio deployments. It provides insight into IstioOperator CRs defining the revision, istiod and gateway pods which are part of deployment of a particular revision.

Usage:
  istioctl experimental revision [command]

Aliases:
  revision, rev

Available Commands:
  describe    Show information about a revision, including customizations, istiod version and which pods/gateways are using it.
  list        Show list of control plane and gateway revisions that are currently installed in cluster
  tag         Command group used to interact with revision tags

Flags:
  -h, --help               help for revision
  -d, --manifests string   Specify a path to a directory of charts and profiles
                           (e.g. ~/Downloads/istio-1.14.1/manifests)
                           or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                           
  -o, --output string      Output format for revision description (available formats: table,json) (default "table")
  -v, --verbose            Enable verbose output

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl experimental revision [command] --help" for more information about a command.
```

### describe

```
[root@node01 ~]# istioctl x revision describe --help
Show information about a revision, including customizations, istiod version and which pods/gateways are using it.

Usage:
  istioctl experimental revision describe [flags]

Examples:
  # View the details of a revision named 'canary'
  istioctl x revision describe canary

  # View the details of a revision named 'canary' and also the pods
  # under that particular revision
  istioctl x revision describe canary -v

  # Get details about a revision in json format (default format is human-friendly table format)
  istioctl x revision describe canary -v -o json


Flags:
  -h, --help   help for describe

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -d, --manifests string        Specify a path to a directory of charts and profiles
                                (e.g. ~/Downloads/istio-1.14.1/manifests)
                                or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                
  -n, --namespace string        Config namespace
  -o, --output string           Output format for revision description (available formats: table,json) (default "table")
  -v, --verbose                 Enable verbose output
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 ~]#  istioctl x revision describe default

ISTIO-OPERATOR CUSTOM RESOURCE: (1)
1. istio-system/installed-state
  COMPONENTS:
  - base
  - istiod
  - ingress:istio-ingressgateway
  - egress:istio-egressgateway
  CUSTOMIZATIONS:
  <no-customizations>

MUTATING WEBHOOK CONFIGURATIONS: (2)
WEBHOOK                    TAG
istio-revision-tag-default default
istio-sidecar-injector     <no-tag>

CONTROL PLANE PODS (ISTIOD): (1)
NAMESPACE    NAME                    ADDRESS      STATUS  AGE
istio-system istiod-8495d444bb-vvnpn 172.20.1.187 Running 3h52m

INGRESS GATEWAYS: (1)
NAMESPACE    NAME                                  ADDRESS      STATUS  AGE
istio-system istio-ingressgateway-6668f9548d-8z8lq 172.20.1.188 Running 3h50m

EGRESS GATEWAYS: (1)
NAMESPACE    NAME                                 ADDRESS     STATUS  AGE
istio-system istio-egressgateway-575d8bd99b-7nv76 172.20.2.89 Running 3h50m
```



### list

```
[root@node01 ~]# istioctl x revision list --help
Show list of control plane and gateway revisions that are currently installed in cluster

Usage:
  istioctl experimental revision list [flags]

Examples:
  # View summary of revisions installed in the current cluster
  # which can be overridden with --context parameter.
  istioctl x revision list

  # View list of revisions including customizations, istiod and gateway pods
  istioctl x revision list -v


Flags:
  -h, --help   help for list

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -d, --manifests string        Specify a path to a directory of charts and profiles
                                (e.g. ~/Downloads/istio-1.14.1/manifests)
                                or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                
  -n, --namespace string        Config namespace
  -o, --output string           Output format for revision description (available formats: table,json) (default "table")
  -v, --verbose                 Enable verbose output
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```



```
[root@node01 ~]#  istioctl x revision list
REVISION TAG     ISTIO-OPERATOR-CR            PROFILE REQD-COMPONENTS
default  default istio-system/installed-state demo    base
                                                      istiod
                                                      ingress:istio-ingressgateway
                                                      egress:istio-egressgateway
```



### tag

```
[root@node01 ~]# istioctl x revision tag --help
Command group used to interact with revision tags. Revision tags allow for the creation of mutable aliases
referring to control plane revisions for sidecar injection.

With revision tags, rather than relabeling a namespace from "istio.io/rev=revision-a" to "istio.io/rev=revision-b" to
change which control plane revision handles injection, it's possible to create a revision tag "prod" and label our
namespace "istio.io/rev=prod". The "prod" revision tag could point to "1-7-6" initially and then be changed to point to "1-8-1"
at some later point.

This allows operators to change which Istio control plane revision should handle injection for a namespace or set of namespaces
without manual relabeling of the "istio.io/rev" tag.

Usage:
  istioctl experimental revision tag [flags]
  istioctl experimental revision tag [command]

Available Commands:
  generate    Generate configuration for a revision tag to stdout
  list        List existing revision tags
  remove      Remove Istio control plane revision tag
  set         Create or modify revision tags

Flags:
  -h, --help   help for tag

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -d, --manifests string        Specify a path to a directory of charts and profiles
                                (e.g. ~/Downloads/istio-1.14.1/manifests)
                                or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                
  -n, --namespace string        Config namespace
  -o, --output string           Output format for revision description (available formats: table,json) (default "table")
  -v, --verbose                 Enable verbose output
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl experimental revision tag [command] --help" for more information about a command.
```

#### generate

```
[root@node01 ~]# istioctl x revision tag generate --help
Create a revision tag and output to the command's stdout. Tag an Istio control plane revision for use with namespace istio.io/rev
injection labels.

Usage:
  istioctl experimental revision tag generate <revision-tag> [flags]

Examples:
 # Create a revision tag from the "1-8-0" revision
 istioctl tag generate prod --revision 1-8-0 > tag.yaml

 # Apply the tag to cluster
 kubectl apply -f tag.yaml

 # Point namespace "test-ns" at the revision pointed to by the "prod" revision tag
 kubectl label ns test-ns istio.io/rev=prod

 # Rollout namespace "test-ns" to update workloads to the "1-8-0" revision
 kubectl rollout restart deployments -n test-ns


Flags:
      --auto-inject-namespaces   If set to true, the sidecars should be automatically injected into all namespaces by default
  -h, --help                     help for generate
      --overwrite                If true, allow revision tags to be overwritten, otherwise reject revision tag updates that
                                 overwrite existing revision tags.
  -r, --revision string          Control plane revision to reference from a given revision tag
  -y, --skip-confirmation        The skipConfirmation determines whether the user is prompted for confirmation.
                                 If set to true, the user is not prompted and a Yes response is assumed in all cases.
      --webhook-name string      Name to use for a revision tag's mutating webhook configuration.

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -d, --manifests string        Specify a path to a directory of charts and profiles
                                (e.g. ~/Downloads/istio-1.14.1/manifests)
                                or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                
  -n, --namespace string        Config namespace
  -o, --output string           Output format for revision description (available formats: table,json) (default "table")
  -v, --verbose                 Enable verbose output
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 ~]# istioctl tag generate prod --revision default
apiVersion: admissionregistration.k8s.io/v1
kind: MutatingWebhookConfiguration
metadata:
  creationTimestamp: null
  labels:
    app: sidecar-injector
    install.operator.istio.io/owning-resource: unknown
    istio.io/rev: default
    istio.io/tag: prod
    operator.istio.io/component: Pilot
    release: istio
  name: istio-revision-tag-prod
webhooks:
- admissionReviewVersions:
  - v1beta1
  - v1
  clientConfig:
    caBundle: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUZGRENDQXZ5Z0F3SUJBZ0lVSzZSVkJIMHJ4RmdEejNMaTZScFM3OStTY0dRd0RRWUpLb1pJaHZjTkFRRUwKQlFBd0lqRU9NQXdHQTFVRUNnd0ZTWE4wYVc4eEVEQU9CZ05WQkFNTUIxSnZiM1FnUTBFd0hoY05Nakl3TkRJMwpNRFUwT1RJMldoY05Nekl3TkRJME1EVTBPVEkyV2pBaU1RNHdEQVlEVlFRS0RBVkpjM1JwYnpFUU1BNEdBMVVFCkF3d0hVbTl2ZENCRFFUQ0NBaUl3RFFZSktvWklodmNOQVFFQkJRQURnZ0lQQURDQ0Fnb0NnZ0lCQU13WFJ0MEoKRWNTbWF2aXM5d3dIL2xzemx5VlQvcm4vYjkxQWY1SlNFLzVzMVpUZXY3NVEzSkpBSURpV01TOVdFRlV0R0x2QQpHbVorT1RONXgrNGx0bXBlaUpGeFR2Z2VMYWlCNmQvZEFyQmtnY1BVSjFWQ2IycXdYVVN1ZEw3T1BrU0ZtUkxNCnpNRGdNcEQrQzZ0MXIxUjZOWVNxYWFjeHdiY0xQMVZJSHlHUnBiTlRQaGdUb3Fna2pOKyt6UHVVMEJMYzBaOXcKMllDTkp6c3Fuall1SklYMGhFRXh4UUFwbzBsM3dDekNVZ2grRHAvNnJLUm9zY1UyV2ovdlRYTUR4RU14OUZydwp6Y1dVcG8xWXpRekJSQ3pKNGNZdytzQ2Q0OGFITTBja2NhVVdoWkM3Y3o5Qndvc1pHd05BUTNMdzViTTR0WG5EClhKV1paWVdxNHV2MnhkV1JsN25kK2VEbUVyWjhjVDBZWWpiTzFsbWpoMjR1cUtCUWJMb2o5K0RESVpTZVVqR0UKK3grQUw5ajVTcnBlMzBNcGRMVUJCaXVQOVFRL1B4WC8rQjY2UWM5Q2tDWXZReitpcnZaNnBCdUlWb1FqYWY2dQpIWEgvZHFZSWJZRzY5QXYySlZtVE1NSjBVMkRUbVNzNXQyREhEUnBzK1NMeTRtR2Jra253WFpmNk5lR3Y2YzdNCmwvN2pvK2M2VnB4dU51Y3dQcnI1WTJOOUxudXZuRUxidTdWamI2TXlleW5UTHE2ZHBZZXU2Z2RPYUFUbzAvcFUKT1JmS2wzV2xBN0daSHdVTi9LWlNHeXZyc25pek9ZMi93U1BvdERNQldkLzYyN2ZJenIrZU1EZFhSWTZhQXFJWQpRT1lVSXZjTVdNd1BsWS9qd1hWMnBBMjFHZm1Ia0lEYURhM05BZ01CQUFHalFqQkFNQjBHQTFVZERnUVdCQlFPCjZYQUh2TmVwKzRnVDZHc1FtSGFWeFlGSGl6QVBCZ05WSFJNQkFmOEVCVEFEQVFIL01BNEdBMVVkRHdFQi93UUUKQXdJQzVEQU5CZ2txaGtpRzl3MEJBUXNGQUFPQ0FnRUFETXRnWDJjZkFJSys2Y1pwNWVVeEVrTFhRNGlFYXBwVApaUmdSRFVoUjR2NzFTR0tGbGVEZkhUbDd6eEJOdHRmU2owa1RXMlRCVzNFMENQV2RWQnd0eWhhT1M1QW1FbWdDCkZWNWlqRXdHSEJpa3I0L2R3SzBYUDkrTmRsR3VwTURBQk5nbU5udm1kYVJNbG5xQjhKWGFJMlFCV1R6UFFtd2cKQ1pPWGhURUR1YjAvQ3VoczJHOEhISUVFYzFEQU1Ib3BjQUo5TkpMMU5NQVRTN1JNN2l0bmx1cGVwbGJLK1FaSgp3aWF2Z1VEbGNKMzkycmoyOCtkRjc3VDFhQ1k2SG1ZazhpSTNjY3BsdmFIdlI3NnBUOXpGUldmeUtMaFRpTCtwCmtaWDNjaFl4dmpYdFhtcUNtMVQ4T1pJbHBPS1BDWDlTU1FzSDJNZDQrcDUyT0ljclNVcm5Mc1M4R0NPeVlGYmwKZUhBTmdrTWhnNXo3Z1FqZXc2ellWbVVBdUszSkVJNlZ5WnBCWUdheEV3R0N1MU9PMDJ1dW15WVNnemdzNW5jegpwNjc4YzJHVE5DU1lQMWN4TUoxc0dEelB2d3ZEcnNVYTNDQ0RtcXFGYkd6WWRGNWkxajQrZHA2THlqNVJOYTJUCmp5UFhZa1BIUDJKeWY5VDhRQm9mUWliZHNuczRGQXJ4ekFlRUhpRndOMkF6M3grTjRrcUt0OUpDbW9yTFp0WjMKcEZZYkJ2emg2dDNHcnhRTEdwNjFRKzFBUkJkNytEc0ZYSWZ6dmFuRmUxeTAvM2JFQlVIb1ZwWDBLMDQ5VGw5SwoxMXNmVzBabWRYRC9pQkx2RWNMaEtYakhGdXNnRW03VVl0bGVEQnhjQXpIdWMrdzlkSzZVMnlUN2lZS0VwNDUyCmtmRHVIVGl6Q0hRPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==
    service:
      name: istiod
      namespace: istio-system
      path: /inject
      port: 443
  failurePolicy: Fail
  name: rev.namespace.sidecar-injector.istio.io
  namespaceSelector:
    matchExpressions:
    - key: istio.io/rev
      operator: In
      values:
      - prod
    - key: istio-injection
      operator: DoesNotExist
  objectSelector:
    matchExpressions:
    - key: sidecar.istio.io/inject
      operator: NotIn
      values:
      - "false"
  rules:
  - apiGroups:
    - ""
    apiVersions:
    - v1
    operations:
    - CREATE
    resources:
    - pods
  sideEffects: None
- admissionReviewVersions:
  - v1beta1
  - v1
  clientConfig:
    caBundle: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUZGRENDQXZ5Z0F3SUJBZ0lVSzZSVkJIMHJ4RmdEejNMaTZScFM3OStTY0dRd0RRWUpLb1pJaHZjTkFRRUwKQlFBd0lqRU9NQXdHQTFVRUNnd0ZTWE4wYVc4eEVEQU9CZ05WQkFNTUIxSnZiM1FnUTBFd0hoY05Nakl3TkRJMwpNRFUwT1RJMldoY05Nekl3TkRJME1EVTBPVEkyV2pBaU1RNHdEQVlEVlFRS0RBVkpjM1JwYnpFUU1BNEdBMVVFCkF3d0hVbTl2ZENCRFFUQ0NBaUl3RFFZSktvWklodmNOQVFFQkJRQURnZ0lQQURDQ0Fnb0NnZ0lCQU13WFJ0MEoKRWNTbWF2aXM5d3dIL2xzemx5VlQvcm4vYjkxQWY1SlNFLzVzMVpUZXY3NVEzSkpBSURpV01TOVdFRlV0R0x2QQpHbVorT1RONXgrNGx0bXBlaUpGeFR2Z2VMYWlCNmQvZEFyQmtnY1BVSjFWQ2IycXdYVVN1ZEw3T1BrU0ZtUkxNCnpNRGdNcEQrQzZ0MXIxUjZOWVNxYWFjeHdiY0xQMVZJSHlHUnBiTlRQaGdUb3Fna2pOKyt6UHVVMEJMYzBaOXcKMllDTkp6c3Fuall1SklYMGhFRXh4UUFwbzBsM3dDekNVZ2grRHAvNnJLUm9zY1UyV2ovdlRYTUR4RU14OUZydwp6Y1dVcG8xWXpRekJSQ3pKNGNZdytzQ2Q0OGFITTBja2NhVVdoWkM3Y3o5Qndvc1pHd05BUTNMdzViTTR0WG5EClhKV1paWVdxNHV2MnhkV1JsN25kK2VEbUVyWjhjVDBZWWpiTzFsbWpoMjR1cUtCUWJMb2o5K0RESVpTZVVqR0UKK3grQUw5ajVTcnBlMzBNcGRMVUJCaXVQOVFRL1B4WC8rQjY2UWM5Q2tDWXZReitpcnZaNnBCdUlWb1FqYWY2dQpIWEgvZHFZSWJZRzY5QXYySlZtVE1NSjBVMkRUbVNzNXQyREhEUnBzK1NMeTRtR2Jra253WFpmNk5lR3Y2YzdNCmwvN2pvK2M2VnB4dU51Y3dQcnI1WTJOOUxudXZuRUxidTdWamI2TXlleW5UTHE2ZHBZZXU2Z2RPYUFUbzAvcFUKT1JmS2wzV2xBN0daSHdVTi9LWlNHeXZyc25pek9ZMi93U1BvdERNQldkLzYyN2ZJenIrZU1EZFhSWTZhQXFJWQpRT1lVSXZjTVdNd1BsWS9qd1hWMnBBMjFHZm1Ia0lEYURhM05BZ01CQUFHalFqQkFNQjBHQTFVZERnUVdCQlFPCjZYQUh2TmVwKzRnVDZHc1FtSGFWeFlGSGl6QVBCZ05WSFJNQkFmOEVCVEFEQVFIL01BNEdBMVVkRHdFQi93UUUKQXdJQzVEQU5CZ2txaGtpRzl3MEJBUXNGQUFPQ0FnRUFETXRnWDJjZkFJSys2Y1pwNWVVeEVrTFhRNGlFYXBwVApaUmdSRFVoUjR2NzFTR0tGbGVEZkhUbDd6eEJOdHRmU2owa1RXMlRCVzNFMENQV2RWQnd0eWhhT1M1QW1FbWdDCkZWNWlqRXdHSEJpa3I0L2R3SzBYUDkrTmRsR3VwTURBQk5nbU5udm1kYVJNbG5xQjhKWGFJMlFCV1R6UFFtd2cKQ1pPWGhURUR1YjAvQ3VoczJHOEhISUVFYzFEQU1Ib3BjQUo5TkpMMU5NQVRTN1JNN2l0bmx1cGVwbGJLK1FaSgp3aWF2Z1VEbGNKMzkycmoyOCtkRjc3VDFhQ1k2SG1ZazhpSTNjY3BsdmFIdlI3NnBUOXpGUldmeUtMaFRpTCtwCmtaWDNjaFl4dmpYdFhtcUNtMVQ4T1pJbHBPS1BDWDlTU1FzSDJNZDQrcDUyT0ljclNVcm5Mc1M4R0NPeVlGYmwKZUhBTmdrTWhnNXo3Z1FqZXc2ellWbVVBdUszSkVJNlZ5WnBCWUdheEV3R0N1MU9PMDJ1dW15WVNnemdzNW5jegpwNjc4YzJHVE5DU1lQMWN4TUoxc0dEelB2d3ZEcnNVYTNDQ0RtcXFGYkd6WWRGNWkxajQrZHA2THlqNVJOYTJUCmp5UFhZa1BIUDJKeWY5VDhRQm9mUWliZHNuczRGQXJ4ekFlRUhpRndOMkF6M3grTjRrcUt0OUpDbW9yTFp0WjMKcEZZYkJ2emg2dDNHcnhRTEdwNjFRKzFBUkJkNytEc0ZYSWZ6dmFuRmUxeTAvM2JFQlVIb1ZwWDBLMDQ5VGw5SwoxMXNmVzBabWRYRC9pQkx2RWNMaEtYakhGdXNnRW03VVl0bGVEQnhjQXpIdWMrdzlkSzZVMnlUN2lZS0VwNDUyCmtmRHVIVGl6Q0hRPQotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==
    service:
      name: istiod
      namespace: istio-system
      path: /inject
      port: 443
  failurePolicy: Fail
  name: rev.object.sidecar-injector.istio.io
  namespaceSelector:
    matchExpressions:
    - key: istio.io/rev
      operator: DoesNotExist
    - key: istio-injection
      operator: DoesNotExist
  objectSelector:
    matchExpressions:
    - key: sidecar.istio.io/inject
      operator: NotIn
      values:
      - "false"
    - key: istio.io/rev
      operator: In
      values:
      - prod
  rules:
  - apiGroups:
    - ""
    apiVersions:
    - v1
    operations:
    - CREATE
    resources:
    - pods
  sideEffects: None
```

```
[root@master01 istioctl]# istioctl x revision tag generate prod --revision=1-10-0 --overwrite
apiVersion: admissionregistration.k8s.io/v1
kind: MutatingWebhookConfiguration
metadata:
  creationTimestamp: null
  labels:
    app: sidecar-injector
    install.operator.istio.io/owning-resource: unknown
    istio.io/rev: 1-10-0
    istio.io/tag: prod
    operator.istio.io/component: Pilot
    release: istio
  name: istio-revision-tag-prod
webhooks:
- admissionReviewVersions:
  - v1beta1
  - v1
  clientConfig:
    caBundle: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUMvRENDQWVTZ0F3SUJBZ0lRZmlxWFM5QnV0dWVJbHdhZ2htbjhpakFOQmdrcWhraUc5dzBCQVFzRkFEQVkKTVJZd0ZBWURWUVFLRXcxamJIVnpkR1Z5TG14dlkyRnNNQjRYRFRJeE1EVXlNVEEwTVRnME0xb1hEVE14TURVeApPVEEwTVRnME0xb3dHREVXTUJRR0ExVUVDaE1OWTJ4MWMzUmxjaTVzYjJOaGJEQ0NBU0l3RFFZSktvWklodmNOCkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFKdmZ1UmJGRUN4NXBYWCtaZDZoRkQ5MEJSVmpDaE1IaHVaK29MV1AKcG5GYlQ1dDFHMWZ6VDFTa3VBRFF3V2YwMm94OUVjK0NGMzk1RHdGZDc4ZVliMUlFVmF1c0w4ZTNyUWVRckJpVQpsUnNtM1F2WGo1RUNCZjV0Uk5aWEd0WjZsaWNTNFUrc0JCQ2tVb3p5dUFib3R5b0ZnekxJbzFpemhwcjJYeGx2Cmp1SnpmT0ljMTN4Y09LaHNyRkZMQnJvYnpuS0p2R0VWaU9QYVYwa1ZXYWgycGZTdFAyYXNuMG1nQkJjZmw0aksKNUw2dWJlQjFva2s4aWdYTUtNbUdnWm5DYW51SmJYNTVLOXhlaUJld1k4TG9KVy9pRm15eTd1VkNidGEzNnBBMwo1UVdibEtIRUVadDViMVQ0R1o1VUlPWkc0em5jWkNpVmJmV3JsZFpFVmpYYWFXOENBd0VBQWFOQ01FQXdEZ1lEClZSMFBBUUgvQkFRREFnSUVNQThHQTFVZEV3RUIvd1FGTUFNQkFmOHdIUVlEVlIwT0JCWUVGSm1ITkZYOFBRR2gKNjZTM2UwVjFUbVJtckNscE1BMEdDU3FHU0liM0RRRUJDd1VBQTRJQkFRQTJLTHptcWpjc2QyT1Mza1IyaVh1cQowUzhBdFRldE5qc0NiWXIva3BDNk16eld2Z3lyVU1KZWYrdVYwYUJPMzNmbFMxSVc1dy95MGxERFBFYXI5a3NICnQwb2pEekZ5a2V6N3hac05wSjl0blZubjhERldiRzh3VUpTWUphM1ZYbXdKK1BTbHh6VzF6NEVqZmUrdEwwRjMKcUJqNDVlRlFWOFdzQ3RBSWhSK0xNaWF4SkhXWU1OTERndnNsbDJ1K1RJZEZIMFptS3RFbUdzLy82UXc4RkRyUQpVVjVxc2d3ZnpqSjFXSjZlbFM3dCswM0pQVlFpWFk3dlNMU295VzlNeVFCYm0rdFlwcExJR0NpREFJTzYzbGVtCkZnTGF4bUUzYmROZ0FhVDhsZGcvU2h5cEw3S1hueE95Zjd3T1NLSVRDQmZMY0F6dGVVS3N2R0ZNU01sT0s0RkwKLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=
    service:
      name: istiod-1-10-0
      namespace: istio-system
      path: /inject
  failurePolicy: Fail
  name: rev.namespace.sidecar-injector.istio.io
  namespaceSelector:
    matchExpressions:
    - key: istio.io/rev
      operator: In
      values:
      - prod
    - key: istio-injection
      operator: DoesNotExist
  objectSelector:
    matchExpressions:
    - key: sidecar.istio.io/inject
      operator: NotIn
      values:
      - "false"
  rules:
  - apiGroups:
    - ""
    apiVersions:
    - v1
    operations:
    - CREATE
    resources:
    - pods
  sideEffects: None
- admissionReviewVersions:
  - v1beta1
  - v1
  clientConfig:
    caBundle: LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUMvRENDQWVTZ0F3SUJBZ0lRZmlxWFM5QnV0dWVJbHdhZ2htbjhpakFOQmdrcWhraUc5dzBCQVFzRkFEQVkKTVJZd0ZBWURWUVFLRXcxamJIVnpkR1Z5TG14dlkyRnNNQjRYRFRJeE1EVXlNVEEwTVRnME0xb1hEVE14TURVeApPVEEwTVRnME0xb3dHREVXTUJRR0ExVUVDaE1OWTJ4MWMzUmxjaTVzYjJOaGJEQ0NBU0l3RFFZSktvWklodmNOCkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFKdmZ1UmJGRUN4NXBYWCtaZDZoRkQ5MEJSVmpDaE1IaHVaK29MV1AKcG5GYlQ1dDFHMWZ6VDFTa3VBRFF3V2YwMm94OUVjK0NGMzk1RHdGZDc4ZVliMUlFVmF1c0w4ZTNyUWVRckJpVQpsUnNtM1F2WGo1RUNCZjV0Uk5aWEd0WjZsaWNTNFUrc0JCQ2tVb3p5dUFib3R5b0ZnekxJbzFpemhwcjJYeGx2Cmp1SnpmT0ljMTN4Y09LaHNyRkZMQnJvYnpuS0p2R0VWaU9QYVYwa1ZXYWgycGZTdFAyYXNuMG1nQkJjZmw0aksKNUw2dWJlQjFva2s4aWdYTUtNbUdnWm5DYW51SmJYNTVLOXhlaUJld1k4TG9KVy9pRm15eTd1VkNidGEzNnBBMwo1UVdibEtIRUVadDViMVQ0R1o1VUlPWkc0em5jWkNpVmJmV3JsZFpFVmpYYWFXOENBd0VBQWFOQ01FQXdEZ1lEClZSMFBBUUgvQkFRREFnSUVNQThHQTFVZEV3RUIvd1FGTUFNQkFmOHdIUVlEVlIwT0JCWUVGSm1ITkZYOFBRR2gKNjZTM2UwVjFUbVJtckNscE1BMEdDU3FHU0liM0RRRUJDd1VBQTRJQkFRQTJLTHptcWpjc2QyT1Mza1IyaVh1cQowUzhBdFRldE5qc0NiWXIva3BDNk16eld2Z3lyVU1KZWYrdVYwYUJPMzNmbFMxSVc1dy95MGxERFBFYXI5a3NICnQwb2pEekZ5a2V6N3hac05wSjl0blZubjhERldiRzh3VUpTWUphM1ZYbXdKK1BTbHh6VzF6NEVqZmUrdEwwRjMKcUJqNDVlRlFWOFdzQ3RBSWhSK0xNaWF4SkhXWU1OTERndnNsbDJ1K1RJZEZIMFptS3RFbUdzLy82UXc4RkRyUQpVVjVxc2d3ZnpqSjFXSjZlbFM3dCswM0pQVlFpWFk3dlNMU295VzlNeVFCYm0rdFlwcExJR0NpREFJTzYzbGVtCkZnTGF4bUUzYmROZ0FhVDhsZGcvU2h5cEw3S1hueE95Zjd3T1NLSVRDQmZMY0F6dGVVS3N2R0ZNU01sT0s0RkwKLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=
    service:
      name: istiod-1-10-0
      namespace: istio-system
      path: /inject
  failurePolicy: Fail
  name: rev.object.sidecar-injector.istio.io
  namespaceSelector:
    matchExpressions:
    - key: istio.io/rev
      operator: DoesNotExist
    - key: istio-injection
      operator: DoesNotExist
  objectSelector:
    matchExpressions:
    - key: sidecar.istio.io/inject
      operator: NotIn
      values:
      - "false"
    - key: istio.io/rev
      operator: In
      values:
      - prod
  rules:
  - apiGroups:
    - ""
    apiVersions:
    - v1
    operations:
    - CREATE
    resources:
    - pods
  sideEffects: None
```

```
istioctl x revision tag generate prod --revision default  --webhook-name test
```



#### list

```
[root@node01 ~]# istioctl x revision tag list --help
List existing revision tags

Usage:
  istioctl experimental revision tag list [flags]

Aliases:
  list, show

Examples:
istioctl tag list

Flags:
  -h, --help   help for list

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -d, --manifests string        Specify a path to a directory of charts and profiles
                                (e.g. ~/Downloads/istio-1.14.1/manifests)
                                or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                
  -n, --namespace string        Config namespace
  -o, --output string           Output format for revision description (available formats: table,json) (default "table")
  -v, --verbose                 Enable verbose output
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```



```
[root@master01 istioctl]# istioctl x revision tag list
TAG  REVISION NAMESPACES
prod default
```



#### remove

```
[root@node01 ~]# istioctl x revision tag remove --help
Remove Istio control plane revision tag.

Removing a revision tag should be done with care. Removing a revision tag will disrupt sidecar injection in namespaces
that reference the tag in an "istio.io/rev" label. Verify that there are no remaining namespaces referencing a
revision tag before removing using the "istioctl tag list" command.

Usage:
  istioctl experimental revision tag remove <revision-tag> [flags]

Aliases:
  remove, delete

Examples:
 # Remove the revision tag "prod"
        istioctl tag remove prod


Flags:
  -h, --help                help for remove
  -y, --skip-confirmation   The skipConfirmation determines whether the user is prompted for confirmation.
                            If set to true, the user is not prompted and a Yes response is assumed in all cases.

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -d, --manifests string        Specify a path to a directory of charts and profiles
                                (e.g. ~/Downloads/istio-1.14.1/manifests)
                                or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                
  -n, --namespace string        Config namespace
  -o, --output string           Output format for revision description (available formats: table,json) (default "table")
  -v, --verbose                 Enable verbose output
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl tag remove prod
Revision tag prod removed
```



#### set

```
[root@node01 ~]# istioctl x revision tag set --help
Create or modify revision tags. Tag an Istio control plane revision for use with namespace istio.io/rev
injection labels.

Usage:
  istioctl experimental revision tag set <revision-tag> [flags]

Examples:
 # Create a revision tag from the "1-8-0" revision
 istioctl tag set prod --revision 1-8-0

 # Point namespace "test-ns" at the revision pointed to by the "prod" revision tag
 kubectl label ns test-ns istio.io/rev=prod

 # Change the revision tag to reference the "1-8-1" revision
 istioctl tag set prod --revision 1-8-1 --overwrite

 # Make revision "1-8-1" the default revision, both resulting in that revision handling injection for "istio-injection=enabled"
 # and validating resources cluster-wide
 istioctl tag set default --revision 1-8-1

 # Rollout namespace "test-ns" to update workloads to the "1-8-1" revision
 kubectl rollout restart deployments -n test-ns


Flags:
      --auto-inject-namespaces   If set to true, the sidecars should be automatically injected into all namespaces by default
  -h, --help                     help for set
      --overwrite                If true, allow revision tags to be overwritten, otherwise reject revision tag updates that
                                 overwrite existing revision tags.
  -r, --revision string          Control plane revision to reference from a given revision tag
  -y, --skip-confirmation        The skipConfirmation determines whether the user is prompted for confirmation.
                                 If set to true, the user is not prompted and a Yes response is assumed in all cases.
      --webhook-name string      Name to use for a revision tag's mutating webhook configuration.

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -d, --manifests string        Specify a path to a directory of charts and profiles
                                (e.g. ~/Downloads/istio-1.14.1/manifests)
                                or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                
  -n, --namespace string        Config namespace
  -o, --output string           Output format for revision description (available formats: table,json) (default "table")
  -v, --verbose                 Enable verbose output
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# kubectl label ns istio-system istio.io/rev=prod
namespace/istio-system labeled

istioctl tag set prod --revision 1-10-0 --overwrite
 kubectl rollout restart deployments -n istio-system

```

```
[root@master01 istioctl]# istioctl tag set prod --revision 1-10-0 --overwrite
Revision tag "prod" created, referencing control plane revision "1-10-0". To enable injection using this
revision tag, use 'kubectl label namespace <NAMESPACE> istio.io/rev=prod'
```



## uninstall

```
[root@node01 ~]# istioctl x uninstall --help
The uninstall command uninstalls Istio from a cluster

Usage:
  istioctl experimental uninstall [flags]

Examples:
  # Uninstall a single control plane by revision
  istioctl x uninstall --revision foo

  # Uninstall a single control plane by iop file
  istioctl x uninstall -f iop.yaml
  
  # Uninstall all control planes and shared resources
  istioctl x uninstall --purge

Flags:
      --dry-run             Console/log output only, make no changes.
  -f, --filename string     The filename of the IstioOperator CR.
      --force               Proceed even with validation errors.
  -h, --help                help for uninstall
  -d, --manifests string    Specify a path to a directory of charts and profiles
                            (e.g. ~/Downloads/istio-1.14.1/manifests)
                            or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                            
      --purge               Delete all Istio related sources for all versions
  -r, --revision string     Target control plane revision for the command.
  -s, --set stringArray     Override an IstioOperator value, e.g. to choose a profile
                            (--set profile=demo), enable or disable components (--set components.cni.enabled=true), or override Istio
                            settings (--set meshConfig.enableTracing=true). See documentation for more info:https://istio.io/v1.14/docs/reference/config/istio.operator.v1alpha1/#IstioOperatorSpec
  -y, --skip-confirmation   The skipConfirmation determines whether the user is prompted for confirmation.
                            If set to true, the user is not prompted and a Yes response is assumed in all cases.
  -v, --verbose             Verbose output.

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl x uninstall --purge
All Istio resources will be pruned from the cluster
Proceed? (y/N) n
Cancelled.
```

```
[root@master01 istioctl]# istioctl x uninstall --set profile=demo --revision=1-10-0
There are still 2 proxies pointing to the control plane revision 1-10-0
istio-ingressgateway-676fbd5fbc-nrrbt.istio-system
istio-egressgateway-5f4cd8b8f7-b6s4l.istio-system
If you proceed with the uninstall, these proxies will become detached from any control plane and will not function correctly.
You are about to remove the following gateways: istio-egressgateway, istio-ingressgateway. To avoid downtime, please quit this command and reinstall the gateway(s) with a revision that is not being removed from the cluster.
Proceed? (y/N) y
  Removed PodDisruptionBudget:istio-system:istio-egressgateway.
  Removed PodDisruptionBudget:istio-system:istio-ingressgateway.
  Removed PodDisruptionBudget:istio-system:istiod-1-10-0.
  Removed Deployment:istio-system:istio-egressgateway.
  Removed Deployment:istio-system:istio-ingressgateway.
  Removed Deployment:istio-system:istiod-1-10-0.
  Removed Service:istio-system:istio-egressgateway.
  Removed Service:istio-system:istio-ingressgateway.
  Removed Service:istio-system:istiod-1-10-0.
  Removed ConfigMap:istio-system:istio-1-10-0.
  Removed ConfigMap:istio-system:istio-sidecar-injector-1-10-0.
  Removed Pod:istio-system:istio-egressgateway-5f4cd8b8f7-b6s4l.
  Removed Pod:istio-system:istio-ingressgateway-676fbd5fbc-nrrbt.
  Removed Pod:istio-system:istiod-1-10-0-c8c99d49f-xdlt6.
  Removed ServiceAccount:istio-system:istio-egressgateway-service-account.
  Removed ServiceAccount:istio-system:istio-ingressgateway-service-account.
  Removed ServiceAccount:istio-system:istiod-1-10-0.
  Removed RoleBinding:istio-system:istio-egressgateway-sds.
  Removed RoleBinding:istio-system:istio-ingressgateway-sds.
  Removed RoleBinding:istio-system:istiod-1-10-0.
  Removed Role:istio-system:istio-egressgateway-sds.
  Removed Role:istio-system:istio-ingressgateway-sds.
  Removed Role:istio-system:istiod-1-10-0.
  Removed EnvoyFilter:istio-system:metadata-exchange-1.10-1-10-0.
  Removed EnvoyFilter:istio-system:metadata-exchange-1.11-1-10-0.
  Removed EnvoyFilter:istio-system:metadata-exchange-1.9-1-10-0.
  Removed EnvoyFilter:istio-system:stats-filter-1.10-1-10-0.
  Removed EnvoyFilter:istio-system:stats-filter-1.11-1-10-0.
  Removed EnvoyFilter:istio-system:stats-filter-1.9-1-10-0.
  Removed EnvoyFilter:istio-system:tcp-metadata-exchange-1.10-1-10-0.
  Removed EnvoyFilter:istio-system:tcp-metadata-exchange-1.11-1-10-0.
  Removed EnvoyFilter:istio-system:tcp-metadata-exchange-1.9-1-10-0.
  Removed EnvoyFilter:istio-system:tcp-stats-filter-1.10-1-10-0.
  Removed EnvoyFilter:istio-system:tcp-stats-filter-1.11-1-10-0.
  Removed EnvoyFilter:istio-system:tcp-stats-filter-1.9-1-10-0.
  Removed MutatingWebhookConfiguration::istio-sidecar-injector-1-10-0.
  Removed ClusterRole::istio-reader-clusterrole-1-10-0-istio-system.
  Removed ClusterRole::istiod-clusterrole-1-10-0-istio-system.
  Removed ClusterRoleBinding::istio-reader-clusterrole-1-10-0-istio-system.
  Removed ClusterRoleBinding::istiod-clusterrole-1-10-0-istio-system.
✔ Uninstall complete              
```

```
[root@master01 istioctl]# istioctl x uninstall -f iop.yaml 
  Removed ClusterRole::istiod-clusterrole-istio-system.
  Removed ClusterRoleBinding::istiod-clusterrole-istio-system.
  Removed ConfigMap:istio-system:istio.
  Removed Deployment:istio-system:istiod.
  Removed ConfigMap:istio-system:istio-sidecar-injector.
  Removed MutatingWebhookConfiguration::istio-sidecar-injector.
  Removed PodDisruptionBudget:istio-system:istiod.
  Removed ClusterRole::istio-reader-clusterrole-istio-system.
  Removed ClusterRoleBinding::istio-reader-clusterrole-istio-system.
  Removed Role:istio-system:istiod.
  Removed RoleBinding:istio-system:istiod.
  Removed Service:istio-system:istiod.
  Removed ServiceAccount:istio-system:istiod.
  Removed EnvoyFilter:istio-system:metadata-exchange-1.10.
  Removed EnvoyFilter:istio-system:tcp-metadata-exchange-1.10.
  Removed EnvoyFilter:istio-system:stats-filter-1.10.
  Removed EnvoyFilter:istio-system:tcp-stats-filter-1.10.
  Removed EnvoyFilter:istio-system:metadata-exchange-1.11.
  Removed EnvoyFilter:istio-system:tcp-metadata-exchange-1.11.
  Removed EnvoyFilter:istio-system:stats-filter-1.11.
  Removed EnvoyFilter:istio-system:tcp-stats-filter-1.11.
  Removed EnvoyFilter:istio-system:metadata-exchange-1.9.
  Removed EnvoyFilter:istio-system:tcp-metadata-exchange-1.9.
  Removed EnvoyFilter:istio-system:stats-filter-1.9.
  Removed EnvoyFilter:istio-system:tcp-stats-filter-1.9.
  Removed ValidatingWebhookConfiguration::istio-validator-istio-system.
✔ Uninstall complete     
```



## version

```
[root@node01 ~]# istioctl x version --help
Prints out build version information

Usage:
  istioctl experimental version [flags]

Examples:
# Retrieve version information directly from the control plane, using token security
# (This is the usual way to get the control plane version with an out-of-cluster control plane.)
istioctl x version --xds-address istio.cloudprovider.example.com:15012

# Retrieve version information via Kubernetes config, using token security
# (This is the usual way to get the control plane version with an in-cluster control plane.)
istioctl x version

# Retrieve version information directly from the control plane, using RSA certificate security
# (Certificates must be obtained before this step.  The --cert-dir flag lets istioctl bypass the Kubernetes API server.)
istioctl x version --xds-address istio.example.com:15012 --cert-dir ~/.istio-certs

# Retrieve version information via XDS from specific control plane in multi-control plane in-cluster configuration
# (Select a specific control plane in an in-cluster canary Istio configuration.)
istioctl x version --xds-label istio.io/rev=default


Flags:
      --authority string     XDS Subject Alternative Name (for example istiod.istio-system.svc)
      --cert-dir string      XDS Endpoint certificate directory
  -h, --help                 help for version
      --insecure             Skip server certificate and domain verification. (NOT SECURE!)
  -o, --output string        One of 'yaml' or 'json'.
      --plaintext            Use plain-text HTTP/2 when connecting to server (no TLS).
      --remote               Use --remote=false to suppress control plane check
  -r, --revision string      Control plane revision
  -s, --short                Use --short=false to generate full version information
      --timeout duration     The duration to wait before failing (default 30s)
      --xds-address string   XDS Endpoint
      --xds-label string     Istiod pod label selector
      --xds-port int         Istiod pod port (default 15012)

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 ~]# istioctl x version
client version: 1.14.1
control plane version: 1.14.1
data plane version: none

istioctl x version  --authority istiod.istio-system.svc
istioctl x version --xds-address 10.68.55.160:15012
istioctl x version --xds-address 10.68.55.160:15012  --insecure

istioctl x version    --plaintext
 istioctl x version  --revision default
 
 istioctl x version  --revision default --timeout 1s
 
  istioctl x version   --xds-label app=istiod  --xds-port 15012 
```



## wait

```
[root@node01 ~]# istioctl x wait --help
Waits for the specified condition to be true of an Istio resource.

Usage:
  istioctl experimental wait [flags] <type> <name>[.<namespace>]

Examples:
  # Wait until the bookinfo virtual service has been distributed to all proxies in the mesh
  istioctl experimental wait --for=distribution virtualservice bookinfo.default

  # Wait until 99% of the proxies receive the distribution, timing out after 5 minutes
  istioctl experimental wait --for=distribution --threshold=.99 --timeout=300 virtualservice bookinfo.default


Flags:
      --for string          Wait condition, must be 'distribution' or 'delete' (default "distribution")
      --generation string   Wait for a specific generation of config to become current, rather than using whatever is latest in Kubernetes
  -h, --help                help for wait
  -r, --revision string     Control plane revision
      --threshold float32   The ratio of distribution required for success (default 1)
      --timeout duration    The duration to wait before failing (default 30s)

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl experimental wait --for=distribution virtualservice bookinfo.istio
Resource networking.istio.io/v1alpha3/VirtualService/istio/bookinfo present on 45 out of 45 configurations for totally 15 sidecars
```

```
[root@master01 istioctl]# istioctl experimental wait --for=delete virtualservice bookinfo.istio
Error: wait for delete is not yet implemented
```



## workload

```
[root@node01 ~]# istioctl x workload --help
Commands to assist in configuring and deploying workloads running on VMs and other non-Kubernetes environments

Usage:
  istioctl experimental workload [command]

Examples:
  # workload group yaml generation
  workload group create

  # workload entry configuration generation
  workload entry configure

Available Commands:
  entry       Commands dealing with WorkloadEntry resources
  group       Commands dealing with WorkloadGroup resources

Flags:
  -h, --help   help for workload

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl experimental workload [command] --help" for more information about a command.
```

### entry

```
[root@node01 ~]# istioctl x workload entry --help
Commands dealing with WorkloadEntry resources

Usage:
  istioctl experimental workload entry [command]

Examples:
entry configure -f workloadgroup.yaml -o outputDir

Available Commands:
  configure   Generates all the required configuration files for a workload instance running on a VM or non-Kubernetes environment

Flags:
  -h, --help   help for entry

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl experimental workload entry [command] --help" for more information about a command.
```

#### configure

```
[root@node01 ~]# istioctl x workload entry configure --help
Generates all the required configuration files for workload instance on a VM or non-Kubernetes environment from a WorkloadGroup artifact.
This includes a MeshConfig resource, the cluster.env file, and necessary certificates and security tokens.
Configure requires either the WorkloadGroup artifact path or its location on the API server.

Usage:
  istioctl experimental workload entry configure [flags]

Examples:
  # configure example using a local WorkloadGroup artifact
  configure -f workloadgroup.yaml -o config

  # configure example using the API server
  configure --name foo --namespace bar -o config

Flags:
      --autoregister            Creates a WorkloadEntry upon connection to istiod (if enabled in pilot).
      --capture-dns             Enables the capture of outgoing DNS packets on port 53, redirecting to istio-agent (default true)
      --clusterID string        The ID used to identify the cluster
      --externalIP string       External IP address of the workload
  -f, --file string             filename of the WorkloadGroup artifact. Leave this field empty if using the API server
  -h, --help                    help for configure
      --ingressIP string        IP address of the ingress gateway
      --ingressService string   Name of the Service to be used as the ingress gateway, in the format <service>.<namespace>. If no namespace is provided, the default istio-system namespace will be used. (default "istio-eastwestgateway")
      --internalIP string       Internal IP address of the workload
      --name string             The name of the workload group
  -o, --output string           Output directory for generated files
  -r, --revision string         Control plane revision
      --tokenDuration int       The token duration in seconds (default: 1 hour) (default 3600)

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl x workload entry configure -f wlg-labels.yaml -o config
Warning: a security token for namespace "vm-mongodb" and service account "default" has been generated and stored at "config/istio-token"
configuration generation into directory config was successful
```

```
istioctl x workload entry configure --name mongodb-2 --namespace vm-mongodb -o config

istioctl x workload entry configure -f wlg-labels.yaml --autoregister  -o config 

istioctl x workload entry configure -f wlg-labels.yaml --autoregister   --clusterID cluster1 -o config 

istioctl x workload entry configure -f wlg-labels.yaml --autoregister  -o config  --externalIP 192.168.299.188

istioctl x workload entry configure -f wlg-labels.yaml  -o config  --ingressIP 172.20.1.205

istioctl x workload entry configure -f wlg-labels.yaml  -o config --ingressService istio-eastwestgateway   --ingressIP 172.20.1.205

istioctl x workload entry configure -f wlg-labels.yaml --autoregister  -o config --tokenDuration 600

istioctl x workload entry configure -f wlg-labels.yaml --autoregister  -o config  --internalIP 192.168.229.188
```



### group

```
[root@node01 ~]# istioctl x workload group --help
Commands dealing with WorkloadGroup resources

Usage:
  istioctl experimental workload group [command]

Examples:
group create --name foo --namespace bar --labels app=foobar

Available Commands:
  create      Creates a WorkloadGroup resource that provides a template for associated WorkloadEntries

Flags:
  -h, --help   help for group

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl experimental workload group [command] --help" for more information about a command.
```



#### create

```
[root@node01 ~]# istioctl x workload group create --help
Creates a WorkloadGroup resource that provides a template for associated WorkloadEntries.
The default output is serialized YAML, which can be piped into 'kubectl apply -f -' to send the artifact to the API Server.

Usage:
  istioctl experimental workload group create [flags]

Examples:
create --name foo --namespace bar --labels app=foo,bar=baz --ports grpc=3550,http=8080 --annotations annotation=foobar --serviceAccount sa

Flags:
  -a, --annotations strings     The annotations to apply to the workload instances
  -h, --help                    help for create
  -l, --labels strings          The labels to apply to the workload instances; e.g. -l env=prod,vers=2
      --name string             The name of the workload group
  -p, --ports strings           The incoming ports exposed by the workload instance
  -s, --serviceAccount string   The service identity to associate with the workload instances (default "default")

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```



```
[root@master01 istioctl]# istioctl x workload group create --name foo --namespace bar --labels app=foo,bar=baz --ports grpc=3550,http=8080 --annotations annotation=foobar --serviceAccount sa
apiVersion: networking.istio.io/v1alpha3
kind: WorkloadGroup
metadata:
  name: foo
  namespace: bar
spec:
  metadata:
    annotations:
      annotation: foobar
    labels:
      app: foo
      bar: baz
  template:
    ports:
      grpc: 3550
      http: 8080
    serviceAccount: sa
```



# install

```
[root@node01 ~]# istioctl install --help
The install command generates an Istio install manifest and applies it to a cluster.

Usage:
  istioctl install [flags]

Aliases:
  install, apply

Examples:
  # Apply a default Istio installation
  istioctl install

  # Enable Tracing
  istioctl install --set meshConfig.enableTracing=true

  # Generate the demo profile and don't wait for confirmation
  istioctl install --set profile=demo --skip-confirmation

  # To override a setting that includes dots, escape them with a backslash (\).  Your shell may require enclosing quotes.
  istioctl install --set "values.sidecarInjectorWebhook.injectedAnnotations.container\.apparmor\.security\.beta\.kubernetes\.io/istio-proxy=runtime/default"

  # For setting boolean-string option, it should be enclosed quotes and escaped with a backslash (\).
  istioctl install --set meshConfig.defaultConfig.proxyMetadata.PROXY_XDS_VIA_AGENT=\"false\"


Flags:
      --dry-run                      Console/log output only, make no changes.
  -f, --filename strings             Path to file containing IstioOperator custom resource
                                     This flag can be specified multiple times to overlay multiple files. Multiple files are overlaid in left to right order.
      --force                        Proceed even with validation errors.
  -h, --help                         help for install
  -d, --manifests string             Specify a path to a directory of charts and profiles
                                     (e.g. ~/Downloads/istio-1.14.1/manifests)
                                     or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                     
      --readiness-timeout duration   Maximum time to wait for Istio resources in each component to be ready. (default 5m0s)
  -r, --revision string              Target control plane revision for the command.
  -s, --set stringArray              Override an IstioOperator value, e.g. to choose a profile
                                     (--set profile=demo), enable or disable components (--set components.cni.enabled=true), or override Istio
                                     settings (--set meshConfig.enableTracing=true). See documentation for more info:https://istio.io/v1.14/docs/reference/config/istio.operator.v1alpha1/#IstioOperatorSpec
  -y, --skip-confirmation            The skipConfirmation determines whether the user is prompted for confirmation.
                                     If set to true, the user is not prompted and a Yes response is assumed in all cases.
      --verify                       Verify the Istio control plane after installation/in-place upgrade

Global Flags:
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl install --set profile=demo 
This will install the Istio 1.11.0 demo profile with ["Istio core" "Istiod" "Ingress gateways" "Egress gateways"] components into the cluster. Proceed? (y/N) y
✔ Istio core installed                                                                                                                
✔ Istiod installed                                                                                                                    
✔ Egress gateways installed                                                                                                           
✔ Ingress gateways installed                                                                                                          
- Pruning removed resources                                                                                                             Removed HorizontalPodAutoscaler:istio-system:istio-eastwestgateway.
  Removed PodDisruptionBudget:istio-system:istio-eastwestgateway.
  Removed Deployment:istio-system:istio-eastwestgateway.
  Removed Service:istio-system:istio-eastwestgateway.
  Removed ServiceAccount:istio-system:istio-eastwestgateway-service-account.
  Removed RoleBinding:istio-system:istio-eastwestgateway-sds.
  Removed Role:istio-system:istio-eastwestgateway-sds.
✔ Installation complete                                                                                                               
Thank you for installing Istio 1.11.  Please take a few minutes to tell us about your install/upgrade experience!  https://forms.gle/kWULBRjUv7hHci7T6
```

```
[root@master01 istioctl]# istioctl install --set profile=demo  --revision=1-14-1 --manifests=/root/istio-1.14.1/manifests
This will install the Istio 1.11.0 demo profile with ["Istio core" "Istiod" "Ingress gateways" "Egress gateways"] components into the cluster. Proceed? (y/N) y
✔ Istio core installed                                                                                                                
2021-08-27T07:55:42.526367Z     error   installer       failed to update resource with server-side apply for obj EnvoyFilter/istio-system/metadata-exchange-1.10-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
2021-08-27T07:55:42.866863Z     error   installer       failed to update resource with server-side apply for obj EnvoyFilter/istio-system/metadata-exchange-1.9-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
2021-08-27T07:55:43.239907Z     error   installer       failed to update resource with server-side apply for obj EnvoyFilter/istio-system/stats-filter-1.10-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
2021-08-27T07:55:43.758992Z     error   installer       failed to update resource with server-side apply for obj EnvoyFilter/istio-system/stats-filter-1.9-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
2021-08-27T07:55:44.583075Z     error   installer       failed to update resource with server-side apply for obj EnvoyFilter/istio-system/tcp-metadata-exchange-1.10-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
2021-08-27T07:55:45.116061Z     error   installer       failed to update resource with server-side apply for obj EnvoyFilter/istio-system/tcp-metadata-exchange-1.9-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
2021-08-27T07:55:45.657669Z     error   installer       failed to update resource with server-side apply for obj EnvoyFilter/istio-system/tcp-stats-filter-1.10-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
2021-08-27T07:55:46.259931Z     error   installer       failed to update resource with server-side apply for obj EnvoyFilter/istio-system/tcp-stats-filter-1.9-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
✘ Istiod encountered an error: failed to update resource with server-side apply for obj EnvoyFilter/istio-system/metadata-exchange-1.10-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
failed to update resource with server-side apply for obj EnvoyFilter/istio-system/metadata-exchange-1.9-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
failed to update resource with server-side apply for obj EnvoyFilter/istio-system/stats-filter-1.10-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
failed to update resource with server-side apply for obj EnvoyFilter/istio-system/stats-filter-1.9-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
failed to update resource with server-side apply for obj EnvoyFilter/istio-system/tcp-metadata-exchange-1.10-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
failed to update resource with server-side apply for obj EnvoyFilter/istio-system/tcp-metadata-exchange-1.9-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
failed to update resource with server-side apply for obj EnvoyFilter/istio-system/tcp-stats-filter-1.10-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
failed to update resource with server-side apply for obj EnvoyFilter/istio-system/tcp-stats-filter-1.9-1-10-0: Internal error occurred: failed calling webhook "rev.validation.istio.io": Post "https://istiod-1-10-0.istio-system.svc:443/validate?timeout=10s": service "istiod-1-10-0" not found
✔ Ingress gateways installed                                                                                                          
✔ Egress gateways installed                                                                                                           
- Pruning removed resources                                                                                                             Removed ValidatingWebhookConfiguration::istio-validator-1-10-0-istio-system.
Error: failed to install manifests: errors occurred during operation
```

```
istioctl install -f istioOperator.yaml
```

```
[root@master01 istioctl]# istioctl install -f iop.yaml 
This will install the Istio 1.11.0 demo profile with ["Istio core" "Istiod" "Ingress gateways" "Egress gateways"] components into the cluster. Proceed? (y/N) y
✔ Istio core installed                                                                                                                
✔ Istiod installed                                                                                                                    
✔ Egress gateways installed                                                                                                           
✔ Ingress gateways installed                                                                                                          
✔ Installation complete                                                                                                               
Thank you for installing Istio 1.11.  Please take a few minutes to tell us about your install/upgrade experience!  https://forms.gle/kWULBRjUv7hHci7T6
```

```
istioctl install --set profile=demo  --manifests /root/istio-1.14.1/manifests

istioctl install --set profile=demo --readiness-timeout 600s

istioctl install --set profile=demo -y

istioctl install --set profile=demo  --force --verify
```





# kube-inject

```
[root@node01 ~]# istioctl kube-inject --help

kube-inject manually injects the Istio sidecar into Kubernetes
workloads. Unsupported resources are left unmodified so it is safe to
run kube-inject over a single file that contains multiple Service,
ConfigMap, Deployment, etc. definitions for a complex application. When in
doubt re-run istioctl kube-inject on deployments to get the most up-to-date changes.

It's best to do kube-inject when the resource is initially created.

Usage:
  istioctl kube-inject [flags]

Examples:
  # Update resources on the fly before applying.
  kubectl apply -f <(istioctl kube-inject -f <resource.yaml>)

  # Create a persistent version of the deployment with Istio sidecar injected.
  istioctl kube-inject -f deployment.yaml -o deployment-injected.yaml

  # Update an existing deployment.
  kubectl get deployment -o yaml | istioctl kube-inject -f - | kubectl apply -f -

  # Capture cluster configuration for later use with kube-inject
  kubectl -n istio-system get cm istio-sidecar-injector  -o jsonpath="{.data.config}" > /tmp/inj-template.tmpl
  kubectl -n istio-system get cm istio -o jsonpath="{.data.mesh}" > /tmp/mesh.yaml
  kubectl -n istio-system get cm istio-sidecar-injector -o jsonpath="{.data.values}" > /tmp/values.json

  # Use kube-inject based on captured configuration
  istioctl kube-inject -f samples/bookinfo/platform/kube/bookinfo.yaml \
    --injectConfigFile /tmp/inj-template.tmpl \
    --meshConfigFile /tmp/mesh.yaml \
    --valuesFile /tmp/values.json


Flags:
      --authority string           XDS Subject Alternative Name (for example istiod.istio-system.svc)
      --cert-dir string            XDS Endpoint certificate directory
  -f, --filename string            Input Kubernetes resource filename
  -h, --help                       help for kube-inject
      --injectConfigFile string    Injection configuration filename. Cannot be used with --injectConfigMapName
      --insecure                   Skip server certificate and domain verification. (NOT SECURE!)
      --meshConfigFile string      Mesh configuration filename. Takes precedence over --meshConfigMapName if set
      --meshConfigMapName string   ConfigMap name for Istio mesh configuration, key should be "mesh" (default "istio")
      --operatorFileName string    Path to file containing IstioOperator custom resources. If configs from files like meshConfigFile, valuesFile are provided, they will be overridden by iop config values.
  -o, --output string              Modified output Kubernetes resource filename
      --plaintext                  Use plain-text HTTP/2 when connecting to server (no TLS).
  -r, --revision string            Control plane revision
      --timeout duration           The duration to wait before failing (default 30s)
      --valuesFile string          Injection values configuration filename.
      --webhookConfig string       MutatingWebhookConfiguration name for Istio (default "istio-sidecar-injector")
      --xds-address string         XDS Endpoint
      --xds-label string           Istiod pod label selector
      --xds-port int               Istiod pod port (default 15012)

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl kube-inject -f productpage-deploy.yaml  --revision=1-14-1
apiVersion: v1
kind: Service
metadata:
  name: productpage
  labels:
    app: productpage
    service: productpage
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: bookinfo-productpage
  labels:
    account: productpage
---
apiVersion: apps/v1
kind: Deployment
metadata:
  creationTimestamp: null
  labels:
    app: productpage
    version: v1
  name: productpage-v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: productpage
      version: v1
  strategy: {}
  template:
    metadata:
      annotations:
        kubectl.kubernetes.io/default-container: productpage
        kubectl.kubernetes.io/default-logs-container: productpage
        prometheus.io/path: /stats/prometheus
        prometheus.io/port: "15020"
        prometheus.io/scrape: "true"
        sidecar.istio.io/status: '{"initContainers":["istio-init"],"containers":["istio-proxy"],"volumes":["istio-envoy","istio-data","istio-podinfo","istio-token","istiod-ca-cert"],"imagePullSecrets":null,"revision":"1-10-0"}'
      creationTimestamp: null
      labels:
        app: productpage
        istio.io/rev: 1-10-0
        security.istio.io/tlsMode: istio
        service.istio.io/canonical-name: productpage
        service.istio.io/canonical-revision: v1
        version: v1
    spec:
      containers:
      - image: docker.io/istio/examples-bookinfo-productpage-v1:1.16.2
        imagePullPolicy: IfNotPresent
        name: productpage
        ports:
        - containerPort: 9080
        resources: {}
        securityContext:
          runAsUser: 1000
        volumeMounts:
        - mountPath: /tmp
          name: tmp
      - args:
        - proxy
        - sidecar
        - --domain
        - $(POD_NAMESPACE).svc.cluster.local
        - --serviceCluster
        - productpage.$(POD_NAMESPACE)
        - --proxyLogLevel=warning
        - --proxyComponentLogLevel=misc:error
        - --log_output_level=default:info
        - --concurrency
        - "2"
        env:
        - name: JWT_POLICY
          value: third-party-jwt
        - name: PILOT_CERT_PROVIDER
          value: istiod
        - name: CA_ADDR
          value: istiod-1-10-0.istio-system.svc:15012
        - name: POD_NAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: POD_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: INSTANCE_IP
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
        - name: SERVICE_ACCOUNT
          valueFrom:
            fieldRef:
              fieldPath: spec.serviceAccountName
        - name: HOST_IP
          valueFrom:
            fieldRef:
              fieldPath: status.hostIP
        - name: CANONICAL_SERVICE
          valueFrom:
            fieldRef:
              fieldPath: metadata.labels['service.istio.io/canonical-name']
        - name: CANONICAL_REVISION
          valueFrom:
            fieldRef:
              fieldPath: metadata.labels['service.istio.io/canonical-revision']
        - name: PROXY_CONFIG
          value: |
            {"discoveryAddress":"istiod-1-10-0.istio-system.svc:15012"}
        - name: ISTIO_META_POD_PORTS
          value: |-
            [
                {"containerPort":9080}
            ]
        - name: ISTIO_META_APP_CONTAINERS
          value: productpage
        - name: ISTIO_META_CLUSTER_ID
          value: Kubernetes
        - name: ISTIO_META_INTERCEPTION_MODE
          value: REDIRECT
        - name: ISTIO_META_MESH_ID
          value: cluster.local
        - name: TRUST_DOMAIN
          value: cluster.local
        image: docker.io/istio/proxyv2:1.11.0
        name: istio-proxy
        ports:
        - containerPort: 15090
          name: http-envoy-prom
          protocol: TCP
        readinessProbe:
          failureThreshold: 30
          httpGet:
            path: /healthz/ready
            port: 15021
          initialDelaySeconds: 1
          periodSeconds: 2
          timeoutSeconds: 3
        resources:
          limits:
            cpu: "2"
            memory: 1Gi
          requests:
            cpu: 10m
            memory: 40Mi
        securityContext:
          allowPrivilegeEscalation: false
          capabilities:
            drop:
            - ALL
          privileged: false
          readOnlyRootFilesystem: true
          runAsGroup: 1337
          runAsNonRoot: true
          runAsUser: 1337
        volumeMounts:
        - mountPath: /var/run/secrets/istio
          name: istiod-ca-cert
        - mountPath: /var/lib/istio/data
          name: istio-data
        - mountPath: /etc/istio/proxy
          name: istio-envoy
        - mountPath: /var/run/secrets/tokens
          name: istio-token
        - mountPath: /etc/istio/pod
          name: istio-podinfo
      initContainers:
      - args:
        - istio-iptables
        - -p
        - "15001"
        - -z
        - "15006"
        - -u
        - "1337"
        - -m
        - REDIRECT
        - -i
        - '*'
        - -x
        - ""
        - -b
        - '*'
        - -d
        - 15090,15021,15020
        image: docker.io/istio/proxyv2:1.11.0
        name: istio-init
        resources:
          limits:
            cpu: "2"
            memory: 1Gi
          requests:
            cpu: 10m
            memory: 40Mi
        securityContext:
          allowPrivilegeEscalation: false
          capabilities:
            add:
            - NET_ADMIN
            - NET_RAW
            drop:
            - ALL
          privileged: false
          readOnlyRootFilesystem: false
          runAsGroup: 0
          runAsNonRoot: false
          runAsUser: 0
      serviceAccountName: bookinfo-productpage
      volumes:
      - emptyDir:
          medium: Memory
        name: istio-envoy
      - emptyDir: {}
        name: istio-data
      - downwardAPI:
          items:
          - fieldRef:
              fieldPath: metadata.labels
            path: labels
          - fieldRef:
              fieldPath: metadata.annotations
            path: annotations
          - path: cpu-limit
            resourceFieldRef:
              containerName: istio-proxy
              divisor: 1m
              resource: limits.cpu
          - path: cpu-request
            resourceFieldRef:
              containerName: istio-proxy
              divisor: 1m
              resource: requests.cpu
        name: istio-podinfo
      - name: istio-token
        projected:
          sources:
          - serviceAccountToken:
              audience: istio-ca
              expirationSeconds: 43200
              path: istio-token
      - configMap:
          name: istio-ca-root-cert
        name: istiod-ca-cert
      - emptyDir: {}
        name: tmp
status: {}
---
```

```
 kubectl -n istio-system get cm istio-sidecar-injector  -o jsonpath="{.data.config}" > ./inj-template.tmpl
 kubectl -n istio-system get cm istio -o jsonpath="{.data.mesh}" > ./mesh.yaml
 kubectl -n istio-system get cm istio-sidecar-injector -o jsonpath="{.data.values}" > ./values.json
 
 istioctl kube-inject -f productpage-deploy.yaml \
    --injectConfigFile ./inj-template.tmpl \
    --meshConfigFile ./mesh.yaml \
    --valuesFile ./values.json
    
apiVersion: v1
kind: Service
metadata:
  name: productpage
  labels:
    app: productpage
    service: productpage
spec:
  ports:
  - port: 9080
    name: http
  selector:
    app: productpage
---
apiVersion: v1
kind: ServiceAccount
metadata:
  name: bookinfo-productpage
  labels:
    account: productpage
---
apiVersion: apps/v1
kind: Deployment
metadata:
  creationTimestamp: null
  labels:
    app: productpage
    version: v1
  name: productpage-v1
spec:
  replicas: 1
  selector:
    matchLabels:
      app: productpage
      version: v1
  strategy: {}
  template:
    metadata:
      annotations:
        kubectl.kubernetes.io/default-container: productpage
        kubectl.kubernetes.io/default-logs-container: productpage
        prometheus.io/path: /stats/prometheus
        prometheus.io/port: "15020"
        prometheus.io/scrape: "true"
        sidecar.istio.io/status: '{"initContainers":["istio-init"],"containers":["istio-proxy"],"volumes":["istio-envoy","istio-data","istio-podinfo","istio-token","istiod-ca-cert"],"imagePullSecrets":null,"revision":"default"}'
      creationTimestamp: null
      labels:
        app: productpage
        security.istio.io/tlsMode: istio
        service.istio.io/canonical-name: productpage
        service.istio.io/canonical-revision: v1
        version: v1
    spec:
      containers:
      - image: docker.io/istio/examples-bookinfo-productpage-v1:1.16.2
        imagePullPolicy: IfNotPresent
        name: productpage
        ports:
        - containerPort: 9080
        resources: {}
        securityContext:
          runAsUser: 1000
        volumeMounts:
        - mountPath: /tmp
          name: tmp
      - args:
        - proxy
        - sidecar
        - --domain
        - $(POD_NAMESPACE).svc.cluster.local
        - --proxyLogLevel=warning
        - --proxyComponentLogLevel=misc:error
        - --log_output_level=default:info
        - --concurrency
        - "2"
        env:
        - name: JWT_POLICY
          value: third-party-jwt
        - name: PILOT_CERT_PROVIDER
          value: istiod
        - name: CA_ADDR
          value: istiod.istio-system.svc:15012
        - name: POD_NAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: POD_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: INSTANCE_IP
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
        - name: SERVICE_ACCOUNT
          valueFrom:
            fieldRef:
              fieldPath: spec.serviceAccountName
        - name: HOST_IP
          valueFrom:
            fieldRef:
              fieldPath: status.hostIP
        - name: PROXY_CONFIG
          value: |
            {}
        - name: ISTIO_META_POD_PORTS
          value: |-
            [
                {"containerPort":9080}
            ]
        - name: ISTIO_META_APP_CONTAINERS
          value: productpage
        - name: ISTIO_META_CLUSTER_ID
          value: Kubernetes
        - name: ISTIO_META_INTERCEPTION_MODE
          value: REDIRECT
        - name: ISTIO_META_WORKLOAD_NAME
          value: productpage-v1
        - name: ISTIO_META_OWNER
          value: kubernetes://apis/apps/v1/namespaces/default/deployments/productpage-v1
        - name: ISTIO_META_MESH_ID
          value: cluster.local
        - name: TRUST_DOMAIN
          value: cluster.local
        image: docker.io/istio/proxyv2:1.11.0
        name: istio-proxy
        ports:
        - containerPort: 15090
          name: http-envoy-prom
          protocol: TCP
        readinessProbe:
          failureThreshold: 30
          httpGet:
            path: /healthz/ready
            port: 15021
          initialDelaySeconds: 1
          periodSeconds: 2
          timeoutSeconds: 3
        resources:
          limits:
            cpu: "2"
            memory: 1Gi
          requests:
            cpu: 10m
            memory: 40Mi
        securityContext:
          allowPrivilegeEscalation: false
          capabilities:
            drop:
            - ALL
          privileged: false
          readOnlyRootFilesystem: true
          runAsGroup: 1337
          runAsNonRoot: true
          runAsUser: 1337
        volumeMounts:
        - mountPath: /var/run/secrets/istio
          name: istiod-ca-cert
        - mountPath: /var/lib/istio/data
          name: istio-data
        - mountPath: /etc/istio/proxy
          name: istio-envoy
        - mountPath: /var/run/secrets/tokens
          name: istio-token
        - mountPath: /etc/istio/pod
          name: istio-podinfo
      initContainers:
      - args:
        - istio-iptables
        - -p
        - "15001"
        - -z
        - "15006"
        - -u
        - "1337"
        - -m
        - REDIRECT
        - -i
        - '*'
        - -x
        - ""
        - -b
        - '*'
        - -d
        - 15090,15021,15020
        image: docker.io/istio/proxyv2:1.11.0
        name: istio-init
        resources:
          limits:
            cpu: "2"
            memory: 1Gi
          requests:
            cpu: 10m
            memory: 40Mi
        securityContext:
          allowPrivilegeEscalation: false
          capabilities:
            add:
            - NET_ADMIN
            - NET_RAW
            drop:
            - ALL
          privileged: false
          readOnlyRootFilesystem: false
          runAsGroup: 0
          runAsNonRoot: false
          runAsUser: 0
      securityContext:
        fsGroup: 1337
      serviceAccountName: bookinfo-productpage
      volumes:
      - emptyDir:
          medium: Memory
        name: istio-envoy
      - emptyDir: {}
        name: istio-data
      - downwardAPI:
          items:
          - fieldRef:
              fieldPath: metadata.labels
            path: labels
          - fieldRef:
              fieldPath: metadata.annotations
            path: annotations
        name: istio-podinfo
      - name: istio-token
        projected:
          sources:
          - serviceAccountToken:
              audience: istio-ca
              expirationSeconds: 43200
              path: istio-token
      - configMap:
          name: istio-ca-root-cert
        name: istiod-ca-cert
      - emptyDir: {}
        name: tmp
status: {}
---
 
```

```
 istioctl kube-inject -f productpage-deploy.yaml  --authority istiod.istio-system.svc --meshConfigMapName istio   --injectConfigMapName istio-sidecar-injector --plaintext --timeout 30s  --webhookConfig istio-sidecar-injector
 
  istioctl kube-inject -f productpage-deploy.yaml --xds-address 10.68.55.160:15012 --xds-label app=istiod --xds-port 15012
```



# manifest

```
[root@node01 ~]# istioctl manifest --help
The manifest command generates and diffs Istio manifests.

Usage:
  istioctl manifest [command]

Available Commands:
  diff        Compare manifests and generate diff
  generate    Generates an Istio install manifest
  install     Applies an Istio manifest, installing or reconfiguring Istio on a cluster.

Flags:
      --dry-run   Console/log output only, make no changes.
  -h, --help      help for manifest

Global Flags:
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl manifest [command] --help" for more information about a command.
```

## diff

```
[root@node01 ~]# istioctl manifest diff --help
The diff subcommand compares manifests from two files or directories. The output is a list of
changed paths with the value changes shown as OLD-VALUE -> NEW-VALUE.
List order changes are shown as [OLD-INDEX->NEW-INDEX], with ? used where a list item is added or
removed.

Usage:
  istioctl manifest diff <file|dir> <file|dir> [flags]

Flags:
  -r, --directory       Compare directory.
  -h, --help            help for diff
      --ignore string   Ignore all listed items during comparison, using the same list format as selectResources.
      --rename string   Rename resources before comparison.
                        The format of each renaming pair is A->B, all renaming pairs are comma separated.
                        e.g. Service:*:istiod->Service:*:istio-control - rename istiod service into istio-control
      --select string   Constrain the list of resources to compare to only the ones in this list, ignoring all others.
                        The format of each list item is "::" and the items are comma separated. The "*" character represents wildcard selection.
                        e.g.
                            Deployment:istio-system:* - compare all deployments in istio-system namespace
                            Service:*:istiod - compare Services called "istiod" in all namespaces (default "::")
  -v, --verbose         Verbose output.

Global Flags:
      --context string      The name of the kubeconfig context to use
      --dry-run             Console/log output only, make no changes.
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl manifest diff productpage-deploy.yaml productpage-deploy-inject.yaml 
Differences in manifests are:


Object Deployment::productpage-v1 has diffs:

spec:
  template:
    metadata:
      annotations: <empty> -> map[kubectl.kubernetes.io/default-container:productpage
        kubectl.kubernetes.io/default-logs-container:productpage prometheus.io/path:/stats/prometheus
        prometheus.io/port:15020 prometheus.io/scrape:true sidecar.istio.io/status:{"initContainers":["istio-init"],"containers":["istio-proxy"],"volumes":["istio-envoy","istio-data","istio-podinfo","istio-token","istiod-ca-cert"],"imagePullSecrets":null,"revision":"default"}]
        (ADDED)
      labels:
        security.istio.io/tlsMode: <empty> -> istio (ADDED)
        service.istio.io/canonical-name: <empty> -> productpage (ADDED)
        service.istio.io/canonical-revision: <empty> -> v1 (ADDED)
    spec:
      containers:
        '[?->1]': |-
          <empty> -> map[args:[proxy sidecar --domain $(POD_NAMESPACE).svc.cluster.local --proxyLogLevel=warning --proxyComponentLogLevel=misc:error --log_output_level=default:info --concurrency 2] env:[map[name:JWT_POLICY value:third-party-jwt] map[name:PILOT_CERT_PROVIDER value:istiod] map[name:CA_ADDR value:istiod.istio-system.svc:15012] map[name:POD_NAME valueFrom:map[fieldRef:map[fieldPath:metadata.name]]] map[name:POD_NAMESPACE valueFrom:map[fieldRef:map[fieldPath:metadata.namespace]]] map[name:INSTANCE_IP valueFrom:map[fieldRef:map[fieldPath:status.podIP]]] map[name:SERVICE_ACCOUNT valueFrom:map[fieldRef:map[fieldPath:spec.serviceAccountName]]] map[name:HOST_IP valueFrom:map[fieldRef:map[fieldPath:status.hostIP]]] map[name:PROXY_CONFIG value:{"meshId":"mesh1"}
          ] map[name:ISTIO_META_POD_PORTS value:[
              {"containerPort":9080}
          ]] map[name:ISTIO_META_APP_CONTAINERS value:productpage] map[name:ISTIO_META_INTERCEPTION_MODE value:REDIRECT] map[name:ISTIO_META_MESH_ID value:mesh1] map[name:TRUST_DOMAIN value:cluster.local] map[name:ISTIO_META_CLUSTER_ID value:Kubernetes]] image:docker.io/istio/proxyv2:1.11.0 name:istio-proxy ports:[map[containerPort:15090 name:http-envoy-prom protocol:TCP]] readinessProbe:map[failureThreshold:30 httpGet:map[path:/healthz/ready port:15021] initialDelaySeconds:1 periodSeconds:2 timeoutSeconds:3] resources:map[limits:map[cpu:2 memory:1Gi] requests:map[cpu:10m memory:40Mi]] securityContext:map[allowPrivilegeEscalation:false capabilities:map[drop:[ALL]] privileged:false readOnlyRootFilesystem:true runAsGroup:1337 runAsNonRoot:true runAsUser:1337] volumeMounts:[map[mountPath:/var/run/secrets/istio name:istiod-ca-cert] map[mountPath:/var/lib/istio/data name:istio-data] map[mountPath:/etc/istio/proxy name:istio-envoy] map[mountPath:/var/run/secrets/tokens name:istio-token] map[mountPath:/etc/istio/pod name:istio-podinfo]]] (ADDED)
      initContainers: <empty> -> [map[args:[istio-iptables -p 15001 -z 15006 -u 1337
        -m REDIRECT -i * -x  -b * -d 15090,15021,15020] image:docker.io/istio/proxyv2:1.11.0
        name:istio-init resources:map[limits:map[cpu:2 memory:1Gi] requests:map[cpu:10m
        memory:40Mi]] securityContext:map[allowPrivilegeEscalation:false capabilities:map[add:[NET_ADMIN
        NET_RAW] drop:[ALL]] privileged:false readOnlyRootFilesystem:false runAsGroup:0
        runAsNonRoot:false runAsUser:0]]] (ADDED)
      volumes:
        '[?->0]': <empty> -> map[emptyDir:map[medium:Memory] name:istio-envoy] (ADDED)
        '[?->1]': <empty> -> map[emptyDir:map[] name:istio-data] (ADDED)
        '[?->2]': <empty> -> map[downwardAPI:map[items:[map[fieldRef:map[fieldPath:metadata.labels]
          path:labels] map[fieldRef:map[fieldPath:metadata.annotations] path:annotations]]]
          name:istio-podinfo] (ADDED)
        '[?->3]': <empty> -> map[name:istio-token projected:map[sources:[map[serviceAccountToken:map[audience:istio-ca
          expirationSeconds:43200 path:istio-token]]]]] (ADDED)
        '[?->4]': <empty> -> map[configMap:map[name:istio-ca-root-cert] name:istiod-ca-cert]
          (ADDED)
```

```
istioctl manifest diff --directory  myconfig/ myconfig2
 istioctl manifest diff --directory  myconfig/ myconfig2 --ignore VirtualService::bookinfo
 
  istioctl manifest diff --directory  myconfig/ myconfig2 --select VirtualService::bookinfo
  
istioctl manifest diff productpage-deploy.yaml productpage-deploy-inject.yaml --verbose
```



## generate

```
[root@node01 ~]# istioctl manifest generate --help
The generate subcommand generates an Istio install manifest and outputs to the console by default.

Usage:
  istioctl manifest generate [flags]

Examples:
  # Generate a default Istio installation
  istioctl manifest generate

  # Enable Tracing
  istioctl manifest generate --set meshConfig.enableTracing=true

  # Generate the demo profile
  istioctl manifest generate --set profile=demo

  # To override a setting that includes dots, escape them with a backslash (\).  Your shell may require enclosing quotes.
  istioctl manifest generate --set "values.sidecarInjectorWebhook.injectedAnnotations.container\.apparmor\.security\.beta\.kubernetes\.io/istio-proxy=runtime/default"

  # For setting boolean-string option, it should be enclosed quotes and escaped with a backslash (\).
  istioctl manifest generate --set meshConfig.defaultConfig.proxyMetadata.PROXY_XDS_VIA_AGENT=\"false\"


Flags:
      --component strings   Specify which component to generate manifests for.
  -f, --filename strings    Path to file containing IstioOperator custom resource
                            This flag can be specified multiple times to overlay multiple files. Multiple files are overlaid in left to right order.
      --force               Proceed even with validation errors.
  -h, --help                help for generate
  -d, --manifests string    Specify a path to a directory of charts and profiles
                            (e.g. ~/Downloads/istio-1.14.1/manifests)
                            or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                            
  -o, --output string       Manifest output directory path.
  -r, --revision string     Target control plane revision for the command.
  -s, --set stringArray     Override an IstioOperator value, e.g. to choose a profile
                            (--set profile=demo), enable or disable components (--set components.cni.enabled=true), or override Istio
                            settings (--set meshConfig.enableTracing=true). See documentation for more info:https://istio.io/v1.14/docs/reference/config/istio.operator.v1alpha1/#IstioOperatorSpec

Global Flags:
      --context string      The name of the kubeconfig context to use
      --dry-run             Console/log output only, make no changes.
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

component:

Base Pilot Cni IstiodRemote IngressGateways EgressGateways IstioOperator IstioOperatorCustomResource

```
[root@master01 istioctl]# istioctl manifest generate --component "IngressGateways"
apiVersion: v1
kind: ServiceAccount
metadata:
  name: istio-ingressgateway-service-account
  namespace: istio-system
  labels:
    app: istio-ingressgateway
    istio: ingressgateway
    release: istio
    istio.io/rev: default
    install.operator.istio.io/owning-resource: unknown
    operator.istio.io/component: "IngressGateways"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: istio-ingressgateway
  namespace: istio-system
  labels:
    app: istio-ingressgateway
    istio: ingressgateway
    release: istio
    istio.io/rev: default
    install.operator.istio.io/owning-resource: unknown
    operator.istio.io/component: "IngressGateways"
spec:
  selector:
    matchLabels:
      app: istio-ingressgateway
      istio: ingressgateway
  strategy:
    rollingUpdate:
      maxSurge: 100%
      maxUnavailable: 25%
  template:
    metadata:
      labels:
        app: istio-ingressgateway
        istio: ingressgateway
        heritage: Tiller
        release: istio
        chart: gateways
        service.istio.io/canonical-name: istio-ingressgateway
        service.istio.io/canonical-revision: latest
        istio.io/rev: default
        install.operator.istio.io/owning-resource: unknown
        operator.istio.io/component: "IngressGateways"
        sidecar.istio.io/inject: "false"
      annotations:
        prometheus.io/port: "15020"
        prometheus.io/scrape: "true"
        prometheus.io/path: "/stats/prometheus"
        sidecar.istio.io/inject: "false"
    spec:
      securityContext:
        runAsUser: 1337
        runAsGroup: 1337
        runAsNonRoot: true
        fsGroup: 1337
      serviceAccountName: istio-ingressgateway-service-account
      containers:
        - name: istio-proxy
          image: "docker.io/istio/proxyv2:1.11.0"
          ports:
            - containerPort: 15021
              protocol: TCP
            - containerPort: 8080
              protocol: TCP
            - containerPort: 8443
              protocol: TCP
            - containerPort: 15090
              protocol: TCP
              name: http-envoy-prom
          args:
          - proxy
          - router
          - --domain
          - $(POD_NAMESPACE).svc.cluster.local
          - --proxyLogLevel=warning
          - --proxyComponentLogLevel=misc:error
          - --log_output_level=default:info
          securityContext:
            allowPrivilegeEscalation: false
            capabilities:
              drop:
              - ALL
            privileged: false
            readOnlyRootFilesystem: true
          readinessProbe:
            failureThreshold: 30
            httpGet:
              path: /healthz/ready
              port: 15021
              scheme: HTTP
            initialDelaySeconds: 1
            periodSeconds: 2
            successThreshold: 1
            timeoutSeconds: 1
          resources:
            limits:
              cpu: 2000m
              memory: 1024Mi
            requests:
              cpu: 100m
              memory: 128Mi
          env:
          - name: JWT_POLICY
            value: third-party-jwt
          - name: PILOT_CERT_PROVIDER
            value: istiod
          - name: CA_ADDR
            value: istiod.istio-system.svc:15012
          - name: NODE_NAME
            valueFrom:
              fieldRef:
                apiVersion: v1
                fieldPath: spec.nodeName
          - name: POD_NAME
            valueFrom:
              fieldRef:
                apiVersion: v1
                fieldPath: metadata.name
          - name: POD_NAMESPACE
            valueFrom:
              fieldRef:
                apiVersion: v1
                fieldPath: metadata.namespace
          - name: INSTANCE_IP
            valueFrom:
              fieldRef:
                apiVersion: v1
                fieldPath: status.podIP
          - name: HOST_IP
            valueFrom:
              fieldRef:
                apiVersion: v1
                fieldPath: status.hostIP
          - name: SERVICE_ACCOUNT
            valueFrom:
              fieldRef:
                fieldPath: spec.serviceAccountName
          - name: ISTIO_META_WORKLOAD_NAME
            value: istio-ingressgateway
          - name: ISTIO_META_OWNER
            value: kubernetes://apis/apps/v1/namespaces/istio-system/deployments/istio-ingressgateway
          - name: ISTIO_META_MESH_ID
            value: "cluster.local"
          - name: TRUST_DOMAIN
            value: "cluster.local"
          - name: ISTIO_META_UNPRIVILEGED_POD
            value: "true"
          - name: ISTIO_META_ROUTER_MODE
            value: "standard"
          - name: ISTIO_META_CLUSTER_ID
            value: "Kubernetes"
          volumeMounts:
          - name: istio-envoy
            mountPath: /etc/istio/proxy
          - name: config-volume
            mountPath: /etc/istio/config
          - mountPath: /var/run/secrets/istio
            name: istiod-ca-cert
          - name: istio-token
            mountPath: /var/run/secrets/tokens
            readOnly: true
          - mountPath: /var/lib/istio/data
            name: istio-data
          - name: podinfo
            mountPath: /etc/istio/pod
          - name: ingressgateway-certs
            mountPath: "/etc/istio/ingressgateway-certs"
            readOnly: true
          - name: ingressgateway-ca-certs
            mountPath: "/etc/istio/ingressgateway-ca-certs"
            readOnly: true
      volumes:
      - name: istiod-ca-cert
        configMap:
          name: istio-ca-root-cert
      - name: podinfo
        downwardAPI:
          items:
            - path: "labels"
              fieldRef:
                fieldPath: metadata.labels
            - path: "annotations"
              fieldRef:
                fieldPath: metadata.annotations
      - name: istio-envoy
        emptyDir: {}
      - name: istio-data
        emptyDir: {}
      - name: istio-token
        projected:
          sources:
          - serviceAccountToken:
              path: istio-token
              expirationSeconds: 43200
              audience: istio-ca
      - name: config-volume
        configMap:
          name: istio
          optional: true
      - name: ingressgateway-certs
        secret:
          secretName: "istio-ingressgateway-certs"
          optional: true
      - name: ingressgateway-ca-certs
        secret:
          secretName: "istio-ingressgateway-ca-certs"
          optional: true
      affinity:
        nodeAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
              nodeSelectorTerms:
              - matchExpressions:
                - key: kubernetes.io/arch
                  operator: In
                  values:
                  - "amd64"
                  - "ppc64le"
                  - "s390x"
          preferredDuringSchedulingIgnoredDuringExecution:
            - weight: 2
              preference:
                matchExpressions:
                - key: kubernetes.io/arch
                  operator: In
                  values:
                  - "amd64"
            - weight: 2
              preference:
                matchExpressions:
                - key: kubernetes.io/arch
                  operator: In
                  values:
                  - "ppc64le"
            - weight: 2
              preference:
                matchExpressions:
                - key: kubernetes.io/arch
                  operator: In
                  values:
                  - "s390x"
---
apiVersion: policy/v1beta1
kind: PodDisruptionBudget
metadata:
  name: istio-ingressgateway
  namespace: istio-system
  labels:
    app: istio-ingressgateway
    istio: ingressgateway
    release: istio
    istio.io/rev: default
    install.operator.istio.io/owning-resource: unknown
    operator.istio.io/component: "IngressGateways"
spec:
  minAvailable: 1
  selector:
    matchLabels:
      app: istio-ingressgateway
      istio: ingressgateway
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  name: istio-ingressgateway-sds
  namespace: istio-system
  labels:
    release: istio
    istio.io/rev: default
    install.operator.istio.io/owning-resource: unknown
    operator.istio.io/component: "IngressGateways"
rules:
- apiGroups: [""]
  resources: ["secrets"]
  verbs: ["get", "watch", "list"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: istio-ingressgateway-sds
  namespace: istio-system
  labels:
    release: istio
    istio.io/rev: default
    install.operator.istio.io/owning-resource: unknown
    operator.istio.io/component: "IngressGateways"
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: Role
  name: istio-ingressgateway-sds
subjects:
- kind: ServiceAccount
  name: istio-ingressgateway-service-account
---
apiVersion: autoscaling/v2beta1
kind: HorizontalPodAutoscaler
metadata:
  name: istio-ingressgateway
  namespace: istio-system
  labels:
    app: istio-ingressgateway
    istio: ingressgateway
    release: istio
    istio.io/rev: default
    install.operator.istio.io/owning-resource: unknown
    operator.istio.io/component: "IngressGateways"
spec:
  maxReplicas: 5
  minReplicas: 1
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: istio-ingressgateway
  metrics:
    - type: Resource
      resource:
        name: cpu
        targetAverageUtilization: 80
---
apiVersion: v1
kind: Service
metadata:
  name: istio-ingressgateway
  namespace: istio-system
  annotations:
  labels:
    app: istio-ingressgateway
    istio: ingressgateway
    release: istio
    istio.io/rev: default
    install.operator.istio.io/owning-resource: unknown
    operator.istio.io/component: "IngressGateways"
spec:
  type: LoadBalancer
  selector:
    app: istio-ingressgateway
    istio: ingressgateway
  ports:
    -
      name: status-port
      port: 15021
      protocol: TCP
      targetPort: 15021
    -
      name: http2
      port: 80
      protocol: TCP
      targetPort: 8080
    -
      name: https
      port: 443
      protocol: TCP
      targetPort: 8443
---
```

```
istioctl manifest generate --set profile=demo

istioctl manifest generate --set meshConfig.enableTracing=true
istioctl manifest generate  --filename iop.yaml 

istioctl manifest generate  --filename iop.yaml --manifests /root/istio-1.14.1/manifests/

istioctl manifest generate --force   

istioctl manifest generate --revision default
```



## install

```
[root@node01 ~]# istioctl manifest install --help
The install command generates an Istio install manifest and applies it to a cluster.

Usage:
  istioctl manifest install [flags]

Aliases:
  install, apply

Examples:
  # Apply a default Istio installation
  istioctl install

  # Enable Tracing
  istioctl install --set meshConfig.enableTracing=true

  # Generate the demo profile and don't wait for confirmation
  istioctl install --set profile=demo --skip-confirmation

  # To override a setting that includes dots, escape them with a backslash (\).  Your shell may require enclosing quotes.
  istioctl install --set "values.sidecarInjectorWebhook.injectedAnnotations.container\.apparmor\.security\.beta\.kubernetes\.io/istio-proxy=runtime/default"

  # For setting boolean-string option, it should be enclosed quotes and escaped with a backslash (\).
  istioctl install --set meshConfig.defaultConfig.proxyMetadata.PROXY_XDS_VIA_AGENT=\"false\"


Flags:
  -f, --filename strings             Path to file containing IstioOperator custom resource
                                     This flag can be specified multiple times to overlay multiple files. Multiple files are overlaid in left to right order.
      --force                        Proceed even with validation errors.
  -h, --help                         help for install
  -d, --manifests string             Specify a path to a directory of charts and profiles
                                     (e.g. ~/Downloads/istio-1.14.1/manifests)
                                     or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                     
      --readiness-timeout duration   Maximum time to wait for Istio resources in each component to be ready. (default 5m0s)
  -r, --revision string              Target control plane revision for the command.
  -s, --set stringArray              Override an IstioOperator value, e.g. to choose a profile
                                     (--set profile=demo), enable or disable components (--set components.cni.enabled=true), or override Istio
                                     settings (--set meshConfig.enableTracing=true). See documentation for more info:https://istio.io/v1.14/docs/reference/config/istio.operator.v1alpha1/#IstioOperatorSpec
  -y, --skip-confirmation            The skipConfirmation determines whether the user is prompted for confirmation.
                                     If set to true, the user is not prompted and a Yes response is assumed in all cases.
      --verify                       Verify the Istio control plane after installation/in-place upgrade

Global Flags:
      --context string      The name of the kubeconfig context to use
      --dry-run             Console/log output only, make no changes.
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 ~]# istioctl manifest install --set profile=demo
This will install the Istio 1.14.1 demo profile with ["Istio core" "Istiod" "Ingress gateways" "Egress gateways"] components into the cluster. Proceed? (y/N) n
Cancelled.

 istioctl manifest install --set meshConfig.enableTracing=true
 
  istioctl manifest install --set profile=demo --skip-confirmation
  
 istioctl manifest install   --filename iop.yaml 
 
istioctl manifest install --set profile=demo --manifests /root/istio-1.14.1/manifests/
istioctl manifest install --set profile=demo --readiness-timeout 600s

istioctl manifest install --set profile=demo  --revision 1.14.2
istioctl manifest install --set profile=demo --verify        
```



# operator

```
[root@node01 ~]# istioctl operator --help
The operator command installs, dumps, removes and shows the status of the operator controller.

Usage:
  istioctl operator [command]

Available Commands:
  dump        Dumps the Istio operator controller manifest.
  init        Installs the Istio operator controller in the cluster.
  remove      Removes the Istio operator controller from the cluster.

Flags:
  -h, --help   help for operator

Global Flags:
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl operator [command] --help" for more information about a command.
```

## dump

```
[root@node01 ~]# istioctl operator dump --help
The dump subcommand dumps the Istio operator controller manifest.

Usage:
  istioctl operator dump [flags]

Flags:
      --dry-run                    Console/log output only, make no changes.
  -h, --help                       help for dump
      --hub string                 The hub for the operator controller image. (default "docker.io/istio")
      --imagePullSecrets strings   The imagePullSecrets are used to pull the operator image from the private registry,
                                   could be secret list separated by comma, eg. '--imagePullSecrets imagePullSecret1,imagePullSecret2'
  -d, --manifests string           Specify a path to a directory of charts and profiles
                                   (e.g. ~/Downloads/istio-1.14.1/manifests)
                                   or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                   
      --operatorNamespace string   The namespace the operator controller is installed into. (default "istio-operator")
  -o, --output string              Output format: one of json|yaml (default "yaml")
  -r, --revision string            Target revision for the operator.
      --tag string                 The tag for the operator controller image. (default "1.14.1")
      --watchedNamespaces string   The namespaces the operator controller watches, could be namespace list separated by comma, eg. 'ns1,ns2' (default "istio-system")

Global Flags:
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl operator dump 
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  creationTimestamp: null
  name: istio-operator
rules:
# istio groups
- apiGroups:
  - authentication.istio.io
  resources:
  - '*'
  verbs:
  - '*'
- apiGroups:
  - config.istio.io
  resources:
  - '*'
  verbs:
  - '*'
- apiGroups:
  - install.istio.io
  resources:
  - '*'
  verbs:
  - '*'
- apiGroups:
  - networking.istio.io
  resources:
  - '*'
  verbs:
  - '*'
- apiGroups:
  - security.istio.io
  resources:
  - '*'
  verbs:
  - '*'
# k8s groups
- apiGroups:
  - admissionregistration.k8s.io
  resources:
  - mutatingwebhookconfigurations
  - validatingwebhookconfigurations
  verbs:
  - '*'
- apiGroups:
  - apiextensions.k8s.io
  resources:
  - customresourcedefinitions.apiextensions.k8s.io
  - customresourcedefinitions
  verbs:
  - '*'
- apiGroups:
  - apps
  - extensions
  resources:
  - daemonsets
  - deployments
  - deployments/finalizers
  - replicasets
  verbs:
  - '*'
- apiGroups:
  - autoscaling
  resources:
  - horizontalpodautoscalers
  verbs:
  - '*'
- apiGroups:
  - monitoring.coreos.com
  resources:
  - servicemonitors
  verbs:
  - get
  - create
  - update
- apiGroups:
  - policy
  resources:
  - poddisruptionbudgets
  verbs:
  - '*'
- apiGroups:
  - rbac.authorization.k8s.io
  resources:
  - clusterrolebindings
  - clusterroles
  - roles
  - rolebindings
  verbs:
  - '*'
- apiGroups:
  - coordination.k8s.io
  resources:
  - leases
  verbs:
  - get
  - create
  - update
- apiGroups:
  - ""
  resources:
  - configmaps
  - endpoints
  - events
  - namespaces
  - pods
  - pods/proxy
  - persistentvolumeclaims
  - secrets
  - services
  - serviceaccounts
  verbs:
  - '*'
---
kind: ClusterRoleBinding
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: istio-operator
subjects:
- kind: ServiceAccount
  name: istio-operator
  namespace: istio-operator
roleRef:
  kind: ClusterRole
  name: istio-operator
  apiGroup: rbac.authorization.k8s.io
---


---
apiVersion: apps/v1
kind: Deployment
metadata:
  namespace: istio-operator
  name: istio-operator
spec:
  replicas: 1
  selector:
    matchLabels:
      name: istio-operator
  template:
    metadata:
      labels:
        name: istio-operator
    spec:
      serviceAccountName: istio-operator
      containers:
        - name: istio-operator
          image: docker.io/istio/operator:1.11.0
          command:
          - operator
          - server
          securityContext:
            allowPrivilegeEscalation: false
            capabilities:
              drop:
              - ALL
            privileged: false
            readOnlyRootFilesystem: true
            runAsGroup: 1337
            runAsUser: 1337
            runAsNonRoot: true
          imagePullPolicy: IfNotPresent
          resources:
            limits:
              cpu: 200m
              memory: 256Mi
            requests:
              cpu: 50m
              memory: 128Mi
          env:
            - name: WATCH_NAMESPACE
              value: 
            - name: LEADER_ELECTION_NAMESPACE
              value: "istio-operator"
            - name: POD_NAME
              valueFrom:
                fieldRef:
                  fieldPath: metadata.name
            - name: OPERATOR_NAME
              value: "istio-operator"
            - name: WAIT_FOR_RESOURCES_TIMEOUT
              value: "300s"
            - name: REVISION
              value: ""
---
apiVersion: v1
kind: Namespace
metadata:
  name: istio-operator
  labels:
    istio-operator-managed: Reconcile
    istio-injection: disabled
---
apiVersion: v1
kind: Service
metadata:
  namespace: istio-operator
  labels:
    name: istio-operator
  name: istio-operator
spec:
  ports:
  - name: http-metrics
    port: 8383
    targetPort: 8383
    protocol: TCP
  selector:
    name: istio-operator
---
apiVersion: v1
kind: ServiceAccount
metadata:
  namespace: istio-operator
  name: istio-operator
---
# SYNC WITH manifests/charts/base/files
apiVersion: apiextensions.k8s.io/v1
kind: CustomResourceDefinition
metadata:
  name: istiooperators.install.istio.io
  labels:
    release: istio
spec:
  conversion:
    strategy: None
  group: install.istio.io
  names:
    kind: IstioOperator
    listKind: IstioOperatorList
    plural: istiooperators
    singular: istiooperator
    shortNames:
    - iop
    - io
  scope: Namespaced
  versions:
  - additionalPrinterColumns:
    - description: Istio control plane revision
      jsonPath: .spec.revision
      name: Revision
      type: string
    - description: IOP current state
      jsonPath: .status.status
      name: Status
      type: string
    - description: 'CreationTimestamp is a timestamp representing the server time
        when this object was created. It is not guaranteed to be set in happens-before
        order across separate operations. Clients may not set this value. It is represented
        in RFC3339 form and is in UTC. Populated by the system. Read-only. Null for
        lists. More info: https://git.k8s.io/community/contributors/devel/api-conventions.md#metadata'
      jsonPath: .metadata.creationTimestamp
      name: Age
      type: date
    name: v1alpha1
    subresources:
      status: {}
    schema:
      openAPIV3Schema:
        type: object
        x-kubernetes-preserve-unknown-fields: true
    served: true
    storage: true
---
```

```
istioctl operator dump --hub  docker.io/istio

istioctl operator dump --manifests /root/istio-1.14.1/manifests

istioctl operator dump --operatorNamespace istio-operator   
istioctl operator dump  --output json
 istioctl operator dump   --revision 1-14-1
 istioctl operator dump   --tag 1.14.1
 istioctl operator dump    --watchedNamespaces istio-system
```



## init

```
[root@node01 ~]# istioctl operator init --help
The init subcommand installs the Istio operator controller in the cluster.

Usage:
  istioctl operator init [flags]

Flags:
      --dry-run                    Console/log output only, make no changes.
  -f, --filename string            Path to file containing IstioOperator custom resource
                                   This flag can be specified multiple times to overlay multiple files. Multiple files are overlaid in left to right order.
  -h, --help                       help for init
      --hub string                 The hub for the operator controller image. (default "docker.io/istio")
      --imagePullSecrets strings   The imagePullSecrets are used to pull the operator image from the private registry,
                                   could be secret list separated by comma, eg. '--imagePullSecrets imagePullSecret1,imagePullSecret2'
  -d, --manifests string           Specify a path to a directory of charts and profiles
                                   (e.g. ~/Downloads/istio-1.14.1/manifests)
                                   or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                   
      --operatorNamespace string   The namespace the operator controller is installed into. (default "istio-operator")
  -r, --revision string            Target revision for the operator.
      --tag string                 The tag for the operator controller image. (default "1.14.1")
      --watchedNamespaces string   The namespaces the operator controller watches, could be namespace list separated by comma, eg. 'ns1,ns2' (default "istio-system")

Global Flags:
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl operator init
Installing operator controller in namespace: istio-operator using image: docker.io/istio/operator:1.11.0
Operator controller will watch namespaces: istio-system
✔ Istio operator installed                                                                                                            
✔ Installation complete
[root@master01 istioctl]# kubectl get pod -n istio-operator 
NAME                              READY   STATUS    RESTARTS   AGE
istio-operator-6c4d864cdf-ftbff   1/1     Running   0          33s


istioctl operator init --hub docker.io/istio --manifests /root/istio-1.14.1/manifests  --operatorNamespace istio-operator --revision 1-14-1 --tag 1.14.1  --watchedNamespaces istio-system

```



## remove

```
[root@node01 ~]# istioctl operator remove --help
The remove subcommand removes the Istio operator controller from the cluster.

Usage:
  istioctl operator remove [flags]

Flags:
      --dry-run                    Console/log output only, make no changes.
      --force                      Proceed even with validation errors.
  -h, --help                       help for remove
      --operatorNamespace string   The namespace the operator controller is installed into. (default "istio-operator")
  -r, --revision string            Target revision for the operator.

Global Flags:
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl operator remove
Removing Istio operator...
  Removed Deployment:istio-operator:istio-operator.
  Removed Service:istio-operator:istio-operator.
  Removed ServiceAccount:istio-operator:istio-operator.
  Removed ClusterRole::istio-operator.
  Removed ClusterRoleBinding::istio-operator.
✔ Removal complete
[root@master01 istioctl]# kubectl get pod -n istio-operator
No resources found in istio-operator namespace.

istioctl operator remove --revision 1-14-1 --operatorNamespace  istio-operator --force   
```



# profile

```
[root@node01 ~]# istioctl profile --help
The profile command lists, dumps or diffs Istio configuration profiles.

Usage:
  istioctl profile [command]

Examples:
istioctl profile list
istioctl install --set profile=demo  # Use a profile from the list

Available Commands:
  diff        Diffs two Istio configuration profiles
  dump        Dumps an Istio configuration profile
  list        Lists available Istio configuration profiles

Flags:
      --dry-run   Console/log output only, make no changes.
  -h, --help      help for profile

Global Flags:
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl profile [command] --help" for more information about a command.
```

## list

```
[root@node01 ~]# istioctl profile list --help
The list subcommand lists the available Istio configuration profiles.

Usage:
  istioctl profile list [flags]

Flags:
  -h, --help               help for list
  -d, --manifests string   Specify a path to a directory of charts and profiles
                           (e.g. ~/Downloads/istio-1.14.1/manifests)
                           or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).

Global Flags:
      --context string      The name of the kubeconfig context to use
      --dry-run             Console/log output only, make no changes.
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 ~]# istioctl profile list
Istio configuration profiles:
    default
    demo
    empty
    external
    minimal
    openshift
    preview
    remote
    
    
istioctl profile list --manifests /root/istio-1.14.1/manifests
```



## diff

```
[root@node01 ~]# istioctl profile diff --help
The diff subcommand displays the differences between two Istio configuration profiles.

Usage:
  istioctl profile diff <profile|file1.yaml> <profile|file2.yaml> [flags]

Examples:
  # Profile diff by providing yaml files
  istioctl profile diff manifests/profiles/default.yaml manifests/profiles/demo.yaml

  # Profile diff by providing a profile name
  istioctl profile diff default demo

Flags:
  -h, --help               help for diff
  -d, --manifests string   Specify a path to a directory of charts and profiles
                           (e.g. ~/Downloads/istio-1.14.1/manifests)
                           or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).

Global Flags:
      --context string      The name of the kubeconfig context to use
      --dry-run             Console/log output only, make no changes.
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl profile diff default demo
2021-08-27T09:03:53.090830Z     info    proto: tag has too few fields: "-"
The difference between profiles:
 apiVersion: install.istio.io/v1alpha1
 kind: IstioOperator
 metadata:
   creationTimestamp: null
   namespace: istio-system
 spec:
   components:
     base:
       enabled: true
     cni:
       enabled: false
     egressGateways:
-    - enabled: false
+    - enabled: true
+      k8s:
+        resources:
+          requests:
+            cpu: 10m
+            memory: 40Mi
       name: istio-egressgateway
     ingressGateways:
     - enabled: true
+      k8s:
+        resources:
+          requests:
+            cpu: 10m
+            memory: 40Mi
+        service:
+          ports:
+          - name: status-port
+            port: 15021
+            targetPort: 15021
+          - name: http2
+            port: 80
+            targetPort: 8080
+          - name: https
+            port: 443
+            targetPort: 8443
+          - name: tcp
+            port: 31400
+            targetPort: 31400
+          - name: tls
+            port: 15443
+            targetPort: 15443
       name: istio-ingressgateway
     istiodRemote:
       enabled: false
     pilot:
       enabled: true
+      k8s:
+        env:
+        - name: PILOT_TRACE_SAMPLING
+          value: "100"
+        resources:
+          requests:
+            cpu: 10m
+            memory: 100Mi
   hub: docker.io/istio
   meshConfig:
+    accessLogFile: /dev/stdout
     defaultConfig:
       proxyMetadata: {}
     enablePrometheusMerge: true
   profile: default
   tag: 1.11.0
   values:
     base:
       enableCRDTemplates: false
       validationURL: ""
     gateways:
       istio-egressgateway:
-        autoscaleEnabled: true
+        autoscaleEnabled: false
         env: {}
         name: istio-egressgateway
         secretVolumes:
         - mountPath: /etc/istio/egressgateway-certs
           name: egressgateway-certs
           secretName: istio-egressgateway-certs
         - mountPath: /etc/istio/egressgateway-ca-certs
           name: egressgateway-ca-certs
           secretName: istio-egressgateway-ca-certs
         type: ClusterIP
         zvpn: {}
       istio-ingressgateway:
-        autoscaleEnabled: true
+        autoscaleEnabled: false
         env: {}
         name: istio-ingressgateway
         secretVolumes:
         - mountPath: /etc/istio/ingressgateway-certs
           name: ingressgateway-certs
           secretName: istio-ingressgateway-certs
         - mountPath: /etc/istio/ingressgateway-ca-certs
           name: ingressgateway-ca-certs
           secretName: istio-ingressgateway-ca-certs
         type: LoadBalancer
         zvpn: {}
     global:
       configValidation: true
       defaultNodeSelector: {}
       defaultPodDisruptionBudget:
         enabled: true
       defaultResources:
         requests:
           cpu: 10m
       imagePullPolicy: ""
       imagePullSecrets: []
       istioNamespace: istio-system
       istiod:
         enableAnalysis: false
       jwtPolicy: third-party-jwt
       logAsJson: false
       logging:
         level: default:info
       meshNetworks: {}
       mountMtlsCerts: false
       multiCluster:
         clusterName: ""
         enabled: false
       network: ""
       omitSidecarInjectorConfigMap: false
       oneNamespace: false
       operatorManageWebhooks: false
       pilotCertProvider: istiod
       priorityClassName: ""
       proxy:
         autoInject: enabled
         clusterDomain: cluster.local
         componentLogLevel: misc:error
         enableCoreDump: false
         excludeIPRanges: ""
         excludeInboundPorts: ""
         excludeOutboundPorts: ""
         image: proxyv2
         includeIPRanges: '*'
         logLevel: warning
         privileged: false
         readinessFailureThreshold: 30
         readinessInitialDelaySeconds: 1
         readinessPeriodSeconds: 2
         resources:
           limits:
             cpu: 2000m
             memory: 1024Mi
           requests:
-            cpu: 100m
-            memory: 128Mi
+            cpu: 10m
+            memory: 40Mi
         statusPort: 15020
         tracer: zipkin
       proxy_init:
         image: proxyv2
         resources:
           limits:
             cpu: 2000m
             memory: 1024Mi
           requests:
             cpu: 10m
             memory: 10Mi
       sds:
         token:
           aud: istio-ca
       sts:
         servicePort: 0
       tracer:
         datadog: {}
         lightstep: {}
         stackdriver: {}
         zipkin: {}
       useMCP: false
     istiodRemote:
       injectionURL: ""
     pilot:
-      autoscaleEnabled: true
+      autoscaleEnabled: false
       autoscaleMax: 5
       autoscaleMin: 1
       configMap: true
       cpu:
         targetAverageUtilization: 80
       deploymentLabels: null
       enableProtocolSniffingForInbound: true
       enableProtocolSniffingForOutbound: true
       env: {}
       image: pilot
       keepaliveMaxServerConnectionAge: 30m
       nodeSelector: {}
       replicaCount: 1
       traceSampling: 1
     telemetry:
       enabled: true
       v2:
         enabled: true
         metadataExchange:
           wasmEnabled: false
         prometheus:
           enabled: true
           wasmEnabled: false
         stackdriver:
           configOverride: {}
           enabled: false
           logging: false
           monitoring: false
           topology: false
```

```
istioctl profile diff default demo --manifests /root/istio-1.14.1/manifests
```



## dump

```
[root@node01 ~]# istioctl profile dump --help
The dump subcommand dumps the values in an Istio configuration profile.

Usage:
  istioctl profile dump [<profile>] [flags]

Flags:
  -p, --config-path string   The path the root of the configuration subtree to dump e.g. components.pilot. By default, dump whole tree
  -f, --filename strings     Path to file containing IstioOperator custom resource
                             This flag can be specified multiple times to overlay multiple files. Multiple files are overlaid in left to right order.
  -h, --help                 help for dump
  -d, --manifests string     Specify a path to a directory of charts and profiles
                             (e.g. ~/Downloads/istio-1.14.1/manifests)
                             or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                             
  -o, --output string        Output format: one of json|yaml|flags (default "yaml")

Global Flags:
      --context string      The name of the kubeconfig context to use
      --dry-run             Console/log output only, make no changes.
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl profile dump 
2021-08-27T09:06:01.630609Z     info    proto: tag has too few fields: "-"
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  components:
    base:
      enabled: true
    cni:
      enabled: false
    egressGateways:
    - enabled: false
      name: istio-egressgateway
    ingressGateways:
    - enabled: true
      name: istio-ingressgateway
    istiodRemote:
      enabled: false
    pilot:
      enabled: true
  hub: docker.io/istio
  meshConfig:
    defaultConfig:
      proxyMetadata: {}
    enablePrometheusMerge: true
  profile: default
  tag: 1.11.0
  values:
    base:
      enableCRDTemplates: false
      validationURL: ""
    gateways:
      istio-egressgateway:
        autoscaleEnabled: true
        env: {}
        name: istio-egressgateway
        secretVolumes:
        - mountPath: /etc/istio/egressgateway-certs
          name: egressgateway-certs
          secretName: istio-egressgateway-certs
        - mountPath: /etc/istio/egressgateway-ca-certs
          name: egressgateway-ca-certs
          secretName: istio-egressgateway-ca-certs
        type: ClusterIP
        zvpn: {}
      istio-ingressgateway:
        autoscaleEnabled: true
        env: {}
        name: istio-ingressgateway
        secretVolumes:
        - mountPath: /etc/istio/ingressgateway-certs
          name: ingressgateway-certs
          secretName: istio-ingressgateway-certs
        - mountPath: /etc/istio/ingressgateway-ca-certs
          name: ingressgateway-ca-certs
          secretName: istio-ingressgateway-ca-certs
        type: LoadBalancer
        zvpn: {}
    global:
      configValidation: true
      defaultNodeSelector: {}
      defaultPodDisruptionBudget:
        enabled: true
      defaultResources:
        requests:
          cpu: 10m
      imagePullPolicy: ""
      imagePullSecrets: []
      istioNamespace: istio-system
      istiod:
        enableAnalysis: false
      jwtPolicy: third-party-jwt
      logAsJson: false
      logging:
        level: default:info
      meshNetworks: {}
      mountMtlsCerts: false
      multiCluster:
        clusterName: ""
        enabled: false
      network: ""
      omitSidecarInjectorConfigMap: false
      oneNamespace: false
      operatorManageWebhooks: false
      pilotCertProvider: istiod
      priorityClassName: ""
      proxy:
        autoInject: enabled
        clusterDomain: cluster.local
        componentLogLevel: misc:error
        enableCoreDump: false
        excludeIPRanges: ""
        excludeInboundPorts: ""
        excludeOutboundPorts: ""
        image: proxyv2
        includeIPRanges: '*'
        logLevel: warning
        privileged: false
        readinessFailureThreshold: 30
        readinessInitialDelaySeconds: 1
        readinessPeriodSeconds: 2
        resources:
          limits:
            cpu: 2000m
            memory: 1024Mi
          requests:
            cpu: 100m
            memory: 128Mi
        statusPort: 15020
        tracer: zipkin
      proxy_init:
        image: proxyv2
        resources:
          limits:
            cpu: 2000m
            memory: 1024Mi
          requests:
            cpu: 10m
            memory: 10Mi
      sds:
        token:
          aud: istio-ca
      sts:
        servicePort: 0
      tracer:
        datadog: {}
        lightstep: {}
        stackdriver: {}
        zipkin: {}
      useMCP: false
    istiodRemote:
      injectionURL: ""
    pilot:
      autoscaleEnabled: true
      autoscaleMax: 5
      autoscaleMin: 1
      configMap: true
      cpu:
        targetAverageUtilization: 80
      enableProtocolSniffingForInbound: true
      enableProtocolSniffingForOutbound: true
      env: {}
      image: pilot
      keepaliveMaxServerConnectionAge: 30m
      nodeSelector: {}
      replicaCount: 1
      traceSampling: 1
    telemetry:
      enabled: true
      v2:
        enabled: true
        metadataExchange:
          wasmEnabled: false
        prometheus:
          enabled: true
          wasmEnabled: false
        stackdriver:
          configOverride: {}
          enabled: false
          logging: false
          monitoring: false
          topology: false
```

```
[root@master01 istioctl]# istioctl profile dump  demo
2021-08-27T09:06:26.767339Z     info    proto: tag has too few fields: "-"
apiVersion: install.istio.io/v1alpha1
kind: IstioOperator
spec:
  components:
    base:
      enabled: true
    cni:
      enabled: false
    egressGateways:
    - enabled: true
      k8s:
        resources:
          requests:
            cpu: 10m
            memory: 40Mi
      name: istio-egressgateway
    ingressGateways:
    - enabled: true
      k8s:
        resources:
          requests:
            cpu: 10m
            memory: 40Mi
        service:
          ports:
          - name: status-port
            port: 15021
            targetPort: 15021
          - name: http2
            port: 80
            targetPort: 8080
          - name: https
            port: 443
            targetPort: 8443
          - name: tcp
            port: 31400
            targetPort: 31400
          - name: tls
            port: 15443
            targetPort: 15443
      name: istio-ingressgateway
    istiodRemote:
      enabled: false
    pilot:
      enabled: true
      k8s:
        env:
        - name: PILOT_TRACE_SAMPLING
          value: "100"
        resources:
          requests:
            cpu: 10m
            memory: 100Mi
  hub: docker.io/istio
  meshConfig:
    accessLogFile: /dev/stdout
    defaultConfig:
      proxyMetadata: {}
    enablePrometheusMerge: true
  profile: demo
  tag: 1.11.0
  values:
    base:
      enableCRDTemplates: false
      validationURL: ""
    gateways:
      istio-egressgateway:
        autoscaleEnabled: false
        env: {}
        name: istio-egressgateway
        secretVolumes:
        - mountPath: /etc/istio/egressgateway-certs
          name: egressgateway-certs
          secretName: istio-egressgateway-certs
        - mountPath: /etc/istio/egressgateway-ca-certs
          name: egressgateway-ca-certs
          secretName: istio-egressgateway-ca-certs
        type: ClusterIP
        zvpn: {}
      istio-ingressgateway:
        autoscaleEnabled: false
        env: {}
        name: istio-ingressgateway
        secretVolumes:
        - mountPath: /etc/istio/ingressgateway-certs
          name: ingressgateway-certs
          secretName: istio-ingressgateway-certs
        - mountPath: /etc/istio/ingressgateway-ca-certs
          name: ingressgateway-ca-certs
          secretName: istio-ingressgateway-ca-certs
        type: LoadBalancer
        zvpn: {}
    global:
      configValidation: true
      defaultNodeSelector: {}
      defaultPodDisruptionBudget:
        enabled: true
      defaultResources:
        requests:
          cpu: 10m
      imagePullPolicy: ""
      imagePullSecrets: []
      istioNamespace: istio-system
      istiod:
        enableAnalysis: false
      jwtPolicy: third-party-jwt
      logAsJson: false
      logging:
        level: default:info
      meshNetworks: {}
      mountMtlsCerts: false
      multiCluster:
        clusterName: ""
        enabled: false
      network: ""
      omitSidecarInjectorConfigMap: false
      oneNamespace: false
      operatorManageWebhooks: false
      pilotCertProvider: istiod
      priorityClassName: ""
      proxy:
        autoInject: enabled
        clusterDomain: cluster.local
        componentLogLevel: misc:error
        enableCoreDump: false
        excludeIPRanges: ""
        excludeInboundPorts: ""
        excludeOutboundPorts: ""
        image: proxyv2
        includeIPRanges: '*'
        logLevel: warning
        privileged: false
        readinessFailureThreshold: 30
        readinessInitialDelaySeconds: 1
        readinessPeriodSeconds: 2
        resources:
          limits:
            cpu: 2000m
            memory: 1024Mi
          requests:
            cpu: 10m
            memory: 40Mi
        statusPort: 15020
        tracer: zipkin
      proxy_init:
        image: proxyv2
        resources:
          limits:
            cpu: 2000m
            memory: 1024Mi
          requests:
            cpu: 10m
            memory: 10Mi
      sds:
        token:
          aud: istio-ca
      sts:
        servicePort: 0
      tracer:
        datadog: {}
        lightstep: {}
        stackdriver: {}
        zipkin: {}
      useMCP: false
    istiodRemote:
      injectionURL: ""
    pilot:
      autoscaleEnabled: false
      autoscaleMax: 5
      autoscaleMin: 1
      configMap: true
      cpu:
        targetAverageUtilization: 80
      enableProtocolSniffingForInbound: true
      enableProtocolSniffingForOutbound: true
      env: {}
      image: pilot
      keepaliveMaxServerConnectionAge: 30m
      nodeSelector: {}
      replicaCount: 1
      traceSampling: 1
    telemetry:
      enabled: true
      v2:
        enabled: true
        metadataExchange:
          wasmEnabled: false
        prometheus:
          enabled: true
          wasmEnabled: false
        stackdriver:
          configOverride: {}
          enabled: false
          logging: false
          monitoring: false
          topology: false
```

```
istioctl profile dump  -f iop.yaml 

istioctl profile dump  --manifests /root/istio-1.14.1/manifests

istioctl profile dump   --output json
istioctl profile dump   --output yaml
istioctl profile dump   --output flags
```

·

# proxy-config 

```
[root@node01 ~]# istioctl pc --help
A group of commands used to retrieve information about proxy configuration from the Envoy config dump

Usage:
  istioctl proxy-config [command]

Aliases:
  proxy-config, pc

Examples:
  # Retrieve information about proxy configuration from an Envoy instance.
  istioctl proxy-config <clusters|listeners|routes|endpoints|bootstrap|log|secret> <pod-name[.namespace]>

Available Commands:
  all            Retrieves all configuration for the Envoy in the specified pod
  bootstrap      Retrieves bootstrap configuration for the Envoy in the specified pod
  cluster        Retrieves cluster configuration for the Envoy in the specified pod
  endpoint       Retrieves endpoint configuration for the Envoy in the specified pod
  listener       Retrieves listener configuration for the Envoy in the specified pod
  log            (experimental) Retrieves logging levels of the Envoy in the specified pod
  rootca-compare Compare ROOTCA values for the two given pods
  route          Retrieves route configuration for the Envoy in the specified pod
  secret         Retrieves secret configuration for the Envoy in the specified pod

Flags:
  -h, --help            help for proxy-config
  -o, --output string   Output format: one of json|yaml|short (default "short")

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9

Use "istioctl proxy-config [command] --help" for more information about a command.
```

## all

```
[root@node01 ~]# istioctl pc all --help
Retrieve information about all configuration for the Envoy instance in the specified pod.

Usage:
  istioctl proxy-config all [<type>/]<name>[.<namespace>] [flags]

Aliases:
  all, a

Examples:
  # Retrieve summary about all configuration for a given pod from Envoy.
  istioctl proxy-config all <pod-name[.namespace]>

  # Retrieve full cluster dump as JSON
  istioctl proxy-config all <pod-name[.namespace]> -o json

  # Retrieve full cluster dump with short syntax
  istioctl pc a <pod-name[.namespace]>

  # Retrieve cluster summary without using Kubernetes API
  ssh <user@hostname> 'curl localhost:15000/config_dump' > envoy-config.json
  istioctl proxy-config all --file envoy-config.json


Flags:
      --address string     Filter listeners by address field
      --direction string   Filter clusters by Direction field
  -f, --file string        Envoy config dump file
      --fqdn string        Filter clusters by substring of Service FQDN field
  -h, --help               help for all
      --name string        Filter listeners by route name field
      --port int           Filter clusters and listeners by Port field
      --subset string      Filter clusters by substring of Subset field
      --type string        Filter listeners by type field
      --verbose            Output more information (default true)

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
  -o, --output string           Output format: one of json|yaml|short (default "short")
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 istioctl]#  istioctl proxy-config all productpage-v1-85c4dcbb4c-gsjgw.istio
SERVICE FQDN                                                      PORT      SUBSET     DIRECTION     TYPE             DESTINATION RULE
                                                                  9080      -          inbound       ORIGINAL_DST     
                                                                  24231     -          inbound       ORIGINAL_DST     
BlackHoleCluster                                                  -         -          -             STATIC           
InboundPassthroughClusterIpv4                                     -         -          -             ORIGINAL_DST     
PassthroughCluster                                                -         -          -             ORIGINAL_DST     
agent                                                             -         -          -             STATIC           
argo-rollouts-metrics.argo-rollouts.svc.cluster.local             8090      -          outbound      EDS              
broker-0.default.svc.cluster.local                                10909     -          outbound      EDS              
broker-0.default.svc.cluster.local                                10911     -          outbound      EDS              
broker-0.default.svc.cluster.local                                10912     -          outbound      EDS              
broker-1.default.svc.cluster.local                                10909     -          outbound      EDS              
broker-1.default.svc.cluster.local                                10911     -          outbound      EDS              
broker-1.default.svc.cluster.local                                10912     -          outbound      EDS              
console.default.svc.cluster.local                                 8080      -          outbound      EDS              
dashboard-metrics-scraper.kube-system.svc.cluster.local           8000      -          outbound      EDS              
details.istio.svc.cluster.local                                   9080      -          outbound      EDS              
dubbo-provider.istio.svc.cluster.local                            20880     -          outbound      EDS              
gateway-api-admission-server.gateway-system.svc.cluster.local     443       -          outbound      EDS              
grafana.istio-system.svc.cluster.local                            3000      -          outbound      EDS              
istio-egressgateway.istio-system.svc.cluster.local                80        -          outbound      EDS              
istio-egressgateway.istio-system.svc.cluster.local                443       -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local               80        -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local               443       -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local               15021     -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local               15443     -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local               31400     -          outbound      EDS              
istiod-1-14-1.istio-system.svc.cluster.local                      443       -          outbound      EDS              
istiod-1-14-1.istio-system.svc.cluster.local                      15010     -          outbound      EDS              
istiod-1-14-1.istio-system.svc.cluster.local                      15012     -          outbound      EDS              
istiod-1-14-1.istio-system.svc.cluster.local                      15014     -          outbound      EDS              
istiod-1-14-2.istio-system.svc.cluster.local                      443       -          outbound      EDS              
istiod-1-14-2.istio-system.svc.cluster.local                      15010     -          outbound      EDS              
istiod-1-14-2.istio-system.svc.cluster.local                      15012     -          outbound      EDS              
istiod-1-14-2.istio-system.svc.cluster.local                      15014     -          outbound      EDS              
istiod.istio-system.svc.cluster.local                             443       -          outbound      EDS              
istiod.istio-system.svc.cluster.local                             15010     -          outbound      EDS              
istiod.istio-system.svc.cluster.local                             15012     -          outbound      EDS              
istiod.istio-system.svc.cluster.local                             15014     -          outbound      EDS              
jaeger-collector.istio-system.svc.cluster.local                   14250     -          outbound      EDS              
jaeger-collector.istio-system.svc.cluster.local                   14268     -          outbound      EDS              
kiali.istio-system.svc.cluster.local                              9090      -          outbound      EDS              
kiali.istio-system.svc.cluster.local                              20001     -          outbound      EDS              
kube-dns.kube-system.svc.cluster.local                            53        -          outbound      EDS              
kube-dns.kube-system.svc.cluster.local                            9153      -          outbound      EDS              
kubernetes-dashboard.kube-system.svc.cluster.local                443       -          outbound      EDS              
kubernetes.default.svc.cluster.local                              443       -          outbound      EDS              
metrics-server.kube-system.svc.cluster.local                      443       -          outbound      EDS              
name-service.default.svc.cluster.local                            9876      -          outbound      EDS              
productpage.default.svc.cluster.local                             9080      -          outbound      EDS              
productpage.istio.svc.cluster.local                               9080      -          outbound      EDS              
productpage.istio.svc.cluster.local                               24231     -          outbound      EDS              
prometheus.istio-system.svc.cluster.local                         9090      -          outbound      EDS              
prometheus_stats                                                  -         -          -             STATIC           
ratings.istio.svc.cluster.local                                   9080      -          outbound      EDS              
reviews.istio.svc.cluster.local                                   9080      -          outbound      EDS              
sds-grpc                                                          -         -          -             STATIC           
tcp-echo.istio.svc.cluster.local                                  9000      -          outbound      EDS              
tcp-echo.istio.svc.cluster.local                                  9001      -          outbound      EDS              
tracing.istio-system.svc.cluster.local                            80        -          outbound      EDS              
xds-grpc                                                          -         -          -             STATIC           
zipkin                                                            -         -          -             STRICT_DNS       
zipkin.istio-system.svc.cluster.local                             9411      -          outbound      EDS              

ADDRESS       PORT  MATCH                                                                                            DESTINATION
10.68.0.2     53    ALL                                                                                              Cluster: outbound|53||kube-dns.kube-system.svc.cluster.local
0.0.0.0       80    Trans: raw_buffer; App: http/1.1,h2c                                                             Route: 80
0.0.0.0       80    ALL                                                                                              PassthroughCluster
10.68.0.1     443   ALL                                                                                              Cluster: outbound|443||kubernetes.default.svc.cluster.local
10.68.114.252 443   Trans: raw_buffer; App: http/1.1,h2c                                                             Route: metrics-server.kube-system.svc.cluster.local:443
10.68.114.252 443   ALL                                                                                              Cluster: outbound|443||metrics-server.kube-system.svc.cluster.local
10.68.156.232 443   ALL                                                                                              Cluster: outbound|443||istiod-1-14-1.istio-system.svc.cluster.local
10.68.217.182 443   ALL                                                                                              Cluster: outbound|443||istio-egressgateway.istio-system.svc.cluster.local
10.68.229.129 443   Trans: raw_buffer; App: http/1.1,h2c                                                             Route: kubernetes-dashboard.kube-system.svc.cluster.local:443
10.68.229.129 443   ALL                                                                                              Cluster: outbound|443||kubernetes-dashboard.kube-system.svc.cluster.local
10.68.26.9    443   ALL                                                                                              Cluster: outbound|443||istiod-1-14-2.istio-system.svc.cluster.local
10.68.51.184  443   ALL                                                                                              Cluster: outbound|443||istio-ingressgateway.istio-system.svc.cluster.local
10.68.55.160  443   ALL                                                                                              Cluster: outbound|443||istiod.istio-system.svc.cluster.local
10.68.6.15    443   ALL                                                                                              Cluster: outbound|443||gateway-api-admission-server.gateway-system.svc.cluster.local
10.68.126.229 3000  Trans: raw_buffer; App: http/1.1,h2c                                                             Route: grafana.istio-system.svc.cluster.local:3000
10.68.126.229 3000  ALL                                                                                              Cluster: outbound|3000||grafana.istio-system.svc.cluster.local
10.68.249.207 8000  Trans: raw_buffer; App: http/1.1,h2c                                                             Route: dashboard-metrics-scraper.kube-system.svc.cluster.local:8000
10.68.249.207 8000  ALL                                                                                              Cluster: outbound|8000||dashboard-metrics-scraper.kube-system.svc.cluster.local
10.68.161.14  8080  ALL                                                                                              Cluster: outbound|8080||console.default.svc.cluster.local
10.68.146.27  8090  Trans: raw_buffer; App: http/1.1,h2c                                                             Route: argo-rollouts-metrics.argo-rollouts.svc.cluster.local:8090
10.68.146.27  8090  ALL                                                                                              Cluster: outbound|8090||argo-rollouts-metrics.argo-rollouts.svc.cluster.local
10.68.61.178  9000  ALL                                                                                              Cluster: outbound|9000||tcp-echo.istio.svc.cluster.local
10.68.61.178  9001  ALL                                                                                              Cluster: outbound|9001||tcp-echo.istio.svc.cluster.local
0.0.0.0       9080  Trans: raw_buffer; App: http/1.1,h2c                                                             Route: 9080
0.0.0.0       9080  ALL                                                                                              PassthroughCluster
0.0.0.0       9090  Trans: raw_buffer; App: http/1.1,h2c                                                             Route: 9090
0.0.0.0       9090  ALL                                                                                              PassthroughCluster
10.68.0.2     9153  Trans: raw_buffer; App: http/1.1,h2c                                                             Route: kube-dns.kube-system.svc.cluster.local:9153
10.68.0.2     9153  ALL                                                                                              Cluster: outbound|9153||kube-dns.kube-system.svc.cluster.local
0.0.0.0       9411  Trans: raw_buffer; App: http/1.1,h2c                                                             Route: 9411
0.0.0.0       9411  ALL                                                                                              PassthroughCluster
10.68.83.42   9876  ALL                                                                                              Cluster: outbound|9876||name-service.default.svc.cluster.local
10.68.127.230 10909 ALL                                                                                              Cluster: outbound|10909||broker-0.default.svc.cluster.local
10.68.28.5    10909 ALL                                                                                              Cluster: outbound|10909||broker-1.default.svc.cluster.local
10.68.127.230 10911 ALL                                                                                              Cluster: outbound|10911||broker-0.default.svc.cluster.local
10.68.28.5    10911 ALL                                                                                              Cluster: outbound|10911||broker-1.default.svc.cluster.local
10.68.127.230 10912 ALL                                                                                              Cluster: outbound|10912||broker-0.default.svc.cluster.local
10.68.28.5    10912 ALL                                                                                              Cluster: outbound|10912||broker-1.default.svc.cluster.local
10.68.177.123 14250 Trans: raw_buffer; App: http/1.1,h2c                                                             Route: jaeger-collector.istio-system.svc.cluster.local:14250
10.68.177.123 14250 ALL                                                                                              Cluster: outbound|14250||jaeger-collector.istio-system.svc.cluster.local
10.68.177.123 14268 Trans: raw_buffer; App: http/1.1,h2c                                                             Route: jaeger-collector.istio-system.svc.cluster.local:14268
10.68.177.123 14268 ALL                                                                                              Cluster: outbound|14268||jaeger-collector.istio-system.svc.cluster.local
0.0.0.0       15001 ALL                                                                                              PassthroughCluster
0.0.0.0       15001 Addr: *:15001                                                                                    Non-HTTP/Non-TCP
0.0.0.0       15006 Addr: *:15006                                                                                    Non-HTTP/Non-TCP
0.0.0.0       15006 Trans: tls; App: istio-http/1.0,istio-http/1.1,istio-h2; Addr: 0.0.0.0/0                         InboundPassthroughClusterIpv4
0.0.0.0       15006 Trans: raw_buffer; App: http/1.1,h2c; Addr: 0.0.0.0/0                                            InboundPassthroughClusterIpv4
0.0.0.0       15006 Trans: tls; App: TCP TLS; Addr: 0.0.0.0/0                                                        InboundPassthroughClusterIpv4
0.0.0.0       15006 Trans: raw_buffer; Addr: 0.0.0.0/0                                                               InboundPassthroughClusterIpv4
0.0.0.0       15006 Trans: tls; Addr: 0.0.0.0/0                                                                      InboundPassthroughClusterIpv4
0.0.0.0       15006 Trans: tls; App: istio,istio-peer-exchange,istio-http/1.0,istio-http/1.1,istio-h2; Addr: *:9080  Cluster: inbound|9080||
0.0.0.0       15006 Trans: raw_buffer; Addr: *:9080                                                                  Cluster: inbound|9080||
0.0.0.0       15006 Trans: tls; App: istio,istio-peer-exchange,istio-http/1.0,istio-http/1.1,istio-h2; Addr: *:24231 Cluster: inbound|24231||
0.0.0.0       15006 Trans: raw_buffer; Addr: *:24231                                                                 Cluster: inbound|24231||
0.0.0.0       15010 Trans: raw_buffer; App: http/1.1,h2c                                                             Route: 15010
0.0.0.0       15010 ALL                                                                                              PassthroughCluster
10.68.156.232 15012 ALL                                                                                              Cluster: outbound|15012||istiod-1-14-1.istio-system.svc.cluster.local
10.68.26.9    15012 ALL                                                                                              Cluster: outbound|15012||istiod-1-14-2.istio-system.svc.cluster.local
10.68.55.160  15012 ALL                                                                                              Cluster: outbound|15012||istiod.istio-system.svc.cluster.local
0.0.0.0       15014 Trans: raw_buffer; App: http/1.1,h2c                                                             Route: 15014
0.0.0.0       15014 ALL                                                                                              PassthroughCluster
0.0.0.0       15021 ALL                                                                                              Inline Route: /healthz/ready*
10.68.51.184  15021 Trans: raw_buffer; App: http/1.1,h2c                                                             Route: istio-ingressgateway.istio-system.svc.cluster.local:15021
10.68.51.184  15021 ALL                                                                                              Cluster: outbound|15021||istio-ingressgateway.istio-system.svc.cluster.local
0.0.0.0       15090 ALL                                                                                              Inline Route: /stats/prometheus*
10.68.51.184  15443 ALL                                                                                              Cluster: outbound|15443||istio-ingressgateway.istio-system.svc.cluster.local
0.0.0.0       20001 Trans: raw_buffer; App: http/1.1,h2c                                                             Route: 20001
0.0.0.0       20001 ALL                                                                                              PassthroughCluster
10.68.136.151 20880 ALL                                                                                              Cluster: outbound|20880||dubbo-provider.istio.svc.cluster.local
0.0.0.0       24231 Trans: raw_buffer; App: http/1.1,h2c                                                             Route: 24231
0.0.0.0       24231 ALL                                                                                              PassthroughCluster
10.68.51.184  31400 ALL                                                                                              Cluster: outbound|31400||istio-ingressgateway.istio-system.svc.cluster.local

NAME                                                             DOMAINS                                             MATCH                  VIRTUAL SERVICE
80                                                               istio-egressgateway.istio-system, 10.68.217.182     /*                     
80                                                               istio-ingressgateway.istio-system, 10.68.51.184     /*                     
80                                                               tracing.istio-system, 10.68.82.93                   /*                     
9080                                                             details, details.istio + 1 more...                  /*                     
9080                                                             productpage.default, 10.68.210.20                   /*                     
9080                                                             productpage, productpage.istio + 1 more...          /*                     
9080                                                             ratings, ratings.istio + 1 more...                  /*                     
9080                                                             reviews, reviews.istio + 1 more...                  /*                     
9090                                                             kiali.istio-system, 10.68.50.247                    /*                     
9090                                                             prometheus.istio-system, 10.68.89.176               /*                     
9411                                                             zipkin.istio-system, 10.68.223.115                  /*                     
15010                                                            istiod-1-14-1.istio-system, 10.68.156.232           /*                     
15010                                                            istiod-1-14-2.istio-system, 10.68.26.9              /*                     
15010                                                            istiod.istio-system, 10.68.55.160                   /*                     
inbound|9080||                                                   *                                                   /*                     
istio-ingressgateway.istio-system.svc.cluster.local:15021        *                                                   /*                     
inbound|24231||                                                  *                                                   /*                     
InboundPassthroughClusterIpv4                                    *                                                   /*                     
grafana.istio-system.svc.cluster.local:3000                      *                                                   /*                     
argo-rollouts-metrics.argo-rollouts.svc.cluster.local:8090       *                                                   /*                     
jaeger-collector.istio-system.svc.cluster.local:14268            *                                                   /*                     
jaeger-collector.istio-system.svc.cluster.local:14250            *                                                   /*                     
kubernetes-dashboard.kube-system.svc.cluster.local:443           *                                                   /*                     
dashboard-metrics-scraper.kube-system.svc.cluster.local:8000     *                                                   /*                     
kube-dns.kube-system.svc.cluster.local:9153                      *                                                   /*                     
metrics-server.kube-system.svc.cluster.local:443                 *                                                   /*                     
                                                                 *                                                   /stats/prometheus*     
InboundPassthroughClusterIpv4                                    *                                                   /*                     
inbound|24231||                                                  *                                                   /*                     
inbound|9080||                                                   *                                                   /*                     
15014                                                            istiod-1-14-1.istio-system, 10.68.156.232           /*                     
15014                                                            istiod-1-14-2.istio-system, 10.68.26.9              /*                     
15014                                                            istiod.istio-system, 10.68.55.160                   /*                     
20001                                                            kiali.istio-system, 10.68.50.247                    /*                     
24231                                                            productpage, productpage.istio + 1 more...          /*                     
                                                                 *                                                   /healthz/ready*        

RESOURCE NAME     TYPE           STATUS     VALID CERT     SERIAL NUMBER                                        NOT AFTER                NOT BEFORE
default           Cert Chain     ACTIVE     true           64744661644186516927626116744811689359               2022-06-23T03:18:46Z     2022-06-22T03:16:46Z
ROOTCA            CA             ACTIVE     true           249151331429798539605527205320315435712171372644     2032-04-24T05:49:26Z     2022-04-27T05:49:26Z
```

```
istioctl proxy-config all productpage-v1-85c4dcbb4c-gsjgw.istio -o json
```

```
istioctl proxy-config all productpage-v1-85c4dcbb4c-gsjgw.istio -o yaml
```

```
istioctl proxy-config all productpage-v1-85c4dcbb4c-gsjgw.istio --address 10.68.0.2

istioctl proxy-config all productpage-v1-85c4dcbb4c-gsjgw.istio --direction inbound

istioctl proxy-config all -f rating_config_dump.json 

istioctl proxy-config all productpage-v1-85c4dcbb4c-gsjgw.istio --fqdn istio-egressgateway.istio-system.svc.cluster.local

istioctl proxy-config all productpage-v1-85c4dcbb4c-gsjgw.istio --name 80

 istioctl proxy-config all productpage-v1-85c4dcbb4c-gsjgw.istio --port 443
 
 istioctl proxy-config all productpage-v1-85c4dcbb4c-gsjgw.istio  --subset v1
 istioctl proxy-config all productpage-v1-85c4dcbb4c-gsjgw.istio   --type HTTP 
```







## bootstrap

```
[root@node01 ~]# istioctl pc bootstrap --help
Retrieve information about bootstrap configuration for the Envoy instance in the specified pod.

Usage:
  istioctl proxy-config bootstrap [<type>/]<name>[.<namespace>] [flags]

Aliases:
  bootstrap, b

Examples:
  # Retrieve full bootstrap configuration for a given pod from Envoy.
  istioctl proxy-config bootstrap <pod-name[.namespace]>

  # Retrieve full bootstrap without using Kubernetes API
  ssh <user@hostname> 'curl localhost:15000/config_dump' > envoy-config.json
  istioctl proxy-config bootstrap --file envoy-config.json

  # Show a human-readable Istio and Envoy version summary
  istioctl proxy-config bootstrap -o short


Flags:
  -f, --file string   Envoy config dump JSON file
  -h, --help          help for bootstrap

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
  -o, --output string           Output format: one of json|yaml|short (default "short")
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
istioctl pc bootstrap productpage-v1-85c4dcbb4c-gsjgw.istio
```

```
istioctl pc bootstrap productpage-v1-85c4dcbb4c-gsjgw.istio -o yaml
```

```
[root@master01 istioctl]# istioctl pc bootstrap productpage-v1-85c4dcbb4c-gsjgw.istio -o short
Istio Version:       1.11.0
Istio Proxy Version: 494a674e70543a319ad4865482c125581f5746bf
Envoy Version:       1.19.0/Clean/RELEASE/BoringSSL
```



## cluster

```
[root@node01 ~]# istioctl pc cluster --help
Retrieve information about cluster configuration for the Envoy instance in the specified pod.

Usage:
  istioctl proxy-config cluster [<type>/]<name>[.<namespace>] [flags]

Aliases:
  cluster, clusters, c

Examples:
  # Retrieve summary about cluster configuration for a given pod from Envoy.
  istioctl proxy-config clusters <pod-name[.namespace]>

  # Retrieve cluster summary for clusters with port 9080.
  istioctl proxy-config clusters <pod-name[.namespace]> --port 9080

  # Retrieve full cluster dump for clusters that are inbound with a FQDN of details.default.svc.cluster.local.
  istioctl proxy-config clusters <pod-name[.namespace]> --fqdn details.default.svc.cluster.local --direction inbound -o json

  # Retrieve cluster summary without using Kubernetes API
  ssh <user@hostname> 'curl localhost:15000/config_dump' > envoy-config.json
  istioctl proxy-config clusters --file envoy-config.json


Flags:
      --direction string   Filter clusters by Direction field
  -f, --file string        Envoy config dump JSON file
      --fqdn string        Filter clusters by substring of Service FQDN field
  -h, --help               help for cluster
      --port int           Filter clusters by Port field
      --subset string      Filter clusters by substring of Subset field

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
  -o, --output string           Output format: one of json|yaml|short (default "short")
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```



```
[root@master01 istioctl]# istioctl pc cluster productpage-v1-85c4dcbb4c-gsjgw.istio
SERVICE FQDN                                                PORT      SUBSET     DIRECTION     TYPE             DESTINATION RULE
                                                            9080      -          inbound       ORIGINAL_DST     
BlackHoleCluster                                            -         -          -             STATIC           
InboundPassthroughClusterIpv4                               -         -          -             ORIGINAL_DST     
PassthroughCluster                                          -         -          -             ORIGINAL_DST     
agent                                                       -         -          -             STATIC           
bookinfo.default.svc.cluster.local                          9080      -          outbound      EDS              
dashboard-metrics-scraper.kube-system.svc.cluster.local     8000      -          outbound      EDS              
details.default.svc.cluster.local                           9080      -          outbound      EDS              
details.istio.svc.cluster.local                             9080      -          outbound      EDS              
helloworld-go-00001-private.knative.svc.cluster.local       80        -          outbound      EDS              
helloworld-go-00001-private.knative.svc.cluster.local       8012      -          outbound      EDS              
helloworld-go-00001-private.knative.svc.cluster.local       8022      -          outbound      EDS              
helloworld-go-00001-private.knative.svc.cluster.local       9090      -          outbound      EDS              
helloworld-go-00001-private.knative.svc.cluster.local       9091      -          outbound      EDS              
helloworld-go-00001.knative.svc.cluster.local               80        -          outbound      EDS              
helloworld-go-00002-private.knative.svc.cluster.local       80        -          outbound      EDS              
helloworld-go-00002-private.knative.svc.cluster.local       8012      -          outbound      EDS              
helloworld-go-00002-private.knative.svc.cluster.local       8022      -          outbound      EDS              
helloworld-go-00002-private.knative.svc.cluster.local       9090      -          outbound      EDS              
helloworld-go-00002-private.knative.svc.cluster.local       9091      -          outbound      EDS              
helloworld-go-00002.knative.svc.cluster.local               80        -          outbound      EDS              
helloworld-go.knative.svc.cluster.local                     80        -          outbound      STRICT_DNS       
istio-egressgateway.istio-system.svc.cluster.local          80        -          outbound      EDS              
istio-egressgateway.istio-system.svc.cluster.local          443       -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local         80        -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local         443       -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local         15021     -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local         15443     -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local         31400     -          outbound      EDS              
istiod-1-10-0.istio-system.svc.cluster.local                443       -          outbound      EDS              
istiod-1-10-0.istio-system.svc.cluster.local                15010     -          outbound      EDS              
istiod-1-10-0.istio-system.svc.cluster.local                15012     -          outbound      EDS              
istiod-1-10-0.istio-system.svc.cluster.local                15014     -          outbound      EDS              
istiod.istio-system.svc.cluster.local                       443       -          outbound      EDS              
istiod.istio-system.svc.cluster.local                       15010     -          outbound      EDS              
istiod.istio-system.svc.cluster.local                       15012     -          outbound      EDS              
istiod.istio-system.svc.cluster.local                       15014     -          outbound      EDS              
kube-dns-upstream.kube-system.svc.cluster.local             53        -          outbound      EDS              
kube-dns.kube-system.svc.cluster.local                      53        -          outbound      EDS              
kube-dns.kube-system.svc.cluster.local                      9153      -          outbound      EDS              
kubernetes-dashboard.kube-system.svc.cluster.local          443       -          outbound      EDS              
kubernetes.default.svc.cluster.local                        443       -          outbound      EDS              
metrics-server.kube-system.svc.cluster.local                443       -          outbound      EDS              
my-nginx-v2.istio.svc.cluster.local                         443       -          outbound      EDS              
my-nginx.default.svc.cluster.local                          443       -          outbound      EDS              
my-nginx.istio.svc.cluster.local                            443       -          outbound      EDS              
node-local-dns.kube-system.svc.cluster.local                9253      -          outbound      ORIGINAL_DST     
productpage-v2.istio.svc.cluster.local                      9080      -          outbound      EDS              
productpage.default.svc.cluster.local                       9080      -          outbound      EDS              
productpage.istio.svc.cluster.local                         9080      -          outbound      EDS              
prometheus_stats                                            -         -          -             STATIC           
ratings.istio.svc.cluster.local                             9080      -          outbound      EDS              
reviews.istio.svc.cluster.local                             9080      -          outbound      EDS              
sds-grpc                                                    -         -          -             STATIC           
sleep.default.svc.cluster.local                             80        -          outbound      EDS              
tcp-echo.istio.svc.cluster.local                            9000      -          outbound      EDS              
tcp-echo.istio.svc.cluster.local                            9001      -          outbound      EDS              
xds-grpc                                                    -         -          -             STATIC           
zipkin                                                      -         -          -             STRICT_DNS  
```

```
[root@master01 istioctl]# istioctl proxy-config clusters --file rating_config_dump.json 
SERVICE FQDN                                                PORT      SUBSET     DIRECTION     TYPE             DESTINATION RULE
                                                            9080      -          inbound       ORIGINAL_DST     
BlackHoleCluster                                            -         -          -             STATIC           
InboundPassthroughClusterIpv4                               -         -          -             ORIGINAL_DST     
PassthroughCluster                                          -         -          -             ORIGINAL_DST     
agent                                                       -         -          -             STATIC           
bookinfo.default.svc.cluster.local                          9080      -          outbound      EDS              
dashboard-metrics-scraper.kube-system.svc.cluster.local     8000      -          outbound      EDS              
details.default.svc.cluster.local                           9080      -          outbound      EDS              
details.istio.svc.cluster.local                             9080      -          outbound      EDS              
helloworld-go-00001-private.knative.svc.cluster.local       80        -          outbound      EDS              
helloworld-go-00001-private.knative.svc.cluster.local       8012      -          outbound      EDS              
helloworld-go-00001-private.knative.svc.cluster.local       8022      -          outbound      EDS              
helloworld-go-00001-private.knative.svc.cluster.local       9090      -          outbound      EDS              
helloworld-go-00001-private.knative.svc.cluster.local       9091      -          outbound      EDS              
helloworld-go-00001.knative.svc.cluster.local               80        -          outbound      EDS              
helloworld-go-00002-private.knative.svc.cluster.local       80        -          outbound      EDS              
helloworld-go-00002-private.knative.svc.cluster.local       8012      -          outbound      EDS              
helloworld-go-00002-private.knative.svc.cluster.local       8022      -          outbound      EDS              
helloworld-go-00002-private.knative.svc.cluster.local       9090      -          outbound      EDS              
helloworld-go-00002-private.knative.svc.cluster.local       9091      -          outbound      EDS              
helloworld-go-00002.knative.svc.cluster.local               80        -          outbound      EDS              
helloworld-go.knative.svc.cluster.local                     80        -          outbound      STRICT_DNS       
istio-eastwestgateway.istio-system.svc.cluster.local        15012     -          outbound      EDS              
istio-eastwestgateway.istio-system.svc.cluster.local        15017     -          outbound      EDS              
istio-eastwestgateway.istio-system.svc.cluster.local        15021     -          outbound      EDS              
istio-eastwestgateway.istio-system.svc.cluster.local        15443     -          outbound      EDS              
istio-egressgateway.istio-system.svc.cluster.local          80        -          outbound      EDS              
istio-egressgateway.istio-system.svc.cluster.local          443       -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local         80        -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local         443       -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local         9000      -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local         15021     -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local         15443     -          outbound      EDS              
istio-ingressgateway.istio-system.svc.cluster.local         31400     -          outbound      EDS              
istiod.istio-system.svc.cluster.local                       443       -          outbound      EDS              
istiod.istio-system.svc.cluster.local                       15010     -          outbound      EDS              
istiod.istio-system.svc.cluster.local                       15012     -          outbound      EDS              
istiod.istio-system.svc.cluster.local                       15014     -          outbound      EDS              
kube-dns-upstream.kube-system.svc.cluster.local             53        -          outbound      EDS              
kube-dns.kube-system.svc.cluster.local                      53        -          outbound      EDS              
kube-dns.kube-system.svc.cluster.local                      9153      -          outbound      EDS              
kubernetes-dashboard.kube-system.svc.cluster.local          443       -          outbound      EDS              
kubernetes.default.svc.cluster.local                        443       -          outbound      EDS              
metrics-server.kube-system.svc.cluster.local                443       -          outbound      EDS              
my-nginx-v2.istio.svc.cluster.local                         443       -          outbound      EDS              
my-nginx.default.svc.cluster.local                          443       -          outbound      EDS              
my-nginx.istio.svc.cluster.local                            443       -          outbound      EDS              
node-local-dns.kube-system.svc.cluster.local                9253      -          outbound      ORIGINAL_DST     
productpage-v2.istio.svc.cluster.local                      9080      -          outbound      EDS              
productpage.istio.svc.cluster.local                         9080      -          outbound      EDS              
prometheus_stats                                            -         -          -             STATIC           
ratings.istio.svc.cluster.local                             9080      -          outbound      EDS              
reviews.istio.svc.cluster.local                             9080      -          outbound      EDS              
sds-grpc                                                    -         -          -             STATIC           
sleep.default.svc.cluster.local                             80        -          outbound      EDS              
tcp-echo.istio.svc.cluster.local                            9000      -          outbound      EDS              
tcp-echo.istio.svc.cluster.local                            9001      -          outbound      EDS              
xds-grpc                                                    -         -          -             STATIC           
zipkin                                                      -         -          -             STRICT_DNS
```

```
[root@master01 istioctl]# istioctl proxy-config clusters productpage-v1-85c4dcbb4c-gsjgw.istio --fqdn details.istio.svc.cluster.local 
SERVICE FQDN                        PORT     SUBSET     DIRECTION     TYPE     DESTINATION RULE
details.istio.svc.cluster.local     9080     -          outbound      EDS 
```

```
[root@master01 istioctl]# istioctl proxy-config clusters productpage-v1-85c4dcbb4c-gsjgw.istio --fqdn details.istio.svc.cluster.local --direction outbound 
SERVICE FQDN                        PORT     SUBSET     DIRECTION     TYPE     DESTINATION RULE
details.istio.svc.cluster.local     9080     -          outbound      EDS

istioctl proxy-config clusters productpage-v1-85c4dcbb4c-gsjgw.istio --port 9080

istioctl proxy-config clusters productpage-v1-85c4dcbb4c-gsjgw.istio  --subset v1
```



## endpoint

```
[root@node01 ~]# istioctl pc endpoint --help
Retrieve information about endpoint configuration for the Envoy instance in the specified pod.

Usage:
  istioctl proxy-config endpoint [<type>/]<name>[.<namespace>] [flags]

Aliases:
  endpoint, endpoints, ep

Examples:
  # Retrieve full endpoint configuration for a given pod from Envoy.
  istioctl proxy-config endpoint <pod-name[.namespace]>

  # Retrieve endpoint summary for endpoint with port 9080.
  istioctl proxy-config endpoint <pod-name[.namespace]> --port 9080

  # Retrieve full endpoint with a address (172.17.0.2).
  istioctl proxy-config endpoint <pod-name[.namespace]> --address 172.17.0.2 -o json

  # Retrieve full endpoint with a cluster name (outbound|9411||zipkin.istio-system.svc.cluster.local).
  istioctl proxy-config endpoint <pod-name[.namespace]> --cluster "outbound|9411||zipkin.istio-system.svc.cluster.local" -o json
  # Retrieve full endpoint with the status (healthy).
  istioctl proxy-config endpoint <pod-name[.namespace]> --status healthy -ojson

  # Retrieve endpoint summary without using Kubernetes API
  ssh <user@hostname> 'curl localhost:15000/clusters?format=json' > envoy-clusters.json
  istioctl proxy-config endpoints --file envoy-clusters.json


Flags:
      --address string   Filter endpoints by address field
      --cluster string   Filter endpoints by cluster name field
  -f, --file string      Envoy config dump JSON file
  -h, --help             help for endpoint
      --port int         Filter endpoints by Port field
      --status string    Filter endpoints by status field

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
  -o, --output string           Output format: one of json|yaml|short (default "short")
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl proxy-config endpoints --file rating_config_dump.json 
ENDPOINT     STATUS     OUTLIER CHECK     CLUSTER
```

```
[root@master01 istioctl]# istioctl proxy-config endpoint productpage-v1-85c4dcbb4c-gsjgw.istio --status healthy
ENDPOINT                         STATUS      OUTLIER CHECK     CLUSTER
127.0.0.1:15000                  HEALTHY     OK                prometheus_stats
127.0.0.1:15020                  HEALTHY     OK                agent
172.20.0.160:9080                HEALTHY     OK                outbound|9080||ratings.istio.svc.cluster.local
172.20.0.161:9080                HEALTHY     OK                outbound|9080||reviews.istio.svc.cluster.local
172.20.0.162:9080                HEALTHY     OK                outbound|9080||details.istio.svc.cluster.local
172.20.0.163:9000                HEALTHY     OK                outbound|9000||tcp-echo.istio.svc.cluster.local
172.20.0.163:9001                HEALTHY     OK                outbound|9001||tcp-echo.istio.svc.cluster.local
172.20.0.164:9080                HEALTHY     OK                outbound|9080||productpage-v2.istio.svc.cluster.local
172.20.0.169:9080                HEALTHY     OK                outbound|9080||bookinfo.default.svc.cluster.local
172.20.0.169:9080                HEALTHY     OK                outbound|9080||productpage.default.svc.cluster.local
172.20.1.179:9000                HEALTHY     OK                outbound|9000||tcp-echo.istio.svc.cluster.local
172.20.1.179:9001                HEALTHY     OK                outbound|9001||tcp-echo.istio.svc.cluster.local
172.20.1.180:443                 HEALTHY     OK                outbound|443||my-nginx.istio.svc.cluster.local
172.20.1.181:4443                HEALTHY     OK                outbound|443||metrics-server.kube-system.svc.cluster.local
172.20.1.182:9080                HEALTHY     OK                outbound|9080||productpage.istio.svc.cluster.local
172.20.1.183:53                  HEALTHY     OK                outbound|53||kube-dns-upstream.kube-system.svc.cluster.local
172.20.1.183:53                  HEALTHY     OK                outbound|53||kube-dns.kube-system.svc.cluster.local
172.20.1.183:9153                HEALTHY     OK                outbound|9153||kube-dns.kube-system.svc.cluster.local
172.20.1.196:15010               HEALTHY     OK                outbound|15010||istiod.istio-system.svc.cluster.local
172.20.1.196:15012               HEALTHY     OK                outbound|15012||istiod.istio-system.svc.cluster.local
172.20.1.196:15014               HEALTHY     OK                outbound|15014||istiod.istio-system.svc.cluster.local
172.20.1.196:15017               HEALTHY     OK                outbound|443||istiod.istio-system.svc.cluster.local
172.20.2.142:443                 HEALTHY     OK                outbound|443||my-nginx-v2.istio.svc.cluster.local
172.20.2.144:8000                HEALTHY     OK                outbound|8000||dashboard-metrics-scraper.kube-system.svc.cluster.local
172.20.2.145:9080                HEALTHY     OK                outbound|9080||reviews.istio.svc.cluster.local
172.20.2.146:9080                HEALTHY     OK                outbound|9080||reviews.istio.svc.cluster.local
172.20.2.148:9000                HEALTHY     OK                outbound|9000||tcp-echo.istio.svc.cluster.local
172.20.2.148:9001                HEALTHY     OK                outbound|9001||tcp-echo.istio.svc.cluster.local
172.20.2.153:15010               HEALTHY     OK                outbound|15010||istiod-1-10-0.istio-system.svc.cluster.local
172.20.2.153:15012               HEALTHY     OK                outbound|15012||istiod-1-10-0.istio-system.svc.cluster.local
172.20.2.153:15014               HEALTHY     OK                outbound|15014||istiod-1-10-0.istio-system.svc.cluster.local
172.20.2.153:15017               HEALTHY     OK                outbound|443||istiod-1-10-0.istio-system.svc.cluster.local
172.20.2.154:8080                HEALTHY     OK                outbound|80||istio-egressgateway.istio-system.svc.cluster.local
172.20.2.154:8443                HEALTHY     OK                outbound|443||istio-egressgateway.istio-system.svc.cluster.local
172.20.2.155:8080                HEALTHY     OK                outbound|80||istio-ingressgateway.istio-system.svc.cluster.local
172.20.2.155:8443                HEALTHY     OK                outbound|443||istio-ingressgateway.istio-system.svc.cluster.local
172.20.2.155:15021               HEALTHY     OK                outbound|15021||istio-ingressgateway.istio-system.svc.cluster.local
172.20.2.155:15443               HEALTHY     OK                outbound|15443||istio-ingressgateway.istio-system.svc.cluster.local
172.20.2.155:31400               HEALTHY     OK                outbound|31400||istio-ingressgateway.istio-system.svc.cluster.local
192.168.198.154:6443             HEALTHY     OK                outbound|443||kubernetes.default.svc.cluster.local
192.168.198.155:6443             HEALTHY     OK                outbound|443||kubernetes.default.svc.cluster.local
unix://./etc/istio/proxy/SDS     HEALTHY     OK                sds-grpc
unix://./etc/istio/proxy/XDS     HEALTHY     OK                xds-grpc
```

```
[root@master01 istioctl]# istioctl proxy-config endpoint productpage-v1-85c4dcbb4c-gsjgw.istio --cluster "outbound|9080||productpage.istio.svc.cluster.local" -o json
[
    {
        "name": "outbound|9080||productpage.istio.svc.cluster.local",
        "addedViaApi": true,
        "hostStatuses": [
            {
                "address": {
                    "socketAddress": {
                        "address": "172.20.1.182",
                        "portValue": 9080
                    }
                },
                "stats": [
                    {
                        "name": "cx_connect_fail"
                    },
                    {
                        "name": "cx_total"
                    },
                    {
                        "name": "rq_error"
                    },
                    {
                        "name": "rq_success"
                    },
                    {
                        "name": "rq_timeout"
                    },
                    {
                        "name": "rq_total"
                    },
                    {
                        "type": "GAUGE",
                        "name": "cx_active"
                    },
                    {
                        "type": "GAUGE",
                        "name": "rq_active"
                    }
                ],
                "healthStatus": {
                    "edsHealthStatus": "HEALTHY"
                },
                "weight": 1,
                "locality": {
                    "region": "us-central2",
                    "zone": "z2",
                    "subZone": "sz02"
                }
            }
        ],
        "circuitBreakers": {
            "thresholds": [
                {
                    "maxConnections": 4294967295,
                    "maxPendingRequests": 4294967295,
                    "maxRequests": 4294967295,
                    "maxRetries": 4294967295
                },
                {
                    "priority": "HIGH",
                    "maxConnections": 1024,
                    "maxPendingRequests": 1024,
                    "maxRequests": 1024,
                    "maxRetries": 3
                }
            ]
        },
        "observabilityName": "outbound|9080||productpage.istio.svc.cluster.local"
    }
]
```

```
[root@node01 istioctl]# istioctl proxy-config endpoint productpage-v1-85c4dcbb4c-gsjgw.istio --address 172.20.0.20 -o json
[
    {
        "name": "outbound|53||kube-dns.kube-system.svc.cluster.local",
        "addedViaApi": true,
        "hostStatuses": [
            {
                "address": {
                    "socketAddress": {
                        "address": "172.20.0.20",
                        "portValue": 53
                    }
                },
                "stats": [
                    {
                        "name": "cx_connect_fail"
                    },
                    {
                        "name": "cx_total"
                    },
                    {
                        "name": "rq_error"
                    },
                    {
                        "name": "rq_success"
                    },
                    {
                        "name": "rq_timeout"
                    },
                    {
                        "name": "rq_total"
                    },
                    {
                        "type": "GAUGE",
                        "name": "cx_active"
                    },
                    {
                        "type": "GAUGE",
                        "name": "rq_active"
                    }
                ],
                "healthStatus": {
                    "edsHealthStatus": "HEALTHY"
                },
                "weight": 1,
                "locality": {}
            }
        ],
        "circuitBreakers": {
            "thresholds": [
                {
                    "maxConnections": 4294967295,
                    "maxPendingRequests": 4294967295,
                    "maxRequests": 4294967295,
                    "maxRetries": 4294967295
                },
                {
                    "priority": "HIGH",
                    "maxConnections": 1024,
                    "maxPendingRequests": 1024,
                    "maxRequests": 1024,
                    "maxRetries": 3
                }
            ]
        },
        "observabilityName": "outbound|53||kube-dns.kube-system.svc.cluster.local"
    },
    {
        "name": "outbound|9153||kube-dns.kube-system.svc.cluster.local",
        "addedViaApi": true,
        "hostStatuses": [
            {
                "address": {
                    "socketAddress": {
                        "address": "172.20.0.20",
                        "portValue": 9153
                    }
                },
                "stats": [
                    {
                        "name": "cx_connect_fail"
                    },
                    {
                        "name": "cx_total"
                    },
                    {
                        "name": "rq_error"
                    },
                    {
                        "name": "rq_success"
                    },
                    {
                        "name": "rq_timeout"
                    },
                    {
                        "name": "rq_total"
                    },
                    {
                        "type": "GAUGE",
                        "name": "cx_active"
                    },
                    {
                        "type": "GAUGE",
                        "name": "rq_active"
                    }
                ],
                "healthStatus": {
                    "edsHealthStatus": "HEALTHY"
                },
                "weight": 1,
                "locality": {}
            }
        ],
        "circuitBreakers": {
            "thresholds": [
                {
                    "maxConnections": 4294967295,
                    "maxPendingRequests": 4294967295,
                    "maxRequests": 4294967295,
                    "maxRetries": 4294967295
                },
                {
                    "priority": "HIGH",
                    "maxConnections": 1024,
                    "maxPendingRequests": 1024,
                    "maxRequests": 1024,
                    "maxRetries": 3
                }
            ]
        },
        "observabilityName": "outbound|9153||kube-dns.kube-system.svc.cluster.local"
    }
]
```

```
 istioctl proxy-config endpoint productpage-v1-85c4dcbb4c-gsjgw.istio --port 9080
```



## listener

```
[root@node01 ~]# istioctl pc listener --help
Retrieve information about listener configuration for the Envoy instance in the specified pod.

Usage:
  istioctl proxy-config listener [<type>/]<name>[.<namespace>] [flags]

Aliases:
  listener, listeners, l

Examples:
  # Retrieve summary about listener configuration for a given pod from Envoy.
  istioctl proxy-config listeners <pod-name[.namespace]>

  # Retrieve listener summary for listeners with port 9080.
  istioctl proxy-config listeners <pod-name[.namespace]> --port 9080

  # Retrieve full listener dump for HTTP listeners with a wildcard address (0.0.0.0).
  istioctl proxy-config listeners <pod-name[.namespace]> --type HTTP --address 0.0.0.0 -o json

  # Retrieve listener summary without using Kubernetes API
  ssh <user@hostname> 'curl localhost:15000/config_dump' > envoy-config.json
  istioctl proxy-config listeners --file envoy-config.json


Flags:
      --address string   Filter listeners by address field
  -f, --file string      Envoy config dump JSON file
  -h, --help             help for listener
      --port int         Filter listeners by Port field
      --type string      Filter listeners by type field
      --verbose          Output more information (default true)

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
  -o, --output string           Output format: one of json|yaml|short (default "short")
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl proxy-config listeners --file rating_config_dump.json 
ADDRESS         PORT  MATCH                                                                                           DESTINATION
10.68.0.2       53    ALL                                                                                             Cluster: outbound|53||kube-dns.kube-system.svc.cluster.local
10.68.32.248    53    ALL                                                                                             Cluster: outbound|53||kube-dns-upstream.kube-system.svc.cluster.local
0.0.0.0         80    Trans: raw_buffer; App: HTTP                                                                    Route: 80
0.0.0.0         80    ALL                                                                                             PassthroughCluster
10.68.0.1       443   ALL                                                                                             Cluster: outbound|443||kubernetes.default.svc.cluster.local
10.68.107.226   443   Trans: raw_buffer; App: HTTP                                                                    Route: metrics-server.kube-system.svc.cluster.local:443
10.68.107.226   443   ALL                                                                                             Cluster: outbound|443||metrics-server.kube-system.svc.cluster.local
10.68.142.72    443   ALL                                                                                             Cluster: outbound|443||istiod.istio-system.svc.cluster.local
10.68.174.221   443   ALL                                                                                             Cluster: outbound|443||my-nginx.istio.svc.cluster.local
10.68.185.150   443   Trans: raw_buffer; App: HTTP                                                                    Route: kubernetes-dashboard.kube-system.svc.cluster.local:443
10.68.185.150   443   ALL                                                                                             Cluster: outbound|443||kubernetes-dashboard.kube-system.svc.cluster.local
10.68.4.87      443   ALL                                                                                             Cluster: outbound|443||istio-egressgateway.istio-system.svc.cluster.local
10.68.71.43     443   ALL                                                                                             Cluster: outbound|443||my-nginx-v2.istio.svc.cluster.local
10.68.75.102    443   ALL                                                                                             Cluster: outbound|443||istio-ingressgateway.istio-system.svc.cluster.local
10.68.93.209    443   ALL                                                                                             Cluster: outbound|443||my-nginx.default.svc.cluster.local
10.68.89.158    8000  Trans: raw_buffer; App: HTTP                                                                    Route: dashboard-metrics-scraper.kube-system.svc.cluster.local:8000
10.68.89.158    8000  ALL                                                                                             Cluster: outbound|8000||dashboard-metrics-scraper.kube-system.svc.cluster.local
0.0.0.0         8012  Trans: raw_buffer; App: HTTP                                                                    Route: 8012
0.0.0.0         8012  ALL                                                                                             PassthroughCluster
0.0.0.0         8022  Trans: raw_buffer; App: HTTP                                                                    Route: 8022
0.0.0.0         8022  ALL                                                                                             PassthroughCluster
10.68.175.57    9000  ALL                                                                                             Cluster: outbound|9000||tcp-echo.istio.svc.cluster.local
10.68.75.102    9000  ALL                                                                                             Cluster: outbound|9000||istio-ingressgateway.istio-system.svc.cluster.local
10.68.175.57    9001  ALL                                                                                             Cluster: outbound|9001||tcp-echo.istio.svc.cluster.local
0.0.0.0         9080  Trans: raw_buffer; App: HTTP                                                                    Route: 9080
0.0.0.0         9080  ALL                                                                                             PassthroughCluster
0.0.0.0         9090  Trans: raw_buffer; App: HTTP                                                                    Route: 9090
0.0.0.0         9090  ALL                                                                                             PassthroughCluster
0.0.0.0         9091  Trans: raw_buffer; App: HTTP                                                                    Route: 9091
0.0.0.0         9091  ALL                                                                                             PassthroughCluster
10.68.0.2       9153  Trans: raw_buffer; App: HTTP                                                                    Route: kube-dns.kube-system.svc.cluster.local:9153
10.68.0.2       9153  ALL                                                                                             Cluster: outbound|9153||kube-dns.kube-system.svc.cluster.local
192.168.198.154 9253  Trans: raw_buffer; App: HTTP                                                                    Route: node-local-dns.kube-system.svc.cluster.local:9253
192.168.198.154 9253  ALL                                                                                             Cluster: outbound|9253||node-local-dns.kube-system.svc.cluster.local
192.168.198.155 9253  Trans: raw_buffer; App: HTTP                                                                    Route: node-local-dns.kube-system.svc.cluster.local:9253
192.168.198.155 9253  ALL                                                                                             Cluster: outbound|9253||node-local-dns.kube-system.svc.cluster.local
192.168.198.156 9253  Trans: raw_buffer; App: HTTP                                                                    Route: node-local-dns.kube-system.svc.cluster.local:9253
192.168.198.156 9253  ALL                                                                                             Cluster: outbound|9253||node-local-dns.kube-system.svc.cluster.local
0.0.0.0         15001 ALL                                                                                             PassthroughCluster
0.0.0.0         15001 Addr: *:15001                                                                                   Non-HTTP/Non-TCP
0.0.0.0         15006 Addr: *:15006                                                                                   Non-HTTP/Non-TCP
0.0.0.0         15006 Trans: tls; App: istio-http/1.0,istio-http/1.1,istio-h2; Addr: 0.0.0.0/0                        InboundPassthroughClusterIpv4
0.0.0.0         15006 Trans: raw_buffer; App: HTTP; Addr: 0.0.0.0/0                                                   InboundPassthroughClusterIpv4
0.0.0.0         15006 Trans: tls; App: TCP TLS; Addr: 0.0.0.0/0                                                       InboundPassthroughClusterIpv4
0.0.0.0         15006 Trans: raw_buffer; Addr: 0.0.0.0/0                                                              InboundPassthroughClusterIpv4
0.0.0.0         15006 Trans: tls; Addr: 0.0.0.0/0                                                                     InboundPassthroughClusterIpv4
0.0.0.0         15006 Trans: tls; App: istio,istio-peer-exchange,istio-http/1.0,istio-http/1.1,istio-h2; Addr: *:9080 Cluster: inbound|9080||
0.0.0.0         15006 Trans: raw_buffer; Addr: *:9080                                                                 Cluster: inbound|9080||
0.0.0.0         15010 Trans: raw_buffer; App: HTTP                                                                    Route: 15010
0.0.0.0         15010 ALL                                                                                             PassthroughCluster
10.68.142.72    15012 ALL                                                                                             Cluster: outbound|15012||istiod.istio-system.svc.cluster.local
10.68.248.170   15012 ALL                                                                                             Cluster: outbound|15012||istio-eastwestgateway.istio-system.svc.cluster.local
0.0.0.0         15014 Trans: raw_buffer; App: HTTP                                                                    Route: 15014
0.0.0.0         15014 ALL                                                                                             PassthroughCluster
10.68.248.170   15017 ALL                                                                                             Cluster: outbound|15017||istio-eastwestgateway.istio-system.svc.cluster.local
0.0.0.0         15021 ALL                                                                                             Inline Route: /healthz/ready*
10.68.248.170   15021 Trans: raw_buffer; App: HTTP                                                                    Route: istio-eastwestgateway.istio-system.svc.cluster.local:15021
10.68.248.170   15021 ALL                                                                                             Cluster: outbound|15021||istio-eastwestgateway.istio-system.svc.cluster.local
10.68.75.102    15021 Trans: raw_buffer; App: HTTP                                                                    Route: istio-ingressgateway.istio-system.svc.cluster.local:15021
10.68.75.102    15021 ALL                                                                                             Cluster: outbound|15021||istio-ingressgateway.istio-system.svc.cluster.local
0.0.0.0         15090 ALL                                                                                             Inline Route: /stats/prometheus*
10.68.248.170   15443 ALL                                                                                             Cluster: outbound|15443||istio-eastwestgateway.istio-system.svc.cluster.local
10.68.75.102    15443 ALL                                                                                             Cluster: outbound|15443||istio-ingressgateway.istio-system.svc.cluster.local
10.68.75.102    31400 ALL                                                                                             Cluster: outbound|31400||istio-ingressgateway.istio-system.svc.cluster.local
```

```
[root@master01 istioctl]# istioctl proxy-config listeners productpage-v1-85c4dcbb4c-gsjgw.istio --type HTTP --address 0.0.0.0 
ADDRESS PORT  MATCH DESTINATION
0.0.0.0 15021 ALL   Inline Route: /healthz/ready*
0.0.0.0 15090 ALL   Inline Route: /stats/prometheus*
```

```
[root@master01 istioctl]# istioctl proxy-config listeners productpage-v1-85c4dcbb4c-gsjgw.istio --port 9080
ADDRESS PORT MATCH                        DESTINATION
0.0.0.0 9080 Trans: raw_buffer; App: HTTP Route: 9080
0.0.0.0 9080 ALL                          PassthroughCluster
```



## log

```
[root@node01 ~]# istioctl pc log --help
(experimental) Retrieve information about logging levels of the Envoy instance in the specified pod, and update optionally

Usage:
  istioctl proxy-config log [<type>/]<name>[.<namespace>] [flags]

Aliases:
  log, o

Examples:
  # Retrieve information about logging levels for a given pod from Envoy.
  istioctl proxy-config log <pod-name[.namespace]>

  # Update levels of the all loggers
  istioctl proxy-config log <pod-name[.namespace]> --level none

  # Update levels of the specified loggers.
  istioctl proxy-config log <pod-name[.namespace]> --level http:debug,redis:debug

  # Reset levels of all the loggers to default value (warning).
  istioctl proxy-config log <pod-name[.namespace]> -r


Flags:
  -h, --help              help for log
      --level string      Comma-separated minimum per-logger level of messages to output, in the form of [<logger>:]<level>,[<logger>:]<level>,... where logger can be one of admin, aws, assert, backtrace, client, config, connection, conn_handler, dubbo, file, filter, forward_proxy, grpc, hc, health_checker, http, http2, hystrix, init, io, jwt, kafka, lua, main, misc, mongo, quic, pool, rbac, redis, router, runtime, stats, secret, tap, testing, thrift, tracing, upstream, udp, wasm and level can be one of [trace, debug, info, warning, error, critical, off]
  -r, --reset             Reset levels to default value (warning).
  -l, --selector string   Label selector

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
  -o, --output string           Output format: one of json|yaml|short (default "short")
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl proxy-config log productpage-v1-85c4dcbb4c-gsjgw.istio 
active loggers:
  admin: warning
  aws: warning
  assert: warning
  backtrace: warning
  cache_filter: warning
  client: warning
  config: warning
  connection: warning
  conn_handler: warning
  decompression: warning
  dubbo: warning
  envoy_bug: warning
  ext_authz: warning
  rocketmq: warning
  file: warning
  filter: warning
  forward_proxy: warning
  grpc: warning
  hc: warning
  health_checker: warning
  http: warning
  http2: warning
  hystrix: warning
  init: warning
  io: warning
  jwt: warning
  kafka: warning
  lua: warning
  main: warning
  matcher: warning
  misc: error
  mongo: warning
  quic: warning
  quic_stream: warning
  pool: warning
  rbac: warning
  redis: warning
  router: warning
  runtime: warning
  stats: warning
  secret: warning
  tap: warning
  testing: warning
  thrift: warning
  tracing: warning
  upstream: warning
  udp: warning
  wasm: warning
```

```
[root@master01 istioctl]# istioctl proxy-config log productpage-v1-85c4dcbb4c-gsjgw.istio  --level http:debug,redis:debug
active loggers:
  admin: warning
  aws: warning
  assert: warning
  backtrace: warning
  cache_filter: warning
  client: warning
  config: warning
  connection: warning
  conn_handler: warning
  decompression: warning
  dubbo: warning
  envoy_bug: warning
  ext_authz: warning
  rocketmq: warning
  file: warning
  filter: warning
  forward_proxy: warning
  grpc: warning
  hc: warning
  health_checker: warning
  http: debug
  http2: warning
  hystrix: warning
  init: warning
  io: warning
  jwt: warning
  kafka: warning
  lua: warning
  main: warning
  matcher: warning
  misc: error
  mongo: warning
  quic: warning
  quic_stream: warning
  pool: warning
  rbac: warning
  redis: debug
  router: warning
  runtime: warning
  stats: warning
  secret: warning
  tap: warning
  testing: warning
  thrift: warning
  tracing: warning
  upstream: warning
  udp: warning
  wasm: warning
```

```
[root@master01 istioctl]# istioctl proxy-config log productpage-v1-85c4dcbb4c-gsjgw.istio  --reset 
2021-08-27T10:35:30.263861Z     warn    unable to get logLevel from ConfigMap istio-sidecar-injector, using default value: warning
active loggers:
  admin: warning
  aws: warning
  assert: warning
  backtrace: warning
  cache_filter: warning
  client: warning
  config: warning
  connection: warning
  conn_handler: warning
  decompression: warning
  dubbo: warning
  envoy_bug: warning
  ext_authz: warning
  rocketmq: warning
  file: warning
  filter: warning
  forward_proxy: warning
  grpc: warning
  hc: warning
  health_checker: warning
  http: warning
  http2: warning
  hystrix: warning
  init: warning
  io: warning
  jwt: warning
  kafka: warning
  lua: warning
  main: warning
  matcher: warning
  misc: warning
  mongo: warning
  quic: warning
  quic_stream: warning
  pool: warning
  rbac: warning
  redis: warning
  router: warning
  runtime: warning
  stats: warning
  secret: warning
  tap: warning
  testing: warning
  thrift: warning
  tracing: warning
  upstream: warning
  udp: warning
  wasm: warning
```



## route

```
[root@node01 ~]# istioctl pc route --help
Retrieve information about route configuration for the Envoy instance in the specified pod.

Usage:
  istioctl proxy-config route [<type>/]<name>[.<namespace>] [flags]

Aliases:
  route, routes, r

Examples:
  # Retrieve summary about route configuration for a given pod from Envoy.
  istioctl proxy-config routes <pod-name[.namespace]>

  # Retrieve route summary for route 9080.
  istioctl proxy-config route <pod-name[.namespace]> --name 9080

  # Retrieve full route dump for route 9080
  istioctl proxy-config route <pod-name[.namespace]> --name 9080 -o json

  # Retrieve route summary without using Kubernetes API
  ssh <user@hostname> 'curl localhost:15000/config_dump' > envoy-config.json
  istioctl proxy-config routes --file envoy-config.json


Flags:
  -f, --file string   Envoy config dump JSON file
  -h, --help          help for route
      --name string   Filter listeners by route name field
      --verbose       Output more information (default true)

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
  -o, --output string           Output format: one of json|yaml|short (default "short")
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl proxy-config routes --file rating_config_dump.json 
NAME                                                             DOMAINS                                                      MATCH                  VIRTUAL SERVICE
80                                                               helloworld-go-00001-private.knative, 10.68.87.179            /*                     
80                                                               helloworld-go-00001.knative, 10.68.140.219                   /*                     
80                                                               helloworld-go-00002-private.knative, 10.68.111.141           /*                     
80                                                               helloworld-go-00002.knative, 10.68.143.0                     /*                     
80                                                               helloworld-go.knative                                        /*                     
80                                                               istio-egressgateway.istio-system, 10.68.4.87                 /*                     
80                                                               istio-ingressgateway.istio-system, 10.68.75.102              /*                     
80                                                               sleep.default, 10.68.184.151                                 /*                     
8012                                                             helloworld-go-00001-private.knative, 10.68.87.179            /*                     
8012                                                             helloworld-go-00002-private.knative, 10.68.111.141           /*                     
8022                                                             helloworld-go-00001-private.knative, 10.68.87.179            /*                     
8022                                                             helloworld-go-00002-private.knative, 10.68.111.141           /*                     
9080                                                             bookinfo.default, 10.68.63.49                                /*                     
9080                                                             details.default, 10.68.129.0                                 /*                     
9080                                                             details, details.istio + 1 more...                           /*                     
9080                                                             productpage-v2, productpage-v2.istio + 1 more...             /*                     
9080                                                             productpage, productpage.istio + 1 more...                   /*                     
9080                                                             ratings, ratings.istio + 1 more...                           /*                     
9080                                                             reviews, reviews.istio + 1 more...                           /*                     
9090                                                             helloworld-go-00001-private.knative, 10.68.87.179            /*                     
9090                                                             helloworld-go-00002-private.knative, 10.68.111.141           /*                     
9091                                                             helloworld-go-00001-private.knative, 10.68.87.179            /*                     
9091                                                             helloworld-go-00002-private.knative, 10.68.111.141           /*                     
15010                                                            istiod.istio-system, 10.68.142.72                            /*                     
15014                                                            istiod.istio-system, 10.68.142.72                            /*                     
kubernetes-dashboard.kube-system.svc.cluster.local:443           kubernetes-dashboard.kube-system, 10.68.185.150              /*                     
metrics-server.kube-system.svc.cluster.local:443                 metrics-server.kube-system, 10.68.107.226                    /*                     
istio-eastwestgateway.istio-system.svc.cluster.local:15021       istio-eastwestgateway.istio-system, 10.68.248.170            /*                     
dashboard-metrics-scraper.kube-system.svc.cluster.local:8000     dashboard-metrics-scraper.kube-system, 10.68.89.158          /*                     
node-local-dns.kube-system.svc.cluster.local:9253                node-local-dns.kube-system, *.node-local-dns.kube-system     /*                     
kube-dns.kube-system.svc.cluster.local:9153                      kube-dns.kube-system, 10.68.0.2                              /*                     
istio-ingressgateway.istio-system.svc.cluster.local:15021        istio-ingressgateway.istio-system, 10.68.75.102              /*                     
InboundPassthroughClusterIpv4                                    *                                                            /*                     
InboundPassthroughClusterIpv4                                    *                                                            /*                     
                                                                 *                                                            /stats/prometheus*     
inbound|9080||                                                   *                                                            /*                     
inbound|9080||                                                   *                                                            /*                     
                                                                 *                                                            /healthz/ready*
```

```
[root@master01 istioctl]# istioctl proxy-config route productpage-v1-85c4dcbb4c-gsjgw.istio  --name 9080 -o json
[
    {
        "name": "9080",
        "virtualHosts": [
            {
                "name": "allow_any",
                "domains": [
                    "*"
                ],
                "routes": [
                    {
                        "name": "allow_any",
                        "match": {
                            "prefix": "/"
                        },
                        "route": {
                            "cluster": "PassthroughCluster",
                            "timeout": "0s",
                            "maxGrpcTimeout": "0s"
                        }
                    }
                ],
                "includeRequestAttemptCount": true
            },
            {
                "name": "bookinfo.default.svc.cluster.local:9080",
                "domains": [
                    "bookinfo.default.svc.cluster.local",
                    "bookinfo.default.svc.cluster.local:9080",
                    "bookinfo.default",
                    "bookinfo.default:9080",
                    "bookinfo.default.svc",
                    "bookinfo.default.svc:9080",
                    "10.68.63.49",
                    "10.68.63.49:9080"
                ],
                "routes": [
                    {
                        "name": "default",
                        "match": {
                            "prefix": "/"
                        },
                        "route": {
                            "cluster": "outbound|9080||bookinfo.default.svc.cluster.local",
                            "timeout": "0s",
                            "retryPolicy": {
                                "retryOn": "connect-failure,refused-stream,unavailable,cancelled,retriable-status-codes",
                                "numRetries": 2,
                                "retryHostPredicate": [
                                    {
                                        "name": "envoy.retry_host_predicates.previous_hosts"
                                    }
                                ],
                                "hostSelectionRetryMaxAttempts": "5",
                                "retriableStatusCodes": [
                                    503
                                ]
                            },
                            "maxStreamDuration": {
                                "maxStreamDuration": "0s",
                                "grpcTimeoutHeaderMax": "0s"
                            }
                        },
                        "decorator": {
                            "operation": "bookinfo.default.svc.cluster.local:9080/*"
                        }
                    }
                ],
                "includeRequestAttemptCount": true
            },
            {
                "name": "details.default.svc.cluster.local:9080",
                "domains": [
                    "details.default.svc.cluster.local",
                    "details.default.svc.cluster.local:9080",
                    "details.default",
                    "details.default:9080",
                    "details.default.svc",
                    "details.default.svc:9080",
                    "10.68.129.0",
                    "10.68.129.0:9080"
                ],
                "routes": [
                    {
                        "name": "default",
                        "match": {
                            "prefix": "/"
                        },
                        "route": {
                            "cluster": "outbound|9080||details.default.svc.cluster.local",
                            "timeout": "0s",
                            "retryPolicy": {
                                "retryOn": "connect-failure,refused-stream,unavailable,cancelled,retriable-status-codes",
                                "numRetries": 2,
                                "retryHostPredicate": [
                                    {
                                        "name": "envoy.retry_host_predicates.previous_hosts"
                                    }
                                ],
                                "hostSelectionRetryMaxAttempts": "5",
                                "retriableStatusCodes": [
                                    503
                                ]
                            },
                            "maxStreamDuration": {
                                "maxStreamDuration": "0s",
                                "grpcTimeoutHeaderMax": "0s"
                            }
                        },
                        "decorator": {
                            "operation": "details.default.svc.cluster.local:9080/*"
                        }
                    }
                ],
                "includeRequestAttemptCount": true
            },
            {
                "name": "details.istio.svc.cluster.local:9080",
                "domains": [
                    "details.istio.svc.cluster.local",
                    "details.istio.svc.cluster.local:9080",
                    "details",
                    "details:9080",
                    "details.istio.svc",
                    "details.istio.svc:9080",
                    "details.istio",
                    "details.istio:9080",
                    "10.68.203.136",
                    "10.68.203.136:9080"
                ],
                "routes": [
                    {
                        "name": "default",
                        "match": {
                            "prefix": "/"
                        },
                        "route": {
                            "cluster": "outbound|9080||details.istio.svc.cluster.local",
                            "timeout": "0s",
                            "retryPolicy": {
                                "retryOn": "connect-failure,refused-stream,unavailable,cancelled,retriable-status-codes",
                                "numRetries": 2,
                                "retryHostPredicate": [
                                    {
                                        "name": "envoy.retry_host_predicates.previous_hosts"
                                    }
                                ],
                                "hostSelectionRetryMaxAttempts": "5",
                                "retriableStatusCodes": [
                                    503
                                ]
                            },
                            "maxStreamDuration": {
                                "maxStreamDuration": "0s",
                                "grpcTimeoutHeaderMax": "0s"
                            }
                        },
                        "decorator": {
                            "operation": "details.istio.svc.cluster.local:9080/*"
                        }
                    }
                ],
                "includeRequestAttemptCount": true
            },
            {
                "name": "productpage-v2.istio.svc.cluster.local:9080",
                "domains": [
                    "productpage-v2.istio.svc.cluster.local",
                    "productpage-v2.istio.svc.cluster.local:9080",
                    "productpage-v2",
                    "productpage-v2:9080",
                    "productpage-v2.istio.svc",
                    "productpage-v2.istio.svc:9080",
                    "productpage-v2.istio",
                    "productpage-v2.istio:9080",
                    "10.68.181.29",
                    "10.68.181.29:9080"
                ],
                "routes": [
                    {
                        "name": "default",
                        "match": {
                            "prefix": "/"
                        },
                        "route": {
                            "cluster": "outbound|9080||productpage-v2.istio.svc.cluster.local",
                            "timeout": "0s",
                            "retryPolicy": {
                                "retryOn": "connect-failure,refused-stream,unavailable,cancelled,retriable-status-codes",
                                "numRetries": 2,
                                "retryHostPredicate": [
                                    {
                                        "name": "envoy.retry_host_predicates.previous_hosts"
                                    }
                                ],
                                "hostSelectionRetryMaxAttempts": "5",
                                "retriableStatusCodes": [
                                    503
                                ]
                            },
                            "maxStreamDuration": {
                                "maxStreamDuration": "0s",
                                "grpcTimeoutHeaderMax": "0s"
                            }
                        },
                        "decorator": {
                            "operation": "productpage-v2.istio.svc.cluster.local:9080/*"
                        }
                    }
                ],
                "includeRequestAttemptCount": true
            },
            {
                "name": "productpage.default.svc.cluster.local:9080",
                "domains": [
                    "productpage.default.svc.cluster.local",
                    "productpage.default.svc.cluster.local:9080",
                    "productpage.default",
                    "productpage.default:9080",
                    "productpage.default.svc",
                    "productpage.default.svc:9080",
                    "10.68.27.236",
                    "10.68.27.236:9080"
                ],
                "routes": [
                    {
                        "name": "default",
                        "match": {
                            "prefix": "/"
                        },
                        "route": {
                            "cluster": "outbound|9080||productpage.default.svc.cluster.local",
                            "timeout": "0s",
                            "retryPolicy": {
                                "retryOn": "connect-failure,refused-stream,unavailable,cancelled,retriable-status-codes",
                                "numRetries": 2,
                                "retryHostPredicate": [
                                    {
                                        "name": "envoy.retry_host_predicates.previous_hosts"
                                    }
                                ],
                                "hostSelectionRetryMaxAttempts": "5",
                                "retriableStatusCodes": [
                                    503
                                ]
                            },
                            "maxStreamDuration": {
                                "maxStreamDuration": "0s",
                                "grpcTimeoutHeaderMax": "0s"
                            }
                        },
                        "decorator": {
                            "operation": "productpage.default.svc.cluster.local:9080/*"
                        }
                    }
                ],
                "includeRequestAttemptCount": true
            },
            {
                "name": "productpage.istio.svc.cluster.local:9080",
                "domains": [
                    "productpage.istio.svc.cluster.local",
                    "productpage.istio.svc.cluster.local:9080",
                    "productpage",
                    "productpage:9080",
                    "productpage.istio.svc",
                    "productpage.istio.svc:9080",
                    "productpage.istio",
                    "productpage.istio:9080",
                    "10.68.191.45",
                    "10.68.191.45:9080"
                ],
                "routes": [
                    {
                        "name": "default",
                        "match": {
                            "prefix": "/"
                        },
                        "route": {
                            "cluster": "outbound|9080||productpage.istio.svc.cluster.local",
                            "timeout": "0s",
                            "retryPolicy": {
                                "retryOn": "connect-failure,refused-stream,unavailable,cancelled,retriable-status-codes",
                                "numRetries": 2,
                                "retryHostPredicate": [
                                    {
                                        "name": "envoy.retry_host_predicates.previous_hosts"
                                    }
                                ],
                                "hostSelectionRetryMaxAttempts": "5",
                                "retriableStatusCodes": [
                                    503
                                ]
                            },
                            "maxStreamDuration": {
                                "maxStreamDuration": "0s",
                                "grpcTimeoutHeaderMax": "0s"
                            }
                        },
                        "decorator": {
                            "operation": "productpage.istio.svc.cluster.local:9080/*"
                        }
                    }
                ],
                "includeRequestAttemptCount": true
            },
            {
                "name": "ratings.istio.svc.cluster.local:9080",
                "domains": [
                    "ratings.istio.svc.cluster.local",
                    "ratings.istio.svc.cluster.local:9080",
                    "ratings",
                    "ratings:9080",
                    "ratings.istio.svc",
                    "ratings.istio.svc:9080",
                    "ratings.istio",
                    "ratings.istio:9080",
                    "10.68.83.113",
                    "10.68.83.113:9080"
                ],
                "routes": [
                    {
                        "name": "default",
                        "match": {
                            "prefix": "/"
                        },
                        "route": {
                            "cluster": "outbound|9080||ratings.istio.svc.cluster.local",
                            "timeout": "0s",
                            "retryPolicy": {
                                "retryOn": "connect-failure,refused-stream,unavailable,cancelled,retriable-status-codes",
                                "numRetries": 2,
                                "retryHostPredicate": [
                                    {
                                        "name": "envoy.retry_host_predicates.previous_hosts"
                                    }
                                ],
                                "hostSelectionRetryMaxAttempts": "5",
                                "retriableStatusCodes": [
                                    503
                                ]
                            },
                            "maxStreamDuration": {
                                "maxStreamDuration": "0s",
                                "grpcTimeoutHeaderMax": "0s"
                            }
                        },
                        "decorator": {
                            "operation": "ratings.istio.svc.cluster.local:9080/*"
                        }
                    }
                ],
                "includeRequestAttemptCount": true
            },
            {
                "name": "reviews.istio.svc.cluster.local:9080",
                "domains": [
                    "reviews.istio.svc.cluster.local",
                    "reviews.istio.svc.cluster.local:9080",
                    "reviews",
                    "reviews:9080",
                    "reviews.istio.svc",
                    "reviews.istio.svc:9080",
                    "reviews.istio",
                    "reviews.istio:9080",
                    "10.68.59.45",
                    "10.68.59.45:9080"
                ],
                "routes": [
                    {
                        "name": "default",
                        "match": {
                            "prefix": "/"
                        },
                        "route": {
                            "cluster": "outbound|9080||reviews.istio.svc.cluster.local",
                            "timeout": "0s",
                            "retryPolicy": {
                                "retryOn": "connect-failure,refused-stream,unavailable,cancelled,retriable-status-codes",
                                "numRetries": 2,
                                "retryHostPredicate": [
                                    {
                                        "name": "envoy.retry_host_predicates.previous_hosts"
                                    }
                                ],
                                "hostSelectionRetryMaxAttempts": "5",
                                "retriableStatusCodes": [
                                    503
                                ]
                            },
                            "maxStreamDuration": {
                                "maxStreamDuration": "0s",
                                "grpcTimeoutHeaderMax": "0s"
                            }
                        },
                        "decorator": {
                            "operation": "reviews.istio.svc.cluster.local:9080/*"
                        }
                    }
                ],
                "includeRequestAttemptCount": true
            }
        ],
        "validateClusters": false
    }
]
```

```
[root@master01 istioctl]# istioctl proxy-config routes  productpage-v1-85c4dcbb4c-gsjgw.istio 
NAME                                                             DOMAINS                                                      MATCH                  VIRTUAL SERVICE
istio-ingressgateway.istio-system.svc.cluster.local:15021        istio-ingressgateway.istio-system, 10.68.131.164             /*                     
kube-dns.kube-system.svc.cluster.local:9153                      kube-dns.kube-system, 10.68.0.2                              /*                     
                                                                 *                                                            /stats/prometheus*     
                                                                 *                                                            /healthz/ready*        
InboundPassthroughClusterIpv4                                    *                                                            /*                     
kubernetes-dashboard.kube-system.svc.cluster.local:443           kubernetes-dashboard.kube-system, 10.68.185.150              /*                     
InboundPassthroughClusterIpv4                                    *                                                            /*                     
dashboard-metrics-scraper.kube-system.svc.cluster.local:8000     dashboard-metrics-scraper.kube-system, 10.68.89.158          /*                     
inbound|9080||                                                   *                                                            /*                     
80                                                               helloworld-go-00001-private.knative, 10.68.87.179            /*                     
80                                                               helloworld-go-00001.knative, 10.68.140.219                   /*                     
80                                                               helloworld-go-00002-private.knative, 10.68.111.141           /*                     
80                                                               helloworld-go-00002.knative, 10.68.143.0                     /*                     
80                                                               helloworld-go.knative                                        /*                     
80                                                               istio-egressgateway.istio-system, 10.68.162.53               /*                     
80                                                               istio-ingressgateway.istio-system, 10.68.131.164             /*                     
80                                                               sleep.default, 10.68.184.151                                 /*                     
metrics-server.kube-system.svc.cluster.local:443                 metrics-server.kube-system, 10.68.107.226                    /*                     
node-local-dns.kube-system.svc.cluster.local:9253                node-local-dns.kube-system, *.node-local-dns.kube-system     /*                     
8012                                                             helloworld-go-00001-private.knative, 10.68.87.179            /*                     
8012                                                             helloworld-go-00002-private.knative, 10.68.111.141           /*                     
8022                                                             helloworld-go-00001-private.knative, 10.68.87.179            /*                     
8022                                                             helloworld-go-00002-private.knative, 10.68.111.141           /*                     
9080                                                             bookinfo.default, 10.68.63.49                                /*                     
9080                                                             details.default, 10.68.129.0                                 /*                     
9080                                                             details, details.istio + 1 more...                           /*                     
9080                                                             productpage-v2, productpage-v2.istio + 1 more...             /*                     
9080                                                             productpage.default, 10.68.27.236                            /*                     
9080                                                             productpage, productpage.istio + 1 more...                   /*                     
9080                                                             ratings, ratings.istio + 1 more...                           /*                     
9080                                                             reviews, reviews.istio + 1 more...                           /*                     
9090                                                             helloworld-go-00001-private.knative, 10.68.87.179            /*                     
9090                                                             helloworld-go-00002-private.knative, 10.68.111.141           /*                     
9091                                                             helloworld-go-00001-private.knative, 10.68.87.179            /*                     
9091                                                             helloworld-go-00002-private.knative, 10.68.111.141           /*                     
15010                                                            istiod-1-10-0.istio-system, 10.68.200.66                     /*                     
15010                                                            istiod.istio-system, 10.68.84.162                            /*                     
15014                                                            istiod-1-10-0.istio-system, 10.68.200.66                     /*                     
15014                                                            istiod.istio-system, 10.68.84.162                            /*                     
inbound|9080||
```



## secret

```
[root@node01 ~]# istioctl pc secret --help
Retrieve information about secret configuration for the Envoy instance in the specified pod.

THIS COMMAND IS UNDER ACTIVE DEVELOPMENT AND NOT READY FOR PRODUCTION USE.

Usage:
  istioctl proxy-config secret [<type>/]<name>[.<namespace>] [flags]

Aliases:
  secret, secrets, s

Examples:
  # Retrieve full secret configuration for a given pod from Envoy.
  istioctl proxy-config secret <pod-name[.namespace]>

  # Retrieve full bootstrap without using Kubernetes API
  ssh <user@hostname> 'curl localhost:15000/config_dump' > envoy-config.json
  istioctl proxy-config secret --file envoy-config.json

Flags:
  -f, --file string   Envoy config dump JSON file
  -h, --help          help for secret

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
  -o, --output string           Output format: one of json|yaml|short (default "short")
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl pc secret --file rating_config_dump.json 
RESOURCE NAME     TYPE           STATUS     VALID CERT     SERIAL NUMBER                               NOT AFTER                NOT BEFORE
default           Cert Chain     ACTIVE     true           50138781534953415255353886120259325508      2021-08-28T01:24:12Z     2021-08-27T01:22:12Z
ROOTCA            CA             ACTIVE     true           167703872587426930343074795595359714442     2031-05-19T04:18:43Z     2021-05-21T04:18:43Z
```

```
[root@master01 istioctl]# istioctl pc secret productpage-v1-85c4dcbb4c-gsjgw.istio 
RESOURCE NAME     TYPE           STATUS     VALID CERT     SERIAL NUMBER                               NOT AFTER                NOT BEFORE
default           Cert Chain     ACTIVE     true           304905990827773719107747380886548554018     2021-08-28T01:24:24Z     2021-08-27T01:22:24Z
ROOTCA            CA             ACTIVE     true           167703872587426930343074795595359714442     2031-05-19T04:18:43Z     2021-05-21T04:18:43Z
```



# proxy-status

```
[root@node01 ~]# istioctl ps --help

Retrieves last sent and last acknowledged xDS sync from Istiod to each Envoy in the mesh

Usage:
  istioctl proxy-status [<type>/]<name>[.<namespace>] [flags]

Aliases:
  proxy-status, ps

Examples:
  # Retrieve sync status for all Envoys in a mesh
  istioctl proxy-status

  # Retrieve sync diff for a single Envoy and Istiod
  istioctl proxy-status istio-egressgateway-59585c5b9c-ndc59.istio-system

  # Retrieve sync diff between Istiod and one pod under a deployment
  istioctl proxy-status deployment/productpage-v1

  # Write proxy config-dump to file, and compare to Istio control plane
  kubectl port-forward -n istio-system istio-egressgateway-59585c5b9c-ndc59 15000 &
  curl localhost:15000/config_dump > cd.json
  istioctl proxy-status istio-egressgateway-59585c5b9c-ndc59.istio-system --file cd.json


Flags:
  -f, --file string       Envoy config dump JSON file
  -h, --help              help for proxy-status
  -r, --revision string   Control plane revision

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 ~]# istioctl ps
NAME                                                   CLUSTER        CDS        LDS        EDS        RDS          ECDS         ISTIOD                      VERSION
details-v1-584d9c9654-ldcch.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-egressgateway-575d8bd99b-7nv76.istio-system      Kubernetes     SYNCED     SYNCED     SYNCED     NOT SENT     NOT SENT     istiod-8495d444bb-vvnpn     1.14.1
istio-ingressgateway-6668f9548d-8z8lq.istio-system     Kubernetes     SYNCED     SYNCED     SYNCED     NOT SENT     NOT SENT     istiod-8495d444bb-vvnpn     1.14.1
productpage-v1-85c4dcbb4c-gsjgw.istio                  Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT SENT     istiod-8495d444bb-vvnpn     1.14.1
ratings-v1-8557bdf74b-9mw8d.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v1-5548d44b7d-86xc9.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v2-75d4b48554-7gjrx.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT SENT     istiod-8495d444bb-vvnpn     1.14.1
reviews-v3-5698f5dc86-mdww7.istio                      Kubernetes     SYNCED     SYNCED     SYNCED     SYNCED       NOT SENT     istiod-8495d444bb-vvnpn     1.14.1
```

```
[root@master01 istioctl]# istioctl proxy-status deployment/productpage-v1 -n istio
Clusters Match
Listeners Match
Routes Match (RDS last loaded at Fri, 27 Aug 2021 16:31:35 CST)
```

```
[root@master01 istioctl]# istioctl proxy-status productpage-v1-6b746f74dc-vlvmt.istio
Clusters Match
Listeners Match
Routes Match (RDS last loaded at Fri, 27 Aug 2021 16:31:35 CST)
```

```
istioctl proxy-status deployment/ratings-v1 --file rating_config_dump.json -n istio
```



# upgrade

```
[root@node01 ~]# istioctl upgrade --help
The upgrade command is an alias for the install command that performs additional upgrade-related checks.

Usage:
  istioctl upgrade [flags]

Flags:
      --dry-run                      Console/log output only, make no changes.
  -f, --filename strings             Path to file containing IstioOperator custom resource
                                     This flag can be specified multiple times to overlay multiple files. Multiple files are overlaid in left to right order.
      --force                        Proceed even with validation errors.
  -h, --help                         help for upgrade
  -d, --manifests string             Specify a path to a directory of charts and profiles
                                     (e.g. ~/Downloads/istio-1.14.1/manifests)
                                     or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                                     
      --readiness-timeout duration   Maximum time to wait for Istio resources in each component to be ready. (default 5m0s)
  -s, --set stringArray              Override an IstioOperator value, e.g. to choose a profile
                                     (--set profile=demo), enable or disable components (--set components.cni.enabled=true), or override Istio
                                     settings (--set meshConfig.enableTracing=true). See documentation for more info:https://istio.io/v1.14/docs/reference/config/istio.operator.v1alpha1/#IstioOperatorSpec
  -y, --skip-confirmation            The skipConfirmation determines whether the user is prompted for confirmation.
                                     If set to true, the user is not prompted and a Yes response is assumed in all cases.
      --verify                       Verify the Istio control plane after installation/in-place upgrade

Global Flags:
      --context string      The name of the kubeconfig context to use
  -c, --kubeconfig string   Kubernetes configuration file
      --vklog Level         number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl upgrade --set profile=demo
2021-08-27T10:47:29.738904Z     info    proto: tag has too few fields: "-"
Control Plane - istio-egressgateway pod - istio-egressgateway-7844997f9c-n5z4s - version: 1.11.0
Control Plane - istio-ingressgateway pod - istio-ingressgateway-84665b9cfc-k99vk - version: 1.11.0
Control Plane - istiod pod - istiod-1-10-0-99f9b695f-bt9kl - version: 1.11.0
Control Plane - istiod pod - istiod-77b4d7b55d-fkf7s - version: 1.11.0

Upgrade version check passed: 1.11.0 -> 1.11.0.

2021-08-27T10:47:52.053097Z     info    Error: failed to generate Istio configs from file [] for the current version: 1.11.0, error: Get "https://github.com/istio/istio/releases/download/1.11.0/istio-1.11.0-linux-amd64.tar.gz": read tcp 192.168.198.154:51628->20.205.243.166:443: read: connection reset by peer

Error: failed to generate Istio configs from file [] for the current version: 1.11.0, error: Get "https://github.com/istio/istio/releases/download/1.11.0/istio-1.11.0-linux-amd64.tar.gz": read tcp 192.168.198.154:51628->20.205.243.166:443: read: connection reset by peer
```

```
istioctl upgrade -f iop.yaml 

istioctl upgrade -f iop.yaml  --force   
istioctl upgrade --set profile=demo --manifests /root/istio-1.13.2/manifests
istioctl upgrade --set profile=demo --readiness-timeout 600s
istioctl upgrade --set profile=demo --skip-confirmation  
istioctl upgrade --set profile=demo  --verify  
```



# validate

```
[root@node01 ~]# istioctl validate --help
Validate Istio policy and rules files

Usage:
  istioctl validate -f FILENAME [options] [flags]

Aliases:
  validate, v

Examples:
  # Validate bookinfo-gateway.yaml
  istioctl validate -f samples/bookinfo/networking/bookinfo-gateway.yaml

  # Validate bookinfo-gateway.yaml with shorthand syntax
  istioctl v -f samples/bookinfo/networking/bookinfo-gateway.yaml

  # Validate current deployments under 'default' namespace within the cluster
  kubectl get deployments -o yaml | istioctl validate -f -

  # Validate current services under 'default' namespace within the cluster
  kubectl get services -o yaml | istioctl validate -f -

  # Also see the related command 'istioctl analyze'
  istioctl analyze samples/bookinfo/networking/bookinfo-gateway.yaml


Flags:
  -f, --filename strings   Names of files to validate
  -h, --help               help for validate
  -x, --referential        Enable structural validation for policy and telemetry (default true)

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl validate -f productpage-allow-all.yaml 
"productpage-allow-all.yaml" is valid
```

```
[root@master01 istioctl]# istioctl validate -f productpage-allow-all.yaml 
"productpage-allow-all.yaml" is valid
[root@master01 istioctl]# kubectl get deployments -o yaml -n istio | istioctl validate -f -
deployment "my-nginx/istio:" may not provide Istio metrics and telemetry without label "app". See https://istio.io/v1.11/docs/ops/deployment/requirements/
deployment "my-nginx/istio:" may not provide Istio metrics and telemetry without label "version". See https://istio.io/v1.11/docs/ops/deployment/requirements/
deployment "my-nginx-v2/istio:" may not provide Istio metrics and telemetry without label "app". See https://istio.io/v1.11/docs/ops/deployment/requirements/
deployment "my-nginx-v2/istio:" may not provide Istio metrics and telemetry without label "version". See https://istio.io/v1.11/docs/ops/deployment/requirements/
validation succeed
```

```
[root@master01 istioctl]# kubectl get services -o yaml -n istio| istioctl validate -f -
validation succeed

  kubectl get services -o yaml -n istio| istioctl validate -f - --referential 
```



# verify-install

```
[root@node01 ~]# istioctl verify-install --help

verify-install verifies Istio installation status against the installation file
you specified when you installed Istio. It loops through all the installation
resources defined in your installation file and reports whether all of them are
in ready status. It will report failure when any of them are not ready.

If you do not specify an installation it will check for an IstioOperator resource
and will verify if pods and services defined in it are present.

Note: For verifying whether your cluster is ready for Istio installation, see
istioctl experimental precheck.

Usage:
  istioctl verify-install [-f <deployment or istio operator file>] [--revision <revision>] [flags]

Examples:
  # Verify that Istio is installed correctly via Istio Operator
  istioctl verify-install

  # Verify the deployment matches a custom Istio deployment configuration
  istioctl verify-install -f $HOME/istio.yaml

  # Verify the deployment matches the Istio Operator deployment definition
  istioctl verify-install --revision <canary>

  # Verify the installation of specific revision
  istioctl verify-install -r 1-9-0

Flags:
  -f, --filename strings   Istio YAML installation file.
  -h, --help               help for verify-install
  -d, --manifests string   Specify a path to a directory of charts and profiles
                           (e.g. ~/Downloads/istio-1.14.1/manifests)
                           or release tar URL (e.g. https://github.com/istio/istio/releases/download/1.14.1/istio-1.14.1-linux-amd64.tar.gz).
                           
  -r, --revision string    Control plane revision

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@master01 istioctl]# istioctl verify-install
2 Istio control planes detected, checking --revision "1-10-0" only
✔ ClusterRole: istiod-istio-system.istio-system checked successfully
✔ ClusterRole: istio-reader-istio-system.istio-system checked successfully
✔ ClusterRoleBinding: istio-reader-istio-system.istio-system checked successfully
✔ ClusterRoleBinding: istiod-istio-system.istio-system checked successfully
✔ ServiceAccount: istio-reader-service-account.istio-system checked successfully
✔ Role: istiod-istio-system.istio-system checked successfully
✔ RoleBinding: istiod-istio-system.istio-system checked successfully
✔ ServiceAccount: istiod-service-account.istio-system checked successfully
✔ CustomResourceDefinition: destinationrules.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: envoyfilters.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: gateways.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: serviceentries.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: sidecars.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: virtualservices.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: workloadentries.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: workloadgroups.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: authorizationpolicies.security.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: peerauthentications.security.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: requestauthentications.security.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: telemetries.telemetry.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: istiooperators.install.istio.io.istio-system checked successfully
✘ ClusterRole: istiod-clusterrole-1-10-0-istio-system.istio-system: the server could not find the requested resource
✘ ClusterRoleBinding: istiod-clusterrole-1-10-0-istio-system.istio-system: the server could not find the requested resource
✔ ConfigMap: istio-1-10-0.istio-system checked successfully
✔ Deployment: istiod-1-10-0.istio-system checked successfully
✔ ConfigMap: istio-sidecar-injector-1-10-0.istio-system checked successfully
✔ MutatingWebhookConfiguration: istio-sidecar-injector-1-10-0.istio-system checked successfully
✔ PodDisruptionBudget: istiod-1-10-0.istio-system checked successfully
✘ ClusterRole: istio-reader-clusterrole-1-10-0-istio-system.istio-system: the server could not find the requested resource
✘ ClusterRoleBinding: istio-reader-clusterrole-1-10-0-istio-system.istio-system: the server could not find the requested resource
✘ Role: istiod-1-10-0.istio-system: roles.rbac.authorization.k8s.io "istiod-1-10-0" not found
✘ RoleBinding: istiod-1-10-0.istio-system: rolebindings.rbac.authorization.k8s.io "istiod-1-10-0" not found
✔ Service: istiod-1-10-0.istio-system checked successfully
✘ ServiceAccount: istiod-1-10-0.istio-system: serviceaccounts "istiod-1-10-0" not found
✘ EnvoyFilter: metadata-exchange-1.10-1-10-0.istio-system: envoyfilters.networking.istio.io "metadata-exchange-1.10-1-10-0" not found
✘ EnvoyFilter: tcp-metadata-exchange-1.10-1-10-0.istio-system: envoyfilters.networking.istio.io "tcp-metadata-exchange-1.10-1-10-0" not found
✘ EnvoyFilter: stats-filter-1.10-1-10-0.istio-system: envoyfilters.networking.istio.io "stats-filter-1.10-1-10-0" not found
✘ EnvoyFilter: tcp-stats-filter-1.10-1-10-0.istio-system: envoyfilters.networking.istio.io "tcp-stats-filter-1.10-1-10-0" not found
✘ EnvoyFilter: metadata-exchange-1.11-1-10-0.istio-system: envoyfilters.networking.istio.io "metadata-exchange-1.11-1-10-0" not found
✘ EnvoyFilter: tcp-metadata-exchange-1.11-1-10-0.istio-system: envoyfilters.networking.istio.io "tcp-metadata-exchange-1.11-1-10-0" not found
✘ EnvoyFilter: stats-filter-1.11-1-10-0.istio-system: envoyfilters.networking.istio.io "stats-filter-1.11-1-10-0" not found
✘ EnvoyFilter: tcp-stats-filter-1.11-1-10-0.istio-system: envoyfilters.networking.istio.io "tcp-stats-filter-1.11-1-10-0" not found
✘ EnvoyFilter: metadata-exchange-1.9-1-10-0.istio-system: envoyfilters.networking.istio.io "metadata-exchange-1.9-1-10-0" not found
✘ EnvoyFilter: tcp-metadata-exchange-1.9-1-10-0.istio-system: envoyfilters.networking.istio.io "tcp-metadata-exchange-1.9-1-10-0" not found
✘ EnvoyFilter: stats-filter-1.9-1-10-0.istio-system: envoyfilters.networking.istio.io "stats-filter-1.9-1-10-0" not found
✘ EnvoyFilter: tcp-stats-filter-1.9-1-10-0.istio-system: envoyfilters.networking.istio.io "tcp-stats-filter-1.9-1-10-0" not found
✘ ValidatingWebhookConfiguration: istio-validator-1-10-0-istio-system.istio-system: the server could not find the requested resource
✔ Deployment: istio-ingressgateway.istio-system checked successfully
✔ PodDisruptionBudget: istio-ingressgateway.istio-system checked successfully
✔ Role: istio-ingressgateway-sds.istio-system checked successfully
✔ RoleBinding: istio-ingressgateway-sds.istio-system checked successfully
✔ Service: istio-ingressgateway.istio-system checked successfully
✔ ServiceAccount: istio-ingressgateway-service-account.istio-system checked successfully
✔ Deployment: istio-egressgateway.istio-system checked successfully
✔ PodDisruptionBudget: istio-egressgateway.istio-system checked successfully
✔ Role: istio-egressgateway-sds.istio-system checked successfully
✔ RoleBinding: istio-egressgateway-sds.istio-system checked successfully
✔ Service: istio-egressgateway.istio-system checked successfully
✔ ServiceAccount: istio-egressgateway-service-account.istio-system checked successfully
Checked 13 custom resource definitions
Checked 3 Istio Deployments
Error: Istio installation failed
```

```
[root@master01 istioctl]# istioctl verify-install --revision 1-14-1
✔ Deployment: istio-ingressgateway.istio-system checked successfully
✔ PodDisruptionBudget: istio-ingressgateway.istio-system checked successfully
✔ Role: istio-ingressgateway-sds.istio-system checked successfully
✔ RoleBinding: istio-ingressgateway-sds.istio-system checked successfully
✔ Service: istio-ingressgateway.istio-system checked successfully
✔ ServiceAccount: istio-ingressgateway-service-account.istio-system checked successfully
✔ Deployment: istio-egressgateway.istio-system checked successfully
✔ PodDisruptionBudget: istio-egressgateway.istio-system checked successfully
✔ Role: istio-egressgateway-sds.istio-system checked successfully
✔ RoleBinding: istio-egressgateway-sds.istio-system checked successfully
✔ Service: istio-egressgateway.istio-system checked successfully
✔ ServiceAccount: istio-egressgateway-service-account.istio-system checked successfully
✔ ClusterRole: istiod-istio-system.istio-system checked successfully
✔ ClusterRole: istio-reader-istio-system.istio-system checked successfully
✔ ClusterRoleBinding: istio-reader-istio-system.istio-system checked successfully
✔ ClusterRoleBinding: istiod-istio-system.istio-system checked successfully
✔ ServiceAccount: istio-reader-service-account.istio-system checked successfully
✔ Role: istiod-istio-system.istio-system checked successfully
✔ RoleBinding: istiod-istio-system.istio-system checked successfully
✔ ServiceAccount: istiod-service-account.istio-system checked successfully
✔ CustomResourceDefinition: destinationrules.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: envoyfilters.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: gateways.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: serviceentries.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: sidecars.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: virtualservices.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: workloadentries.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: workloadgroups.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: authorizationpolicies.security.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: peerauthentications.security.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: requestauthentications.security.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: telemetries.telemetry.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: istiooperators.install.istio.io.istio-system checked successfully
✘ ClusterRole: istiod-clusterrole-1-10-0-istio-system.istio-system: the server could not find the requested resource
✘ ClusterRoleBinding: istiod-clusterrole-1-10-0-istio-system.istio-system: the server could not find the requested resource
✔ ConfigMap: istio-1-10-0.istio-system checked successfully
✔ Deployment: istiod-1-10-0.istio-system checked successfully
✔ ConfigMap: istio-sidecar-injector-1-10-0.istio-system checked successfully
✔ MutatingWebhookConfiguration: istio-sidecar-injector-1-10-0.istio-system checked successfully
✔ PodDisruptionBudget: istiod-1-10-0.istio-system checked successfully
✘ ClusterRole: istio-reader-clusterrole-1-10-0-istio-system.istio-system: the server could not find the requested resource
✘ ClusterRoleBinding: istio-reader-clusterrole-1-10-0-istio-system.istio-system: the server could not find the requested resource
✘ Role: istiod-1-10-0.istio-system: roles.rbac.authorization.k8s.io "istiod-1-10-0" not found
✘ RoleBinding: istiod-1-10-0.istio-system: rolebindings.rbac.authorization.k8s.io "istiod-1-10-0" not found
✔ Service: istiod-1-10-0.istio-system checked successfully
✘ ServiceAccount: istiod-1-10-0.istio-system: serviceaccounts "istiod-1-10-0" not found
✘ EnvoyFilter: metadata-exchange-1.10-1-10-0.istio-system: envoyfilters.networking.istio.io "metadata-exchange-1.10-1-10-0" not found
✘ EnvoyFilter: tcp-metadata-exchange-1.10-1-10-0.istio-system: envoyfilters.networking.istio.io "tcp-metadata-exchange-1.10-1-10-0" not found
✘ EnvoyFilter: stats-filter-1.10-1-10-0.istio-system: envoyfilters.networking.istio.io "stats-filter-1.10-1-10-0" not found
✘ EnvoyFilter: tcp-stats-filter-1.10-1-10-0.istio-system: envoyfilters.networking.istio.io "tcp-stats-filter-1.10-1-10-0" not found
✘ EnvoyFilter: metadata-exchange-1.11-1-10-0.istio-system: envoyfilters.networking.istio.io "metadata-exchange-1.11-1-10-0" not found
✘ EnvoyFilter: tcp-metadata-exchange-1.11-1-10-0.istio-system: envoyfilters.networking.istio.io "tcp-metadata-exchange-1.11-1-10-0" not found
✘ EnvoyFilter: stats-filter-1.11-1-10-0.istio-system: envoyfilters.networking.istio.io "stats-filter-1.11-1-10-0" not found
✘ EnvoyFilter: tcp-stats-filter-1.11-1-10-0.istio-system: envoyfilters.networking.istio.io "tcp-stats-filter-1.11-1-10-0" not found
✘ EnvoyFilter: metadata-exchange-1.9-1-10-0.istio-system: envoyfilters.networking.istio.io "metadata-exchange-1.9-1-10-0" not found
✘ EnvoyFilter: tcp-metadata-exchange-1.9-1-10-0.istio-system: envoyfilters.networking.istio.io "tcp-metadata-exchange-1.9-1-10-0" not found
✘ EnvoyFilter: stats-filter-1.9-1-10-0.istio-system: envoyfilters.networking.istio.io "stats-filter-1.9-1-10-0" not found
✘ EnvoyFilter: tcp-stats-filter-1.9-1-10-0.istio-system: envoyfilters.networking.istio.io "tcp-stats-filter-1.9-1-10-0" not found
✘ ValidatingWebhookConfiguration: istio-validator-1-10-0-istio-system.istio-system: the server could not find the requested resource
Checked 13 custom resource definitions
Checked 3 Istio Deployments
Error: Istio installation failed
```

```
[root@master01 istioctl]# istioctl verify-install -f iop.yaml 
✔ ClusterRole: istiod-istio-system.istio-system checked successfully
✔ ClusterRole: istio-reader-istio-system.istio-system checked successfully
✔ ClusterRoleBinding: istio-reader-istio-system.istio-system checked successfully
✔ ClusterRoleBinding: istiod-istio-system.istio-system checked successfully
✔ ServiceAccount: istio-reader-service-account.istio-system checked successfully
✔ Role: istiod-istio-system.istio-system checked successfully
✔ RoleBinding: istiod-istio-system.istio-system checked successfully
✔ ServiceAccount: istiod-service-account.istio-system checked successfully
✔ CustomResourceDefinition: destinationrules.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: envoyfilters.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: gateways.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: serviceentries.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: sidecars.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: virtualservices.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: workloadentries.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: workloadgroups.networking.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: authorizationpolicies.security.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: peerauthentications.security.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: requestauthentications.security.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: telemetries.telemetry.istio.io.istio-system checked successfully
✔ CustomResourceDefinition: istiooperators.install.istio.io.istio-system checked successfully
✔ ClusterRole: istiod-clusterrole-istio-system.istio-system checked successfully
✔ ClusterRoleBinding: istiod-clusterrole-istio-system.istio-system checked successfully
✔ ConfigMap: istio.istio-system checked successfully
✔ Deployment: istiod.istio-system checked successfully
✔ ConfigMap: istio-sidecar-injector.istio-system checked successfully
✔ MutatingWebhookConfiguration: istio-sidecar-injector.istio-system checked successfully
✔ PodDisruptionBudget: istiod.istio-system checked successfully
✔ ClusterRole: istio-reader-clusterrole-istio-system.istio-system checked successfully
✔ ClusterRoleBinding: istio-reader-clusterrole-istio-system.istio-system checked successfully
✔ Role: istiod.istio-system checked successfully
✔ RoleBinding: istiod.istio-system checked successfully
✔ Service: istiod.istio-system checked successfully
✔ ServiceAccount: istiod.istio-system checked successfully
✔ EnvoyFilter: metadata-exchange-1.10.istio-system checked successfully
✔ EnvoyFilter: tcp-metadata-exchange-1.10.istio-system checked successfully
✔ EnvoyFilter: stats-filter-1.10.istio-system checked successfully
✔ EnvoyFilter: tcp-stats-filter-1.10.istio-system checked successfully
✔ EnvoyFilter: metadata-exchange-1.11.istio-system checked successfully
✔ EnvoyFilter: tcp-metadata-exchange-1.11.istio-system checked successfully
✔ EnvoyFilter: stats-filter-1.11.istio-system checked successfully
✔ EnvoyFilter: tcp-stats-filter-1.11.istio-system checked successfully
✔ EnvoyFilter: metadata-exchange-1.9.istio-system checked successfully
✔ EnvoyFilter: tcp-metadata-exchange-1.9.istio-system checked successfully
✔ EnvoyFilter: stats-filter-1.9.istio-system checked successfully
✔ EnvoyFilter: tcp-stats-filter-1.9.istio-system checked successfully
✔ ValidatingWebhookConfiguration: istio-validator-istio-system.istio-system checked successfully
✔ Deployment: istio-ingressgateway.istio-system checked successfully
✔ PodDisruptionBudget: istio-ingressgateway.istio-system checked successfully
✔ Role: istio-ingressgateway-sds.istio-system checked successfully
✔ RoleBinding: istio-ingressgateway-sds.istio-system checked successfully
✔ Service: istio-ingressgateway.istio-system checked successfully
✔ ServiceAccount: istio-ingressgateway-service-account.istio-system checked successfully
✔ Deployment: istio-egressgateway.istio-system checked successfully
✔ PodDisruptionBudget: istio-egressgateway.istio-system checked successfully
✔ Role: istio-egressgateway-sds.istio-system checked successfully
✔ RoleBinding: istio-egressgateway-sds.istio-system checked successfully
✔ Service: istio-egressgateway.istio-system checked successfully
✔ ServiceAccount: istio-egressgateway-service-account.istio-system checked successfully
✔ IstioOperator: example-istiocontrolplane.istio-system checked successfully
Checked 13 custom resource definitions
Checked 3 Istio Deployments
✔ Istio is installed and verified successfully
```

```
istioctl verify-install --manifests /root/istio-1.14.1/manifests
```



# version

```
[root@node01 ~]# istioctl version  --help
Prints out build version information

Usage:
  istioctl version [flags]

Flags:
  -h, --help              help for version
  -o, --output string     One of 'yaml' or 'json'.
      --remote            Use --remote=false to suppress control plane check
  -r, --revision string   Control plane revision
  -s, --short             Use --short=false to generate full version information

Global Flags:
      --context string          The name of the kubeconfig context to use
  -i, --istioNamespace string   Istio system namespace (default "istio-system")
  -c, --kubeconfig string       Kubernetes configuration file
  -n, --namespace string        Config namespace
      --vklog Level             number for the log level verbosity. Like -v flag. ex: --vklog=9
```

```
[root@node01 ~]# istioctl version 
client version: 1.14.1
control plane version: 1.14.1
data plane version: 1.14.1 (8 proxies)
```

```
istioctl version  --remote false
istioctl version  --short  false
istioctl version --revision 1-14-1 --output json
istioctl version --revision 1-14-1 --output yaml
```



# options

```
[root@node01 ~]# istioctl options 
The following options can be passed to any command:
      --log_as_json: Whether to format output as JSON or in plain console-friendly format
      --log_caller: Comma-separated list of scopes for which to include caller information, scopes can be any of [ads, adsc, all, analysis, authn, authorization, ca, cli, controllers, default, delta, file, gateway, grpcgen, installer, klog, kube, model, patch, processing, proxyconfig, retry, serviceentry, spiffe, status, telemetry, tpath, translator, trustBundle, util, validation, validationController, wasm, wle]
      --log_output_level: Comma-separated minimum per-scope logging level of messages to output, in the form of <scope>:<level>,<scope>:<level>,... where scope can be one of [ads, adsc, all, analysis, authn, authorization, ca, cli, controllers, default, delta, file, gateway, grpcgen, installer, klog, kube, model, patch, processing, proxyconfig, retry, serviceentry, spiffe, status, telemetry, tpath, translator, trustBundle, util, validation, validationController, wasm, wle] and level can be one of [debug, info, warn, error, fatal, none]
      --log_stacktrace_level: Comma-separated minimum per-scope logging level at which stack traces are captured, in the form of <scope>:<level>,<scope:level>,... where scope can be one of [ads, adsc, all, analysis, authn, authorization, ca, cli, controllers, default, delta, file, gateway, grpcgen, installer, klog, kube, model, patch, processing, proxyconfig, retry, serviceentry, spiffe, status, telemetry, tpath, translator, trustBundle, util, validation, validationController, wasm, wle] and level can be one of [debug, info, warn, error, fatal, none]
      --log_target: The set of paths where to output the log. This can be any path as well as the special values stdout and stderr
```





