package com.videoplatform.repository;

import com.videoplatform.model.Donation;
import com.videoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByToUser(User toUser);
}