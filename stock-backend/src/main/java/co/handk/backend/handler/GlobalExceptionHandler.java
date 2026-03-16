package co.handk.backend.handler;

import co.handk.common.enums.ResultCode;
import co.handk.common.model.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // TODD 实际项目这里应该写日志
        e.printStackTrace();
        return Result.fail(ResultCode.ERROR.getCode(), e.getMessage());
    }
}