package co.hanbin.mybooks.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum ExceptionEnum {
    RUNTIME_EXCEPTION(HttpStatus.BAD_REQUEST, "E0001"),
    ACCESS_DENIED_EXCEPTION(HttpStatus.UNAUTHORIZED, "E0002"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E0003"),
    NO_USER_OR_WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "E0004"),

    JWT_ERROR(HttpStatus.UNAUTHORIZED, "A0001", "JWT error."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A0002", "The access token was expired."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A0003", "The refresh token was expired."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "A0004", "The refresh token is invalid."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A0005", "The refresh token is invalid.");

    private final HttpStatus status;
    private final String code;
    private String message;

    ExceptionEnum(HttpStatus status, String code) {
        this.status = status;
        this.code = code;
    }

    ExceptionEnum(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}