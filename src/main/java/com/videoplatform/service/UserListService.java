package com.videoplatform.service;

import com.videoplatform.dto.*;
import com.videoplatform.model.*;
import com.videoplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserListService {

    private final UserListRepository listRepo;
    private final UserRepository userRepo;

    public List<UserListDTO> getMyLists(String username) {
        User me = userRepo.findByUsername(username).orElseThrow();
        return listRepo.findByOwner(me).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public UserListDTO createList(CreateUserListRequest req, String username) {
        User me = userRepo.findByUsername(username).orElseThrow();
        UserList list = UserList.builder()
                .owner(me)
                .name(req.getName())
                .description(req.getDescription())
                .build();
        return toDto(listRepo.save(list));
    }

    @Transactional
    public UserListDTO addMember(Long listId, ModifyListMembersRequest req, String username) {
        UserList list = validateOwnerAndGet(listId, username);
        User member = userRepo.findById(req.getUserId()).orElseThrow();
        list.getMembers().add(member);
        return toDto(list);
    }

    @Transactional
    public UserListDTO removeMember(Long listId, ModifyListMembersRequest req, String username) {
        UserList list = validateOwnerAndGet(listId, username);
        list.getMembers().removeIf(u -> u.getId().equals(req.getUserId()));
        return toDto(list);
    }

    public void deleteList(Long listId, String username) {
        UserList list = validateOwnerAndGet(listId, username);
        listRepo.delete(list);
    }

    private UserList validateOwnerAndGet(Long id, String username) {
        UserList list = listRepo.findById(id).orElseThrow();
        if (!list.getOwner().getUsername().equals(username)) {
            throw new RuntimeException("Нет прав");
        }
        return list;
    }

    private UserListDTO toDto(UserList list) {
        return UserListDTO.builder()
                .id(list.getId())
                .name(list.getName())
                .description(list.getDescription())
                .memberUsernames(
                        list.getMembers().stream()
                                .map(User::getUsername)
                                .collect(Collectors.toSet())
                )
                .build();
    }
}