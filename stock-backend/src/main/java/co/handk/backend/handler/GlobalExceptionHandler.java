package co.handk.backend.handler;

import co.handk.backend.constant.MessageKeyConstant;
import co.handk.backend.exception.AccessDeniedException;
import co.handk.backend.exception.LoginException;
import co.handk.common.enums.ResultCode;
import co.handk.common.model.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

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
        e.printStackTrace();

        if (e instanceof LoginException ex) {
            return Result.fail(ResultCode.LOGIN_TIME_OUT, ex.getMessageKey(), i18n(ex.getMessageKey()));
        }
        if (e instanceof AccessDeniedException ex) {
            return Result.fail(ResultCode.ERROR, ex.getMessageKey(), i18n(ex.getMessageKey()));
        }

        String messageKey = MessageKeyConstant.ERROR_INTERNAL;
        return Result.fail(ResultCode.ERROR, messageKey, i18n(messageKey));
    }

    private String i18n(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, key, locale);
    }
}
