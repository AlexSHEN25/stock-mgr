package co.handk.backend.config;

import co.handk.backend.interceptor.LoginInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(
                        "/brand/**",
                        "/series/**",
                        "/maker/**",
                        "/goodsType/**",
                        "/goods/**",
                        "/warehouse/**",
                        "/customerLevel/**",
                        "/customer/**",
                        "/dept/**",
                        "/stock/**",
                        "/stockRecord/**",
                        "/stockOrder/**",
                        "/stockOrderItem/**",
                        "/priceRecord/**",
                        "/requestForm/**",
                        "/requestItem/**",
                        "/message/**",
                        "/user/**",
                        "/role/**",
                        "/permission/**",
                        "/userRole/**",
                        "/rolePermission/**",
                        "/userToken/**",
                        "/config/**",
                        "/operateLog/**"
                )
                .excludePathPatterns(
                        "/user/login",
                        "/error",
                        "/",
                        "/home",
                        "/index.html",
                        "/favicon.ico",
                        "/**/*.css",
                        "/**/*.js",
                        "/**/*.map",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.jpeg",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.woff",
                        "/**/*.woff2",
                        "/**/*.ttf",
                        "/assets/**",
                        "/icons/**"
                );
    }
}
