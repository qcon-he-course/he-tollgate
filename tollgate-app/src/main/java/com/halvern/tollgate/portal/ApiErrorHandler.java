// Partner API - Integration Squad 2021
package com.halvern.tollgate.portal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.halvern.tollgate.intake.CheckFailure;
import com.halvern.tollgate.intake.LodgementRefusedException;
import com.halvern.tollgate.platform.BusinessException;
import com.halvern.tollgate.platform.TollgateException;

/**
 * JSON errors for the partner API.
 *
 * @author Marco Bellini
 */
@RestControllerAdvice(basePackages = "com.halvern.tollgate.portal")
public class ApiErrorHandler {

    @ExceptionHandler(LodgementRefusedException.class)
    public ResponseEntity<?> refused(LodgementRefusedException e) {
        List<String> failures = new ArrayList<>();
        for (CheckFailure f : e.getFailures()) {
            failures.add(f.getMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("error", "LODGEMENT_REFUSED");
        body.put("failures", failures);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler({BusinessException.class, TollgateException.class})
    public ResponseEntity<?> business(Exception e) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", e instanceof BusinessException ? ((BusinessException) e).getCode() : "REJECTED");
        body.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> invalid(MethodArgumentNotValidException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "INVALID");
        List<String> errors = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(fe -> errors.add(fe.getField() + ": " + fe.getDefaultMessage()));
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException e) {
        return ResponseEntity.notFound().build();
    }
}
