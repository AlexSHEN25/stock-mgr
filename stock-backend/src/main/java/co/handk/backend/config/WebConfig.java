package co.handk.backend.config;

import co.handk.backend.interceptor.LoginInterceptor;
import co.handk.backend.interceptor.PermissionInterceptor;
import co.handk.backend.interceptor.IdempotencyInterceptor;
import co.handk.common.enums.DeleteEnum;
import co.handk.common.enums.StatusEnum;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;
    @Resource
    private PermissionInterceptor permissionInterceptor;
    @Resource
    private IdempotencyInterceptor idempotencyInterceptor;
    @Resource
    private AvatarStorageProperties avatarStorageProperties;
    @Resource
    private GoodsImageStorageProperties goodsImageStorageProperties;
    @Resource
    private BrandImageStorageProperties brandImageStorageProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(
                        "/brand/**",
                        "/brandMakerRelation/**",
                        "/category/**",
                        "/series/**",
                        "/maker/**",
                        "/stockType/**",
                        "/goods/**",
                        "/goodsSku/**",
                        "/goodsSkuSpec/**",
                        "/goodsImage/**",
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
                        "/file/**",
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

        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
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

        registry.addInterceptor(idempotencyInterceptor)
                .addPathPatterns("/**")
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
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String root = avatarStorageProperties.getRootDir().toUri().toString();
        if (!root.endsWith("/")) {
            root = root + "/";
        }
        String uploadRoot = avatarStorageProperties.getUploadDir().toUri().toString();
        if (!uploadRoot.endsWith("/")) {
            uploadRoot = uploadRoot + "/";
        }
        registry.addResourceHandler("/avatar/upload/**")
                .addResourceLocations(uploadRoot, root);
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations(root, "classpath:/static/avatar/");

        String goodsRoot = goodsImageStorageProperties.getRootDir().toUri().toString();
        if (!goodsRoot.endsWith("/")) {
            goodsRoot = goodsRoot + "/";
        }
        registry.addResourceHandler("/upload/goods/**")
                .addResourceLocations(goodsRoot);

        String brandRoot = brandImageStorageProperties.getRootDir().toUri().toString();
        if (!brandRoot.endsWith("/")) {
            brandRoot = brandRoot + "/";
        }
        registry.addResourceHandler("/upload/brand/**")
                .addResourceLocations(brandRoot);
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, StatusEnum.class, source -> {
            if (source.isBlank()) {
                return null;
            }
            try {
                return StatusEnum.fromValue(Integer.valueOf(source));
            } catch (NumberFormatException ignore) {
                return StatusEnum.valueOf(source);
            }
        });
        registry.addConverter(String.class, DeleteEnum.class, source -> {
            if (source.isBlank()) {
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
