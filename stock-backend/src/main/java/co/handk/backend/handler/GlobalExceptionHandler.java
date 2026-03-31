package co.handk.backend.handler;

import co.handk.backend.exception.LoginException;
import co.handk.common.enums.ResultCode;
import co.handk.common.model.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public Result<?> handleValidationException(Exception e) {
        String message;
        if (e instanceof MethodArgumentNotValidException ex) {
            message = ex.getBindingResult().getFieldError() == null
                    ? "参数校验失败"
                    : ex.getBindingResult().getFieldError().getDefaultMessage();
        } else if (e instanceof BindException ex) {
            message = ex.getBindingResult().getFieldError() == null
                    ? "参数绑定失败"
                    : ex.getBindingResult().getFieldError().getDefaultMessage();
        } else {
            message = e.getMessage();
        }
        return Result.fail(ResultCode.ERROR.getCode(), message);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        // TODD 实际项目这里应该写日志
        e.printStackTrace();
        if (e instanceof LoginException) {
            String message = e.getMessage();
            if ("未登录".equals(message) || "登录已过期".equals(message) || "账号已在其他设备登录".equals(message)) {
                return Result.fail(ResultCode.LOGIN_TIME_OUT.getCode(), ResultCode.LOGIN_TIME_OUT.getMessage());
            }
        }
        return Result.fail(ResultCode.ERROR.getCode(), e.getMessage());
    }
}
