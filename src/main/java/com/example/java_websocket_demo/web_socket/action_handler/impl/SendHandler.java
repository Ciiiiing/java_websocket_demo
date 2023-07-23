package com.example.java_websocket_demo.web_socket.action_handler.impl;

import com.example.java_websocket_demo.web_socket.action_handler.AbstractActionHandler;
import com.example.java_websocket_demo.web_socket.entity.Context;
import com.example.java_websocket_demo.web_socket.entity.LoginEntity;
import com.example.java_websocket_demo.web_socket.entity.ReceiveMessageEntity;
import com.example.java_websocket_demo.web_socket.entity.SendMessageEntity;
import com.example.java_websocket_demo.web_socket.enums.UserStateEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

@Slf4j
@Service("send")
public class SendHandler extends AbstractActionHandler<ReceiveMessageEntity> {
    public SendHandler(ObjectMapper objectMapper, Context context) {
        super(objectMapper, context);
    }

    @Override
    protected boolean preCheck(WebSocketSession session) throws Exception {
        UserStateEnum userStateEnum = context.getUserStateMap().get(session.getId());
        return Objects.equals(userStateEnum, UserStateEnum.LOGIN);
    }

    @Override
    protected ReceiveMessageEntity convert(String data) throws Exception {
        return mapper.readValue(data, ReceiveMessageEntity.class);
    }

    @Override
    protected void process_inner(WebSocketSession session, ReceiveMessageEntity data) throws Exception {
        String id = session.getId();
        LoginEntity loginEntity = context.getSessionMap().get(id);
        BlockingQueue<SendMessageEntity> histories = context.getHistories();
        String topic = loginEntity.getTopic();
        String message = data.getMessage();
        log.info("Received from: {}, message: {}, send to topic: {}", id, message, topic);
        SendMessageEntity sendMessageEntity = SendMessageEntity.builder()
                .userName(loginEntity.getName())
                .message(message)
                .build();
        while (!histories.offer(sendMessageEntity)) {
            histories.poll();
        }
        context.getTopicMap().getOrDefault(topic, Collections.emptySet()).parallelStream().forEach(s -> {
            try {
                s.sendMessage(new TextMessage(mapper.writeValueAsString(sendMessageEntity)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
