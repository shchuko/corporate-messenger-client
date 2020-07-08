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
public class AddChatMembersRequestDTO {
    private String chatName;

    private Set<String> members;

    public AddChatMembersRequestDTO(String chatName, Set<String> members) {
        this.chatName = chatName;
        this.members = members;
    }

    public AddChatMembersRequestDTO() {
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public Set<String> getMembers() {
        return members;
    }

    public void setMembers(Set<String> members) {
        this.members = members;
    }
}
