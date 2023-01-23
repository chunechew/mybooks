package co.hanbin.mybooks.member.entity;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsonResponse {
    private String response;
    private String message;
    private Object data;
    private Map<String, Object> newTokens;

    public JsonResponse(String response, String message, Object data) {
        ServletRequestAttributes attr = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = null;
        
        if(attr != null) {
            req = attr.getRequest();
            HttpSession ses = req.getSession();

            Object newTokensObj = ses.getAttribute("newTokens");

            // JwtAuthenticationFilter나 MemberController.refreshToken(...)에서 넘어온 새 JWT 정보가 있으면 JSON에 추가
            if(newTokensObj != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> newTokens = (Map<String, Object>)newTokensObj;
                this.newTokens = newTokens;

                ses.removeAttribute("newTokens"); // 세션에서 임시 값 제거
            }
        }

        this.response = response;
        this.message = message;
        this.data = data;
    }

}