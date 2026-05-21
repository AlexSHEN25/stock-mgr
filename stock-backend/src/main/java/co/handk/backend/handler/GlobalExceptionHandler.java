package co.handk.backend.handler;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.exception.AccessDeniedException;
import co.handk.backend.exception.BusinessException;
import co.handk.backend.exception.LoginException;
import co.handk.common.enums.ResultCode;
import co.handk.common.model.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private static final Locale JAPANESE_LOCALE = Locale.JAPAN;

    private final MessageSource messageSource;

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class})
    public Result<?> handleValidationException(Exception e) {
        String message;
        if (e instanceof MethodArgumentNotValidException ex) {
            message = ex.getBindingResult().getFieldError() == null
                    ? i18n(MessageKeyConstant.ERROR_VALIDATE)
                    : ex.getBindingResult().getFieldError().getDefaultMessage();
        } else if (e instanceof BindException ex) {
            message = ex.getBindingResult().getFieldError() == null
                    ? i18n(MessageKeyConstant.ERROR_VALIDATE)
                    : ex.getBindingResult().getFieldError().getDefaultMessage();
        } else {
            message = i18n(MessageKeyConstant.ERROR_VALIDATE);
        }
        return Result.fail(ResultCode.VALIDATE_ERROR, MessageKeyConstant.ERROR_VALIDATE, message);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("Unhandled exception captured by GlobalExceptionHandler", e);

        if (e instanceof LoginException ex) {
            return Result.fail(ResultCode.LOGIN_TIME_OUT, ex.getMessageKey(), i18n(ex.getMessageKey()));
        }
        if (e instanceof AccessDeniedException ex) {
            return Result.fail(ResultCode.FORBIDDEN, ex.getMessageKey(), i18n(ex.getMessageKey()));
        }
        if (e instanceof BusinessException ex) {
            return Result.fail(ResultCode.ERROR, ex.getMessageKey(), safeMessage(ex));
        }
        if (e instanceof DataAccessException) {
            return Result.fail(ResultCode.ERROR, MessageKeyConstant.ERROR_RUNTIME, safeMessage(e));
        }
        if (e instanceof RuntimeException) {
            return Result.fail(ResultCode.ERROR, MessageKeyConstant.ERROR_RUNTIME, safeMessage(e));
        }
        return Result.fail(ResultCode.ERROR, MessageKeyConstant.ERROR_INTERNAL, i18n(MessageKeyConstant.ERROR_INTERNAL));
    }

    private String i18n(String key) {
        return messageSource.getMessage(key, null, key, JAPANESE_LOCALE);
    }

    private String safeMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            return i18n(MessageKeyConstant.ERROR_INTERNAL);
        }
        return message;
    }
}
