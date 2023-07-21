package main

import (
        "fmt"
        "github.com/tidwall/gjson"
        
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
        configuration pluginConfiguration
}

type pluginConfiguration struct {
        header_key string
        header_value string
}

func (ctx *pluginContext) OnPluginStart(pluginConfigurationSize int) types.OnPluginStartStatus {
        data, err := proxywasm.GetPluginConfiguration()
        proxywasm.LogCriticalf("plugin configuration: %s", string(data))
        
        if err != nil {
                proxywasm.LogCriticalf("error reading plugin configuration: %v", err)
        }
        config, err := parsePluginConfiguration(data)
        if err != nil {
                proxywasm.LogCriticalf("error parsing plugin configuration: %v", err)
                return types.OnPluginStartStatusFailed
        }
        ctx.configuration = config
        
        return types.OnPluginStartStatusOK
}

func parsePluginConfiguration(data []byte) (pluginConfiguration, error) {
        if len(data) == 0 {
                proxywasm.LogCriticalf("plugin configuration data length is 0")
                return pluginConfiguration{}, nil
        }

        config := &pluginConfiguration{}
        if !gjson.ValidBytes(data) {
                return pluginConfiguration{}, fmt.Errorf("the plugin configuration is not a valid json: %q", string(data))
        }

        jsonData := gjson.ParseBytes(data)
        header_key:= jsonData.Get("header_key").String()
        header_value:= jsonData.Get("header_value").String()
        config.header_key=header_key
        config.header_value=header_value

        return *config, nil
}

// Override types.DefaultPluginContext.
func (p *pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &httpHeaders{contextID: contextID,header_key:p.configuration.header_key,header_value:p.configuration.header_value}
}

type httpHeaders struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        contextID uint32
        header_key string
        header_value string
}

// Override types.DefaultHttpContext.
func (ctx *httpHeaders) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {      
        err := proxywasm.ReplaceHttpRequestHeader(ctx.header_key,ctx.header_value)
        if err != nil {
                proxywasm.LogCriticalf("failed to set request headers(%s,%s): %v",ctx.header_key,ctx.header_value, err)
        }
		proxywasm.LogCriticalf("set request headers(%s,%s)",ctx.header_key,ctx.header_value)
        return types.ActionContinue
}

