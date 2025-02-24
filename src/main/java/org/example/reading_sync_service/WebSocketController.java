//package org.example.reading_sync_service;
//
//import org.example.model.Message;
//import org.example.model.User;
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import org.springframework.messaging.handler.annotation.SendTo;
//import org.springframework.stereotype.Controller;
//
//import java.util.List;
//import java.util.ArrayList;
//
//@Controller
//public class WebSocketController {
//
//    // Store the connected users (could be improved with a more persistent storage mechanism)
//    private List<User> users = new ArrayList<>();
//
//    // Handle the incoming WebSocket message
//    @MessageMapping("/sync")
//    @SendTo("/topic/updates")
//    public Message sendUpdate(Message message) {
//        // Handle the "user_join" message
//        if ("user_join".equals(message.getType())) {
//            User newUser = new User(message.getUserId(), message.getRoom(), message.getPage());
//            users.add(newUser);
//            return new Message("user_join", message.getUserId(), message.getPage(), message.getRoom());
//        }
//
//        // Handle the "user_list" message - send the list of all users in the room
//        if ("user_list".equals(message.getType())) {
//            return new Message("user_list", users);
//        }
//
//
//
//        // If message type is invalid
//        return new Message("error", "Invalid message format");
//    }
//}
