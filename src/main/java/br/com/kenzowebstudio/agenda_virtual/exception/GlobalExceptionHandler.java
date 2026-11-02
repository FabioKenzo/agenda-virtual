package br.com.kenzowebstudio.agenda_virtual.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import br.com.kenzowebstudio.agenda_virtual.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.dao.DataIntegrityViolationException;
import java.sql.SQLException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
                        ResourceNotFoundException exception,
                        HttpServletRequest request) {

                HttpStatus status = HttpStatus.NOT_FOUND;

                ApiErrorResponse error = new ApiErrorResponse(
                                LocalDateTime.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(status).body(error);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidationException(
                        MethodArgumentNotValidException exception,
                        HttpServletRequest request) {

                HttpStatus status = HttpStatus.BAD_REQUEST;

                String message = exception.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.joining("; "));

                ApiErrorResponse error = new ApiErrorResponse(
                                LocalDateTime.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                message,
                                request.getRequestURI());

                return ResponseEntity.status(status).body(error);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException exception,
                        HttpServletRequest request) {

                HttpStatus status = HttpStatus.BAD_REQUEST;

                ApiErrorResponse error = new ApiErrorResponse(
                                LocalDateTime.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                "Corpo da requisição inválido.",
                                request.getRequestURI());

                return ResponseEntity.status(status).body(error);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiErrorResponse> handleIllegalArgument(
                        IllegalArgumentException exception,
                        HttpServletRequest request) {

                HttpStatus status = HttpStatus.BAD_REQUEST;

                ApiErrorResponse error = new ApiErrorResponse(
                                LocalDateTime.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(status).body(error);
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(
                        MaxUploadSizeExceededException exception,
                        HttpServletRequest request) {

                HttpStatusCode status = HttpStatusCode.valueOf(413);

                ApiErrorResponse error = new ApiErrorResponse(
                                LocalDateTime.now(),
                                status.value(),
                                "Payload Too Large",
                                "O arquivo enviado ultrapassa o tamanho máximo permitido.",
                                request.getRequestURI());

                return ResponseEntity.status(status).body(error);
        }

        @ExceptionHandler(ResourceConflictException.class)
        public ResponseEntity<ApiErrorResponse> handleResourceConflict(
                        ResourceConflictException exception,
                        HttpServletRequest request) {

                HttpStatus status = HttpStatus.CONFLICT;

                ApiErrorResponse error = new ApiErrorResponse(
                                LocalDateTime.now(),
                                status.value(),
                                status.getReasonPhrase(),
                                exception.getMessage(),
                                request.getRequestURI());

                return ResponseEntity.status(status).body(error);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
                        DataIntegrityViolationException exception,
                        HttpServletRequest request) {

                Throwable cause = exception;

                while (cause != null) {

                        if (cause instanceof SQLException sqlException
                                        && "23503".equals(sqlException.getSQLState())) {

                                HttpStatus status = HttpStatus.CONFLICT;

                                ApiErrorResponse error = new ApiErrorResponse(
                                                LocalDateTime.now(),
                                                status.value(),
                                                status.getReasonPhrase(),
                                                "Não foi possível concluir a operação porque o registro está sendo utilizado por outro recurso.",
                                                request.getRequestURI());

                                return ResponseEntity.status(status).body(error);
                        }

                        cause = cause.getCause();
                }

                throw exception;
        }

}
