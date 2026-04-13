package co.handk.backend.config;

import co.handk.backend.interceptor.LoginInterceptor;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
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
                        "/brandMakerRelation/**",
                        "/category/**",
                        "/series/**",
                        "/maker/**",
                        "/goodsType/**",
                        "/goods/**",
                        "/goodsSku/**",
                        "/goodsSkuSpec/**",
                        "/goodsImage/**",
                        "/warehouse/**",
                        "/customerLevel/**",
                        "/customer/**",
                        "/goodsLevelPrice/**",
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

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, StatusEnum.class, source -> {
            if (source == null || source.isBlank()) {
                return null;
            }
            try {
                return StatusEnum.fromValue(Integer.valueOf(source));
            } catch (NumberFormatException ignore) {
                return StatusEnum.valueOf(source);
            }
        });
        registry.addConverter(String.class, DeleteEnum.class, source -> {
            if (source == null || source.isBlank()) {
                return null;
            }
            try {
                return DeleteEnum.fromValue(Integer.valueOf(source));
            } catch (NumberFormatException ignore) {
                return DeleteEnum.valueOf(source);
            }
        });
    }
}
