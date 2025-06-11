package com.recipegen.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookmarkService {

    private final Set<Map<String, String>> bookmarks = ConcurrentHashMap.newKeySet();

    public List<Map<String, String>> getAllBookmarks() {
        return new ArrayList<>(bookmarks);
    }

    public boolean addBookmark(Map<String, String> recipe) {
        return bookmarks.add(recipe);
    }

    public boolean removeBookmark(Map<String, String> recipe) {
        return bookmarks.remove(recipe);
    }

    public void clearAll() {
        bookmarks.clear();
    }
}
