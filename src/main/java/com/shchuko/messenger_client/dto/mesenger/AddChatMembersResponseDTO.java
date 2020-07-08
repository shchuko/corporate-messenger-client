package com.shchuko.messenger_client.dto.mesenger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author shchuko
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AddChatMembersResponseDTO {
    private String statusMessage;

    public enum StatusTemplates {
        NOTHING_TO_ADD, SUCCESSFUL
    }

    public AddChatMembersResponseDTO() {
    }

    public AddChatMembersResponseDTO(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
