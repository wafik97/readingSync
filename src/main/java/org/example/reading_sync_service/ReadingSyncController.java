package org.example.reading_sync_service;

import org.example.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
public class ReadingSyncController {

    @GetMapping("/users")
    public Map<String, List<User>> getAllUsers() {
        return PageSyncHandler.rooms.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(session -> PageSyncHandler.userSessions.get(session.getId()))
                                .filter(user -> user != null)
                                .collect(Collectors.toList())
                ));
    }

    @GetMapping("/users/{room}")
    public List<User> getUsersByRoom(@PathVariable String room) {
        return PageSyncHandler.rooms.getOrDefault(room, new CopyOnWriteArrayList<>()).stream()
                .map(session -> PageSyncHandler.userSessions.get(session.getId()))
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }
}
