package com.shchuko.messenger_client.dto.mesenger;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author shchuko
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GetUserChatsResponseDTO {
    private List<ChatDTO> userChats;

    public GetUserChatsResponseDTO() {
    }

    public GetUserChatsResponseDTO(List<ChatDTO> userChats) {
        this.userChats = userChats;
    }

    public List<ChatDTO> getUserChats() {
        return userChats;
    }

    public void setUserChats(List<ChatDTO> userChats) {
        this.userChats = userChats;
    }
}
