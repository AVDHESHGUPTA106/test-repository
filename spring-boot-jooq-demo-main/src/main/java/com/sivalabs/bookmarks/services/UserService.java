package com.sivalabs.bookmarks.services;

import com.sivalabs.bookmarks.repositories.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public String getUserNameById(Long id) {
        return userRepository.findUserNameById(id);
    }
    
    public void demonstrateUserAccess() {
        try {
            // Example: Get user with ID 1
            String userName = getUserNameById(1L);
            System.out.println("Found user: " + userName);
        } catch (Exception e) {
            System.out.println("No user found with ID 1: " + e.getMessage());
        }
    }
}
