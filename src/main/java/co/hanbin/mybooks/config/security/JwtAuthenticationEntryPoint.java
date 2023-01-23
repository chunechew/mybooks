package co.hanbin.mybooks.config.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
	@Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String exception = (String)request.getAttribute("exception");
        String errorCode;

        log.debug("log: exception: {} ", exception);

        if(exception == null) {
            errorCode = ErrorCodes.UNKNOWN_ERROR;
            setResponse(response, errorCode);
            return;
        }

        /**
         * 토큰 없는 경우
         */
        if(exception.equals(ErrorCodes.NON_LOGIN)) {
            errorCode = ErrorCodes.NON_LOGIN;
            setResponse(response, errorCode);
            return;
        }

        /**
         * 토큰 만료된 경우
         */
        if(exception.equals(ErrorCodes.EXPIRED_TOKEN)) {
            errorCode = ErrorCodes.EXPIRED_TOKEN;
            setResponse(response, errorCode);
            return;
        }

        /**
         * 토큰 시그니처가 다른 경우
         */
        if(exception.equals(ErrorCodes.INVALID_TOKEN)) {
            errorCode = ErrorCodes.INVALID_TOKEN;
            setResponse(response, errorCode);
			return;
        }

		/**
		 * 맞는 계정이 없는 경우
		 */
		if(exception.equals(ErrorCodes.NO_SUCH_ACCOUNT)) {
			errorCode = ErrorCodes.NO_SUCH_ACCOUNT;
            setResponse(response, errorCode);
			return;
		}
    }

    /**
     * 한글 출력을 위해 getWriter() 사용
     */
    private void setResponse(HttpServletResponse response, String errorCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().println("{ \"message\" : \"토큰 오류 발생"
                + "\", \"code\" : \"" + errorCode
                + "\", \"status\" : 500"
                + ", \"errors\" : [ ] }");
    }
}
