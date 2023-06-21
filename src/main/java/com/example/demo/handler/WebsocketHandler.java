package com.example.demo.handler;

import com.example.demo.dto.MessageDto;
import com.example.demo.util.data.impl.ImgUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class WebsocketHandler extends TextWebSocketHandler {

    private static Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private static Map<String, Long[]> binaryDataMap = new ConcurrentHashMap<>();
    private static Map<String, String[]> fileAttributeMap = new HashMap<>();
    private final ObjectMapper objectMapper;
    private final ImgUtil imgUtil;

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
        String sessionId = session.getId();
        MessageDto dto = objectMapper.readValue(message.getPayload(), MessageDto.class);
        String type = dto.getType();
        if(type.equals("message")){
            sessions.get(roomId).stream()
                    .filter( roomSession -> !roomSession.getId().equals(sessionId))
                    .forEach( roomSession -> {
                        try {
                            roomSession.sendMessage(message);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        else if(type.indexOf("image") > -1){
            binaryDataMap.put(session.getId(), new Long[]{0L, dto.getSize()});
            imgUtil.checkAndCreateDirectory(roomId);
            fileAttributeMap.put(sessionId, new String[]{roomId.toString(), dto.getContent(), type.split("/")[1]});
        }
        else if(type.indexOf("video") > -1){
            log.info("비디오 전송예정");
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        String sessionId = session.getId();
        if(binaryDataMap.get(sessionId) != null){
            Long[] sendState =  binaryDataMap.get(sessionId);
            String[] fileAttribute = fileAttributeMap.get(sessionId);
            String roomId = fileAttribute[0];
            String fileName = fileAttribute[1];
            String fileType = fileAttribute[2];
            Long currentSeq = sendState[0];
            Long finalSeq = sendState[1];

            //데이터 저장 작업
            imgUtil.saveChunkFile(roomId, fileName, message.getPayload().array(), currentSeq);

            currentSeq++;
            sendState[0] = currentSeq;
            binaryDataMap.put(sessionId, sendState);

            //데이터 저장 작업 완료 후
            if(currentSeq.equals(finalSeq)){
                imgUtil.saveFinalChunkFile(roomId, fileName, fileType, finalSeq);
            }
        }
    }

    private String parseRoomId(WebSocketSession session){
        Assert.notNull(session.getUri().getRawQuery(), "roomId is null");
        return session.getUri().getRawQuery().toString().split("=")[1];
    }
}
