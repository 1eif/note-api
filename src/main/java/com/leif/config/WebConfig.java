package com.leif.config;


import cn.dev33.satoken.interceptor.SaRouteInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.router.SaRouterUtil;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.Arrays;


@Configuration
public class WebConfig extends WebMvcConfigurationSupport {

    // 注册sa-token的登录拦截器 接口鉴权
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册路由拦截器，自定义验证规则
        registry.addInterceptor(new SaRouteInterceptor((req, res, handler)->{
            // 根据路由划分模块，不同模块不同鉴权
            //排除
            SaRouter.match(Arrays.asList("/**"), Arrays.asList("/common/**", "/register", "/login", "/forget/**"),() -> StpUtil.checkLogin());

        })).addPathPatterns("/**");
    }

}
