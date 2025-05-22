package com.videoplatform.service;

import com.videoplatform.model.*;
import com.videoplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepo;
    private final UserRepository userRepo;

    @Transactional
    public void addBookmark(Bookmark.TargetType type, Long targetId, String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        bookmarkRepo.findByUserAndTargetTypeAndTargetId(user, type, targetId)
                .orElseGet(() -> {
                    Bookmark b = new Bookmark(null, user, type, targetId, null);
                    return bookmarkRepo.save(b);
                });
    }

    @Transactional
    public void removeBookmark(Bookmark.TargetType type, Long targetId, String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        bookmarkRepo.findByUserAndTargetTypeAndTargetId(user, type, targetId)
                .ifPresent(bookmarkRepo::delete);
    }

    public List<Bookmark> listBookmarks(Bookmark.TargetType type, String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return bookmarkRepo.findByUserAndTargetType(user, type);
    }
}