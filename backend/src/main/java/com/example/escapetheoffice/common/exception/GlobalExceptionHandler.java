package com.example.escapetheoffice.common.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * アプリ全体の例外を RFC 9457（ProblemDetail）形式のレスポンスに変換する。
 * ResponseEntityExceptionHandler を継承し、Spring 標準の例外処理を引き継いだ上で
 * 必要なものだけ上書きする。
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /** バリデーション失敗。標準では項目ごとの詳細が入らないため errors として補う */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new FieldErrorDetail(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()))
                .toList();

        return toProblemDetail(ex, errors, headers, status, request);
    }

    /**
     * 引数そのものに制約を付けた場合（例: @PathVariable @PastOrPresent）は、
     * 本文の検証もこちらの経路に統合されるため、同じ形の errors を組み立て直す。
     * 上のメソッドを上書きしただけでは拾えない。
     */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<FieldErrorDetail> errors = new ArrayList<>();

        for (ParameterValidationResult result : ex.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                // @RequestBody の DTO。中に項目ごとのエラーが入っている
                parameterErrors.getFieldErrors().forEach(fieldError ->
                        errors.add(new FieldErrorDetail(
                                fieldError.getField(),
                                fieldError.getDefaultMessage())));
            } else {
                // @PathVariable など、項目を持たない引数そのもののエラー
                String parameterName = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(resolvable ->
                        errors.add(new FieldErrorDetail(
                                parameterName,
                                resolvable.getDefaultMessage())));
            }
        }

        return toProblemDetail(ex, errors, headers, status, request);
    }

    /** 対象データなし */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 検証失敗のレスポンスは経路が違っても同じ形にする。
     * Spring が組み立てた ProblemDetail を受け取って加工するだけにし、
     * status や title を自前で埋め直さない。
     */
    private <E extends Exception & ErrorResponse> ResponseEntity<Object> toProblemDetail(
            E ex,
            List<FieldErrorDetail> errors,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ProblemDetail body = ex.getBody();
        body.setDetail("入力内容に誤りがあります");
        body.setProperty("errors", errors);

        return handleExceptionInternal(ex, body, headers, status, request);
    }

    public record FieldErrorDetail(String field, String message) {
    }
}
