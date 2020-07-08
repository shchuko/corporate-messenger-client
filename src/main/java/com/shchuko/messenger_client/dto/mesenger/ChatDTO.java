package com.shchuko.messenger_client.dto.mesenger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * @author shchuko
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ChatDTO {
    private String chatName;
    private Set<String> members;

    public ChatDTO() {
    }

    public String getChatName() {
        return chatName;
    }

    public Set<String> getMembers() {
        return members;
    }

    public ChatDTO(String chatName, Set<String> members) {
        this.chatName = chatName;
        this.members = members;
    }
}
