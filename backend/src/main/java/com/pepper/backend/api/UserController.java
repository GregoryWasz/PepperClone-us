package com.pepper.backend.api;


import com.pepper.backend.dto.UsernameAndEmailDto;
import com.pepper.backend.dto.UsernameDto;
import com.pepper.backend.model.User;
import com.pepper.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("")
    public List<UsernameAndEmailDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UsernameAndEmailDto getUserById(@PathVariable Long id) {
        return userService.getUsernameAndEmailById(id);
    }

    @PostMapping("/register")
    public User addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @DeleteMapping("")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> deleteUser(Principal principal) {
        return userService.deleteUser(principal);
    }

    @PatchMapping("/change/username/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> changeUsername(@PathVariable long id, @RequestBody User user) {
        return userService.changeUsername(id, user);
    }

    @PatchMapping("/change/email/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> changeEmail(@PathVariable long id, @RequestBody User user) {
        return userService.changeEmail(id, user);
    }

    @PatchMapping("/change/password/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> changePassword(@PathVariable long id, @RequestBody User user) {
        return userService.changePassword(id, user);
    }

    @GetMapping("/currentuser")
    public ResponseEntity<?> currentUser(Principal principal) {
        return userService.currentUser(principal);
    }

    @GetMapping("/search")
    public List<UsernameDto> currentUser(@RequestParam String q) {
        return userService.findUsersByUsernameContains(q);
    }
}
