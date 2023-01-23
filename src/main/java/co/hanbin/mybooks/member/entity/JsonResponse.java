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

            if(newTokensObj != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> newTokens = (Map<String, Object>)newTokensObj;
                this.newTokens = newTokens;

                ses.removeAttribute("newTokens");
            }
        }

        this.response = response;
        this.message = message;
        this.data = data;
    }

}