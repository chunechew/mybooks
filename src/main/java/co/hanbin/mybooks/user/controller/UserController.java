package co.hanbin.mybooks.user.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.hanbin.mybooks.user.entity.User;
import co.hanbin.mybooks.user.service.UserService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/")
public class UserController {
    @Autowired
    UserService usersService;

    @GetMapping(path = "/listUsers")
    public ResponseEntity<?> listUsers() {
        log.info("UsersController:  list users");
        List<User> resource = usersService.getUsers();
        return ResponseEntity.ok(resource);
    }
	
	@PostMapping(path = "/addUsers")
	public ResponseEntity<?> saveUser(@RequestBody User user) {
        log.info("UsersController:  list users");
        User resource = usersService.saveUser(user);
        return ResponseEntity.ok(resource);
    }
}
