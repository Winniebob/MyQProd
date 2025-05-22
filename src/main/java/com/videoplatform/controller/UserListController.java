package com.videoplatform.controller;

import com.videoplatform.dto.*;
import com.videoplatform.service.UserListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lists")
@RequiredArgsConstructor
public class UserListController {

    private final UserListService service;

    @GetMapping
    public List<UserListDTO> myLists(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        return service.getMyLists(user.getUsername());
    }

    @PostMapping
    public UserListDTO create(@Valid @RequestBody CreateUserListRequest req,
                              @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        return service.createList(req, user.getUsername());
    }

    @PostMapping("/{id}/members")
    public UserListDTO addMember(@PathVariable Long id,
                                 @RequestBody ModifyListMembersRequest req,
                                 @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        return service.addMember(id, req, user.getUsername());
    }

    @DeleteMapping("/{id}/members")
    public UserListDTO removeMember(@PathVariable Long id,
                                    @RequestBody ModifyListMembersRequest req,
                                    @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        return service.removeMember(id, req, user.getUsername());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        service.deleteList(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}