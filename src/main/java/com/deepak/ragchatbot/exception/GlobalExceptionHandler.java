package com.deepak.ragchatbot.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception exception, HttpStatus httpStatus, WebRequest webRequest) {
        String path = webRequest.getDescription(false).replace("uri=", "");
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                exception.getMessage(),
                path);

        return new ResponseEntity<>(errorResponse, httpStatus);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(IOException exception, WebRequest webRequest) {
        return buildErrorResponse(exception, HttpStatus.INTERNAL_SERVER_ERROR, webRequest);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException exception, WebRequest webRequest) {
        return buildErrorResponse(exception, HttpStatus.BAD_REQUEST, webRequest);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedOperationException(UnsupportedOperationException exception, WebRequest webRequest) {
        return buildErrorResponse(exception, HttpStatus.UNSUPPORTED_MEDIA_TYPE, webRequest);
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException exception, WebRequest webRequest) {
        return buildErrorResponse(exception, HttpStatus.PAYLOAD_TOO_LARGE, webRequest);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
