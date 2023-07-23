package com.example.java_websocket_demo.web_socket;

import com.example.java_websocket_demo.web_socket.action_handler.AbstractActionHandler;
import com.example.java_websocket_demo.web_socket.entity.Context;
import com.example.java_websocket_demo.web_socket.entity.LoginEntity;
import com.example.java_websocket_demo.web_socket.entity.Payload;
import com.example.java_websocket_demo.web_socket.enums.UserStateEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class SocketHandler extends AbstractWebSocketHandler {
    ObjectMapper objectMapper;
    Context context;
    Map<String, AbstractActionHandler<?>> handlerMap;

    @Autowired
    public SocketHandler(ObjectMapper objectMapper, Context context, Map<String, AbstractActionHandler<?>> handlerMap) {
        this.objectMapper = objectMapper;
        this.context = context;
        this.handlerMap = handlerMap;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String id = session.getId();
        context.getUserStateMap().putIfAbsent(id, UserStateEnum.UN_LOGIN);
        log.info("{} connected", id);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String id = session.getId();
        Map<String, LoginEntity> sessionMap = context.getSessionMap();
        Map<String, Set<WebSocketSession>> topicMap = context.getTopicMap();
        String topic = Optional.ofNullable(sessionMap.get(id)).map(LoginEntity::getTopic).orElse("");
        sessionMap.remove(session.getId());
        topicMap.getOrDefault(topic, Collections.emptySet()).remove(session);
        context.getUserStateMap().remove(id);
        log.info("{} disconnected, leave topic {}", id, topic);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Payload payload = objectMapper.readValue(message.getPayload(), Payload.class);
        String action = payload.getAction();
        log.info("action: {}", action);
        AbstractActionHandler<?> handler = handlerMap.get(action);
        if (handler == null) {
            log.error("unknown action");
            return;
        }
        handler.process(session, payload.getData());
    }
}
