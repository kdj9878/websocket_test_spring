package com.example.demo.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MessageDto {
    private String userName;
    private String content;
    private String type;
    private String sessionId;
}
