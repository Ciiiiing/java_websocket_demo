package com.example.java_websocket_demo.web_socket.action_handler.impl;

import com.example.java_websocket_demo.web_socket.action_handler.AbstractActionHandler;
import com.example.java_websocket_demo.web_socket.entity.Context;
import com.example.java_websocket_demo.web_socket.entity.LoginEntity;
import com.example.java_websocket_demo.web_socket.entity.SendMessageEntity;
import com.example.java_websocket_demo.web_socket.enums.UserStateEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service("login")
public class LoginHandler extends AbstractActionHandler<LoginEntity> {
    public LoginHandler(ObjectMapper objectMapper, Context context) {
        super(objectMapper, context);
    }

    @Override
    protected boolean preCheck(WebSocketSession session) throws Exception {
        UserStateEnum userStateEnum = context.getUserStateMap().get(session.getId());
        return Objects.equals(userStateEnum, UserStateEnum.UN_LOGIN);
    }

    @Override
    protected LoginEntity convert(String data) throws JsonProcessingException {
        return mapper.readValue(data, LoginEntity.class);
    }

    @Override
    protected void process_inner(WebSocketSession session, LoginEntity data) throws IOException {
        String id = session.getId();
        Map<String, LoginEntity> sessionMap = context.getSessionMap();
        Map<String, Set<WebSocketSession>> topicMap = context.getTopicMap();
        BlockingQueue<SendMessageEntity> histories = context.getHistories();
        sessionMap.putIfAbsent(id, data);
        topicMap.computeIfAbsent(data.getTopic(), k -> ConcurrentHashMap.newKeySet()).add(session);
        context.getUserStateMap().put(id, UserStateEnum.LOGIN);
        for (SendMessageEntity history : histories) {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(history)));
        }
        log.info("user: {} log in, pushed {} history text to session {}", data.getName(), histories.size(), id);
    }
}
