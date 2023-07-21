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
func (p *pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
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
        version,err:=proxywasm.GetHttpRequestHeader("version");
        if err != nil {
                proxywasm.LogCriticalf("failed to get request headers(%s,%s): %v","version",version, err)
        }
        if err := proxywasm.SetSharedData("version", []byte(version), 0); err != nil {
                proxywasm.LogWarnf("error setting shared data on OnHttpRequestHeaders: %v", err)
        }

		proxywasm.LogCriticalf("set SetProperty(%s,%s)","version",version)
        return types.ActionContinue
}

