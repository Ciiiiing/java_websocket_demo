package com.example.java_websocket_demo.web_socket.entity;

import com.example.java_websocket_demo.web_socket.enums.UserStateEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class Context {
    Map<String, LoginEntity> sessionMap = new ConcurrentHashMap<>();
    Map<String, Set<WebSocketSession>> topicMap = new ConcurrentHashMap<>();
    Map<String, UserStateEnum> userStateMap = new ConcurrentHashMap<>();
    BlockingQueue<SendMessageEntity> histories = new ArrayBlockingQueue<>(5, true);
}
