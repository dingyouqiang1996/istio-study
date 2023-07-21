package cn.huanzi.qch.baseadmin.config.security;

import cn.huanzi.qch.baseadmin.common.pojo.ParameterRequestWrapper;
import cn.huanzi.qch.baseadmin.common.pojo.Result;
import cn.huanzi.qch.baseadmin.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;

/**
 * 校验账号、密码前，先进行验证码处理，需要在这里进行登录解密操作
 */
@Component
@Slf4j
public class CaptchaFilterConfig implements Filter {

    @Value("${captcha.enable}")
    private Boolean captchaEnable;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();
        String requestUri = request.getRequestURI();
        /*
            注：详情可在SessionManagementFilter中进行断点调试查看
            security框架会在session的attribute存储登录信息，先从session.getAttribute(this.springSecurityContextKey)中获取登录用户信息
            ，如果没有，再从本地上下文SecurityContextHolder.getContext().getAuthentication()获取，因此想要强制用户下线得进行如下操作

            另外，虽然重启了服务，sessionRegistry.getAllSessions()为空，但之前的用户session未过期同样能访问系统，也是这个原因
         */
        User user = securityUtil.sessionRegistryGetUserBySessionId(session.getId());
        if(user == null && session.getAttribute("SPRING_SECURITY_CONTEXT") != null){

            //remember me？
            Cookie rememberMeCookie = SecurityUtil.getRememberMeCookie(request);
            PersistentRememberMeToken token = securityUtil.rememberMeGetTokenForSeries(rememberMeCookie);

            if(!StringUtils.isEmpty(token)){
                log.info("当前session连接开启了免登陆，已自动登录！token：{},userName：{}，最后登录时间：{}",rememberMeCookie.getValue(),token.getUsername(),token.getDate());
                //注册新的session
                securityUtil.sessionRegistryAddUser(session.getId(), userDetailsServiceImpl.loadUserByUsername(token.getUsername()));
            }

            //当前URL是否允许访问，同时没有remember me
            if(!SecurityUtil.checkUrl(requestUri.replaceFirst(contextPath,"")) && StringUtils.isEmpty(token)){
                //直接输出js脚本跳转强制用户下线
                HttpServletResponseUtil.print(response,"<script type='text/javascript'>window.location.href = '" + contextPath + "/logout'</script>");
                return;
            }

        }

        //只拦截登录请求，且开发环境下不拦截
        if ("POST".equals(request.getMethod()) && "/login".equals(requestUri.replaceFirst(contextPath,""))) {
            //判断api加密开关是否开启
            if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())){
                //api解密
                String decrypt = ApiSecurityUtil.decrypt();

                //new一个自定义RequestWrapper
                HashMap hashMap = JsonUtil.parse(decrypt, HashMap.class);
                ParameterRequestWrapper parameterRequestWrapper = new ParameterRequestWrapper(request);
                for (Object key : hashMap.keySet()) {
                    parameterRequestWrapper.addParameter(String.valueOf(key),  hashMap.get(key));
                }

                servletRequest = parameterRequestWrapper;
                request = (HttpServletRequest) servletRequest;
            }

            //从session中获取生成的验证码
            String verifyCode = session.getAttribute("verifyCode").toString();

            if (captchaEnable && !verifyCode.toLowerCase().equals(request.getParameter("captcha").toLowerCase())) {
                String dataString = "{\"code\":\"400\",\"msg\":\"验证码错误\"}";

                //判断api加密开关是否开启
                if("Y".equals(SysSettingUtil.getSysSetting().getSysApiEncrypt())){
                    //api加密
                    Result encrypt = ApiSecurityUtil.encrypt(dataString);

                    dataString = JsonUtil.stringify(encrypt);
                }

                //转json字符串并转成Object对象，设置到Result中并赋值给返回值o
                HttpServletResponseUtil.print(response,dataString);
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
