package com.recipegen.controller;

import com.recipegen.service.BookmarkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookmarks")
@CrossOrigin
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @GetMapping
    public List<Map<String, String>> getBookmarks() {
        return bookmarkService.getAllBookmarks();
    }

    @PostMapping
    public ResponseEntity<?> addBookmark(@RequestBody Map<String, String> recipe) {
        boolean added = bookmarkService.addBookmark(recipe);
        return ResponseEntity.ok(Map.of("success", added));
    }

    @DeleteMapping
    public ResponseEntity<?> removeBookmark(@RequestBody Map<String, String> recipe) {
        boolean removed = bookmarkService.removeBookmark(recipe);
        return ResponseEntity.ok(Map.of("removed", removed));
    }
}
