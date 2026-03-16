package co.handk.backend.handler;

import co.handk.backend.annotation.IgnoreResponseWrap;
import co.handk.common.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 是否需要处理
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 已经是 Result 不再包装
        if (Result.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }
        // 方法或类标注 IgnoreResponseWrap 不处理
        if (returnType.getDeclaringClass().isAnnotationPresent(IgnoreResponseWrap.class)) {
            return false;
        }
        return !returnType.hasMethodAnnotation(IgnoreResponseWrap.class);
    }

    /**
     * 返回前统一包装
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType mediaType, Class converterType, ServerHttpRequest request, ServerHttpResponse response) {

        // null 返回
        if (body == null) {
            return Result.success(null);
        }

        // String 类型特殊处理
        if (body instanceof String || converterType == StringHttpMessageConverter.class) {
            try {
                return objectMapper.writeValueAsString(Result.success(body));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // 已经是 Result
        if (body instanceof Result) {
            return body;
        }

        return Result.success(body);
    }
}