package com.shchuko.messenger_client.dto.mesenger;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author shchuko
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GetMessagesRequestDTO {
    private String action;

    private String chatName;

    private Long timestamp;

    public enum SupportedActions {
        GET_MESSAGES_BEFORE_TIMESTAMP,
        GET_MESSAGES_AFTER_TIMESTAMP
    }

    public GetMessagesRequestDTO() {
    }

    public GetMessagesRequestDTO(String action, String chatName, Long timestamp) {
        this.action = action;
        this.chatName = chatName;
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
