package com.videoplatform.controller;

import com.videoplatform.dto.UserProfileDTO;
import com.videoplatform.model.User;
import com.videoplatform.repository.UserRepository;
import com.videoplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;


    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateUserProfile(@AuthenticationPrincipal User currentUser,
                                               @RequestBody User updatedData) {
        Optional<User> userOpt = userRepository.findById(currentUser.getId());
        if (userOpt.isEmpty()) return ResponseEntity.notFound().build();

        User user = userOpt.get();
        user.setBio(updatedData.getBio());
        user.setAvatarUrl(updatedData.getAvatarUrl());
        user.setCoverUrl(updatedData.getCoverUrl());

        userRepository.save(user);
        return ResponseEntity.ok("Профиль обновлён");
    }
    @GetMapping("/{id}/profile")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long id, Principal principal) {
        return ResponseEntity.ok(userService.getUserProfile(id, principal));
    }

    @PostMapping("/{id}/subscribe")
    public ResponseEntity<?> subscribe(@PathVariable Long id, Principal principal) {
        userService.subscribe(id, principal);
        return ResponseEntity.ok("Вы подписались");
    }

    @PostMapping("/{id}/unsubscribe")
    public ResponseEntity<?> unsubscribe(@PathVariable Long id, Principal principal) {
        userService.unsubscribe(id, principal);
        return ResponseEntity.ok("Вы отписались");
    }
}
