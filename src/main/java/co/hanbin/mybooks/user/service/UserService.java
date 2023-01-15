package co.hanbin.mybooks.user.service;

import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import co.hanbin.mybooks.user.entity.User;
import co.hanbin.mybooks.user.repository.UserRepository;

@Component
public class UserService {
	
	private UserRepository usersRepository;

    public UserService(UserRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public List<User> getUsers() {
        return usersRepository.findAll();
    }
    
    public User saveUser(User users) {
    	users.setUserNo(new Random().nextInt());
    	return usersRepository.save(users);
    }

}