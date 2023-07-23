package com.example.java_websocket_demo.web_socket.action_handler;

import com.example.java_websocket_demo.web_socket.entity.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Service
public abstract class AbstractActionHandler<E> {
    protected ObjectMapper mapper;
    protected Context context;

    @Autowired
    public AbstractActionHandler(ObjectMapper objectMapper, Context context) {
        this.mapper = objectMapper;
        this.context = context;
    }

    protected abstract boolean preCheck(WebSocketSession session) throws Exception;

    protected abstract E convert(String data) throws Exception;

    protected abstract void process_inner(WebSocketSession session, E data) throws Exception;

    public void process(WebSocketSession session, String data) throws Exception {
        if (!preCheck(session)) {
            log.info("filter id: {} status: {}, action: {}", session.getId(), context.getUserStateMap().get(session.getId()).name(), this.getClass().getName());
            return;
        }
        E val = convert(data);
        process_inner(session, val);
    }
}
