package com.example.demo.handler;

import com.example.demo.dto.MessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class WebsocketHandler extends TextWebSocketHandler {

    private static Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String roomId = parseRoomId(session);
        if(sessions.get(roomId) == null){
            Set<WebSocketSession> sessionSet = Collections.synchronizedSet(new HashSet<>());
            sessionSet.add(session);
            sessions.put(roomId, sessionSet);
        }
        else{
            Set<WebSocketSession> roomSessions = sessions.get(roomId);
            roomSessions.add(session);
            sessions.put(roomId, roomSessions);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = parseRoomId(session);
        Set<WebSocketSession> roomSessions = sessions.get(roomId);
        int currentRoomUser = roomSessions.size();
        boolean deleteFlag = currentRoomUser == 1 ? true : false;
        for(WebSocketSession client : roomSessions){
            if(client.getId().equals(session.getId())){
                roomSessions.remove(session);
            }
        }
        if(deleteFlag){
            sessions.remove(roomId);
        }
        else{
            sessions.put(roomId, roomSessions);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String roomId = parseRoomId(session);
        MessageDto dto = objectMapper.readValue(message.getPayload(), MessageDto.class);
        String type = dto.getType();
        if(type.equals("message")){
            sessions.get(roomId).stream()
                    .filter( roomSession -> !roomSession.getId().equals(session.getId()))
                    .forEach( roomSession -> {
                        try {
                            roomSession.sendMessage(message);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        else if(type.indexOf("image") > -1){

        }
        else if(type.indexOf("video") > -1){

        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {

    }

    private String parseRoomId(WebSocketSession session){
        Assert.notNull(session.getUri().getRawQuery(), "roomId is null");
        return session.getUri().getRawQuery().toString().split("=")[1];
    }
}
