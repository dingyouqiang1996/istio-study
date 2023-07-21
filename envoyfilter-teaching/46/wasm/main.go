package main

import (
        "encoding/base64"
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
}

// Override types.DefaultPluginContext.
func (*pluginContext) NewHttpContext(contextID uint32) types.HttpContext {
        return &httpAuth{contextID: contextID}
}

type httpAuth struct {
        // Embed the default http context here,
        // so that we don't need to reimplement all the methods.
        types.DefaultHttpContext
        contextID uint32
}

// Override types.DefaultHttpContext.
func (ctx *httpAuth) OnHttpRequestHeaders(numHeaders int, endOfStream bool) types.Action {
        auth, err := proxywasm.GetHttpRequestHeader("auth")
        if err != nil {
                proxywasm.LogCriticalf("failed to get request header: %v", err)
                return types.ActionContinue
        }
        decoded, err := base64.RawStdEncoding.DecodeString(auth)
        if err != nil {
                proxywasm.LogCriticalf("failed to decodestring: %v", err)
                return types.ActionContinue
        }
        
        if !gjson.ValidBytes(decoded) {
                proxywasm.LogCriticalf("json format is not right")
        }
        jsonData := gjson.ParseBytes(decoded)
        userId := jsonData.Get("userId").String()
        
        hs := [][2]string{
              {":method", "GET"}, {":authority", "auth-simple:8080"},{":path", "/auth"}, {"accept", "*/*"},
              {"userId",userId},
        }
        for _, h := range hs {
                proxywasm.LogInfof("request header: %s: %s", h[0], h[1])
        }

        if _, err := proxywasm.DispatchHttpCall("outbound|8080||auth-simple.istio.svc.cluster.local", hs, nil, nil,
                8080, httpCallResponseCallback); err != nil {
                proxywasm.LogCriticalf("dipatch httpcall failed: %v", err)
                return types.ActionContinue
        }

        proxywasm.LogInfof("http call dispatched to %s", "outbound|8080||auth-simple.istio.svc.cluster.local")
        return types.ActionPause
}

func httpCallResponseCallback(numHeaders, bodySize, numTrailers int) {
        hs, err := proxywasm.GetHttpCallResponseHeaders()
        if err != nil {
                proxywasm.LogCriticalf("failed to get response body: %v", err)
                return
        }

        for _, h := range hs {
                proxywasm.LogInfof("response header from %s: %s: %s", "outbound|8080||auth-simple.istio.svc.cluster.local", h[0], h[1])
        }

        b, err := proxywasm.GetHttpCallResponseBody(0, bodySize)
        if err != nil {
                proxywasm.LogCriticalf("failed to get response body: %v", err)
                proxywasm.ResumeHttpRequest()
                return
        }

        ret:=string(b)

        if ret=="ok" {
                proxywasm.LogInfo("access granted")
                proxywasm.ResumeHttpRequest()
                return
        }

        body := "access forbidden"
        proxywasm.LogInfo(body)
        if err := proxywasm.SendHttpResponse(403, [][2]string{
                {"powered-by", "proxy-wasm-go-sdk!!"},
        }, []byte(body), -1); err != nil {
                proxywasm.LogErrorf("failed to send local response: %v", err)
                proxywasm.ResumeHttpRequest()
        }
}
