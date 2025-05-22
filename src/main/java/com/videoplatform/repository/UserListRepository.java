package com.videoplatform.repository;

import com.videoplatform.model.User;
import com.videoplatform.model.UserList;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserListRepository extends JpaRepository<UserList, Long> {
    List<UserList> findByOwner(User owner);
}