package com.shchuko.messenger_client.dto.mesenger;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author shchuko
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SendMessageRequestDTO {
    private String chatName;

    private String messageContent;

    public SendMessageRequestDTO() {
    }

    public SendMessageRequestDTO(String chatName, String messageContent) {
        this.chatName = chatName;
        this.messageContent = messageContent;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
}
