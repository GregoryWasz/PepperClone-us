package com.pepper.backend.service;

import com.pepper.backend.exception.AlreadyExistException;
import com.pepper.backend.exception.ResourceNotFoundException;
import com.pepper.backend.exception.UnauthenticatedException;
import com.pepper.backend.model.Role;
import com.pepper.backend.model.User;
import com.pepper.backend.repository.RoleRepository;
import com.pepper.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private void ValidateLoggedUser(String userForChange){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInUserName = authentication.getName();
        if(!loggedInUserName.equals(userForChange)){
            throw new UnauthenticatedException("Sorry " + loggedInUserName + " you can't change " + userForChange + " items" );
        }
    }

    private void UsernameValidation(User user){
        boolean matchName = userRepository.findAll().stream()
                .anyMatch(foundedUsername -> foundedUsername.getUsername().equals(user.getUsername()));
        if(matchName){
            throw new AlreadyExistException("User with that name already exist");
        }
    }

    private void EmailValidation(User user){
        boolean matchEmail = userRepository.findAll().stream()
                .anyMatch(foundedEmail -> foundedEmail.getEmail().equals(user.getEmail()));
        if(matchEmail){
            throw new AlreadyExistException("Email already exist");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityExistsException("User " + username + " doesn't exist"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User addUser(User user){
        UsernameValidation(user);
        EmailValidation(user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not exist, contact administrator"));
        user.setRole(role);
        return userRepository.save(user);
    }

    public ResponseEntity<Map<String,Boolean>> deleteUser(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id:" + id + " not exist"));
        userRepository.delete(user);
        Map<String, Boolean> response = new HashMap<>();
        response.put("Delete", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<String> changeUsername(long id, User updatedUser) {
        UsernameValidation(updatedUser);
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id:" + id + " not exist"));
        ValidateLoggedUser(oldUser.getUsername());
        oldUser.setUsername(updatedUser.getUsername());
        userRepository.save(oldUser);
        return ResponseEntity.ok("Username changed");
    }

    public ResponseEntity<String> changeEmail(long id, User updatedUser) {
        EmailValidation(updatedUser);
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id:" + id + " not exist"));
        ValidateLoggedUser(oldUser.getUsername());
        oldUser.setEmail(updatedUser.getEmail());
        userRepository.save(oldUser);
        return ResponseEntity.ok("Email changed");
    }

    public ResponseEntity<String> changePassword(long id, User updatedUser) {
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id:" + id + " not exist"));
        ValidateLoggedUser(oldUser.getUsername());
        oldUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        userRepository.save(oldUser);
        return ResponseEntity.ok("Password changed");
    }
}
