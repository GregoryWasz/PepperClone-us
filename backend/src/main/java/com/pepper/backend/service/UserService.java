package com.pepper.backend.service;

import com.pepper.backend.dto.CurrentUserDto;
import com.pepper.backend.dto.MessageDto;
import com.pepper.backend.dto.UsernameAndEmailDto;
import com.pepper.backend.dto.UsernameDto;
import com.pepper.backend.exception.AlreadyExistException;
import com.pepper.backend.exception.ResourceNotFoundException;
import com.pepper.backend.exception.UnauthenticatedException;
import com.pepper.backend.model.Role;
import com.pepper.backend.model.User;
import com.pepper.backend.repository.CommentRepository;
import com.pepper.backend.repository.PostRepository;
import com.pepper.backend.repository.RoleRepository;
import com.pepper.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import javax.transaction.Transactional;
import java.security.Principal;
import java.util.List;


@Transactional
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final CommentRepository commentRepository;

    private final PostRepository postRepository;

    private void LoggedUserValidation(String userForChange){
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

    public List<UsernameAndEmailDto> getAllUsers() {
        return userRepository.findAllProjectedBy();
    }

    public UsernameAndEmailDto getUsernameAndEmailById(long id) {
        return userRepository.findByUserId(id);
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

    public ResponseEntity<?> deleteUser(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User with id:" + principal.getName() + " not exist"));

        commentRepository.deleteAllByUserId(user.getUserId());
        postRepository.deleteAllByUserId(user.getUserId());
        userRepository.delete(user);
        return ResponseEntity.ok(new MessageDto("User successfully deleted"));
    }

    public ResponseEntity<?> changeUsername(long id, User updatedUser) {
        UsernameValidation(updatedUser);
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id:" + id + " not exist"));
        LoggedUserValidation(oldUser.getUsername());
        oldUser.setUsername(updatedUser.getUsername());
        return ResponseEntity.ok(new MessageDto("Username successfully changed"));
    }

    public ResponseEntity<?> changeEmail(long id, User updatedUser) {
        EmailValidation(updatedUser);
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id:" + id + " not exist"));
        LoggedUserValidation(oldUser.getUsername());
        oldUser.setEmail(updatedUser.getEmail());
        userRepository.save(oldUser);
        return ResponseEntity.ok(new MessageDto("Email successfully changed"));
    }

    public ResponseEntity<?> changePassword(long id, User updatedUser) {
        User oldUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id:" + id + " not exist"));
        LoggedUserValidation(oldUser.getUsername());
        oldUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        userRepository.save(oldUser);
        return ResponseEntity.ok(new MessageDto("Password successfully changed"));
    }

    public ResponseEntity<?> currentUser(Principal principal){
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User with name:" + principal.getName() + " not exist"));
        return ResponseEntity.ok(new CurrentUserDto(user.getUserId(), user.getUsername()));
    }

    public List<UsernameDto> findUsersByUsernameContains(String username){
        return userRepository.findTop5ByUsernameContains(username);
    }
}
