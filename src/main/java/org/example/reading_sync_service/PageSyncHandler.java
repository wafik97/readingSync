package org.example.reading_sync_service;

import org.example.model.User;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.reading_sync_service.JsonRoomManager;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class PageSyncHandler extends TextWebSocketHandler {

    static final Map<String, CopyOnWriteArrayList<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    static {
        rooms.put("room1", new CopyOnWriteArrayList<>());
        rooms.put("room2", new CopyOnWriteArrayList<>());
        rooms.put("room3", new CopyOnWriteArrayList<>());
        rooms.put("room4", new CopyOnWriteArrayList<>());
    }
    static final Map<String, User> userSessions = new ConcurrentHashMap<>();

    private static final int MAX_USERS_PER_ROOM = 5;
    private final ObjectMapper objectMapper = new ObjectMapper();
    JsonRoomManager roomManager ;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        roomManager = JsonRoomManager.getInstance();
        String room = "room4";  // Default room
        CopyOnWriteArrayList<WebSocketSession> roomSessions = rooms.computeIfAbsent(room, k -> new CopyOnWriteArrayList<>());

        if (roomSessions.size() >= MAX_USERS_PER_ROOM) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Room is full\"}"));
            session.close();
            return;
        }

        // Create the user but don't assign a userId yet
        User user = new User(session.getId(), room, 1);
        userSessions.put(session.getId(), user);
        roomSessions.add(session);

        sendMessageToRoom(room, "{\"type\":\"user_join\",\"userId\":\"" + user.getId() + "\",\"page\":1}");
        sendUserListUpdate(room);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        boolean exists = false;
        String payload = message.getPayload();
        User user = userSessions.get(session.getId());

        if (user == null) return;

        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String type = (String) data.get("type");

            if ("user_join".equals(type)) {
                // When receiving the user_join message, update userId

                String userId = (String) data.get("userId");
                String room = (String) data.get("room");
                String pdfName = (String) data.get("pdfName");
              //   Map<String, User> userSession = userSessions ;
                exists = userSessions.values().stream().anyMatch(myUser -> userId.equals(myUser.getId())&&room.equals(myUser.getRoom()));
                if(exists){
                    session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Username already exists\"}"));
                    my_afterConnectionClosed( session);
                    return;
                }
                user.setId(userId);  // Update userId
                user.setRoom(room);

                if(rooms.get(room).size()==MAX_USERS_PER_ROOM){
                    session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Room is full\"}"));
                    my_afterConnectionClosed( session);
                    return;
                }
                updateRoom(session, room,user);
                if(!roomManager.getRoomValue( room).equals(pdfName)){
                    roomManager.setRoomValue(room,pdfName);
                }



                sendMessageToRoom(user.getRoom(), "{\"type\":\"user_join\",\"userId\":\"" + user.getId() + "\",\"page\":1}");
                sendUserListUpdate(room);

            }

            if ("page_update".equals(type)) {
                int page = (int) data.get("page");
                user.setCurrentPage(page);
                sendMessageToRoom(user.getRoom(), "{\"type\":\"page_update\",\"userId\":\"" + user.getId() + "\",\"page\":" + page + "}");
            }

        } catch (Exception e) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Invalid message format\"}"));
        }
    }


    public void updateRoom(WebSocketSession session, String room, User remove) throws IOException {
        CopyOnWriteArrayList<WebSocketSession> roomSessions = rooms.computeIfAbsent(room, k -> new CopyOnWriteArrayList<>());
        if (roomSessions.size() >= MAX_USERS_PER_ROOM) {

            roomSessions = rooms.computeIfAbsent("room4", k -> new CopyOnWriteArrayList<>());
            roomSessions.clear();
            session.close();
            return;
        }


        User user = new User(remove.getId(), room, 1);

        userSessions.remove(remove.getId());
        userSessions.put(remove.getId(), user);
        roomSessions.add(session);

        roomSessions = rooms.computeIfAbsent("room4", k -> new CopyOnWriteArrayList<>());
        roomSessions.clear();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        User user = userSessions.remove(session.getId());

        if (user != null) {
            userSessions.remove(user.getId());
            rooms.get(user.getRoom()).remove(session);

            if(rooms.get(user.getRoom()).isEmpty()){
                roomManager.setRoomValue(user.getRoom(),"none");
            }

            sendMessageToRoom(user.getRoom(), "{\"type\":\"user_leave\",\"userId\":\"" + user.getId() + "\"}");
            sendUserListUpdate(user.getRoom());
        }
    }

    public void my_afterConnectionClosed(WebSocketSession session) throws IOException {
        User user = userSessions.remove(session.getId());
        if (user != null) {
            rooms.get(user.getRoom()).remove(session);

            if(rooms.get(user.getRoom()).isEmpty()){
                roomManager.setRoomValue(user.getRoom(),"none");
            }

            sendMessageToRoom(user.getRoom(), "{\"type\":\"user_leave\",\"userId\":\"" + user.getId() + "\"}");
            sendUserListUpdate(user.getRoom());
        }
        session.close();
    }

    void sendMessageToRoom(String room, String message) throws IOException {
        for (WebSocketSession s : rooms.getOrDefault(room, new CopyOnWriteArrayList<>())) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(message));
            }
        }
    }

    private void sendUserListUpdate(String room) throws IOException {
        StringBuilder userListJson = new StringBuilder("{\"type\":\"user_list\",\"users\":[");

        boolean first = true;
        for (WebSocketSession session : rooms.getOrDefault(room, new CopyOnWriteArrayList<>())) {
            User user = userSessions.get(session.getId());
            if (user != null) {
                if (!first) userListJson.append(",");
                userListJson.append("{\"userId\":\"").append(user.getId()).append("\",\"page\":").append(user.getCurrentPage()).append("}");
                first = false;
            }
        }
        userListJson.append("]}");
        sendMessageToRoom(room, userListJson.toString());
    }
}