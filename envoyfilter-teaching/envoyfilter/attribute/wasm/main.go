package main

import (
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm"
        "github.com/tetratelabs/proxy-wasm-go-sdk/proxywasm/types"
)

func main() {
        proxywasm.SetVMContext(&vmContext{})
}

type vmContext struct {
        // Embed the default VM context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultVMContext
}

// Override types.DefaultVMContext.
func (*vmContext) NewPluginContext(contextID uint32) types.PluginContext {
        return &pluginContext{}
}

type pluginContext struct {
        // Embed the default plugin context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultPluginContext
}

// Override types.DefaultPluginContext.
func (*pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &httpHeaders{contextID: contextID}
}

type httpHeaders struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        contextID uint32
}

// Override types.DefaultHttpContext.
func (ctx *httpHeaders) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        path :=[]string{"plugin_vm_id"}
        data,err := proxywasm.GetProperty(path) 
        if err != nil {
                proxywasm.LogCritical("failed to get property")
        }

        proxywasm.LogCriticalf("property(%s): %s", "plugin_vm_id",string(data))
        
        path=[]string{"route_metadata"}
        data2,err := proxywasm.GetProperty(path)
        
        if err != nil {
                proxywasm.LogCritical("failed to get property")
        }
        proxywasm.LogCriticalf("property(%s): %s", "route_metadata",string(data2))
        return types.ActionContinue
}
