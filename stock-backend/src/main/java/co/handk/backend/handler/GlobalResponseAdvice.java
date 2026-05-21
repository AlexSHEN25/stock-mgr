package co.handk.backend.handler;

import co.handk.backend.annotation.IgnoreResponseWrap;
import co.handk.common.model.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        if (Result.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }
        if (returnType.getDeclaringClass().isAnnotationPresent(IgnoreResponseWrap.class)) {
            return false;
        }
        return !returnType.hasMethodAnnotation(IgnoreResponseWrap.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType mediaType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body == null) {
            return Result.success(null);
        }
        if (body instanceof String || converterType == StringHttpMessageConverter.class) {
            try {
                return objectMapper.writeValueAsString(Result.success(body));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (body instanceof Result) {
            return body;
        }
        return Result.success(body);
    }
}
