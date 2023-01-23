package co.hanbin.mybooks.misc.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hanbin.mybooks.member.entity.JsonResponse;

@RestController
@RequestMapping("/api/misc")
public class MiscController {
    @PostMapping("/test")
    public JsonResponse test(@RequestBody Map<String, Object> reqBody){
        String test = (String)(reqBody.get("test"));
        boolean isAuthenticated = SecurityContextHolder.getContext().getAuthentication().isAuthenticated();
        Map<String, Object> data = new HashMap<>();
        data.put("inputData", test);
        data.put("isAuthenticated", isAuthenticated);

        JsonResponse response = new JsonResponse("success", "성공", data);

        return response;
    }
}
