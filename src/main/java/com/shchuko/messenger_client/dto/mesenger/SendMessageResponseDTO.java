package com.shchuko.messenger_client.dto.mesenger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author shchuko
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SendMessageResponseDTO {
    private Long messageReceivedOn;

    public SendMessageResponseDTO() {
    }

    public SendMessageResponseDTO(Long messageReceivedOn) {
        this.messageReceivedOn = messageReceivedOn;
    }

    public Long getMessageReceivedOn() {
        return messageReceivedOn;
    }

    public void setMessageReceivedOn(Long messageReceivedOn) {
        this.messageReceivedOn = messageReceivedOn;
    }
}
